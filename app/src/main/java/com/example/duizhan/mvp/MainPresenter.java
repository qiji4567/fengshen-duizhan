package com.example.duizhan.mvp;

import android.content.Context;

import com.example.duizhan.data.BattleRepository;
import com.example.duizhan.model.MainModel;

public class MainPresenter implements MainContract.Presenter {
    private final MainModel model;
    private MainContract.View view;

    public MainPresenter(Context context) {
        model = new MainModel(context);
    }

    @Override
    public void attach(MainContract.View view) {
        this.view = view;
    }

    @Override
    public void detach() {
        view = null;
        model.close();
    }

    @Override
    public void loadHistorySummary() {
        model.loadHistorySummary(records -> {
            if (view == null) {
                return;
            }
            int count = records == null ? 0 : records.size();
            com.example.duizhan.data.BattleRecord lastRecord = count > 0
                    ? BattleRepository.toLegacyRecord(records.get(0))
                    : null;
            view.showHistorySummary(count, lastRecord);
        });
    }
}
