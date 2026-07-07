package com.example.duizhan.model;

import android.content.Context;

import com.example.duizhan.R;
import com.example.duizhan.data.BattleRepository;
import com.example.duizhan.data.BattleSummary;
import com.example.duizhan.data.GameRepositoryProvider;
import com.example.duizhan.data.replay.ReplayRecorder;
import com.example.duizhan.game.GameEngine;
import com.example.duizhan.game.GameListener;
import com.example.duizhan.game.BattleDifficulty;
import com.example.duizhan.game.GameSnapshot;
import com.example.duizhan.game.HeroType;
import com.example.duizhan.game.ItemType;
import com.example.duizhan.game.SkillSlot;
import com.example.duizhan.game.Team;

public class BattleModel implements GameListener {
    public interface Callback {
        void onGameChanged(GameSnapshot snapshot);

        void onGameFinished(Team winner, long durationMs, long recordId);
    }

    public interface ReplaySaveCallback {
        void onReplaySaved(long recordId);
    }

    private final BattleRepository repository;
    private final ReplayRecorder replayRecorder = new ReplayRecorder();
    private final ReplayRecorder autoReplayRecorder = new ReplayRecorder();
    private final Context context;
    private Callback callback;
    private GameEngine engine;
    private HeroType blueHero;
    private HeroType redHero;
    private boolean saved;

    public BattleModel(Context context) {
        this.context = context.getApplicationContext();
        repository = GameRepositoryProvider.get(context);
    }

    public void createBattle(HeroType blueHero, HeroType redHero, BattleDifficulty difficulty, Callback callback) {
        this.blueHero = blueHero;
        this.redHero = redHero;
        this.callback = callback;
        engine = new GameEngine(blueHero, redHero, difficulty, this, new AndroidGameTextProvider(context));
    }

    public void start() {
        saved = false;
        replayRecorder.reset();
        autoReplayRecorder.reset();
        if (engine != null) {
            engine.start();
        }
    }

    public void stop() {
        if (engine != null) {
            engine.stop();
        }
    }

    public void setPaused(boolean paused) {
        if (engine != null) {
            engine.setPaused(paused);
        }
    }

    public boolean isPaused() {
        return engine != null && engine.isPaused();
    }

    public void move(float x, float y) {
        if (engine != null) {
            engine.setBlueMove(x, y);
        }
    }

    public void castSkill(SkillSlot skillSlot) {
        if (engine != null) {
            engine.castBlueSkill(skillSlot);
        }
    }

    public void basicAttack() {
        if (engine != null) {
            engine.blueBasicAttack();
        }
    }

    public void castTalent() {
        if (engine != null) {
            engine.castBlueTalent();
        }
    }

    public String recall() {
        if (engine == null) {
            return context.getString(R.string.battle_not_started);
        }
        return engine.recallBlueHero();
    }

    public String buyItem(ItemType itemType) {
        if (engine == null) {
            return context.getString(R.string.battle_not_started);
        }
        return engine.buyBlueItem(itemType);
    }

    public com.example.duizhan.game.guide.BuildPlan getBuildPlan() {
        if (engine == null) {
            return com.example.duizhan.game.guide.BuildGuideResolver.buildPlan(
                    blueHero, null, null, null, null, null, 0);
        }
        return engine.getBlueBuildPlan();
    }

    public String applyBuildPlan(boolean replaceAll) {
        if (engine == null) {
            return context.getString(R.string.battle_not_started);
        }
        return engine.applyBlueBuildPlan(replaceAll);
    }

    public void close() {
        stop();
        callback = null;
    }

    public boolean isRecording() {
        return replayRecorder.isRecording();
    }

    public void setRecording(boolean recording) {
        replayRecorder.setRecording(recording);
    }

    public int getReplayFrameCount() {
        return replayRecorder.frameCount();
    }

    public void saveReplayNowAsync(ReplaySaveCallback callback) {
        if (engine == null) {
            notifyReplaySave(callback, -1L);
            return;
        }
        captureFinalSnapshot(replayRecorder);
        if (replayRecorder.frameCount() == 0) {
            notifyReplaySave(callback, -1L);
            return;
        }
        BattleSummary summary = engine.buildOngoingSummary();
        long durationMs = engine.getElapsedMs();
        repository.saveRecordAsync(summary, durationMs, replayRecorder, id ->
                notifyReplaySave(callback, id));
    }

    private void notifyReplaySave(ReplaySaveCallback callback, long recordId) {
        com.example.duizhan.util.AppAsync.runOnMain(() -> {
            if (callback != null) {
                callback.onReplaySaved(recordId);
            }
        });
    }

    @Override
    public void onGameChanged(GameSnapshot snapshot) {
        if (replayRecorder.isRecording()) {
            replayRecorder.onSnapshot(snapshot);
        }
        autoReplayRecorder.onSnapshot(snapshot);
        if (callback != null) {
            callback.onGameChanged(snapshot);
        }
    }

    @Override
    public void onGameFinished(Team winner, long durationMs, BattleSummary summary) {
        if (!saved) {
            saved = true;
            captureFinalSnapshot(autoReplayRecorder);
            repository.saveRecordAsync(summary, durationMs, autoReplayRecorder, id ->
                    com.example.duizhan.util.AppAsync.runOnMain(() -> {
                        if (callback != null) {
                            callback.onGameFinished(winner, durationMs, id);
                        }
                    }));
            return;
        }
        if (callback != null) {
            callback.onGameFinished(winner, durationMs, -1L);
        }
    }

    private void captureFinalSnapshot(ReplayRecorder recorder) {
        if (engine != null) {
            recorder.captureSnapshot(engine.snapshot());
        }
    }
}
