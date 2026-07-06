package com.example.duizhan.mvp;

public interface BasePresenter<V extends BaseView> {
    void attach(V view);

    void detach();
}
