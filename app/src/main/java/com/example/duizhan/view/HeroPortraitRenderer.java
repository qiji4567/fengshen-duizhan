package com.example.duizhan.view;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;

import com.example.duizhan.game.HeroType;
import com.example.duizhan.game.HeroVisualProfile;

/**
 * Procedural hero bust portraits for picker UI — one distinct look per myth hero.
 */
public final class HeroPortraitRenderer {
    public static final int THUMB_SIZE = 108;
    public static final int PREVIEW_SIZE = 240;
    public static final int SHOWCASE_WIDTH = 640;
    public static final int SHOWCASE_HEIGHT = 360;

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path = new Path();
    private final RectF rect = new RectF();

    public Bitmap render(HeroType type, int size) {
        HeroVisualProfile profile = HeroVisualProfile.of(type);
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        float cx = size * 0.5f;
        float cy = size * 0.52f;
        float unit = size / 96f;

        drawBackground(canvas, size, profile, type);
        drawAura(canvas, cx, cy, size * 0.46f, profile.trimColor);
        drawBody(canvas, cx, cy, unit, profile);
        drawHead(canvas, cx, cy - 10f * unit, unit, profile);
        drawWeaponBadge(canvas, size, unit, profile);
        drawNamePlate(canvas, size, type.label, profile.trimColor);
        drawFrame(canvas, size, profile.trimColor);
        return bitmap;
    }

    private void drawBackground(Canvas canvas, int size, HeroVisualProfile profile, HeroType type) {
        int top = shiftColor(profile.robeColor, 28);
        int bottom = shiftColor(profile.robeColor, -36);
        paint.setShader(new LinearGradient(0f, 0f, 0f, size, top, bottom, Shader.TileMode.CLAMP));
        canvas.drawRect(0f, 0f, size, size, paint);
        paint.setShader(null);
        paint.setColor(Color.argb(50, Color.red(profile.trimColor), Color.green(profile.trimColor),
                Color.blue(profile.trimColor)));
        canvas.drawCircle(size * 0.78f, size * 0.22f, size * 0.28f, paint);
        paint.setColor(Color.argb(28, 255, 255, 255));
        canvas.drawRect(0f, size * 0.72f, size, size, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(120, 15, 23, 42));
        paint.setTextSize(size * 0.11f);
        paint.setFakeBoldText(true);
        String faction = type.faction.length() > 4 ? type.faction.substring(0, 4) : type.faction;
        canvas.drawText(faction, size * 0.08f, size * 0.16f, paint);
    }

    private void drawAura(Canvas canvas, float cx, float cy, float radius, int color) {
        paint.setShader(new RadialGradient(cx, cy, radius,
                new int[]{Color.argb(90, Color.red(color), Color.green(color), Color.blue(color)), Color.TRANSPARENT},
                new float[]{0.2f, 1f}, Shader.TileMode.CLAMP));
        canvas.drawCircle(cx, cy, radius, paint);
        paint.setShader(null);
    }

    private void drawBody(Canvas canvas, float cx, float cy, float unit, HeroVisualProfile profile) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(profile.robeColor);
        float shoulder = 34f * unit * profile.scale;
        float bodyTop = cy + 2f * unit;
        float bodyBottom = cy + 42f * unit;
        path.reset();
        path.moveTo(cx - shoulder, bodyTop);
        path.lineTo(cx + shoulder, bodyTop);
        path.quadTo(cx + shoulder * 0.9f, bodyBottom, cx, bodyBottom + 8f * unit);
        path.quadTo(cx - shoulder * 0.9f, bodyBottom, cx - shoulder, bodyTop);
        path.close();
        canvas.drawPath(path, paint);
        paint.setColor(profile.trimColor);
        paint.setStrokeWidth(Math.max(1f, 2f * unit));
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawLine(cx - shoulder * 0.55f, bodyTop + 6f * unit, cx + shoulder * 0.55f, bodyTop + 6f * unit, paint);
        paint.setStyle(Paint.Style.FILL);
    }

    private void drawHead(Canvas canvas, float cx, float cy, float unit, HeroVisualProfile profile) {
        switch (profile.body) {
            case MONKEY_KING:
                drawMonkeyHead(canvas, cx, cy, unit, profile);
                break;
            case PIG_DEMON:
                drawPigHead(canvas, cx, cy, unit, profile);
                break;
            case BULL_DEMON:
            case OX_DEMON:
                drawHornedHead(canvas, cx, cy, unit, profile, true);
                break;
            case SKELETON:
                drawSkullHead(canvas, cx, cy, unit);
                break;
            case DRAGON_KING:
                drawDragonHead(canvas, cx, cy, unit, profile);
                break;
            case FOX_SPIRIT:
                drawFoxHead(canvas, cx, cy, unit, profile);
                break;
            case BIRD_DEMON:
                drawBirdHead(canvas, cx, cy, unit, profile);
                break;
            case GIANT:
                drawGiantHead(canvas, cx, cy, unit, profile);
                break;
            case BUDDHA:
                drawBuddhaHead(canvas, cx, cy, unit, profile);
                break;
            case EMPEROR:
                drawEmperorHead(canvas, cx, cy, unit, profile);
                break;
            case GHOST_JUDGE:
                drawGhostHead(canvas, cx, cy, unit, profile);
                break;
            case FEMALE_IMMORTAL:
                drawFemaleHead(canvas, cx, cy, unit, profile);
                break;
            case CHILD_WARRIOR:
                drawChildHead(canvas, cx, cy, unit, profile);
                break;
            case HORSE_DEMON:
                drawHorseHead(canvas, cx, cy, unit, profile);
                break;
            case SNAKE_DEMON:
                drawSnakeHead(canvas, cx, cy, unit, profile);
                break;
            case HUMAN_MAGE:
                drawMageHead(canvas, cx, cy, unit, profile);
                break;
            case HUMAN_MONK:
                drawMonkHead(canvas, cx, cy, unit, profile);
                break;
            case HUMAN_WARRIOR:
            default:
                drawWarriorHead(canvas, cx, cy, unit, profile);
                break;
        }
        if (profile.thirdEye) {
            paint.setColor(Color.rgb(56, 189, 248));
            canvas.drawCircle(cx, cy - 2f * unit, 2.2f * unit, paint);
            paint.setColor(Color.WHITE);
            canvas.drawCircle(cx, cy - 2f * unit, 1f * unit, paint);
        }
        if (profile.crown) {
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(profile.trimColor);
            path.reset();
            path.moveTo(cx - 12f * unit, cy - 16f * unit);
            path.lineTo(cx - 6f * unit, cy - 24f * unit);
            path.lineTo(cx, cy - 18f * unit);
            path.lineTo(cx + 6f * unit, cy - 24f * unit);
            path.lineTo(cx + 12f * unit, cy - 16f * unit);
            path.close();
            canvas.drawPath(path, paint);
        }
        if (profile.wings) {
            paint.setColor(Color.argb(120, 147, 197, 253));
            canvas.drawOval(cx - 30f * unit, cy - 4f * unit, cx - 10f * unit, cy + 16f * unit, paint);
            canvas.drawOval(cx + 10f * unit, cy - 4f * unit, cx + 30f * unit, cy + 16f * unit, paint);
        }
    }

    private void drawWarriorHead(Canvas canvas, float cx, float cy, float unit, HeroVisualProfile profile) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(profile.skinColor);
        canvas.drawCircle(cx, cy, 15f * unit, paint);
        paint.setColor(profile.hairColor);
        canvas.drawArc(cx - 16f * unit, cy - 24f * unit, cx + 16f * unit, cy + 2f * unit, 180f, 180f, true, paint);
        paint.setColor(Color.rgb(30, 41, 59));
        canvas.drawCircle(cx - 5f * unit, cy + 1f * unit, 1.8f * unit, paint);
        canvas.drawCircle(cx + 5f * unit, cy + 1f * unit, 1.8f * unit, paint);
    }

    private void drawMageHead(Canvas canvas, float cx, float cy, float unit, HeroVisualProfile profile) {
        drawWarriorHead(canvas, cx, cy, unit, profile);
        paint.setColor(profile.trimColor);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2f * unit);
        canvas.drawCircle(cx, cy - 1f * unit, 16f * unit, paint);
    }

    private void drawMonkHead(Canvas canvas, float cx, float cy, float unit, HeroVisualProfile profile) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(profile.skinColor);
        canvas.drawCircle(cx, cy, 14f * unit, paint);
        paint.setColor(Color.rgb(250, 204, 21));
        canvas.drawOval(cx - 4f * unit, cy - 20f * unit, cx + 4f * unit, cy - 10f * unit, paint);
    }

    private void drawFemaleHead(Canvas canvas, float cx, float cy, float unit, HeroVisualProfile profile) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(profile.skinColor);
        canvas.drawCircle(cx, cy, 14f * unit, paint);
        paint.setColor(profile.hairColor);
        canvas.drawOval(cx - 18f * unit, cy - 8f * unit, cx - 8f * unit, cy + 28f * unit, paint);
        canvas.drawOval(cx + 8f * unit, cy - 8f * unit, cx + 18f * unit, cy + 28f * unit, paint);
        paint.setColor(profile.trimColor);
        canvas.drawCircle(cx, cy - 12f * unit, 4f * unit, paint);
    }

    private void drawChildHead(Canvas canvas, float cx, float cy, float unit, HeroVisualProfile profile) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(profile.skinColor);
        canvas.drawCircle(cx, cy + 2f * unit, 12f * unit, paint);
        paint.setColor(profile.hairColor);
        canvas.drawCircle(cx, cy - 4f * unit, 10f * unit, paint);
        paint.setColor(Color.rgb(30, 41, 59));
        canvas.drawCircle(cx - 4f * unit, cy + 2f * unit, 1.4f * unit, paint);
        canvas.drawCircle(cx + 4f * unit, cy + 2f * unit, 1.4f * unit, paint);
    }

    private void drawMonkeyHead(Canvas canvas, float cx, float cy, float unit, HeroVisualProfile profile) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(profile.skinColor);
        canvas.drawCircle(cx, cy, 15f * unit, paint);
        paint.setColor(shiftColor(profile.skinColor, -30));
        canvas.drawCircle(cx - 16f * unit, cy + 4f * unit, 6f * unit, paint);
        canvas.drawCircle(cx + 16f * unit, cy + 4f * unit, 6f * unit, paint);
        paint.setColor(Color.rgb(250, 204, 21));
        paint.setStrokeWidth(2.5f * unit);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawLine(cx - 12f * unit, cy - 10f * unit, cx + 12f * unit, cy - 10f * unit, paint);
    }

    private void drawPigHead(Canvas canvas, float cx, float cy, float unit, HeroVisualProfile profile) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(profile.skinColor);
        canvas.drawOval(cx - 16f * unit, cy - 10f * unit, cx + 16f * unit, cy + 14f * unit, paint);
        paint.setColor(shiftColor(profile.skinColor, -20));
        canvas.drawOval(cx - 5f * unit, cy + 2f * unit, cx + 5f * unit, cy + 8f * unit, paint);
        paint.setColor(profile.skinColor);
        canvas.drawCircle(cx - 8f * unit, cy + 2f * unit, 2f * unit, paint);
        canvas.drawCircle(cx + 8f * unit, cy + 2f * unit, 2f * unit, paint);
    }

    private void drawHornedHead(Canvas canvas, float cx, float cy, float unit, HeroVisualProfile profile, boolean bull) {
        drawWarriorHead(canvas, cx, cy, unit, profile);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(shiftColor(profile.hairColor, bull ? 10 : -10));
        path.reset();
        path.moveTo(cx - 10f * unit, cy - 14f * unit);
        path.lineTo(cx - 16f * unit, cy - 26f * unit);
        path.lineTo(cx - 4f * unit, cy - 16f * unit);
        path.close();
        canvas.drawPath(path, paint);
        path.reset();
        path.moveTo(cx + 10f * unit, cy - 14f * unit);
        path.lineTo(cx + 16f * unit, cy - 26f * unit);
        path.lineTo(cx + 4f * unit, cy - 16f * unit);
        path.close();
        canvas.drawPath(path, paint);
    }

    private void drawSkullHead(Canvas canvas, float cx, float cy, float unit) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(241, 245, 249));
        canvas.drawCircle(cx, cy, 14f * unit, paint);
        paint.setColor(Color.rgb(30, 41, 59));
        canvas.drawCircle(cx - 5f * unit, cy, 3f * unit, paint);
        canvas.drawCircle(cx + 5f * unit, cy, 3f * unit, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1.5f * unit);
        canvas.drawLine(cx - 4f * unit, cy + 8f * unit, cx + 4f * unit, cy + 8f * unit, paint);
    }

    private void drawDragonHead(Canvas canvas, float cx, float cy, float unit, HeroVisualProfile profile) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(profile.skinColor);
        canvas.drawOval(cx - 15f * unit, cy - 12f * unit, cx + 15f * unit, cy + 12f * unit, paint);
        paint.setColor(profile.trimColor);
        canvas.drawCircle(cx - 8f * unit, cy - 2f * unit, 2.5f * unit, paint);
        canvas.drawCircle(cx + 8f * unit, cy - 2f * unit, 2.5f * unit, paint);
        paint.setColor(Color.rgb(125, 211, 252));
        path.reset();
        path.moveTo(cx, cy + 10f * unit);
        path.lineTo(cx - 4f * unit, cy + 18f * unit);
        path.lineTo(cx + 4f * unit, cy + 18f * unit);
        path.close();
        canvas.drawPath(path, paint);
    }

    private void drawFoxHead(Canvas canvas, float cx, float cy, float unit, HeroVisualProfile profile) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(profile.skinColor);
        canvas.drawCircle(cx, cy + 2f * unit, 13f * unit, paint);
        paint.setColor(profile.hairColor);
        path.reset();
        path.moveTo(cx - 14f * unit, cy - 6f * unit);
        path.lineTo(cx - 20f * unit, cy - 20f * unit);
        path.lineTo(cx - 4f * unit, cy - 10f * unit);
        path.close();
        canvas.drawPath(path, paint);
        path.reset();
        path.moveTo(cx + 14f * unit, cy - 6f * unit);
        path.lineTo(cx + 20f * unit, cy - 20f * unit);
        path.lineTo(cx + 4f * unit, cy - 10f * unit);
        path.close();
        canvas.drawPath(path, paint);
    }

    private void drawBirdHead(Canvas canvas, float cx, float cy, float unit, HeroVisualProfile profile) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(profile.skinColor);
        canvas.drawCircle(cx, cy, 13f * unit, paint);
        paint.setColor(profile.trimColor);
        canvas.drawOval(cx - 4f * unit, cy + 6f * unit, cx + 4f * unit, cy + 12f * unit, paint);
        paint.setColor(Color.rgb(250, 204, 21));
        canvas.drawOval(cx + 8f * unit, cy - 2f * unit, cx + 18f * unit, cy + 4f * unit, paint);
    }

    private void drawGiantHead(Canvas canvas, float cx, float cy, float unit, HeroVisualProfile profile) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(profile.skinColor);
        canvas.drawCircle(cx, cy, 18f * unit, paint);
        paint.setColor(profile.hairColor);
        canvas.drawRect(cx - 18f * unit, cy - 20f * unit, cx + 18f * unit, cy - 8f * unit, paint);
        paint.setColor(Color.rgb(30, 41, 59));
        canvas.drawRect(cx - 8f * unit, cy + 4f * unit, cx - 3f * unit, cy + 7f * unit, paint);
        canvas.drawRect(cx + 3f * unit, cy + 4f * unit, cx + 8f * unit, cy + 7f * unit, paint);
    }

    private void drawBuddhaHead(Canvas canvas, float cx, float cy, float unit, HeroVisualProfile profile) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(profile.skinColor);
        canvas.drawCircle(cx, cy + 2f * unit, 16f * unit, paint);
        paint.setColor(Color.rgb(250, 204, 21));
        for (int i = 0; i < 5; i++) {
            float ang = (float) (i * Math.PI * 2 / 5 - Math.PI / 2);
            canvas.drawCircle(cx + (float) Math.cos(ang) * 10f * unit,
                    cy - 10f * unit + (float) Math.sin(ang) * 4f * unit, 2f * unit, paint);
        }
    }

    private void drawEmperorHead(Canvas canvas, float cx, float cy, float unit, HeroVisualProfile profile) {
        drawWarriorHead(canvas, cx, cy, unit, profile);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(220, 38, 38));
        canvas.drawRect(cx - 14f * unit, cy - 22f * unit, cx + 14f * unit, cy - 14f * unit, paint);
        paint.setColor(Color.rgb(250, 204, 21));
        for (int i = -2; i <= 2; i++) {
            canvas.drawCircle(cx + i * 5f * unit, cy - 18f * unit, 1.5f * unit, paint);
        }
    }

    private void drawGhostHead(Canvas canvas, float cx, float cy, float unit, HeroVisualProfile profile) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(profile.skinColor);
        canvas.drawCircle(cx, cy, 14f * unit, paint);
        paint.setColor(Color.rgb(30, 41, 59));
        canvas.drawRect(cx - 12f * unit, cy - 20f * unit, cx + 12f * unit, cy - 12f * unit, paint);
        paint.setColor(Color.rgb(248, 113, 113));
        canvas.drawCircle(cx - 5f * unit, cy + 1f * unit, 2f * unit, paint);
        canvas.drawCircle(cx + 5f * unit, cy + 1f * unit, 2f * unit, paint);
    }

    private void drawHorseHead(Canvas canvas, float cx, float cy, float unit, HeroVisualProfile profile) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(profile.skinColor);
        canvas.drawOval(cx - 12f * unit, cy - 10f * unit, cx + 12f * unit, cy + 14f * unit, paint);
        paint.setColor(profile.hairColor);
        canvas.drawRect(cx - 4f * unit, cy - 22f * unit, cx + 4f * unit, cy - 8f * unit, paint);
    }

    private void drawSnakeHead(Canvas canvas, float cx, float cy, float unit, HeroVisualProfile profile) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(profile.skinColor);
        canvas.drawOval(cx - 10f * unit, cy - 14f * unit, cx + 10f * unit, cy + 10f * unit, paint);
        paint.setColor(Color.rgb(34, 197, 94));
        canvas.drawCircle(cx - 4f * unit, cy - 2f * unit, 2f * unit, paint);
        canvas.drawCircle(cx + 4f * unit, cy - 2f * unit, 2f * unit, paint);
    }

    private void drawWeaponBadge(Canvas canvas, int size, float unit, HeroVisualProfile profile) {
        float x = size * 0.72f;
        float y = size * 0.72f;
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(210, 15, 23, 42));
        canvas.drawCircle(x, y, 14f * unit, paint);
        paint.setColor(profile.trimColor);
        switch (profile.weapon) {
            case BOW:
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(2f * unit);
                canvas.drawArc(x - 8f * unit, y - 8f * unit, x + 8f * unit, y + 8f * unit, 300f, 120f, false, paint);
                break;
            case GOLDEN_STAFF:
            case STAFF:
                paint.setStrokeWidth(3f * unit);
                canvas.drawLine(x - 8f * unit, y + 6f * unit, x + 8f * unit, y - 6f * unit, paint);
                break;
            case TRIDENT:
                canvas.drawLine(x, y - 8f * unit, x, y + 8f * unit, paint);
                canvas.drawLine(x - 5f * unit, y - 4f * unit, x, y - 8f * unit, paint);
                canvas.drawLine(x + 5f * unit, y - 4f * unit, x, y - 8f * unit, paint);
                break;
            case FAN:
                paint.setStyle(Paint.Style.FILL);
                canvas.drawOval(x - 8f * unit, y - 10f * unit, x + 8f * unit, y + 2f * unit, paint);
                break;
            case LOTUS:
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(Color.rgb(244, 114, 182));
                for (int i = 0; i < 5; i++) {
                    canvas.drawCircle(x + (float) Math.cos(i) * 4f * unit, y + (float) Math.sin(i) * 4f * unit, 3f * unit, paint);
                }
                break;
            case RAKE:
                paint.setStrokeWidth(2f * unit);
                paint.setStyle(Paint.Style.STROKE);
                for (int i = -2; i <= 2; i++) {
                    canvas.drawLine(x + i * 3f * unit, y - 8f * unit, x + i * 3f * unit, y + 2f * unit, paint);
                }
                break;
            case BOOK:
            case SEAL:
                paint.setStyle(Paint.Style.FILL);
                canvas.drawRect(x - 7f * unit, y - 8f * unit, x + 7f * unit, y + 8f * unit, paint);
                break;
            default:
                paint.setStrokeWidth(2.5f * unit);
                paint.setStyle(Paint.Style.STROKE);
                canvas.drawLine(x - 7f * unit, y + 5f * unit, x + 7f * unit, y - 5f * unit, paint);
                break;
        }
    }

    private void drawNamePlate(Canvas canvas, int size, String label, int trimColor) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(180, 15, 23, 42));
        rect.set(0f, size * 0.8f, size, size);
        canvas.drawRect(rect, paint);
        paint.setColor(trimColor);
        paint.setTextSize(size * 0.11f);
        paint.setFakeBoldText(true);
        String shortName = label;
        if (shortName.contains("·")) {
            shortName = shortName.substring(shortName.indexOf('·') + 1);
        }
        if (shortName.length() > 4) {
            shortName = shortName.substring(0, 4);
        }
        float textWidth = paint.measureText(shortName);
        canvas.drawText(shortName, (size - textWidth) * 0.5f, size * 0.93f, paint);
    }

    private void drawFrame(Canvas canvas, int size, int trimColor) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(Math.max(2f, size * 0.025f));
        paint.setColor(trimColor);
        rect.set(1f, 1f, size - 1f, size - 1f);
        canvas.drawRoundRect(rect, size * 0.08f, size * 0.08f, paint);
    }

    private static int shiftColor(int color, int amount) {
        return Color.rgb(clamp(Color.red(color) + amount), clamp(Color.green(color) + amount),
                clamp(Color.blue(color) + amount));
    }

    private static int clamp(int value) {
        return Math.max(0, Math.min(255, value));
    }
}
