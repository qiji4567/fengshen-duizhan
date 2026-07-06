package com.example.duizhan.mvp;

import android.content.Context;

import com.example.duizhan.game.BattleDifficulty;
import com.example.duizhan.game.GameSnapshot;
import com.example.duizhan.game.HeroType;
import com.example.duizhan.game.ItemType;
import com.example.duizhan.game.SkillSlot;
import com.example.duizhan.game.Team;
import com.example.duizhan.model.BattleModel;

public class BattlePresenter implements BattleContract.Presenter, BattleModel.Callback {
    private final BattleModel model;
    private BattleContract.View view;
    private HeroType blueHero;
    private HeroType redHero;

    public BattlePresenter(Context context) {
        model = new BattleModel(context);
    }

    @Override
    public void attach(BattleContract.View view) {
        this.view = view;
    }

    @Override
    public void setBattleSetup(HeroType blueHero, HeroType redHero, BattleDifficulty difficulty) {
        this.blueHero = blueHero;
        this.redHero = redHero;
        model.createBattle(blueHero, redHero, difficulty, this);
    }

    @Override
    public void detach() {
        view = null;
        model.close();
    }

    @Override
    public void start() {
        model.start();
    }

    @Override
    public void togglePause() {
        model.setPaused(!model.isPaused());
    }

    @Override
    public boolean isPaused() {
        return model.isPaused();
    }

    @Override
    public void move(float x, float y) {
        model.move(x, y);
    }

    @Override
    public void castSkill(SkillSlot skillSlot) {
        model.castSkill(skillSlot);
    }

    @Override
    public void basicAttack() {
        model.basicAttack();
    }

    @Override
    public void castTalent() {
        model.castTalent();
    }

    @Override
    public void recall() {
        model.recall();
    }

    @Override
    public void buyItem(ItemType itemType) {
        String message = model.buyItem(itemType);
        if (view != null) {
            view.showShopResult(message);
        }
    }

    @Override
    public com.example.duizhan.game.guide.BuildPlan getBuildPlan() {
        return model.getBuildPlan();
    }

    @Override
    public void applyBuildPlan(boolean replaceAll) {
        String message = model.applyBuildPlan(replaceAll);
        if (view != null) {
            view.showShopResult(message);
        }
    }

    @Override
    public void toggleRecording() {
        model.setRecording(!model.isRecording());
        if (view != null) {
            view.onRecordingStateChanged(model.isRecording(), model.getReplayFrameCount());
        }
    }

    @Override
    public boolean isRecording() {
        return model.isRecording();
    }

    @Override
    public int getReplayFrameCount() {
        return model.getReplayFrameCount();
    }

    @Override
    public void saveReplayNow() {
        model.saveReplayNowAsync(recordId -> {
            if (view != null) {
                view.onReplaySaved(recordId);
            }
        });
    }

    @Override
    public void onGameChanged(GameSnapshot snapshot) {
        if (view != null) {
            view.render(snapshot);
        }
    }

    @Override
    public void onGameFinished(Team winner, long durationMs, long recordId) {
        if (view != null) {
            view.showFinish(winner, durationMs, recordId);
        }
    }
}
