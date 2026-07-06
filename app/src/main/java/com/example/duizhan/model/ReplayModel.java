package com.example.duizhan.model;

import android.content.Context;

import com.example.duizhan.data.GameRepositoryProvider;
import com.example.duizhan.data.DbCallback;
import com.example.duizhan.data.ReplaySession;

public class ReplayModel {
    private final com.example.duizhan.data.BattleRepository repository;

    public ReplayModel(Context context) {
        repository = GameRepositoryProvider.get(context);
    }

    public void loadSession(long recordId, DbCallback<ReplaySession> callback) {
        repository.loadReplaySession(recordId, callback);
    }

    public void close() {
    }
}
