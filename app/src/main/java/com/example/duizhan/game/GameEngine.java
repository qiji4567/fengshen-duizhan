package com.example.duizhan.game;

import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;

import com.example.duizhan.game.util.CombatCalculator;
import com.example.duizhan.game.util.EquipmentCalculator;
import com.example.duizhan.game.util.GameMath;
import com.example.duizhan.game.util.GameTerrain;
import com.example.duizhan.game.util.HeroSkillNameResolver;
import com.example.duizhan.game.util.HeroTalentCalculator;
import com.example.duizhan.game.util.ProgressionRules;
import com.example.duizhan.game.util.TeamStyle;
import com.example.duizhan.data.BattleSummary;
import com.example.duizhan.game.guide.BuildGuideResolver;
import com.example.duizhan.game.guide.BuildPlan;
import com.example.duizhan.game.ai.AiDifficultyProfile;
import com.example.duizhan.game.BattleDifficulty;
import com.example.duizhan.game.audio.BattleVoicePhrase;
import com.example.duizhan.game.audio.BattleVoiceStep;
import com.example.duizhan.game.text.GameTextKey;
import com.example.duizhan.game.text.GameTextProvider;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GameEngine {
    private static final long MULTI_KILL_WINDOW_MS = 10000L;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final List<GameEntity> entities = new ArrayList<>();
    private final List<Projectile> projectiles = new ArrayList<>();
    private final List<VisualEffect> effects = new ArrayList<>();
    private final List<GameSoundEffect> soundEffects = new ArrayList<>();
    private final List<BattleVoiceStep> voiceSteps = new ArrayList<>();
    private final GameListener listener;
    private final HeroType blueHeroType;
    private final HeroType redHeroType;
    private final BattleDifficulty difficulty;
    private final AiDifficultyProfile aiProfile;
    private final GameTextProvider text;
    private final Runnable tickRunnable = this::tick;

    private GameEntity blueHero;
    private GameEntity redHero;
    private GameEntity blueOuterTower;
    private GameEntity blueMiddleTower;
    private GameEntity blueHighlandTower;
    private GameEntity redOuterTower;
    private GameEntity redMiddleTower;
    private GameEntity redHighlandTower;
    private long startMs;
    private long lastMs;
    private float spawnTimer;
    private float bruteTimer;
    private float monsterTimer;
    private float redShopTimer;
    private float blueMoveX;
    private float blueMoveY;
    private int blueGold = 180;
    private int redGold = 180;
    private int blueKills;
    private int redKills;
    private int blueMultiKillCount;
    private int redMultiKillCount;
    private int blueHeroKillStreak;
    private int redHeroKillStreak;
    private long blueMultiKillUntilMs;
    private long redMultiKillUntilMs;
    private float blueFountainEffectTimer;
    private float redFountainEffectTimer;
    private boolean blueWasInBrush;
    private boolean blueRecallSnapCamera;
    private GameEntity skillFocusTarget;
    private boolean running;
    private boolean paused;
    private Team winner;
    private String message;

    public GameEngine(HeroType blueHeroType, HeroType redHeroType, BattleDifficulty difficulty,
                      GameListener listener, GameTextProvider text) {
        this.blueHeroType = blueHeroType;
        this.redHeroType = redHeroType;
        this.difficulty = difficulty == null ? BattleDifficulty.MEDIUM : difficulty;
        this.aiProfile = AiDifficultyProfile.forDifficulty(this.difficulty);
        this.listener = listener;
        this.text = text;
        message = text.get(GameTextKey.BATTLE_START);
    }

    public void start() {
        entities.clear();
        projectiles.clear();
        effects.clear();
        soundEffects.clear();
        voiceSteps.clear();
        blueHighlandTower = EntityFactory.tower(Team.BLUE, TowerTier.HIGHLAND,
                GameConfig.BLUE_HIGHLAND_TOWER_X, GameConfig.LANE_Y, towerName(Team.BLUE, TowerTier.HIGHLAND));
        blueMiddleTower = EntityFactory.tower(Team.BLUE, TowerTier.MIDDLE,
                GameConfig.BLUE_MIDDLE_TOWER_X, GameConfig.LANE_Y, towerName(Team.BLUE, TowerTier.MIDDLE));
        blueOuterTower = EntityFactory.tower(Team.BLUE, TowerTier.OUTER,
                GameConfig.BLUE_OUTER_TOWER_X, GameConfig.LANE_Y, towerName(Team.BLUE, TowerTier.OUTER));
        redOuterTower = EntityFactory.tower(Team.RED, TowerTier.OUTER,
                GameConfig.RED_OUTER_TOWER_X, GameConfig.LANE_Y, towerName(Team.RED, TowerTier.OUTER));
        redMiddleTower = EntityFactory.tower(Team.RED, TowerTier.MIDDLE,
                GameConfig.RED_MIDDLE_TOWER_X, GameConfig.LANE_Y, towerName(Team.RED, TowerTier.MIDDLE));
        redHighlandTower = EntityFactory.tower(Team.RED, TowerTier.HIGHLAND,
                GameConfig.RED_HIGHLAND_TOWER_X, GameConfig.LANE_Y, towerName(Team.RED, TowerTier.HIGHLAND));
        blueHero = EntityFactory.hero(Team.BLUE, blueHeroType,
                GameConfig.fountainX(Team.BLUE), GameConfig.fountainY(Team.BLUE),
                heroName(Team.BLUE, blueHeroType));
        redHero = EntityFactory.hero(Team.RED, redHeroType,
                GameConfig.fountainX(Team.RED), GameConfig.fountainY(Team.RED),
                heroName(Team.RED, redHeroType));
        entities.add(blueHighlandTower);
        entities.add(blueMiddleTower);
        entities.add(blueOuterTower);
        entities.add(redOuterTower);
        entities.add(redMiddleTower);
        entities.add(redHighlandTower);
        entities.add(blueHero);
        entities.add(redHero);
        spawnWave(false);
        spawnMonsters();
        emitSound(GameSoundEffect.BATTLE_START);
        spawnTimer = 0f;
        bruteTimer = 0f;
        monsterTimer = 0f;
        redShopTimer = 0f;
        blueGold = 180;
        redGold = 180;
        blueKills = 0;
        redKills = 0;
        blueMultiKillCount = 0;
        redMultiKillCount = 0;
        blueHeroKillStreak = 0;
        redHeroKillStreak = 0;
        blueMultiKillUntilMs = 0L;
        redMultiKillUntilMs = 0L;
        blueFountainEffectTimer = 0f;
        redFountainEffectTimer = 0f;
        blueWasInBrush = false;
        startMs = System.currentTimeMillis();
        lastMs = startMs;
        running = true;
        paused = false;
        winner = null;
        handler.post(tickRunnable);
    }

    public void stop() {
        running = false;
        handler.removeCallbacks(tickRunnable);
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
        message = paused ? text.get(GameTextKey.PAUSED) : text.get(GameTextKey.RESUMED);
    }

    public boolean isPaused() {
        return paused;
    }

    public void setBlueMove(float x, float y) {
        blueMoveX = x;
        blueMoveY = y;
    }

    public void castBlueSkill(SkillSlot skillSlot) {
        if (blueHero == null || !blueHero.alive || winner != null) {
            return;
        }
        cancelRecall(blueHero, GameTextKey.RECALL_CANCEL_ACTION);
        castSkill(blueHero, skillSlot);
    }

    public void castBlueTalent() {
        if (blueHero == null || !blueHero.alive || winner != null || blueHero.talentTimer > 0f) {
            return;
        }
        cancelRecall(blueHero, GameTextKey.RECALL_CANCEL_ACTION);
        if (!canCastTalent(blueHero)) {
            showNoSkillTarget(blueHero);
            return;
        }
        castTalentSkill(blueHero);
    }

    private boolean canCastTalent(GameEntity hero) {
        if (hero == null || hero.archetype == null) {
            return false;
        }
        switch (hero.archetype) {
            case MARKSMAN:
                return selectSkillTarget(hero, hero.attackRange + 160f) != null;
            case ASSASSIN:
                return selectSkillTarget(hero, 360f) != null;
            case MAGE:
            case SUPPORT:
                return selectSkillTarget(hero, 520f) != null;
            case FIGHTER:
                return selectSkillTarget(hero, 360f) != null;
            case TANK:
                return selectSkillTarget(hero, 280f) != null;
            default:
                return selectSkillTarget(hero, 320f) != null;
        }
    }

    public void blueBasicAttack() {
        if (blueHero == null || !blueHero.alive || winner != null) {
            return;
        }
        cancelRecall(blueHero, GameTextKey.RECALL_CANCEL_ACTION);
        GameEntity target = selectSkillTarget(blueHero, blueHero.attackRange);
        if (target == null) {
            showNoSkillTarget(blueHero);
            return;
        }
        blueHero.attackTimer = 0f;
        tryAttack(blueHero);
    }

    public String recallBlueHero() {
        if (blueHero == null || !blueHero.alive || winner != null) {
            message = text.get(GameTextKey.HERO_DEAD_CANNOT_BUY);
            return message;
        }
        if (isInOwnFountain(blueHero)) {
            message = text.get(GameTextKey.RECALL_ALREADY_HOME, blueHero.name);
            return message;
        }
        if (blueHero.recallChannelTimer > 0f) {
            message = text.get(GameTextKey.RECALL_START, blueHero.name, GameConfig.RECALL_CHANNEL_DURATION);
            return message;
        }
        blueHero.recallChannelTimer = GameConfig.RECALL_CHANNEL_DURATION;
        ring(blueHero.x, blueHero.y, 96f, Color.rgb(34, 197, 94), 0.45f);
        String result = text.get(GameTextKey.RECALL_START, blueHero.name, GameConfig.RECALL_CHANNEL_DURATION);
        message = result;
        return result;
    }

    public String buyBlueItem(ItemType itemType) {
        if (blueHero == null || !blueHero.alive) {
            message = text.get(GameTextKey.HERO_DEAD_CANNOT_BUY);
            return message;
        }
        if (!isInOwnFountain(blueHero)) {
            message = text.get(GameTextKey.HERO_MUST_RETURN_TO_FOUNTAIN);
            return message;
        }
        if (blueGold < itemType.cost) {
            message = text.get(GameTextKey.GOLD_NOT_ENOUGH, itemType.cost - blueGold);
            return message;
        }
        blueGold -= itemType.cost;
        applyItem(blueHero, itemType);
        emitSound(GameSoundEffect.SHOP);
        message = text.get(GameTextKey.BUY_SUCCESS, itemType.label);
        return message;
    }

    public BuildPlan getBlueBuildPlan() {
        if (blueHero == null) {
            return BuildGuideResolver.buildPlan(blueHeroType, redHeroType, null, null, null, null, null,
                    blueGold, 1f, 1, DamageType.PHYSICAL);
        }
        return BuildGuideResolver.buildPlan(
                blueHeroType,
                redHeroType,
                blueHero.weapon,
                blueHero.armor,
                blueHero.boots,
                blueHero.hat,
                blueHero.relic,
                blueGold,
                blueHero.maxHp > 0f ? blueHero.hp / blueHero.maxHp : 1f,
                blueHero.level,
                blueHero.basicAttackDamageType);
    }

    public String applyBlueBuildPlan(boolean replaceAll) {
        BuildPlan plan = getBlueBuildPlan();
        if (plan.alreadyOptimal) {
            message = text.get(GameTextKey.BUILD_ALREADY_OPTIMAL);
            return message;
        }
        java.util.List<ItemType> items = replaceAll
                ? plan.affordablePurchases
                : (plan.nextPurchase == null
                ? java.util.Collections.<ItemType>emptyList()
                : java.util.Collections.singletonList(plan.nextPurchase));
        if (items.isEmpty()) {
            if (replaceAll) {
                message = text.get(GameTextKey.GOLD_NOT_ENOUGH, plan.totalCost - blueGold);
            } else if (plan.nextPurchase != null) {
                message = text.get(GameTextKey.GOLD_NOT_ENOUGH, plan.nextPurchase.cost - blueGold);
            } else {
                message = text.get(GameTextKey.BUILD_ALREADY_OPTIMAL);
            }
            return message;
        }
        return buyBlueItems(items, !replaceAll ? plan : null);
    }

    private String buyBlueItems(java.util.List<ItemType> items, BuildPlan plan) {
        if (items.isEmpty()) {
            return text.get(GameTextKey.BUILD_ALREADY_OPTIMAL);
        }
        String lastMessage = "";
        int bought = 0;
        for (ItemType itemType : items) {
            String result = buyBlueItem(itemType);
            lastMessage = result;
            if (result.equals(text.get(GameTextKey.BUY_SUCCESS, itemType.label))) {
                bought++;
                continue;
            }
            if (bought == 0) {
                return result;
            }
            break;
        }
        if (bought > 1) {
            message = text.get(GameTextKey.BUILD_APPLY_SUCCESS, bought);
            return message;
        }
        if (bought == 1 && plan != null && plan.nextPurchaseReason.length() > 0) {
            message = text.get(GameTextKey.BUY_SUCCESS, items.get(0).label) + "（" + plan.nextPurchaseReason + "）";
            return message;
        }
        return lastMessage;
    }

    public GameSnapshot snapshot() {
        GameSnapshot snapshot = new GameSnapshot();
        snapshot.entities.addAll(entities);
        snapshot.projectiles.addAll(projectiles);
        snapshot.effects.addAll(effects);
        snapshot.soundEffects.addAll(soundEffects);
        soundEffects.clear();
        snapshot.voiceSteps.addAll(voiceSteps);
        voiceSteps.clear();
        snapshot.blueTowerHp = blueHighlandTower == null ? 0f : blueHighlandTower.hp;
        snapshot.redTowerHp = redHighlandTower == null ? 0f : redHighlandTower.hp;
        snapshot.blueHeroHp = blueHero == null ? 0f : blueHero.hp;
        snapshot.blueHeroMaxHp = blueHero == null ? 0f : blueHero.maxHp;
        snapshot.blueAttack = blueHero == null ? 0f : blueHero.attack;
        snapshot.blueDamageBonusRate = blueHero == null ? 0f : blueHero.damageBonusRate;
        snapshot.blueDamageReductionRate = blueHero == null ? 0f : blueHero.damageReductionRate;
        snapshot.blueSkillCooldown = blueHero == null ? 0f : blueHero.skillTimer;
        snapshot.blueSkillCooldownMax = blueHero == null ? 0f : blueHero.skillCooldown;
        snapshot.blueSecondarySkillCooldown = blueHero == null ? 0f : blueHero.secondarySkillTimer;
        snapshot.blueSecondarySkillCooldownMax = blueHero == null ? 0f : blueHero.secondarySkillCooldown;
        snapshot.blueUltimateCooldown = blueHero == null ? 0f : blueHero.ultimateTimer;
        snapshot.blueUltimateCooldownMax = blueHero == null ? 0f : blueHero.ultimateCooldown;
        snapshot.blueArchetype = blueHero == null ? HeroArchetype.FIGHTER : blueHero.archetype;
        snapshot.blueBasicAttackDamageType = blueHero == null ? DamageType.PHYSICAL : blueHero.basicAttackDamageType;
        HeroType snapshotHeroType = blueHero == null ? blueHeroType : blueHero.heroType;
        snapshot.bluePrimarySkillName = HeroSkillNameResolver.primaryName(snapshotHeroType);
        snapshot.blueSecondarySkillName = HeroSkillNameResolver.secondaryName(snapshotHeroType);
        snapshot.blueUltimateSkillName = HeroSkillNameResolver.ultimateName(snapshotHeroType);
        snapshot.blueTalentName = HeroSkillNameResolver.talentName(snapshotHeroType);
        snapshot.blueTalentCooldown = blueHero == null ? 0f : blueHero.talentTimer;
        snapshot.blueTalentCooldownMax = blueHero == null ? 0f : blueHero.talentCooldown;
        snapshot.blueTalentDamageBonusRate = blueHero == null ? 0f : blueHero.talentDamageBonusRate;
        snapshot.blueRecallChannel = blueHero == null ? 0f : blueHero.recallChannelTimer;
        snapshot.snapCameraToBlueHero = blueRecallSnapCamera;
        blueRecallSnapCamera = false;
        snapshot.blueWeaponName = equippedName(blueHero == null ? null : blueHero.weapon);
        snapshot.blueArmorName = equippedName(blueHero == null ? null : blueHero.armor);
        snapshot.blueBootsName = equippedName(blueHero == null ? null : blueHero.boots);
        snapshot.blueHatName = equippedName(blueHero == null ? null : blueHero.hat);
        snapshot.blueRelicName = equippedName(blueHero == null ? null : blueHero.relic);
        snapshot.blueLevel = blueHero == null ? 1 : blueHero.level;
        snapshot.blueExp = blueHero == null ? 0 : blueHero.exp;
        snapshot.blueNextExp = blueHero == null ? 100 : blueHero.nextExp;
        snapshot.blueGold = blueGold;
        snapshot.redGold = redGold;
        snapshot.blueKills = blueKills;
        snapshot.redKills = redKills;
        snapshot.difficulty = difficulty;
        snapshot.message = message;
        snapshot.finished = winner != null;
        snapshot.winner = winner;
        return snapshot;
    }

    private void tick() {
        if (!running) {
            return;
        }
        long now = System.currentTimeMillis();
        float dt = Math.min(0.05f, (now - lastMs) / 1000f);
        lastMs = now;
        if (!paused && winner == null) {
            update(dt);
        }
        listener.onGameChanged(snapshot());
        handler.postDelayed(tickRunnable, GameConfig.FRAME_MS);
    }

    private void update(float dt) {
        spawnTimer += dt;
        bruteTimer += dt;
        monsterTimer += dt;
        redShopTimer += dt;
        if (spawnTimer >= GameConfig.MINION_WAVE_INTERVAL) {
            spawnTimer -= GameConfig.MINION_WAVE_INTERVAL;
            spawnWave(false);
        }
        if (bruteTimer >= GameConfig.BRUTE_WAVE_INTERVAL) {
            bruteTimer -= GameConfig.BRUTE_WAVE_INTERVAL;
            spawnWave(true);
        }
        if (monsterTimer >= GameConfig.MONSTER_RESPAWN_INTERVAL) {
            monsterTimer -= GameConfig.MONSTER_RESPAWN_INTERVAL;
            if (countLiving(UnitKind.MONSTER) == 0) {
                spawnMonsters();
            }
        }
        if (redShopTimer >= aiProfile.shopInterval) {
            redShopTimer = 0f;
            autoBuyForRed();
        }

        for (GameEntity entity : new ArrayList<>(entities)) {
            updateTimers(entity, dt);
            if (!entity.alive) {
                updateRespawn(entity, dt);
                continue;
            }
            if (entity.kind == UnitKind.HERO) {
                updateFountainRegen(entity, dt);
                updateBlueBuffRegen(entity, dt);
            }
            if (entity.stunTimer > 0f) {
                if (entity.kind == UnitKind.TOWER) {
                    tryAttack(entity);
                }
                continue;
            }
            if (entity.kind == UnitKind.HERO && entity.team == Team.BLUE) {
                boolean justRecalled = updateBlueRecall(entity, dt);
                if (entity.recallChannelTimer <= 0f && !justRecalled) {
                    moveHero(entity, dt, blueMoveX, blueMoveY);
                    updateBlueBrushFeedback(entity);
                }
            } else if (entity.kind == UnitKind.HERO && entity.team == Team.RED) {
                updateEnemyHero(entity, dt);
            } else if (isLaneMinion(entity)) {
                updateMinion(entity, dt);
            } else if (entity.kind == UnitKind.MONSTER) {
                updateMonster(entity, dt);
            }
            tryAttack(entity);
        }
        updateProjectiles(dt);
        updateEffects(dt);
        removeDeadUnits();
        checkWinner();
    }

    private void updateTimers(GameEntity entity, float dt) {
        entity.attackTimer = Math.max(0f, entity.attackTimer - dt);
        entity.skillTimer = Math.max(0f, entity.skillTimer - dt);
        entity.secondarySkillTimer = Math.max(0f, entity.secondarySkillTimer - dt);
        entity.ultimateTimer = Math.max(0f, entity.ultimateTimer - dt);
        entity.talentTimer = Math.max(0f, entity.talentTimer - dt);
        entity.minionSkillTimer = Math.max(0f, entity.minionSkillTimer - dt);
        entity.shieldTimer = Math.max(0f, entity.shieldTimer - dt);
        entity.blockTimer = Math.max(0f, entity.blockTimer - dt);
        entity.damageBoostTimer = Math.max(0f, entity.damageBoostTimer - dt);
        if (entity.shieldTimer <= 0f) {
            entity.barrierHp = 0f;
        }
        entity.slowTimer = Math.max(0f, entity.slowTimer - dt);
        entity.stunTimer = Math.max(0f, entity.stunTimer - dt);
        entity.stealthTimer = Math.max(0f, entity.stealthTimer - dt);
        entity.mimicTimer = Math.max(0f, entity.mimicTimer - dt);
        if (entity.mimicTimer <= 0f) {
            entity.mimicHeroType = null;
        }
        entity.redBuffTimer = Math.max(0f, entity.redBuffTimer - dt);
        entity.blueBuffTimer = Math.max(0f, entity.blueBuffTimer - dt);
        entity.speedBuffTimer = Math.max(0f, entity.speedBuffTimer - dt);
        entity.hitTimer = Math.max(0f, entity.hitTimer - dt);
    }

    private void updateRespawn(GameEntity entity, float dt) {
        if (entity.kind != UnitKind.HERO) {
            return;
        }
        entity.respawnTimer = Math.max(0f, entity.respawnTimer - dt);
        if (entity.respawnTimer <= 0f) {
            entity.alive = true;
            entity.hp = entity.maxHp;
            entity.x = GameConfig.fountainX(entity.team);
            entity.y = GameConfig.fountainY(entity.team);
            ring(entity.x, entity.y, 80f, Color.rgb(34, 197, 94), 0.5f);
            message = text.get(GameTextKey.ENTITY_RESPAWN, entity.name);
        }
    }

    private void updateFountainRegen(GameEntity hero, float dt) {
        if (hero.hp >= hero.maxHp || !isInOwnFountain(hero)) {
            return;
        }
        float recover = GameConfig.FOUNTAIN_REGEN_PER_MS * dt * 1000f;
        float oldHp = hero.hp;
        hero.hp = Math.min(hero.maxHp, hero.hp + recover);
        updateFountainEffect(hero, hero.hp - oldHp, dt);
    }

    private boolean isInOwnFountain(GameEntity hero) {
        return GameMath.distance(hero.x, hero.y,
                GameConfig.fountainX(hero.team), GameConfig.fountainY(hero.team))
                <= GameConfig.FOUNTAIN_RADIUS;
    }

    private String buildRecallSuccessMessage(GameEntity hero) {
        String base = text.get(GameTextKey.RECALL_SUCCESS, hero.name);
        BuildPlan plan = getBlueBuildPlan();
        if (plan.nextPurchase != null) {
            return base + " · 推荐：" + plan.nextPurchase.label + "（" + plan.nextPurchaseReason + "）";
        }
        if (plan.consumableSuggestion != null && plan.consumableReason.length() > 0) {
            return base + " · " + plan.consumableReason;
        }
        return base;
    }

    private void recallHero(GameEntity hero) {
        hero.recallChannelTimer = 0f;
        hero.x = GameConfig.fountainX(hero.team);
        hero.y = GameConfig.fountainY(hero.team);
        if (hero.team == Team.BLUE) {
            blueMoveX = 0f;
            blueMoveY = 0f;
            blueRecallSnapCamera = true;
        }
        ring(hero.x, hero.y, 112f, Color.rgb(34, 197, 94), 0.6f);
        hero.hp = hero.maxHp;
        addText(hero.x, hero.y - 72f,
                text.get(GameTextKey.FLOATING_HEAL, Math.max(1, Math.round(hero.maxHp * 0.18f))),
                Color.rgb(134, 239, 172), 1.05f);
    }

    private void updateFountainEffect(GameEntity hero, float recovered, float dt) {
        if (hero.team == Team.BLUE) {
            blueFountainEffectTimer -= dt;
            if (blueFountainEffectTimer > 0f) {
                return;
            }
            blueFountainEffectTimer = GameConfig.FOUNTAIN_EFFECT_INTERVAL;
        } else {
            redFountainEffectTimer -= dt;
            if (redFountainEffectTimer > 0f) {
                return;
            }
            redFountainEffectTimer = GameConfig.FOUNTAIN_EFFECT_INTERVAL;
        }
        ring(hero.x, hero.y, 82f, Color.rgb(34, 197, 94), 0.38f);
        addText(hero.x, hero.y - 64f,
                text.get(GameTextKey.FLOATING_HEAL, Math.max(1, Math.round(recovered))),
                Color.rgb(134, 239, 172), 1.05f);
    }

    private void updateBlueBuffRegen(GameEntity hero, float dt) {
        if (hero.blueBuffTimer <= 0f || hero.hp >= hero.maxHp) {
            return;
        }
        healHero(hero, hero.maxHp * 0.006f * dt, false);
    }

    private void updateEnemyHero(GameEntity hero, float dt) {
        if (isInOwnFountain(hero) && hero.hp < hero.maxHp * aiProfile.fountainStayHpRate) {
            return;
        }
        if (shouldRedRetreat(hero)) {
            moveToward(hero, GameConfig.fountainX(Team.RED), GameConfig.fountainY(Team.RED),
                    dt, moveSpeed(hero) * aiProfile.retreatMoveScale);
            return;
        }

        GameEntity target = selectRedCombatTarget(hero);
        if (target == null) {
            if (aiProfile.wanderWhenIdle) {
                moveHero(hero, dt, -0.48f,
                        (float) Math.sin(System.currentTimeMillis() / 700.0) * 0.32f);
            } else {
                moveToward(hero, GameConfig.BLUE_OUTER_TOWER_X, GameConfig.LANE_Y,
                        dt, moveSpeed(hero) * aiProfile.pushSpeedScale);
            }
            return;
        }

        if (target.kind != UnitKind.HERO && hero.distanceTo(target) > aiProfile.lanePressureRange) {
            if (aiProfile.jungleFarmEnabled) {
                GameEntity jungle = nearestLivingMonster(hero);
                if (jungle != null && hero.distanceTo(jungle) < aiProfile.jungleFarmRange) {
                    float jungleDistance = hero.distanceTo(jungle);
                    if (jungleDistance > hero.attackRange * 0.85f) {
                        moveToward(hero, jungle.x, jungle.y, dt, moveSpeed(hero));
                    }
                    tryCastRedSkills(hero, jungle, jungleDistance);
                    return;
                }
            }
            moveToward(hero, GameConfig.BLUE_OUTER_TOWER_X, GameConfig.LANE_Y,
                    dt, moveSpeed(hero) * aiProfile.pushSpeedScale);
            target = selectRedCombatTarget(hero);
            if (target == null) {
                return;
            }
        }

        float distance = hero.distanceTo(target);
        if (aiProfile.kiteWhenRanged
                && hero.rangedBasicAttack
                && distance < hero.attackRange * 0.52f
                && target.kind == UnitKind.HERO) {
            float awayX = hero.x + (hero.x - target.x) * 1.4f;
            float awayY = hero.y + (hero.y - target.y) * 1.4f;
            moveToward(hero, awayX, awayY, dt, moveSpeed(hero) * 0.92f);
        } else if (distance > hero.attackRange * 0.82f) {
            moveToward(hero, target.x, target.y, dt, moveSpeed(hero));
        }
        tryCastRedSkills(hero, target, distance);
    }

    private boolean shouldRedRetreat(GameEntity hero) {
        return hero.hp < hero.maxHp * aiProfile.retreatHpRate && !isInOwnFountain(hero);
    }

    private void tryCastRedSkills(GameEntity hero, GameEntity target, float distance) {
        if (hero.ultimateTimer <= 0f
                && shouldUseUltimate(hero, target, distance)
                && hasSkillTarget(hero, SkillSlot.ULTIMATE)) {
            castSkill(hero, SkillSlot.ULTIMATE);
        } else if (hero.secondarySkillTimer <= 0f
                && shouldUseSecondary(hero, target, distance)
                && hasSkillTarget(hero, SkillSlot.SECONDARY)) {
            castSkill(hero, SkillSlot.SECONDARY);
        } else if (hero.skillTimer <= 0f
                && shouldCastPrimary(hero, target, distance)
                && hasSkillTarget(hero, SkillSlot.PRIMARY)) {
            castSkill(hero, SkillSlot.PRIMARY);
        }
    }

    private boolean shouldCastPrimary(GameEntity hero, GameEntity target, float distance) {
        float range = skillTargetRange(hero, SkillSlot.PRIMARY) * aiProfile.skillCastRangeMultiplier;
        if (distance > range) {
            return false;
        }
        if (target.kind == UnitKind.HERO) {
            return true;
        }
        return distance < range * (0.55f + aiProfile.skillAggression * 0.25f);
    }

    private GameEntity selectRedCombatTarget(GameEntity hero) {
        float range = aiProfile.combatVisionRange;
        if (blueHero != null && blueHero.alive && canTarget(hero, blueHero)) {
            float distance = hero.distanceTo(blueHero);
            if (distance <= aiProfile.heroFocusRange + blueHero.radius) {
                return blueHero;
            }
        }
        GameEntity tower = nearestEnemyTower(hero, range);
        if (tower != null) {
            return tower;
        }
        return nearestTarget(hero, range, false);
    }

    private GameEntity nearestEnemyTower(GameEntity hero, float range) {
        GameEntity best = null;
        float bestDistance = Float.MAX_VALUE;
        for (GameEntity entity : entities) {
            if (entity.kind != UnitKind.TOWER || !entity.alive || entity.team == hero.team) {
                continue;
            }
            float distance = hero.distanceTo(entity) - entity.radius;
            if (distance <= range && distance < bestDistance) {
                bestDistance = distance;
                best = entity;
            }
        }
        return best;
    }

    private GameEntity nearestLivingMonster(GameEntity hero) {
        GameEntity best = null;
        float bestDistance = Float.MAX_VALUE;
        for (GameEntity entity : entities) {
            if (entity.kind != UnitKind.MONSTER || !entity.alive) {
                continue;
            }
            float distance = hero.distanceTo(entity);
            if (distance < bestDistance) {
                bestDistance = distance;
                best = entity;
            }
        }
        return best;
    }

    private void updateMinion(GameEntity minion, float dt) {
        GameEntity target = nearestTarget(minion, minion.attackRange + 22f, true);
        if (target == null) {
            target = nearestTarget(minion, 340f, true);
        }
        if (target == null) {
            float dir = minion.team == Team.BLUE ? 1f : -1f;
            minion.x += dir * moveSpeed(minion) * dt;
            minion.y += (GameConfig.LANE_Y - minion.y) * Math.min(1f, dt * 2f);
        } else if (minion.distanceTo(target) > minion.attackRange * 0.86f) {
            moveToward(minion, target.x, target.y, dt, moveSpeed(minion));
        }
        if (target != null && minion.minionSkillTimer <= 0f && minion.distanceTo(target) < minion.attackRange + 35f) {
            castMinionSkill(minion, target);
        }
        clamp(minion);
    }

    private void updateMonster(GameEntity monster, float dt) {
        GameEntity target = nearestTarget(monster, 420f, false);
        if (target != null && monster.distanceTo(target) > monster.attackRange * 0.82f) {
            moveToward(monster, target.x, target.y, dt, moveSpeed(monster));
        }
        if (target != null && monster.minionSkillTimer <= 0f && monster.distanceTo(target) < monster.attackRange + 20f) {
            monster.minionSkillTimer = 4f;
            ring(monster.x, monster.y, 82f, Color.rgb(245, 158, 11), 0.28f);
            damageAround(monster, 86f, monster.attack * 1.25f);
            message = text.get(GameTextKey.MONSTER_SWEEP, monster.name);
        }
    }

    private void tryAttack(GameEntity attacker) {
        if (attacker.attackTimer > 0f || attacker.attack <= 0f || !attacker.alive) {
            return;
        }
        GameEntity target = selectAttackTarget(attacker);
        if (target == null) {
            return;
        }
        attacker.attackTimer = attacker.attackCooldown;
        boolean rangedHeroAttack = attacker.kind == UnitKind.HERO && attacker.rangedBasicAttack;
        if (rangedHeroAttack
                || attacker.kind == UnitKind.TOWER
                || attacker.kind == UnitKind.MINION
                || attacker.kind == UnitKind.RANGED_MINION) {
            DamageType damageType = attacker.kind == UnitKind.HERO
                    ? attacker.basicAttackDamageType
                    : DamageType.PHYSICAL;
            shoot(attacker, target, attacker.attack, damageType,
                    projectileSpeed(attacker), projectileRadius(attacker), 0f,
                    projectileColor(attacker, damageType), basicAttackVisual(attacker, damageType));
        } else {
            slash(attacker, target);
            dealDamage(attacker, target, attacker.attack, attacker.basicAttackDamageType);
        }
        emitAttackSound(attacker);
    }

    private void castSkill(GameEntity hero, SkillSlot skillSlot) {
        if (hero == null || !hero.alive) {
            return;
        }
        skillFocusTarget = findSkillTarget(hero, skillSlot);
        if (skillFocusTarget == null && !allowsSelfCastWithoutEnemy(hero, skillSlot)) {
            showNoSkillTarget(hero);
            return;
        }
        try {
            if (skillSlot == SkillSlot.PRIMARY) {
                if (hero.skillTimer > 0f) {
                    return;
                }
                hero.skillTimer = cooldownWithBuff(hero, hero.skillCooldown);
                castPrimarySkill(hero);
                emitSound(GameSoundEffect.SKILL);
            } else if (skillSlot == SkillSlot.SECONDARY) {
                if (hero.secondarySkillTimer > 0f) {
                    return;
                }
                hero.secondarySkillTimer = cooldownWithBuff(hero, hero.secondarySkillCooldown);
                castSecondarySkill(hero);
                emitSound(GameSoundEffect.SKILL);
            } else {
                if (hero.ultimateTimer > 0f) {
                    return;
                }
                hero.ultimateTimer = cooldownWithBuff(hero, hero.ultimateCooldown);
                castUltimateSkill(hero);
                emitSound(GameSoundEffect.ULTIMATE);
            }
            clamp(hero);
        } finally {
            skillFocusTarget = null;
        }
    }

    private float cooldownWithBuff(GameEntity hero, float cooldown) {
        return hero.blueBuffTimer > 0f ? cooldown * 0.82f : cooldown;
    }

    private boolean hasSkillTarget(GameEntity hero, SkillSlot skillSlot) {
        return findSkillTarget(hero, skillSlot) != null || allowsSelfCastWithoutEnemy(hero, skillSlot);
    }

    private boolean allowsSelfCastWithoutEnemy(GameEntity hero, SkillSlot skillSlot) {
        if (hero.heroType == null) {
            return false;
        }
        if (skillSlot == SkillSlot.PRIMARY && isSelfCastStyle(hero.heroType.skillStyle)) {
            return true;
        }
        if (skillSlot == SkillSlot.SECONDARY && isSelfCastStyle(hero.heroType.secondaryStyle)) {
            return true;
        }
        if (skillSlot == SkillSlot.ULTIMATE && isSelfCastStyle(hero.heroType.ultimateStyle)) {
            return true;
        }
        return false;
    }

    private boolean isSelfCastStyle(SkillStyle skillStyle) {
        return isSupportStyle(skillStyle) || skillStyle == SkillStyle.PHOENIX_FLAME;
    }

    private GameEntity findSkillTarget(GameEntity hero, SkillSlot skillSlot) {
        if (hero == null) {
            return null;
        }
        if (hero.heroType == null) {
            return selectSkillTarget(hero, 240f);
        }
        if (skillSlot == SkillSlot.SECONDARY) {
            if (hero.heroType == HeroType.SUN_WUKONG) {
                return redHero != null && redHero.alive && redHero.heroType != null ? redHero : null;
            }
            if (hero.heroType != null && hero.heroType.archetype() == HeroArchetype.ASSASSIN
                    && hero.heroType.secondaryStyle != SkillStyle.YIN_YANG_ORB) {
                return selectSkillTarget(hero, 360f);
            }
            return selectSkillTarget(hero, skillTargetRange(hero, skillSlot));
        }
        if (skillSlot == SkillSlot.ULTIMATE) {
            return selectSkillTarget(hero, skillTargetRange(hero, skillSlot));
        }
        return selectSkillTarget(hero, skillTargetRange(hero, skillSlot));
    }

    private float skillTargetRange(GameEntity hero, SkillSlot skillSlot) {
        if (hero == null || hero.heroType == null) {
            return 280f;
        }
        if (skillSlot == SkillSlot.ULTIMATE) {
            return ultimateTargetRange(hero.heroType.ultimateStyle);
        }
        SkillStyle style = skillSlot == SkillSlot.SECONDARY
                ? hero.heroType.secondaryStyle
                : hero.heroType.skillStyle;
        return skillTargetRange(style);
    }

    private float skillTargetRange(SkillStyle skillStyle) {
        switch (skillStyle) {
            case DASH_SWEEP:
                return 330f;
            case FIRE_WHEEL:
                return 370f;
            case EYE_BEAM:
            case GOD_LIST:
            case NETHER_WAVE:
                return 560f;
            case THUNDER_CHAIN:
                return 520f;
            case GIANT_STOMP:
                return 280f;
            case MOON_FROST:
                return 455f;
            case EARTH_SPLIT:
                return 430f;
            case BONE_TRAP:
                return 440f;
            case FIRE_CAGE:
                return 450f;
            case PAGODA_SEAL:
                return 500f;
            case HEALING_RAIN:
            case LOTUS_BARRIER:
                return 210f;
            case WIND_BLADE:
                return 460f;
            case YIN_YANG_ORB:
                return 440f;
            case PHOENIX_FLAME:
                return 225f;
            case SPIRIT_CHAIN:
                return 520f;
            case STARFALL:
                return 480f;
            default:
                return 320f;
        }
    }

    private float primaryEffectRange(GameEntity hero) {
        if (hero.heroType == null) {
            return 240f;
        }
        return skillTargetRange(hero.heroType.skillStyle);
    }

    private GameEntity selectSkillTarget(GameEntity from, float range) {
        GameEntity bestHero = null;
        float bestHeroDistance = Float.MAX_VALUE;
        GameEntity bestMinion = null;
        float bestMinionDistance = Float.MAX_VALUE;
        GameEntity bestMonster = null;
        float bestMonsterDistance = Float.MAX_VALUE;
        GameEntity bestTower = null;
        float bestTowerDistance = Float.MAX_VALUE;
        for (GameEntity entity : entities) {
            if (!canTarget(from, entity)) {
                continue;
            }
            float distance = from.distanceTo(entity) - entity.radius;
            if (distance > range) {
                continue;
            }
            if (entity.kind == UnitKind.HERO) {
                if (distance < bestHeroDistance) {
                    bestHeroDistance = distance;
                    bestHero = entity;
                }
            } else if (isLaneMinion(entity)) {
                if (distance < bestMinionDistance) {
                    bestMinionDistance = distance;
                    bestMinion = entity;
                }
            } else if (entity.kind == UnitKind.MONSTER) {
                if (distance < bestMonsterDistance) {
                    bestMonsterDistance = distance;
                    bestMonster = entity;
                }
            } else if (entity.kind == UnitKind.TOWER) {
                if (distance < bestTowerDistance) {
                    bestTowerDistance = distance;
                    bestTower = entity;
                }
            }
        }
        if (bestHero != null) {
            return bestHero;
        }
        if (bestMinion != null) {
            return bestMinion;
        }
        if (bestMonster != null) {
            return bestMonster;
        }
        return bestTower;
    }

    private float ultimateTargetRange(SkillStyle skillStyle) {
        switch (skillStyle) {
            case DASH_SWEEP:
            case FIRE_WHEEL:
            case WIND_BLADE:
            case YIN_YANG_ORB:
                return 520f;
            case EYE_BEAM:
            case THUNDER_CHAIN:
            case GOD_LIST:
            case PAGODA_SEAL:
            case NETHER_WAVE:
            case SPIRIT_CHAIN:
                return 650f;
            case GIANT_STOMP:
            case EARTH_SPLIT:
                return 520f;
            case MOON_FROST:
            case BONE_TRAP:
            case FIRE_CAGE:
            case STARFALL:
                return 560f;
            case HEALING_RAIN:
            case LOTUS_BARRIER:
            case PHOENIX_FLAME:
                return 330f;
            default:
                return 360f;
        }
    }

    private void showNoSkillTarget(GameEntity hero) {
        if (hero.team != Team.BLUE) {
            return;
        }
        message = text.get(GameTextKey.SKILL_NO_TARGET);
        addText(hero.x, hero.y - 72f, message, Color.rgb(226, 232, 240), 0.9f);
    }

    private void castPrimarySkill(GameEntity hero) {
        if (hero.heroType == null) {
            return;
        }
        executeSkillStyle(hero, hero.heroType.skillStyle, 1f, false);
        message = text.get(skillAnnounceKey(hero.heroType.skillStyle), hero.name);
        if (hero.heroType.skillStyle == SkillStyle.DASH_SWEEP) {
            addText(hero.x, hero.y - 76f, text.get(GameTextKey.CONTROL_STEALTH),
                    Color.rgb(187, 247, 208), 1.08f);
            if (GameTerrain.canLeapWall(hero.heroType)) {
                addText(hero.x, hero.y - 96f, text.get(GameTextKey.CONTROL_WALL_LEAP),
                        Color.rgb(250, 204, 21), 1.05f);
            }
        }
    }

    private void castSecondarySkill(GameEntity hero) {
        if (hero.heroType == null) {
            return;
        }
        if (hero.heroType == HeroType.SUN_WUKONG) {
            castWukongMimic(hero);
            return;
        }
        if (hero.heroType.archetype() == HeroArchetype.ASSASSIN
                && hero.heroType.secondaryStyle != SkillStyle.YIN_YANG_ORB) {
            castAssassinStealth(hero);
            return;
        }
        executeSkillStyle(hero, hero.heroType.secondaryStyle, 0.78f, false);
        message = text.get(skillAnnounceKey(hero.heroType.secondaryStyle), hero.name);
    }

    private void castWukongMimic(GameEntity hero) {
        GameEntity target = redHero;
        if (target == null || !target.alive || target.heroType == null) {
            showNoSkillTarget(hero);
            return;
        }
        hero.mimicHeroType = target.heroType;
        hero.mimicTimer = 8f;
        hero.stealthTimer = Math.max(hero.stealthTimer, 8f);
        applyEmpower(hero, 2.4f);
        ring(hero.x, hero.y, 190f, Color.rgb(250, 204, 21), 0.55f);
        addText(hero.x, hero.y - 88f, text.get(GameTextKey.MIMIC_ACTIVE, target.heroType.label),
                Color.rgb(250, 204, 21), 1.15f);
        addText(hero.x, hero.y - 68f, text.get(GameTextKey.CONTROL_STEALTH),
                Color.rgb(187, 247, 208), 1.05f);
        message = text.get(GameTextKey.SKILL_MIMIC, hero.name, target.heroType.label);
    }

    private void castAssassinStealth(GameEntity hero) {
        dashTowardFocus(hero, 150f);
        hero.stealthTimer = Math.max(hero.stealthTimer, 3.8f);
        applyEmpower(hero, 1.8f);
        damageAround(hero, 130f, hero.attack * 1.2f);
        hero.attackTimer = Math.min(hero.attackTimer, 0.15f);
        ring(hero.x, hero.y, 140f, Color.rgb(134, 239, 172), 0.42f);
        addText(hero.x, hero.y - 76f, text.get(GameTextKey.CONTROL_STEALTH),
                Color.rgb(187, 247, 208), 1.08f);
        if (GameTerrain.canLeapWall(hero.heroType)) {
            addText(hero.x, hero.y - 96f, text.get(GameTextKey.CONTROL_WALL_LEAP),
                    Color.rgb(250, 204, 21), 1.05f);
        }
        message = text.get(GameTextKey.SKILL_STEALTH, hero.name);
    }

    private void castTalentSkill(GameEntity hero) {
        if (hero.heroType == null) {
            return;
        }
        skillFocusTarget = hero.archetype == HeroArchetype.MARKSMAN
                ? selectSkillTarget(hero, hero.attackRange + 180f)
                : selectSkillTarget(hero, talentTargetRange(hero));
        try {
            hero.talentTimer = hero.talentCooldown;
            DamageType talentType = HeroTalentCalculator.talentDamageType(hero.heroType);
            float burst = hero.attack * (1.15f + hero.talentDamageBonusRate * 2.4f);
            switch (hero.archetype) {
                case MAGE:
                case SUPPORT:
                    damageAtFocusOrSelf(hero, 190f, burst * 1.12f, DamageType.MAGIC,
                            Color.rgb(192, 132, 252), 0.48f);
                    break;
                case ASSASSIN:
                    hero.stealthTimer = Math.max(hero.stealthTimer, 2.2f);
                    dashTowardFocus(hero, 120f);
                    damageAround(hero, 135f, burst * 1.18f, talentType);
                    ring(hero.x, hero.y, 145f, Color.rgb(134, 239, 172), 0.42f);
                    break;
                case TANK:
                    applyShield(hero, 2.4f, hero.maxHp * 0.14f);
                    damageAround(hero, 155f, burst * 0.95f, DamageType.PHYSICAL);
                    stunAround(hero, 130f, 0.35f);
                    ring(hero.x, hero.y, 165f, Color.rgb(245, 158, 11), 0.42f);
                    break;
                case MARKSMAN:
                    if (skillFocusTarget != null) {
                        int talentColor = HeroProjectileProfile.heroColor(hero.heroType, talentType);
                        ProjectileVisual talentVisual = HeroProjectileProfile.basicAttackVisual(
                                hero.heroType, talentType, true);
                        shoot(hero, skillFocusTarget, burst * 1.35f, talentType, 780f, 9f, 68f,
                                talentColor, talentVisual);
                        line(hero.x, hero.y, skillFocusTarget.x, skillFocusTarget.y, talentColor);
                    }
                    break;
                case FIGHTER:
                default:
                    damageAround(hero, 150f, burst * 1.08f, DamageType.PHYSICAL);
                    healHero(hero, hero.maxHp * 0.08f, true);
                    ring(hero.x, hero.y, 160f, Color.rgb(96, 165, 250), 0.38f);
                    break;
            }
            addText(hero.x, hero.y - 84f,
                    text.get(GameTextKey.TALENT_BURST, HeroSkillNameResolver.talentName(hero.heroType)),
                    Color.rgb(250, 204, 21), 1.12f);
            emitSound(GameSoundEffect.SKILL);
            message = text.get(GameTextKey.SKILL_TALENT, hero.name, HeroSkillNameResolver.talentName(hero.heroType));
        } finally {
            skillFocusTarget = null;
        }
    }

    private float talentTargetRange(GameEntity hero) {
        if (hero == null || hero.archetype == null) {
            return 320f;
        }
        switch (hero.archetype) {
            case MARKSMAN:
                return hero.attackRange + 160f;
            case MAGE:
            case SUPPORT:
                return 520f;
            case ASSASSIN:
            case FIGHTER:
                return 360f;
            case TANK:
                return 280f;
            default:
                return 320f;
        }
    }

    private void updateBlueBrushFeedback(GameEntity hero) {
        boolean inBrush = GameTerrain.isInBrush(hero.x, hero.y);
        if (inBrush && !blueWasInBrush) {
            message = text.get(GameTextKey.TERRAIN_BRUSH_ENTER);
            addText(hero.x, hero.y - 82f, text.get(GameTextKey.TERRAIN_BRUSH_HINT),
                    Color.rgb(134, 239, 172), 1.1f);
            ring(hero.x, hero.y, 120f, Color.rgb(74, 222, 128), 0.35f);
        }
        blueWasInBrush = inBrush;
    }

    private boolean updateBlueRecall(GameEntity hero, float dt) {
        if (hero.recallChannelTimer <= 0f) {
            return false;
        }
        if (GameMath.vectorLength(blueMoveX, blueMoveY) > 0.32f) {
            cancelRecall(hero, GameTextKey.RECALL_CANCEL_MOVE);
            return false;
        }
        hero.recallChannelTimer = Math.max(0f, hero.recallChannelTimer - dt);
        if (hero.recallChannelTimer <= 0f) {
            recallHero(hero);
            message = buildRecallSuccessMessage(hero);
            return true;
        }
        return false;
    }

    private void cancelRecall(GameEntity hero, GameTextKey reasonKey) {
        if (hero == null || hero.recallChannelTimer <= 0f) {
            return;
        }
        hero.recallChannelTimer = 0f;
        message = text.get(reasonKey, hero.name);
        ring(hero.x, hero.y, 92f, Color.rgb(239, 68, 68), 0.42f);
    }

    private void castUltimateSkill(GameEntity hero) {
        if (hero.heroType == null) {
            return;
        }
        executeSkillStyle(hero, hero.heroType.ultimateStyle, 1.72f, true);
        message = text.get(skillAnnounceKey(hero.heroType.ultimateStyle), hero.name);
    }

    private GameTextKey skillAnnounceKey(SkillStyle style) {
        switch (style) {
            case DASH_SWEEP:
                return GameTextKey.SKILL_DASH_SWEEP;
            case EYE_BEAM:
                return GameTextKey.SKILL_EYE_BEAM;
            case FIRE_WHEEL:
                return GameTextKey.SKILL_FIRE_WHEEL;
            case THUNDER_CHAIN:
                return GameTextKey.SKILL_THUNDER_CHAIN;
            case GIANT_STOMP:
                return GameTextKey.SKILL_GIANT_STOMP;
            case MOON_FROST:
                return GameTextKey.SKILL_MOON_FROST;
            case EARTH_SPLIT:
                return GameTextKey.SKILL_EARTH_SPLIT;
            case BONE_TRAP:
                return GameTextKey.SKILL_BONE_TRAP;
            case GOD_LIST:
                return GameTextKey.SKILL_GOD_LIST;
            case FIRE_CAGE:
                return GameTextKey.SKILL_FIRE_CAGE;
            case PAGODA_SEAL:
                return GameTextKey.SKILL_PAGODA_SEAL;
            case HEALING_RAIN:
                return GameTextKey.SKILL_HEALING_RAIN;
            case WIND_BLADE:
                return GameTextKey.SKILL_WIND_BLADE;
            case YIN_YANG_ORB:
                return GameTextKey.SKILL_YIN_YANG_ORB;
            case PHOENIX_FLAME:
                return GameTextKey.SKILL_PHOENIX_FLAME;
            case SPIRIT_CHAIN:
                return GameTextKey.SKILL_SPIRIT_CHAIN;
            case STARFALL:
                return GameTextKey.SKILL_STARFALL;
            case NETHER_WAVE:
                return GameTextKey.SKILL_NETHER_WAVE;
            case LOTUS_BARRIER:
                return GameTextKey.SKILL_LOTUS_BARRIER;
            default:
                return GameTextKey.SKILL_SECONDARY;
        }
    }

    private void executeSkillStyle(GameEntity hero, SkillStyle style, float power, boolean ultimate) {
        if (hero == null || style == null) {
            return;
        }
        float atk = hero.attack * power;
        switch (style) {
            case DASH_SWEEP:
                dashTowardFocus(hero, ultimate ? 280f : 150f);
                hero.stealthTimer = Math.max(hero.stealthTimer, ultimate ? 2.2f : 1.25f);
                applyEmpower(hero, ultimate ? 3.2f : 2.2f);
                damageAround(hero, ultimate ? 255f : 170f, atk * (ultimate ? 2.65f : 1.8f));
                if (ultimate) {
                    stunAround(hero, 210f, 0.42f);
                }
                ring(hero.x, hero.y, ultimate ? 270f : 170f, Color.rgb(250, 204, 21), ultimate ? 0.72f : 0.35f);
                break;
            case EYE_BEAM:
                beam(hero, ultimate ? 840f : 520f, atk * (ultimate ? 2.55f : 1.65f), DamageType.MAGIC, Color.rgb(56, 189, 248));
                if (ultimate) {
                    chainLightning(hero);
                }
                break;
            case FIRE_WHEEL:
                dashTowardFocus(hero, ultimate ? 280f : 210f);
                hero.speed += ultimate ? 14f : 8f;
                applyEmpower(hero, ultimate ? 2.8f : 1.8f);
                if (hero.heroType != null && hero.heroType.archetype() == HeroArchetype.ASSASSIN) {
                    hero.stealthTimer = Math.max(hero.stealthTimer, ultimate ? 3.2f : 2.4f);
                }
                damageAround(hero, ultimate ? 255f : 150f, atk * (ultimate ? 2.65f : 1.5f));
                if (ultimate) {
                    stunAround(hero, 210f, 0.42f);
                }
                ring(hero.x, hero.y, ultimate ? 270f : 155f, Color.rgb(249, 115, 22), ultimate ? 0.72f : 0.38f);
                break;
            case THUNDER_CHAIN:
                chainLightning(hero);
                if (ultimate) {
                    beam(hero, 840f, atk * 2.55f, DamageType.MAGIC, Color.rgb(56, 189, 248));
                    ring(hero.x, hero.y, 210f, Color.rgb(125, 211, 252), 0.55f);
                }
                break;
            case GIANT_STOMP:
                damageAround(hero, ultimate ? 300f : 185f, atk * (ultimate ? 2.35f : 1.6f));
                stunAround(hero, ultimate ? 260f : 160f, ultimate ? 0.72f : 0.55f);
                if (ultimate) {
                    beam(hero, 430f, atk * 1.4f, Color.rgb(180, 83, 9));
                }
                ring(hero.x, hero.y, ultimate ? 310f : 190f, Color.rgb(245, 158, 11), ultimate ? 0.75f : 0.42f);
                break;
            case MOON_FROST:
                damageAtFocusOrSelf(hero, ultimate ? 280f : 190f,
                        atk * (ultimate ? 2.05f : 1.25f), DamageType.MAGIC,
                        Color.rgb(165, 243, 252), ultimate ? 0.8f : 0.5f);
                slowAtFocusOrSelf(hero, ultimate ? 300f : 210f, ultimate ? 3.2f : 2.4f);
                if (ultimate) {
                    stunAtFocusOrSelf(hero, 220f, 0.55f);
                }
                break;
            case EARTH_SPLIT:
                beam(hero, ultimate ? 430f : 330f, atk * (ultimate ? 2.2f : 1.7f), Color.rgb(180, 83, 9));
                damageAround(hero, ultimate ? 300f : 135f, atk * (ultimate ? 2.35f : 1.15f));
                if (ultimate) {
                    stunAround(hero, 260f, 0.72f);
                }
                break;
            case BONE_TRAP:
                damageAtFocusOrSelf(hero, ultimate ? 270f : 180f,
                        atk * (ultimate ? 2.05f : 1.35f), DamageType.MAGIC,
                        Color.rgb(226, 232, 240), ultimate ? 0.8f : 0.55f);
                stunAtFocusOrSelf(hero, ultimate ? 230f : 175f, ultimate ? 0.55f : 0.7f);
                if (ultimate) {
                    slowAtFocusOrSelf(hero, 310f, 3.2f);
                }
                break;
            case GOD_LIST:
                if (skillFocusTarget != null) {
                    int skillColor = HeroProjectileProfile.skillColor(hero.heroType, SkillStyle.GOD_LIST);
                    shoot(hero, skillFocusTarget, atk * (ultimate ? 2.8f : 2.15f), DamageType.MAGIC, 700f, 13f, 90f,
                            skillColor, HeroProjectileProfile.skillVisual(hero.heroType, SkillStyle.GOD_LIST));
                    line(hero.x, hero.y, skillFocusTarget.x, skillFocusTarget.y, skillColor);
                }
                if (ultimate) {
                    chainLightning(hero);
                }
                break;
            case PAGODA_SEAL:
                if (skillFocusTarget != null) {
                    skillFocusTarget.stunTimer = Math.max(skillFocusTarget.stunTimer, ultimate ? 1.2f : 0.85f);
                    dealDamage(hero, skillFocusTarget, atk * (ultimate ? 2.4f : 1.7f), DamageType.MAGIC);
                    ring(skillFocusTarget.x, skillFocusTarget.y, ultimate ? 140f : 100f, Color.rgb(250, 204, 21), 0.42f);
                }
                if (ultimate) {
                    beam(hero, 840f, atk * 2.55f, DamageType.MAGIC, Color.rgb(56, 189, 248));
                }
                break;
            case FIRE_CAGE:
                damageAtFocusOrSelf(hero, ultimate ? 285f : 205f,
                        atk * (ultimate ? 2.25f : 1.45f), DamageType.MAGIC,
                        Color.rgb(239, 68, 68), ultimate ? 0.88f : 0.65f);
                if (ultimate) {
                    slowAtFocusOrSelf(hero, 300f, 2.2f);
                }
                break;
            case HEALING_RAIN:
                hero.hp = Math.min(hero.maxHp, hero.hp + hero.maxHp * (ultimate ? 0.42f : 0.28f));
                applyShield(hero, ultimate ? 3.8f : 2.5f, hero.maxHp * (ultimate ? 0.24f : 0.16f));
                damageAround(hero, ultimate ? 280f : 150f, atk * (ultimate ? 1.35f : 0.85f), ultimate ? DamageType.TRUE_DAMAGE : DamageType.MAGIC);
                ring(hero.x, hero.y, ultimate ? 330f : 210f, Color.rgb(34, 197, 94), ultimate ? 0.82f : 0.6f);
                healBurst(hero.x, hero.y - 24f, 72f);
                break;
            case WIND_BLADE:
                if (skillFocusTarget != null) {
                    int skillColor = HeroProjectileProfile.skillColor(hero.heroType, SkillStyle.WIND_BLADE);
                    shoot(hero, skillFocusTarget, atk * 1.2f, DamageType.MAGIC, 680f, 7f, 48f,
                            skillColor, HeroProjectileProfile.skillVisual(hero.heroType, SkillStyle.WIND_BLADE));
                    damageAtFocusOrSelf(hero, ultimate ? 210f : 150f, atk * (ultimate ? 1.6f : 1.05f),
                            DamageType.MAGIC, skillColor, 0.45f);
                    slowAtFocusOrSelf(hero, ultimate ? 240f : 170f, ultimate ? 2.8f : 1.8f);
                } else {
                    damageAround(hero, ultimate ? 280f : 200f, atk * (ultimate ? 2.1f : 1.4f));
                    slowAround(hero, ultimate ? 260f : 190f, ultimate ? 2.8f : 1.8f);
                    ring(hero.x, hero.y, ultimate ? 300f : 205f,
                            HeroProjectileProfile.skillColor(hero.heroType, SkillStyle.WIND_BLADE), 0.45f);
                }
                break;
            case YIN_YANG_ORB:
                if (skillFocusTarget != null) {
                    dealDamage(hero, skillFocusTarget, atk * (ultimate ? 2.3f : 1.55f), DamageType.MAGIC);
                    skillFocusTarget.stunTimer = Math.max(skillFocusTarget.stunTimer, ultimate ? 0.9f : 0.55f);
                    skillFocusTarget.slowTimer = Math.max(skillFocusTarget.slowTimer, ultimate ? 2.4f : 1.6f);
                    ring(skillFocusTarget.x, skillFocusTarget.y, ultimate ? 150f : 110f, Color.rgb(216, 180, 254), 0.5f);
                } else {
                    damageAround(hero, ultimate ? 240f : 170f, atk * 1.3f, DamageType.MAGIC);
                }
                if (!ultimate) {
                    dashTowardFocus(hero, 120f);
                }
                break;
            case PHOENIX_FLAME:
                damageAround(hero, ultimate ? 320f : 215f, atk * (ultimate ? 2.2f : 1.5f), DamageType.MAGIC);
                healHero(hero, hero.maxHp * (ultimate ? 0.22f : 0.14f), true);
                ring(hero.x, hero.y, ultimate ? 340f : 225f, Color.rgb(249, 115, 22), ultimate ? 0.82f : 0.58f);
                break;
            case SPIRIT_CHAIN:
                chainLightning(hero);
                if (skillFocusTarget != null) {
                    line(hero.x, hero.y, skillFocusTarget.x, skillFocusTarget.y, Color.rgb(192, 132, 252));
                    dealDamage(hero, skillFocusTarget, atk * (ultimate ? 2.0f : 1.35f), DamageType.MAGIC);
                }
                break;
            case STARFALL:
                for (int i = 0; i < (ultimate ? 5 : 3); i++) {
                    float radius = (ultimate ? 240f : 165f) - i * 18f;
                    damageAtFocusOrSelf(hero, radius, atk * (ultimate ? 0.95f : 0.72f),
                            DamageType.MAGIC, Color.rgb(250, 204, 21), 0.28f);
                }
                if (ultimate) {
                    stunAtFocusOrSelf(hero, 210f, 0.38f);
                }
                break;
            case NETHER_WAVE:
                beam(hero, ultimate ? 760f : 520f, atk * (ultimate ? 2.4f : 1.6f), DamageType.MAGIC, Color.rgb(99, 102, 241));
                damageAround(hero, ultimate ? 260f : 175f, atk * 1.1f, DamageType.MAGIC);
                healHero(hero, atk * (ultimate ? 0.35f : 0.2f), ultimate);
                break;
            case LOTUS_BARRIER:
                applyShield(hero, ultimate ? 4.2f : 2.6f, hero.maxHp * (ultimate ? 0.26f : 0.16f));
                damageAround(hero, ultimate ? 260f : 165f, atk * (ultimate ? 1.45f : 0.9f), DamageType.MAGIC);
                if (ultimate) {
                    healHero(hero, hero.maxHp * 0.18f, true);
                }
                ring(hero.x, hero.y, ultimate ? 300f : 200f, Color.rgb(244, 114, 182), ultimate ? 0.75f : 0.5f);
                break;
            default:
                damageAround(hero, 150f, atk, DamageType.MAGIC);
                break;
        }
    }

    private boolean shouldUseSecondary(GameEntity hero, GameEntity target, float distance) {
        float rangeScale = aiProfile.skillCastRangeMultiplier;
        if (target.kind == UnitKind.HERO) {
            return distance < Math.max(hero.attackRange + 160f, 420f) * rangeScale * aiProfile.skillAggression;
        }
        if (hero.hp < hero.maxHp * 0.5f) {
            return aiProfile.skillAggression >= 0.85f;
        }
        return distance < Math.max(hero.attackRange + 120f, 360f) * rangeScale * aiProfile.skillAggression;
    }

    private boolean shouldUseUltimate(GameEntity hero, GameEntity target, float distance) {
        float rangeScale = aiProfile.skillCastRangeMultiplier * aiProfile.skillAggression;
        if (target.kind == UnitKind.HERO
                && distance < Math.max(hero.attackRange + 180f, 480f) * rangeScale) {
            return true;
        }
        return hero.hp < hero.maxHp * (0.42f - aiProfile.skillAggression * 0.08f)
                || countTargetsAround(hero, 660f) >= 2
                || distance < Math.max(hero.attackRange + 160f, 480f) * rangeScale
                && target.kind == UnitKind.HERO;
    }

    private boolean isSupportStyle(SkillStyle skillStyle) {
        return skillStyle == SkillStyle.HEALING_RAIN || skillStyle == SkillStyle.LOTUS_BARRIER;
    }

    private boolean isTankStyle(SkillStyle skillStyle) {
        return skillStyle == SkillStyle.GIANT_STOMP
                || skillStyle == SkillStyle.EARTH_SPLIT
                || skillStyle == SkillStyle.LOTUS_BARRIER;
    }

    private boolean isCasterStyle(SkillStyle skillStyle) {
        return skillStyle == SkillStyle.EYE_BEAM
                || skillStyle == SkillStyle.THUNDER_CHAIN
                || skillStyle == SkillStyle.MOON_FROST
                || skillStyle == SkillStyle.BONE_TRAP
                || skillStyle == SkillStyle.GOD_LIST
                || skillStyle == SkillStyle.FIRE_CAGE
                || skillStyle == SkillStyle.PAGODA_SEAL
                || skillStyle == SkillStyle.YIN_YANG_ORB
                || skillStyle == SkillStyle.NETHER_WAVE
                || skillStyle == SkillStyle.SPIRIT_CHAIN
                || skillStyle == SkillStyle.STARFALL;
    }

    private void applyShield(GameEntity hero, float seconds, float barrierAmount) {
        hero.shieldTimer = Math.max(hero.shieldTimer, seconds);
        hero.barrierHp = Math.max(hero.barrierHp, barrierAmount);
        ring(hero.x, hero.y, hero.radius + 72f, Color.rgb(34, 197, 94), 0.52f);
        addText(hero.x, hero.y - 78f,
                text.get(GameTextKey.FLOATING_SHIELD, Math.max(1, Math.round(barrierAmount))),
                Color.rgb(134, 239, 172), 1.12f);
        addText(hero.x, hero.y - 56f, text.get(GameTextKey.CONTROL_SHIELD),
                Color.rgb(187, 247, 208), 0.94f);
    }

    private void applyBlock(GameEntity hero, float seconds) {
        hero.blockTimer = Math.max(hero.blockTimer, seconds);
        ring(hero.x, hero.y, hero.radius + 56f, Color.rgb(148, 163, 184), 0.45f);
        addText(hero.x, hero.y - 100f, text.get(GameTextKey.CONTROL_BLOCK),
                Color.rgb(226, 232, 240), 1.02f);
    }

    private void applyEmpower(GameEntity hero, float seconds) {
        hero.damageBoostTimer = Math.max(hero.damageBoostTimer, seconds);
        ring(hero.x, hero.y, hero.radius + 50f, Color.rgb(250, 204, 21), 0.35f);
        addText(hero.x, hero.y - 98f, text.get(GameTextKey.CONTROL_EMPOWER),
                Color.rgb(250, 204, 21), 1.04f);
    }

    private void castMinionSkill(GameEntity minion, GameEntity target) {
        minion.minionSkillTimer = minion.kind == UnitKind.BRUTE
                ? 4.2f
                : minion.kind == UnitKind.RANGED_MINION ? 4.8f : 5.5f;
        if (minion.kind == UnitKind.BRUTE) {
            ring(minion.x, minion.y, 92f, Color.rgb(245, 158, 11), 0.28f);
            damageAround(minion, 95f, minion.attack * 1.25f);
            message = text.get(GameTextKey.BRUTE_SMASH, minion.name);
        } else {
            float damageScale = minion.kind == UnitKind.RANGED_MINION ? 1.18f : 1.35f;
            float speed = minion.kind == UnitKind.RANGED_MINION ? 560f : 470f;
            shoot(minion, target, minion.attack * damageScale, speed, 6f, 0f,
                    minion.team == Team.BLUE ? Color.rgb(147, 197, 253) : Color.rgb(252, 165, 165),
                    ProjectileVisual.TALISMAN);
            message = text.get(GameTextKey.TALISMAN_MISSILE, minion.name);
        }
    }

    private void updateProjectiles(float dt) {
        Iterator<Projectile> iterator = projectiles.iterator();
        while (iterator.hasNext()) {
            Projectile projectile = iterator.next();
            GameEntity target = findById(projectile.targetId);
            if (target == null || !target.alive) {
                iterator.remove();
                continue;
            }
            float dx = target.x - projectile.x;
            float dy = target.y - projectile.y;
            float len = Math.max(1f, GameMath.vectorLength(dx, dy));
            projectile.angleRad = (float) Math.atan2(dy, dx);
            projectile.x += dx / len * projectile.speed * dt;
            projectile.y += dy / len * projectile.speed * dt;
            if (len <= target.radius + projectile.radius + 8f) {
                GameEntity owner = findById(projectile.ownerId);
                if (projectile.splashRadius > 0f) {
                    ring(target.x, target.y, projectile.splashRadius, projectile.color, 0.35f);
                    damageAt(owner, projectile.team, target.x, target.y, projectile.splashRadius,
                            projectile.damage, projectile.damageType);
                } else {
                    dealDamage(owner, target, projectile.damage, projectile.damageType, projectile.team);
                }
                iterator.remove();
            }
        }
    }

    private void updateEffects(float dt) {
        Iterator<VisualEffect> iterator = effects.iterator();
        while (iterator.hasNext()) {
            VisualEffect effect = iterator.next();
            effect.ttl -= dt;
            if (effect.kind == EffectKind.TEXT) {
                effect.y -= 42f * dt;
            }
            if (effect.ttl <= 0f) {
                iterator.remove();
            }
        }
    }

    private void damageAround(GameEntity attacker, float radius, float damage) {
        damageAround(attacker, radius, damage, DamageType.PHYSICAL);
    }

    private void damageAround(GameEntity attacker, float radius, float damage, DamageType damageType) {
        for (GameEntity entity : entities) {
            if (canTarget(attacker, entity) && attacker.distanceTo(entity) <= radius + entity.radius) {
                dealDamage(attacker, entity, damage, damageType);
            }
        }
    }

    private void damageAt(GameEntity owner, Team team, float x, float y, float radius, float damage) {
        damageAt(owner, team, x, y, radius, damage, DamageType.PHYSICAL);
    }

    private void damageAt(GameEntity owner, Team team, float x, float y, float radius, float damage, DamageType damageType) {
        for (GameEntity entity : entities) {
            if (entity.alive && canTarget(team, entity) && GameMath.distance(x, y, entity.x, entity.y) <= radius + entity.radius) {
                dealDamage(owner, entity, damage, damageType, team);
            }
        }
    }

    private void damageAtFocusOrSelf(GameEntity attacker, float radius, float damage,
                                     DamageType damageType, int color, float alpha) {
        if (skillFocusTarget != null && skillFocusTarget.alive) {
            ring(skillFocusTarget.x, skillFocusTarget.y, radius, color, alpha);
            damageAt(attacker, attacker.team, skillFocusTarget.x, skillFocusTarget.y,
                    radius, damage, damageType);
            return;
        }
        ring(attacker.x, attacker.y, radius, color, alpha);
        damageAround(attacker, radius, damage, damageType);
    }

    private void slowAround(GameEntity attacker, float radius, float seconds) {
        for (GameEntity entity : entities) {
            if (canTarget(attacker, entity) && attacker.distanceTo(entity) <= radius + entity.radius) {
                entity.slowTimer = Math.max(entity.slowTimer, seconds);
                statusBurst(entity.x, entity.y, entity.radius + 24f, Color.rgb(165, 243, 252), 0.86f);
            }
        }
    }

    private void slowAtFocusOrSelf(GameEntity attacker, float radius, float seconds) {
        if (skillFocusTarget == null || !skillFocusTarget.alive) {
            slowAround(attacker, radius, seconds);
            return;
        }
        for (GameEntity entity : entities) {
            if (canTarget(attacker, entity)
                    && GameMath.distance(skillFocusTarget.x, skillFocusTarget.y, entity.x, entity.y)
                    <= radius + entity.radius) {
                entity.slowTimer = Math.max(entity.slowTimer, seconds);
                statusBurst(entity.x, entity.y, entity.radius + 24f, Color.rgb(165, 243, 252), 0.86f);
            }
        }
    }

    private void stunAround(GameEntity attacker, float radius, float seconds) {
        for (GameEntity entity : entities) {
            if (canTarget(attacker, entity) && attacker.distanceTo(entity) <= radius + entity.radius) {
                entity.stunTimer = Math.max(entity.stunTimer, seconds);
                statusBurst(entity.x, entity.y, entity.radius + 24f, Color.rgb(250, 204, 21), 0.95f);
            }
        }
    }

    private void stunAtFocusOrSelf(GameEntity attacker, float radius, float seconds) {
        if (skillFocusTarget == null || !skillFocusTarget.alive) {
            stunAround(attacker, radius, seconds);
            return;
        }
        for (GameEntity entity : entities) {
            if (canTarget(attacker, entity)
                    && GameMath.distance(skillFocusTarget.x, skillFocusTarget.y, entity.x, entity.y)
                    <= radius + entity.radius) {
                entity.stunTimer = Math.max(entity.stunTimer, seconds);
                statusBurst(entity.x, entity.y, entity.radius + 24f, Color.rgb(250, 204, 21), 0.95f);
            }
        }
    }

    private void chainLightning(GameEntity hero) {
        float range = 560f;
        int hits = 0;
        for (GameEntity entity : new ArrayList<>(entities)) {
            if (hits >= 4) {
                return;
            }
            if (canTarget(hero, entity) && hero.distanceTo(entity) <= range) {
                dealDamage(hero, entity, hero.attack * (1.45f - hits * 0.12f), DamageType.MAGIC);
                line(hero.x, hero.y, entity.x, entity.y, Color.rgb(125, 211, 252));
                hits++;
            }
        }
    }

    private void beam(GameEntity hero, float length, float damage, int color) {
        beam(hero, length, damage, DamageType.PHYSICAL, color);
    }

    private void beam(GameEntity hero, float length, float damage, DamageType damageType, int color) {
        if (skillFocusTarget != null) {
            beamToward(hero, skillFocusTarget, length, damage, damageType, color);
            return;
        }
        float dir = hero.team == Team.BLUE ? 1f : -1f;
        float endX = hero.x + dir * length;
        line(hero.x, hero.y, endX, hero.y, color);
        for (GameEntity entity : entities) {
            if (!canTarget(hero, entity)) {
                continue;
            }
            boolean inX = dir > 0 ? entity.x >= hero.x && entity.x <= endX : entity.x <= hero.x && entity.x >= endX;
            if (inX && Math.abs(entity.y - hero.y) <= 95f + entity.radius) {
                dealDamage(hero, entity, damage, damageType);
            }
        }
    }

    private void beamToward(GameEntity hero, GameEntity target, float maxLength, float damage,
                            DamageType damageType, int color) {
        float dx = target.x - hero.x;
        float dy = target.y - hero.y;
        float distance = GameMath.distance(0f, 0f, dx, dy);
        if (distance < 1f) {
            return;
        }
        float length = Math.min(maxLength, distance + target.radius + 40f);
        float endX = hero.x + dx / distance * length;
        float endY = hero.y + dy / distance * length;
        line(hero.x, hero.y, endX, endY, color);
        float hitWidth = 95f;
        for (GameEntity entity : entities) {
            if (!canTarget(hero, entity)) {
                continue;
            }
            if (GameMath.distancePointToSegment(entity.x, entity.y, hero.x, hero.y, endX, endY)
                    <= hitWidth + entity.radius) {
                dealDamage(hero, entity, damage, damageType);
            }
        }
    }

    private void dashTowardFocus(GameEntity hero, float distance) {
        if (skillFocusTarget != null) {
            GameTerrain.applyDashToward(hero, skillFocusTarget, distance);
        } else {
            GameTerrain.applyDash(hero, distance);
        }
    }

    private void shoot(GameEntity attacker, GameEntity target, float damage, float speed, float radius,
                       float splashRadius, int color, ProjectileVisual visual) {
        shoot(attacker, target, damage, DamageType.PHYSICAL, speed, radius, splashRadius, color, visual);
    }

    private void shoot(GameEntity attacker, GameEntity target, float damage, DamageType damageType, float speed,
                       float radius, float splashRadius, int color, ProjectileVisual visual) {
        float dx = target.x - attacker.x;
        float dy = target.y - attacker.y;
        Projectile projectile = new Projectile(attacker.team, attacker.id, target.id, attacker.x, attacker.y, speed, damage,
                damageType, radius, splashRadius, color, visual);
        projectile.angleRad = (float) Math.atan2(dy, dx);
        projectiles.add(projectile);
    }

    private ProjectileVisual basicAttackVisual(GameEntity attacker, DamageType damageType) {
        return HeroProjectileProfile.basicAttackVisual(attacker, damageType);
    }

    private int projectileColor(GameEntity attacker, DamageType damageType) {
        return HeroProjectileProfile.projectileColor(attacker, damageType);
    }

    private float projectileSpeed(GameEntity attacker) {
        if (attacker.kind == UnitKind.TOWER) {
            return attacker.team == Team.BLUE ? 760f : 620f;
        }
        if (attacker.kind == UnitKind.HERO) {
            return attacker.rangedBasicAttack ? 720f : 520f;
        }
        if (attacker.kind == UnitKind.RANGED_MINION) {
            return 560f;
        }
        return 430f;
    }

    private float projectileRadius(GameEntity attacker) {
        if (attacker.kind == UnitKind.TOWER) {
            return 10f;
        }
        if (attacker.kind == UnitKind.HERO) {
            return attacker.rangedBasicAttack ? 8.5f : 7f;
        }
        return 6f;
    }

    private void slash(GameEntity attacker, GameEntity target) {
        float dx = target.x - attacker.x;
        float dy = target.y - attacker.y;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);
        if (dist < 1f) {
            dx = (float) Math.cos(attacker.facingRad);
            dy = (float) Math.sin(attacker.facingRad);
            dist = 48f;
        }
        VisualEffect effect = new VisualEffect(EffectKind.SLASH_ARC,
                attacker.x + dx * 0.55f, attacker.y + dy * 0.55f,
                Math.max(24f, dist * 0.45f), 0.24f,
                TeamStyle.color(attacker.team), "");
        effect.angleRad = (float) Math.atan2(dy, dx);
        effects.add(effect);
        trimEffects();
    }

    private void dealDamage(GameEntity attacker, GameEntity target, float damage) {
        dealDamage(attacker, target, damage, DamageType.PHYSICAL);
    }

    private void dealDamage(GameEntity attacker, GameEntity target, float damage, DamageType damageType) {
        dealDamage(attacker, target, damage, damageType, attacker == null ? Team.NEUTRAL : attacker.team);
    }

    private void dealDamage(GameEntity attacker, GameEntity target, float damage, Team damageTeam) {
        dealDamage(attacker, target, damage, DamageType.PHYSICAL, damageTeam);
    }

    private void dealDamage(GameEntity attacker, GameEntity target, float damage, DamageType damageType, Team damageTeam) {
        if (target == null || !target.alive) {
            return;
        }
        boolean wasAlive = target.alive;
        if (target.kind == UnitKind.HERO && target.recallChannelTimer > 0f) {
            cancelRecall(target, GameTextKey.RECALL_INTERRUPTED);
        }
        float finalDamage = CombatCalculator.calculateDamage(attacker, target, damage, damageType, damageTeam);
        finalDamage = applySnowballExecute(attacker, target, finalDamage);
        float applied = target.damage(finalDamage);
        if (target.lastBlockedDamage > 0.5f) {
            statusBurst(target.x, target.y, target.radius + 28f, Color.rgb(148, 163, 184), 0.9f);
            ring(target.x, target.y, target.radius + 34f, Color.rgb(148, 163, 184), 0.24f);
        }
        boolean heroDamage = attacker != null && attacker.kind == UnitKind.HERO;
        int damageColor = heroDamage ? damageColor(damageType, attacker.team) : Color.rgb(248, 250, 252);
        float hitIntensity = Math.min(1.8f, 0.35f + applied / Math.max(1f, target.maxHp) * 3.5f);
        hitBurst(target.x, target.y - target.radius * 0.15f, target.radius + 18f, damageColor,
                hitIntensity, damageType);
        if (heroDamage && attacker.lifeStealRate > 0f && applied > 0f) {
            healHero(attacker, applied * attacker.lifeStealRate, applied >= 28f);
        }
        if (wasAlive && !target.alive) {
            onKilled(attacker, target, damageTeam);
        }
    }

    private int damageColor(DamageType damageType, Team team) {
        if (damageType == DamageType.MAGIC) {
            return Color.rgb(216, 180, 254);
        }
        if (damageType == DamageType.TRUE_DAMAGE) {
            return Color.rgb(250, 204, 21);
        }
        return TeamStyle.color(team);
    }

    private String damageTypeName(DamageType damageType) {
        if (damageType == DamageType.MAGIC) {
            return text.get(GameTextKey.DAMAGE_TYPE_MAGIC);
        }
        if (damageType == DamageType.TRUE_DAMAGE) {
            return text.get(GameTextKey.DAMAGE_TYPE_TRUE);
        }
        return text.get(GameTextKey.DAMAGE_TYPE_PHYSICAL);
    }

    private void onKilled(GameEntity killer, GameEntity target, Team killerTeam) {
        int gold = target.goldValue;
        if (killerTeam == Team.BLUE) {
            blueGold += gold;
            blueKills += target.kind == UnitKind.HERO || target.kind == UnitKind.MONSTER || target.kind == UnitKind.BRUTE ? 1 : 0;
        } else if (killerTeam == Team.RED) {
            redGold += Math.max(1, Math.round(gold * aiProfile.goldRate));
            redKills += target.kind == UnitKind.HERO || target.kind == UnitKind.MONSTER || target.kind == UnitKind.BRUTE ? 1 : 0;
        }
        if (killer != null) {
            killer.kills++;
        }
        grantExperience(killer, target, killerTeam);
        applyKillSustain(killer, target, killerTeam);
        applyMonsterBuff(killer, target, killerTeam);
        if (target.kind == UnitKind.HERO) {
            target.respawnTimer = 6f;
            target.skillTimer = Math.min(target.skillTimer, 2f);
            target.secondarySkillTimer = Math.min(target.secondarySkillTimer, 2f);
            target.ultimateTimer = Math.min(target.ultimateTimer, 3f);
        }
        ring(target.x, target.y, target.radius + 42f, Color.rgb(248, 113, 113), 0.32f);
        if (target.kind == UnitKind.HERO) {
            message = text.get(GameTextKey.KILL_REWARD,
                    killer == null ? text.get(GameTextKey.NEUTRAL_KILLER) : killer.name,
                    target.name, gold, target.expValue);
        }
        handleKillAnnouncement(killer, target, killerTeam);
    }

    private void handleKillAnnouncement(GameEntity killer, GameEntity target, Team killerTeam) {
        if (target.kind == UnitKind.HERO) {
            resetHeroKillStreak(target.team);
        }
        if (killer == null || killer.kind != UnitKind.HERO || target.kind != UnitKind.HERO) {
            if (target.kind == UnitKind.HERO && target == blueHero) {
                emitSlainVoice(killer);
            }
            return;
        }
        long now = System.currentTimeMillis();
        int multiKillCount = updateMultiKillCount(killerTeam, now);
        int killStreak = updateHeroKillStreak(killerTeam);
        GameTextKey announcementKey = multiKillTextKey(multiKillCount);
        GameSoundEffect soundEffect = multiKillSoundEffect(multiKillCount);
        if (killStreak >= 8) {
            announcementKey = GameTextKey.KILL_LEGENDARY;
            soundEffect = GameSoundEffect.LEGENDARY;
        } else if (killStreak >= 6) {
            announcementKey = GameTextKey.KILL_GODLIKE;
            soundEffect = GameSoundEffect.GODLIKE;
        }
        String announcement = text.get(announcementKey, killer.name);
        emitSound(soundEffect);
        emitHeroKillVoice(killer, target, killerTeam, announcementKey);
        addText(killer.x, killer.y - 106f, announcement, Color.rgb(250, 204, 21), 1.55f);
        ring(killer.x, killer.y, 130f, Color.rgb(250, 204, 21), 0.55f);
        message = announcement;
    }

    private void emitSlainVoice(GameEntity killer) {
        emitVoice(BattleVoiceStep.phrase(BattleVoicePhrase.SLAIN));
        if (killer != null && killer.kind == UnitKind.HERO && killer.heroType != null) {
            emitVoice(BattleVoiceStep.hero(killer.heroType));
        }
    }

    private void emitHeroKillVoice(GameEntity killer, GameEntity target, Team killerTeam, GameTextKey announcementKey) {
        if (killer == null || killer.heroType == null) {
            return;
        }
        BattleVoicePhrase multiKillPhrase = multiKillVoicePhrase(announcementKey);
        if (multiKillPhrase == null) {
            return;
        }
        if (target == blueHero) {
            emitVoice(BattleVoiceStep.phrase(BattleVoicePhrase.SLAIN));
        }
        // LoL-style: %1$s 单杀 / 双杀 / 三杀 ...
        emitVoice(
                BattleVoiceStep.hero(killer.heroType),
                BattleVoiceStep.phrase(multiKillPhrase));
    }

    private BattleVoicePhrase multiKillVoicePhrase(GameTextKey announcementKey) {
        switch (announcementKey) {
            case KILL_DOUBLE:
                return BattleVoicePhrase.KILL_DOUBLE;
            case KILL_TRIPLE:
                return BattleVoicePhrase.KILL_TRIPLE;
            case KILL_QUADRA:
                return BattleVoicePhrase.KILL_QUADRA;
            case KILL_PENTA:
                return BattleVoicePhrase.KILL_PENTA;
            case KILL_GODLIKE:
                return BattleVoicePhrase.KILL_GODLIKE;
            case KILL_LEGENDARY:
                return BattleVoicePhrase.KILL_LEGENDARY;
            case KILL_SINGLE:
            default:
                return BattleVoicePhrase.KILL_SINGLE;
        }
    }

    private void emitVoice(BattleVoiceStep... steps) {
        if (steps == null) {
            return;
        }
        for (BattleVoiceStep step : steps) {
            if (step != null && voiceSteps.size() < 12) {
                voiceSteps.add(step);
            }
        }
    }

    private int updateMultiKillCount(Team team, long now) {
        if (team == Team.BLUE) {
            blueMultiKillCount = now <= blueMultiKillUntilMs ? blueMultiKillCount + 1 : 1;
            blueMultiKillUntilMs = now + MULTI_KILL_WINDOW_MS;
            return blueMultiKillCount;
        }
        redMultiKillCount = now <= redMultiKillUntilMs ? redMultiKillCount + 1 : 1;
        redMultiKillUntilMs = now + MULTI_KILL_WINDOW_MS;
        return redMultiKillCount;
    }

    private int updateHeroKillStreak(Team team) {
        if (team == Team.BLUE) {
            blueHeroKillStreak++;
            return blueHeroKillStreak;
        }
        redHeroKillStreak++;
        return redHeroKillStreak;
    }

    private void resetHeroKillStreak(Team team) {
        if (team == Team.BLUE) {
            blueHeroKillStreak = 0;
            blueMultiKillCount = 0;
            blueMultiKillUntilMs = 0L;
        } else if (team == Team.RED) {
            redHeroKillStreak = 0;
            redMultiKillCount = 0;
            redMultiKillUntilMs = 0L;
        }
    }

    private GameTextKey multiKillTextKey(int multiKillCount) {
        if (multiKillCount >= 5) {
            return GameTextKey.KILL_PENTA;
        }
        if (multiKillCount == 4) {
            return GameTextKey.KILL_QUADRA;
        }
        if (multiKillCount == 3) {
            return GameTextKey.KILL_TRIPLE;
        }
        if (multiKillCount == 2) {
            return GameTextKey.KILL_DOUBLE;
        }
        return GameTextKey.KILL_SINGLE;
    }

    private GameSoundEffect multiKillSoundEffect(int multiKillCount) {
        if (multiKillCount >= 5) {
            return GameSoundEffect.PENTA_KILL;
        }
        if (multiKillCount == 4) {
            return GameSoundEffect.QUADRA_KILL;
        }
        if (multiKillCount == 3) {
            return GameSoundEffect.TRIPLE_KILL;
        }
        if (multiKillCount == 2) {
            return GameSoundEffect.DOUBLE_KILL;
        }
        return GameSoundEffect.SINGLE_KILL;
    }

    private void grantExperience(GameEntity killer, GameEntity target, Team killerTeam) {
        if (killerTeam != Team.BLUE && killerTeam != Team.RED) {
            return;
        }
        int exp = target.expValue;
        if (killerTeam == Team.RED) {
            exp = Math.max(1, Math.round(exp * aiProfile.expRate));
        }
        if (killer != null && killer.kind == UnitKind.HERO) {
            gainExp(killer, exp);
            return;
        }
        GameEntity hero = killerTeam == Team.BLUE ? blueHero : redHero;
        if (hero != null && hero.alive && GameMath.distance(hero.x, hero.y, target.x, target.y) < 620f) {
            gainExp(hero, Math.max(1, Math.round(exp * 0.55f)));
        }
    }

    private void gainExp(GameEntity hero, int amount) {
        if (hero == null || hero.kind != UnitKind.HERO || amount <= 0) {
            return;
        }
        hero.exp += amount;
        hero.totalExp += amount;
        boolean leveled = false;
        while (hero.level < ProgressionRules.MAX_LEVEL && hero.exp >= hero.nextExp) {
            hero.exp -= hero.nextExp;
            hero.level++;
            hero.nextExp = ProgressionRules.nextExpForLevel(hero.level);
            hero.baseMaxHp += 82f;
            hero.baseAttack += 6.5f;
            hero.baseAttackRange += hero.level % 4 == 0 ? 4f : 0f;
            hero.baseSkillCooldown = Math.max(3.5f, hero.baseSkillCooldown - 0.08f);
            EquipmentCalculator.recalculateHeroStats(hero, 82f);
            leveled = true;
        }
        if (leveled) {
            healHero(hero, hero.maxHp * GameConfig.LEVEL_UP_HEAL_RATE, true);
            ring(hero.x, hero.y, 110f, Color.rgb(250, 204, 21), 0.6f);
            addText(hero.x, hero.y - 74f,
                    text.get(GameTextKey.FLOATING_LEVEL, hero.level), Color.rgb(250, 204, 21));
            emitSound(GameSoundEffect.LEVEL_UP);
            message = text.get(GameTextKey.LEVEL_UP, hero.name, hero.level);
        }
    }

    private void applyKillSustain(GameEntity killer, GameEntity target, Team killerTeam) {
        if (killerTeam != Team.BLUE && killerTeam != Team.RED) {
            return;
        }
        GameEntity sustainHero = killer != null && killer.kind == UnitKind.HERO
                ? killer
                : killerTeam == Team.BLUE ? blueHero : redHero;
        if (sustainHero == null || !sustainHero.alive) {
            return;
        }
        float healRate = killSustainRate(target);
        if (healRate <= 0f) {
            return;
        }
        if (killer == null || killer.kind != UnitKind.HERO) {
            float distance = GameMath.distance(sustainHero.x, sustainHero.y, target.x, target.y);
            if (distance > 620f) {
                return;
            }
        }
        healHero(sustainHero, sustainHero.maxHp * healRate, true);
    }

    private void applyMonsterBuff(GameEntity killer, GameEntity target, Team killerTeam) {
        if (target.kind != UnitKind.MONSTER || killerTeam == Team.NEUTRAL) {
            return;
        }
        GameEntity buffHero = killer != null && killer.kind == UnitKind.HERO
                ? killer
                : killerTeam == Team.BLUE ? blueHero : redHero;
        if (buffHero == null || !buffHero.alive) {
            return;
        }
        if (GameMath.distance(buffHero.x, buffHero.y, target.x, target.y) > 1440f) {
            return;
        }
        for (MonsterCamp camp : MonsterCamp.ALL) {
            if (!isNear(target, camp.x, camp.y, 300f)) {
                continue;
            }
            applyCampBuff(buffHero, camp.buffType);
            break;
        }
    }

    private void applyCampBuff(GameEntity buffHero, MonsterCamp.BuffType buffType) {
        switch (buffType) {
            case RED:
                buffHero.redBuffTimer = 45f;
                ring(buffHero.x, buffHero.y, 118f, Color.rgb(239, 68, 68), 0.72f);
                message = text.get(GameTextKey.RED_BUFF_GAIN, buffHero.name);
                addText(buffHero.x, buffHero.y - 92f, message, Color.rgb(252, 165, 165), 1.12f);
                break;
            case BLUE:
                buffHero.blueBuffTimer = 45f;
                ring(buffHero.x, buffHero.y, 118f, Color.rgb(96, 165, 250), 0.72f);
                message = text.get(GameTextKey.BLUE_BUFF_GAIN, buffHero.name);
                addText(buffHero.x, buffHero.y - 92f, message, Color.rgb(147, 197, 253), 1.12f);
                break;
            case SPEED:
                buffHero.speedBuffTimer = 45f;
                ring(buffHero.x, buffHero.y, 110f, Color.rgb(250, 204, 21), 0.62f);
                message = text.get(GameTextKey.SPEED_BUFF_GAIN, buffHero.name);
                addText(buffHero.x, buffHero.y - 92f, message, Color.rgb(253, 224, 71), 1.1f);
                break;
            case SHIELD:
                applyShield(buffHero, 45f, buffHero.maxHp * 0.2f);
                ring(buffHero.x, buffHero.y, 110f, Color.rgb(134, 239, 172), 0.62f);
                message = text.get(GameTextKey.SHIELD_BUFF_GAIN, buffHero.name);
                addText(buffHero.x, buffHero.y - 92f, message, Color.rgb(187, 247, 208), 1.1f);
                break;
            case NONE:
            default:
                break;
        }
    }

    private boolean isNear(GameEntity entity, float x, float y, float radius) {
        return GameMath.distance(entity.x, entity.y, x, y) <= radius;
    }

    private float killSustainRate(GameEntity target) {
        if (target.kind == UnitKind.MONSTER) {
            return GameConfig.NEUTRAL_KILL_HEAL_RATE;
        }
        if (target.kind == UnitKind.BRUTE) {
            return GameConfig.BRUTE_KILL_HEAL_RATE;
        }
        return 0f;
    }

    private void healHero(GameEntity hero, float amount, boolean showEffect) {
        if (hero == null || hero.kind != UnitKind.HERO || amount <= 0f || hero.hp >= hero.maxHp) {
            return;
        }
        float oldHp = hero.hp;
        hero.hp = Math.min(hero.maxHp, hero.hp + amount);
        float recovered = hero.hp - oldHp;
        if (!showEffect || recovered <= 0f) {
            return;
        }
        ring(hero.x, hero.y, 86f, Color.rgb(34, 197, 94), 0.38f);
        addText(hero.x, hero.y - 58f,
                text.get(GameTextKey.FLOATING_HEAL, Math.max(1, Math.round(recovered))),
                Color.rgb(134, 239, 172), 1.08f);
    }

    private GameEntity selectAttackTarget(GameEntity attacker) {
        if (attacker.kind == UnitKind.TOWER) {
            return nearestTowerTarget(attacker, attacker.attackRange);
        }
        return selectSkillTarget(attacker, attacker.attackRange);
    }

    private GameEntity nearestTowerTarget(GameEntity tower, float range) {
        GameEntity bestMinion = null;
        float bestMinionDistance = Float.MAX_VALUE;
        GameEntity bestHero = null;
        float bestHeroDistance = Float.MAX_VALUE;
        for (GameEntity entity : entities) {
            if (!canTarget(tower, entity) || entity.team == Team.NEUTRAL) {
                continue;
            }
            float distance = tower.distanceTo(entity) - entity.radius;
            if (distance > range) {
                continue;
            }
            if (isLaneMinion(entity) || entity.kind == UnitKind.BRUTE) {
                if (distance < bestMinionDistance) {
                    bestMinionDistance = distance;
                    bestMinion = entity;
                }
            } else if (entity.kind == UnitKind.HERO && distance < bestHeroDistance) {
                bestHeroDistance = distance;
                bestHero = entity;
            }
        }
        return bestMinion != null ? bestMinion : bestHero;
    }

    private GameEntity nearestTarget(GameEntity from, float range, boolean includeNeutral) {
        GameEntity best = null;
        float bestDistance = Float.MAX_VALUE;
        for (GameEntity entity : entities) {
            if (!canTarget(from, entity)) {
                continue;
            }
            if (!includeNeutral && entity.team == Team.NEUTRAL) {
                continue;
            }
            if (from.kind == UnitKind.TOWER && entity.team == Team.NEUTRAL) {
                continue;
            }
            float distance = from.distanceTo(entity) - entity.radius;
            if (distance <= range && distance < bestDistance) {
                bestDistance = distance;
                best = entity;
            }
        }
        return best;
    }

    private boolean canTarget(GameEntity from, GameEntity target) {
        return from != null
                && canTarget(from.team, target)
                && from.id != target.id
                && GameTerrain.canSee(from, target);
    }

    private boolean canTarget(Team team, GameEntity target) {
        if (target == null || !target.alive) {
            return false;
        }
        if (team == Team.NEUTRAL) {
            return target.team != Team.NEUTRAL;
        }
        return target.team != team;
    }

    private GameEntity findById(long id) {
        for (GameEntity entity : entities) {
            if (entity.id == id) {
                return entity;
            }
        }
        return null;
    }

    private void moveHero(GameEntity hero, float dt, float mx, float my) {
        float len = GameMath.vectorLength(mx, my);
        if (len <= 0.08f) {
            return;
        }
        hero.facingRad = (float) Math.atan2(my, mx);
        float dx = mx / len * moveSpeed(hero) * dt;
        float dy = my / len * moveSpeed(hero) * dt;
        float[] resolved = GameTerrain.resolveSlide(hero.x, hero.y, dx, dy);
        hero.x = resolved[0];
        hero.y = resolved[1];
        clamp(hero);
    }

    private void moveToward(GameEntity entity, float tx, float ty, float dt, float speed) {
        float oldX = entity.x;
        float oldY = entity.y;
        GameMath.moveToward(entity, tx, ty, dt, speed);
        float dx = entity.x - oldX;
        float dy = entity.y - oldY;
        if (Math.abs(dx) > 0.001f || Math.abs(dy) > 0.001f) {
            float[] resolved = GameTerrain.resolveSlide(oldX, oldY, dx, dy);
            entity.x = resolved[0];
            entity.y = resolved[1];
            float moved = GameMath.distance(oldX, oldY, entity.x, entity.y);
            if (moved > 0.6f) {
                entity.facingRad = (float) Math.atan2(entity.y - oldY, entity.x - oldX);
            }
        }
        clamp(entity);
    }

    private float moveSpeed(GameEntity entity) {
        float speed = entity.speed;
        if (entity.kind == UnitKind.HERO && entity.team == Team.RED) {
            speed *= aiProfile.speedScale;
        } else if (entity.kind == UnitKind.HERO && entity.team == Team.BLUE) {
            speed *= blueChaseSpeedScale(entity);
        }
        if (entity.slowTimer > 0f) {
            speed *= 0.55f;
        }
        if (entity.speedBuffTimer > 0f) {
            speed *= 1.28f;
        }
        return speed;
    }

    private float blueChaseSpeedScale(GameEntity entity) {
        float scale = 1f;
        if (entity.boots != null) {
            scale += 0.10f;
        }
        if (redHero != null && redHero.alive && redHero.hp < redHero.maxHp * 0.38f) {
            scale += 0.08f;
        }
        if (redHero != null && entity.level > redHero.level) {
            scale += Math.min(0.14f, (entity.level - redHero.level) * 0.035f);
        }
        if (entity.damageBoostTimer > 0f || entity.speedBuffTimer > 0f) {
            scale += 0.06f;
        }
        return Math.min(1.28f, scale);
    }

    private float applySnowballExecute(GameEntity attacker, GameEntity target, float finalDamage) {
        if (attacker == null || attacker.team != Team.BLUE || attacker.kind != UnitKind.HERO
                || target == null || target.team != Team.RED || target.kind != UnitKind.HERO) {
            return finalDamage;
        }
        float advantage = 0f;
        advantage += Math.max(0, attacker.level - target.level) * 0.06f;
        advantage += Math.max(0, blueGold - redGold) / 2200f;
        if (attacker.weapon != null) {
            advantage += 0.08f;
        }
        if (attacker.hat != null || attacker.relic != null) {
            advantage += 0.07f;
        }
        if (target.hp < target.maxHp * 0.32f) {
            advantage += 0.18f;
        }
        if (target.hp < target.maxHp * 0.18f) {
            advantage += 0.16f;
        }
        if (advantage <= 0f) {
            return finalDamage;
        }
        if (target.hp < target.maxHp * 0.16f && advantage >= 0.34f) {
            return Math.max(finalDamage, target.hp + 1f);
        }
        return finalDamage * (1f + Math.min(0.75f, advantage));
    }

    private void clamp(GameEntity entity) {
        GameMath.clampEntity(entity, GameConfig.MIN_X, GameConfig.MAX_X, GameConfig.MIN_Y, GameConfig.MAX_Y);
    }

    private void spawnWave(boolean bruteWave) {
        spawnLaneWaveSide(Team.BLUE, bruteWave);
        spawnLaneWaveSide(Team.RED, bruteWave);
        if (bruteWave) {
            message = text.get(GameTextKey.BRUTE_WAVE);
        } else {
            message = text.get(GameTextKey.MINION_WAVE);
        }
    }

    private void spawnLaneWaveSide(Team team, boolean bruteWave) {
        int meleeCount = bruteWave ? 2 : 3;
        int rangedCount = 2;
        for (int i = 0; i < meleeCount; i++) {
            float y = GameConfig.LANE_Y + (i - (meleeCount - 1) / 2f) * GameConfig.MINION_WAVE_Y_SPACING;
            addMinion(team, MinionType.MELEE, i, 0f, y);
        }
        for (int i = 0; i < rangedCount; i++) {
            float y = GameConfig.LANE_Y + (i == 0 ? -1.5f : 1.5f) * GameConfig.MINION_WAVE_Y_SPACING;
            addMinion(team, MinionType.RANGED, i, -54f, y);
        }
        if (bruteWave) {
            float y = GameConfig.LANE_Y + (team == Team.BLUE
                    ? -GameConfig.BRUTE_WAVE_Y_OFFSET
                    : GameConfig.BRUTE_WAVE_Y_OFFSET);
            addMinion(team, MinionType.BRUTE, 0, -88f, y);
        }
    }

    private void addMinion(Team team, MinionType type, int index, float trailOffset, float y) {
        float direction = team == Team.BLUE ? 1f : -1f;
        float baseX = team == Team.BLUE ? GameConfig.BLUE_BASE_X : GameConfig.RED_BASE_X;
        float x = baseX + direction * (GameConfig.MINION_WAVE_X_OFFSET
                + index * GameConfig.MINION_WAVE_X_SPACING
                + trailOffset);
        entities.add(EntityFactory.minion(team, x, y, type, minionName(team, type)));
    }

    private void spawnMonsters() {
        for (MonsterCamp camp : MonsterCamp.ALL) {
            String campName = text.get(camp.nameKey);
            entities.add(EntityFactory.monster(campName, camp.x, camp.y));
            for (float[] offset : camp.guardOffsets) {
                entities.add(EntityFactory.monster(
                        text.get(GameTextKey.MONSTER_GUARD, campName),
                        camp.x + offset[0], camp.y + offset[1]));
            }
        }
        message = text.get(GameTextKey.MONSTER_REFRESH);
    }

    private void applyItem(GameEntity hero, ItemType itemType) {
        EquipmentCalculator.applyItem(hero, itemType);
        if (itemType.slot.isConsumable()) {
            addText(hero.x, hero.y - 52f,
                    text.get(GameTextKey.FLOATING_HEAL, Math.round(itemType.healAmount)),
                    Color.rgb(134, 239, 172));
            return;
        }
        addText(hero.x, hero.y - 52f,
                text.get(GameTextKey.ITEM_EQUIPPED, itemType.label), Color.rgb(250, 204, 21), 1.2f);
    }

    private void autoBuyForRed() {
        if (redHero == null || !redHero.alive || !isInOwnFountain(redHero)) {
            return;
        }
        if (redHero.hp < redHero.maxHp * 0.45f && redGold >= ItemType.GOLDEN_PILL.cost) {
            redGold -= ItemType.GOLDEN_PILL.cost;
            applyItem(redHero, ItemType.GOLDEN_PILL);
            emitSound(GameSoundEffect.SHOP);
            message = text.get(GameTextKey.RED_BUY_ITEM, ItemType.GOLDEN_PILL.label);
            return;
        }
        BuildPlan plan = BuildGuideResolver.buildPlan(
                redHeroType,
                blueHeroType,
                redHero.weapon,
                redHero.armor,
                redHero.boots,
                redHero.hat,
                redHero.relic,
                redGold,
                redHero.maxHp > 0f ? redHero.hp / redHero.maxHp : 1f,
                redHero.level,
                redHero.basicAttackDamageType);
        if (plan.nextPurchase == null || redGold < plan.nextPurchase.cost) {
            return;
        }
        redGold -= plan.nextPurchase.cost;
        applyItem(redHero, plan.nextPurchase);
        emitSound(GameSoundEffect.SHOP);
        message = text.get(GameTextKey.RED_BUY_ITEM, plan.nextPurchase.label);
    }

    private void removeDeadUnits() {
        Iterator<GameEntity> iterator = entities.iterator();
        while (iterator.hasNext()) {
            GameEntity entity = iterator.next();
            if (!entity.alive && entity.kind != UnitKind.TOWER && entity.kind != UnitKind.HERO) {
                iterator.remove();
            }
        }
    }

    private int countLiving(UnitKind kind) {
        int count = 0;
        for (GameEntity entity : entities) {
            if (entity.kind == kind && entity.alive) {
                count++;
            }
        }
        return count;
    }

    private int countTargetsAround(GameEntity from, float radius) {
        int count = 0;
        for (GameEntity entity : entities) {
            if (canTarget(from, entity) && from.distanceTo(entity) <= radius + entity.radius) {
                count++;
            }
        }
        return count;
    }

    private void checkWinner() {
        if (!blueHighlandTower.alive || blueHighlandTower.hp <= 0f) {
            finish(Team.RED);
        } else if (!redHighlandTower.alive || redHighlandTower.hp <= 0f) {
            finish(Team.BLUE);
        }
    }

    private void finish(Team team) {
        winner = team;
        message = text.get(GameTextKey.TEAM_WIN, sideName(team));
        emitSound(GameSoundEffect.VICTORY);
        listener.onGameFinished(team, System.currentTimeMillis() - startMs, buildBattleSummary(team));
    }

    public BattleSummary buildBattleSummary(Team winnerTeam) {
        BattleSummary summary = new BattleSummary();
        summary.winnerTeam = winnerTeam.name();
        summary.winnerLabel = sideName(winnerTeam);
        summary.blueHeroKey = blueHeroType.name();
        summary.blueHeroLabel = blueHeroType.label;
        summary.redHeroKey = redHeroType.name();
        summary.redHeroLabel = redHeroType.label;
        summary.blueKills = blueKills;
        summary.redKills = redKills;
        summary.blueGold = blueGold;
        summary.redGold = redGold;
        summary.blueLevel = blueHero == null ? 1 : blueHero.level;
        summary.redLevel = redHero == null ? 1 : redHero.level;
        summary.blueBuild = buildLoadout(blueHero);
        summary.redBuild = buildLoadout(redHero);
        summary.playerWon = winnerTeam == Team.BLUE;
        return summary;
    }

    public long getElapsedMs() {
        return startMs > 0L ? System.currentTimeMillis() - startMs : 0L;
    }

    public BattleSummary buildOngoingSummary() {
        BattleSummary summary = buildBattleSummary(Team.BLUE);
        summary.winnerTeam = "SNAPSHOT";
        summary.winnerLabel = "录像快照";
        summary.playerWon = false;
        return summary;
    }

    private String buildLoadout(GameEntity hero) {
        if (hero == null) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        appendItemLabel(builder, hero.weapon);
        appendItemLabel(builder, hero.armor);
        appendItemLabel(builder, hero.boots);
        appendItemLabel(builder, hero.hat);
        appendItemLabel(builder, hero.relic);
        return builder.toString();
    }

    private void appendItemLabel(StringBuilder builder, ItemType itemType) {
        if (itemType == null) {
            return;
        }
        if (builder.length() > 0) {
            builder.append(" / ");
        }
        builder.append(itemType.label);
    }

    private String heroName(Team team, HeroType heroType) {
        return text.get(GameTextKey.HERO_ENTITY_NAME, sideName(team), heroType.label);
    }

    private String towerName(Team team, TowerTier tier) {
        if (team == Team.BLUE) {
            if (tier == TowerTier.HIGHLAND) {
                return text.get(GameTextKey.TOWER_BLUE_HIGHLAND);
            }
            return tier == TowerTier.MIDDLE
                    ? text.get(GameTextKey.TOWER_BLUE_MIDDLE)
                    : text.get(GameTextKey.TOWER_BLUE_OUTER);
        }
        if (tier == TowerTier.HIGHLAND) {
            return text.get(GameTextKey.TOWER_RED_HIGHLAND);
        }
        return tier == TowerTier.MIDDLE
                ? text.get(GameTextKey.TOWER_RED_MIDDLE)
                : text.get(GameTextKey.TOWER_RED_OUTER);
    }

    private String minionName(Team team, MinionType type) {
        GameTextKey unitKey;
        if (type == MinionType.BRUTE) {
            unitKey = GameTextKey.MINION_BRUTE;
        } else {
            unitKey = type == MinionType.RANGED ? GameTextKey.MINION_RANGED : GameTextKey.MINION_MELEE;
        }
        return text.get(GameTextKey.MINION_ENTITY_NAME, minionPrefix(team), text.get(unitKey));
    }

    private boolean isLaneMinion(GameEntity entity) {
        return entity.kind == UnitKind.MINION
                || entity.kind == UnitKind.RANGED_MINION
                || entity.kind == UnitKind.BRUTE;
    }

    private void emitAttackSound(GameEntity attacker) {
        if (attacker.kind == UnitKind.TOWER) {
            emitSound(GameSoundEffect.TOWER_ATTACK);
        } else if (attacker.kind == UnitKind.HERO) {
            emitSound(GameSoundEffect.HERO_ATTACK);
        }
    }

    private void emitSound(GameSoundEffect soundEffect) {
        if (soundEffects.size() < 6) {
            soundEffects.add(soundEffect);
        }
    }

    private String sideName(Team team) {
        if (team == Team.BLUE) {
            return text.get(GameTextKey.SIDE_BLUE);
        }
        if (team == Team.RED) {
            return text.get(GameTextKey.SIDE_RED);
        }
        return text.get(GameTextKey.SIDE_NEUTRAL);
    }

    private String minionPrefix(Team team) {
        if (team == Team.BLUE) {
            return text.get(GameTextKey.MINION_PREFIX_BLUE);
        }
        if (team == Team.RED) {
            return text.get(GameTextKey.MINION_PREFIX_RED);
        }
        return text.get(GameTextKey.MINION_PREFIX_NEUTRAL);
    }

    private String equippedName(ItemType itemType) {
        return itemType == null ? "" : itemType.label;
    }

    private void ring(float x, float y, float radius, int color, float ttl) {
        effects.add(new VisualEffect(EffectKind.RING, x, y, radius, ttl, color, ""));
        trimEffects();
    }

    private void line(float x, float y, float x2, float y2, int color) {
        float dx = x2 - x;
        float dy = y2 - y;
        if (dx * dx + dy * dy < 4f) {
            x2 = x + 18f;
        }
        effects.add(VisualEffect.line(x, y, x2, y2, 0.18f, color));
        trimEffects();
    }

    private void hitBurst(float x, float y, float radius, int color, float intensity, DamageType damageType) {
        if (!Float.isFinite(x) || !Float.isFinite(y)) {
            return;
        }
        VisualEffect effect = new VisualEffect(EffectKind.HIT_BURST, x, y, Math.max(12f, radius), 0.42f, color, "");
        effect.intensity = Float.isFinite(intensity) ? Math.max(0.2f, intensity) : 1f;
        effect.damageType = damageType;
        effects.add(effect);
        trimEffects();
    }

    private void statusBurst(float x, float y, float radius, int color, float intensity) {
        VisualEffect effect = new VisualEffect(EffectKind.STATUS_BURST, x, y, Math.max(12f, radius), 0.55f, color, "");
        effect.intensity = intensity;
        effects.add(effect);
        trimEffects();
    }

    private void skillBurst(float x, float y, float radius, int color, float intensity) {
        VisualEffect effect = new VisualEffect(EffectKind.SKILL_BURST, x, y, Math.max(16f, radius), 0.65f, color, "");
        effect.intensity = intensity;
        effects.add(effect);
        trimEffects();
    }

    private void healBurst(float x, float y, float radius) {
        effects.add(new VisualEffect(EffectKind.HEAL_BURST, x, y, Math.max(20f, radius), 0.55f, Color.rgb(134, 239, 172), ""));
        trimEffects();
    }

    private void trimEffects() {
        while (effects.size() > 140) {
            effects.remove(0);
        }
    }

    private void addText(float x, float y, String text, int color) {
        skillBurst(x, y, 36f, color, 0.85f);
    }

    private void addText(float x, float y, String text, int color, float textScale) {
        skillBurst(x, y, 30f + textScale * 18f, color, textScale);
    }

}
