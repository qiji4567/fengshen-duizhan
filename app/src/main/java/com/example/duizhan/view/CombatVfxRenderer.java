package com.example.duizhan.view;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Shader;

import com.example.duizhan.game.DamageType;
import com.example.duizhan.game.EffectKind;
import com.example.duizhan.game.Projectile;
import com.example.duizhan.game.ProjectileVisual;
import com.example.duizhan.game.VisualEffect;

/**
 * Procedural combat sprites and hit animations — no text labels on the battlefield.
 */
public final class CombatVfxRenderer {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path = new Path();
    private float animPhase;
    private Bitmap sparkBitmap;
    private Bitmap glowBitmap;

    public void advance(float dt) {
        animPhase += dt;
        if (animPhase > 1000f) {
            animPhase -= 1000f;
        }
    }

    public void ensureBitmaps() {
        if (sparkBitmap == null) {
            sparkBitmap = buildSparkBitmap();
        }
        if (glowBitmap == null) {
            glowBitmap = buildGlowBitmap();
        }
    }

    public void drawProjectile(Canvas canvas, Projectile projectile) {
        if (projectile == null || !finite(projectile.x) || !finite(projectile.y)) {
            return;
        }
        ensureBitmaps();
        float pulse = (float) (0.5f + 0.5f * Math.sin(animPhase * 14f + projectile.x * 0.03f));
        float r = Math.max(3f, projectile.radius);
        float angle = finite(projectile.angleRad) ? projectile.angleRad : 0f;
        switch (projectile.visual) {
            case ARROW:
                drawArrow(canvas, projectile.x, projectile.y, r, projectile.color, pulse, angle);
                break;
            case MAGIC_ORB:
                drawMagicOrb(canvas, projectile.x, projectile.y, r, projectile.color, pulse);
                break;
            case TALISMAN:
                drawTalisman(canvas, projectile.x, projectile.y, r, pulse, angle);
                break;
            case TOWER_BLAST:
                drawTowerBlast(canvas, projectile.x, projectile.y, r, projectile.color, pulse);
                break;
            case GOLD_SEAL:
                drawGoldSeal(canvas, projectile.x, projectile.y, r, pulse, angle);
                break;
            case STAR:
                drawStarProjectile(canvas, projectile.x, projectile.y, r, pulse, angle);
                break;
            case FIRE_ORB:
                drawFireOrb(canvas, projectile.x, projectile.y, r, pulse);
                break;
            case LIGHTNING:
                drawLightningOrb(canvas, projectile.x, projectile.y, r, pulse, angle);
                break;
            default:
                drawBolt(canvas, projectile.x, projectile.y, r, projectile.color, pulse, angle);
                break;
        }
        drawProjectileTrail(canvas, projectile, angle);
    }

    public void drawEffect(Canvas canvas, VisualEffect effect) {
        if (effect == null || !finite(effect.x) || !finite(effect.y)) {
            return;
        }
        ensureBitmaps();
        float progress = effect.maxTtl <= 0f ? 1f : 1f - effect.ttl / effect.maxTtl;
        int alpha = Math.max(0, Math.min(255, (int) (230 * effect.ttl / Math.max(0.01f, effect.maxTtl))));
        switch (effect.kind) {
            case HIT_BURST:
                drawHitBurst(canvas, effect.x, effect.y, effect.radius, effect.color, effect.intensity,
                        effect.damageType, progress, alpha);
                break;
            case SLASH_ARC:
                drawSlashArc(canvas, effect.x, effect.y, effect.radius,
                        finite(effect.angleRad) ? effect.angleRad : 0f, effect.color, progress, alpha);
                break;
            case HEAL_BURST:
                drawHealBurst(canvas, effect.x, effect.y, effect.radius, progress, alpha);
                break;
            case STATUS_BURST:
                drawStatusBurst(canvas, effect.x, effect.y, effect.radius, effect.color, progress, alpha);
                break;
            case SKILL_BURST:
                drawSkillBurst(canvas, effect.x, effect.y, effect.radius, effect.color, effect.intensity, progress, alpha);
                break;
            case RING:
                drawShockRing(canvas, effect.x, effect.y, effect.radius, effect.color, progress, alpha);
                break;
            case LINE:
                if (finite(effect.x2) && finite(effect.y2)) {
                    drawEnergyBeam(canvas, effect.x, effect.y, effect.x2, effect.y2, effect.color, progress, alpha);
                }
                break;
            case TEXT:
            default:
                drawSkillBurst(canvas, effect.x, effect.y, 42f * effect.textScale, effect.color,
                        effect.textScale, progress, alpha);
                break;
        }
    }

    public void drawEntityHitFlash(Canvas canvas, float x, float y, float radius, float hitTimer, int teamColor) {
        if (hitTimer <= 0f) {
            return;
        }
        ensureBitmaps();
        float t = hitTimer / 0.18f;
        float expand = safeRadius(radius * (1.2f + (1f - t) * 0.9f));
        paint.setShader(new RadialGradient(x, y, expand,
                new int[]{Color.argb((int) (200 * t), 255, 255, 255), Color.argb(0, 255, 255, 255)},
                new float[]{0f, 1f}, Shader.TileMode.CLAMP));
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(x, y, expand, paint);
        paint.setShader(null);
        for (int i = 0; i < 6; i++) {
            float ang = animPhase * 8f + i * 1.05f;
            float px = x + (float) Math.cos(ang) * radius * (0.8f + (1f - t));
            float py = y + (float) Math.sin(ang) * radius * 0.55f * (0.8f + (1f - t));
            paint.setColor(Color.argb((int) (200 * t), Color.red(teamColor), Color.green(teamColor), Color.blue(teamColor)));
            canvas.drawCircle(px, py, 3f + (1f - t) * 4f, paint);
        }
    }

    private void drawProjectileTrail(Canvas canvas, Projectile projectile, float angleRad) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(Math.max(2f, projectile.radius * 0.45f));
        paint.setColor(Color.argb(90, Color.red(projectile.color), Color.green(projectile.color), Color.blue(projectile.color)));
        float tail = projectile.radius * 3.2f;
        float cos = (float) Math.cos(angleRad);
        float sin = (float) Math.sin(angleRad);
        canvas.drawLine(projectile.x - cos * tail, projectile.y - sin * tail,
                projectile.x - cos * tail * 0.2f, projectile.y - sin * tail * 0.2f, paint);
    }

    private void drawBolt(Canvas canvas, float x, float y, float r, int color, float pulse, float angleRad) {
        canvas.save();
        canvas.translate(x, y);
        canvas.rotate((float) Math.toDegrees(angleRad));
        float glow = safeRadius(r * (2.8f + pulse * 0.35f));
        paint.setShader(new RadialGradient(0f, 0f, glow,
                new int[]{Color.argb(220, Color.red(color), Color.green(color), Color.blue(color)),
                        Color.argb(0, Color.red(color), Color.green(color), Color.blue(color))},
                new float[]{0.15f, 1f}, Shader.TileMode.CLAMP));
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(0f, 0f, r * (1.6f + pulse * 0.35f), paint);
        paint.setShader(null);
        paint.setColor(Color.WHITE);
        canvas.drawCircle(0f, 0f, r * 0.45f, paint);
        canvas.restore();
    }

    private void drawArrow(Canvas canvas, float x, float y, float r, int color, float pulse, float angleRad) {
        canvas.save();
        canvas.translate(x, y);
        canvas.rotate((float) Math.toDegrees(angleRad));
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(200, Color.red(color), Color.green(color), Color.blue(color)));
        path.reset();
        path.moveTo(r * 2.2f, 0f);
        path.lineTo(-r * 1.2f, -r * 0.7f);
        path.lineTo(-r * 0.4f, 0f);
        path.lineTo(-r * 1.2f, r * 0.7f);
        path.close();
        canvas.drawPath(path, paint);
        paint.setColor(Color.rgb(250, 204, 21));
        canvas.drawRect(-r * 2.4f, -r * 0.15f, -r * 0.5f, r * 0.15f, paint);
        paint.setShader(new RadialGradient(0f, 0f, safeRadius(r * 1.4f),
                new int[]{Color.argb(220, Color.red(color), Color.green(color), Color.blue(color)),
                        Color.argb(0, Color.red(color), Color.green(color), Color.blue(color))},
                new float[]{0.15f, 1f}, Shader.TileMode.CLAMP));
        canvas.drawCircle(0f, 0f, r * 0.55f, paint);
        paint.setShader(null);
        paint.setColor(Color.WHITE);
        canvas.drawCircle(0f, 0f, r * 0.22f, paint);
        canvas.restore();
    }

    private void drawMagicOrb(Canvas canvas, float x, float y, float r, int color, float pulse) {
        float glow = safeRadius(r * (3f + pulse * 0.5f));
        paint.setShader(new RadialGradient(x, y, glow,
                new int[]{Color.argb(240, 233, 213, 255), Color.argb(120, 192, 132, 252), Color.argb(0, 124, 58, 237)},
                new float[]{0f, 0.45f, 1f}, Shader.TileMode.CLAMP));
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(x, y, r * (2f + pulse * 0.5f), paint);
        paint.setShader(null);
        for (int i = 0; i < 3; i++) {
            float ang = animPhase * 4f + i * 2.1f;
            paint.setColor(Color.argb(160, 216, 180, 254));
            canvas.drawCircle(x + (float) Math.cos(ang) * r * 0.9f, y + (float) Math.sin(ang) * r * 0.5f, r * 0.25f, paint);
        }
    }

    private void drawTalisman(Canvas canvas, float x, float y, float r, float pulse, float angleRad) {
        canvas.save();
        canvas.translate(x, y);
        canvas.rotate((float) Math.toDegrees(angleRad));
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(250, 204, 21));
        canvas.drawRect(-r * 0.8f, -r * 1.2f, r * 0.8f, r * 1.2f, paint);
        paint.setColor(Color.rgb(220, 38, 38));
        canvas.drawRect(-r * 0.55f, -r * 0.9f, r * 0.55f, r * 0.9f, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2f);
        paint.setColor(Color.rgb(254, 243, 199));
        canvas.drawLine(-r * 0.3f, -r * 0.4f, r * 0.3f, r * 0.4f, paint);
        canvas.drawLine(r * 0.3f, -r * 0.4f, -r * 0.3f, r * 0.4f, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(120, 250, 204, 21));
        canvas.drawCircle(0f, 0f, r * (1.8f + pulse), paint);
        canvas.restore();
    }

    private void drawTowerBlast(Canvas canvas, float x, float y, float r, int color, float pulse) {
        float glow = safeRadius(r * (3.5f + pulse * 0.6f));
        paint.setShader(new RadialGradient(x, y, glow,
                new int[]{Color.argb(255, 251, 146, 60), Color.argb(160, 239, 68, 68), Color.argb(0, 127, 29, 29)},
                new float[]{0f, 0.35f, 1f}, Shader.TileMode.CLAMP));
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(x, y, r * (2.2f + pulse * 0.6f), paint);
        paint.setShader(null);
    }

    private void drawGoldSeal(Canvas canvas, float x, float y, float r, float pulse, float angleRad) {
        canvas.save();
        canvas.translate(x, y);
        canvas.rotate((float) Math.toDegrees(angleRad));
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(250, 204, 21));
        canvas.drawCircle(0f, 0f, r * (1.4f + pulse * 0.3f), paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3f);
        paint.setColor(Color.rgb(180, 120, 20));
        canvas.drawCircle(0f, 0f, r * (1.1f + pulse * 0.2f), paint);
        paint.setColor(Color.rgb(254, 243, 199));
        canvas.drawLine(-r, 0f, r, 0f, paint);
        canvas.drawLine(0f, -r, 0f, r, paint);
        canvas.restore();
    }

    private void drawStarProjectile(Canvas canvas, float x, float y, float r, float pulse, float angleRad) {
        canvas.save();
        canvas.translate(x, y);
        canvas.rotate((float) Math.toDegrees(angleRad));
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(250, 204, 21));
        for (int i = 0; i < 5; i++) {
            canvas.save();
            canvas.rotate(i * 72f + animPhase * 120f);
            canvas.drawRect(-r * 0.15f, -r * 1.4f, r * 0.15f, r * 0.2f, paint);
            canvas.restore();
        }
        paint.setColor(Color.argb(180, 134, 239, 172));
        canvas.drawCircle(0f, 0f, r * (0.7f + pulse * 0.3f), paint);
        canvas.restore();
    }

    private void drawFireOrb(Canvas canvas, float x, float y, float r, float pulse) {
        for (int i = 0; i < 4; i++) {
            float ang = animPhase * 6f + i * 1.57f;
            paint.setColor(Color.argb(180, 251, 146, 60));
            canvas.drawCircle(x + (float) Math.cos(ang) * r * 0.4f, y + (float) Math.sin(ang) * r * 0.25f - i * 2f,
                    r * (0.55f + pulse * 0.25f), paint);
        }
        paint.setColor(Color.rgb(250, 204, 21));
        canvas.drawCircle(x, y - r * 0.3f, r * 0.35f, paint);
    }

    private void drawLightningOrb(Canvas canvas, float x, float y, float r, float pulse, float angleRad) {
        canvas.save();
        canvas.translate(x, y);
        canvas.rotate((float) Math.toDegrees(angleRad));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3f);
        paint.setColor(Color.rgb(250, 204, 21));
        canvas.drawLine(-r, r * 0.4f, 0f, -r, paint);
        canvas.drawLine(0f, -r, r * 0.7f, 0f, paint);
        canvas.drawLine(r * 0.7f, 0f, -r * 0.3f, r * 0.9f, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(150, 147, 197, 253));
        canvas.drawCircle(0f, 0f, r * (0.8f + pulse * 0.4f), paint);
        canvas.restore();
    }

    private void drawHitBurst(Canvas canvas, float x, float y, float radius, int color, float intensity,
                              DamageType damageType, float progress, int alpha) {
        float safeIntensity = finite(intensity) ? Math.max(0.2f, intensity) : 1f;
        float size = safeRadius(radius * (0.55f + safeIntensity * 0.9f) * (1f + progress * 0.65f));
        int core = damageType == DamageType.MAGIC ? Color.rgb(216, 180, 254)
                : damageType == DamageType.TRUE_DAMAGE ? Color.rgb(250, 204, 21) : color;
        paint.setShader(new RadialGradient(x, y, size,
                new int[]{Color.argb(alpha, Color.red(core), Color.green(core), Color.blue(core)),
                        Color.argb(0, Color.red(core), Color.green(core), Color.blue(core))},
                new float[]{0.1f, 1f}, Shader.TileMode.CLAMP));
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(x, y, size, paint);
        paint.setShader(null);
        int sparks = Math.min(14, 6 + Math.round(safeIntensity * 6f));
        if (sparkBitmap != null) {
            for (int i = 0; i < sparks; i++) {
                float ang = (float) (i * (Math.PI * 2 / sparks) + progress * 2.4f);
                float dist = size * (0.45f + progress * 0.75f);
                float px = x + (float) Math.cos(ang) * dist;
                float py = y + (float) Math.sin(ang) * dist * 0.65f;
                paint.setColor(Color.argb(alpha, Color.red(core), Color.green(core), Color.blue(core)));
                canvas.drawBitmap(sparkBitmap, px - 6f, py - 6f, paint);
            }
        }
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(Math.max(1f, 4f - progress * 2f));
        paint.setColor(Color.argb(Math.max(1, alpha / 2), 255, 255, 255));
        canvas.drawCircle(x, y, size * 0.7f, paint);
    }

    private void drawSlashArc(Canvas canvas, float x, float y, float radius, float angle, int color,
                              float progress, int alpha) {
        float arcRadius = safeRadius(radius);
        canvas.save();
        canvas.translate(x, y);
        canvas.rotate((float) Math.toDegrees(angle));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(Math.max(2f, 10f - progress * 6f));
        paint.setColor(Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color)));
        canvas.drawArc(-arcRadius, -arcRadius * 0.35f, arcRadius, arcRadius * 0.35f, -60f, 120f, false, paint);
        paint.setStrokeWidth(4f);
        paint.setColor(Color.argb(Math.max(1, alpha / 2), 255, 255, 255));
        canvas.drawArc(-arcRadius * 0.85f, -arcRadius * 0.28f, arcRadius * 0.85f, arcRadius * 0.28f, -50f, 100f, false, paint);
        canvas.restore();
    }

    private void drawHealBurst(Canvas canvas, float x, float y, float radius, float progress, int alpha) {
        float glow = safeRadius(radius * (1f + progress));
        paint.setShader(new RadialGradient(x, y, glow,
                new int[]{Color.argb(alpha, 134, 239, 172), Color.argb(0, 34, 197, 94)},
                new float[]{0.2f, 1f}, Shader.TileMode.CLAMP));
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(x, y, radius * (0.8f + progress * 0.5f), paint);
        paint.setShader(null);
        for (int i = 0; i < 5; i++) {
            float px = x + (i - 2) * radius * 0.22f;
            float py = y - radius * (0.4f + progress * 0.9f + i * 0.08f);
            paint.setColor(Color.argb(alpha, 187, 247, 208));
            canvas.drawCircle(px, py, 4f, paint);
        }
    }

    private void drawStatusBurst(Canvas canvas, float x, float y, float radius, int color, float progress, int alpha) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5f);
        paint.setColor(Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color)));
        for (int i = 0; i < 3; i++) {
            canvas.drawCircle(x, y, radius * (0.5f + i * 0.22f + progress * 0.35f), paint);
        }
    }

    private void drawSkillBurst(Canvas canvas, float x, float y, float radius, int color, float intensity,
                                float progress, int alpha) {
        float safeIntensity = finite(intensity) ? Math.max(0.3f, intensity) : 1f;
        float size = safeRadius(radius * (1f + safeIntensity * 0.5f) * (0.7f + progress * 0.6f));
        paint.setShader(new RadialGradient(x, y, size,
                new int[]{Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color)),
                        Color.argb(0, Color.red(color), Color.green(color), Color.blue(color))},
                new float[]{0f, 1f}, Shader.TileMode.CLAMP));
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(x, y, size, paint);
        paint.setShader(null);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(6f);
        paint.setColor(Color.argb(alpha, 255, 255, 255));
        canvas.drawCircle(x, y, size * 0.65f, paint);
    }

    private void drawShockRing(Canvas canvas, float x, float y, float radius, int color, float progress, int alpha) {
        float ringRadius = safeRadius(radius);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(Math.max(2f, 12f - progress * 8f));
        paint.setColor(Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color)));
        canvas.drawCircle(x, y, ringRadius * (0.55f + progress * 0.55f), paint);
        paint.setStrokeWidth(4f);
        paint.setColor(Color.argb(Math.max(1, alpha / 2), 255, 255, 255));
        canvas.drawCircle(x, y, ringRadius * (0.4f + progress * 0.4f), paint);
        paint.setShader(new RadialGradient(x, y, ringRadius,
                new int[]{Color.argb(alpha / 3, Color.red(color), Color.green(color), Color.blue(color)),
                        Color.argb(0, Color.red(color), Color.green(color), Color.blue(color))},
                new float[]{0.3f, 1f}, Shader.TileMode.CLAMP));
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(x, y, radius * 0.35f, paint);
        paint.setShader(null);
    }

    private void drawEnergyBeam(Canvas canvas, float x1, float y1, float x2, float y2, int color,
                                float progress, int alpha) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float lenSq = dx * dx + dy * dy;
        if (lenSq < 4f) {
            drawHitBurst(canvas, x2, y2, 28f, color, 1.1f, null, progress, alpha);
            return;
        }
        float len = (float) Math.sqrt(lenSq);
        float nx = dx / len;
        float ny = dy / len;
        float ex2 = x2 + nx * 8f;
        float ey2 = y2 + ny * 8f;
        paint.setShader(new LinearGradient(x1, y1, ex2, ey2,
                Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color)),
                Color.argb(0, Color.red(color), Color.green(color), Color.blue(color)),
                Shader.TileMode.CLAMP));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(Math.max(2f, 14f - progress * 8f));
        canvas.drawLine(x1, y1, x2, y2, paint);
        paint.setShader(null);
        paint.setStrokeWidth(5f);
        paint.setColor(Color.argb(alpha, 255, 255, 255));
        canvas.drawLine(x1, y1, x2, y2, paint);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(x2, y2, 12f + progress * 8f, paint);
    }

    private static float safeRadius(float radius) {
        if (!finite(radius)) {
            return 12f;
        }
        return Math.max(4f, radius);
    }

    private static boolean finite(float value) {
        return Float.isFinite(value);
    }

    private Bitmap buildSparkBitmap() {
        Bitmap bitmap = Bitmap.createBitmap(12, 12, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bitmap);
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(Color.WHITE);
        c.drawCircle(6f, 6f, 4f, p);
        p.setColor(Color.rgb(250, 204, 21));
        c.drawCircle(6f, 6f, 2f, p);
        return bitmap;
    }

    private Bitmap buildGlowBitmap() {
        Bitmap bitmap = Bitmap.createBitmap(32, 32, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bitmap);
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setShader(new RadialGradient(16f, 16f, 16f,
                new int[]{Color.argb(220, 255, 255, 255), Color.argb(0, 255, 255, 255)},
                new float[]{0f, 1f}, Shader.TileMode.CLAMP));
        c.drawCircle(16f, 16f, 16f, p);
        return bitmap;
    }
}
