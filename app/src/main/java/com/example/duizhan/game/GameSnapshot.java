package com.example.duizhan.game;

import com.example.duizhan.game.audio.BattleVoiceStep;

import java.util.ArrayList;
import java.util.List;

public class GameSnapshot {
    public final List<GameEntity> entities = new ArrayList<>();
    public final List<Projectile> projectiles = new ArrayList<>();
    public final List<VisualEffect> effects = new ArrayList<>();
    public final List<GameSoundEffect> soundEffects = new ArrayList<>();
    public final List<BattleVoiceStep> voiceSteps = new ArrayList<>();
    public float blueTowerHp;
    public float redTowerHp;
    public float blueHeroHp;
    public float blueHeroMaxHp;
    public float blueAttack;
    public float blueDamageBonusRate;
    public float blueDamageReductionRate;
    public float blueSkillCooldown;
    public float blueSkillCooldownMax;
    public float blueSecondarySkillCooldown;
    public float blueSecondarySkillCooldownMax;
    public float blueUltimateCooldown;
    public float blueUltimateCooldownMax;
    public HeroArchetype blueArchetype;
    public DamageType blueBasicAttackDamageType;
    public String bluePrimarySkillName;
    public String blueSecondarySkillName;
    public String blueUltimateSkillName;
    public String blueTalentName;
    public float blueTalentCooldown;
    public float blueTalentCooldownMax;
    public float blueTalentDamageBonusRate;
    public float blueRecallChannel;
    public boolean snapCameraToBlueHero;
    public String blueWeaponName;
    public String blueArmorName;
    public String blueBootsName;
    public String blueHatName;
    public String blueRelicName;
    public int blueLevel;
    public int blueExp;
    public int blueNextExp;
    public int blueGold;
    public int redGold;
    public int blueKills;
    public int redKills;
    public BattleDifficulty difficulty = BattleDifficulty.MEDIUM;
    public String message = "";
    public boolean finished;
    public Team winner;
}
