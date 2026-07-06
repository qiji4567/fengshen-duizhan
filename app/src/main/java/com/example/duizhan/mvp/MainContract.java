package com.example.duizhan.mvp;

import com.example.duizhan.data.BattleRecord;

public interface MainContract {
    interface View extends BaseView {
        void showHistorySummary(int totalCount, BattleRecord lastRecord);
    }

    interface Presenter extends BasePresenter<View> {
        void loadHistorySummary();
    }
}
