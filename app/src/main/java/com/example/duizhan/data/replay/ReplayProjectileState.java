package com.example.duizhan.data.replay;

import com.example.duizhan.game.DamageType;
import com.example.duizhan.game.Projectile;
import com.example.duizhan.game.ProjectileVisual;
import com.example.duizhan.game.Team;

import org.json.JSONException;
import org.json.JSONObject;

final class ReplayProjectileState {
    String team;
    String visual;
    float x;
    float y;
    float radius;
    int color;
    String label;
    float angleRad;

    static ReplayProjectileState from(Projectile projectile) {
        ReplayProjectileState state = new ReplayProjectileState();
        state.team = projectile.team.name();
        state.visual = projectile.visual.name();
        state.x = projectile.x;
        state.y = projectile.y;
        state.radius = projectile.radius;
        state.color = projectile.color;
        state.label = projectile.label;
        state.angleRad = projectile.angleRad;
        return state;
    }

    Projectile toProjectile() {
        Team projectileTeam = Team.NEUTRAL;
        try {
            projectileTeam = Team.valueOf(team);
        } catch (IllegalArgumentException ignored) {
        }
        ProjectileVisual projectileVisual = ProjectileVisual.BOLT;
        try {
            if (visual != null) {
                projectileVisual = ProjectileVisual.valueOf(visual);
            }
        } catch (IllegalArgumentException ignored) {
        }
        Projectile projectile = new Projectile(projectileTeam, 0L, 0L, x, y, 0f, 0f,
                DamageType.PHYSICAL, radius > 0f ? radius : 6f, 0f, color, projectileVisual);
        projectile.angleRad = angleRad;
        return projectile;
    }

    JSONObject toJson() throws JSONException {
        JSONObject object = new JSONObject();
        object.put("tm", team);
        object.put("v", visual);
        object.put("x", x);
        object.put("y", y);
        object.put("r", radius);
        object.put("c", color);
        object.put("a", angleRad);
        if (label != null && label.length() > 0) {
            object.put("l", label);
        }
        return object;
    }

    static ReplayProjectileState fromJson(JSONObject object) {
        if (object == null) {
            return null;
        }
        ReplayProjectileState state = new ReplayProjectileState();
        state.team = object.optString("tm", Team.NEUTRAL.name());
        state.visual = object.optString("v", ProjectileVisual.BOLT.name());
        state.x = (float) object.optDouble("x");
        state.y = (float) object.optDouble("y");
        state.radius = (float) object.optDouble("r", 6f);
        state.color = object.optInt("c", 0xFFFFFFFF);
        state.angleRad = (float) object.optDouble("a", 0d);
        state.label = object.optString("l", null);
        if (state.label != null && state.label.length() == 0) {
            state.label = null;
        }
        return state;
    }
}
