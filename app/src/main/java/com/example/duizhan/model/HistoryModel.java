package com.example.duizhan.model;

import android.content.Context;

import com.example.duizhan.data.BattleRecordDetail;
import com.example.duizhan.data.GameRepositoryProvider;
import com.example.duizhan.data.DbCallback;

import java.util.List;

public class HistoryModel {
    private final com.example.duizhan.data.BattleRepository repository;

    public HistoryModel(Context context) {
        repository = GameRepositoryProvider.get(context);
    }

    public void loadHistory(DbCallback<List<BattleRecordDetail>> callback) {
        repository.getAllRecords(callback);
    }

    public void close() {
    }
}
