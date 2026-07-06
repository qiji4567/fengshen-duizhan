package com.example.duizhan.game;

public final class GameConfig {
    public static final float MAP_SCALE = 2f;

    public static final float WORLD_WIDTH = 7200f;
    public static final float WORLD_HEIGHT = 7600f;
    public static final float VISIBLE_WORLD_HEIGHT = 760f;
    public static final float LANE_Y = 3800f;
    public static final float BLUE_BASE_X = 340f;
    public static final float RED_BASE_X = 6860f;
    public static final float BLUE_HIGHLAND_TOWER_X = BLUE_BASE_X;
    public static final float BLUE_MIDDLE_TOWER_X = 1520f;
    public static final float BLUE_OUTER_TOWER_X = 2700f;
    public static final float RED_OUTER_TOWER_X = 4500f;
    public static final float RED_MIDDLE_TOWER_X = 5680f;
    public static final float RED_HIGHLAND_TOWER_X = RED_BASE_X;
    public static final float BLUE_HERO_SPAWN_X = 860f;
    public static final float RED_HERO_SPAWN_X = 6340f;
    public static final float HERO_SPAWN_LANE_OFFSET = 856f;
    public static final float FOUNTAIN_RADIUS = 520f;
    public static final float FOUNTAIN_REGEN_PER_MS = 0.08f;
    public static final float FOUNTAIN_EFFECT_INTERVAL = 0.65f;
    public static final float LEVEL_UP_HEAL_RATE = 0.18f;
    public static final float NEUTRAL_KILL_HEAL_RATE = 0.08f;
    public static final float BRUTE_KILL_HEAL_RATE = 0.05f;
    public static final float MINION_WAVE_X_OFFSET = 236f;
    public static final float MINION_WAVE_X_SPACING = 116f;
    public static final float MINION_WAVE_Y_SPACING = 180f;
    public static final float BRUTE_WAVE_X_OFFSET = 184f;
    public static final float BRUTE_WAVE_Y_OFFSET = 184f;

    public static final long FRAME_MS = 16L;
    public static final float MINION_WAVE_INTERVAL = 5f;
    public static final float BRUTE_WAVE_INTERVAL = 10f;
    public static final float MONSTER_RESPAWN_INTERVAL = 16f;
    public static final float RED_SHOP_INTERVAL = 6f;
    public static final float RECALL_CHANNEL_DURATION = 10f;

    public static final float HERO_HP_SCALE = 1.58f;
    public static final float HERO_DAMAGE_SCALE = 0.72f;
    public static final float RED_HERO_SPEED_SCALE = 0.94f;
    public static final float RED_EXP_RATE = 0.74f;
    public static final float RED_GOLD_RATE = 0.86f;
    public static final float MARKSMAN_MIN_RANGE = 390f;
    public static final float MAGE_MIN_RANGE = 330f;
    public static final float SUPPORT_MIN_RANGE = 310f;

    public static final float MONSTER_BLACK_WIND_X = 2240f;
    public static final float MONSTER_BLACK_WIND_Y = 1852f;
    public static final float MONSTER_SAND_X = 3600f;
    public static final float MONSTER_SAND_Y = 5748f;
    public static final float MONSTER_DRAGON_TURTLE_X = 4960f;
    public static final float MONSTER_DRAGON_TURTLE_Y = 1996f;

    public static final float MIN_X = 140f;
    public static final float MAX_X = WORLD_WIDTH - 100f;
    public static final float MIN_Y = 476f;
    public static final float MAX_Y = WORLD_HEIGHT - 160f;

    public static float fountainX(Team team) {
        return team == Team.BLUE ? BLUE_HERO_SPAWN_X : RED_HERO_SPAWN_X;
    }

    public static float fountainY(Team team) {
        return team == Team.BLUE
                ? LANE_Y + HERO_SPAWN_LANE_OFFSET
                : LANE_Y - HERO_SPAWN_LANE_OFFSET;
    }

    private GameConfig() {
    }
}
