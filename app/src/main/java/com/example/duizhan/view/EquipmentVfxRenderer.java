package com.example.duizhan.view;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;

import com.example.duizhan.game.GameEntity;
import com.example.duizhan.game.ItemType;

/**
 * Animated equipment auras: golden weapon gleam, armor shimmer, fire boots, crown halos, etc.
 */
public final class EquipmentVfxRenderer {
    private float animPhase;

    public void advance(float dt) {
        animPhase += dt;
        if (animPhase > 1000f) {
            animPhase -= 1000f;
        }
    }

    public void draw(Canvas canvas, Paint paint, GameEntity entity, float lineWidth) {
        if (entity == null || !entity.alive || !finite(entity.x) || !finite(entity.y)) {
            return;
        }
        float r = Math.max(8f, entity.radius);
        float pulse = (float) (0.5f + 0.5f * Math.sin(animPhase * 5.2f + entity.id * 0.17f));
        float spin = animPhase * 2.4f + entity.id * 0.11f;
        float facing = finite(entity.facingRad) ? entity.facingRad : 0f;

        if (entity.weapon != null) {
            drawWeaponVfx(canvas, paint, entity, r, pulse, spin, facing, lineWidth);
        }
        if (entity.armor != null) {
            drawArmorVfx(canvas, paint, entity, r, pulse, spin, lineWidth);
        }
        if (entity.boots != null) {
            drawBootsVfx(canvas, paint, entity, r, pulse, spin, facing, lineWidth);
        }
        if (entity.hat != null) {
            drawHatVfx(canvas, paint, entity, r, pulse, spin, lineWidth);
        }
        if (entity.relic != null) {
            drawRelicVfx(canvas, paint, entity, r, pulse, spin, lineWidth);
        }
    }

    private void drawWeaponVfx(Canvas canvas, Paint paint, GameEntity entity, float r,
                               float pulse, float spin, float facing, float lineWidth) {
        float handX = entity.x + (float) Math.cos(facing) * r * 0.55f;
        float handY = entity.y + (float) Math.sin(facing) * r * 0.55f;
        switch (entity.weapon) {
            case RU_YI_JINGU_BANG:
            case GOLDEN_SCISSORS:
                drawGoldenBurst(canvas, paint, handX, handY, r * 0.55f, pulse);
                drawOrbitingSparks(canvas, paint, handX, handY, r * 0.7f, spin, Color.rgb(250, 204, 21));
                break;
            case DEMON_BLADE:
            case HEAVEN_SWORD:
                drawWeaponGleam(canvas, paint, handX, handY, r * 0.45f, pulse,
                        entity.weapon == ItemType.HEAVEN_SWORD ? Color.rgb(34, 197, 94) : Color.rgb(191, 219, 254));
                break;
            case DRAGON_SPEAR:
            case HUNTIAN_LING:
                drawFireWisp(canvas, paint, handX, handY, r * 0.4f, pulse, spin);
                break;
            case WHITE_TIGER_CLAW:
                drawWeaponGleam(canvas, paint, handX, handY, r * 0.5f, pulse, Color.rgb(248, 113, 113));
                break;
            case STAR_BOW:
                drawOrbitingSparks(canvas, paint, handX, handY, r * 0.65f, spin, Color.rgb(147, 197, 253));
                break;
            case SOUL_CHAIN:
                drawArcaneRing(canvas, paint, handX, handY, r * 0.42f, pulse, Color.rgb(192, 132, 252));
                break;
            case SANJIAN_LIANGREN:
                drawWeaponGleam(canvas, paint, handX, handY, r * 0.48f, pulse, Color.rgb(226, 232, 240));
                break;
            default:
                drawWeaponGleam(canvas, paint, handX, handY, r * 0.38f, pulse * 0.8f, Color.rgb(250, 204, 21));
                break;
        }
    }

    private void drawArmorVfx(Canvas canvas, Paint paint, GameEntity entity, float r,
                              float pulse, float spin, float lineWidth) {
        switch (entity.armor) {
            case BAGUA_ARMOR:
            case BAGUA_CLOAK:
                drawBaguaRing(canvas, paint, entity.x, entity.y, r * 1.55f, spin, Color.rgb(34, 197, 94));
                break;
            case XUANWU_ARMOR:
            case QILIN_COAT:
                drawShieldPulse(canvas, paint, entity.x, entity.y, r * 1.65f, pulse, Color.rgb(59, 130, 246));
                break;
            case DRAGON_SCALE_ROBE:
                drawDragonShimmer(canvas, paint, entity.x, entity.y, r * 1.5f, pulse, spin);
                break;
            case LOTUS_ROBE:
                drawLotusGlow(canvas, paint, entity.x, entity.y, r * 1.45f, pulse, Color.rgb(244, 114, 182));
                break;
            case MOON_ROBE:
                drawLotusGlow(canvas, paint, entity.x, entity.y, r * 1.45f, pulse, Color.rgb(165, 243, 252));
                break;
            case TIGER_MAIL:
                drawShieldPulse(canvas, paint, entity.x, entity.y, r * 1.5f, pulse, Color.rgb(251, 146, 60));
                break;
            default:
                drawArmorShimmer(canvas, paint, entity.x, entity.y, r * 1.35f, pulse, Color.rgb(148, 163, 184));
                break;
        }
    }

    private void drawBootsVfx(Canvas canvas, Paint paint, GameEntity entity, float r,
                              float pulse, float spin, float facing, float lineWidth) {
        float footY = entity.y + r * 0.35f;
        switch (entity.boots) {
            case FIRE_BOOTS:
                drawFireRing(canvas, paint, entity.x, footY, r * 0.85f, pulse, spin);
                break;
            case CLOUD_BOOTS:
                drawCloudPuff(canvas, paint, entity.x, footY, r * 0.7f, pulse);
                break;
            case WIND_BOOTS:
                drawWindStreak(canvas, paint, entity.x, footY, r * 0.75f, facing, pulse);
                break;
            case THUNDER_BOOTS:
                drawThunderSparks(canvas, paint, entity.x, footY, r * 0.8f, spin, pulse);
                break;
            case JADE_BOOTS:
                drawLotusGlow(canvas, paint, entity.x, footY, r * 0.55f, pulse, Color.rgb(74, 222, 128));
                break;
            case BLOOD_BOOTS:
                drawWeaponGleam(canvas, paint, entity.x, footY, r * 0.5f, pulse, Color.rgb(220, 38, 38));
                break;
            default:
                break;
        }
    }

    private void drawHatVfx(Canvas canvas, Paint paint, GameEntity entity, float r,
                            float pulse, float spin, float lineWidth) {
        float headY = entity.y - r * 1.05f;
        switch (entity.hat) {
            case PHOENIX_CROWN:
                drawFireCrown(canvas, paint, entity.x, headY, r * 0.75f, pulse, spin);
                break;
            case THUNDER_CROWN:
                drawThunderSparks(canvas, paint, entity.x, headY, r * 0.7f, spin, pulse);
                break;
            case DRAGON_PEARL:
                drawGoldenBurst(canvas, paint, entity.x, headY - r * 0.15f, r * 0.45f, pulse);
                drawOrbitingSparks(canvas, paint, entity.x, headY, r * 0.85f, spin, Color.rgb(56, 189, 248));
                break;
            case MOON_CROWN:
                drawLotusGlow(canvas, paint, entity.x, headY, r * 0.6f, pulse, Color.rgb(186, 230, 253));
                break;
            case GOD_LIST_RELIC:
                drawDivineGlyphs(canvas, paint, entity.x, headY, r * 0.9f, spin, pulse);
                break;
            case LOTUS_CROWN:
                drawLotusGlow(canvas, paint, entity.x, headY, r * 0.55f, pulse, Color.rgb(250, 204, 21));
                break;
            default:
                drawCrownHalo(canvas, paint, entity.x, headY, r * 0.5f, pulse, Color.rgb(250, 204, 21));
                break;
        }
    }

    private void drawRelicVfx(Canvas canvas, Paint paint, GameEntity entity, float r,
                              float pulse, float spin, float lineWidth) {
        drawOrbitingSparks(canvas, paint, entity.x, entity.y - r * 0.2f, r * 1.2f, spin, Color.rgb(250, 204, 21));
        drawGoldenBurst(canvas, paint, entity.x, entity.y, r * 0.35f, pulse * 0.7f);
    }

    private void drawGoldenBurst(Canvas canvas, Paint paint, float x, float y, float radius, float pulse) {
        if (!finite(x) || !finite(y)) {
            return;
        }
        float glow = safeRadius(radius * (0.8f + pulse * 0.35f));
        paint.setStyle(Paint.Style.FILL);
        paint.setShader(new RadialGradient(x, y, glow,
                new int[]{Color.argb(120, 250, 204, 21), Color.argb(0, 250, 204, 21)},
                new float[]{0f, 1f}, Shader.TileMode.CLAMP));
        canvas.drawCircle(x, y, glow, paint);
        paint.setShader(null);
    }

    private void drawWeaponGleam(Canvas canvas, Paint paint, float x, float y, float radius,
                                 float pulse, int color) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3f + pulse * 2f);
        paint.setColor(Color.argb((int) (90 + pulse * 110), Color.red(color), Color.green(color), Color.blue(color)));
        canvas.drawCircle(x, y, radius * (0.7f + pulse * 0.25f), paint);
    }

    private void drawOrbitingSparks(Canvas canvas, Paint paint, float cx, float cy, float radius,
                                    float spin, int color) {
        paint.setStyle(Paint.Style.FILL);
        for (int i = 0; i < 4; i++) {
            float ang = spin + i * 1.57f;
            float px = cx + (float) Math.cos(ang) * radius;
            float py = cy + (float) Math.sin(ang) * radius * 0.45f;
            paint.setColor(Color.argb(150, Color.red(color), Color.green(color), Color.blue(color)));
            canvas.drawCircle(px, py, 4f + i * 0.5f, paint);
        }
    }

    private void drawFireWisp(Canvas canvas, Paint paint, float x, float y, float radius,
                              float pulse, float spin) {
        paint.setStyle(Paint.Style.FILL);
        for (int i = 0; i < 3; i++) {
            float ang = spin * 1.4f + i * 2.1f;
            float fx = x + (float) Math.cos(ang) * radius * 0.35f;
            float fy = y + (float) Math.sin(ang) * radius * 0.25f - i * 6f;
            int alpha = (int) (120 + pulse * 80);
            paint.setColor(Color.argb(alpha, 251, 146, 60));
            canvas.drawCircle(fx, fy, 5f + pulse * 3f, paint);
            paint.setColor(Color.argb(alpha / 2, 250, 204, 21));
            canvas.drawCircle(fx, fy - 4f, 3f + pulse * 2f, paint);
        }
    }

    private void drawArcaneRing(Canvas canvas, Paint paint, float x, float y, float radius,
                                float pulse, int color) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2.5f + pulse);
        paint.setColor(Color.argb((int) (100 + pulse * 100), Color.red(color), Color.green(color), Color.blue(color)));
        canvas.drawCircle(x, y, radius, paint);
    }

    private void drawBaguaRing(Canvas canvas, Paint paint, float x, float y, float radius,
                               float spin, int color) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3f);
        paint.setColor(Color.argb(130, Color.red(color), Color.green(color), Color.blue(color)));
        canvas.save();
        canvas.translate(x, y);
        canvas.rotate((float) Math.toDegrees(spin));
        canvas.drawCircle(0f, 0f, radius, paint);
        canvas.drawLine(-radius, 0f, radius, 0f, paint);
        canvas.drawLine(0f, -radius, 0f, radius, paint);
        canvas.restore();
    }

    private void drawShieldPulse(Canvas canvas, Paint paint, float x, float y, float radius,
                                 float pulse, int color) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(4f + pulse * 3f);
        paint.setColor(Color.argb((int) (70 + pulse * 120), Color.red(color), Color.green(color), Color.blue(color)));
        canvas.drawCircle(x, y, radius * (0.85f + pulse * 0.2f), paint);
    }

    private void drawDragonShimmer(Canvas canvas, Paint paint, float x, float y, float radius,
                                   float pulse, float spin) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2f);
        for (int i = 0; i < 6; i++) {
            float ang = spin + i * 1.05f;
            float sx = x + (float) Math.cos(ang) * radius;
            float sy = y + (float) Math.sin(ang) * radius * 0.55f;
            paint.setColor(Color.argb((int) (80 + pulse * 100), 59, 130, 246));
            canvas.drawLine(x, y, sx, sy, paint);
        }
    }

    private void drawLotusGlow(Canvas canvas, Paint paint, float x, float y, float radius,
                               float pulse, int color) {
        if (!finite(x) || !finite(y)) {
            return;
        }
        float glow = safeRadius(radius);
        paint.setStyle(Paint.Style.FILL);
        paint.setShader(new RadialGradient(x, y, glow,
                new int[]{Color.argb((int) (60 + pulse * 80), Color.red(color), Color.green(color), Color.blue(color)),
                        Color.argb(0, Color.red(color), Color.green(color), Color.blue(color))},
                new float[]{0.2f, 1f}, Shader.TileMode.CLAMP));
        canvas.drawCircle(x, y, glow, paint);
        paint.setShader(null);
    }

    private void drawArmorShimmer(Canvas canvas, Paint paint, float x, float y, float radius,
                                  float pulse, int color) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2f);
        paint.setColor(Color.argb((int) (50 + pulse * 70), Color.red(color), Color.green(color), Color.blue(color)));
        canvas.drawCircle(x, y, radius * (0.9f + pulse * 0.1f), paint);
    }

    private void drawFireRing(Canvas canvas, Paint paint, float x, float y, float radius,
                              float pulse, float spin) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3f + pulse * 2f);
        for (int i = 0; i < 5; i++) {
            float ang = spin * 1.8f + i * 1.26f;
            float fx = x + (float) Math.cos(ang) * radius * 0.65f;
            float fy = y + (float) Math.sin(ang) * radius * 0.3f;
            paint.setColor(Color.argb(140, 239, 68, 68));
            canvas.drawCircle(fx, fy, 6f + pulse * 4f, paint);
            paint.setColor(Color.argb(100, 250, 204, 21));
            canvas.drawCircle(fx, fy - 5f, 3f + pulse * 2f, paint);
        }
    }

    private void drawCloudPuff(Canvas canvas, Paint paint, float x, float y, float radius, float pulse) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb((int) (60 + pulse * 50), 226, 232, 240));
        canvas.drawCircle(x - radius * 0.3f, y, radius * 0.35f, paint);
        canvas.drawCircle(x + radius * 0.25f, y - 3f, radius * 0.3f, paint);
        canvas.drawCircle(x, y - 5f, radius * 0.28f, paint);
    }

    private void drawWindStreak(Canvas canvas, Paint paint, float x, float y, float radius,
                                float facing, float pulse) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2f + pulse);
        paint.setColor(Color.argb(120, 134, 239, 172));
        float backX = x - (float) Math.cos(facing) * radius * 0.5f;
        float backY = y - (float) Math.sin(facing) * radius * 0.5f;
        canvas.drawLine(backX, backY, backX - (float) Math.cos(facing) * 18f, backY - (float) Math.sin(facing) * 18f, paint);
        canvas.drawLine(backX + 6f, backY + 4f, backX - (float) Math.cos(facing) * 14f + 6f, backY - (float) Math.sin(facing) * 14f + 4f, paint);
    }

    private void drawThunderSparks(Canvas canvas, Paint paint, float x, float y, float radius,
                                   float spin, float pulse) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2.5f);
        paint.setColor(Color.argb((int) (130 + pulse * 100), 250, 204, 21));
        for (int i = 0; i < 3; i++) {
            float ang = spin * 2.2f + i * 2.4f;
            float sx = x + (float) Math.cos(ang) * radius * 0.5f;
            float sy = y + (float) Math.sin(ang) * radius * 0.35f;
            canvas.drawLine(sx, sy, sx + 8f, sy - 12f - pulse * 6f, paint);
            canvas.drawLine(sx + 8f, sy - 12f - pulse * 6f, sx + 2f, sy - 18f - pulse * 8f, paint);
        }
    }

    private void drawFireCrown(Canvas canvas, Paint paint, float x, float y, float radius,
                               float pulse, float spin) {
        drawCrownHalo(canvas, paint, x, y, radius, pulse, Color.rgb(250, 204, 21));
        drawFireWisp(canvas, paint, x, y - radius * 0.2f, radius * 0.6f, pulse, spin);
    }

    private void drawCrownHalo(Canvas canvas, Paint paint, float x, float y, float radius,
                               float pulse, int color) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3f);
        paint.setColor(Color.argb((int) (100 + pulse * 100), Color.red(color), Color.green(color), Color.blue(color)));
        canvas.drawArc(x - radius, y - radius * 0.4f, x + radius, y + radius * 0.8f, 200f, 140f, false, paint);
    }

    private void drawDivineGlyphs(Canvas canvas, Paint paint, float x, float y, float radius,
                                  float spin, float pulse) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2f);
        paint.setColor(Color.argb((int) (90 + pulse * 100), 250, 204, 21));
        for (int i = 0; i < 4; i++) {
            float ang = spin + i * 1.57f;
            float gx = x + (float) Math.cos(ang) * radius * 0.55f;
            float gy = y + (float) Math.sin(ang) * radius * 0.3f;
            canvas.drawCircle(gx, gy, 5f + pulse * 2f, paint);
            canvas.drawLine(gx - 4f, gy, gx + 4f, gy, paint);
        }
    }

    private static float safeRadius(float radius) {
        if (!finite(radius)) {
            return 8f;
        }
        return Math.max(4f, radius);
    }

    private static boolean finite(float value) {
        return Float.isFinite(value);
    }
}
