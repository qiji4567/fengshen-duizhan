package com.example.duizhan.game.guide;

import com.example.duizhan.game.HeroArchetype;
import com.example.duizhan.game.HeroType;

public final class HeroTipsProvider {
    private HeroTipsProvider() {
    }

    public static String movementTips(HeroType heroType) {
        HeroArchetype archetype = heroType == null ? HeroArchetype.FIGHTER : heroType.archetype();
        switch (archetype) {
            case ASSASSIN:
                return "利用草丛与二段突进绕后，优先切敌方后排；没目标时不要空放技能。";
            case MAGE:
                return "保持安全距离输出，利用溪流与墙体卡视野；团战先消耗再开大招。";
            case TANK:
                return "顶在前排吸收伤害，用控制技能保护高地塔；注意断墙翻越包抄。";
            case MARKSMAN:
                return "走A拉扯，优先攻击最近可打目标；回城读条时避免移动和放技能。";
            case SUPPORT:
                return "跟随队友提供治疗与护盾，团战站后排；河道视野要常看。";
            case FIGHTER:
            default:
                return "线上换血后利用小兵/野怪发育；有优势时推塔压进，劣势时控线回城补给。";
        }
    }

    public static String heroSpecificTips(HeroType heroType) {
        if (heroType == null) {
            return "优先补刀与推塔，别在没目标时浪费技能冷却。";
        }
        switch (heroType) {
            case SUN_WUKONG:
                return "孙悟空：二技七十二变可化身敌方英雄，配合草丛隐身打突袭。";
            case ERLANG_SHEN:
                return "杨戬：天眼光束朝最近敌人释放，适合对线消耗与远程收割。";
            case NEZHA:
                return "哪吒：风火轮突进后留二段逃生，优先攻击英雄再清兵。";
            case GUANYIN:
            case TANG_SENG:
            case HE_XIAN_GU:
                return "辅助型：没敌人时仍可开治疗技能自保，但输出技能需锁定目标。";
            default:
                return heroType.label + "：" + heroType.skillDescription + "。优先攻击英雄，再清兵打野。";
        }
    }

    public static String replayTips() {
        return "回放时可切换蓝方/红方/第三人称/自由视角，观察走位轨迹与进草时机。";
    }
}
