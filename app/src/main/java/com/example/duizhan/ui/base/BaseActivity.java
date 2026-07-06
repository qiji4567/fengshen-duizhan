package com.example.duizhan.ui.base;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.viewbinding.ViewBinding;

public abstract class BaseActivity<VB extends ViewBinding> extends Activity {
    protected VB binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = inflateBinding(getLayoutInflater());
        setContentView(binding.getRoot());
        onActivityCreated(savedInstanceState);
    }

    protected abstract VB inflateBinding(LayoutInflater inflater);

    protected void onActivityCreated(Bundle savedInstanceState) {
        initView(savedInstanceState);
        initData();
    }

    protected void initView(Bundle savedInstanceState) {
    }

    protected void initData() {
    }
}
