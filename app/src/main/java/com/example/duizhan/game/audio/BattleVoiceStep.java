package com.example.duizhan.game.audio;

import com.example.duizhan.game.HeroType;

public class BattleVoiceStep {
    public final BattleVoicePhrase phrase;
    public final HeroType heroType;

    private BattleVoiceStep(BattleVoicePhrase phrase, HeroType heroType) {
        this.phrase = phrase;
        this.heroType = heroType;
    }

    public static BattleVoiceStep phrase(BattleVoicePhrase phrase) {
        return new BattleVoiceStep(phrase, null);
    }

    public static BattleVoiceStep hero(HeroType heroType) {
        return new BattleVoiceStep(null, heroType);
    }

    public boolean isPhrase() {
        return phrase != null;
    }

    public boolean isHero() {
        return heroType != null;
    }
}
