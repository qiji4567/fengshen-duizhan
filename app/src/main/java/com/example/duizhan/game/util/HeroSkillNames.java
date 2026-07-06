package com.example.duizhan.game.util;

import com.example.duizhan.game.HeroType;

import java.util.EnumMap;
import java.util.Map;

/** Per-hero skill names; every name across all heroes and slots is unique. */
public final class HeroSkillNames {
    private static final class Names {
        final String secondary;
        final String ultimate;
        final String talent;

        Names(String secondary, String ultimate, String talent) {
            this.secondary = secondary;
            this.ultimate = ultimate;
            this.talent = talent;
        }
    }

    private static final Map<HeroType, Names> NAMES = new EnumMap<>(HeroType.class);

    static {
        register(HeroType.SUN_WUKONG, "七十二变", "大闹天宫", "铜头铁骨");
        register(HeroType.ERLANG_SHEN, "三尖两刃", "哮天擒妖", "八九玄功");
        register(HeroType.NEZHA, "混天绫", "莲花化身", "莲花重塑");
        register(HeroType.LEI_ZHENZI, "风雷双翅", "天雷降世", "雷纹护体");
        register(HeroType.JU_LING_SHEN, "巨斧横扫", "山岳倾覆", "神力觉醒");
        register(HeroType.CHANGE, "玉兔伴月", "广寒清影", "月华回春");
        register(HeroType.NIU_MO_WANG, "魔王狂怒", "平天撼世", "妖血沸腾");
        register(HeroType.BAI_GU_JING, "三打迷心", "白骨幻域", "尸毒侵蚀");
        register(HeroType.JIANG_ZIYA, "打神鞭", "诸神敕封", "天机演算");
        register(HeroType.TAIYI, "九龙吐焰", "乾元灭劫", "仙翁护体");
        register(HeroType.PAGODA_KING, "天罗地网", "托塔镇魔", "天王威仪");
        register(HeroType.GUANYIN, "杨柳轻拂", "普渡众生", "慈悲甘露");
        register(HeroType.WEN_ZHONG, "墨麒麟冲", "雷部天罚", "忠烈不灭");
        register(HeroType.ZHAO_GONGMING, "财神赐福", "财宝天降", "聚宝生财");
        register(HeroType.YUN_XIAO, "九曲黄河", "黄河阵开", "截教仙体");
        register(HeroType.QIONG_XIAO, "碧霄剑气", "三霄合击", "仙岛护佑");
        register(HeroType.BI_XIAO, "金蛟绞杀", "剪断乾坤", "截教心法");
        register(HeroType.DOU_MU, "星斗轮转", "周天星斗", "斗姆慈光");
        register(HeroType.TAI_BAI, "金星指路", "长庚破邪", "仙翁妙算");
        register(HeroType.ZHEN_YUAN, "人参果宴", "天地同寿", "地仙之祖");
        register(HeroType.ZHU_BAJIE, "天蓬冲撞", "猪刚鬣怒", "贪吃回血");
        register(HeroType.SHA_WUJING, "流沙陷阵", "卷帘护驾", "河沙自愈");
        register(HeroType.TANG_SENG, "梵音超度", "金蝉转世", "禅心回元");
        register(HeroType.HONG_HAIER, "火尖枪刺", "圣婴焚天", "魔童之躯");
        register(HeroType.TIE_SHAN, "铁扇灭火", "芭蕉风暴", "公主风骨");
        register(HeroType.YU_TU, "捣药仙风", "月宫疾驰", "灵兔回春");
        register(HeroType.KUIMU_LANG, "狼牙撕咬", "奎宿猎杀", "星狼嗜血");
        register(HeroType.KANG_JIN_LONG, "龙角顶击", "亢龙有悔", "金龙护鳞");
        register(HeroType.JIAO_MU_JIAO, "盘龙绞杀", "蛟王翻海", "角宿战意");
        register(HeroType.NAO_HAI_LONG, "龙宫震怒", "四海归一", "龙族血统");
        register(HeroType.YAMA, "阎罗审判", "冥府降世", "幽冥主宰");
        register(HeroType.BLACK_WHITE, "无常索命", "阴阳双煞", "鬼差追魂");
        register(HeroType.MAZU, "海神庇佑", "天后镇浪", "护国佑民");
        register(HeroType.XUAN_WU, "龟蛇合击", "真武荡魔", "北方玄武");
        register(HeroType.QING_LONG, "苍龙摆尾", "东方青龙", "木德生机");
        register(HeroType.BAI_HU, "虎啸山林", "西方白虎", "金煞杀伐");
        register(HeroType.ZHU_QUE, "南明离火", "南方朱雀", "火羽涅槃");
        register(HeroType.XUAN_NU, "兵法天书", "玄女破阵", "战阵精通");
        register(HeroType.LU_DONG_BIN, "御剑飞行", "纯阳剑阵", "剑仙逍遥");
        register(HeroType.HE_XIAN_GU, "荷花护体", "何家仙法", "女仙回春");
        register(HeroType.HAN_ZHONGLI, "扇起狂岚", "汉钟震世", "芭蕉回元");
        register(HeroType.LAN_CAIHE, "花篮撒花", "蓝采仙歌", "逍遥散仙");
        register(HeroType.CAO_GUOJIU, "玉板清音", "曹国护国", "皇亲威仪");
        register(HeroType.LI_TIEGUAI, "铁拐点化", "葫芦收妖", "瘸仙妙手");
        register(HeroType.ZHANG_GUOLAO, "倒骑毛驴", "张果显圣", "驴背仙风");
        register(HeroType.DA_JI, "九尾幻舞", "妲己倾国", "狐火迷心");
        register(HeroType.SHEN_GONGBAO, "豹头环眼", "申公夺魄", "左道邪术");
        register(HeroType.BI_GAN, "掏心忠谏", "比干护国", "七窍玲珑");
        register(HeroType.HUANG_FEI_HU, "武成铁骑", "黄飞虎怒", "成王威势");
        register(HeroType.DENG_CHAN_YU, "石雨飞掷", "邓婵玉舞", "巾帼英风");
        register(HeroType.TU_XING_SUN, "土遁潜行", "地行无踪", "土行奇术");
        register(HeroType.MU_ZHA, "木剑斩妖", "木吒护法", "李门忠勇");
        register(HeroType.JIN_ZHA, "金身护体", "金吒破邪", "佛门金身");
        register(HeroType.LONG_XU_HU, "石弹轰击", "龙须虎吼", "异兽蛮力");
        register(HeroType.WEN_SHU, "智慧剑轮", "文殊说法", "慧光普照");
        register(HeroType.PU_XIAN, "白象踏阵", "普贤宏愿", "大行愿力");
        register(HeroType.NU_WA, "造人圣手", "补天再造", "母神慈恩");
        register(HeroType.FU_XI, "河图洛书", "伏羲演卦", "人文始祖");
        register(HeroType.PAN_GU, "斧劈混沌", "盘古身化", "开天精血");
        register(HeroType.HOU_YI, "连射九日", "后羿射日", "神箭穿云");
        register(HeroType.CHANG_E_NEW, "嫦娥奔月", "广寒孤影", "月宫仙姿");
        register(HeroType.KUA_FU, "夸父逐日", "杖化邓林", "逐日志坚");
        register(HeroType.JING_WEI, "精卫填海", "衔石不息", "魂火不灭");
        register(HeroType.XING_TIAN, "刑天舞干", "干戚狂战", "无首不屈");
        register(HeroType.CHI_YOU, "铜头铁额", "兵主噬天", "九黎战血");
        register(HeroType.GONG_GONG, "水神怒击", "共工触天", "怒浪滔天");
        register(HeroType.ZHU_RONG, "火神祝融", "焚世烈焰", "南岳火精");
        register(HeroType.JADE_EMPEROR, "昊天金阙", "玉皇敕令", "天道威仪");
        register(HeroType.QUEEN_MOTHER, "瑶池仙宴", "王母赐桃", "西华圣光");
        register(HeroType.BUDDHA, "如来神掌", "五指封魔", "万劫金身");
        register(HeroType.DI_TING, "谛听辨恶", "听世明心", "兽王慧眼");
        register(HeroType.MANJUSRI, "狮子吼", "文殊智慧", "般若妙慧");
        register(HeroType.GOLD_HORN, "紫金收人", "金角夺宝", "老君道童");
        register(HeroType.SILVER_HORN, "银角搬山", "七星斩邪", "银角夺法");
        register(HeroType.BLUE_BULL, "金刚琢收", "青牛踏阵", "太上坐骑");
        register(HeroType.NINE_HEAD, "九头连咬", "虫王翻江", "血海妖躯");
        register(HeroType.GOLD_ROPE, "捆仙索缚", "黄袍夺衣", "奎星本相");
        register(HeroType.MENG_PO, "孟婆汤", "忘川渡魂", "轮回汤力");
        register(HeroType.JUDGE_CUI, "判官笔勾", "崔珏断罪", "阴司律令");
        register(HeroType.OX_HEAD, "牛头冲撞", "冥牛踏阵", "地府猛将");
        register(HeroType.HORSE_FACE, "马面勾魂", "冥马追命", "阴司追猎");
        register(HeroType.WEST_DRAGON, "西海冰潮", "霜龙封海", "西海龙威");
        register(HeroType.SOUTH_DRAGON, "南海怒涛", "海啸吞天", "南海龙吟");
        register(HeroType.HEART_FIRE, "心月狐火", "心宿焚野", "狐火夜行");
        register(HeroType.ROOM_FIRE, "室火猪突", "烈焰猪王", "火宿冲锋");
        register(HeroType.PLEIADES, "昴日啼晨", "金鸡破晓", "日鸡神光");
    }

    private HeroSkillNames() {
    }

    private static void register(HeroType heroType, String secondary, String ultimate, String talent) {
        NAMES.put(heroType, new Names(secondary, ultimate, talent));
    }

    public static String secondaryName(HeroType heroType) {
        Names names = NAMES.get(heroType);
        return names == null ? "" : names.secondary;
    }

    public static String ultimateName(HeroType heroType) {
        Names names = NAMES.get(heroType);
        return names == null ? "" : names.ultimate;
    }

    public static String talentName(HeroType heroType) {
        Names names = NAMES.get(heroType);
        return names == null ? "" : names.talent;
    }
}

