package com.example.duizhan.game.story;

import com.example.duizhan.game.HeroType;

import java.util.Random;

public final class BattlePrologueBuilder {
    private static long buildCount;

    private BattlePrologueBuilder() {
    }

    public static BattlePrologue build(HeroType blueHero, HeroType redHero) {
        HeroType blue = blueHero == null ? HeroType.SUN_WUKONG : blueHero;
        HeroType red = redHero == null ? HeroType.ERLANG_SHEN : redHero;
        long seed = System.nanoTime()
                ^ (System.currentTimeMillis() << 7)
                ^ ((long) blue.ordinal() << 32)
                ^ red.ordinal()
                ^ (++buildCount * 1103515245L);
        Random random = new Random(seed);
        String era = resolveEra(blue, red);
        String blueHomeland = homeland(blue.faction);
        String redHomeland = homeland(red.faction);
        String omen = pick(random, omenLines());
        String cause = causeLine(random, blue, red, blueHomeland, redHomeland);
        String eraTitle = "上古·" + era + "·" + battleMark(seed);
        String matchupLine = blue.label + "  对阵  " + red.label + "｜" + omen;
        String blueLine = sideLine(random, "蓝方", blue, red, blueHomeland, seed ^ 0xB10E);
        String redLine = sideLine(random, "红方", red, blue, redHomeland, seed ^ 0xC0DE);
        String themeLine = cause + pick(random, themeLines(blue, red));
        String closingLine = closingLine(random, blue, red, seed);
        return new BattlePrologue(eraTitle, matchupLine, blueLine, redLine, themeLine, closingLine);
    }

    private static String sideLine(Random random, String side, HeroType self, HeroType rival,
                                   String homeland, long seed) {
        String skillName = skillName(self);
        String roleIntent = roleIntent(self.role);
        String rivalPressure = rivalPressure(rival);
        String beat = battleBeat(seed);
        switch (random.nextInt(5)) {
            case 0:
                return side + " " + self.label + "在" + beat + "从" + homeland + "踏入峡谷，"
                        + skillName + "未出，" + roleIntent + "已压过" + rival.label + "的阵脚。";
            case 1:
                return side + " " + self.label + "不为扬名而来，" + beat + "记着他的来意，"
                        + self.faction + "旧誓在身，今日只问" + rivalPressure + "。";
            case 2:
                return side + " " + self.label + "把" + homeland + "的风尘藏进甲胄，"
                        + beat + "一到，" + skillName + "落下，退路便不再属于" + rival.label + "。";
            case 3:
                return side + " " + self.label + "听见" + beat + "后的故土催战，"
                        + roleIntent + "，也要逼" + rival.label + "交出真正底牌。";
            default:
                return side + " " + self.label + "立于河道之前，"
                        + self.faction + "的因果缠上兵刃，" + skillName + "只等" + beat + "。";
        }
    }

    private static String causeLine(Random random, HeroType blue, HeroType red,
                                    String blueHomeland, String redHomeland) {
        switch (random.nextInt(6)) {
            case 0:
                return blueHomeland + "与" + redHomeland + "之间的灵脉忽明忽暗，"
                        + "谁先占住峡谷，谁就能把下一场天命写进自己族谱。";
            case 1:
                return "封神峡谷今夜不认善恶，只认选择；"
                        + blue.label + "和" + red.label + "都带着不能退的理由。";
            case 2:
                return "一枚失落的星印坠入中路，"
                        + blue.faction + "要取回名分，" + red.faction + "要守住旧约。";
            case 3:
                return "旧神的判词被风吹散，"
                        + blue.label + "与" + red.label + "必须用兵刃补上缺失的一页。";
            case 4:
                return "峡谷深处传来第二次钟鸣，"
                        + blueHomeland + "求生，" + redHomeland + "求证，双方都没有旁观的资格。";
            default:
                return "此局不是偶遇，是两条因果在河道相撞；"
                        + blue.faction + "与" + red.faction + "都在等一个答案。";
        }
    }

    private static String closingLine(Random random, HeroType blue, HeroType red, long seed) {
        String seal = Integer.toHexString((int) (seed ^ (seed >>> 32))).toUpperCase();
        seal = seal.length() > 4 ? seal.substring(0, 4) : seal;
        switch (random.nextInt(6)) {
            case 0:
                return "战印" + seal + "已亮，胜者不是无罪之人，只是先走完代价的人。";
            case 1:
                return "战印" + seal + "的鼓声三落之后，" + blue.label + "与" + red.label + "只能有一人带走答案。";
            case 2:
                return "刀光会替沉默开口，峡谷会记下战印" + seal + "这一局独有的名字。";
            case 3:
                return "此战若有神明旁观，也只能等战印" + seal + "的最后一击再下判词。";
            case 4:
                return "风已转向，河道收声，战印" + seal + "的下一步便是命数翻面。";
            default:
                return "没有重来的战场，只有战印" + seal + "此刻的抉择。入阵。";
        }
    }

    private static String[] omenLines() {
        return new String[]{
                "星火倒悬",
                "雷纹入水",
                "月影压城",
                "金鼓未鸣",
                "潮声逆流",
                "香火断续",
                "鬼门半启",
                "天书缺页",
                "龙鳞泛白",
                "烽烟无风"
        };
    }

    private static String[] themeLines(HeroType blue, HeroType red) {
        return new String[]{
                "这不是谁替天行道，而是谁敢替自己的来路承担后果。",
                "两边都不是陪衬，" + blue.label + "有执念，" + red.label + "也有不可说的旧伤。",
                "今日的胜负会传回故土，变成酒馆里的传闻，也变成神台前的沉默。",
                "他们都知道，对方不是单薄的敌人，而是另一种活下去的理由。",
                "若此战必须有人被误解，那也要先把峡谷打到天明。"
        };
    }

    private static String battleMark(long seed) {
        String[] stems = {"甲", "乙", "丙", "丁", "戊", "己", "庚", "辛", "壬", "癸"};
        String[] branches = {"子", "丑", "寅", "卯", "辰", "巳", "午", "未", "申", "酉", "戌", "亥"};
        int a = (int) Math.floorMod(seed, stems.length);
        int b = (int) Math.floorMod(seed >>> 5, branches.length);
        int c = (int) Math.floorMod(seed >>> 11, 99) + 1;
        return stems[a] + branches[b] + "第" + c + "签";
    }

    private static String battleBeat(long seed) {
        String[] beats = {"第一声暗鼓", "第二道星痕", "第三缕逆风", "第四盏残灯",
                "第五片龙鳞", "第六枚雷纹", "第七页残诏", "第八重潮影",
                "第九粒劫灰", "第十段钟声"};
        int a = (int) Math.floorMod(seed, beats.length);
        int b = (int) Math.floorMod(seed >>> 9, 97) + 3;
        return beats[a] + "第" + b + "刻";
    }

    private static String skillName(HeroType heroType) {
        if (heroType == null || heroType.skillDescription == null) {
            return "神通";
        }
        int split = heroType.skillDescription.indexOf('：');
        if (split <= 0) {
            return heroType.skillDescription;
        }
        return heroType.skillDescription.substring(0, split);
    }

    private static String roleIntent(String role) {
        if (role == null) {
            return "杀意";
        }
        if (role.contains("刺")) {
            return "藏锋的杀意";
        }
        if (role.contains("坦")) {
            return "不退的阵线";
        }
        if (role.contains("法")) {
            return "将成未成的法阵";
        }
        if (role.contains("射")) {
            return "锁定咽喉的准星";
        }
        if (role.contains("辅助") || role.contains("军师")) {
            return "改写全局的算计";
        }
        return "正面破阵的胆气";
    }

    private static String rivalPressure(HeroType rival) {
        if (rival == null) {
            return "胜负";
        }
        if (rival.role.contains("刺")) {
            return "暗影从何处现身";
        }
        if (rival.role.contains("坦")) {
            return "那堵铜墙何时开裂";
        }
        if (rival.role.contains("法")) {
            return "法阵能否撑到天明";
        }
        if (rival.role.contains("射")) {
            return "第一支箭会射向谁";
        }
        if (rival.role.contains("辅助") || rival.role.contains("军师")) {
            return "谋局是否还有后手";
        }
        return "谁先露出破绽";
    }

    private static String pick(Random random, String[] values) {
        if (values == null || values.length == 0) {
            return "";
        }
        return values[random.nextInt(values.length)];
    }

    private static String resolveEra(HeroType blue, HeroType red) {
        if (containsAncient(blue, red)) {
            return "神魔争天之战";
        }
        if (containsFaction(blue, red, "封神")) {
            return "封神榜前夜";
        }
        if (containsFaction(blue, red, "西游") && containsFaction(blue, red, "天庭")) {
            return "西行天庭之辩";
        }
        if (containsFaction(blue, red, "地府")) {
            return "幽冥阳世之争";
        }
        if (containsFaction(blue, red, "四海龙王") || containsFaction(blue, red, "海神")) {
            return "四海潮汐之劫";
        }
        if (containsFaction(blue, red, "雷部")) {
            return "雷部天罚之役";
        }
        if (containsFaction(blue, red, "月宫")) {
            return "月宫清辉之辩";
        }
        if (containsFaction(blue, red, "佛门")) {
            return "释道并行之会";
        }
        if (containsFaction(blue, red, "八仙")) {
            return "八仙渡劫之局";
        }
        return "封神峡谷异变";
    }

    private static boolean containsAncient(HeroType blue, HeroType red) {
        return isAncientFaction(blue.faction) || isAncientFaction(red.faction);
    }

    private static boolean isAncientFaction(String faction) {
        return "上古正神".equals(faction)
                || "上古魔神".equals(faction)
                || "上古神射".equals(faction)
                || "上古火神".equals(faction)
                || "上古水神".equals(faction)
                || "山海经".equals(faction);
    }

    private static boolean containsFaction(HeroType blue, HeroType red, String faction) {
        return faction.equals(blue.faction) || faction.equals(red.faction);
    }

    private static String homeland(String faction) {
        if (faction == null) {
            return "故土";
        }
        switch (faction) {
            case "西游":
                return "东土灵山";
            case "天庭":
                return "三十三天";
            case "地府":
                return "九幽冥府";
            case "封神":
                return "西岐故土";
            case "封神正神":
                return "封神台阙";
            case "月宫":
                return "广寒宫阙";
            case "四海龙王":
                return "四海龙宫";
            case "海神":
                return "东南海疆";
            case "雷部":
            case "雷部正神":
                return "雷部天域";
            case "二十八宿":
                return "星宿天垣";
            case "四象":
                return "四象神域";
            case "三霄":
                return "三霄仙岛";
            case "群星正神":
                return "斗姆星域";
            case "北极正神":
                return "真武北境";
            case "八仙":
                return "蓬莱仙乡";
            case "阐教":
                return "昆仑玉虚";
            case "佛门":
                return "灵山净土";
            case "上古正神":
                return "补天圣域";
            case "上古魔神":
                return "兵主旧域";
            case "上古神射":
                return "十日之野";
            case "上古火神":
                return "祝融火域";
            case "上古水神":
                return "不周水脉";
            case "山海经":
                return "大荒山海";
            default:
                return faction + "故土";
        }
    }
}
