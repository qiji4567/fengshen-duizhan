package com.example.duizhan.ui.base;

import android.os.Bundle;

import androidx.viewbinding.ViewBinding;

import com.example.duizhan.mvp.BasePresenter;
import com.example.duizhan.mvp.BaseView;

public abstract class BaseMvpActivity<VB extends ViewBinding, V extends BaseView, P extends BasePresenter<V>>
        extends BaseActivity<VB> {
    protected P presenter;

    @Override
    protected void onActivityCreated(Bundle savedInstanceState) {
        presenter = createPresenter();
        if (presenter != null) {
            presenter.attach(getMvpView());
        }
        super.onActivityCreated(savedInstanceState);
    }

    protected abstract P createPresenter();

    protected abstract V getMvpView();

    @Override
    protected void onDestroy() {
        if (presenter != null) {
            presenter.detach();
            presenter = null;
        }
        super.onDestroy();
    }
}
