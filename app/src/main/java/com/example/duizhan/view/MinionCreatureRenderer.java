package com.example.duizhan.view;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

import com.example.duizhan.game.GameEntity;
import com.example.duizhan.game.Team;
import com.example.duizhan.game.UnitKind;
import com.example.duizhan.game.util.GameMath;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/** Lane minions and wild monsters as天兵/妖魔/鬼怪. */
public final class MinionCreatureRenderer {
    private static final class PoseState {
        float lastX;
        float lastY;
        float displayFacingDeg;
        float walkPhase;
    }

    private final Map<Long, PoseState> poses = new HashMap<>();
    private final RectF rect = new RectF();
    private final Path path = new Path();

    public void prune(Set<Long> activeIds) {
        Iterator<Map.Entry<Long, PoseState>> it = poses.entrySet().iterator();
        while (it.hasNext()) {
            if (!activeIds.contains(it.next().getKey())) {
                it.remove();
            }
        }
    }

    public void draw(Canvas canvas, Paint paint, GameEntity entity, float lineWidth) {
        PoseState pose = poses.computeIfAbsent(entity.id, id -> new PoseState());
        float moveDist = GameMath.distance(entity.x, entity.y, pose.lastX, pose.lastY);
        pose.lastX = entity.x;
        pose.lastY = entity.y;
        pose.displayFacingDeg = lerpAngle(pose.displayFacingDeg,
                (float) Math.toDegrees(entity.facingRad), 0.3f);
        boolean moving = moveDist > 0.8f;
        if (moving) {
            pose.walkPhase += moveDist * 0.2f;
        }
        float legSwing = moving ? (float) Math.sin(pose.walkPhase) * 24f : 0f;
        boolean attacking = entity.attackTimer > entity.attackCooldown * 0.82f;

        canvas.save();
        canvas.translate(entity.x, entity.y);
        UprightFacing.apply(canvas, entity.facingRad);
        float scale = entity.radius / 18f;

        if (entity.kind == UnitKind.MONSTER) {
            drawWildMonster(canvas, paint, legSwing, attacking, lineWidth, scale * 1.35f);
        } else if (entity.team == Team.BLUE) {
            drawHeavenUnit(canvas, paint, entity.kind, legSwing, attacking, lineWidth, scale);
        } else {
            drawDemonUnit(canvas, paint, entity.kind, legSwing, attacking, lineWidth, scale);
        }
        canvas.restore();
    }

    public void drawDead(Canvas canvas, Paint paint, GameEntity entity, float lineWidth) {
        canvas.save();
        canvas.translate(entity.x, entity.y + entity.radius * 0.3f);
        UprightFacing.apply(canvas, entity.facingRad);
        canvas.rotate(90f);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(80, 0, 0, 0));
        canvas.drawOval(-entity.radius, 4f, entity.radius, 14f, paint);
        paint.setColor(Color.argb(120, 100, 100, 100));
        rect.set(-entity.radius * 0.8f, -6f, entity.radius * 0.8f, 8f);
        canvas.drawRoundRect(rect, 6f, 6f, paint);
        canvas.restore();
    }

    private void drawHeavenUnit(Canvas canvas, Paint paint, UnitKind kind, float legSwing,
                                boolean attacking, float lineWidth, float scale) {
        canvas.scale(scale, scale);
        int armor = Color.rgb(191, 219, 254);
        int metal = Color.rgb(148, 163, 184);
        int gold = Color.rgb(250, 204, 21);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(65, 0, 0, 0));
        canvas.drawOval(-14f, 12f, 14f, 22f, paint);
        drawCreatureLeg(canvas, paint, -6f, legSwing, armor, metal, lineWidth);
        drawCreatureLeg(canvas, paint, 6f, -legSwing, armor, metal, lineWidth);
        paint.setColor(metal);
        rect.set(-10f, -18f, 10f, 8f);
        canvas.drawRoundRect(rect, 5f, 5f, paint);
        paint.setColor(armor);
        rect.set(-8f, -20f, 8f, 6f);
        canvas.drawRoundRect(rect, 4f, 4f, paint);
        paint.setColor(gold);
        canvas.drawRect(-8f, -8f, 8f, -4f, paint);
        paint.setColor(Color.rgb(255, 224, 196));
        canvas.drawCircle(0f, -24f, 7f, paint);
        paint.setColor(metal);
        canvas.drawRect(-9f, -30f, 9f, -24f, paint);
        if (kind == UnitKind.BRUTE) {
            canvas.scale(1.25f, 1.25f);
            paint.setColor(gold);
            canvas.drawRect(-12f, -10f, 12f, -4f, paint);
        }
        drawHeavenWeapon(canvas, paint, kind, attacking, metal, gold, lineWidth);
    }

    private void drawHeavenWeapon(Canvas canvas, Paint paint, UnitKind kind, boolean attacking,
                                  int metal, int gold, float lineWidth) {
        canvas.save();
        canvas.translate(9f, -12f);
        canvas.rotate(attacking ? -50f : -10f);
        paint.setStyle(Paint.Style.FILL);
        if (kind == UnitKind.RANGED_MINION) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(lineWidth);
            paint.setColor(metal);
            canvas.drawArc(new RectF(-4f, 4f, 4f, 22f), -90f, 180f, false, paint);
        } else if (kind == UnitKind.BRUTE) {
            paint.setColor(metal);
            canvas.drawRect(-2f, 0f, 2f, 26f, paint);
            paint.setColor(gold);
            path.reset();
            path.moveTo(2f, 0f);
            path.lineTo(12f, 6f);
            path.lineTo(2f, 12f);
            path.close();
            canvas.drawPath(path, paint);
        } else {
            paint.setColor(metal);
            canvas.drawRect(-1.5f, 0f, 1.5f, 22f, paint);
            paint.setColor(gold);
            canvas.drawRect(-4f, 18f, 4f, 22f, paint);
        }
        canvas.restore();
    }

    private void drawDemonUnit(Canvas canvas, Paint paint, UnitKind kind, float legSwing,
                               boolean attacking, float lineWidth, float scale) {
        canvas.scale(scale, scale);
        int body = Color.rgb(127, 29, 29);
        int glow = Color.rgb(74, 222, 128);
        int dark = Color.rgb(30, 41, 59);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(70, 0, 0, 0));
        canvas.drawOval(-14f, 12f, 14f, 22f, paint);
        drawCreatureLeg(canvas, paint, -6f, legSwing, dark, body, lineWidth);
        drawCreatureLeg(canvas, paint, 6f, -legSwing, dark, body, lineWidth);
        paint.setColor(body);
        rect.set(-10f, -18f, 10f, 8f);
        canvas.drawRoundRect(rect, 6f, 6f, paint);
        paint.setColor(glow);
        canvas.drawCircle(0f, -24f, 8f, paint);
        paint.setColor(dark);
        canvas.drawCircle(-3f, -24f, 2f, paint);
        canvas.drawCircle(3f, -24f, 2f, paint);
        path.reset();
        path.moveTo(-4f, -18f);
        path.lineTo(0f, -14f);
        path.lineTo(4f, -18f);
        canvas.drawPath(path, paint);
        if (kind == UnitKind.BRUTE) {
            paint.setColor(Color.rgb(153, 27, 27));
            canvas.drawCircle(-12f, -16f, 4f, paint);
            canvas.drawCircle(12f, -16f, 4f, paint);
        }
        drawDemonWeapon(canvas, paint, kind, attacking, glow, dark, lineWidth);
    }

    private void drawDemonWeapon(Canvas canvas, Paint paint, UnitKind kind, boolean attacking,
                                 int glow, int dark, float lineWidth) {
        canvas.save();
        canvas.translate(9f, -12f);
        canvas.rotate(attacking ? -55f : -12f);
        paint.setStyle(Paint.Style.FILL);
        if (kind == UnitKind.RANGED_MINION) {
            paint.setColor(glow);
            canvas.drawCircle(0f, 14f, 5f, paint);
            paint.setColor(dark);
            canvas.drawRect(-1f, 4f, 1f, 14f, paint);
        } else if (kind == UnitKind.BRUTE) {
            paint.setColor(dark);
            canvas.drawRect(-2f, 0f, 2f, 24f, paint);
            paint.setColor(glow);
            canvas.drawRect(-8f, 2f, 8f, 8f, paint);
        } else {
            paint.setColor(dark);
            canvas.drawRect(-1.5f, 0f, 1.5f, 18f, paint);
            paint.setColor(glow);
            canvas.drawCircle(0f, 18f, 3f, paint);
        }
        canvas.restore();
    }

    private void drawWildMonster(Canvas canvas, Paint paint, float legSwing, boolean attacking,
                                 float lineWidth, float scale) {
        canvas.scale(scale, scale);
        int fur = Color.rgb(120, 53, 15);
        int horn = Color.rgb(250, 204, 21);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(75, 0, 0, 0));
        canvas.drawOval(-20f, 14f, 20f, 28f, paint);
        drawCreatureLeg(canvas, paint, -10f, legSwing, darken(fur, 0.7f), fur, lineWidth);
        drawCreatureLeg(canvas, paint, 10f, -legSwing, darken(fur, 0.7f), fur, lineWidth);
        paint.setColor(fur);
        rect.set(-16f, -22f, 16f, 10f);
        canvas.drawRoundRect(rect, 8f, 8f, paint);
        paint.setColor(Color.rgb(251, 146, 60));
        canvas.drawCircle(0f, -28f, 11f, paint);
        paint.setColor(horn);
        canvas.drawCircle(-8f, -36f, 4f, paint);
        canvas.drawCircle(8f, -36f, 4f, paint);
        canvas.save();
        canvas.translate(14f, -14f);
        canvas.rotate(attacking ? -40f : -8f);
        paint.setColor(horn);
        canvas.drawRect(-2f, 0f, 2f, 20f, paint);
        canvas.restore();
    }

    private void drawCreatureLeg(Canvas canvas, Paint paint, float offsetX, float swingDeg,
                                 int shade, int color, float lineWidth) {
        canvas.save();
        canvas.translate(offsetX, 6f);
        canvas.rotate(swingDeg);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(shade);
        rect.set(-4f, 0f, 4f, 16f);
        canvas.drawRoundRect(rect, 4f, 4f, paint);
        paint.setColor(color);
        rect.set(-3f, 1f, 3f, 14f);
        canvas.drawRoundRect(rect, 3f, 3f, paint);
        canvas.restore();
    }

    private float lerpAngle(float from, float to, float t) {
        float diff = ((to - from + 540f) % 360f) - 180f;
        return from + diff * t;
    }

    private int darken(int color, float factor) {
        return Color.rgb(
                clamp(Color.red(color) * factor),
                clamp(Color.green(color) * factor),
                clamp(Color.blue(color) * factor));
    }

    private int clamp(float v) {
        return Math.max(0, Math.min(255, Math.round(v)));
    }
}
