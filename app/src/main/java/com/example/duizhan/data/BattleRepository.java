package com.example.duizhan.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import com.example.duizhan.data.replay.ReplayAnalyzer;
import com.example.duizhan.data.replay.ReplayData;
import com.example.duizhan.data.replay.ReplayPlayer;
import com.example.duizhan.data.replay.ReplayRecorder;
import com.example.duizhan.data.replay.ReplayStorage;
import com.example.duizhan.util.AppAsync;

import android.util.Base64;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BattleRepository {
    private static final String TAG = "BattleRepository";
    private static final int INLINE_REPLAY_GZIP_LIMIT_BYTES = 900_000;
    private static final int SQLITE_BUSY_RETRIES = 3;

    private static final String[] RECORD_LIST_COLUMNS = {
            "id",
            "winner_team",
            "winner_label",
            "blue_hero_key",
            "blue_hero_label",
            "red_hero_key",
            "red_hero_label",
            "duration_ms",
            "blue_kills",
            "red_kills",
            "blue_gold",
            "red_gold",
            "blue_level",
            "red_level",
            "blue_build",
            "red_build",
            "player_won",
            "created_at"
    };

    private final Context appContext;
    private final GameDbHelper dbHelper;

    public BattleRepository(Context context) {
        appContext = context.getApplicationContext();
        dbHelper = GameDbHelper.getInstance(context);
    }

    public void warmUp() {
        AppAsync.runOnIo(() -> {
            try {
                openWritableDatabase();
            } catch (RuntimeException exception) {
                Log.e(TAG, "Database warm-up failed", exception);
            }
        });
    }

    public void saveRecord(BattleSummary summary, long durationMs, DbCallback<Long> callback) {
        AppAsync.runOnIo(() -> notifyResult(callback, insertMetadata(summary, durationMs)));
    }

    public void saveRecordAsync(BattleSummary summary, long durationMs, ReplayRecorder replayRecorder,
                              DbCallback<Long> callback) {
        AppAsync.runOnIo(() -> notifyResult(callback, saveRecordSafely(summary, durationMs, replayRecorder)));
    }

    public void saveRecordWithReplayJsonAsync(BattleSummary summary, long durationMs, String replayJson,
                                              DbCallback<Long> callback) {
        AppAsync.runOnIo(() -> notifyResult(callback, saveRecordSafely(summary, durationMs, replayJson)));
    }

    public void getRecordCount(DbCallback<Integer> callback) {
        AppAsync.runOnIo(() -> {
            int count = 0;
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM battle_record", null);
            try {
                if (cursor.moveToFirst()) {
                    count = cursor.getInt(0);
                }
            } finally {
                cursor.close();
            }
            notifyResult(callback, count);
        });
    }

    private long saveRecordSafely(BattleSummary summary, long durationMs, ReplayRecorder replayRecorder) {
        long id = insertMetadata(summary, durationMs);
        if (id <= 0L) {
            return id;
        }
        if (!attachReplayIfPossible(id, replayRecorder)) {
            deleteRecordQuietly(id);
            return -2L;
        }
        return id;
    }

    private long saveRecordSafely(BattleSummary summary, long durationMs, String replayJson) {
        long id = insertMetadata(summary, durationMs);
        if (id <= 0L) {
            return id;
        }
        if (!attachReplayJsonIfPossible(id, replayJson)) {
            deleteRecordQuietly(id);
            return -2L;
        }
        return id;
    }

    private long insertMetadata(BattleSummary summary, long durationMs) {
        if (summary == null) {
            Log.w(TAG, "Skip save: summary is null");
            return -1L;
        }
        for (int attempt = 0; attempt < SQLITE_BUSY_RETRIES; attempt++) {
            try {
                SQLiteDatabase db = openWritableDatabase();
                long id = db.insert("battle_record", null, buildContentValues(summary, durationMs));
                if (id <= 0L) {
                    Log.e(TAG, "Insert battle_record failed");
                }
                return id;
            } catch (SQLiteException exception) {
                Log.w(TAG, "Insert attempt " + (attempt + 1) + " failed", exception);
                sleepQuietly(40L * (attempt + 1));
            } catch (RuntimeException exception) {
                Log.e(TAG, "Insert battle_record crashed", exception);
                return -1L;
            }
        }
        return -1L;
    }

    private boolean attachReplayIfPossible(long recordId, ReplayRecorder replayRecorder) {
        if (recordId <= 0L || replayRecorder == null || replayRecorder.frameCount() == 0) {
            Log.w(TAG, "Replay attach skipped for record " + recordId + ": no replay frames");
            return false;
        }
        boolean fileWritten;
        try {
            fileWritten = ReplayStorage.writeReplayFile(appContext, recordId, replayRecorder);
        } catch (OutOfMemoryError error) {
            Log.e(TAG, "Replay streaming write ran out of memory for record " + recordId, error);
            fileWritten = false;
        } catch (RuntimeException exception) {
            Log.e(TAG, "Replay streaming write crashed for record " + recordId, exception);
            fileWritten = false;
        }
        if (!fileWritten) {
            Log.e(TAG, "Replay file streaming write failed for record " + recordId);
            return false;
        }
        boolean updated = updateReplayReference(recordId, ReplayStorage.FILE_PREFIX + recordId);
        if (updated) {
            Log.i(TAG, "Replay saved for record " + recordId
                    + " (" + replayRecorder.frameCount() + " frames, file storage)");
        }
        return updated;
    }

    private boolean attachReplayJsonIfPossible(long recordId, String replayJson) {
        if (recordId <= 0L || replayJson == null || replayJson.length() == 0) {
            Log.w(TAG, "Replay attach skipped for record " + recordId + ": empty replay json");
            return false;
        }
        try {
            byte[] gzipBytes = ReplayStorage.gzipUtf8(replayJson);
            String storageRef;
            if (gzipBytes.length <= INLINE_REPLAY_GZIP_LIMIT_BYTES) {
                storageRef = ReplayStorage.GZIP_PREFIX
                        + Base64.encodeToString(gzipBytes, Base64.NO_WRAP);
            } else if (ReplayStorage.writeFile(appContext, recordId, gzipBytes)) {
                storageRef = ReplayStorage.FILE_PREFIX + recordId;
            } else {
                Log.e(TAG, "Replay file write failed for record " + recordId);
                return false;
            }
            if (!updateReplayReference(recordId, storageRef)) {
                return false;
            }
            Log.i(TAG, "Replay saved for record " + recordId
                    + " (" + replayJson.length() + " chars, " + gzipBytes.length + " gzip bytes)");
            return true;
        } catch (RuntimeException | IOException exception) {
            Log.w(TAG, "Replay attach failed for record " + recordId, exception);
            return false;
        }
    }

    private boolean updateReplayReference(long recordId, String storageRef) {
        ContentValues update = new ContentValues();
        update.put("replay_json", storageRef);
        int updated = openWritableDatabase().update("battle_record", update, "id=?",
                new String[]{String.valueOf(recordId)});
        if (updated <= 0) {
            Log.e(TAG, "Replay database update missed record " + recordId);
            return false;
        }
        return true;
    }

    private SQLiteDatabase openWritableDatabase() {
        if (dbHelper == null) {
            throw new IllegalStateException("GameDbHelper unavailable");
        }
        return dbHelper.getWritableDatabase();
    }

    private void deleteRecordQuietly(long recordId) {
        if (recordId <= 0L) {
            return;
        }
        try {
            openWritableDatabase().delete("battle_record", "id=?",
                    new String[]{String.valueOf(recordId)});
        } catch (RuntimeException exception) {
            Log.w(TAG, "Failed to remove incomplete replay record " + recordId, exception);
        }
    }

    private void sleepQuietly(long delayMs) {
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
        }
    }

    public void getLastRecord(DbCallback<BattleRecord> callback) {
        getAllRecords(list -> notifyResult(callback, list.isEmpty() ? null : toLegacyRecord(list.get(0))));
    }

    public void getAllRecords(DbCallback<List<BattleRecordDetail>> callback) {
        AppAsync.runOnIo(() -> {
            List<BattleRecordDetail> records;
            try {
                records = queryAllRecords();
            } catch (RuntimeException exception) {
                Log.e(TAG, "Load history failed", exception);
                records = new ArrayList<>();
            }
            notifyResult(callback, records);
        });
    }

    public void getRecordById(long id, DbCallback<BattleRecordDetail> callback) {
        AppAsync.runOnIo(() -> notifyResult(callback, queryRecordById(id)));
    }

    public void loadStatsDashboard(DbCallback<StatsDashboard> callback) {
        AppAsync.runOnIo(() -> {
            StatsDashboard dashboard = new StatsDashboard();
            dashboard.playerStats = queryPlayerStats();
            dashboard.heroStats.addAll(queryHeroStats());
            notifyResult(callback, dashboard);
        });
    }

    public void loadBattleDetail(long id, DbCallback<BattleDetailPayload> callback) {
        AppAsync.runOnIo(() -> {
            BattleRecordDetail record = queryRecordById(id);
            if (record == null) {
                notifyResult(callback, null);
                return;
            }
            BattleDetailPayload payload = new BattleDetailPayload();
            payload.record = record;
            payload.replayFrameCount = ReplayStorage.countStoredFrames(appContext, record.replayJson);
            payload.hasReplay = payload.replayFrameCount > 0;
            payload.analyzer = new ReplayAnalyzer();
            payload.analyzer.frameCount = payload.replayFrameCount;
            notifyResult(callback, payload);
        });
    }

    public void loadReplaySession(long id, DbCallback<ReplaySession> callback) {
        AppAsync.runOnIo(() -> {
            BattleRecordDetail record = queryRecordById(id);
            if (record == null || record.replayJson == null || record.replayJson.length() == 0) {
                notifyResult(callback, null);
                return;
            }
            ReplaySession session = new ReplaySession();
            session.record = record;
            session.replayData = ReplayData.fromJson(decodeReplayJson(record.replayJson));
            session.replayPlayer = new ReplayPlayer(session.replayData);
            notifyResult(callback, session);
        });
    }

    public void getPlayerStats(DbCallback<PlayerStats> callback) {
        AppAsync.runOnIo(() -> notifyResult(callback, queryPlayerStats()));
    }

    public void getHeroStats(DbCallback<List<HeroBattleStats>> callback) {
        AppAsync.runOnIo(() -> notifyResult(callback, queryHeroStats()));
    }

    public void close() {
        // Shared singleton database; keep open for app lifetime.
    }

    private ContentValues buildContentValues(BattleSummary summary, long durationMs) {
        ContentValues values = new ContentValues();
        values.put("winner_team", nullToEmpty(summary.winnerTeam));
        values.put("winner_label", nullToEmpty(summary.winnerLabel));
        values.put("blue_hero_key", nullToEmpty(summary.blueHeroKey));
        values.put("blue_hero_label", nullToEmpty(summary.blueHeroLabel));
        values.put("red_hero_key", nullToEmpty(summary.redHeroKey));
        values.put("red_hero_label", nullToEmpty(summary.redHeroLabel));
        values.put("duration_ms", Math.max(0L, durationMs));
        values.put("blue_kills", summary.blueKills);
        values.put("red_kills", summary.redKills);
        values.put("blue_gold", summary.blueGold);
        values.put("red_gold", summary.redGold);
        values.put("blue_level", summary.blueLevel);
        values.put("red_level", summary.redLevel);
        values.put("blue_build", nullToEmpty(summary.blueBuild));
        values.put("red_build", nullToEmpty(summary.redBuild));
        values.put("player_won", summary.playerWon ? 1 : 0);
        values.put("created_at", System.currentTimeMillis());
        return values;
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private List<BattleRecordDetail> queryAllRecords() {
        List<BattleRecordDetail> records = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("battle_record", RECORD_LIST_COLUMNS, null, null, null, null, "id DESC");
        try {
            while (cursor.moveToNext()) {
                records.add(readRecord(cursor));
            }
        } finally {
            cursor.close();
        }
        return records;
    }

    private BattleRecordDetail queryRecordById(long id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("battle_record", null, "id=?", new String[]{String.valueOf(id)},
                null, null, null);
        try {
            if (cursor.moveToFirst()) {
                return readRecord(cursor);
            }
        } finally {
            cursor.close();
        }
        return null;
    }

    private PlayerStats queryPlayerStats() {
        PlayerStats stats = new PlayerStats();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*), SUM(player_won), AVG(duration_ms), SUM(blue_kills), SUM(red_kills) FROM battle_record",
                null);
        try {
            if (cursor.moveToFirst()) {
                stats.totalBattles = cursor.getInt(0);
                stats.wins = cursor.getInt(1);
                stats.avgDurationMs = Math.round(cursor.getDouble(2));
                stats.totalKills = cursor.getInt(3);
                stats.totalDeaths = cursor.getInt(4);
            }
        } finally {
            cursor.close();
        }
        stats.losses = Math.max(0, stats.totalBattles - stats.wins);
        stats.winRate = stats.totalBattles == 0 ? 0f : stats.wins * 100f / stats.totalBattles;
        stats.avgKda = stats.totalDeaths == 0
                ? stats.totalKills
                : (float) stats.totalKills / stats.totalDeaths;
        return stats;
    }

    private List<HeroBattleStats> queryHeroStats() {
        List<HeroBattleStats> statsList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT blue_hero_key, blue_hero_label, COUNT(*), SUM(player_won), AVG(blue_kills) " +
                        "FROM battle_record GROUP BY blue_hero_key, blue_hero_label ORDER BY COUNT(*) DESC",
                null);
        try {
            while (cursor.moveToNext()) {
                HeroBattleStats stats = new HeroBattleStats();
                stats.heroKey = cursor.getString(0);
                stats.heroLabel = cursor.getString(1);
                stats.battles = cursor.getInt(2);
                stats.wins = cursor.getInt(3);
                stats.avgKills = (float) cursor.getDouble(4);
                stats.winRate = stats.battles == 0 ? 0f : stats.wins * 100f / stats.battles;
                statsList.add(stats);
            }
        } finally {
            cursor.close();
        }
        return statsList;
    }

    private <T> void notifyResult(DbCallback<T> callback, T data) {
        if (callback == null) {
            return;
        }
        AppAsync.runOnMain(() -> callback.onResult(data));
    }

    private BattleRecordDetail readRecord(Cursor cursor) {
        BattleRecordDetail record = new BattleRecordDetail();
        record.id = cursor.getLong(cursor.getColumnIndexOrThrow("id"));
        record.winnerTeam = cursor.getString(cursor.getColumnIndexOrThrow("winner_team"));
        record.winnerLabel = cursor.getString(cursor.getColumnIndexOrThrow("winner_label"));
        record.blueHeroKey = cursor.getString(cursor.getColumnIndexOrThrow("blue_hero_key"));
        record.blueHeroLabel = cursor.getString(cursor.getColumnIndexOrThrow("blue_hero_label"));
        record.redHeroKey = cursor.getString(cursor.getColumnIndexOrThrow("red_hero_key"));
        record.redHeroLabel = cursor.getString(cursor.getColumnIndexOrThrow("red_hero_label"));
        record.durationMs = cursor.getLong(cursor.getColumnIndexOrThrow("duration_ms"));
        record.blueKills = cursor.getInt(cursor.getColumnIndexOrThrow("blue_kills"));
        record.redKills = cursor.getInt(cursor.getColumnIndexOrThrow("red_kills"));
        record.blueGold = cursor.getInt(cursor.getColumnIndexOrThrow("blue_gold"));
        record.redGold = cursor.getInt(cursor.getColumnIndexOrThrow("red_gold"));
        record.blueLevel = cursor.getInt(cursor.getColumnIndexOrThrow("blue_level"));
        record.redLevel = cursor.getInt(cursor.getColumnIndexOrThrow("red_level"));
        record.blueBuild = cursor.getString(cursor.getColumnIndexOrThrow("blue_build"));
        record.redBuild = cursor.getString(cursor.getColumnIndexOrThrow("red_build"));
        record.playerWon = cursor.getInt(cursor.getColumnIndexOrThrow("player_won")) == 1;
        int replayIndex = cursor.getColumnIndex("replay_json");
        record.replayJson = replayIndex >= 0 ? cursor.getString(replayIndex) : null;
        record.createdAt = cursor.getLong(cursor.getColumnIndexOrThrow("created_at"));
        return record;
    }

    private String decodeReplayJson(String storedReplay) {
        return ReplayStorage.decodeStored(appContext, storedReplay);
    }

    public static BattleRecord toLegacyRecord(BattleRecordDetail detail) {
        BattleRecord record = new BattleRecord();
        record.id = detail.id;
        record.winner = detail.winnerLabel;
        record.blueHero = detail.blueHeroLabel;
        record.redHero = detail.redHeroLabel;
        record.durationMs = detail.durationMs;
        record.createdAt = detail.createdAt;
        return record;
    }
}
