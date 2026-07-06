package com.example.duizhan.game.util;

import com.example.duizhan.game.EquipmentSlot;
import com.example.duizhan.game.GameEntity;
import com.example.duizhan.game.ItemType;

public final class EquipmentCalculator {
    private EquipmentCalculator() {
    }

    public static void applyItem(GameEntity hero, ItemType itemType) {
        if (itemType.slot == EquipmentSlot.CONSUMABLE) {
            hero.hp = Math.min(hero.maxHp, hero.hp + itemType.healAmount);
            return;
        }
        equip(hero, itemType);
        recalculateHeroStats(hero, 0f);
    }

    public static void recalculateHeroStats(GameEntity hero, float extraHeal) {
        float missingHp = Math.max(0f, hero.maxHp - hero.hp);
        ItemTotals totals = totalItems(hero);
        hero.maxHp = hero.baseMaxHp + totals.hp;
        hero.attack = hero.baseAttack + totals.attack + totals.magicPower * 0.62f;
        hero.attackRange = hero.baseAttackRange + Math.min(160f, totals.range);
        hero.attackCooldown = Math.max(0.42f,
                hero.baseAttackCooldown * (1f - Math.min(0.55f, totals.attackSpeedRate)));
        hero.speed = hero.baseSpeed + totals.speed;
        hero.skillCooldown = Math.max(2.8f, hero.baseSkillCooldown * (1f - Math.min(0.35f, totals.cooldownRate)));
        hero.secondarySkillCooldown = Math.max(3.6f, hero.baseSkillCooldown * 0.72f
                * (1f - Math.min(0.35f, totals.cooldownRate)));
        hero.ultimateCooldown = Math.max(9.5f, hero.baseSkillCooldown * 2.25f
                * (1f - Math.min(0.35f, totals.cooldownRate)));
        hero.damageBonusRate = Math.min(0.75f, totals.damageRate);
        hero.magicDamageBonusRate = Math.min(0.75f, totals.magicDamageRate);
        hero.damageReductionRate = Math.min(0.45f, totals.reductionRate);
        hero.attackSpeedBonusRate = Math.min(0.55f, totals.attackSpeedRate);
        hero.lifeStealRate = Math.min(0.25f, totals.lifeStealRate);
        hero.hp = Math.min(hero.maxHp, Math.max(1f, hero.maxHp - missingHp + extraHeal));
        HeroTalentCalculator.refreshTalentStats(hero);
    }

    private static void equip(GameEntity hero, ItemType itemType) {
        switch (itemType.slot) {
            case WEAPON:
                hero.weapon = itemType;
                break;
            case ARMOR:
                hero.armor = itemType;
                break;
            case BOOTS:
                hero.boots = itemType;
                break;
            case HAT:
                hero.hat = itemType;
                break;
            case RELIC:
                hero.relic = itemType;
                break;
            case CONSUMABLE:
                break;
        }
    }

    private static ItemTotals totalItems(GameEntity hero) {
        ItemTotals totals = new ItemTotals();
        add(totals, hero.weapon);
        add(totals, hero.armor);
        add(totals, hero.boots);
        add(totals, hero.hat);
        add(totals, hero.relic);
        return totals;
    }

    private static void add(ItemTotals totals, ItemType item) {
        if (item == null) {
            return;
        }
        totals.attack += item.attackBonus;
        totals.magicPower += item.magicPowerBonus;
        totals.hp += item.hpBonus;
        totals.speed += item.speedBonus;
        totals.damageRate += item.damageBonusRate;
        totals.magicDamageRate += item.magicDamageBonusRate;
        totals.reductionRate += item.damageReductionRate;
        totals.cooldownRate += item.cooldownReductionRate;
        totals.attackSpeedRate += item.attackSpeedBonusRate;
        totals.range += item.rangeBonus;
        totals.lifeStealRate += item.lifeStealRate;
    }

    private static class ItemTotals {
        float attack;
        float magicPower;
        float hp;
        float speed;
        float damageRate;
        float magicDamageRate;
        float reductionRate;
        float cooldownRate;
        float attackSpeedRate;
        float range;
        float lifeStealRate;
    }
}
