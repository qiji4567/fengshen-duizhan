package com.example.duizhan.model;

import android.content.Context;

import com.example.duizhan.data.BattleRecord;
import com.example.duizhan.data.BattleRecordDetail;
import com.example.duizhan.data.GameRepositoryProvider;
import com.example.duizhan.data.DbCallback;

import java.util.List;

public class MainModel {
    private final com.example.duizhan.data.BattleRepository repository;

    public MainModel(Context context) {
        repository = GameRepositoryProvider.get(context);
    }

    public void loadHistorySummary(DbCallback<List<BattleRecordDetail>> callback) {
        repository.getAllRecords(callback);
    }

    public void close() {
    }
}
