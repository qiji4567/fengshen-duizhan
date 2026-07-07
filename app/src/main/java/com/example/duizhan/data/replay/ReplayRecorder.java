package com.example.duizhan.data.replay;

import com.example.duizhan.game.GameSnapshot;
import com.example.duizhan.game.Team;
import com.example.duizhan.game.UnitKind;

import org.json.JSONException;

import java.io.IOException;
import java.io.Writer;

public final class ReplayRecorder {
    private static final int MAX_FRAMES = 7200;
    private static final int SAMPLE_INTERVAL_MS = 50;
    private static final int MAX_PROJECTILES_PER_FRAME = 96;
    private static final int MAX_EFFECTS_PER_FRAME = 160;

    private final ReplayData data = new ReplayData();
    private long battleStartMs;
    private long lastSampleMs;
    private boolean recording = true;

    public void reset() {
        data.frames.clear();
        data.intervalMs = SAMPLE_INTERVAL_MS;
        battleStartMs = 0L;
        lastSampleMs = 0L;
        recording = true;
    }

    public void setRecording(boolean recording) {
        this.recording = recording;
    }

    public boolean isRecording() {
        return recording;
    }

    public int frameCount() {
        synchronized (this) {
            return data.frames.size();
        }
    }

    public void onSnapshot(GameSnapshot snapshot) {
        synchronized (this) {
            appendSnapshot(snapshot, false);
        }
    }

    private void appendSnapshot(GameSnapshot snapshot, boolean force) {
        if (snapshot == null) {
            return;
        }
        if (!force && !recording) {
            return;
        }
        long now = System.currentTimeMillis();
        if (battleStartMs == 0L) {
            battleStartMs = now;
            lastSampleMs = now - SAMPLE_INTERVAL_MS;
        }
        if (!force && now - lastSampleMs < SAMPLE_INTERVAL_MS && !snapshot.finished) {
            return;
        }
        lastSampleMs = now;
        ReplayFrame frame = new ReplayFrame();
        frame.timeMs = now - battleStartMs;
        frame.blueTowerHp = snapshot.blueTowerHp;
        frame.redTowerHp = snapshot.redTowerHp;
        frame.blueKills = snapshot.blueKills;
        frame.redKills = snapshot.redKills;
        frame.captureFrom(snapshot);
        trimExtras(frame);
        data.frames.add(frame);
        if (data.frames.size() > MAX_FRAMES) {
            data.frames.remove(0);
        }
    }

    public void captureSnapshot(GameSnapshot snapshot) {
        synchronized (this) {
            appendSnapshot(snapshot, true);
        }
    }

    public synchronized String toJsonString() {
        return data.toJsonString();
    }

    /** Thread-safe copy for async persistence while battle continues. */
    public synchronized String exportJsonSnapshot() {
        return data.toJsonString();
    }

    /** Full replay export. This keeps every sampled frame; do not downsample persisted battles. */
    public synchronized String exportForPersistence() {
        return data.toJsonString();
    }

    /** Streams the full replay without building one large JSON object in memory. */
    public synchronized void writeForPersistence(Writer writer) throws IOException {
        if (writer == null) {
            return;
        }
        writer.write("{\"version\":");
        writer.write(String.valueOf(data.version));
        writer.write(",\"intervalMs\":");
        writer.write(String.valueOf(data.intervalMs));
        writer.write(",\"frames\":[");
        for (int index = 0; index < data.frames.size(); index++) {
            if (index > 0) {
                writer.write(',');
            }
            try {
                writer.write(data.frames.get(index).toJson().toString());
            } catch (JSONException exception) {
                throw new IOException("Replay frame JSON failed at index " + index, exception);
            }
        }
        writer.write("]}");
    }

    /** Last-resort replay payload when a long battle is too large to compact in memory. */
    public synchronized String exportFinalSnapshotOnly() {
        ReplayData compact = new ReplayData();
        compact.version = data.version;
        compact.intervalMs = data.intervalMs;
        if (!data.frames.isEmpty()) {
            compact.frames.add(copyFrameForPersist(data.frames.get(data.frames.size() - 1), true));
        }
        return compact.toJsonString();
    }

    private ReplayFrame copyFrameForPersist(ReplayFrame source, boolean keepVisuals) {
        ReplayFrame frame = new ReplayFrame();
        frame.timeMs = source.timeMs;
        frame.blueX = source.blueX;
        frame.blueY = source.blueY;
        frame.blueHp = source.blueHp;
        frame.blueMaxHp = source.blueMaxHp;
        frame.redX = source.redX;
        frame.redY = source.redY;
        frame.redHp = source.redHp;
        frame.redMaxHp = source.redMaxHp;
        frame.blueTowerHp = source.blueTowerHp;
        frame.redTowerHp = source.redTowerHp;
        frame.blueKills = source.blueKills;
        frame.redKills = source.redKills;
        frame.entityWorldRecorded = source.entityWorldRecorded;
        frame.entities.addAll(source.entities);
        if (keepVisuals) {
            frame.projectiles.addAll(source.projectiles);
            frame.effects.addAll(source.effects);
        }
        return frame;
    }

    private void trimExtras(ReplayFrame frame) {
        if (frame.projectiles.size() > MAX_PROJECTILES_PER_FRAME) {
            int from = frame.projectiles.size() - MAX_PROJECTILES_PER_FRAME;
            frame.projectiles.subList(0, from).clear();
        }
        if (frame.effects.size() > MAX_EFFECTS_PER_FRAME) {
            int from = frame.effects.size() - MAX_EFFECTS_PER_FRAME;
            frame.effects.subList(0, from).clear();
        }
    }
}
