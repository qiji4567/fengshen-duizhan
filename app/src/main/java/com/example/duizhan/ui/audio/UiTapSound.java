package com.example.duizhan.ui.audio;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioTrack;

import com.example.duizhan.util.AppAsync;

public final class UiTapSound {
    private static final int SAMPLE_RATE = 22050;
    private static volatile boolean enabled = true;

    private UiTapSound() {
    }

    public static void play(Context context) {
        if (!enabled || context == null) {
            return;
        }
        Context appContext = context.getApplicationContext();
        AppAsync.runOnIo(() -> playTone(appContext));
    }

    private static void playTone(Context context) {
        byte[] pcm = createTapTone();
        AudioTrack track = null;
        try {
            track = new AudioTrack.Builder()
                    .setAudioAttributes(new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build())
                    .setAudioFormat(new AudioFormat.Builder()
                            .setSampleRate(SAMPLE_RATE)
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build())
                    .setBufferSizeInBytes(pcm.length)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build();
            track.write(pcm, 0, pcm.length);
            track.setVolume(0.35f);
            track.play();
            Thread.sleep(90L);
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
        } catch (RuntimeException ignored) {
            // Audio may be unavailable on some devices.
        } finally {
            if (track != null) {
                track.release();
            }
        }
    }

    private static byte[] createTapTone() {
        int sampleCount = SAMPLE_RATE / 28;
        byte[] data = new byte[sampleCount * 2];
        for (int i = 0; i < sampleCount; i++) {
            float t = i / (float) SAMPLE_RATE;
            float envelope = (float) Math.exp(-t * 42f);
            float wave = (float) Math.sin(2f * Math.PI * 920f * t) * envelope;
            short sample = (short) (wave * 9000f);
            data[i * 2] = (byte) (sample & 0xff);
            data[i * 2 + 1] = (byte) ((sample >> 8) & 0xff);
        }
        return data;
    }
}
