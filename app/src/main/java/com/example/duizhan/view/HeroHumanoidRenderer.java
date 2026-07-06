package com.example.duizhan.view;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

import com.example.duizhan.game.GameEntity;
import com.example.duizhan.game.HeroType;
import com.example.duizhan.game.HeroVisualProfile;
import com.example.duizhan.game.util.GameMath;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Myth-accurate articulated heroes with combat stance and smooth facing.
 */
public final class HeroHumanoidRenderer {
    private static final class PoseState {
        float lastX;
        float lastY;
        float displayFacingDeg;
        float walkPhase;
    }

    private final Map<Long, PoseState> poses = new HashMap<>();
    private final RectF bodyRect = new RectF();
    private final Path path = new Path();

    public void prune(Set<Long> activeIds) {
        Iterator<Map.Entry<Long, PoseState>> iterator = poses.entrySet().iterator();
        while (iterator.hasNext()) {
            if (!activeIds.contains(iterator.next().getKey())) {
                iterator.remove();
            }
        }
    }

    public void draw(Canvas canvas, Paint paint, GameEntity entity, int teamColor, float lineWidth) {
        HeroType type = entity.mimicTimer > 0f && entity.mimicHeroType != null
                ? entity.mimicHeroType : entity.heroType;
        HeroVisualProfile profile = HeroVisualProfile.of(type);
        PoseState pose = poses.computeIfAbsent(entity.id, id -> new PoseState());
        float moveDist = GameMath.distance(entity.x, entity.y, pose.lastX, pose.lastY);
        pose.lastX = entity.x;
        pose.lastY = entity.y;

        float targetFacingDeg = (float) Math.toDegrees(entity.facingRad);
        pose.displayFacingDeg = lerpAngle(pose.displayFacingDeg, targetFacingDeg, 0.28f);

        boolean attacking = entity.attackTimer > entity.attackCooldown * 0.8f;
        boolean casting = entity.skillTimer > entity.skillCooldown * 0.75f
                || entity.ultimateTimer > entity.ultimateCooldown * 0.75f;
        boolean moving = moveDist > 1.2f;
        if (moving) {
            pose.walkPhase += moveDist * 0.18f;
        }
        float walkSwing = moving ? (float) Math.sin(pose.walkPhase) * 32f : (float) Math.sin(pose.walkPhase * 0.3f) * 3f;
        float armSwing = -walkSwing * 0.9f;
        float bodyLean = 0f;
        if (attacking) {
            armSwing = -72f;
            bodyLean = 10f;
            walkSwing *= 0.25f;
        } else if (casting) {
            armSwing = -55f;
            bodyLean = 6f;
            walkSwing = 0f;
        } else if (!moving) {
            armSwing = 14f + (float) Math.sin(pose.walkPhase * 0.4f) * 4f;
            walkSwing = (float) Math.sin(pose.walkPhase * 0.4f) * 2f;
        }

        float alpha = entity.stealthTimer > 0f ? 0.55f : 1f;
        float scale = profile.scale * Math.max(1.02f, entity.radius / 29f);

        canvas.save();
        canvas.translate(entity.x, entity.y);
        if (entity.stunTimer > 0f) {
            canvas.rotate((float) Math.sin(pose.walkPhase * 2.2f) * 8f);
        }
        UprightFacing.apply(canvas, entity.facingRad);
        canvas.scale(scale, scale);

        drawShadow(canvas, paint);
        canvas.save();
        canvas.rotate(bodyLean);

        drawFloatingRobe(canvas, paint, profile, alpha, moving, pose.walkPhase);
        drawLeg(canvas, paint, -9f, walkSwing, profile, lineWidth);
        drawBackArm(canvas, paint, armSwing * 0.55f, profile, lineWidth, attacking);
        drawTorso(canvas, paint, profile, lineWidth, teamColor, alpha);
        drawHead(canvas, paint, profile, lineWidth, alpha);
        drawSignature(canvas, paint, profile, lineWidth);
        drawFrontArm(canvas, paint, armSwing, profile, lineWidth, attacking, casting);
        drawSigWeapon(canvas, paint, profile, armSwing, attacking, casting, lineWidth);
        drawLeg(canvas, paint, 9f, -walkSwing, profile, lineWidth);
        drawFoot(canvas, paint, -9f, walkSwing, profile, alpha);
        drawFoot(canvas, paint, 9f, -walkSwing, profile, alpha);

        canvas.restore();
        canvas.restore();
    }

    public void drawDead(Canvas canvas, Paint paint, GameEntity entity, int teamColor, float lineWidth) {
        HeroVisualProfile profile = HeroVisualProfile.of(entity.heroType);
        poses.computeIfAbsent(entity.id, id -> new PoseState());

        canvas.save();
        canvas.translate(entity.x, entity.y + entity.radius * 0.4f);
        UprightFacing.apply(canvas, entity.facingRad);
        canvas.rotate(88f);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(60, 0, 0, 0));
        canvas.drawOval(-36f, 8f, 36f, 22f, paint);
        bodyRect.set(-28f, -8f, 28f, 14f);
        paint.setColor(applyAlpha(profile.robeColor, 0.4f));
        canvas.drawRoundRect(bodyRect, 8f, 8f, paint);
        paint.setColor(applyAlpha(profile.skinColor, 0.4f));
        canvas.drawCircle(-18f, -2f, 10f, paint);
        canvas.restore();
    }

    private void drawShadow(Canvas canvas, Paint paint) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(75, 0, 0, 0));
        canvas.drawOval(-26f, 18f, 26f, 32f, paint);
    }

    private void drawTorso(Canvas canvas, Paint paint, HeroVisualProfile profile, float lineWidth,
                           int teamColor, float alpha) {
        int robe = applyAlpha(profile.robeColor, alpha);
        int shade = applyAlpha(darken(profile.robeColor, 0.7f), alpha);
        int trim = applyAlpha(profile.trimColor, alpha);
        float bodyW = profile.body == HeroVisualProfile.BodyKind.GIANT ? 22f
                : profile.body == HeroVisualProfile.BodyKind.PIG_DEMON ? 20f : 16f;
        float bodyTop = profile.body == HeroVisualProfile.BodyKind.GIANT ? -44f : -40f;
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(shade);
        bodyRect.set(-bodyW - 2f, bodyTop + 2f, bodyW + 2f, 12f);
        canvas.drawRoundRect(bodyRect, 11f, 11f, paint);
        paint.setColor(robe);
        bodyRect.set(-bodyW, bodyTop, bodyW, 10f);
        canvas.drawRoundRect(bodyRect, 10f, 10f, paint);
        paint.setColor(trim);
        canvas.drawRect(-16f, -18f, 16f, -12f, paint);
        if (profile.body == HeroVisualProfile.BodyKind.MONKEY_KING) {
            paint.setColor(applyAlpha(Color.rgb(250, 204, 21), alpha));
            canvas.drawRect(-17f, -42f, 17f, -36f, paint);
        }
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(lineWidth);
        paint.setColor(Color.argb(90, 255, 255, 255));
        canvas.drawRoundRect(bodyRect, 10f, 10f, paint);
    }

    private void drawHead(Canvas canvas, Paint paint, HeroVisualProfile profile, float lineWidth, float alpha) {
        float headY = headOffsetY(profile);
        paint.setStyle(Paint.Style.FILL);
        switch (profile.body) {
            case MONKEY_KING:
                paint.setColor(applyAlpha(Color.rgb(180, 110, 50), alpha));
                canvas.drawCircle(0f, headY, 14f, paint);
                paint.setColor(applyAlpha(Color.rgb(250, 204, 21), alpha));
                canvas.drawRect(-15f, headY - 16f, 15f, headY - 10f, paint);
                paint.setColor(applyAlpha(profile.skinColor, alpha));
                canvas.drawCircle(0f, headY + 2f, 11f, paint);
                break;
            case PIG_DEMON:
                paint.setColor(applyAlpha(Color.rgb(252, 165, 165), alpha));
                canvas.drawCircle(0f, headY, 15f, paint);
                paint.setColor(applyAlpha(Color.rgb(190, 80, 80), alpha));
                canvas.drawOval(-10f, headY - 2f, -2f, headY + 8f, paint);
                canvas.drawOval(2f, headY - 2f, 10f, headY + 8f, paint);
                canvas.drawCircle(-8f, headY - 4f, 3f, paint);
                canvas.drawCircle(8f, headY - 4f, 3f, paint);
                break;
            case BULL_DEMON:
                paint.setColor(applyAlpha(profile.skinColor, alpha));
                canvas.drawCircle(0f, headY, 13f, paint);
                paint.setColor(applyAlpha(Color.rgb(60, 30, 20), alpha));
                path.reset();
                path.moveTo(-14f, headY - 8f);
                path.lineTo(-18f, headY - 20f);
                path.lineTo(-10f, headY - 10f);
                path.close();
                canvas.drawPath(path, paint);
                path.reset();
                path.moveTo(14f, headY - 8f);
                path.lineTo(18f, headY - 20f);
                path.lineTo(10f, headY - 10f);
                path.close();
                canvas.drawPath(path, paint);
                break;
            case SKELETON:
                paint.setColor(applyAlpha(Color.rgb(248, 250, 252), alpha));
                canvas.drawCircle(0f, headY, 12f, paint);
                paint.setColor(applyAlpha(Color.rgb(30, 41, 59), alpha));
                canvas.drawCircle(-4f, headY - 1f, 2.5f, paint);
                canvas.drawCircle(4f, headY - 1f, 2.5f, paint);
                break;
            case GHOST_JUDGE:
                paint.setColor(applyAlpha(Color.rgb(74, 222, 128), alpha));
                canvas.drawCircle(0f, headY, 13f, paint);
                paint.setColor(applyAlpha(Color.rgb(20, 50, 30), alpha));
                canvas.drawCircle(-4f, headY, 2f, paint);
                canvas.drawCircle(4f, headY, 2f, paint);
                if (profile.crown) {
                    paint.setColor(applyAlpha(Color.rgb(30, 41, 59), alpha));
                    canvas.drawRect(-10f, headY - 18f, 10f, headY - 12f, paint);
                }
                break;
            case DRAGON_KING:
                paint.setColor(applyAlpha(profile.skinColor, alpha));
                canvas.drawCircle(0f, headY, 12f, paint);
                paint.setColor(applyAlpha(Color.rgb(37, 99, 235), alpha));
                path.reset();
                path.moveTo(-12f, headY - 4f);
                path.lineTo(-18f, headY - 14f);
                path.lineTo(-6f, headY - 8f);
                path.close();
                canvas.drawPath(path, paint);
                path.reset();
                path.moveTo(12f, headY - 4f);
                path.lineTo(18f, headY - 14f);
                path.lineTo(6f, headY - 8f);
                path.close();
                canvas.drawPath(path, paint);
                break;
            case BUDDHA:
                paint.setColor(applyAlpha(profile.skinColor, alpha));
                canvas.drawCircle(0f, headY + 2f, 14f, paint);
                paint.setColor(applyAlpha(Color.rgb(250, 204, 21), alpha));
                canvas.drawOval(-4f, headY - 6f, 4f, headY + 2f, paint);
                break;
            case EMPEROR:
                paint.setColor(applyAlpha(profile.hairColor, alpha));
                canvas.drawCircle(0f, headY, 14f, paint);
                paint.setColor(applyAlpha(profile.skinColor, alpha));
                canvas.drawCircle(0f, headY + 2f, 11f, paint);
                paint.setColor(applyAlpha(Color.rgb(250, 204, 21), alpha));
                canvas.drawRect(-12f, headY - 18f, 12f, headY - 12f, paint);
                break;
            case OX_DEMON:
                paint.setColor(applyAlpha(profile.skinColor, alpha));
                canvas.drawCircle(0f, headY, 14f, paint);
                paint.setColor(applyAlpha(Color.rgb(60, 30, 20), alpha));
                drawHorn(canvas, paint, -12f, headY - 8f, -16f, headY - 22f, -5f, headY - 12f);
                drawHorn(canvas, paint, 12f, headY - 8f, 16f, headY - 22f, 5f, headY - 12f);
                paint.setColor(applyAlpha(Color.rgb(30, 41, 59), alpha));
                canvas.drawCircle(-5f, headY + 1f, 2f, paint);
                canvas.drawCircle(5f, headY + 1f, 2f, paint);
                break;
            case HORSE_DEMON:
                paint.setColor(applyAlpha(profile.skinColor, alpha));
                canvas.drawOval(-14f, headY - 6f, 14f, headY + 10f, paint);
                paint.setColor(applyAlpha(profile.hairColor, alpha));
                canvas.drawRect(-4f, headY - 20f, 4f, headY - 10f, paint);
                paint.setColor(applyAlpha(Color.rgb(30, 41, 59), alpha));
                canvas.drawCircle(-6f, headY, 2.5f, paint);
                canvas.drawCircle(6f, headY, 2.5f, paint);
                break;
            case FOX_SPIRIT:
                paint.setColor(applyAlpha(profile.skinColor, alpha));
                canvas.drawCircle(0f, headY + 2f, 12f, paint);
                paint.setColor(applyAlpha(profile.hairColor, alpha));
                drawHorn(canvas, paint, -12f, headY - 4f, -18f, headY - 18f, -4f, headY - 8f);
                drawHorn(canvas, paint, 12f, headY - 4f, 18f, headY - 18f, 4f, headY - 8f);
                paint.setColor(applyAlpha(Color.rgb(30, 41, 59), alpha));
                canvas.drawCircle(-4f, headY + 2f, 2f, paint);
                canvas.drawCircle(4f, headY + 2f, 2f, paint);
                break;
            case BIRD_DEMON:
                paint.setColor(applyAlpha(profile.skinColor, alpha));
                canvas.drawCircle(0f, headY, 12f, paint);
                paint.setColor(applyAlpha(profile.trimColor, alpha));
                canvas.drawOval(-3f, headY + 5f, 3f, headY + 10f, paint);
                paint.setColor(applyAlpha(Color.rgb(250, 204, 21), alpha));
                canvas.drawOval(8f, headY - 2f, 18f, headY + 4f, paint);
                break;
            case SNAKE_DEMON:
                paint.setColor(applyAlpha(profile.skinColor, alpha));
                canvas.drawOval(-11f, headY - 8f, 11f, headY + 8f, paint);
                paint.setColor(applyAlpha(Color.rgb(34, 197, 94), alpha));
                canvas.drawCircle(-5f, headY - 2f, 2.5f, paint);
                canvas.drawCircle(5f, headY - 2f, 2.5f, paint);
                paint.setColor(applyAlpha(Color.rgb(220, 38, 38), alpha));
                canvas.drawOval(-2f, headY + 4f, 2f, headY + 8f, paint);
                break;
            case GIANT:
                paint.setColor(applyAlpha(profile.skinColor, alpha));
                canvas.drawCircle(0f, headY, 17f, paint);
                paint.setColor(applyAlpha(profile.hairColor, alpha));
                canvas.drawRect(-16f, headY - 20f, 16f, headY - 8f, paint);
                paint.setColor(applyAlpha(Color.rgb(30, 41, 59), alpha));
                canvas.drawRect(-7f, headY + 4f, -3f, headY + 7f, paint);
                canvas.drawRect(3f, headY + 4f, 7f, headY + 7f, paint);
                break;
            case FEMALE_IMMORTAL:
                paint.setColor(applyAlpha(profile.skinColor, alpha));
                canvas.drawCircle(0f, headY + 1f, 12f, paint);
                paint.setColor(applyAlpha(profile.hairColor, alpha));
                canvas.drawOval(-16f, headY - 6f, -8f, headY + 22f, paint);
                canvas.drawOval(8f, headY - 6f, 16f, headY + 22f, paint);
                paint.setColor(applyAlpha(profile.trimColor, alpha));
                canvas.drawCircle(0f, headY - 10f, 3.5f, paint);
                break;
            case CHILD_WARRIOR:
                paint.setColor(applyAlpha(profile.skinColor, alpha));
                canvas.drawCircle(0f, headY + 2f, 10f, paint);
                paint.setColor(applyAlpha(profile.hairColor, alpha));
                canvas.drawCircle(0f, headY - 2f, 9f, paint);
                paint.setColor(applyAlpha(Color.rgb(30, 41, 59), alpha));
                canvas.drawCircle(-3.5f, headY + 2f, 1.4f, paint);
                canvas.drawCircle(3.5f, headY + 2f, 1.4f, paint);
                break;
            case HUMAN_MAGE:
                paint.setColor(applyAlpha(profile.hairColor, alpha));
                canvas.drawCircle(0f, headY, 14f, paint);
                paint.setColor(applyAlpha(profile.skinColor, alpha));
                canvas.drawCircle(0f, headY + 2f, 11f, paint);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(lineWidth);
                paint.setColor(applyAlpha(profile.trimColor, alpha));
                canvas.drawCircle(0f, headY, 15f, paint);
                paint.setStyle(Paint.Style.FILL);
                break;
            case HUMAN_MONK:
                paint.setColor(applyAlpha(profile.skinColor, alpha));
                canvas.drawCircle(0f, headY + 1f, 13f, paint);
                paint.setColor(applyAlpha(Color.rgb(250, 204, 21), alpha));
                canvas.drawOval(-4f, headY - 18f, 4f, headY - 10f, paint);
                break;
            case HUMAN_WARRIOR:
                paint.setColor(applyAlpha(profile.hairColor, alpha));
                canvas.drawArc(-15f, headY - 16f, 15f, headY + 2f, 180f, 180f, true, paint);
                paint.setColor(applyAlpha(profile.skinColor, alpha));
                canvas.drawCircle(0f, headY + 2f, 11f, paint);
                paint.setColor(applyAlpha(Color.rgb(30, 41, 59), alpha));
                canvas.drawCircle(-4f, headY + 1f, 1.8f, paint);
                canvas.drawCircle(4f, headY + 1f, 1.8f, paint);
                break;
            default:
                paint.setColor(applyAlpha(profile.hairColor, alpha));
                canvas.drawCircle(0f, headY, 14f, paint);
                paint.setColor(applyAlpha(profile.skinColor, alpha));
                canvas.drawCircle(0f, headY + 2f, 11f, paint);
                if (profile.thirdEye) {
                    paint.setColor(applyAlpha(Color.rgb(250, 204, 21), alpha));
                    canvas.drawCircle(0f, headY - 2f, 2.5f, paint);
                }
                if (profile.crown) {
                    paint.setColor(applyAlpha(profile.trimColor, alpha));
                    canvas.drawRect(-10f, headY - 17f, 10f, headY - 12f, paint);
                }
                break;
        }
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(lineWidth * 0.7f);
        paint.setColor(Color.argb(70, 20, 20, 20));
        canvas.drawCircle(0f, headY + 1f, 11f, paint);
    }

    private void drawSignature(Canvas canvas, Paint paint, HeroVisualProfile profile, float lineWidth) {
        if (profile.wings) {
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(applyAlpha(Color.rgb(147, 197, 253), 0.85f));
            path.reset();
            path.moveTo(-8f, -30f);
            path.lineTo(-34f, -18f);
            path.lineTo(-12f, -20f);
            path.close();
            canvas.drawPath(path, paint);
            path.reset();
            path.moveTo(8f, -30f);
            path.lineTo(34f, -18f);
            path.lineTo(12f, -20f);
            path.close();
            canvas.drawPath(path, paint);
        }
        if (profile.body == HeroVisualProfile.BodyKind.CHILD_WARRIOR) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(lineWidth);
            paint.setColor(applyAlpha(Color.rgb(250, 204, 21), 0.9f));
            canvas.drawCircle(-14f, 16f, 5f, paint);
            canvas.drawCircle(14f, 16f, 5f, paint);
        }
    }

    private void drawFloatingRobe(Canvas canvas, Paint paint, HeroVisualProfile profile,
                                  float alpha, boolean moving, float walkPhase) {
        if (!isImmortalBody(profile.body)) {
            return;
        }
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(applyAlpha(darken(profile.robeColor, 0.55f), alpha * 0.85f));
        float sway = moving ? (float) Math.sin(walkPhase * 0.5f) * 4f : 0f;
        path.reset();
        path.moveTo(-20f + sway, 6f);
        path.quadTo(-28f, 28f, -14f, 42f);
        path.lineTo(14f, 42f);
        path.quadTo(28f, 28f, 20f - sway, 6f);
        path.close();
        canvas.drawPath(path, paint);
        paint.setColor(applyAlpha(profile.trimColor, alpha * 0.5f));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1.5f);
        canvas.drawPath(path, paint);
    }

    private boolean isImmortalBody(HeroVisualProfile.BodyKind body) {
        switch (body) {
            case FEMALE_IMMORTAL:
            case HUMAN_MAGE:
            case HUMAN_MONK:
            case BUDDHA:
            case EMPEROR:
            case GHOST_JUDGE:
            case DRAGON_KING:
                return true;
            default:
                return false;
        }
    }

    private void drawHorn(Canvas canvas, Paint paint, float x1, float y1, float x2, float y2, float x3, float y3) {
        path.reset();
        path.moveTo(x1, y1);
        path.lineTo(x2, y2);
        path.lineTo(x3, y3);
        path.close();
        canvas.drawPath(path, paint);
    }

    private void drawLeg(Canvas canvas, Paint paint, float offsetX, float swingDeg,
                         HeroVisualProfile profile, float lineWidth) {
        canvas.save();
        canvas.translate(offsetX, 8f);
        canvas.rotate(swingDeg);
        int color = applyAlpha(darken(profile.robeColor, 0.72f), 1f);
        int highlight = applyAlpha(profile.robeColor, 1f);
        float legLen = legLength(profile);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(color);
        bodyRect.set(-5.5f, 0f, 5.5f, legLen);
        canvas.drawRoundRect(bodyRect, 5f, 5f, paint);
        paint.setColor(highlight);
        bodyRect.set(-4f, 2f, 4f, legLen - 2f);
        canvas.drawRoundRect(bodyRect, 4f, 4f, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(lineWidth * 0.55f);
        paint.setColor(Color.argb(60, 255, 255, 255));
        canvas.drawRoundRect(bodyRect, 4f, 4f, paint);
        canvas.restore();
    }

    private float legLength(HeroVisualProfile profile) {
        if (profile.body == HeroVisualProfile.BodyKind.GIANT) {
            return 36f;
        }
        if (profile.body == HeroVisualProfile.BodyKind.CHILD_WARRIOR) {
            return 24f;
        }
        if (profile.body == HeroVisualProfile.BodyKind.PIG_DEMON) {
            return 26f;
        }
        return 32f;
    }

    private void drawFoot(Canvas canvas, Paint paint, float offsetX, float swingDeg,
                          HeroVisualProfile profile, float alpha) {
        canvas.save();
        canvas.translate(offsetX, 8f);
        canvas.rotate(swingDeg);
        float legLen = legLength(profile);
        canvas.translate(0f, legLen - 2f);
        paint.setStyle(Paint.Style.FILL);
        if (isImmortalBody(profile.body)) {
            paint.setColor(applyAlpha(Color.rgb(147, 197, 253), alpha * 0.9f));
            canvas.drawOval(-8f, -2f, 8f, 6f, paint);
            paint.setColor(applyAlpha(Color.rgb(191, 219, 254), alpha * 0.6f));
            canvas.drawOval(-5f, -4f, 5f, 2f, paint);
        } else if (profile.body == HeroVisualProfile.BodyKind.MONKEY_KING
                || profile.body == HeroVisualProfile.BodyKind.PIG_DEMON) {
            paint.setColor(applyAlpha(darken(profile.skinColor, 0.7f), alpha));
            canvas.drawOval(-7f, -1f, 7f, 5f, paint);
        } else {
            paint.setColor(applyAlpha(darken(profile.trimColor, 0.55f), alpha));
            canvas.drawRoundRect(-7f, -1f, 7f, 5f, 3f, 3f, paint);
        }
        canvas.restore();
    }

    private void drawBackArm(Canvas canvas, Paint paint, float swingDeg, HeroVisualProfile profile,
                             float lineWidth, boolean attacking) {
        canvas.save();
        canvas.translate(-17f, -26f);
        canvas.rotate(swingDeg - (attacking ? 10f : 0f));
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(applyAlpha(darken(profile.robeColor, 0.72f), 1f));
        bodyRect.set(-4f, 0f, 4f, 20f);
        canvas.drawRoundRect(bodyRect, 4f, 4f, paint);
        canvas.restore();
    }

    private void drawFrontArm(Canvas canvas, Paint paint, float swingDeg, HeroVisualProfile profile,
                              float lineWidth, boolean attacking, boolean casting) {
        canvas.save();
        canvas.translate(17f, -26f);
        canvas.rotate(swingDeg + (attacking ? -28f : 0f) + (casting ? -18f : 0f));
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(applyAlpha(profile.robeColor, 1f));
        bodyRect.set(-4.5f, 0f, 4.5f, 24f);
        canvas.drawRoundRect(bodyRect, 4f, 4f, paint);
        paint.setColor(applyAlpha(profile.skinColor, 1f));
        canvas.drawCircle(0f, 23f, 4.5f, paint);
        canvas.restore();
    }

    private void drawSigWeapon(Canvas canvas, Paint paint, HeroVisualProfile profile,
                               float armSwing, boolean attacking, boolean casting, float lineWidth) {
        canvas.save();
        canvas.translate(18f, -26f);
        canvas.rotate(armSwing + (attacking ? -58f : -14f) + (casting ? -36f : 0f));
        paint.setStyle(Paint.Style.FILL);
        int metal = profile.trimColor;
        int dark = darken(profile.trimColor, 0.65f);
        switch (profile.weapon) {
            case GOLDEN_STAFF:
                paint.setColor(Color.rgb(250, 204, 21));
                canvas.drawRect(-3f, 8f, 3f, 48f, paint);
                paint.setColor(Color.rgb(180, 120, 20));
                canvas.drawCircle(0f, 8f, 5f, paint);
                break;
            case RAKE:
                paint.setColor(dark);
                canvas.drawRect(-2f, 10f, 2f, 42f, paint);
                paint.setColor(metal);
                for (int i = -2; i <= 2; i++) {
                    canvas.drawRect(i * 4f - 1f, 38f, i * 4f + 1f, 46f, paint);
                }
                break;
            case BOW:
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(lineWidth * 1.2f);
                paint.setColor(dark);
                canvas.drawArc(new RectF(-6f, 14f, 6f, 42f), -90f, 180f, false, paint);
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(metal);
                canvas.drawLine(0f, 14f, 0f, 40f, paint);
                break;
            case PAGODA:
                paint.setColor(Color.rgb(250, 204, 21));
                bodyRect.set(-8f, 12f, 8f, 30f);
                canvas.drawRoundRect(bodyRect, 3f, 3f, paint);
                paint.setColor(Color.rgb(30, 64, 175));
                canvas.drawRect(-6f, 16f, 6f, 26f, paint);
                break;
            case LOTUS:
                paint.setColor(Color.rgb(244, 114, 182));
                for (int i = 0; i < 6; i++) {
                    canvas.save();
                    canvas.rotate(i * 60f);
                    canvas.drawOval(-3f, 14f, 3f, 22f, paint);
                    canvas.restore();
                }
                break;
            case BOOK:
                paint.setColor(Color.rgb(250, 204, 21));
                bodyRect.set(-9f, 14f, 9f, 28f);
                canvas.drawRoundRect(bodyRect, 2f, 2f, paint);
                break;
            case GOURD:
                paint.setColor(Color.rgb(22, 101, 52));
                canvas.drawCircle(0f, 22f, 8f, paint);
                canvas.drawCircle(0f, 14f, 5f, paint);
                break;
            case TRIDENT:
                paint.setColor(dark);
                canvas.drawRect(-1.5f, 12f, 1.5f, 42f, paint);
                paint.setColor(metal);
                canvas.drawRect(-8f, 10f, 8f, 14f, paint);
                canvas.drawRect(-1.5f, 6f, 1.5f, 12f, paint);
                break;
            case FIRE_SPEAR:
            case SPEAR:
                paint.setColor(attacking ? Color.rgb(251, 146, 60) : dark);
                canvas.drawRect(-2f, 8f, 2f, 44f, paint);
                paint.setColor(metal);
                canvas.drawRect(-5f, 6f, 5f, 10f, paint);
                break;
            case CHAIN:
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(lineWidth);
                paint.setColor(Color.rgb(148, 163, 184));
                canvas.drawCircle(0f, 20f, 6f, paint);
                canvas.drawCircle(0f, 32f, 5f, paint);
                break;
            case FAN:
                paint.setColor(Color.rgb(239, 68, 68));
                canvas.drawArc(new RectF(-12f, 10f, 12f, 30f), 200f, 140f, true, paint);
                break;
            case BEADS:
                for (int i = 0; i < 5; i++) {
                    paint.setColor(i % 2 == 0 ? Color.rgb(250, 204, 21) : Color.rgb(180, 120, 20));
                    canvas.drawCircle(-8f + i * 4f, 18f + i * 2f, 3f, paint);
                }
                break;
            case AXE:
                paint.setColor(dark);
                canvas.drawRect(-2f, 10f, 2f, 40f, paint);
                paint.setColor(metal);
                path.reset();
                path.moveTo(2f, 10f);
                path.lineTo(16f, 16f);
                path.lineTo(2f, 24f);
                path.close();
                canvas.drawPath(path, paint);
                break;
            case SWORD:
            default:
                paint.setColor(metal);
                canvas.drawRect(-2f, 10f, 2f, 40f, paint);
                paint.setColor(Color.rgb(148, 163, 184));
                canvas.drawRect(-6f, 38f, 6f, 42f, paint);
                break;
        }
        canvas.restore();
    }

    private float headOffsetY(HeroVisualProfile profile) {
        if (profile.body == HeroVisualProfile.BodyKind.GIANT) {
            return -50f;
        }
        if (profile.body == HeroVisualProfile.BodyKind.CHILD_WARRIOR) {
            return -36f;
        }
        return -44f;
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

    private int applyAlpha(int color, float alpha) {
        return Color.argb(clamp(255 * alpha), Color.red(color), Color.green(color), Color.blue(color));
    }

    private int clamp(float v) {
        return Math.max(0, Math.min(255, Math.round(v)));
    }
}
