package com.example.duizhan.game.util;

import com.example.duizhan.game.DamageType;
import com.example.duizhan.game.GameEntity;
import com.example.duizhan.game.Team;
import com.example.duizhan.game.UnitKind;

public final class CombatCalculator {
    private CombatCalculator() {
    }

    public static float calculateDamage(GameEntity attacker, GameEntity target, float rawDamage, Team damageTeam) {
        return calculateDamage(attacker, target, rawDamage, DamageType.PHYSICAL, damageTeam);
    }

    public static float calculateDamage(GameEntity attacker, GameEntity target, float rawDamage,
                                        DamageType damageType, Team damageTeam) {
        float result = rawDamage;
        if (attacker != null && attacker.kind == UnitKind.HERO) {
            result *= 1f + attacker.damageBonusRate + attacker.talentDamageBonusRate
                    + (attacker.level - 1) * 0.045f;
            if (damageType == DamageType.MAGIC) {
                result *= 1f + attacker.magicDamageBonusRate;
            }
            if (attacker.redBuffTimer > 0f) {
                result *= 1.12f;
            }
            if (attacker.damageBoostTimer > 0f) {
                result *= 1.18f;
            }
        }
        if (attacker != null && attacker.kind == UnitKind.MONSTER) {
            result *= 1.08f;
        }
        if (target != null && damageType != DamageType.TRUE_DAMAGE) {
            result *= resistanceMultiplier(target, damageType);
        }
        if (damageTeam == Team.NEUTRAL && target != null && target.kind == UnitKind.TOWER) {
            result *= 0.35f;
        }
        return Math.max(1f, result);
    }

    private static float resistanceMultiplier(GameEntity target, DamageType damageType) {
        float resistance = baseResistance(target, damageType);
        resistance += target.damageReductionRate * (damageType == DamageType.MAGIC ? 82f : 100f);
        return 100f / (100f + Math.max(0f, resistance));
    }

    private static float baseResistance(GameEntity target, DamageType damageType) {
        if (target.kind == UnitKind.HERO) {
            return damageType == DamageType.MAGIC
                    ? 24f + target.level * 2.4f
                    : 30f + target.level * 3.2f;
        }
        if (target.kind == UnitKind.TOWER) {
            return damageType == DamageType.MAGIC ? 45f : 62f;
        }
        if (target.kind == UnitKind.BRUTE) {
            return damageType == DamageType.MAGIC ? 20f : 28f;
        }
        if (target.kind == UnitKind.RANGED_MINION) {
            return damageType == DamageType.MAGIC ? 9f : 12f;
        }
        if (target.kind == UnitKind.MONSTER) {
            return damageType == DamageType.MAGIC ? 18f : 22f;
        }
        return damageType == DamageType.MAGIC ? 10f : 14f;
    }
}
