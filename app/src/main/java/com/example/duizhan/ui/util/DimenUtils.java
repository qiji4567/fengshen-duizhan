package com.example.duizhan.ui.util;

import android.content.Context;
import android.util.TypedValue;

public final class DimenUtils {
    private DimenUtils() {
    }

    public static int dp(Context context, float value) {
        return Math.round(dpF(context, value));
    }

    public static float dpF(Context context, float value) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value,
                context.getResources().getDisplayMetrics());
    }

    public static float spF(Context context, float value) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, value,
                context.getResources().getDisplayMetrics());
    }
}
