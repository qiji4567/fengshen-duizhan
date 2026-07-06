package com.example.duizhan.ui.util;

import android.view.HapticFeedbackConstants;
import android.view.View;

import com.example.duizhan.ui.audio.UiTapSound;

public final class UiFeedbackHelper {
    private UiFeedbackHelper() {
    }

    public static void bindClick(View view, Runnable action) {
        if (view == null || action == null) {
            return;
        }
        view.setOnClickListener(v -> {
            performClick(v);
            action.run();
        });
    }

    public static void performClick(View view) {
        if (view == null) {
            return;
        }
        view.animate().cancel();
        view.animate()
                .scaleX(0.88f)
                .scaleY(0.88f)
                .setDuration(55L)
                .withEndAction(() -> view.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(90L)
                        .start())
                .start();
        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
        UiTapSound.play(view.getContext());
    }
}
