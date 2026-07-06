package com.example.duizhan.data;

import android.content.Context;

public final class GameRepositoryProvider {
    private static volatile BattleRepository instance;

    private GameRepositoryProvider() {
    }

    public static BattleRepository get(Context context) {
        if (instance == null) {
            synchronized (GameRepositoryProvider.class) {
                if (instance == null) {
                    instance = new BattleRepository(context.getApplicationContext());
                    instance.warmUp();
                }
            }
        }
        return instance;
    }
}
