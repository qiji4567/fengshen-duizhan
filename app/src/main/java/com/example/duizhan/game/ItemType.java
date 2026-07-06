package com.example.duizhan.game;

public enum ItemType {
    DEMON_BLADE("斩妖剑", EquipmentSlot.WEAPON, 160, "武器  攻击+24  伤害+8%", 24f, 0f, 0f, 0f, 0.08f, 0f, 0f, 0f, 0f, 0f, 0f, 0f),
    RU_YI_JINGU_BANG("如意金箍棒", EquipmentSlot.WEAPON, 420, "武器  攻击+64  攻速+16%  伤害+12%", 64f, 0f, 0f, 0f, 0.12f, 0f, 0f, 0f, 0.16f, 0f, 0f, 0f),
    SANJIAN_LIANGREN("三尖两刃刀", EquipmentSlot.WEAPON, 380, "武器  攻击+58  射程+34  攻速+10%", 58f, 0f, 0f, 0f, 0.06f, 0f, 0f, 0f, 0.10f, 34f, 0f, 0f),
    DRAGON_SPEAR("定海龙枪", EquipmentSlot.WEAPON, 300, "武器  攻击+42  射程+46  伤害+10%", 42f, 0f, 0f, 0f, 0.10f, 0f, 0f, 0f, 0f, 46f, 0f, 0f),
    HEAVEN_SWORD("青萍剑", EquipmentSlot.WEAPON, 360, "武器  攻击+50  冷却-8%  吸血+5%", 50f, 0f, 0f, 0f, 0.06f, 0f, 0f, 0.08f, 0f, 0f, 0.05f, 0f),
    WHITE_TIGER_CLAW("白虎爪", EquipmentSlot.WEAPON, 340, "武器  攻击+44  攻速+24%  吸血+6%", 44f, 0f, 0f, 0f, 0.05f, 0f, 0f, 0f, 0.24f, 0f, 0.06f, 0f),
    HUNTIAN_LING("混天绫", EquipmentSlot.WEAPON, 260, "武器  攻击+30  移速+18  冷却-6%", 30f, 0f, 0f, 18f, 0.06f, 0f, 0f, 0.06f, 0f, 0f, 0f, 0f),
    GOLDEN_SCISSORS("金蛟剪", EquipmentSlot.WEAPON, 460, "武器  攻击+70  伤害+16%  吸血+4%", 70f, 0f, 0f, 0f, 0.16f, 0f, 0f, 0f, 0f, 0f, 0.04f, 0f),
    STAR_BOW("落星弓", EquipmentSlot.WEAPON, 320, "武器  攻击+36  射程+78  攻速+18%", 36f, 0f, 0f, 0f, 0.04f, 0f, 0f, 0f, 0.18f, 78f, 0f, 0f),
    SOUL_CHAIN("锁魂链", EquipmentSlot.WEAPON, 310, "武器  攻击+38  法强+30  冷却-8%", 38f, 30f, 0f, 0f, 0.04f, 0.08f, 0f, 0.08f, 0f, 20f, 0f, 0f),

    BAGUA_ARMOR("八卦护心衣", EquipmentSlot.ARMOR, 150, "衣服  生命+240  减伤+10%", 0f, 0f, 240f, 0f, 0f, 0f, 0.10f, 0f, 0f, 0f, 0f, 0f),
    XUANWU_ARMOR("玄武战衣", EquipmentSlot.ARMOR, 280, "衣服  生命+440  减伤+18%", 0f, 0f, 440f, 0f, 0f, 0f, 0.18f, 0f, 0f, 0f, 0f, 0f),
    LOTUS_ROBE("莲花宝衣", EquipmentSlot.ARMOR, 240, "衣服  生命+300  冷却-8%  减伤+8%", 0f, 0f, 300f, 0f, 0f, 0f, 0.08f, 0.08f, 0f, 0f, 0f, 0f),
    DRAGON_SCALE_ROBE("龙鳞战袍", EquipmentSlot.ARMOR, 340, "衣服  生命+520  减伤+14%  移速+14", 0f, 0f, 520f, 14f, 0f, 0f, 0.14f, 0f, 0f, 0f, 0f, 0f),
    BAGUA_CLOAK("八卦仙衣", EquipmentSlot.ARMOR, 300, "衣服  生命+360  法强+28  减伤+10%", 0f, 28f, 360f, 0f, 0f, 0.08f, 0.10f, 0f, 0f, 0f, 0f, 0f),
    TIGER_MAIL("白虎锁子衣", EquipmentSlot.ARMOR, 260, "衣服  生命+280  攻击+18  吸血+4%", 18f, 0f, 280f, 0f, 0.03f, 0f, 0.08f, 0f, 0f, 0f, 0.04f, 0f),
    QILIN_COAT("麒麟护身衣", EquipmentSlot.ARMOR, 380, "衣服  生命+560  减伤+16%  冷却-6%", 0f, 0f, 560f, 0f, 0f, 0f, 0.16f, 0.06f, 0f, 0f, 0f, 0f),
    MOON_ROBE("广寒月衣", EquipmentSlot.ARMOR, 320, "衣服  生命+330  法强+40  冷却-10%", 0f, 40f, 330f, 0f, 0f, 0.12f, 0.06f, 0.10f, 0f, 0f, 0f, 0f),

    FIRE_BOOTS("风火靴", EquipmentSlot.BOOTS, 120, "鞋子  移速+46  冷却-6%", 0f, 0f, 0f, 46f, 0f, 0f, 0f, 0.06f, 0f, 0f, 0f, 0f),
    CLOUD_BOOTS("筋斗云履", EquipmentSlot.BOOTS, 220, "鞋子  移速+72  冷却-10%", 0f, 0f, 0f, 72f, 0f, 0f, 0f, 0.10f, 0f, 0f, 0f, 0f),
    WIND_BOOTS("追风履", EquipmentSlot.BOOTS, 180, "鞋子  移速+58  攻速+10%", 0f, 0f, 0f, 58f, 0f, 0f, 0f, 0f, 0.10f, 0f, 0f, 0f),
    JADE_BOOTS("踏云玉履", EquipmentSlot.BOOTS, 240, "鞋子  移速+68  射程+28  冷却-6%", 0f, 0f, 0f, 68f, 0f, 0f, 0f, 0.06f, 0f, 28f, 0f, 0f),
    THUNDER_BOOTS("雷行靴", EquipmentSlot.BOOTS, 260, "鞋子  移速+64  法强+24  冷却-8%", 0f, 24f, 0f, 64f, 0f, 0.08f, 0f, 0.08f, 0f, 0f, 0f, 0f),
    BLOOD_BOOTS("血影靴", EquipmentSlot.BOOTS, 260, "鞋子  移速+60  攻击+18  吸血+5%", 18f, 0f, 0f, 60f, 0.03f, 0f, 0f, 0f, 0f, 0f, 0.05f, 0f),

    PHOENIX_CROWN("凤翅紫金冠", EquipmentSlot.HAT, 360, "帽子  法强+68  法伤+16%", 0f, 68f, 0f, 0f, 0f, 0.16f, 0f, 0f, 0f, 0f, 0f, 0f),
    LOTUS_CROWN("莲花冠", EquipmentSlot.HAT, 240, "帽子  法强+38  生命+180  冷却-8%", 0f, 38f, 180f, 0f, 0f, 0.10f, 0f, 0.08f, 0f, 0f, 0f, 0f),
    TAIBAI_CROWN("太白星冠", EquipmentSlot.HAT, 300, "帽子  法强+48  射程+42  冷却-10%", 0f, 48f, 0f, 0f, 0f, 0.12f, 0f, 0.10f, 0f, 42f, 0f, 0f),
    THUNDER_CROWN("雷部金冠", EquipmentSlot.HAT, 340, "帽子  法强+56  法伤+12%  冷却-8%", 0f, 56f, 0f, 0f, 0f, 0.12f, 0f, 0.08f, 0f, 0f, 0f, 0f),
    MOON_CROWN("月华冠", EquipmentSlot.HAT, 280, "帽子  法强+44  生命+220  法伤+8%", 0f, 44f, 220f, 0f, 0f, 0.08f, 0.04f, 0f, 0f, 0f, 0f, 0f),
    BONE_CROWN("白骨冠", EquipmentSlot.HAT, 320, "帽子  法强+52  吸血+6%  冷却-6%", 0f, 52f, 0f, 0f, 0f, 0.10f, 0f, 0.06f, 0f, 0f, 0.06f, 0f),
    GOD_LIST_RELIC("封神榜残卷", EquipmentSlot.HAT, 260, "帽子  伤害+12%  法伤+10%  冷却-8%", 0f, 30f, 0f, 0f, 0.12f, 0.10f, 0f, 0.08f, 0f, 0f, 0f, 0f),
    DRAGON_PEARL("定海珠", EquipmentSlot.HAT, 420, "帽子  法强+60  射程+62  冷却-12%", 0f, 60f, 0f, 0f, 0f, 0.14f, 0f, 0.12f, 0f, 62f, 0f, 0f),

    GOLDEN_PILL("九转金丹", EquipmentSlot.CONSUMABLE, 90, "丹药  立即回复300", 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 300f),
    PEACH_PILL("蟠桃", EquipmentSlot.CONSUMABLE, 150, "丹药  立即回复520", 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 520f),
    GINSENG_FRUIT("人参果", EquipmentSlot.CONSUMABLE, 220, "丹药  立即回复780", 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 780f);

    public final String label;
    public final EquipmentSlot slot;
    public final int cost;
    public final String description;
    public final float attackBonus;
    public final float magicPowerBonus;
    public final float hpBonus;
    public final float speedBonus;
    public final float damageBonusRate;
    public final float magicDamageBonusRate;
    public final float damageReductionRate;
    public final float cooldownReductionRate;
    public final float attackSpeedBonusRate;
    public final float rangeBonus;
    public final float lifeStealRate;
    public final float healAmount;

    ItemType(String label, EquipmentSlot slot, int cost, String description,
             float attackBonus, float magicPowerBonus, float hpBonus, float speedBonus,
             float damageBonusRate, float magicDamageBonusRate, float damageReductionRate,
             float cooldownReductionRate, float attackSpeedBonusRate, float rangeBonus,
             float lifeStealRate, float healAmount) {
        this.label = label;
        this.slot = slot;
        this.cost = cost;
        this.description = description;
        this.attackBonus = attackBonus;
        this.magicPowerBonus = magicPowerBonus;
        this.hpBonus = hpBonus;
        this.speedBonus = speedBonus;
        this.damageBonusRate = damageBonusRate;
        this.magicDamageBonusRate = magicDamageBonusRate;
        this.damageReductionRate = damageReductionRate;
        this.cooldownReductionRate = cooldownReductionRate;
        this.attackSpeedBonusRate = attackSpeedBonusRate;
        this.rangeBonus = rangeBonus;
        this.lifeStealRate = lifeStealRate;
        this.healAmount = healAmount;
    }
}
