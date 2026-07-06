package com.example.duizhan.view;

import android.graphics.Canvas;

/** Keeps side-view sprites upright; mirror horizontally when facing left. */
public final class UprightFacing {
    private UprightFacing() {
    }

    public static boolean faceLeft(float facingRad) {
        float cos = (float) Math.cos(facingRad);
        if (Math.abs(cos) < 0.18f) {
            return (float) Math.sin(facingRad) < 0f;
        }
        return cos < 0f;
    }

    public static void apply(Canvas canvas, float facingRad) {
        if (faceLeft(facingRad)) {
            canvas.scale(-1f, 1f);
        }
    }
}
