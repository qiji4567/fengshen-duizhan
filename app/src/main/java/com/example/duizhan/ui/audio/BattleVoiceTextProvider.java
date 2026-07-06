package com.example.duizhan.ui.audio;

import com.example.duizhan.game.HeroType;
import com.example.duizhan.game.audio.BattleVoicePhrase;
import com.example.duizhan.game.audio.BattleVoiceStep;

import java.util.ArrayList;
import java.util.List;

final class BattleVoiceTextProvider {
    private BattleVoiceTextProvider() {
    }

    static String stepText(BattleVoiceStep step) {
        if (step == null) {
            return "";
        }
        if (step.isPhrase()) {
            return phraseText(step.phrase);
        }
        if (step.isHero()) {
            return heroLine(step.heroType);
        }
        return "";
    }

    static List<String> resolveSteps(List<BattleVoiceStep> steps) {
        List<String> lines = new ArrayList<>();
        if (steps == null) {
            return lines;
        }
        for (BattleVoiceStep step : steps) {
            if (step == null) {
                continue;
            }
            if (step.isPhrase()) {
                String line = phraseText(step.phrase);
                if (line.length() > 0) {
                    lines.add(line);
                }
            } else if (step.isHero()) {
                String line = heroName(step.heroType);
                if (line.length() > 0) {
                    lines.add(line);
                }
            }
        }
        return lines;
    }

    static String welcomeText() {
        return "欢迎进入封神峡谷，战斗开始";
    }

    private static String phraseText(BattleVoicePhrase phrase) {
        if (phrase == null) {
            return "";
        }
        switch (phrase) {
            case DEFEAT:
                return "击败";
            case SLAIN:
                return "你已被击杀";
            case KILL_SINGLE:
                return "单杀！";
            case KILL_DOUBLE:
                return "双杀！";
            case KILL_TRIPLE:
                return "三杀！";
            case KILL_QUADRA:
                return "四杀！";
            case KILL_PENTA:
                return "五杀！";
            case KILL_GODLIKE:
                return "接近神了！";
            case KILL_LEGENDARY:
                return "超神了！";
            case KILL_MONSTER:
                return "击败野怪";
            default:
                return "";
        }
    }

    static String heroLine(HeroType heroType) {
        if (heroType == null) {
            return "";
        }
        return heroType.label;
    }

    private static String heroName(HeroType heroType) {
        if (heroType == null) {
            return "";
        }
        return heroType.label;
    }
}
