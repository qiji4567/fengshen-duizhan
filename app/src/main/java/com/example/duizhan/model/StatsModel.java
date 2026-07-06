package com.example.duizhan.model;

import android.content.Context;

import com.example.duizhan.data.GameRepositoryProvider;
import com.example.duizhan.data.DbCallback;
import com.example.duizhan.data.StatsDashboard;

public class StatsModel {
    private final com.example.duizhan.data.BattleRepository repository;

    public StatsModel(Context context) {
        repository = GameRepositoryProvider.get(context);
    }

    public void loadDashboard(DbCallback<StatsDashboard> callback) {
        repository.loadStatsDashboard(callback);
    }

    public void close() {
    }
}
