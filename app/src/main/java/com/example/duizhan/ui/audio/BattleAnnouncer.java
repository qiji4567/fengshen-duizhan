package com.example.duizhan.ui.audio;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;

import java.io.IOException;

import com.example.duizhan.game.HeroType;
import com.example.duizhan.game.audio.BattleVoiceStep;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Plays bundled voice clips from {@code res/raw} on the main thread.
 */
public class BattleAnnouncer {
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final Queue<VoiceSequence> pendingSequences = new LinkedList<>();
    private volatile boolean released;
    private volatile boolean playing;
    private MediaPlayer activePlayer;

    public void playWelcome(Context context, Runnable onFinished, Runnable onFailed) {
        if (context == null) {
            notifyFailed(onFailed);
            return;
        }
        interruptQueuedPlayback();
        playClip(context.getApplicationContext(), BattleVoiceRegistry.welcomeRes(context),
                onFinished, onFailed);
    }

    public void playVoiceSteps(Context context, List<BattleVoiceStep> steps, HeroLineAnnouncer tts) {
        if (context == null || steps == null || steps.isEmpty() || released) {
            return;
        }
        Context appContext = context.getApplicationContext();
        List<VoiceClip> clips = new ArrayList<>();
        for (BattleVoiceStep step : steps) {
            if (step == null) {
                continue;
            }
            int resId = BattleVoiceRegistry.resolveStep(appContext, step);
            String text = BattleVoiceTextProvider.stepText(step);
            if (resId != 0 || (text != null && text.length() > 0)) {
                clips.add(new VoiceClip(resId, text));
            }
        }
        enqueueSequence(appContext, new VoiceSequence(clips, tts));
    }

    public boolean playSteps(Context context, List<BattleVoiceStep> steps) {
        if (context == null || steps == null || steps.isEmpty()) {
            return false;
        }
        List<Integer> clipResIds = BattleVoiceRegistry.resolve(context, steps);
        if (clipResIds.isEmpty()) {
            return false;
        }
        List<VoiceClip> clips = new ArrayList<>();
        for (Integer clipResId : clipResIds) {
            if (clipResId != null && clipResId != 0) {
                clips.add(new VoiceClip(clipResId, ""));
            }
        }
        enqueueSequence(context.getApplicationContext(), new VoiceSequence(clips, null));
        return true;
    }

    public void playHeroPick(Context context, HeroType heroType, Runnable onNoClipAvailable) {
        playHeroPick(context, heroType, null, onNoClipAvailable);
    }

    public void playHeroPick(Context context, HeroType heroType, Runnable onFinished,
                             Runnable onNoClipAvailable) {
        if (context == null || heroType == null) {
            notifyFinished(onNoClipAvailable != null ? onNoClipAvailable : onFinished);
            return;
        }
        Context appContext = context.getApplicationContext();
        int pickRes = BattleVoiceRegistry.heroPickRes(context, heroType);
        if (pickRes != 0) {
            interruptQueuedPlayback();
            playClip(appContext, pickRes, onFinished, onNoClipAvailable);
            return;
        }
        int heroRes = BattleVoiceRegistry.heroClipRes(context, heroType);
        if (heroRes != 0) {
            interruptQueuedPlayback();
            playClip(appContext, heroRes, onFinished, onNoClipAvailable);
            return;
        }
        notifyFinished(onNoClipAvailable);
    }

    public void stopSpeaking() {
        mainHandler.post(this::interruptQueuedPlayback);
    }

    public void release() {
        released = true;
        synchronized (pendingSequences) {
            pendingSequences.clear();
        }
        mainHandler.post(() -> {
            stopActivePlayer();
            playing = false;
        });
    }

    private void speakTts(HeroLineAnnouncer tts, String text, Runnable onDone) {
        if (tts != null && text != null && text.length() > 0) {
            tts.speakLine(text, onDone);
            return;
        }
        notifyFinished(onDone);
    }

    private void enqueueSequence(Context appContext, VoiceSequence sequence) {
        if (released || sequence == null || sequence.clips.isEmpty()) {
            return;
        }
        synchronized (pendingSequences) {
            pendingSequences.add(sequence);
        }
        mainHandler.post(() -> drainQueue(appContext));
    }

    private void drainQueue(Context appContext) {
        if (released || playing) {
            return;
        }
        VoiceSequence sequence;
        synchronized (pendingSequences) {
            sequence = pendingSequences.poll();
        }
        if (sequence == null) {
            return;
        }
        playing = true;
        playSequence(appContext, sequence, 0);
    }

    private void playSequence(Context appContext, VoiceSequence sequence, int index) {
        if (released || index >= sequence.clips.size()) {
            playing = false;
            mainHandler.post(() -> drainQueue(appContext));
            return;
        }
        VoiceClip clip = sequence.clips.get(index);
        Runnable next = () -> playSequence(appContext, sequence, index + 1);
        if (clip.resId == 0) {
            speakTts(sequence.tts, clip.fallbackText, next);
            return;
        }
        playClip(appContext, clip.resId,
                () -> playSequence(appContext, sequence, index + 1),
                () -> speakTts(sequence.tts, clip.fallbackText,
                        () -> playSequence(appContext, sequence, index + 1)));
    }

    private void interruptQueuedPlayback() {
        synchronized (pendingSequences) {
            pendingSequences.clear();
        }
        playing = false;
        stopActivePlayer();
    }

    private void playClip(Context appContext, int resId, Runnable onFinished, Runnable onFailed) {
        if (released) {
            notifyFailed(onFailed);
            return;
        }
        if (resId == 0) {
            notifyFailed(onFailed);
            return;
        }
        mainHandler.post(() -> {
            if (released) {
                notifyFailed(onFailed);
                return;
            }
            stopActivePlayer();
            MediaPlayer player = createRawPlayer(appContext, resId);
            if (player == null) {
                notifyFailed(onFailed);
                return;
            }
            activePlayer = player;
            try {
                GameAudioFocus.requestVoiceFocus(appContext);
                player.setVolume(1f, 1f);
                player.setOnCompletionListener(mp -> {
                    mp.release();
                    if (activePlayer == mp) {
                        activePlayer = null;
                    }
                    notifyFinished(onFinished);
                });
                player.setOnErrorListener((mp, what, extra) -> {
                    mp.release();
                    if (activePlayer == mp) {
                        activePlayer = null;
                    }
                    notifyFailed(onFailed);
                    return true;
                });
                player.start();
            } catch (RuntimeException ignored) {
                player.release();
                if (activePlayer == player) {
                    activePlayer = null;
                }
                notifyFailed(onFailed);
            }
        });
    }

    private MediaPlayer createRawPlayer(Context appContext, int resId) {
        MediaPlayer player = new MediaPlayer();
        try (AssetFileDescriptor afd = appContext.getResources().openRawResourceFd(resId)) {
            if (afd == null) {
                player.release();
                return null;
            }
            player.setAudioAttributes(GameAudioFocus.voiceAttributes());
            player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            player.prepare();
            return player;
        } catch (IOException | RuntimeException exception) {
            player.release();
            return null;
        }
    }

    private void stopActivePlayer() {
        if (activePlayer == null) {
            return;
        }
        try {
            activePlayer.stop();
        } catch (IllegalStateException ignored) {
            // Player may already be stopped.
        }
        activePlayer.release();
        activePlayer = null;
    }

    private void notifyFinished(Runnable callback) {
        if (callback != null) {
            callback.run();
        }
    }

    private void notifyFailed(Runnable callback) {
        if (callback != null) {
            callback.run();
        }
    }

    private static final class VoiceSequence {
        private final List<VoiceClip> clips;
        private final HeroLineAnnouncer tts;

        private VoiceSequence(List<VoiceClip> clips, HeroLineAnnouncer tts) {
            this.clips = clips == null ? new ArrayList<>() : new ArrayList<>(clips);
            this.tts = tts;
        }
    }

    private static final class VoiceClip {
        private final int resId;
        private final String fallbackText;

        private VoiceClip(int resId, String fallbackText) {
            this.resId = resId;
            this.fallbackText = fallbackText;
        }
    }
}
