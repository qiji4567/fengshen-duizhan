package com.example.duizhan.game;

import com.example.duizhan.game.util.ProgressionRules;
import com.example.duizhan.game.util.HeroTalentCalculator;
public final class EntityFactory {
    private EntityFactory() {
    }

    public static GameEntity hero(Team team, HeroType type, float x, float y, String name) {
        GameEntity hero = new GameEntity(team, UnitKind.HERO, x, y);
        hero.heroType = type;
        hero.archetype = type.archetype();
        hero.basicAttackDamageType = type.basicAttackDamageType();
        hero.rangedBasicAttack = type.usesRangedBasicAttack();
        hero.name = name;
        hero.radius = 29f;
        hero.facingRad = team == Team.RED ? (float) Math.PI : 0f;
        hero.baseMaxHp = type.hp * GameConfig.HERO_HP_SCALE;
        hero.baseAttack = type.attack * GameConfig.HERO_DAMAGE_SCALE;
        hero.baseAttackRange = adjustedHeroRange(type);
        hero.baseAttackCooldown = adjustedAttackCooldown(type);
        hero.baseSpeed = type.speed;
        hero.baseSkillCooldown = type.skillCooldown;
        hero.secondarySkillCooldown = Math.max(4.2f, type.skillCooldown * 0.72f);
        hero.ultimateCooldown = Math.max(12f, type.skillCooldown * 2.25f);
        hero.nextExp = ProgressionRules.nextExpForLevel(hero.level);
        hero.maxHp = hero.baseMaxHp;
        hero.hp = hero.maxHp;
        hero.attack = hero.baseAttack;
        hero.attackRange = hero.baseAttackRange;
        hero.attackCooldown = hero.baseAttackCooldown;
        hero.speed = type.speed;
        hero.skillCooldown = type.skillCooldown;
        hero.goldValue = 120;
        hero.expValue = 115;
        hero.talentCooldown = 11f;
        HeroTalentCalculator.refreshTalentStats(hero);
        return hero;
    }

    public static GameEntity tower(Team team, TowerTier tier, float x, float y, String name) {
        GameEntity tower = new GameEntity(team, UnitKind.TOWER, x, y);
        tower.name = name;
        tower.radius = towerRadius(tier);
        tower.maxHp = towerHp(tier);
        tower.hp = tower.maxHp;
        tower.attack = towerAttack(tier) * (team == Team.BLUE ? 1.08f : 1f);
        tower.attackRange = towerRange(tier);
        tower.attackCooldown = towerCooldown(tier);
        tower.goldValue = 0;
        tower.expValue = 0;
        return tower;
    }

    public static GameEntity minion(Team team, float x, float y, MinionType type, String name) {
        GameEntity minion = new GameEntity(team, minionKind(type), x, y);
        minion.name = name;
        minion.radius = minionRadius(type);
        minion.maxHp = minionHp(type);
        minion.hp = minion.maxHp;
        minion.attack = minionAttack(type);
        minion.attackRange = minionRange(type);
        minion.attackCooldown = minionCooldown(type);
        minion.speed = minionSpeed(type);
        minion.baseSpeed = minion.speed;
        minion.minionSkillTimer = minionSkillDelay(type);
        minion.goldValue = minionGold(type);
        minion.expValue = minionExp(type);
        minion.facingRad = team == Team.RED ? (float) Math.PI : 0f;
        return minion;
    }

    public static GameEntity monster(String name, float x, float y) {
        GameEntity monster = new GameEntity(Team.NEUTRAL, UnitKind.MONSTER, x, y);
        monster.name = name;
        monster.radius = 27f;
        monster.maxHp = 920f;
        monster.hp = monster.maxHp;
        monster.attack = 34f;
        monster.attackRange = 92f;
        monster.attackCooldown = 1.05f;
        monster.speed = 74f;
        monster.baseSpeed = monster.speed;
        monster.goldValue = 72;
        monster.expValue = 58;
        monster.minionSkillTimer = 2.5f;
        monster.facingRad = (float) Math.PI;
        return monster;
    }

    private static float adjustedHeroRange(HeroType type) {
        HeroArchetype archetype = type.archetype();
        if (archetype == HeroArchetype.MARKSMAN) {
            return Math.max(type.range, GameConfig.MARKSMAN_MIN_RANGE) * 0.88f;
        }
        if (archetype == HeroArchetype.MAGE) {
            return Math.max(type.range, GameConfig.MAGE_MIN_RANGE) * 0.90f;
        }
        if (archetype == HeroArchetype.SUPPORT) {
            return Math.max(type.range, GameConfig.SUPPORT_MIN_RANGE) * 0.90f;
        }
        return type.range * 0.92f;
    }

    private static float adjustedAttackCooldown(HeroType type) {
        HeroArchetype archetype = type.archetype();
        if (archetype == HeroArchetype.MARKSMAN) {
            return 0.82f;
        }
        if (archetype == HeroArchetype.MAGE || archetype == HeroArchetype.SUPPORT) {
            return 0.98f;
        }
        return type.range > 170f ? 0.98f : 0.88f;
    }

    private static float towerRadius(TowerTier tier) {
        if (tier == TowerTier.HIGHLAND) {
            return 56f;
        }
        return tier == TowerTier.MIDDLE ? 51f : 48f;
    }

    private static float towerHp(TowerTier tier) {
        if (tier == TowerTier.HIGHLAND) {
            return 2750f;
        }
        return tier == TowerTier.MIDDLE ? 2200f : 1850f;
    }

    private static float towerAttack(TowerTier tier) {
        if (tier == TowerTier.HIGHLAND) {
            return 172f;
        }
        return tier == TowerTier.MIDDLE ? 142f : 118f;
    }

    private static float towerRange(TowerTier tier) {
        if (tier == TowerTier.HIGHLAND) {
            return 465f;
        }
        return tier == TowerTier.MIDDLE ? 420f : 390f;
    }

    private static float towerCooldown(TowerTier tier) {
        if (tier == TowerTier.HIGHLAND) {
            return 0.78f;
        }
        return tier == TowerTier.MIDDLE ? 0.86f : 0.94f;
    }

    private static UnitKind minionKind(MinionType type) {
        if (type == MinionType.BRUTE) {
            return UnitKind.BRUTE;
        }
        if (type == MinionType.RANGED) {
            return UnitKind.RANGED_MINION;
        }
        return UnitKind.MINION;
    }

    private static float minionRadius(MinionType type) {
        if (type == MinionType.BRUTE) {
            return 26f;
        }
        return type == MinionType.RANGED ? 15f : 18f;
    }

    private static float minionHp(MinionType type) {
        if (type == MinionType.BRUTE) {
            return 560f;
        }
        return type == MinionType.RANGED ? 175f : 270f;
    }

    private static float minionAttack(MinionType type) {
        if (type == MinionType.BRUTE) {
            return 31f;
        }
        return type == MinionType.RANGED ? 18f : 15f;
    }

    private static float minionRange(MinionType type) {
        if (type == MinionType.BRUTE) {
            return 115f;
        }
        return type == MinionType.RANGED ? 225f : 82f;
    }

    private static float minionCooldown(MinionType type) {
        if (type == MinionType.BRUTE) {
            return 1.22f;
        }
        return type == MinionType.RANGED ? 1.48f : 1.28f;
    }

    private static float minionSpeed(MinionType type) {
        if (type == MinionType.BRUTE) {
            return 68f;
        }
        return type == MinionType.RANGED ? 82f : 86f;
    }

    private static float minionSkillDelay(MinionType type) {
        if (type == MinionType.BRUTE) {
            return 2.2f;
        }
        return type == MinionType.RANGED ? 3.6f : 4.2f;
    }

    private static int minionGold(MinionType type) {
        if (type == MinionType.BRUTE) {
            return 42;
        }
        return type == MinionType.RANGED ? 16 : 14;
    }

    private static int minionExp(MinionType type) {
        if (type == MinionType.BRUTE) {
            return 32;
        }
        return type == MinionType.RANGED ? 10 : 9;
    }
}
