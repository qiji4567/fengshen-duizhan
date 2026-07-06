package com.example.duizhan.ui.util;

import android.content.Context;

import com.example.duizhan.R;
import com.example.duizhan.game.BattleDifficulty;
import com.example.duizhan.game.DamageType;
import com.example.duizhan.game.GameEntity;
import com.example.duizhan.game.GameSnapshot;
import com.example.duizhan.game.HeroArchetype;
import com.example.duizhan.game.HeroType;
import com.example.duizhan.game.ItemType;
import com.example.duizhan.game.util.HeroSkillNameResolver;

public final class UiTextUtils {
    private UiTextUtils() {
    }

    public static String heroCard(Context context, HeroType heroType) {
        return context.getString(R.string.hero_card_format,
                heroType.label, heroType.role, heroType.skillDescription,
                HeroSkillNameResolver.talentDescription(heroType));
    }

    public static String shopItem(Context context, ItemType itemType) {
        return context.getString(R.string.shop_item_format,
                itemType.label, itemType.cost, itemType.description);
    }

    public static String skillCooldown(Context context, float seconds) {
        return context.getString(R.string.skill_cooldown_format, seconds);
    }

    public static String battleStatus(Context context, GameSnapshot snapshot) {
        return context.getString(R.string.battle_status_format,
                UiTextUtils.difficultyLabel(context, snapshot.difficulty),
                snapshot.blueLevel, snapshot.blueExp, snapshot.blueNextExp, snapshot.blueGold,
                snapshot.blueAttack, snapshot.blueDamageBonusRate * 100f,
                snapshot.blueTalentDamageBonusRate * 100f,
                snapshot.blueDamageReductionRate * 100f, snapshot.blueTowerHp,
                snapshot.redTowerHp);
    }

    public static String difficultyLabel(Context context, BattleDifficulty difficulty) {
        if (difficulty == BattleDifficulty.NORMAL) {
            return context.getString(R.string.difficulty_normal);
        }
        if (difficulty == BattleDifficulty.HARD) {
            return context.getString(R.string.difficulty_hard);
        }
        return context.getString(R.string.difficulty_medium);
    }

    public static String battleHeroLabel(Context context, GameEntity entity) {
        return context.getString(R.string.battle_view_hero_label_format, entity.level, entity.name);
    }

    public static String respawn(Context context, float seconds) {
        return context.getString(R.string.battle_view_respawn_format, seconds);
    }

    public static String battleHud(Context context, int unitCount, int projectileCount, int effectCount) {
        return context.getString(R.string.battle_view_hud_format, unitCount, projectileCount, effectCount);
    }

    public static String battleStats(Context context, GameSnapshot snapshot) {
        return context.getString(R.string.battle_view_stats_format,
                snapshot.blueHeroHp, snapshot.blueHeroMaxHp, snapshot.blueAttack,
                snapshot.blueDamageBonusRate * 100f, snapshot.blueTalentDamageBonusRate * 100f,
                snapshot.blueDamageReductionRate * 100f);
    }

    public static String battleCombatLine(Context context, GameSnapshot snapshot) {
        return context.getString(R.string.battle_view_combat_line_format,
                archetypeName(context, snapshot.blueArchetype),
                damageTypeName(context, snapshot.blueBasicAttackDamageType),
                snapshot.blueTalentName);
    }

    public static String equipmentLineOne(Context context, GameSnapshot snapshot) {
        return context.getString(R.string.battle_view_equipment_line_one_format,
                displayEquipment(context, snapshot.blueWeaponName),
                displayEquipment(context, snapshot.blueArmorName));
    }

    public static String equipmentLineTwo(Context context, GameSnapshot snapshot) {
        return context.getString(R.string.battle_view_equipment_line_two_format,
                displayEquipment(context, snapshot.blueBootsName),
                displayEquipment(context, snapshot.blueHatName));
    }

    private static String displayEquipment(Context context, String name) {
        return name == null || name.length() == 0
                ? context.getString(R.string.battle_view_equipment_empty)
                : name;
    }

    private static String archetypeName(Context context, HeroArchetype archetype) {
        if (archetype == HeroArchetype.MARKSMAN) {
            return context.getString(R.string.hero_archetype_marksman);
        }
        if (archetype == HeroArchetype.MAGE) {
            return context.getString(R.string.hero_archetype_mage);
        }
        if (archetype == HeroArchetype.TANK) {
            return context.getString(R.string.hero_archetype_tank);
        }
        if (archetype == HeroArchetype.ASSASSIN) {
            return context.getString(R.string.hero_archetype_assassin);
        }
        if (archetype == HeroArchetype.SUPPORT) {
            return context.getString(R.string.hero_archetype_support);
        }
        return context.getString(R.string.hero_archetype_fighter);
    }

    private static String damageTypeName(Context context, DamageType damageType) {
        if (damageType == DamageType.MAGIC) {
            return context.getString(R.string.game_damage_type_magic);
        }
        if (damageType == DamageType.TRUE_DAMAGE) {
            return context.getString(R.string.game_damage_type_true);
        }
        return context.getString(R.string.game_damage_type_physical);
    }
}
