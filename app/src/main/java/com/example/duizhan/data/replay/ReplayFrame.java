package com.example.duizhan.data.replay;

import com.example.duizhan.game.GameEntity;
import com.example.duizhan.game.GameSnapshot;
import com.example.duizhan.game.Projectile;
import com.example.duizhan.game.Team;
import com.example.duizhan.game.UnitKind;
import com.example.duizhan.game.VisualEffect;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public final class ReplayFrame {
    public long timeMs;
    public float blueX;
    public float blueY;
    public float blueHp;
    public float blueMaxHp;
    public float redX;
    public float redY;
    public float redHp;
    public float redMaxHp;
    public float blueTowerHp;
    public float redTowerHp;
    public int blueKills;
    public int redKills;
    public final List<ReplayEntityState> entities = new ArrayList<>();
    public final List<ReplayProjectileState> projectiles = new ArrayList<>();
    public final List<ReplayEffectState> effects = new ArrayList<>();
    public boolean entityWorldRecorded;

    void captureFrom(GameSnapshot snapshot) {
        entities.clear();
        projectiles.clear();
        effects.clear();
        entityWorldRecorded = false;
        if (snapshot == null) {
            return;
        }
        entityWorldRecorded = true;
        for (GameEntity entity : snapshot.entities) {
            entities.add(ReplayEntityState.from(entity));
            if (entity.team == Team.BLUE && entity.kind == UnitKind.HERO) {
                blueX = entity.x;
                blueY = entity.y;
                blueHp = entity.hp;
                blueMaxHp = entity.maxHp;
            } else if (entity.team == Team.RED && entity.kind == UnitKind.HERO) {
                redX = entity.x;
                redY = entity.y;
                redHp = entity.hp;
                redMaxHp = entity.maxHp;
            }
        }
        for (Projectile projectile : snapshot.projectiles) {
            projectiles.add(ReplayProjectileState.from(projectile));
        }
        for (VisualEffect effect : snapshot.effects) {
            effects.add(ReplayEffectState.from(effect));
        }
    }

    boolean hasFullWorld() {
        return entityWorldRecorded;
    }

    JSONObject toJson() throws JSONException {
        JSONObject object = new JSONObject();
        object.put("t", timeMs);
        object.put("bx", blueX);
        object.put("by", blueY);
        object.put("bhp", blueHp);
        object.put("bmh", blueMaxHp);
        object.put("rx", redX);
        object.put("ry", redY);
        object.put("rhp", redHp);
        object.put("rmh", redMaxHp);
        object.put("bth", blueTowerHp);
        object.put("rth", redTowerHp);
        object.put("bk", blueKills);
        object.put("rk", redKills);
        object.put("ew", entityWorldRecorded ? 1 : 0);
        JSONArray entityArray = new JSONArray();
        for (ReplayEntityState entity : entities) {
            entityArray.put(entity.toJson());
        }
        object.put("entities", entityArray);
        if (!projectiles.isEmpty()) {
            JSONArray projectileArray = new JSONArray();
            for (ReplayProjectileState projectile : projectiles) {
                projectileArray.put(projectile.toJson());
            }
            object.put("projectiles", projectileArray);
        }
        if (!effects.isEmpty()) {
            JSONArray effectArray = new JSONArray();
            for (ReplayEffectState effect : effects) {
                effectArray.put(effect.toJson());
            }
            object.put("effects", effectArray);
        }
        return object;
    }

    static ReplayFrame fromJson(JSONObject object) {
        if (object == null) {
            return null;
        }
        ReplayFrame frame = new ReplayFrame();
        frame.timeMs = object.optLong("t");
        frame.blueX = (float) object.optDouble("bx");
        frame.blueY = (float) object.optDouble("by");
        frame.blueHp = (float) object.optDouble("bhp");
        frame.blueMaxHp = (float) object.optDouble("bmh", 1000f);
        frame.redX = (float) object.optDouble("rx");
        frame.redY = (float) object.optDouble("ry");
        frame.redHp = (float) object.optDouble("rhp");
        frame.redMaxHp = (float) object.optDouble("rmh", 1000f);
        frame.blueTowerHp = (float) object.optDouble("bth");
        frame.redTowerHp = (float) object.optDouble("rth");
        frame.blueKills = object.optInt("bk");
        frame.redKills = object.optInt("rk");
        frame.entityWorldRecorded = object.optInt("ew", object.has("entities") ? 1 : 0) == 1;
        JSONArray entityArray = object.optJSONArray("entities");
        if (entityArray != null) {
            for (int i = 0; i < entityArray.length(); i++) {
                ReplayEntityState entity = ReplayEntityState.fromJson(entityArray.optJSONObject(i));
                if (entity != null) {
                    frame.entities.add(entity);
                }
            }
        }
        JSONArray projectileArray = object.optJSONArray("projectiles");
        if (projectileArray != null) {
            for (int i = 0; i < projectileArray.length(); i++) {
                ReplayProjectileState projectile = ReplayProjectileState.fromJson(projectileArray.optJSONObject(i));
                if (projectile != null) {
                    frame.projectiles.add(projectile);
                }
            }
        }
        JSONArray effectArray = object.optJSONArray("effects");
        if (effectArray != null) {
            for (int i = 0; i < effectArray.length(); i++) {
                ReplayEffectState effect = ReplayEffectState.fromJson(effectArray.optJSONObject(i));
                if (effect != null) {
                    frame.effects.add(effect);
                }
            }
        }
        frame.repairHeroFieldsFromEntities();
        return frame;
    }

    private void repairHeroFieldsFromEntities() {
        for (ReplayEntityState entity : entities) {
            UnitKind kind;
            Team entityTeam;
            try {
                kind = UnitKind.valueOf(entity.kind);
                entityTeam = Team.valueOf(entity.team);
            } catch (IllegalArgumentException ignored) {
                continue;
            }
            if (kind != UnitKind.HERO) {
                continue;
            }
            if (entityTeam == Team.BLUE) {
                blueX = entity.x;
                blueY = entity.y;
                blueHp = entity.hp;
                blueMaxHp = entity.maxHp;
            } else if (entityTeam == Team.RED) {
                redX = entity.x;
                redY = entity.y;
                redHp = entity.hp;
                redMaxHp = entity.maxHp;
            }
        }
    }
}
