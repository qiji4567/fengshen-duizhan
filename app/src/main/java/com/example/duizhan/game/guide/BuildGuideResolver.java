package com.example.duizhan.game.guide;

import com.example.duizhan.game.DamageType;
import com.example.duizhan.game.EquipmentSlot;
import com.example.duizhan.game.HeroArchetype;
import com.example.duizhan.game.HeroType;
import com.example.duizhan.game.ItemType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class BuildGuideResolver {
    private BuildGuideResolver() {
    }

    public static List<ItemType> recommendedItems(HeroType heroType) {
        List<ItemType> items = new ArrayList<>();
        if (heroType == null) {
            return items;
        }
        if (applyHeroSpecificLoadout(heroType, items)) {
            return items;
        }
        switch (heroType.archetype()) {
            case MARKSMAN:
                items.addAll(Arrays.asList(
                        ItemType.STAR_BOW, ItemType.JADE_BOOTS, ItemType.BAGUA_ARMOR,
                        ItemType.DRAGON_PEARL, ItemType.GOLDEN_PILL));
                break;
            case MAGE:
                items.addAll(Arrays.asList(
                        ItemType.SOUL_CHAIN, ItemType.LOTUS_ROBE, ItemType.THUNDER_BOOTS,
                        ItemType.PHOENIX_CROWN, ItemType.GOLDEN_PILL));
                break;
            case ASSASSIN:
                items.addAll(Arrays.asList(
                        ItemType.WHITE_TIGER_CLAW, ItemType.BLOOD_BOOTS, ItemType.TIGER_MAIL,
                        ItemType.BONE_CROWN, ItemType.GOLDEN_PILL));
                break;
            case TANK:
                items.addAll(Arrays.asList(
                        ItemType.XUANWU_ARMOR, ItemType.QILIN_COAT, ItemType.CLOUD_BOOTS,
                        ItemType.GOD_LIST_RELIC, ItemType.GOLDEN_PILL));
                break;
            case SUPPORT:
                items.addAll(Arrays.asList(
                        ItemType.LOTUS_ROBE, ItemType.LOTUS_CROWN, ItemType.CLOUD_BOOTS,
                        ItemType.MOON_ROBE, ItemType.PEACH_PILL));
                break;
            case FIGHTER:
            default:
                items.addAll(Arrays.asList(
                        ItemType.DEMON_BLADE, ItemType.FIRE_BOOTS, ItemType.DRAGON_SCALE_ROBE,
                        ItemType.PHOENIX_CROWN, ItemType.GOLDEN_PILL));
                break;
        }
        return items;
    }

    public static Map<EquipmentSlot, ItemType> optimalLoadout(HeroType heroType) {
        LinkedHashMap<EquipmentSlot, ItemType> loadout = new LinkedHashMap<>();
        for (ItemType item : recommendedItems(heroType)) {
            if (item.slot.isConsumable()) {
                continue;
            }
            loadout.putIfAbsent(item.slot, item);
        }
        return loadout;
    }

    public static BuildPlan buildPlan(HeroType heroType,
                                      HeroType enemyType,
                                      ItemType weapon,
                                      ItemType armor,
                                      ItemType boots,
                                      ItemType hat,
                                      ItemType relic,
                                      int gold,
                                      float hpRatio,
                                      int level,
                                      DamageType damageType) {
        Map<EquipmentSlot, ItemType> target = optimalLoadout(heroType);
        List<ScoredItem> missing = new ArrayList<>();
        int totalCost = 0;

        for (Map.Entry<EquipmentSlot, ItemType> entry : target.entrySet()) {
            ItemType targetItem = entry.getValue();
            ItemType current = equipped(entry.getKey(), weapon, armor, boots, hat, relic);
            if (current == targetItem) {
                continue;
            }
            int score = scorePurchase(heroType, enemyType, entry.getKey(), targetItem, level, hpRatio, damageType);
            missing.add(new ScoredItem(targetItem, score));
            totalCost += targetItem.cost;
        }

        missing.sort(Comparator.comparingInt((ScoredItem item) -> item.score).reversed());

        List<ItemType> purchasesInOrder = new ArrayList<>();
        for (ScoredItem scoredItem : missing) {
            purchasesInOrder.add(scoredItem.item);
        }

        List<ItemType> affordablePurchases = new ArrayList<>();
        int affordableCost = 0;
        int remainingGold = Math.max(0, gold);
        for (ItemType item : purchasesInOrder) {
            if (remainingGold < item.cost) {
                break;
            }
            affordablePurchases.add(item);
            affordableCost += item.cost;
            remainingGold -= item.cost;
        }

        ItemType nextPurchase = purchasesInOrder.isEmpty() ? null : purchasesInOrder.get(0);
        String nextReason = nextPurchase == null ? "" : reasonFor(heroType, enemyType, nextPurchase, level, hpRatio);

        ItemType consumableSuggestion = null;
        String consumableReason = "";
        if (hpRatio < 0.55f) {
            consumableSuggestion = gold >= ItemType.PEACH_PILL.cost && level >= 8
                    ? ItemType.PEACH_PILL
                    : ItemType.GOLDEN_PILL;
            consumableReason = hpRatio < 0.35f ? "血量偏低，先备一枚回复丹药" : "血量不足一半，建议带回复药";
        }

        boolean alreadyOptimal = purchasesInOrder.isEmpty();
        return new BuildPlan(target, purchasesInOrder, affordablePurchases,
                nextPurchase, nextReason, consumableSuggestion, consumableReason,
                totalCost, affordableCost, alreadyOptimal);
    }

    public static BuildPlan buildPlan(HeroType heroType,
                                      ItemType weapon,
                                      ItemType armor,
                                      ItemType boots,
                                      ItemType hat,
                                      ItemType relic,
                                      int gold) {
        return buildPlan(heroType, null, weapon, armor, boots, hat, relic, gold, 1f, 1, DamageType.PHYSICAL);
    }

    public static String skillOrder(HeroType heroType) {
        if (heroType == null) {
            return "一技 → 二技 → 大招";
        }
        HeroArchetype archetype = heroType.archetype();
        if (archetype == HeroArchetype.ASSASSIN) {
            return "二技 → 一技 → 大招（突进留逃生）";
        }
        if (archetype == HeroArchetype.MAGE || archetype == HeroArchetype.SUPPORT) {
            return "一技 → 大招 → 二技（先消耗再爆发）";
        }
        if (archetype == HeroArchetype.TANK) {
            return "一技 → 二技 → 大招（先手开团）";
        }
        return "一技 → 二技 → 大招";
    }

    private static boolean applyHeroSpecificLoadout(HeroType heroType, List<ItemType> items) {
        switch (heroType) {
            case SUN_WUKONG:
                items.addAll(Arrays.asList(ItemType.RU_YI_JINGU_BANG, ItemType.QILIN_COAT,
                        ItemType.CLOUD_BOOTS, ItemType.PHOENIX_CROWN, ItemType.GOLDEN_PILL));
                return true;
            case HOU_YI:
            case DENG_CHAN_YU:
                items.addAll(Arrays.asList(ItemType.STAR_BOW, ItemType.JADE_BOOTS,
                        ItemType.BAGUA_ARMOR, ItemType.DRAGON_PEARL, ItemType.GOLDEN_PILL));
                return true;
            case NEZHA:
                items.addAll(Arrays.asList(ItemType.HUNTIAN_LING, ItemType.LOTUS_ROBE,
                        ItemType.FIRE_BOOTS, ItemType.LOTUS_CROWN, ItemType.GOLDEN_PILL));
                return true;
            case ERLANG_SHEN:
                items.addAll(Arrays.asList(ItemType.SANJIAN_LIANGREN, ItemType.DRAGON_SCALE_ROBE,
                        ItemType.WIND_BOOTS, ItemType.TAIBAI_CROWN, ItemType.GOLDEN_PILL));
                return true;
            case JIANG_ZIYA:
                items.addAll(Arrays.asList(ItemType.SOUL_CHAIN, ItemType.MOON_ROBE,
                        ItemType.THUNDER_BOOTS, ItemType.GOD_LIST_RELIC, ItemType.PEACH_PILL));
                return true;
            case GUANYIN:
            case NU_WA:
                items.addAll(Arrays.asList(ItemType.HEAVEN_SWORD, ItemType.LOTUS_ROBE,
                        ItemType.CLOUD_BOOTS, ItemType.LOTUS_CROWN, ItemType.PEACH_PILL));
                return true;
            case LEI_ZHENZI:
                items.addAll(Arrays.asList(ItemType.GOLDEN_SCISSORS, ItemType.BAGUA_CLOAK,
                        ItemType.THUNDER_BOOTS, ItemType.THUNDER_CROWN, ItemType.GOLDEN_PILL));
                return true;
            default:
                return false;
        }
    }

    private static int scorePurchase(HeroType heroType,
                                     HeroType enemyType,
                                     EquipmentSlot slot,
                                     ItemType item,
                                     int level,
                                     float hpRatio,
                                     DamageType damageType) {
        int score = 50;
        if (slot == EquipmentSlot.BOOTS) {
            score += 28;
        } else if (slot == EquipmentSlot.WEAPON) {
            score += 24;
        } else if (slot == EquipmentSlot.ARMOR) {
            score += 20;
        } else if (slot == EquipmentSlot.HAT) {
            score += 16;
        }

        if (level <= 4 && item.cost <= 220) {
            score += 18;
        } else if (level >= 10 && item.cost >= 320) {
            score += 12;
        }

        if (heroType != null) {
            HeroArchetype archetype = heroType.archetype();
            if (archetype == HeroArchetype.MARKSMAN && (item == ItemType.STAR_BOW || item.rangeBonus > 0f)) {
                score += 20;
            }
            if ((archetype == HeroArchetype.MAGE || archetype == HeroArchetype.SUPPORT)
                    && item.magicPowerBonus > 0f) {
                score += 18;
            }
            if (archetype == HeroArchetype.ASSASSIN && (item.attackSpeedBonusRate > 0f || item.speedBonus > 0f)) {
                score += 16;
            }
            if (archetype == HeroArchetype.TANK && item.damageReductionRate > 0f) {
                score += 18;
            }
            if (damageType == DamageType.MAGIC && item.magicDamageBonusRate > 0f) {
                score += 10;
            }
        }

        if (enemyType != null) {
            HeroArchetype enemyArchetype = enemyType.archetype();
            if (enemyArchetype == HeroArchetype.MAGE && item.damageReductionRate > 0f) {
                score += 22;
            }
            if (enemyArchetype == HeroArchetype.ASSASSIN && item.hpBonus >= 280f) {
                score += 20;
            }
            if (enemyArchetype == HeroArchetype.MARKSMAN && item.speedBonus >= 58f) {
                score += 16;
            }
            if (enemyType.basicAttackDamageType() == DamageType.MAGIC && item.magicDamageBonusRate > 0f) {
                score += 12;
            }
        }

        if (hpRatio < 0.45f && slot == EquipmentSlot.ARMOR) {
            score += 14;
        }

        return score;
    }

    private static String reasonFor(HeroType heroType,
                                    HeroType enemyType,
                                    ItemType item,
                                    int level,
                                    float hpRatio) {
        if (item.slot == EquipmentSlot.BOOTS) {
            return "先补鞋子，方便走位和回线";
        }
        if (item.slot == EquipmentSlot.WEAPON) {
            if (heroType != null && heroType.archetype() == HeroArchetype.MARKSMAN) {
                return "射手核心武器，提升射程和输出";
            }
            return "核心武器，提升清线和打架伤害";
        }
        if (item.slot == EquipmentSlot.ARMOR) {
            if (enemyType != null && enemyType.archetype() == HeroArchetype.MAGE) {
                return "对面法术伤害高，先出防装";
            }
            if (hpRatio < 0.45f) {
                return "血量偏低，先堆生存";
            }
            return "提升坦度，能扛住团战";
        }
        if (item.slot == EquipmentSlot.HAT) {
            if (heroType != null && heroType.basicAttackDamageType() == DamageType.MAGIC) {
                return "法系帽子，提升技能伤害";
            }
            return "补强属性，拉开装备差距";
        }
        if (level <= 4 && item.cost <= 200) {
            return "前期优先买便宜好用的装备";
        }
        return "按当前局势推荐的下一件装备";
    }

    private static ItemType equipped(EquipmentSlot slot,
                                     ItemType weapon,
                                     ItemType armor,
                                     ItemType boots,
                                     ItemType hat,
                                     ItemType relic) {
        switch (slot) {
            case WEAPON:
                return weapon;
            case ARMOR:
                return armor;
            case BOOTS:
                return boots;
            case HAT:
                return hat;
            case RELIC:
                return relic;
            default:
                return null;
        }
    }

    private static final class ScoredItem {
        final ItemType item;
        final int score;

        ScoredItem(ItemType item, int score) {
            this.item = item;
            this.score = score;
        }
    }
}
