package com.example.duizhan;

import android.app.Application;

import com.example.duizhan.data.GameRepositoryProvider;
import com.example.duizhan.ui.audio.GameSpeech;

public class GameApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        GameSpeech.warmUp(this);
        GameRepositoryProvider.get(this);
    }
}
