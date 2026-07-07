package com.example.duizhan.ui.audio;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

import com.example.duizhan.game.HeroType;
import com.example.duizhan.game.audio.BattleVoiceStep;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Random;

public class HeroLineAnnouncer implements TextToSpeech.OnInitListener {
    private static final String TAG = "HeroLineAnnouncer";
    private static final long PICK_MIN_INTERVAL_MS = 650L;
    private static final long MOVE_MIN_INTERVAL_MS = 3500L;
    private static final long ACTION_MIN_INTERVAL_MS = 900L;
    private static final long AMBIENT_MIN_INTERVAL_MS = 12000L;
    private static final int MAX_RECENT_DYNAMIC_LINES = 14;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final Random random = new Random();
    private final Deque<PendingLine> pendingLines = new ArrayDeque<>();
    private final Deque<Runnable> pendingDoneCallbacks = new ArrayDeque<>();
    private final Deque<Runnable> pendingReadyActions = new ArrayDeque<>();
    private final Deque<String> recentDynamicLines = new ArrayDeque<>();
    private final Context appContext;
    private final List<String> engineCandidates = new ArrayList<>();
    private TextToSpeech textToSpeech;
    private boolean ready;
    private boolean initFailed;
    private int engineIndex;
    private long lastPickLineMs;
    private long lastMoveLineMs;
    private long lastActionLineMs;
    private long lastAmbientLineMs;
    private boolean released;

    public HeroLineAnnouncer(Context context) {
        appContext = context == null ? null : context.getApplicationContext();
        if (appContext != null) {
            engineCandidates.addAll(TtsEngineSelector.candidateEngines(appContext));
            startNextEngine();
        }
    }

    private final UtteranceProgressListener utteranceListener = new UtteranceProgressListener() {
        @Override
        public void onStart(String utteranceId) {
        }

        @Override
        public void onDone(String utteranceId) {
            notifyLineDone();
        }

        @Override
        public void onError(String utteranceId) {
            notifyLineDone();
        }

        @Override
        public void onError(String utteranceId, int errorCode) {
            Log.w(TAG, "TTS utterance error: " + errorCode);
            notifyLineDone();
        }
    };

    public boolean isReady() {
        return ready && !released;
    }

    public boolean isUnavailable() {
        return initFailed;
    }

    public void runWhenReady(Runnable action) {
        if (released || action == null) {
            return;
        }
        if (ready) {
            mainHandler.post(action);
            return;
        }
        if (initFailed) {
            return;
        }
        pendingReadyActions.addLast(action);
    }

    @Override
    public void onInit(int status) {
        if (released) {
            return;
        }
        if (textToSpeech == null) {
            tryNextEngine();
            return;
        }
        if (status != TextToSpeech.SUCCESS || !TtsEngineSelector.configureLanguage(textToSpeech)) {
            Log.w(TAG, "TTS engine init failed, status=" + status + ", engine=" + currentEngineName());
            tryNextEngine();
            return;
        }
        textToSpeech.setOnUtteranceProgressListener(utteranceListener);
        textToSpeech.setSpeechRate(0.98f);
        textToSpeech.setPitch(1.02f);
        ready = true;
        initFailed = false;
        Log.i(TAG, "TTS ready: " + currentEngineName());
        drainPendingLines();
        drainReadyActions();
    }

    public void playWelcome() {
        speak(BattleVoiceTextProvider.welcomeText(), TextToSpeech.QUEUE_FLUSH, null);
    }

    public void playWelcome(Runnable onDone) {
        speak(BattleVoiceTextProvider.welcomeText(), TextToSpeech.QUEUE_FLUSH, onDone);
    }

    public void playNarration(String text) {
        speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }

    public void playNarration(String text, Runnable onDone) {
        speak(text, TextToSpeech.QUEUE_FLUSH, onDone);
    }

    public void playNarrationLine(String text, boolean firstLine) {
        speak(text, firstLine ? TextToSpeech.QUEUE_FLUSH : TextToSpeech.QUEUE_ADD, null);
    }

    public void playNarrationLine(String text, boolean firstLine, Runnable onDone) {
        speak(text, firstLine ? TextToSpeech.QUEUE_FLUSH : TextToSpeech.QUEUE_ADD, onDone);
    }

    public void speakLine(String text) {
        speak(text, TextToSpeech.QUEUE_ADD, null);
    }

    public void speakLine(String text, Runnable onDone) {
        speak(text, TextToSpeech.QUEUE_ADD, onDone);
    }

    public void stopSpeaking() {
        pendingDoneCallbacks.clear();
        if (textToSpeech != null) {
            textToSpeech.stop();
        }
    }

    public void playPickLine(HeroType heroType) {
        playPickLine(heroType, true);
    }

    public void playPickLine(HeroType heroType, boolean interruptPrevious) {
        playPickLine(heroType, interruptPrevious, null);
    }

    public void playPickLine(HeroType heroType, boolean interruptPrevious, Runnable onDone) {
        long now = System.currentTimeMillis();
        if (now - lastPickLineMs < PICK_MIN_INTERVAL_MS) {
            notifyDone(onDone);
            return;
        }
        lastPickLineMs = now;
        int queueMode = interruptPrevious ? TextToSpeech.QUEUE_FLUSH : TextToSpeech.QUEUE_ADD;
        speak(HeroVoiceLineProvider.pickLine(heroType), queueMode, onDone);
    }

    public void playVoiceSteps(List<BattleVoiceStep> steps) {
        List<String> lines = BattleVoiceTextProvider.resolveSteps(steps);
        if (lines.isEmpty()) {
            return;
        }
        for (String line : lines) {
            speak(line, TextToSpeech.QUEUE_ADD, null);
        }
    }

    public void playMissingHeroSteps(Context context, List<BattleVoiceStep> steps) {
        if (context == null || steps == null) {
            return;
        }
        for (BattleVoiceStep step : steps) {
            if (step != null && step.isHero() && !BattleVoiceRegistry.hasHeroClip(context, step.heroType)) {
                speak(BattleVoiceTextProvider.heroLine(step.heroType), TextToSpeech.QUEUE_ADD, null);
            }
        }
    }

    public void maybePlayMoveLine(HeroType heroType, float moveX, float moveY) {
        if (moveX * moveX + moveY * moveY < 0.18f) {
            return;
        }
        long now = System.currentTimeMillis();
        if (now - lastMoveLineMs < MOVE_MIN_INTERVAL_MS || random.nextFloat() > 0.55f) {
            return;
        }
        lastMoveLineMs = now;
        speakDynamic(HeroVoiceLineProvider.moveLine(heroType, random));
    }

    public void playActionLine(HeroType heroType) {
        long now = System.currentTimeMillis();
        if (now - lastActionLineMs < ACTION_MIN_INTERVAL_MS) {
            return;
        }
        lastActionLineMs = now;
        speakDynamic(HeroVoiceLineProvider.actionLine(heroType, random));
    }

    public void maybePlayAmbientLine(HeroType heroType) {
        if (heroType == null) {
            return;
        }
        long now = System.currentTimeMillis();
        if (now - lastAmbientLineMs < AMBIENT_MIN_INTERVAL_MS || random.nextFloat() > 0.4f) {
            return;
        }
        lastAmbientLineMs = now;
        speakDynamic(HeroVoiceLineProvider.ambientLine(heroType, random));
    }

    public void release() {
        released = true;
        ready = false;
        initFailed = true;
        pendingLines.clear();
        pendingDoneCallbacks.clear();
        pendingReadyActions.clear();
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
            textToSpeech = null;
        }
    }

    private void startNextEngine() {
        if (released || appContext == null) {
            return;
        }
        if (engineIndex >= engineCandidates.size()) {
            initFailed = true;
            ready = false;
            Log.e(TAG, "No usable TTS engine found");
            drainReadyActions();
            return;
        }
        String engine = engineCandidates.get(engineIndex++);
        shutdownEngine();
        if (engine == null || engine.length() == 0) {
            textToSpeech = new TextToSpeech(appContext, this);
        } else {
            textToSpeech = new TextToSpeech(appContext, this, engine);
        }
    }

    private void tryNextEngine() {
        ready = false;
        startNextEngine();
    }

    private void shutdownEngine() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
            textToSpeech = null;
        }
    }

    private String currentEngineName() {
        if (textToSpeech == null) {
            return "none";
        }
        String engine = textToSpeech.getDefaultEngine();
        return engine == null ? "default" : engine;
    }

    private void speak(String text, int queueMode, Runnable onDone) {
        if (released || text == null || text.length() == 0) {
            notifyDone(onDone);
            return;
        }
        if (initFailed || textToSpeech == null) {
            notifyDone(onDone);
            return;
        }
        if (!ready) {
            pendingLines.addLast(new PendingLine(text, queueMode, onDone));
            return;
        }
        if (onDone != null) {
            pendingDoneCallbacks.addLast(onDone);
        }
        GameAudioFocus.requestVoiceFocus(appContext);
        String utteranceId = "hero_line_" + System.nanoTime();
        int result = speakInternal(text, queueMode, utteranceId, true);
        if (result == TextToSpeech.ERROR) {
            Log.w(TAG, "TTS speak failed on engine " + currentEngineName());
            pendingDoneCallbacks.pollLast();
            notifyDone(onDone);
        }
    }

    private int speakInternal(String text, int queueMode, String utteranceId, boolean withBundle) {
        if (textToSpeech == null) {
            return TextToSpeech.ERROR;
        }
        if (withBundle) {
            Bundle params = new Bundle();
            params.putInt(TextToSpeech.Engine.KEY_PARAM_STREAM, android.media.AudioManager.STREAM_MUSIC);
            return textToSpeech.speak(text, queueMode, params, utteranceId);
        }
        return textToSpeech.speak(text, queueMode, null, utteranceId);
    }

    private void notifyLineDone() {
        mainHandler.post(() -> {
            Runnable callback = pendingDoneCallbacks.pollFirst();
            if (callback != null) {
                callback.run();
            }
        });
    }

    private void notifyDone(Runnable onDone) {
        if (onDone != null) {
            mainHandler.post(onDone);
        }
    }

    private void speakDynamic(String text) {
        if (text == null || text.length() == 0 || recentlySaid(text)) {
            return;
        }
        rememberDynamicLine(text);
        speak(text, TextToSpeech.QUEUE_ADD, null);
    }

    private boolean recentlySaid(String text) {
        return recentDynamicLines.contains(text);
    }

    private void rememberDynamicLine(String text) {
        recentDynamicLines.addLast(text);
        while (recentDynamicLines.size() > MAX_RECENT_DYNAMIC_LINES) {
            recentDynamicLines.removeFirst();
        }
    }

    private void drainPendingLines() {
        while (!pendingLines.isEmpty()) {
            PendingLine line = pendingLines.removeFirst();
            speak(line.text, line.queueMode, line.onDone);
        }
    }

    private void drainReadyActions() {
        while (!pendingReadyActions.isEmpty()) {
            Runnable action = pendingReadyActions.removeFirst();
            if (action != null) {
                action.run();
            }
        }
    }

    private static final class PendingLine {
        private final String text;
        private final int queueMode;
        private final Runnable onDone;

        private PendingLine(String text, int queueMode, Runnable onDone) {
            this.text = text;
            this.queueMode = queueMode;
            this.onDone = onDone;
        }
    }
}
