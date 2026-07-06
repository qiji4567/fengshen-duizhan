package com.example.duizhan.ui.audio;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioTrack;

import com.example.duizhan.game.GameSoundEffect;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SoundEffectPlayer {
    private static final int SAMPLE_RATE = 22050;
    private static final int MIN_REPEAT_INTERVAL_MS = 70;

    private final ExecutorService executorService = Executors.newFixedThreadPool(2);
    private final Map<GameSoundEffect, Long> lastPlayTimeMap = new EnumMap<>(GameSoundEffect.class);
    private final Context appContext;
    private volatile boolean released;

    public SoundEffectPlayer() {
        this(null);
    }

    public SoundEffectPlayer(Context context) {
        appContext = context == null ? null : context.getApplicationContext();
    }

    public void play(List<GameSoundEffect> soundEffects) {
        if (soundEffects == null || soundEffects.isEmpty() || released) {
            return;
        }
        for (GameSoundEffect soundEffect : soundEffects) {
            play(soundEffect);
        }
    }

    public void release() {
        released = true;
        executorService.shutdownNow();
        lastPlayTimeMap.clear();
    }

    private void play(GameSoundEffect soundEffect) {
        long now = System.currentTimeMillis();
        Long lastPlayTime = lastPlayTimeMap.get(soundEffect);
        if (lastPlayTime != null && now - lastPlayTime < MIN_REPEAT_INTERVAL_MS) {
            return;
        }
        lastPlayTimeMap.put(soundEffect, now);
        executorService.execute(() -> playProfile(SoundProfile.from(soundEffect)));
    }

    private void playProfile(SoundProfile profile) {
        if (released) {
            return;
        }
        byte[] pcmData = createPcmData(profile);
        AudioTrack audioTrack = null;
        try {
            GameAudioFocus.requestEffectFocus(appContext);
            audioTrack = new AudioTrack.Builder()
                    .setAudioAttributes(new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build())
                    .setAudioFormat(new AudioFormat.Builder()
                            .setSampleRate(SAMPLE_RATE)
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build())
                    .setBufferSizeInBytes(pcmData.length)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build();
            audioTrack.write(pcmData, 0, pcmData.length);
            audioTrack.setVolume(profile.volume);
            audioTrack.play();
            Thread.sleep(profile.totalDurationMs() + 40L);
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
        } catch (RuntimeException ignored) {
            // Audio output may be unavailable on some emulators or muted devices.
        } finally {
            if (audioTrack != null) {
                audioTrack.release();
            }
        }
    }

    private byte[] createPcmData(SoundProfile profile) {
        int totalSamples = 0;
        for (int durationMs : profile.durationsMs) {
            totalSamples += durationMs * SAMPLE_RATE / 1000;
        }
        byte[] pcmData = new byte[totalSamples * 2];
        int sampleOffset = 0;
        for (int noteIndex = 0; noteIndex < profile.frequencies.length; noteIndex++) {
            int sampleCount = profile.durationsMs[noteIndex] * SAMPLE_RATE / 1000;
            double phaseStep = 2.0 * Math.PI * profile.frequencies[noteIndex] / SAMPLE_RATE;
            for (int sampleIndex = 0; sampleIndex < sampleCount; sampleIndex++) {
                float progress = sampleIndex / (float) Math.max(1, sampleCount - 1);
                float envelope = Math.min(1f, sampleIndex / (SAMPLE_RATE * 0.008f));
                envelope *= 1f - progress * 0.72f;
                short value = (short) (Math.sin(sampleIndex * phaseStep) * Short.MAX_VALUE * envelope);
                int byteIndex = (sampleOffset + sampleIndex) * 2;
                pcmData[byteIndex] = (byte) (value & 0xFF);
                pcmData[byteIndex + 1] = (byte) ((value >> 8) & 0xFF);
            }
            sampleOffset += sampleCount;
        }
        return pcmData;
    }

    private static class SoundProfile {
        private final int[] frequencies;
        private final int[] durationsMs;
        private final float volume;

        private SoundProfile(float volume, int[] frequencies, int[] durationsMs) {
            this.volume = volume;
            this.frequencies = frequencies;
            this.durationsMs = durationsMs;
        }

        private long totalDurationMs() {
            long total = 0L;
            for (int durationMs : durationsMs) {
                total += durationMs;
            }
            return total;
        }

        private static SoundProfile from(GameSoundEffect soundEffect) {
            switch (soundEffect) {
                case BATTLE_START:
                    return profile(0.34f, new int[]{392, 523, 659, 784}, new int[]{70, 70, 90, 140});
                case HERO_ATTACK:
                    return profile(0.18f, new int[]{320}, new int[]{48});
                case TOWER_ATTACK:
                    return profile(0.34f, new int[]{145, 110}, new int[]{70, 60});
                case SKILL:
                    return profile(0.28f, new int[]{680, 900}, new int[]{60, 80});
                case ULTIMATE:
                    return profile(0.38f, new int[]{180, 320, 620}, new int[]{100, 90, 150});
                case KILL:
                    return profile(0.28f, new int[]{220, 165}, new int[]{75, 110});
                case SINGLE_KILL:
                    return profile(0.30f, new int[]{392, 523}, new int[]{95, 140});
                case DOUBLE_KILL:
                    return profile(0.33f, new int[]{392, 523, 659}, new int[]{85, 85, 150});
                case TRIPLE_KILL:
                    return profile(0.36f, new int[]{330, 494, 659, 784}, new int[]{75, 75, 90, 170});
                case QUADRA_KILL:
                    return profile(0.40f, new int[]{262, 392, 523, 784, 988}, new int[]{70, 70, 80, 90, 190});
                case PENTA_KILL:
                    return profile(0.46f, new int[]{220, 330, 494, 659, 880, 1175}, new int[]{80, 80, 90, 90, 110, 240});
                case GODLIKE:
                    return profile(0.40f, new int[]{196, 294, 440, 660}, new int[]{120, 100, 100, 220});
                case LEGENDARY:
                    return profile(0.48f, new int[]{147, 220, 330, 494, 740, 988}, new int[]{100, 90, 90, 100, 120, 260});
                case LEVEL_UP:
                    return profile(0.25f, new int[]{660, 880, 1175}, new int[]{60, 60, 95});
                case SHOP:
                    return profile(0.20f, new int[]{880, 1320}, new int[]{50, 70});
                case VICTORY:
                    return profile(0.30f, new int[]{523, 659, 784, 1046}, new int[]{120, 120, 120, 180});
                default:
                    return profile(0.18f, new int[]{440}, new int[]{80});
            }
        }

        private static SoundProfile profile(float volume, int[] frequencies, int[] durationsMs) {
            return new SoundProfile(volume, frequencies, durationsMs);
        }
    }
}
