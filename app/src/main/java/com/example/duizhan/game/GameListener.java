package com.example.duizhan.game;

import com.example.duizhan.data.BattleSummary;

public interface GameListener {
    void onGameChanged(GameSnapshot snapshot);

    void onGameFinished(Team winner, long durationMs, BattleSummary summary);
}
