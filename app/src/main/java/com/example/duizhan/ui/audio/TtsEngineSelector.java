package com.example.duizhan.ui.audio;

import android.content.Context;
import android.content.Intent;
import android.speech.tts.TextToSpeech;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

final class TtsEngineSelector {
    private static final Locale[] LANGUAGE_CANDIDATES = {
            Locale.SIMPLIFIED_CHINESE,
            Locale.CHINESE,
            Locale.CHINA,
            Locale.TRADITIONAL_CHINESE,
            Locale.getDefault()
    };

    private static final String[] ENGINE_PRIORITY = {
            "com.google.android.tts",
            "com.iflytek.speechcloud",
            "com.iflytek.speechsuite",
            "com.xiaomi.mibrain.speech",
            "com.huawei.hiai",
            "com.samsung.SMT",
            "com.bytedance.labcvtts",
            "com.vivo.tts",
            "com.oplus.tts"
    };

    private TtsEngineSelector() {
    }

    static List<String> candidateEngines(Context context) {
        List<String> engines = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        engines.add(null);

        TextToSpeech probe = new TextToSpeech(context, status -> {
        });
        List<TextToSpeech.EngineInfo> available = probe.getEngines();
        probe.shutdown();

        if (available == null) {
            return engines;
        }
        for (String preferred : ENGINE_PRIORITY) {
            for (TextToSpeech.EngineInfo info : available) {
                if (info != null && preferred.equals(info.name) && seen.add(info.name)) {
                    engines.add(info.name);
                }
            }
        }
        for (TextToSpeech.EngineInfo info : available) {
            if (info != null && info.name != null && seen.add(info.name)) {
                engines.add(info.name);
            }
        }
        return engines;
    }

    static boolean configureLanguage(TextToSpeech textToSpeech) {
        if (textToSpeech == null) {
            return false;
        }
        for (Locale locale : LANGUAGE_CANDIDATES) {
            if (locale == null) {
                continue;
            }
            int result = textToSpeech.setLanguage(locale);
            if (result != TextToSpeech.LANG_MISSING_DATA
                    && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                return true;
            }
        }
        return false;
    }

    static Intent installDataIntent() {
        Intent intent = new Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }
}
