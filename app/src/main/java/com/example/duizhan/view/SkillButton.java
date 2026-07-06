package com.example.duizhan.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.Button;

public class SkillButton extends Button {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF oval = new RectF();
    private String title = "";
    private String cooldownText = "";
    private float cooldownRate;
    private boolean gold;

    public SkillButton(Context context) {
        this(context, null);
    }

    public SkillButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        setGravity(Gravity.CENTER);
        setAllCaps(false);
        setIncludeFontPadding(false);
        setMinWidth(0);
        setMinHeight(0);
        setMinimumWidth(0);
        setMinimumHeight(0);
        setPadding(0, 0, 0, 0);
        setBackgroundColor(Color.TRANSPARENT);
    }

    public void setGold(boolean gold) {
        this.gold = gold;
        invalidate();
    }

    public void setSkillState(String title, float cooldown, float maxCooldown) {
        this.title = abbreviate(title);
        cooldownRate = maxCooldown <= 0f ? 0f : Math.max(0f, Math.min(1f, cooldown / maxCooldown));
        cooldownText = cooldown > 0f ? String.valueOf(Math.max(1, Math.round(cooldown))) : "";
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float w = getWidth();
        float h = getHeight();
        float size = Math.min(w, h);
        float cx = w / 2f;
        float cy = h / 2f;
        float r = size / 2f - dp(2f);

        paint.setStyle(Paint.Style.FILL);
        paint.setShader(new RadialGradient(cx, cy - r * 0.35f, r * 1.25f,
                new int[]{
                        gold ? Color.rgb(254, 240, 138) : Color.rgb(125, 211, 252),
                        gold ? Color.rgb(180, 83, 9) : Color.rgb(30, 64, 175),
                        Color.rgb(8, 13, 28)
                },
                new float[]{0f, 0.52f, 1f}, Shader.TileMode.CLAMP));
        canvas.drawCircle(cx, cy, r, paint);
        paint.setShader(null);

        if (isPressed()) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(dp(3.5f));
            paint.setColor(Color.argb(220, 255, 255, 255));
            canvas.drawCircle(cx, cy, r - dp(0.5f), paint);
        }

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(2.2f));
        paint.setColor(gold ? Color.rgb(250, 204, 21) : Color.rgb(147, 197, 253));
        canvas.drawCircle(cx, cy, r - dp(1.5f), paint);
        paint.setStrokeWidth(dp(1f));
        paint.setColor(Color.argb(150, 255, 255, 255));
        canvas.drawCircle(cx, cy, r - dp(6f), paint);

        if (cooldownRate > 0f) {
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.argb(182, 2, 6, 18));
            oval.set(cx - r, cy - r, cx + r, cy + r);
            canvas.drawArc(oval, -90f, 360f * cooldownRate, true, paint);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(dp(3f));
            paint.setColor(Color.argb(220, 248, 250, 252));
            canvas.drawArc(oval, -90f, 360f * (1f - cooldownRate), false, paint);
        }

        paint.setShader(new RadialGradient(cx, cy, r * 0.72f,
                new int[]{Color.argb(72, 255, 255, 255), Color.TRANSPARENT},
                new float[]{0f, 1f}, Shader.TileMode.CLAMP));
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(cx, cy, r * 0.72f, paint);
        paint.setShader(null);

        paint.setTextAlign(Paint.Align.CENTER);
        paint.setFakeBoldText(true);
        if (cooldownText.length() > 0) {
            paint.setColor(Color.WHITE);
            paint.setTextSize(size * 0.34f);
            canvas.drawText(cooldownText, cx, cy + size * 0.12f, paint);
        } else {
            paint.setColor(gold ? Color.rgb(17, 24, 39) : Color.rgb(248, 250, 252));
            paint.setTextSize(size * (title.length() > 2 ? 0.18f : 0.25f));
            canvas.drawText(title, cx, cy + size * 0.08f, paint);
        }
        paint.setFakeBoldText(false);
    }

    private String abbreviate(String value) {
        if (value == null || value.length() == 0) {
            return "";
        }
        String compact = value.replace("：", "").replace(" ", "");
        if (compact.length() <= 3) {
            return compact;
        }
        return compact.substring(0, 2);
    }

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }
}
