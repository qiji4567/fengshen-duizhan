package com.example.duizhan.mvp;

import com.example.duizhan.game.BattleDifficulty;
import com.example.duizhan.game.GameSnapshot;
import com.example.duizhan.game.HeroType;
import com.example.duizhan.game.ItemType;
import com.example.duizhan.game.SkillSlot;
import com.example.duizhan.game.Team;

public interface BattleContract {
    interface View extends BaseView {
        void render(GameSnapshot snapshot);

        void showFinish(Team winner, long durationMs, long recordId);

        void showShopResult(String message);

        void onRecordingStateChanged(boolean recording, int frameCount);

        void onReplaySaved(long recordId);
    }

    interface Presenter extends BasePresenter<View> {
        void setBattleSetup(HeroType blueHero, HeroType redHero, BattleDifficulty difficulty);

        void start();

        void togglePause();

        boolean isPaused();

        void move(float x, float y);

        void castSkill(SkillSlot skillSlot);

        void basicAttack();

        void castTalent();

        void recall();

        void buyItem(ItemType itemType);

        com.example.duizhan.game.guide.BuildPlan getBuildPlan();

        void applyBuildPlan(boolean replaceAll);

        void toggleRecording();

        boolean isRecording();

        int getReplayFrameCount();

        void saveReplayNow();
    }
}
