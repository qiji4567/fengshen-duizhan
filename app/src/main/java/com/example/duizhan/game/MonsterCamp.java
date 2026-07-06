package com.example.duizhan.game;

import com.example.duizhan.game.text.GameTextKey;

public final class MonsterCamp {
    public enum BuffType {
        NONE,
        RED,
        BLUE,
        SPEED,
        SHIELD
    }

    public final float x;
    public final float y;
    public final GameTextKey nameKey;
    public final String shortLabel;
    public final BuffType buffType;
    public final float[][] guardOffsets;

    public MonsterCamp(float x, float y, GameTextKey nameKey, String shortLabel,
                       BuffType buffType, float[][] guardOffsets) {
        this.x = x;
        this.y = y;
        this.nameKey = nameKey;
        this.shortLabel = shortLabel;
        this.buffType = buffType;
        this.guardOffsets = guardOffsets;
    }

    public static final MonsterCamp[] ALL = {
            new MonsterCamp(2240f, 1852f, GameTextKey.MONSTER_BLACK_WIND, "黑风", BuffType.RED,
                    new float[][]{{-82f, 62f}, {86f, -58f}}),
            new MonsterCamp(4960f, 1996f, GameTextKey.MONSTER_DRAGON_TURTLE, "龙龟", BuffType.BLUE,
                    new float[][]{{-88f, -56f}, {84f, 60f}}),
            new MonsterCamp(3600f, 5748f, GameTextKey.MONSTER_SAND, "流沙", BuffType.NONE,
                    new float[][]{{-74f, 48f}, {78f, -44f}}),
            new MonsterCamp(960f, 2470f, GameTextKey.MONSTER_KUNLUN, "昆仑", BuffType.SPEED,
                    new float[][]{{68f, 52f}, {-64f, -46f}}),
            new MonsterCamp(1240f, 5272f, GameTextKey.MONSTER_FLAME, "火焰", BuffType.RED,
                    new float[][]{{72f, -40f}, {-70f, 44f}}),
            new MonsterCamp(2760f, 1996f, GameTextKey.MONSTER_PEACH, "蟠桃", BuffType.SHIELD,
                    new float[][]{{-66f, 54f}, {70f, -50f}}),
            new MonsterCamp(5880f, 4656f, GameTextKey.MONSTER_NINE_TAIL, "九尾", BuffType.NONE,
                    new float[][]{{-76f, 42f}, {80f, -48f}}),
            new MonsterCamp(6400f, 2470f, GameTextKey.MONSTER_ROC, "大鹏", BuffType.SPEED,
                    new float[][]{{-72f, 56f}, {74f, -52f}}),
            new MonsterCamp(1960f, 5606f, GameTextKey.MONSTER_UNDERWORLD, "地府", BuffType.SHIELD,
                    new float[][]{{64f, -46f}, {-68f, 50f}}),
            new MonsterCamp(4100f, 5606f, GameTextKey.MONSTER_TAISHAN, "泰山", BuffType.NONE,
                    new float[][]{{-70f, 40f}, {72f, -42f}}),
            new MonsterCamp(5360f, 4228f, GameTextKey.MONSTER_DRAGON_PALACE, "龙宫", BuffType.BLUE,
                    new float[][]{{-74f, -44f}, {76f, 48f}}),
            new MonsterCamp(3960f, 3610f, GameTextKey.MONSTER_PANGU, "盘古", BuffType.RED,
                    new float[][]{{-80f, 58f}, {82f, -54f}, {0f, 72f}}),
    };
}
