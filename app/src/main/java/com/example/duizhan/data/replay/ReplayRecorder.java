package com.example.duizhan.data.replay;

import com.example.duizhan.game.GameSnapshot;
import com.example.duizhan.game.Team;
import com.example.duizhan.game.UnitKind;

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

    /** Compact snapshot for durable storage after battle ends. */
    public synchronized String exportForPersistence() {
        return buildCompactCopy().toJsonString();
    }

    private ReplayData buildCompactCopy() {
        ReplayData compact = new ReplayData();
        compact.version = data.version;
        compact.intervalMs = data.intervalMs;
        if (data.frames.isEmpty()) {
            return compact;
        }
        int maxFrames = 3600;
        int step = 1;
        if (data.frames.size() > maxFrames) {
            step = (int) Math.ceil(data.frames.size() / (double) maxFrames);
            compact.intervalMs = data.intervalMs * step;
        }
        for (int index = 0; index < data.frames.size(); index += step) {
            boolean keepVisuals = index + step >= data.frames.size();
            compact.frames.add(copyFrameForPersist(data.frames.get(index), keepVisuals));
        }
        ReplayFrame lastSource = data.frames.get(data.frames.size() - 1);
        ReplayFrame lastCopied = compact.frames.isEmpty()
                ? null
                : compact.frames.get(compact.frames.size() - 1);
        if (lastCopied == null || lastCopied.timeMs != lastSource.timeMs) {
            compact.frames.add(copyFrameForPersist(lastSource, true));
        }
        return compact;
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
