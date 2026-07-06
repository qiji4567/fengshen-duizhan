package com.example.duizhan.game;

import com.example.duizhan.game.util.GameMath;

public class GameEntity {
    private static long nextId = 1;

    public long id = nextId++;
    public final Team team;
    public final UnitKind kind;
    public String name;
    public float x;
    public float y;
    public float radius;
    public float baseMaxHp;
    public float baseAttack;
    public float baseAttackRange;
    public float baseAttackCooldown;
    public float baseSkillCooldown;
    public float maxHp;
    public float hp;
    public float attack;
    public float attackRange;
    public float attackCooldown;
    public float attackTimer;
    public float speed;
    public float baseSpeed;
    public boolean alive = true;
    public HeroType heroType;
    public HeroArchetype archetype;
    public DamageType basicAttackDamageType = DamageType.PHYSICAL;
    public boolean rangedBasicAttack;
    public float skillCooldown;
    public float skillTimer;
    public float secondarySkillCooldown;
    public float secondarySkillTimer;
    public float ultimateCooldown;
    public float ultimateTimer;
    public float minionSkillTimer;
    public float shieldTimer;
    public float barrierHp;
    public float blockTimer;
    public float damageBoostTimer;
    public float slowTimer;
    public float stunTimer;
    public float stealthTimer;
    public float mimicTimer;
    public HeroType mimicHeroType;
    public float redBuffTimer;
    public float blueBuffTimer;
    public float speedBuffTimer;
    public float hitTimer;
    public float respawnTimer;
    public float damageBonusRate;
    public float magicDamageBonusRate;
    public float damageReductionRate;
    public float attackSpeedBonusRate;
    public float lifeStealRate;
    public float talentDamageBonusRate;
    public float talentCooldown = 11f;
    public float talentTimer;
    public float recallChannelTimer;
    /** Radians. 0 = right, PI = left. Driven by move input, not position jitter. */
    public float facingRad;
    public int level = 1;
    public int exp;
    public int nextExp = 100;
    public int totalExp;
    public int expValue;
    public int goldValue;
    public int kills;
    public int lastHitGold;
    public float lastBlockedDamage;
    public ItemType weapon;
    public ItemType armor;
    public ItemType boots;
    public ItemType hat;
    public ItemType relic;

    public GameEntity(Team team, UnitKind kind, float x, float y) {
        this.team = team;
        this.kind = kind;
        this.x = x;
        this.y = y;
    }

    public void assignReplayId(long replayId) {
        if (replayId > 0L) {
            this.id = replayId;
            if (replayId >= nextId) {
                nextId = replayId + 1L;
            }
        }
    }

    public boolean isEnemy(GameEntity other) {
        return other != null && alive && other.alive && team != other.team;
    }

    public float damage(float amount) {
        lastBlockedDamage = 0f;
        if (blockTimer > 0f) {
            float beforeBlock = amount;
            amount *= 0.68f;
            lastBlockedDamage += beforeBlock - amount;
        }
        if (shieldTimer > 0f) {
            float beforeShieldReduce = amount;
            amount *= 0.45f;
            lastBlockedDamage += beforeShieldReduce - amount;
            if (barrierHp > 0f) {
                float absorbed = Math.min(amount, barrierHp);
                barrierHp -= absorbed;
                amount -= absorbed;
                lastBlockedDamage += absorbed;
            }
        }
        hp -= amount;
        if (hp <= 0f) {
            hp = 0f;
            alive = false;
        }
        hitTimer = 0.18f;
        return amount;
    }

    public float distanceTo(GameEntity other) {
        return GameMath.distance(x, y, other.x, other.y);
    }
}
