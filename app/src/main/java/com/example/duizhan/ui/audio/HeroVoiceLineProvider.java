package com.example.duizhan.ui.audio;

import com.example.duizhan.game.HeroType;

import java.util.Random;

final class HeroVoiceLineProvider {
    private HeroVoiceLineProvider() {
    }

    static String pickLine(HeroType heroType) {
        if (heroType == null) {
            return "";
        }
        switch (heroType) {
            case SUN_WUKONG:
                return "俺老孙来也，妖魔退散！";
            case ERLANG_SHEN:
                return "天眼已开，邪祟无所遁形。";
            case NEZHA:
                return "风火轮起，今日闹他个痛快！";
            case LEI_ZHENZI:
                return "雷云听令，万钧齐落。";
            case JU_LING_SHEN:
                return "巨灵开山，挡我者碎。";
            case CHANGE:
            case CHANG_E_NEW:
                return "月华如霜，照见人间。";
            case NIU_MO_WANG:
                return "平天大圣在此，谁敢争锋？";
            case BAI_GU_JING:
                return "白骨成阵，生人莫近。";
            case JIANG_ZIYA:
                return "愿者上钩，封神有名。";
            case TAIYI:
                return "九龙神火，炼尽尘嚣。";
            case PAGODA_KING:
                return "宝塔镇妖，军令如山。";
            case GUANYIN:
                return "净瓶甘露，护佑众生。";
            case WEN_ZHONG:
                return "雷部闻仲，奉诏讨逆。";
            case ZHAO_GONGMING:
                return "定海珠落，财气也能伤人。";
            case YUN_XIAO:
                return "混元金斗，收尽锋芒。";
            case QIONG_XIAO:
                return "云雷结网，寸步难行。";
            case BI_XIAO:
                return "金蛟剪出，乾坤两断。";
            case DOU_MU:
                return "群星列阵，照我前路。";
            case TAI_BAI:
                return "星辉一点，也可定胜负。";
            case ZHEN_YUAN:
                return "袖里乾坤，天地在掌中。";
            case ZHU_BAJIE:
                return "老猪来了，先吃饱再开打！";
            case SHA_WUJING:
                return "卷帘旧将，今日再护西行。";
            case TANG_SENG:
                return "心中有戒，步步皆修行。";
            case HONG_HAIER:
                return "三昧真火，烧个干净！";
            case TIE_SHAN:
                return "芭蕉扇一挥，风月皆听令。";
            case YU_TU:
                return "捣药声轻，步子可不慢。";
            case KUIMU_LANG:
                return "奎木狼牙，撕开夜色。";
            case KANG_JIN_LONG:
                return "亢金龙吟，云开见锋。";
            case JIAO_MU_JIAO:
                return "蛟影盘空，锁住退路。";
            case NAO_HAI_LONG:
            case WEST_DRAGON:
            case SOUTH_DRAGON:
                return "龙宫起潮，四海听令。";
            case YAMA:
                return "生死簿上，已有定数。";
            case BLACK_WHITE:
                return "黑白同行，魂归有路。";
            case MAZU:
                return "海灯不灭，归舟有路。";
            case XUAN_WU:
                return "玄武镇岳，稳如北辰。";
            case QING_LONG:
                return "青龙出海，长风破阵。";
            case BAI_HU:
                return "白虎扑杀，只在一瞬。";
            case ZHU_QUE:
                return "朱雀振羽，烈焰开天。";
            case XUAN_NU:
                return "兵法在心，胜机已现。";
            case LU_DONG_BIN:
                return "纯阳一剑，斩破迷津。";
            case HE_XIAN_GU:
                return "莲香一缕，万伤可愈。";
            case HAN_ZHONGLI:
                return "宝扇一开，浊气尽散。";
            case LAN_CAIHE:
                return "花篮轻摇，阵中有春。";
            case CAO_GUOJIU:
                return "玉板一响，邪心自乱。";
            case LI_TIEGUAI:
                return "葫芦有火，也有活路。";
            case ZHANG_GUOLAO:
                return "倒骑青驴，也能先到。";
            case DA_JI:
                return "一笑倾城，一念倾国。";
            case SHEN_GONGBAO:
                return "道友请留步，好戏才开始。";
            case BI_GAN:
                return "七窍忠心，护我山河。";
            case HUANG_FEI_HU:
                return "武成破军，直取中路。";
            case DENG_CHAN_YU:
                return "五彩石落，百步穿杨。";
            case TU_XING_SUN:
                return "地脉在脚下，谁能抓得住我？";
            case MU_ZHA:
                return "莲枝化刃，锁敌无声。";
            case JIN_ZHA:
                return "金莲护体，阵前不退。";
            case LONG_XU_HU:
                return "龙须虎啸，山石俱裂。";
            case WEN_SHU:
            case MANJUSRI:
                return "慧剑在手，斩妄除魔。";
            case PU_XIAN:
                return "慈悲亦有锋芒。";
            case NU_WA:
                return "补天之光，护此苍生。";
            case FU_XI:
                return "八卦既成，万象皆明。";
            case PAN_GU:
                return "开天辟地，唯我先行。";
            case HOU_YI:
                return "日落九天，箭无虚发。";
            case KUA_FU:
                return "追日不停，热血不息。";
            case JING_WEI:
                return "衔石填海，恨意不灭。";
            case XING_TIAN:
                return "干戚在手，战魂不死。";
            case CHI_YOU:
                return "兵主临阵，血火开路。";
            case GONG_GONG:
                return "怒触不周，天地同震。";
            case ZHU_RONG:
                return "祝融燃天，万物皆明。";
            case JADE_EMPEROR:
                return "天威所至，诸神听诏。";
            case QUEEN_MOTHER:
                return "蟠桃一熟，生机自来。";
            case BUDDHA:
                return "掌中佛国，镇住万魔。";
            case DI_TING:
                return "万籁入耳，真伪分明。";
            case GOLD_HORN:
                return "紫金葫芦，叫你一声敢应吗？";
            case SILVER_HORN:
                return "七星剑寒，妖风先至。";
            case BLUE_BULL:
                return "金刚琢在手，神兵也低头。";
            case NINE_HEAD:
                return "九首同啸，谁挡得住？";
            case GOLD_ROPE:
                return "捆仙索起，寸步难逃。";
            case MENG_PO:
                return "一碗忘川，旧事皆空。";
            case JUDGE_CUI:
                return "判笔一点，功过分明。";
            case OX_HEAD:
                return "牛头开路，阴司无情。";
            case HORSE_FACE:
                return "马面追魂，莫回头。";
            case HEART_FIRE:
                return "心月一点，狐火成灾。";
            case ROOM_FIRE:
                return "室火奔腾，撞开前路。";
            case PLEIADES:
                return "昴日初升，破晓一击。";
            default:
                return heroType.label + "参战。" + heroType.skillDescription;
        }
    }

    static String moveLine(HeroType heroType, Random random) {
        if (heroType == null) {
            return "";
        }
        int index = next(random, 6);
        switch (index) {
            case 0:
                return motto(heroType);
            case 1:
                return skillName(heroType) + "未动，阵脚先移。";
            case 2:
                return heroType.faction + "之路，" + movementByRole(heroType.role);
            case 3:
                return heroType.label + "换位，别让风声泄了行踪。";
            case 4:
                return "河道有变，" + heroType.label + "先占一线。";
            default:
                return roleMoveLine(heroType.role);
        }
    }

    static String ambientLine(HeroType heroType, Random random) {
        if (heroType == null) {
            return "";
        }
        int index = next(random, 7);
        switch (index) {
            case 0:
                return pickLine(heroType);
            case 1:
                return motto(heroType);
            case 2:
                return heroType.label + "在此，别把沉默当退让。";
            case 3:
                return heroType.faction + "一脉，今日必见分晓。";
            case 4:
                return "风向不对，正好适合" + skillName(heroType) + "开局。";
            case 5:
                return roleAmbientLine(heroType.role);
            default:
                return "这一局的因果，" + heroType.label + "亲自来收。";
        }
    }

    static String actionLine(HeroType heroType, Random random) {
        if (heroType == null) {
            return "";
        }
        int index = next(random, 7);
        switch (index) {
            case 0:
                return motto(heroType);
            case 1:
                return heroType.skillDescription.replace('：', '，') + "！";
            case 2:
                return roleActionLine(heroType.role);
            case 3:
                return skillName(heroType) + "已起，别眨眼。";
            case 4:
                return heroType.label + "出手，这一下要改战线。";
            case 5:
                return "破局就在此刻，" + heroType.faction + "听令！";
            default:
                return "这一击，不为威风，只为定局！";
        }
    }

    private static int next(Random random, int bound) {
        return random == null ? 0 : random.nextInt(bound);
    }

    private static String skillName(HeroType heroType) {
        if (heroType == null || heroType.skillDescription == null) {
            return "神通";
        }
        int split = heroType.skillDescription.indexOf('：');
        return split > 0 ? heroType.skillDescription.substring(0, split) : heroType.skillDescription;
    }

    private static String roleMoveLine(String role) {
        if (role == null) {
            return "脚步压低，战机自会露头。";
        }
        if (role.contains("刺")) {
            return "影子先到，人随后收刀。";
        }
        if (role.contains("坦")) {
            return "阵线往前推，别让他们喘气。";
        }
        if (role.contains("法")) {
            return "法阵换位，下一步封路。";
        }
        if (role.contains("射")) {
            return "拉开距离，视野就是刀锋。";
        }
        if (role.contains("辅助") || role.contains("军师")) {
            return "看住全局，别急着落子。";
        }
        return "贴近战场，正面打开缺口。";
    }

    private static String roleAmbientLine(String role) {
        if (role == null) {
            return "峡谷太静，静得像在等一声裂响。";
        }
        if (role.contains("刺")) {
            return "别找我，等你发现时已经迟了。";
        }
        if (role.contains("坦")) {
            return "他们可以绕路，但绕不开这道身影。";
        }
        if (role.contains("法")) {
            return "术式还差一息，天地已经先变色。";
        }
        if (role.contains("射")) {
            return "风停的一刻，就是箭到的一刻。";
        }
        if (role.contains("辅助") || role.contains("军师")) {
            return "胜负不在一击，在每一步都算准。";
        }
        return "兵刃不问来处，只问谁敢上前。";
    }

    private static String roleActionLine(String role) {
        if (role == null) {
            return "正面一战，别想逃！";
        }
        if (role.contains("刺")) {
            return "破绽已现，收下这一击！";
        }
        if (role.contains("坦")) {
            return "跟我冲，阵线由我来顶！";
        }
        if (role.contains("法")) {
            return "法阵已成，退也无用！";
        }
        if (role.contains("射")) {
            return "别进我的射程。";
        }
        if (role.contains("辅助") || role.contains("军师")) {
            return "稳住，我来改写局势。";
        }
        return "正面一战，别想逃！";
    }

    private static String motto(HeroType heroType) {
        switch (heroType) {
            case SUN_WUKONG:
                return "吃俺老孙一棒！";
            case ERLANG_SHEN:
                return "三尖两刃，正好破阵。";
            case NEZHA:
                return "我命由我，风火随行。";
            case BAI_GU_JING:
                return "皮相会朽，白骨长存。";
            case JIANG_ZIYA:
                return "钓的是人心，也是天命。";
            case ZHU_BAJIE:
                return "慢点慢点，老猪还没喘口气呢。";
            case TANG_SENG:
                return "莫起嗔念，前路自明。";
            case GUANYIN:
                return "愿以甘露，洗去刀兵。";
            case YAMA:
                return "阳间走一遭，也逃不过判词。";
            case DA_JI:
                return "别急，心乱的人走不远。";
            case BUDDHA:
                return "一步一世界，一掌定乾坤。";
            default:
                return heroType.label + "，前进。";
        }
    }

    private static String movementByRole(String role) {
        if (role == null) {
            return "步步逼近。";
        }
        if (role.contains("刺")) {
            return "影随身动，一击即退。";
        }
        if (role.contains("坦")) {
            return "稳住阵脚，替队友开路。";
        }
        if (role.contains("法")) {
            return "术法已成，只待落子。";
        }
        if (role.contains("射")) {
            return "保持距离，箭在弦上。";
        }
        if (role.contains("辅助") || role.contains("军师")) {
            return "观全局，护队友。";
        }
        return "迎面破阵，寸步不让。";
    }
}
