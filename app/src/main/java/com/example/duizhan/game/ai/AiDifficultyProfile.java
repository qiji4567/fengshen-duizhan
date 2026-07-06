package com.example.duizhan.game.ai;

import com.example.duizhan.game.BattleDifficulty;

public final class AiDifficultyProfile {
    public final float speedScale;
    public final float expRate;
    public final float goldRate;
    public final float combatVisionRange;
    public final float heroFocusRange;
    public final float jungleFarmRange;
    public final float lanePressureRange;
    public final float retreatHpRate;
    public final float fountainStayHpRate;
    public final float skillCastRangeMultiplier;
    public final float skillAggression;
    public final boolean kiteWhenRanged;
    public final boolean jungleFarmEnabled;
    public final float shopInterval;
    public final float pushSpeedScale;
    public final float retreatMoveScale;
    public final boolean wanderWhenIdle;

    private AiDifficultyProfile(float speedScale,
                                float expRate,
                                float goldRate,
                                float combatVisionRange,
                                float heroFocusRange,
                                float jungleFarmRange,
                                float lanePressureRange,
                                float retreatHpRate,
                                float fountainStayHpRate,
                                float skillCastRangeMultiplier,
                                float skillAggression,
                                boolean kiteWhenRanged,
                                boolean jungleFarmEnabled,
                                float shopInterval,
                                float pushSpeedScale,
                                float retreatMoveScale,
                                boolean wanderWhenIdle) {
        this.speedScale = speedScale;
        this.expRate = expRate;
        this.goldRate = goldRate;
        this.combatVisionRange = combatVisionRange;
        this.heroFocusRange = heroFocusRange;
        this.jungleFarmRange = jungleFarmRange;
        this.lanePressureRange = lanePressureRange;
        this.retreatHpRate = retreatHpRate;
        this.fountainStayHpRate = fountainStayHpRate;
        this.skillCastRangeMultiplier = skillCastRangeMultiplier;
        this.skillAggression = skillAggression;
        this.kiteWhenRanged = kiteWhenRanged;
        this.jungleFarmEnabled = jungleFarmEnabled;
        this.shopInterval = shopInterval;
        this.pushSpeedScale = pushSpeedScale;
        this.retreatMoveScale = retreatMoveScale;
        this.wanderWhenIdle = wanderWhenIdle;
    }

    public static AiDifficultyProfile forDifficulty(BattleDifficulty difficulty) {
        if (difficulty == BattleDifficulty.NORMAL) {
            return normal();
        }
        if (difficulty == BattleDifficulty.HARD) {
            return hard();
        }
        return medium();
    }

    private static AiDifficultyProfile normal() {
        return new AiDifficultyProfile(
                0.76f, 0.52f, 0.64f,
                680f, 520f, 1200f, 420f,
                0.34f, 0.72f,
                0.72f, 0.58f,
                false, false,
                8.5f, 0.82f, 0.86f,
                true);
    }

    private static AiDifficultyProfile medium() {
        return new AiDifficultyProfile(
                0.94f, 0.74f, 0.86f,
                980f, 980f, 2600f, 640f,
                0.30f, 0.90f,
                1.0f, 1.0f,
                true, true,
                6f, 0.92f, 0.92f,
                false);
    }

    private static AiDifficultyProfile hard() {
        return new AiDifficultyProfile(
                1.02f, 0.92f, 0.96f,
                1180f, 1180f, 3200f, 520f,
                0.18f, 0.95f,
                1.22f, 1.32f,
                true, true,
                4.5f, 1.05f, 1.04f,
                false);
    }
}
