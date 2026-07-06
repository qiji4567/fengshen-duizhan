package com.example.duizhan.ui;

import android.app.Activity;
import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.example.duizhan.R;
import com.example.duizhan.game.HeroType;
import com.example.duizhan.game.ItemType;
import com.example.duizhan.game.guide.BuildGuideResolver;
import com.example.duizhan.game.guide.HeroTipsProvider;
import com.example.duizhan.ui.util.UiTextUtils;

import java.util.List;

public class HeroGuideActivity extends Activity {
    private HeroType selectedHero = HeroType.SUN_WUKONG;
    private TextView tvGuideHeroTitle;
    private TextView tvGuideBuild;
    private TextView tvGuideSkillOrder;
    private TextView tvGuideTips;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hero_guide);
        tvGuideHeroTitle = findViewById(R.id.tvGuideHeroTitle);
        tvGuideBuild = findViewById(R.id.tvGuideBuild);
        tvGuideSkillOrder = findViewById(R.id.tvGuideSkillOrder);
        tvGuideTips = findViewById(R.id.tvGuideTips);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        setupHeroTabs();
        renderGuide(selectedHero);
    }

    private void setupHeroTabs() {
        RadioGroup group = findViewById(R.id.rgHeroGuide);
        for (HeroType heroType : HeroType.values()) {
            RadioButton button = new RadioButton(this);
            button.setText(heroType.label);
            button.setTag(heroType);
            button.setTextColor(getColor(R.color.text));
            button.setChecked(heroType == selectedHero);
            button.setOnClickListener(v -> {
                selectedHero = (HeroType) v.getTag();
                renderGuide(selectedHero);
            });
            group.addView(button);
        }
    }

    private void renderGuide(HeroType heroType) {
        tvGuideHeroTitle.setText(getString(R.string.guide_hero_title_format, heroType.label, heroType.role));
        StringBuilder buildText = new StringBuilder();
        List<ItemType> items = BuildGuideResolver.recommendedItems(heroType);
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) {
                buildText.append("\n");
            }
            buildText.append(i + 1).append(". ").append(UiTextUtils.shopItem(this, items.get(i)));
        }
        tvGuideBuild.setText(getString(R.string.guide_build_section, buildText.toString()));
        tvGuideSkillOrder.setText(getString(R.string.guide_skill_section,
                BuildGuideResolver.skillOrder(heroType)));
        tvGuideTips.setText(getString(R.string.guide_tips_section,
                HeroTipsProvider.heroSpecificTips(heroType),
                HeroTipsProvider.movementTips(heroType),
                HeroTipsProvider.replayTips()));
    }
}
