package com.example.duizhan.model;

import android.content.Context;

import com.example.duizhan.data.BattleDetailPayload;
import com.example.duizhan.data.GameRepositoryProvider;
import com.example.duizhan.data.DbCallback;

public class DetailModel {
    private final com.example.duizhan.data.BattleRepository repository;

    public DetailModel(Context context) {
        repository = GameRepositoryProvider.get(context);
    }

    public void loadDetail(long recordId, DbCallback<BattleDetailPayload> callback) {
        repository.loadBattleDetail(recordId, callback);
    }

    public void close() {
    }
}
