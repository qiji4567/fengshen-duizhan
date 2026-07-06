package com.example.duizhan.ui.audio;

import android.content.Context;

import com.example.duizhan.game.HeroType;
import com.example.duizhan.game.audio.BattleVoicePhrase;
import com.example.duizhan.game.audio.BattleVoiceStep;

import java.util.ArrayList;
import java.util.List;

final class BattleVoiceRegistry {
    private BattleVoiceRegistry() {
    }

    static List<Integer> resolve(Context context, List<BattleVoiceStep> steps) {
        List<Integer> clipResIds = new ArrayList<>();
        if (context == null || steps == null) {
            return clipResIds;
        }
        String packageName = context.getPackageName();
        for (BattleVoiceStep step : steps) {
            if (step == null) {
                continue;
            }
            if (step.isPhrase()) {
                int resId = rawRes(context, packageName, "voice_" + step.phrase.name().toLowerCase());
                if (resId != 0) {
                    clipResIds.add(resId);
                }
            } else if (step.isHero()) {
                int resId = rawRes(context, packageName, "voice_hero_" + step.heroType.name().toLowerCase());
                if (resId != 0) {
                    clipResIds.add(resId);
                }
            }
        }
        return clipResIds;
    }

    static int resolveStep(Context context, BattleVoiceStep step) {
        if (context == null || step == null) {
            return 0;
        }
        String packageName = context.getPackageName();
        if (step.isPhrase()) {
            return rawRes(context, packageName, "voice_" + step.phrase.name().toLowerCase());
        }
        if (step.isHero()) {
            return rawRes(context, packageName, "voice_hero_" + step.heroType.name().toLowerCase());
        }
        return 0;
    }

    static int welcomeRes(Context context) {
        return rawRes(context, context.getPackageName(), "battle_welcome");
    }

    static int heroClipRes(Context context, HeroType heroType) {
        if (context == null || heroType == null) {
            return 0;
        }
        return rawRes(context, context.getPackageName(),
                "voice_hero_" + heroType.name().toLowerCase());
    }

    static int heroPickRes(Context context, HeroType heroType) {
        if (context == null || heroType == null) {
            return 0;
        }
        return rawRes(context, context.getPackageName(),
                "voice_pick_" + heroType.name().toLowerCase());
    }

    static boolean hasHeroClip(Context context, HeroType heroType) {
        return heroClipRes(context, heroType) != 0;
    }

    private static int rawRes(Context context, String packageName, String rawName) {
        return context.getResources().getIdentifier(rawName, "raw", packageName);
    }
}
