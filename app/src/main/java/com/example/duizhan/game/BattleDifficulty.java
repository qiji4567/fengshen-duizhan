package com.example.duizhan.game;

public enum BattleDifficulty {
    NORMAL,
    MEDIUM,
    HARD;

    public static BattleDifficulty fromName(String value, BattleDifficulty fallback) {
        if (value == null) {
            return fallback;
        }
        try {
            return BattleDifficulty.valueOf(value);
        } catch (IllegalArgumentException ignored) {
            return fallback;
        }
    }
}
