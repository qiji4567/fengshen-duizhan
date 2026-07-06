package com.example.duizhan.view.render3d;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.example.duizhan.game.GameEntity;
import com.example.duizhan.game.HeroType;
import com.example.duizhan.game.HeroVisualProfile;
import com.example.duizhan.game.util.GameMath;
import com.example.duizhan.view.UprightFacing;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Software-rasterized 3D heroes with lighting, depth sorting and combat animation.
 */
public final class SoftwareHeroRenderer3D {
    private static final class PoseState {
        float lastX;
        float lastY;
        float displayFacingRad;
        float walkPhase;
    }

    private final HeroMeshFactory meshFactory = new HeroMeshFactory();
    private final MeshRasterizer rasterizer = new MeshRasterizer();
    private final Map<Long, PoseState> poses = new HashMap<>();

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

        pose.displayFacingRad = lerpAngleRad(pose.displayFacingRad, entity.facingRad, 0.28f);

        boolean attacking = entity.attackTimer > entity.attackCooldown * 0.8f;
        boolean casting = isCasting(entity);
        boolean moving = moveDist > 1.2f;
        if (moving) {
            pose.walkPhase += moveDist * 0.18f;
        }

        MeshPose meshPose = buildMeshPose(pose, attacking, casting, moving);
        List<MeshTriangle> mesh = meshFactory.build(profile, meshPose);

        float alpha = entity.stealthTimer > 0f ? 0.55f : 1f;
        float pixelScale = entity.radius * 2.15f * profile.scale;
        float facing = pose.displayFacingRad;
        if (entity.stunTimer > 0f) {
            facing += (float) Math.sin(pose.walkPhase * 2.2f) * 0.14f;
        }
        boolean faceLeft = UprightFacing.faceLeft(facing);

        rasterizer.draw(canvas, paint, mesh, entity.x, entity.y, 0f, pixelScale, alpha, 0f, faceLeft);
        drawTeamTrim(canvas, paint, entity, teamColor, pixelScale, facing, profile);
    }

    public void drawDead(Canvas canvas, Paint paint, GameEntity entity, int teamColor, float lineWidth) {
        HeroVisualProfile profile = HeroVisualProfile.of(entity.heroType);
        PoseState pose = poses.computeIfAbsent(entity.id, id -> new PoseState());
        float facing = pose.displayFacingRad != 0f ? pose.displayFacingRad : entity.facingRad;
        boolean faceLeft = UprightFacing.faceLeft(facing);

        MeshPose meshPose = new MeshPose();
        List<MeshTriangle> mesh = meshFactory.build(profile, meshPose);
        float pixelScale = entity.radius * 1.8f * profile.scale;
        rasterizer.draw(canvas, paint, mesh, entity.x, entity.y + entity.radius * 0.35f,
                0f, pixelScale, 0.42f, (float) Math.toRadians(82f), faceLeft);
    }

    private MeshPose buildMeshPose(PoseState pose, boolean attacking, boolean casting, boolean moving) {
        MeshPose meshPose = new MeshPose();
        float walkSwing = moving ? (float) Math.sin(pose.walkPhase) * 0.55f
                : (float) Math.sin(pose.walkPhase * 0.3f) * 0.05f;
        float armSwing = -walkSwing * 0.9f;
        meshPose.bodyLean = 0f;
        if (attacking) {
            armSwing = -1.25f;
            meshPose.bodyLean = 0.18f;
            walkSwing *= 0.25f;
        } else if (casting) {
            armSwing = -0.95f;
            meshPose.bodyLean = 0.1f;
            walkSwing = 0f;
        } else if (!moving) {
            armSwing = 0.32f;
            walkSwing = 0.14f;
        }
        meshPose.leftLeg = walkSwing;
        meshPose.rightLeg = -walkSwing;
        meshPose.leftArm = armSwing * 0.55f;
        meshPose.rightArm = armSwing;
        return meshPose;
    }

    private void drawTeamTrim(Canvas canvas, Paint paint, GameEntity entity, int teamColor,
                              float pixelScale, float facing, HeroVisualProfile profile) {
        float cos = (float) Math.cos(facing);
        float sin = (float) Math.sin(facing);
        float bx = entity.x + sin * pixelScale * 0.02f;
        float by = entity.y - pixelScale * 0.42f * profile.scale;
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(180, Color.red(teamColor), Color.green(teamColor), Color.blue(teamColor)));
        canvas.drawCircle(bx, by, pixelScale * 0.06f, paint);
    }

    private float lerpAngleRad(float from, float to, float t) {
        float diff = (float) Math.atan2(Math.sin(to - from), Math.cos(to - from));
        return from + diff * t;
    }

    private boolean isCasting(GameEntity entity) {
        return inCastWindow(entity.skillTimer, entity.skillCooldown)
                || inCastWindow(entity.secondarySkillTimer, entity.secondarySkillCooldown)
                || inCastWindow(entity.ultimateTimer, entity.ultimateCooldown);
    }

    private boolean inCastWindow(float timer, float cooldown) {
        if (timer <= 0f || cooldown <= 0f) {
            return false;
        }
        return timer > cooldown * 0.72f;
    }
}
