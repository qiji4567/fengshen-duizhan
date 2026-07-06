package com.example.duizhan.data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class GameDbHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "duizhan.db";
    private static final int DB_VERSION = 4;
    private static volatile GameDbHelper instance;

    public static GameDbHelper getInstance(Context context) {
        if (context == null) {
            return null;
        }
        if (instance == null) {
            synchronized (GameDbHelper.class) {
                if (instance == null) {
                    instance = new GameDbHelper(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    private GameDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        db.enableWriteAheadLogging();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createBattleRecordTable(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            migrateToV2(db);
        }
        if (oldVersion < 3) {
            migrateToV3(db);
        }
        if (oldVersion < 4) {
            migrateToV4(db);
        }
    }

    /** v2: add replay_json without wiping existing rows. */
    private void migrateToV2(SQLiteDatabase db) {
        if (!tableExists(db, "battle_record")) {
            createBattleRecordTable(db);
            return;
        }
        ensureColumn(db, "battle_record", "replay_json", "TEXT");
    }

    /** v3: schema marker only; keep all battle_record rows. */
    private void migrateToV3(SQLiteDatabase db) {
        if (!tableExists(db, "battle_record")) {
            createBattleRecordTable(db);
        }
    }

    /** v4: repair missing columns without dropping data. */
    private void migrateToV4(SQLiteDatabase db) {
        if (!tableExists(db, "battle_record")) {
            createBattleRecordTable(db);
            return;
        }
        ensureColumn(db, "battle_record", "winner_team", "TEXT NOT NULL DEFAULT ''");
        ensureColumn(db, "battle_record", "winner_label", "TEXT NOT NULL DEFAULT ''");
        ensureColumn(db, "battle_record", "blue_hero_key", "TEXT NOT NULL DEFAULT ''");
        ensureColumn(db, "battle_record", "blue_hero_label", "TEXT NOT NULL DEFAULT ''");
        ensureColumn(db, "battle_record", "red_hero_key", "TEXT NOT NULL DEFAULT ''");
        ensureColumn(db, "battle_record", "red_hero_label", "TEXT NOT NULL DEFAULT ''");
        ensureColumn(db, "battle_record", "duration_ms", "INTEGER NOT NULL DEFAULT 0");
        ensureColumn(db, "battle_record", "blue_kills", "INTEGER NOT NULL DEFAULT 0");
        ensureColumn(db, "battle_record", "red_kills", "INTEGER NOT NULL DEFAULT 0");
        ensureColumn(db, "battle_record", "blue_gold", "INTEGER NOT NULL DEFAULT 0");
        ensureColumn(db, "battle_record", "red_gold", "INTEGER NOT NULL DEFAULT 0");
        ensureColumn(db, "battle_record", "blue_level", "INTEGER NOT NULL DEFAULT 1");
        ensureColumn(db, "battle_record", "red_level", "INTEGER NOT NULL DEFAULT 1");
        ensureColumn(db, "battle_record", "blue_build", "TEXT");
        ensureColumn(db, "battle_record", "red_build", "TEXT");
        ensureColumn(db, "battle_record", "player_won", "INTEGER NOT NULL DEFAULT 0");
        ensureColumn(db, "battle_record", "replay_json", "TEXT");
        ensureColumn(db, "battle_record", "created_at", "INTEGER NOT NULL DEFAULT 0");
    }

    private void ensureColumn(SQLiteDatabase db, String tableName, String columnName, String definition) {
        if (!columnExists(db, tableName, columnName)) {
            db.execSQL("ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + definition);
        }
    }

    private boolean tableExists(SQLiteDatabase db, String tableName) {
        Cursor cursor = db.rawQuery(
                "SELECT name FROM sqlite_master WHERE type='table' AND name=?",
                new String[]{tableName});
        try {
            return cursor.moveToFirst();
        } finally {
            cursor.close();
        }
    }

    private boolean columnExists(SQLiteDatabase db, String tableName, String columnName) {
        Cursor cursor = db.rawQuery("PRAGMA table_info(" + tableName + ")", null);
        try {
            while (cursor.moveToNext()) {
                if (columnName.equalsIgnoreCase(cursor.getString(1))) {
                    return true;
                }
            }
        } finally {
            cursor.close();
        }
        return false;
    }

    private void createBattleRecordTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE battle_record (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "winner_team TEXT NOT NULL," +
                "winner_label TEXT NOT NULL," +
                "blue_hero_key TEXT NOT NULL," +
                "blue_hero_label TEXT NOT NULL," +
                "red_hero_key TEXT NOT NULL," +
                "red_hero_label TEXT NOT NULL," +
                "duration_ms INTEGER NOT NULL," +
                "blue_kills INTEGER NOT NULL," +
                "red_kills INTEGER NOT NULL," +
                "blue_gold INTEGER NOT NULL," +
                "red_gold INTEGER NOT NULL," +
                "blue_level INTEGER NOT NULL," +
                "red_level INTEGER NOT NULL," +
                "blue_build TEXT," +
                "red_build TEXT," +
                "player_won INTEGER NOT NULL," +
                "replay_json TEXT," +
                "created_at INTEGER NOT NULL)");
    }
}
