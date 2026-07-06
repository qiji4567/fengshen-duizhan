package com.example.duizhan.game;

import android.graphics.Color;

import com.example.duizhan.game.util.TeamStyle;

/**
 * Maps each hero's signature weapon and skill style to distinct projectile visuals.
 */
public final class HeroProjectileProfile {
    private HeroProjectileProfile() {
    }

    public static ProjectileVisual basicAttackVisual(GameEntity attacker, DamageType damageType) {
        if (attacker.kind == UnitKind.TOWER) {
            return ProjectileVisual.TOWER_BLAST;
        }
        if (attacker.kind == UnitKind.RANGED_MINION) {
            return ProjectileVisual.TALISMAN;
        }
        if (attacker.kind != UnitKind.HERO) {
            return ProjectileVisual.BOLT;
        }
        return basicAttackVisual(attacker.heroType, damageType, attacker.rangedBasicAttack);
    }

    public static ProjectileVisual basicAttackVisual(HeroType type, DamageType damageType, boolean ranged) {
        HeroVisualProfile profile = HeroVisualProfile.of(type);
        if (damageType == DamageType.MAGIC) {
            return magicVisual(profile);
        }
        if (!ranged) {
            return physicalMeleeVisual(profile);
        }
        return rangedPhysicalVisual(profile);
    }

    public static ProjectileVisual skillVisual(HeroType type, SkillStyle style) {
        if (style == null) {
            return ProjectileVisual.BOLT;
        }
        switch (style) {
            case GOD_LIST:
            case PAGODA_SEAL:
                return ProjectileVisual.GOLD_SEAL;
            case WIND_BLADE:
                return ProjectileVisual.STAR;
            case YIN_YANG_ORB:
            case MOON_FROST:
            case LOTUS_BARRIER:
                return ProjectileVisual.MAGIC_ORB;
            case THUNDER_CHAIN:
            case EYE_BEAM:
                return ProjectileVisual.LIGHTNING;
            case FIRE_WHEEL:
            case FIRE_CAGE:
            case PHOENIX_FLAME:
                return ProjectileVisual.FIRE_ORB;
            case BONE_TRAP:
            case SPIRIT_CHAIN:
                return ProjectileVisual.TALISMAN;
            default:
                return magicVisual(HeroVisualProfile.of(type));
        }
    }

    private static ProjectileVisual rangedPhysicalVisual(HeroVisualProfile profile) {
        switch (profile.weapon) {
            case BOW:
                return ProjectileVisual.ARROW;
            case FAN:
                return ProjectileVisual.STAR;
            case GOURD:
            case BEADS:
                return ProjectileVisual.TALISMAN;
            case BOOK:
            case SEAL:
            case PAGODA:
                return ProjectileVisual.GOLD_SEAL;
            case FIRE_SPEAR:
                return ProjectileVisual.FIRE_ORB;
            case TRIDENT:
                return ProjectileVisual.LIGHTNING;
            case STAFF:
            case GOLDEN_STAFF:
            case SPEAR:
            case WHIP:
            case SWORD:
            case AXE:
            case RAKE:
            case CHAIN:
            case HOOP:
            case LOTUS:
            default:
                return ProjectileVisual.BOLT;
        }
    }

    private static ProjectileVisual magicVisual(HeroVisualProfile profile) {
        switch (profile.weapon) {
            case FIRE_SPEAR:
            case FAN:
                return ProjectileVisual.FIRE_ORB;
            case TRIDENT:
                return ProjectileVisual.LIGHTNING;
            case LOTUS:
                return ProjectileVisual.MAGIC_ORB;
            case BOOK:
            case SEAL:
            case PAGODA:
                return ProjectileVisual.GOLD_SEAL;
            case GOURD:
            case BEADS:
                return ProjectileVisual.TALISMAN;
            case BOW:
                return ProjectileVisual.ARROW;
            default:
                return ProjectileVisual.MAGIC_ORB;
        }
    }

    private static ProjectileVisual physicalMeleeVisual(HeroVisualProfile profile) {
        switch (profile.weapon) {
            case FIRE_SPEAR:
                return ProjectileVisual.FIRE_ORB;
            case TRIDENT:
                return ProjectileVisual.LIGHTNING;
            case BOW:
                return ProjectileVisual.ARROW;
            default:
                return ProjectileVisual.BOLT;
        }
    }

    public static int projectileColor(GameEntity attacker, DamageType damageType) {
        if (attacker.kind == UnitKind.TOWER) {
            return attacker.team == Team.BLUE ? Color.rgb(147, 197, 253) : Color.rgb(252, 165, 165);
        }
        if (attacker.kind == UnitKind.RANGED_MINION) {
            return attacker.team == Team.BLUE ? Color.rgb(147, 197, 253) : Color.rgb(252, 165, 165);
        }
        if (attacker.kind == UnitKind.HERO && attacker.heroType != null) {
            return heroColor(attacker.heroType, damageType);
        }
        if (damageType == DamageType.MAGIC) {
            return Color.rgb(192, 132, 252);
        }
        if (damageType == DamageType.TRUE_DAMAGE) {
            return Color.rgb(250, 204, 21);
        }
        return TeamStyle.color(attacker.team);
    }

    public static int heroColor(HeroType type, DamageType damageType) {
        HeroVisualProfile profile = HeroVisualProfile.of(type);
        if (damageType == DamageType.TRUE_DAMAGE) {
            return Color.rgb(250, 204, 21);
        }
        if (damageType == DamageType.MAGIC) {
            return profile.trimColor;
        }
        return profile.trimColor;
    }

    public static int skillColor(HeroType type, SkillStyle style) {
        if (style != null) {
            switch (style) {
                case FIRE_WHEEL:
                case FIRE_CAGE:
                case PHOENIX_FLAME:
                    return Color.rgb(249, 115, 22);
                case THUNDER_CHAIN:
                case EYE_BEAM:
                    return Color.rgb(56, 189, 248);
                case MOON_FROST:
                    return Color.rgb(165, 243, 252);
                case HEALING_RAIN:
                case LOTUS_BARRIER:
                    return Color.rgb(34, 197, 94);
                case GOD_LIST:
                    return Color.rgb(250, 204, 21);
                case WIND_BLADE:
                    return Color.rgb(134, 239, 172);
                default:
                    break;
            }
        }
        return HeroVisualProfile.of(type).trimColor;
    }
}
