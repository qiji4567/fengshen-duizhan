package com.example.duizhan.game.util;

import com.example.duizhan.game.DamageType;
import com.example.duizhan.game.GameEntity;
import com.example.duizhan.game.HeroArchetype;
import com.example.duizhan.game.HeroType;

public final class HeroTalentCalculator {
    private HeroTalentCalculator() {
    }

    public static float talentDamageBonusRate(GameEntity hero) {
        if (hero == null || hero.heroType == null) {
            return 0f;
        }
        float base;
        switch (hero.archetype) {
            case MAGE:
                base = 0.09f;
                break;
            case ASSASSIN:
                base = 0.11f;
                break;
            case MARKSMAN:
                base = 0.08f;
                break;
            case TANK:
                base = 0.05f;
                break;
            case SUPPORT:
                base = 0.06f;
                break;
            case FIGHTER:
            default:
                base = 0.07f;
                break;
        }
        return base + Math.max(0, hero.level - 1) * 0.012f;
    }

    public static void refreshTalentStats(GameEntity hero) {
        if (hero == null) {
            return;
        }
        hero.talentDamageBonusRate = talentDamageBonusRate(hero);
    }

    public static DamageType talentDamageType(HeroType heroType) {
        if (heroType == null) {
            return DamageType.PHYSICAL;
        }
        HeroArchetype archetype = heroType.archetype();
        if (archetype == HeroArchetype.MAGE || archetype == HeroArchetype.SUPPORT) {
            return DamageType.MAGIC;
        }
        if (archetype == HeroArchetype.ASSASSIN && heroType.basicAttackDamageType() == DamageType.MAGIC) {
            return DamageType.MAGIC;
        }
        return DamageType.PHYSICAL;
    }
}
