package com.example.duizhan.ui.audio;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.os.Build;

final class GameAudioFocus {
    private static final AudioAttributes VOICE_ATTRIBUTES = new AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
            .build();
    private static final AudioAttributes EFFECT_ATTRIBUTES = new AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build();

    private GameAudioFocus() {
    }

    static boolean requestVoiceFocus(Context context) {
        return requestFocus(context, VOICE_ATTRIBUTES, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
    }

    static boolean requestEffectFocus(Context context) {
        return requestFocus(context, EFFECT_ATTRIBUTES, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK);
    }

    static AudioAttributes voiceAttributes() {
        return VOICE_ATTRIBUTES;
    }

    static AudioAttributes effectAttributes() {
        return EFFECT_ATTRIBUTES;
    }

    private static boolean requestFocus(Context context, AudioAttributes attributes, int gain) {
        if (context == null) {
            return false;
        }
        AudioManager audioManager = (AudioManager) context.getApplicationContext()
                .getSystemService(Context.AUDIO_SERVICE);
        if (audioManager == null) {
            return false;
        }
        int result;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            AudioFocusRequest request = new AudioFocusRequest.Builder(gain)
                    .setAudioAttributes(attributes)
                    .setAcceptsDelayedFocusGain(false)
                    .setWillPauseWhenDucked(false)
                    .setOnAudioFocusChangeListener(focusChange -> {
                        // Speech clips are short; playback code handles interruption explicitly.
                    })
                    .build();
            result = audioManager.requestAudioFocus(request);
        } else {
            result = audioManager.requestAudioFocus(null,
                    AudioManager.STREAM_MUSIC,
                    gain);
        }
        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    }
}
