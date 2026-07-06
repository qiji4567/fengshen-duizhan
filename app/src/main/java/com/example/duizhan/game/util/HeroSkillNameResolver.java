package com.example.duizhan.game.util;

import com.example.duizhan.game.HeroType;

public final class HeroSkillNameResolver {
    private HeroSkillNameResolver() {
    }

    public static String primaryName(HeroType heroType) {
        if (heroType == null || heroType.skillDescription == null) {
            return "";
        }
        int splitIndex = heroType.skillDescription.indexOf('：');
        return splitIndex > 0 ? heroType.skillDescription.substring(0, splitIndex) : heroType.skillDescription;
    }

    public static String secondaryName(HeroType heroType) {
        return HeroSkillNames.secondaryName(heroType);
    }

    public static String ultimateName(HeroType heroType) {
        return HeroSkillNames.ultimateName(heroType);
    }

    public static String talentName(HeroType heroType) {
        return HeroSkillNames.talentName(heroType);
    }

    public static String talentDescription(HeroType heroType) {
        return talentName(heroType) + "：升级和击败野怪会回复生命。";
    }
}
