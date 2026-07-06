package com.example.duizhan.game.util;

public final class ProgressionRules {
    public static final int MAX_LEVEL = 18;

    private ProgressionRules() {
    }

    public static int nextExpForLevel(int level) {
        return 90 + level * 55 + level * level * 8;
    }
}
