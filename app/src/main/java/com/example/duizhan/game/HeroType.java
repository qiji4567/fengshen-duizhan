package com.example.duizhan.game;

public enum HeroType {
    SUN_WUKONG("齐天大圣·孙悟空", "西游", "斗战", "如意金箍：突进翻墙横扫", 850, 92, 120, 285, 5.5f, SkillStyle.DASH_SWEEP),
    ERLANG_SHEN("二郎真君·杨戬", "封神正神", "战士", "天眼锁魂：直线神光贯穿敌阵", 820, 96, 165, 255, 6.5f, SkillStyle.EYE_BEAM),
    NEZHA("三坛海会·哪吒", "封神正神", "刺客", "风火轮：高速冲锋并留下火焰", 730, 84, 135, 330, 5.0f, SkillStyle.FIRE_WHEEL),
    LEI_ZHENZI("雷部正神·雷震子", "雷部", "法师", "雷霆万钧：连锁雷击多个目标", 680, 105, 230, 250, 7.5f, SkillStyle.THUNDER_CHAIN),
    JU_LING_SHEN("巨灵神", "天庭", "坦克", "开山斧：震地击退周围敌人", 1120, 72, 105, 220, 7.2f, SkillStyle.GIANT_STOMP),
    CHANGE("月宫嫦娥", "月宫", "法师", "月华凝霜：范围减速并造成伤害", 660, 98, 235, 245, 7.0f, SkillStyle.MOON_FROST),
    NIU_MO_WANG("平天大圣·牛魔王", "西游", "坦克", "混铁裂地：扇形重击兵线", 1150, 82, 115, 225, 7.8f, SkillStyle.EARTH_SPLIT),
    BAI_GU_JING("白骨夫人", "西游", "法刺", "白骨迷阵：困住附近敌人", 640, 108, 215, 260, 7.0f, SkillStyle.BONE_TRAP),
    JIANG_ZIYA("姜子牙", "封神", "军师", "封神榜：召唤天书轰击目标", 700, 96, 245, 235, 8.0f, SkillStyle.GOD_LIST),
    TAIYI("太乙真人", "阐教", "法师", "九龙神火罩：持续灼烧范围敌人", 720, 100, 210, 240, 8.0f, SkillStyle.FIRE_CAGE),
    PAGODA_KING("托塔天王·李靖", "天庭", "射手", "玲珑宝塔：镇压并削弱目标", 840, 86, 190, 235, 6.8f, SkillStyle.PAGODA_SEAL),
    GUANYIN("观音菩萨", "西游", "辅助", "净瓶甘露：治疗自身并净化战场", 780, 74, 205, 245, 8.8f, SkillStyle.HEALING_RAIN),
    WEN_ZHONG("雷声普化·闻仲", "雷部正神", "法战", "雌雄双鞭：连锁雷击多个目标", 860, 94, 185, 245, 7.2f, SkillStyle.THUNDER_CHAIN),
    ZHAO_GONGMING("玄坛真君·赵公明", "财神正神", "战士", "定海珠：飞珠爆裂溅射", 900, 90, 175, 250, 7.0f, SkillStyle.GOD_LIST),
    YUN_XIAO("云霄娘娘", "三霄", "法师", "混元金斗：范围镇压敌人", 690, 104, 230, 242, 8.0f, SkillStyle.PAGODA_SEAL),
    QIONG_XIAO("琼霄娘娘", "三霄", "法师", "碧霄雷网：范围减速伤害", 670, 102, 225, 250, 7.4f, SkillStyle.MOON_FROST),
    BI_XIAO("碧霄娘娘", "三霄", "刺客", "金蛟剪：突进横扫", 700, 96, 135, 300, 6.0f, SkillStyle.DASH_SWEEP),
    DOU_MU("斗姆元君", "群星正神", "法师", "星斗照命：神光贯穿敌阵", 720, 102, 240, 235, 8.0f, SkillStyle.EYE_BEAM),
    TAI_BAI("太白金星", "天庭", "辅助", "太白星辉：治疗并护身", 720, 70, 215, 250, 8.5f, SkillStyle.HEALING_RAIN),
    ZHEN_YUAN("镇元大仙", "西游", "坦法", "袖里乾坤：困住附近敌人", 960, 82, 175, 225, 8.0f, SkillStyle.BONE_TRAP),
    ZHU_BAJIE("天蓬元帅·猪八戒", "西游", "坦克", "九齿钉耙：震地击退周围敌人", 1180, 78, 105, 215, 7.5f, SkillStyle.GIANT_STOMP),
    SHA_WUJING("卷帘大将·沙悟净", "西游", "战士", "降妖宝杖：直线重击", 920, 86, 150, 240, 6.8f, SkillStyle.EARTH_SPLIT),
    TANG_SENG("旃檀功德佛·唐僧", "西游", "辅助", "紧箍梵音：治疗并净化战场", 760, 62, 205, 240, 9.0f, SkillStyle.HEALING_RAIN),
    HONG_HAIER("圣婴大王·红孩儿", "西游", "法师", "三昧真火：持续灼烧范围敌人", 650, 112, 220, 260, 7.2f, SkillStyle.FIRE_CAGE),
    TIE_SHAN("铁扇公主", "西游", "法师", "芭蕉风月：范围减速并造成伤害", 720, 92, 235, 245, 7.0f, SkillStyle.MOON_FROST),
    YU_TU("月宫玉兔", "月宫", "刺客", "捣药疾行：高速冲锋并回复", 650, 88, 130, 340, 5.4f, SkillStyle.FIRE_WHEEL),
    KUIMU_LANG("奎木狼", "二十八宿", "战士", "星宿狼牙：突进横扫一圈", 860, 92, 130, 275, 6.3f, SkillStyle.DASH_SWEEP),
    KANG_JIN_LONG("亢金龙", "二十八宿", "法战", "龙吟金光：直线神光贯穿", 880, 88, 185, 255, 7.0f, SkillStyle.EYE_BEAM),
    JIAO_MU_JIAO("角木蛟", "二十八宿", "坦法", "蛟影盘缠：困住附近敌人", 940, 82, 170, 245, 7.2f, SkillStyle.BONE_TRAP),
    NAO_HAI_LONG("东海龙王", "四海龙王", "法师", "沧海龙吟：范围减速伤害", 790, 96, 235, 235, 7.5f, SkillStyle.MOON_FROST),
    YAMA("阎罗王", "地府", "法师", "生死簿：天书轰击目标", 820, 98, 225, 230, 8.0f, SkillStyle.GOD_LIST),
    BLACK_WHITE("黑白无常", "地府", "刺客", "锁魂链：镇压并削弱目标", 700, 104, 150, 300, 6.5f, SkillStyle.PAGODA_SEAL),
    MAZU("天后妈祖", "海神", "辅助", "护海灵灯：治疗自身并护盾", 800, 72, 205, 245, 8.5f, SkillStyle.HEALING_RAIN),
    XUAN_WU("真武大帝", "北极正神", "坦克", "玄武镇岳：震地并眩晕", 1200, 76, 110, 215, 8.0f, SkillStyle.GIANT_STOMP),
    QING_LONG("青龙孟章神君", "四象", "战士", "青龙裂空：直线重击敌阵", 960, 88, 170, 255, 7.0f, SkillStyle.EARTH_SPLIT),
    BAI_HU("白虎监兵神君", "四象", "刺客", "白虎扑杀：高速冲锋", 820, 106, 125, 320, 5.8f, SkillStyle.FIRE_WHEEL),
    ZHU_QUE("朱雀陵光神君", "四象", "法师", "朱雀神火：范围持续灼烧", 760, 104, 225, 250, 7.6f, SkillStyle.FIRE_CAGE),
    XUAN_NU("九天玄女", "天庭", "军师", "玄女兵法：召来天书轰击", 740, 96, 245, 250, 7.8f, SkillStyle.GOD_LIST),
    LU_DONG_BIN("吕洞宾", "八仙", "剑仙", "纯阳剑气：光束贯穿敌阵", 780, 100, 200, 280, 6.6f, SkillStyle.EYE_BEAM),
    HE_XIAN_GU("何仙姑", "八仙", "辅助", "莲花甘露：治疗并护身", 700, 74, 215, 255, 8.6f, SkillStyle.HEALING_RAIN),
    HAN_ZHONGLI("汉钟离", "八仙", "坦法", "芭蕉宝扇：范围减速伤害", 960, 82, 180, 225, 7.4f, SkillStyle.MOON_FROST),
    LAN_CAIHE("蓝采和", "八仙", "法师", "花篮迷阵：困住敌人", 680, 98, 220, 260, 7.2f, SkillStyle.BONE_TRAP),
    CAO_GUOJIU("曹国舅", "八仙", "战士", "玉板震击：震地击退", 900, 84, 125, 250, 7.0f, SkillStyle.GIANT_STOMP),
    LI_TIEGUAI("铁拐李", "八仙", "坦克", "葫芦真火：范围灼烧", 1080, 78, 150, 215, 7.8f, SkillStyle.FIRE_CAGE),
    ZHANG_GUOLAO("张果老", "八仙", "射手", "倒骑神术：飞弹溅射", 720, 94, 235, 265, 7.0f, SkillStyle.GOD_LIST),

    // 封神榜
    DA_JI("妲己", "封神", "法刺", "狐媚惑心：魅惑并削弱敌人", 620, 112, 210, 270, 6.8f, SkillStyle.YIN_YANG_ORB),
    SHEN_GONGBAO("申公豹", "封神", "法师", "地裂咒：冥河怒涛席卷", 700, 108, 225, 248, 7.2f, SkillStyle.NETHER_WAVE),
    BI_GAN("比干", "封神", "辅助", "忠魂护佑：莲华结界守护", 740, 68, 215, 240, 8.8f, SkillStyle.LOTUS_BARRIER),
    HUANG_FEI_HU("武成王·黄飞虎", "封神", "战士", "武成破军：风刃裂空", 880, 94, 155, 262, 6.6f, SkillStyle.WIND_BLADE),
    DENG_CHAN_YU("邓婵玉", "封神", "射手", "五彩石：飞星坠击", 700, 96, 240, 268, 6.4f, SkillStyle.STARFALL),
    TU_XING_SUN("土行孙", "封神", "刺客", "地行遁：阴阳法球突进", 660, 102, 128, 325, 5.6f, SkillStyle.YIN_YANG_ORB),
    MU_ZHA("木吒", "封神", "战士", "木叉护法：灵链锁敌", 840, 90, 148, 258, 6.8f, SkillStyle.SPIRIT_CHAIN),
    JIN_ZHA("金吒", "封神", "坦克", "护体金莲：镇岳震地", 1140, 74, 108, 218, 7.6f, SkillStyle.LOTUS_BARRIER),
    LONG_XU_HU("龙须虎", "封神", "坦克", "巨石轰击：开山震地", 1100, 80, 112, 215, 7.4f, SkillStyle.GIANT_STOMP),
    WEN_SHU("文殊广法天尊", "阐教", "法师", "慧剑斩魔：神光贯穿", 710, 104, 232, 242, 7.8f, SkillStyle.EYE_BEAM),
    PU_XIAN("普贤真人", "阐教", "辅助", "慈航普度：甘露治愈", 770, 70, 210, 246, 8.6f, SkillStyle.HEALING_RAIN),

    // 上古 / 山海
    NU_WA("女娲娘娘", "上古正神", "辅助", "补天圣光：莲华结界", 800, 78, 220, 245, 8.5f, SkillStyle.LOTUS_BARRIER),
    FU_XI("伏羲大帝", "上古正神", "军师", "八卦推演：阴阳法球", 750, 98, 240, 238, 7.8f, SkillStyle.YIN_YANG_ORB),
    PAN_GU("盘古", "上古正神", "坦克", "开天辟地：山河崩裂", 1250, 88, 118, 210, 8.2f, SkillStyle.EARTH_SPLIT),
    HOU_YI("后羿", "上古神射", "射手", "落日神箭：飞星坠击", 720, 100, 250, 272, 6.2f, SkillStyle.STARFALL),
    CHANG_E_NEW("嫦娥仙子", "月宫", "法师", "广寒神箭：月华凝霜", 680, 102, 238, 250, 7.0f, SkillStyle.MOON_FROST),
    KUA_FU("夸父", "山海经", "战士", "逐日狂奔：风火冲阵", 920, 92, 140, 295, 6.0f, SkillStyle.FIRE_WHEEL),
    JING_WEI("精卫", "山海经", "刺客", "衔石复仇：风刃裂空", 640, 106, 132, 318, 5.8f, SkillStyle.WIND_BLADE),
    XING_TIAN("刑天", "山海经", "战士", "不屈战魂：突进横扫", 950, 96, 135, 268, 6.4f, SkillStyle.DASH_SWEEP),
    CHI_YOU("蚩尤", "上古魔神", "战士", "兵主怒斩：冥河怒涛", 980, 100, 162, 255, 7.0f, SkillStyle.NETHER_WAVE),
    GONG_GONG("共工", "上古水神", "坦克", "怒触不周：震地眩晕", 1180, 76, 110, 220, 7.8f, SkillStyle.GIANT_STOMP),
    ZHU_RONG("祝融", "上古火神", "法师", "焚天烈焰：浴火重生", 740, 110, 228, 252, 7.2f, SkillStyle.PHOENIX_FLAME),

    // 天庭 / 佛门
    JADE_EMPEROR("玉皇大帝", "天庭", "军师", "天威敕令：封神榜轰击", 820, 94, 245, 235, 8.0f, SkillStyle.GOD_LIST),
    QUEEN_MOTHER("王母娘娘", "天庭", "辅助", "蟠桃圣光：甘露治愈", 790, 72, 215, 242, 8.8f, SkillStyle.HEALING_RAIN),
    BUDDHA("如来佛祖", "佛门", "坦法", "五指山：宝塔镇压", 1050, 86, 175, 228, 8.5f, SkillStyle.PAGODA_SEAL),
    DI_TING("谛听", "佛门", "法战", "听音辨位：灵链锁敌", 860, 92, 188, 248, 7.0f, SkillStyle.SPIRIT_CHAIN),
    MANJUSRI("文殊菩萨", "佛门", "法师", "智慧佛光：神光贯穿", 730, 106, 235, 240, 7.6f, SkillStyle.EYE_BEAM),

    // 西游妖魔
    GOLD_HORN("金角大王", "西游", "法师", "紫金葫芦：困敌法阵", 700, 104, 218, 255, 7.2f, SkillStyle.BONE_TRAP),
    SILVER_HORN("银角大王", "西游", "法刺", "七星剑：阴阳法球", 680, 108, 205, 265, 6.8f, SkillStyle.YIN_YANG_ORB),
    BLUE_BULL("青牛精", "西游", "坦克", "金刚琢：镇岳震地", 1160, 78, 108, 218, 7.6f, SkillStyle.GIANT_STOMP),
    NINE_HEAD("九头虫", "西游", "刺客", "九首连击：风火冲阵", 750, 100, 128, 322, 5.6f, SkillStyle.FIRE_WHEEL),
    GOLD_ROPE("黄袍怪", "西游", "战士", "捆仙索：灵链锁敌", 870, 90, 150, 260, 6.8f, SkillStyle.SPIRIT_CHAIN),

    // 地府
    MENG_PO("孟婆", "地府", "辅助", "忘川汤：莲华结界", 760, 70, 208, 244, 8.6f, SkillStyle.LOTUS_BARRIER),
    JUDGE_CUI("崔判官", "地府", "法师", "勾魂判笔：天书轰击", 810, 100, 228, 232, 7.8f, SkillStyle.GOD_LIST),
    OX_HEAD("牛头", "地府", "坦克", "冥府冲撞：开山震地", 1120, 76, 106, 216, 7.4f, SkillStyle.GIANT_STOMP),
    HORSE_FACE("马面", "地府", "刺客", "勾魂索命：匿影突进", 690, 104, 130, 310, 5.8f, SkillStyle.WIND_BLADE),

    // 四海 / 星宿
    WEST_DRAGON("西海龙王", "四海龙王", "法师", "冰霜龙息：月华凝霜", 780, 98, 232, 240, 7.4f, SkillStyle.MOON_FROST),
    SOUTH_DRAGON("南海龙王", "四海龙王", "法战", "海啸怒涛：冥河怒涛", 820, 94, 195, 248, 7.2f, SkillStyle.NETHER_WAVE),
    HEART_FIRE("心月狐", "二十八宿", "法刺", "心焰灼灼：浴火重生", 670, 110, 212, 262, 6.8f, SkillStyle.PHOENIX_FLAME),
    ROOM_FIRE("室火猪", "二十八宿", "战士", "烈焰冲锋：风火冲阵", 900, 92, 142, 278, 6.2f, SkillStyle.FIRE_WHEEL),
    PLEIADES("昴日鸡", "二十八宿", "射手", "日出神光：飞星坠击", 710, 98, 242, 270, 6.4f, SkillStyle.STARFALL);

    public final String label;
    public final String faction;
    public final String role;
    public final String skillDescription;
    public final float hp;
    public final float attack;
    public final float range;
    public final float speed;
    public final float skillCooldown;
    public final SkillStyle skillStyle;
    public final SkillStyle secondaryStyle;
    public final SkillStyle ultimateStyle;

    HeroType(String label, String faction, String role, String skillDescription,
             float hp, float attack, float range, float speed, float skillCooldown, SkillStyle skillStyle) {
        this(label, faction, role, skillDescription, hp, attack, range, speed, skillCooldown,
                skillStyle, pairedSecondary(skillStyle), pairedUltimate(skillStyle));
    }

    HeroType(String label, String faction, String role, String skillDescription,
             float hp, float attack, float range, float speed, float skillCooldown,
             SkillStyle skillStyle, SkillStyle secondaryStyle, SkillStyle ultimateStyle) {
        this.label = label;
        this.faction = faction;
        this.role = role;
        this.skillDescription = skillDescription;
        this.hp = hp;
        this.attack = attack;
        this.range = range;
        this.speed = speed;
        this.skillCooldown = skillCooldown;
        this.skillStyle = skillStyle;
        this.secondaryStyle = secondaryStyle;
        this.ultimateStyle = ultimateStyle;
    }

    private static SkillStyle pairedSecondary(SkillStyle primary) {
        SkillStyle[] styles = SkillStyle.values();
        return styles[(primary.ordinal() + 5) % styles.length];
    }

    private static SkillStyle pairedUltimate(SkillStyle primary) {
        SkillStyle[] styles = SkillStyle.values();
        return styles[(primary.ordinal() + 11) % styles.length];
    }

    public HeroArchetype archetype() {
        if (role.contains("射手")) {
            return HeroArchetype.MARKSMAN;
        }
        if (role.contains("辅助") || role.contains("军师")) {
            return HeroArchetype.SUPPORT;
        }
        if (role.contains("坦")) {
            return HeroArchetype.TANK;
        }
        if (role.contains("刺")) {
            return HeroArchetype.ASSASSIN;
        }
        if (role.contains("法")) {
            return HeroArchetype.MAGE;
        }
        return HeroArchetype.FIGHTER;
    }

    public boolean usesRangedBasicAttack() {
        HeroArchetype archetype = archetype();
        return range > 155f
                || archetype == HeroArchetype.MARKSMAN
                || archetype == HeroArchetype.MAGE
                || archetype == HeroArchetype.SUPPORT;
    }

    public DamageType basicAttackDamageType() {
        if (role.contains("法") || role.contains("辅助") || role.contains("军师") || role.contains("剑仙")) {
            return DamageType.MAGIC;
        }
        HeroArchetype archetype = archetype();
        if (archetype == HeroArchetype.MAGE || archetype == HeroArchetype.SUPPORT) {
            return DamageType.MAGIC;
        }
        return DamageType.PHYSICAL;
    }
}
