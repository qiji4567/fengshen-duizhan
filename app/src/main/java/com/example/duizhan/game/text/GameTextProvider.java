package com.example.duizhan.game.text;

public interface GameTextProvider {
    String get(GameTextKey key, Object... args);
}
