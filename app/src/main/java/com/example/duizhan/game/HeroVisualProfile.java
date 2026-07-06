package com.example.duizhan.game;

import android.graphics.Color;

/**
 * Per-hero visual identity derived from myth source material.
 */
public final class HeroVisualProfile {
    public enum BodyKind {
        HUMAN_WARRIOR,
        HUMAN_MAGE,
        HUMAN_MONK,
        FEMALE_IMMORTAL,
        CHILD_WARRIOR,
        MONKEY_KING,
        PIG_DEMON,
        BULL_DEMON,
        SKELETON,
        GHOST_JUDGE,
        DRAGON_KING,
        GIANT,
        FOX_SPIRIT,
        BIRD_DEMON,
        SNAKE_DEMON,
        EMPEROR,
        BUDDHA,
        OX_DEMON,
        HORSE_DEMON
    }

    public enum SigWeapon {
        STAFF, GOLDEN_STAFF, SPEAR, FIRE_SPEAR, RAKE, SWORD, BOW, WHIP, PAGODA,
        LOTUS, AXE, CHAIN, FAN, GOURD, BOOK, BEADS, TRIDENT, HOOP, SEAL
    }

    public final BodyKind body;
    public final SigWeapon weapon;
    public final int robeColor;
    public final int trimColor;
    public final int skinColor;
    public final int hairColor;
    public final float scale;
    public final boolean crown;
    public final boolean thirdEye;
    public final boolean wings;

    private HeroVisualProfile(BodyKind body, SigWeapon weapon, int robe, int trim, int skin, int hair,
                              float scale, boolean crown, boolean thirdEye, boolean wings) {
        this.body = body;
        this.weapon = weapon;
        this.robeColor = robe;
        this.trimColor = trim;
        this.skinColor = skin;
        this.hairColor = hair;
        this.scale = scale;
        this.crown = crown;
        this.thirdEye = thirdEye;
        this.wings = wings;
    }

    public static HeroVisualProfile of(HeroType type) {
        if (type == null) {
            return fallback("战士", "天庭");
        }
        switch (type) {
            case SUN_WUKONG:
                return new HeroVisualProfile(BodyKind.MONKEY_KING, SigWeapon.GOLDEN_STAFF,
                        Color.rgb(220, 38, 38), Color.rgb(250, 204, 21),
                        Color.rgb(196, 132, 52), Color.rgb(55, 35, 20), 1.08f, false, false, false);
            case ERLANG_SHEN:
                return new HeroVisualProfile(BodyKind.HUMAN_WARRIOR, SigWeapon.TRIDENT,
                        Color.rgb(148, 163, 184), Color.rgb(226, 232, 240),
                        Color.rgb(254, 226, 196), Color.rgb(30, 41, 59), 1.05f, false, true, false);
            case NEZHA:
                return new HeroVisualProfile(BodyKind.CHILD_WARRIOR, SigWeapon.FIRE_SPEAR,
                        Color.rgb(239, 68, 68), Color.rgb(250, 204, 21),
                        Color.rgb(255, 214, 170), Color.rgb(30, 30, 30), 0.88f, false, false, false);
            case ZHU_BAJIE:
                return new HeroVisualProfile(BodyKind.PIG_DEMON, SigWeapon.RAKE,
                        Color.rgb(30, 41, 59), Color.rgb(100, 116, 139),
                        Color.rgb(252, 165, 165), Color.rgb(40, 30, 30), 1.12f, false, false, false);
            case SHA_WUJING:
                return new HeroVisualProfile(BodyKind.HUMAN_MONK, SigWeapon.BEADS,
                        Color.rgb(88, 28, 135), Color.rgb(196, 181, 253),
                        Color.rgb(120, 80, 60), Color.rgb(30, 30, 30), 1.02f, false, false, false);
            case TANG_SENG:
                return new HeroVisualProfile(BodyKind.HUMAN_MONK, SigWeapon.BEADS,
                        Color.rgb(250, 204, 21), Color.rgb(254, 240, 138),
                        Color.rgb(255, 224, 196), Color.rgb(20, 20, 20), 0.96f, false, false, false);
            case GUANYIN:
            case HE_XIAN_GU:
            case NU_WA:
                return new HeroVisualProfile(BodyKind.FEMALE_IMMORTAL, SigWeapon.LOTUS,
                        Color.rgb(248, 250, 252), Color.rgb(244, 114, 182),
                        Color.rgb(255, 228, 210), Color.rgb(20, 20, 30), 1.0f, true, false, false);
            case NIU_MO_WANG:
                return new HeroVisualProfile(BodyKind.BULL_DEMON, SigWeapon.AXE,
                        Color.rgb(67, 20, 7), Color.rgb(180, 83, 9),
                        Color.rgb(120, 70, 50), Color.rgb(30, 20, 10), 1.18f, false, true, false);
            case BAI_GU_JING:
                return new HeroVisualProfile(BodyKind.SKELETON, SigWeapon.CHAIN,
                        Color.rgb(248, 250, 252), Color.rgb(203, 213, 225),
                        Color.rgb(241, 245, 249), Color.rgb(200, 200, 210), 0.98f, false, false, false);
            case HONG_HAIER:
            case HEART_FIRE:
                return new HeroVisualProfile(BodyKind.CHILD_WARRIOR, SigWeapon.FAN,
                        Color.rgb(220, 38, 38), Color.rgb(251, 146, 60),
                        Color.rgb(255, 200, 170), Color.rgb(40, 20, 10), 0.9f, false, false, false);
            case LEI_ZHENZI:
                return new HeroVisualProfile(BodyKind.BIRD_DEMON, SigWeapon.AXE,
                        Color.rgb(30, 58, 138), Color.rgb(250, 204, 21),
                        Color.rgb(90, 70, 50), Color.rgb(20, 20, 30), 1.06f, false, false, true);
            case JU_LING_SHEN:
            case PAN_GU:
            case GONG_GONG:
            case LONG_XU_HU:
                return new HeroVisualProfile(BodyKind.GIANT, SigWeapon.AXE,
                        Color.rgb(71, 85, 105), Color.rgb(148, 163, 184),
                        Color.rgb(140, 100, 80), Color.rgb(40, 30, 20), 1.22f, false, false, false);
            case YAMA:
            case JUDGE_CUI:
                return new HeroVisualProfile(BodyKind.GHOST_JUDGE, SigWeapon.BOOK,
                        Color.rgb(30, 41, 59), Color.rgb(248, 113, 113),
                        Color.rgb(180, 160, 140), Color.rgb(20, 20, 20), 1.04f, true, false, false);
            case HOU_YI:
            case DENG_CHAN_YU:
            case PLEIADES:
            case ZHANG_GUOLAO:
                return new HeroVisualProfile(BodyKind.HUMAN_WARRIOR, SigWeapon.BOW,
                        Color.rgb(180, 83, 9), Color.rgb(250, 204, 21),
                        Color.rgb(255, 214, 170), Color.rgb(50, 35, 25), 1.0f, false, false, false);
            case JADE_EMPEROR:
            case QUEEN_MOTHER:
                return new HeroVisualProfile(BodyKind.EMPEROR, SigWeapon.SEAL,
                        Color.rgb(250, 204, 21), Color.rgb(220, 38, 38),
                        Color.rgb(255, 224, 196), Color.rgb(30, 20, 10), 1.08f, true, false, false);
            case BUDDHA:
                return new HeroVisualProfile(BodyKind.BUDDHA, SigWeapon.SEAL,
                        Color.rgb(250, 204, 21), Color.rgb(234, 179, 8),
                        Color.rgb(255, 220, 170), Color.rgb(10, 10, 10), 1.15f, false, false, false);
            case NAO_HAI_LONG:
            case WEST_DRAGON:
            case SOUTH_DRAGON:
                return new HeroVisualProfile(BodyKind.DRAGON_KING, SigWeapon.TRIDENT,
                        Color.rgb(37, 99, 235), Color.rgb(125, 211, 252),
                        Color.rgb(100, 160, 200), Color.rgb(20, 60, 120), 1.1f, true, false, false);
            case DA_JI:
            case NINE_HEAD:
                return new HeroVisualProfile(BodyKind.FOX_SPIRIT, SigWeapon.FAN,
                        Color.rgb(157, 23, 77), Color.rgb(251, 207, 232),
                        Color.rgb(255, 214, 200), Color.rgb(120, 20, 80), 0.98f, false, false, false);
            case BLACK_WHITE:
            case OX_HEAD:
                return new HeroVisualProfile(BodyKind.OX_DEMON, SigWeapon.CHAIN,
                        Color.rgb(55, 48, 163), Color.rgb(165, 180, 252),
                        Color.rgb(100, 80, 70), Color.rgb(20, 20, 30), 1.05f, true, false, false);
            case HORSE_FACE:
                return new HeroVisualProfile(BodyKind.HORSE_DEMON, SigWeapon.SPEAR,
                        Color.rgb(30, 41, 59), Color.rgb(148, 163, 184),
                        Color.rgb(90, 70, 60), Color.rgb(20, 20, 20), 1.0f, false, false, false);
            case JIANG_ZIYA:
            case XUAN_NU:
            case FU_XI:
                return new HeroVisualProfile(BodyKind.HUMAN_MAGE, SigWeapon.BOOK,
                        Color.rgb(71, 85, 105), Color.rgb(250, 204, 21),
                        Color.rgb(255, 224, 196), Color.rgb(180, 160, 120), 1.02f, false, false, false);
            case PAGODA_KING:
                return new HeroVisualProfile(BodyKind.HUMAN_WARRIOR, SigWeapon.PAGODA,
                        Color.rgb(30, 64, 175), Color.rgb(250, 204, 21),
                        Color.rgb(255, 214, 170), Color.rgb(40, 30, 20), 1.04f, true, false, false);
            case LI_TIEGUAI:
                return new HeroVisualProfile(BodyKind.HUMAN_MAGE, SigWeapon.GOURD,
                        Color.rgb(22, 101, 52), Color.rgb(134, 239, 172),
                        Color.rgb(200, 160, 120), Color.rgb(60, 40, 20), 1.0f, false, false, false);
            case LU_DONG_BIN:
                return new HeroVisualProfile(BodyKind.HUMAN_WARRIOR, SigWeapon.SWORD,
                        Color.rgb(30, 58, 138), Color.rgb(191, 219, 254),
                        Color.rgb(255, 214, 170), Color.rgb(30, 30, 40), 1.02f, false, false, false);
            case BI_XIAO:
            case JING_WEI:
            case TU_XING_SUN:
                return new HeroVisualProfile(BodyKind.CHILD_WARRIOR, SigWeapon.SWORD,
                        Color.rgb(79, 70, 229), Color.rgb(196, 181, 253),
                        Color.rgb(255, 210, 180), Color.rgb(40, 30, 50), 0.9f, false, false, false);
            case XING_TIAN:
            case CHI_YOU:
                return new HeroVisualProfile(BodyKind.GIANT, SigWeapon.AXE,
                        Color.rgb(127, 29, 29), Color.rgb(248, 113, 113),
                        Color.rgb(140, 90, 70), Color.rgb(30, 10, 10), 1.12f, false, false, false);
            default:
                return fromFaction(type);
        }
    }

    private static HeroVisualProfile fromFaction(HeroType type) {
        String faction = type.faction == null ? "" : type.faction;
        String role = type.role == null ? "" : type.role;
        if (faction.contains("佛") || type == HeroType.MANJUSRI || type == HeroType.DI_TING) {
            return new HeroVisualProfile(BodyKind.BUDDHA, SigWeapon.SEAL,
                    Color.rgb(250, 204, 21), Color.rgb(234, 179, 8),
                    Color.rgb(255, 220, 170), Color.rgb(10, 10, 10), 1.05f, false, false, false);
        }
        if (faction.contains("地府") || faction.contains("鬼")) {
            return new HeroVisualProfile(BodyKind.GHOST_JUDGE, SigWeapon.CHAIN,
                    Color.rgb(30, 41, 59), Color.rgb(74, 222, 128),
                    Color.rgb(160, 180, 160), Color.rgb(20, 20, 20), 1.0f, false, false, false);
        }
        if (faction.contains("龙") || faction.contains("海")) {
            return new HeroVisualProfile(BodyKind.DRAGON_KING, SigWeapon.TRIDENT,
                    Color.rgb(29, 78, 216), Color.rgb(147, 197, 253),
                    Color.rgb(110, 150, 190), Color.rgb(15, 40, 90), 1.06f, false, false, false);
        }
        if (faction.contains("妖") || faction.contains("魔") || faction.contains("西游") && role.contains("妖")) {
            return new HeroVisualProfile(BodyKind.SNAKE_DEMON, SigWeapon.SPEAR,
                    Color.rgb(88, 28, 135), Color.rgb(216, 180, 254),
                    Color.rgb(180, 140, 110), Color.rgb(50, 20, 60), 1.02f, true, false, false);
        }
        if (role.contains("辅助") || role.contains("军师")) {
            return new HeroVisualProfile(BodyKind.FEMALE_IMMORTAL, SigWeapon.LOTUS,
                    Color.rgb(236, 254, 255), Color.rgb(34, 197, 94),
                    Color.rgb(255, 228, 210), Color.rgb(60, 40, 30), 0.98f, false, false, false);
        }
        if (role.contains("法")) {
            return new HeroVisualProfile(BodyKind.HUMAN_MAGE, SigWeapon.BOOK,
                    Color.rgb(67, 56, 202), Color.rgb(196, 181, 253),
                    Color.rgb(255, 224, 196), Color.rgb(50, 40, 70), 1.0f, false, false, false);
        }
        if (role.contains("射")) {
            return new HeroVisualProfile(BodyKind.HUMAN_WARRIOR, SigWeapon.BOW,
                    Color.rgb(120, 53, 15), Color.rgb(251, 191, 36),
                    Color.rgb(255, 214, 170), Color.rgb(50, 35, 25), 1.0f, false, false, false);
        }
        if (role.contains("坦")) {
            return new HeroVisualProfile(BodyKind.GIANT, SigWeapon.AXE,
                    Color.rgb(71, 85, 105), Color.rgb(203, 213, 225),
                    Color.rgb(200, 160, 130), Color.rgb(40, 30, 20), 1.1f, false, false, false);
        }
        if (role.contains("刺")) {
            return new HeroVisualProfile(BodyKind.HUMAN_WARRIOR, SigWeapon.SWORD,
                    Color.rgb(55, 48, 163), Color.rgb(165, 180, 252),
                    Color.rgb(255, 210, 180), Color.rgb(30, 25, 40), 0.95f, false, false, false);
        }
        return fallback(role, faction);
    }

    private static HeroVisualProfile fallback(String role, String faction) {
        int robe = faction.contains("天庭") || faction.contains("封神")
                ? Color.rgb(37, 99, 235) : Color.rgb(185, 28, 28);
        int trim = Color.rgb(250, 204, 21);
        return new HeroVisualProfile(BodyKind.HUMAN_WARRIOR, SigWeapon.SWORD,
                robe, trim, Color.rgb(255, 214, 170), Color.rgb(45, 35, 30), 1.0f, false, false, false);
    }
}
