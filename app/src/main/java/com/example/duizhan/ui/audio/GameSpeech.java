package com.example.duizhan.ui.audio;

import android.content.Context;
import android.content.Intent;

import com.example.duizhan.game.HeroType;
import com.example.duizhan.game.audio.BattleVoiceStep;

import java.util.List;

/**
 * Single shared speech engine for the whole app. Avoids multiple TTS instances fighting each other.
 */
public final class GameSpeech {
    private static volatile GameSpeech instance;

    private final HeroLineAnnouncer tts;
    private final BattleAnnouncer wav;

    private GameSpeech(Context appContext) {
        tts = new HeroLineAnnouncer(appContext);
        wav = new BattleAnnouncer();
    }

    public static GameSpeech get(Context context) {
        if (context == null) {
            return null;
        }
        if (instance == null) {
            synchronized (GameSpeech.class) {
                if (instance == null) {
                    instance = new GameSpeech(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    public static void warmUp(Context context) {
        get(context);
    }

    public boolean isTtsReady() {
        return tts != null && tts.isReady();
    }

    public boolean isTtsUnavailable() {
        return tts != null && tts.isUnavailable();
    }

    public void openTtsInstall(Context context) {
        if (context == null) {
            return;
        }
        try {
            context.startActivity(TtsEngineSelector.installDataIntent());
        } catch (RuntimeException ignored) {
            // Some devices block implicit TTS install intents.
        }
    }

    public HeroLineAnnouncer tts() {
        return tts;
    }

    public BattleAnnouncer wav() {
        return wav;
    }

    public void playVoiceSteps(Context context, List<BattleVoiceStep> steps) {
        if (context == null || steps == null || steps.isEmpty()) {
            return;
        }
        wav.playVoiceSteps(context, steps, tts);
    }

    public void stopSpeaking() {
        if (tts != null) {
            tts.stopSpeaking();
        }
        if (wav != null) {
            wav.stopSpeaking();
        }
    }
}
