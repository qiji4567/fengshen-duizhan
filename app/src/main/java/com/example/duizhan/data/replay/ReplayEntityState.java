package com.example.duizhan.data.replay;

import com.example.duizhan.game.GameEntity;
import com.example.duizhan.game.HeroType;
import com.example.duizhan.game.ItemType;
import com.example.duizhan.game.Team;
import com.example.duizhan.game.UnitKind;

import org.json.JSONException;
import org.json.JSONObject;

final class ReplayEntityState {
    long id;
    String kind;
    String team;
    String name;
    String heroType;
    String weapon;
    String armor;
    String boots;
    String hat;
    String relic;
    float x;
    float y;
    float hp;
    float maxHp;
    float radius;
    float facingRad;
    boolean alive;
    float attackTimer;
    float attackCooldown;
    float skillTimer;
    float skillCooldown;
    float secondarySkillTimer;
    float secondarySkillCooldown;
    float ultimateTimer;
    float ultimateCooldown;
    float stealthTimer;
    float shieldTimer;
    float damageBoostTimer;
    float redBuffTimer;
    float blueBuffTimer;
    int level;
    int exp;
    int nextExp;
    int totalExp;
    int expValue;
    int goldValue;
    int kills;
    int lastHitGold;

    static ReplayEntityState from(GameEntity entity) {
        ReplayEntityState state = new ReplayEntityState();
        state.id = entity.id;
        state.kind = entity.kind.name();
        state.team = entity.team.name();
        state.name = entity.name;
        state.x = entity.x;
        state.y = entity.y;
        state.hp = entity.hp;
        state.maxHp = entity.maxHp;
        state.radius = entity.radius;
        state.facingRad = entity.facingRad;
        state.alive = entity.alive;
        state.attackTimer = entity.attackTimer;
        state.attackCooldown = entity.attackCooldown;
        state.skillTimer = entity.skillTimer;
        state.skillCooldown = entity.skillCooldown;
        state.secondarySkillTimer = entity.secondarySkillTimer;
        state.secondarySkillCooldown = entity.secondarySkillCooldown;
        state.ultimateTimer = entity.ultimateTimer;
        state.ultimateCooldown = entity.ultimateCooldown;
        state.stealthTimer = entity.stealthTimer;
        state.shieldTimer = entity.shieldTimer;
        state.damageBoostTimer = entity.damageBoostTimer;
        state.redBuffTimer = entity.redBuffTimer;
        state.blueBuffTimer = entity.blueBuffTimer;
        state.level = entity.level;
        state.exp = entity.exp;
        state.nextExp = entity.nextExp;
        state.totalExp = entity.totalExp;
        state.expValue = entity.expValue;
        state.goldValue = entity.goldValue;
        state.kills = entity.kills;
        state.lastHitGold = entity.lastHitGold;
        if (entity.heroType != null) {
            state.heroType = entity.heroType.name();
        }
        if (entity.weapon != null) {
            state.weapon = entity.weapon.name();
        }
        if (entity.armor != null) {
            state.armor = entity.armor.name();
        }
        if (entity.boots != null) {
            state.boots = entity.boots.name();
        }
        if (entity.hat != null) {
            state.hat = entity.hat.name();
        }
        if (entity.relic != null) {
            state.relic = entity.relic.name();
        }
        return state;
    }

    GameEntity toEntity() {
        UnitKind unitKind = parseKind(kind);
        Team unitTeam = parseTeam(team);
        GameEntity entity = new GameEntity(unitTeam, unitKind, x, y);
        entity.assignReplayId(id);
        entity.name = name;
        entity.hp = hp;
        entity.maxHp = maxHp > 0f ? maxHp : hp;
        entity.radius = radius > 0f ? radius : defaultRadius(unitKind);
        entity.facingRad = facingRad;
        entity.alive = alive;
        entity.attackTimer = attackTimer;
        entity.attackCooldown = attackCooldown;
        entity.skillTimer = skillTimer;
        entity.skillCooldown = skillCooldown;
        entity.secondarySkillTimer = secondarySkillTimer;
        entity.secondarySkillCooldown = secondarySkillCooldown;
        entity.ultimateTimer = ultimateTimer;
        entity.ultimateCooldown = ultimateCooldown;
        entity.stealthTimer = stealthTimer;
        entity.shieldTimer = shieldTimer;
        entity.damageBoostTimer = damageBoostTimer;
        entity.redBuffTimer = redBuffTimer;
        entity.blueBuffTimer = blueBuffTimer;
        entity.level = level > 0 ? level : 1;
        entity.exp = exp;
        entity.nextExp = nextExp > 0 ? nextExp : 100;
        entity.totalExp = totalExp;
        entity.expValue = expValue;
        entity.goldValue = goldValue;
        entity.kills = kills;
        entity.lastHitGold = lastHitGold;
        entity.heroType = parseHeroType(heroType);
        if (entity.heroType != null) {
            entity.archetype = entity.heroType.archetype();
        }
        entity.weapon = parseItem(weapon);
        entity.armor = parseItem(armor);
        entity.boots = parseItem(boots);
        entity.hat = parseItem(hat);
        entity.relic = parseItem(relic);
        return entity;
    }

    JSONObject toJson() throws JSONException {
        JSONObject object = new JSONObject();
        if (id > 0L) {
            object.put("id", id);
        }
        object.put("k", kind);
        object.put("tm", team);
        if (name != null) {
            object.put("n", name);
        }
        object.put("x", x);
        object.put("y", y);
        object.put("hp", hp);
        object.put("mh", maxHp);
        object.put("r", radius);
        object.put("fr", facingRad);
        object.put("a", alive ? 1 : 0);
        if (heroType != null) {
            object.put("ht", heroType);
        }
        if (weapon != null) {
            object.put("w", weapon);
        }
        if (armor != null) {
            object.put("ar", armor);
        }
        if (boots != null) {
            object.put("bo", boots);
        }
        if (hat != null) {
            object.put("ha", hat);
        }
        if (relic != null) {
            object.put("re", relic);
        }
        if (attackTimer > 0f) {
            object.put("at", attackTimer);
        }
        if (attackCooldown > 0f) {
            object.put("ac", attackCooldown);
        }
        if (skillTimer > 0f) {
            object.put("st", skillTimer);
        }
        if (skillCooldown > 0f) {
            object.put("sc", skillCooldown);
        }
        if (secondarySkillTimer > 0f) {
            object.put("s2t", secondarySkillTimer);
        }
        if (secondarySkillCooldown > 0f) {
            object.put("s2c", secondarySkillCooldown);
        }
        if (ultimateTimer > 0f) {
            object.put("ut", ultimateTimer);
        }
        if (ultimateCooldown > 0f) {
            object.put("uc", ultimateCooldown);
        }
        if (stealthTimer > 0f) {
            object.put("sl", stealthTimer);
        }
        if (shieldTimer > 0f) {
            object.put("sh", shieldTimer);
        }
        if (damageBoostTimer > 0f) {
            object.put("db", damageBoostTimer);
        }
        if (redBuffTimer > 0f) {
            object.put("rb", redBuffTimer);
        }
        if (blueBuffTimer > 0f) {
            object.put("bb", blueBuffTimer);
        }
        if (level > 1) {
            object.put("lv", level);
        }
        if (exp > 0) {
            object.put("xp", exp);
        }
        if (nextExp > 0 && nextExp != 100) {
            object.put("nx", nextExp);
        }
        if (totalExp > 0) {
            object.put("txp", totalExp);
        }
        if (expValue > 0) {
            object.put("xpv", expValue);
        }
        if (goldValue > 0) {
            object.put("gv", goldValue);
        }
        if (kills > 0) {
            object.put("kl", kills);
        }
        if (lastHitGold > 0) {
            object.put("lhg", lastHitGold);
        }
        return object;
    }

    static ReplayEntityState fromJson(JSONObject object) {
        if (object == null) {
            return null;
        }
        ReplayEntityState state = new ReplayEntityState();
        state.id = object.optLong("id");
        state.kind = object.optString("k", UnitKind.MINION.name());
        state.team = object.optString("tm", Team.NEUTRAL.name());
        state.name = object.optString("n", null);
        state.x = (float) object.optDouble("x");
        state.y = (float) object.optDouble("y");
        state.hp = (float) object.optDouble("hp");
        state.maxHp = (float) object.optDouble("mh", state.hp);
        state.radius = (float) object.optDouble("r", 18f);
        state.facingRad = (float) object.optDouble("fr");
        state.alive = object.optInt("a", 1) == 1;
        state.heroType = object.optString("ht", null);
        if (state.heroType != null && state.heroType.length() == 0) {
            state.heroType = null;
        }
        state.weapon = optName(object, "w");
        state.armor = optName(object, "ar");
        state.boots = optName(object, "bo");
        state.hat = optName(object, "ha");
        state.relic = optName(object, "re");
        state.attackTimer = (float) object.optDouble("at");
        state.attackCooldown = (float) object.optDouble("ac");
        state.skillTimer = (float) object.optDouble("st");
        state.skillCooldown = (float) object.optDouble("sc");
        state.secondarySkillTimer = (float) object.optDouble("s2t");
        state.secondarySkillCooldown = (float) object.optDouble("s2c");
        state.ultimateTimer = (float) object.optDouble("ut");
        state.ultimateCooldown = (float) object.optDouble("uc");
        state.stealthTimer = (float) object.optDouble("sl");
        state.shieldTimer = (float) object.optDouble("sh");
        state.damageBoostTimer = (float) object.optDouble("db");
        state.redBuffTimer = (float) object.optDouble("rb");
        state.blueBuffTimer = (float) object.optDouble("bb");
        state.level = object.optInt("lv", 1);
        state.exp = object.optInt("xp");
        state.nextExp = object.optInt("nx", 100);
        state.totalExp = object.optInt("txp");
        state.expValue = object.optInt("xpv");
        state.goldValue = object.optInt("gv");
        state.kills = object.optInt("kl");
        state.lastHitGold = object.optInt("lhg");
        return state;
    }

    private static String optName(JSONObject object, String key) {
        String value = object.optString(key, null);
        return value == null || value.length() == 0 ? null : value;
    }

    private static UnitKind parseKind(String value) {
        try {
            return UnitKind.valueOf(value);
        } catch (IllegalArgumentException ignored) {
            return UnitKind.MINION;
        }
    }

    private static Team parseTeam(String value) {
        try {
            return Team.valueOf(value);
        } catch (IllegalArgumentException ignored) {
            return Team.NEUTRAL;
        }
    }

    private static HeroType parseHeroType(String value) {
        if (value == null) {
            return null;
        }
        try {
            return HeroType.valueOf(value);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private static ItemType parseItem(String value) {
        if (value == null) {
            return null;
        }
        try {
            return ItemType.valueOf(value);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private static float defaultRadius(UnitKind kind) {
        switch (kind) {
            case HERO:
                return 29f;
            case TOWER:
                return 51f;
            case MONSTER:
                return 27f;
            case BRUTE:
                return 26f;
            case RANGED_MINION:
                return 15f;
            case MINION:
            default:
                return 18f;
        }
    }
}
