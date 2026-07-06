package com.example.duizhan.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.duizhan.R;
import com.example.duizhan.data.BattleRecord;
import com.example.duizhan.databinding.ActivityMainBinding;
import com.example.duizhan.game.BattleDifficulty;
import com.example.duizhan.game.HeroArchetype;
import com.example.duizhan.game.HeroType;
import com.example.duizhan.mvp.MainContract;
import com.example.duizhan.mvp.MainPresenter;
import com.example.duizhan.ui.audio.BattleAnnouncer;
import com.example.duizhan.ui.audio.GameSpeech;
import com.example.duizhan.ui.audio.HeroLineAnnouncer;
import com.example.duizhan.ui.base.BaseMvpActivity;
import com.example.duizhan.ui.util.UiFeedbackHelper;
import com.example.duizhan.view.HeroPortraitCache;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class MainActivity extends BaseMvpActivity<ActivityMainBinding, MainContract.View, MainContract.Presenter>
        implements MainContract.View {
    private static final String PREFS_NAME = "duizhan_prefs";
    private static final String PREF_DIFFICULTY = "battle_difficulty";

    private HeroType selectedBlueHero = HeroType.SUN_WUKONG;
    private HeroType selectedRedHero = HeroType.ERLANG_SHEN;
    private HeroGroup selectedHeroGroup = HeroGroup.ALL;
    private PickSide activePickSide = PickSide.BLUE;
    private boolean redRandom = true;
    private BattleDifficulty selectedDifficulty = BattleDifficulty.MEDIUM;
    private long lastRecordId = -1L;
    private final Random random = new Random();
    private GameSpeech speech;

    @Override
    protected ActivityMainBinding inflateBinding(LayoutInflater inflater) {
        return ActivityMainBinding.inflate(inflater);
    }

    @Override
    protected MainContract.Presenter createPresenter() {
        return new MainPresenter(this);
    }

    @Override
    protected MainContract.View getMvpView() {
        return this;
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        HeroPortraitCache.init(this);
        speech = GameSpeech.get(this);
        setupDifficultySelector();
        setupSidePicker();
        populateHeroGroups();
        populateHeroList();
        updateShowcase();
        UiFeedbackHelper.bindClick(binding.btnStart, this::startBattle);
        UiFeedbackHelper.bindClick(binding.btnHistory, () ->
                startActivity(new Intent(this, HistoryActivity.class)));
        UiFeedbackHelper.bindClick(binding.btnStats, () ->
                startActivity(new Intent(this, StatsActivity.class)));
        UiFeedbackHelper.bindClick(binding.btnGuide, () ->
                startActivity(new Intent(this, HeroGuideActivity.class)));
        UiFeedbackHelper.bindClick(binding.tvLastRecord, this::openLastRecord);
        binding.getRoot().postDelayed(() -> playHeroPickVoice(selectedBlueHero), 500);
    }

    private void playHeroPickVoice(HeroType heroType) {
        if (heroType == null || speech == null) {
            return;
        }
        BattleAnnouncer wav = speech.wav();
        HeroLineAnnouncer tts = speech.tts();
        if (wav == null) {
            if (tts != null) {
                tts.playPickLine(heroType, true);
            }
            return;
        }
        wav.playHeroPick(this, heroType, () -> {
            if (tts != null) {
                tts.playPickLine(heroType, true);
            }
        });
    }

    private void setupSidePicker() {
        UiFeedbackHelper.bindClick(binding.btnPickBlue, () -> switchPickSide(PickSide.BLUE));
        UiFeedbackHelper.bindClick(binding.btnPickRed, () -> switchPickSide(PickSide.RED));
        UiFeedbackHelper.bindClick(binding.btnRedRandom, () -> {
            selectedRedHero = randomRedHero();
            redRandom = false;
            activePickSide = PickSide.RED;
            refreshSideButtons();
            refreshHeroSelectionStyles();
            updateShowcase();
            playHeroPickVoice(selectedRedHero);
        });
        refreshSideButtons();
    }

    private void switchPickSide(PickSide side) {
        activePickSide = side;
        refreshSideButtons();
        refreshHeroSelectionStyles();
        updateShowcase();
    }

    private void refreshSideButtons() {
        boolean pickingBlue = activePickSide == PickSide.BLUE;
        binding.btnPickBlue.setBackgroundResource(pickingBlue
                ? R.drawable.bg_button_gold : R.drawable.bg_button);
        binding.btnPickBlue.setTextColor(pickingBlue ? 0xFF111827 : getColor(R.color.text));
        binding.btnPickRed.setBackgroundResource(!pickingBlue
                ? R.drawable.bg_button_gold : R.drawable.bg_button);
        binding.btnRedRandom.setAlpha(redRandom && !pickingBlue ? 1f : 0.72f);
    }

    private void openLastRecord() {
        if (lastRecordId > 0L) {
            Intent intent = new Intent(this, BattleDetailActivity.class);
            intent.putExtra(BattleDetailActivity.EXTRA_RECORD_ID, lastRecordId);
            startActivity(intent);
            return;
        }
        startActivity(new Intent(this, HistoryActivity.class));
    }

    @Override
    protected void onResume() {
        super.onResume();
        presenter.loadHistorySummary();
    }

    @Override
    public void showHistorySummary(int totalCount, BattleRecord lastRecord) {
        binding.btnHistory.setText(totalCount > 0
                ? getString(R.string.nav_history_count, totalCount)
                : getString(R.string.nav_history));
        if (lastRecord == null) {
            lastRecordId = -1L;
            binding.tvLastRecord.setText(R.string.no_record_hint);
            binding.tvLastRecord.setTextColor(getColor(R.color.muted));
            return;
        }
        lastRecordId = lastRecord.id;
        String time = new SimpleDateFormat(getString(R.string.last_record_time_pattern), Locale.CHINA)
                .format(new Date(lastRecord.createdAt));
        binding.tvLastRecord.setText(getString(R.string.last_record_tap_format,
                lastRecord.winner, lastRecord.blueHero, lastRecord.redHero,
                lastRecord.durationMs / 1000, time));
        binding.tvLastRecord.setTextColor(getColor(R.color.gold));
    }

    private void setupDifficultySelector() {
        selectedDifficulty = readSavedDifficulty();
        switch (selectedDifficulty) {
            case NORMAL:
                binding.rgDifficulty.check(R.id.rbDifficultyNormal);
                break;
            case HARD:
                binding.rgDifficulty.check(R.id.rbDifficultyHard);
                break;
            case MEDIUM:
            default:
                binding.rgDifficulty.check(R.id.rbDifficultyMedium);
                selectedDifficulty = BattleDifficulty.MEDIUM;
                break;
        }
        binding.rgDifficulty.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbDifficultyNormal) {
                selectedDifficulty = BattleDifficulty.NORMAL;
            } else if (checkedId == R.id.rbDifficultyHard) {
                selectedDifficulty = BattleDifficulty.HARD;
            } else {
                selectedDifficulty = BattleDifficulty.MEDIUM;
            }
            saveDifficulty(selectedDifficulty);
        });
    }

    private BattleDifficulty readSavedDifficulty() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return BattleDifficulty.fromName(
                prefs.getString(PREF_DIFFICULTY, BattleDifficulty.MEDIUM.name()),
                BattleDifficulty.MEDIUM);
    }

    private void saveDifficulty(BattleDifficulty difficulty) {
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .edit()
                .putString(PREF_DIFFICULTY, difficulty.name())
                .apply();
    }

    private void startBattle() {
        HeroType battleRedHero = redRandom ? randomRedHero() : selectedRedHero;
        Intent intent = new Intent(this, BattlePrologueActivity.class);
        intent.putExtra(BattlePrologueActivity.EXTRA_BLUE_HERO, selectedBlueHero.name());
        intent.putExtra(BattlePrologueActivity.EXTRA_RED_HERO, battleRedHero.name());
        intent.putExtra(BattlePrologueActivity.EXTRA_DIFFICULTY, selectedDifficulty.name());
        startActivity(intent);
    }

    private void populateHeroGroups() {
        binding.llHeroGroup.removeAllViews();
        for (HeroGroup group : HeroGroup.values()) {
            binding.llHeroGroup.addView(createGroupButton(group));
        }
    }

    private Button createGroupButton(HeroGroup group) {
        Button button = new Button(this);
        button.setAllCaps(false);
        button.setMinHeight(0);
        button.setMinimumHeight(0);
        button.setText(group.titleRes);
        button.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_xs));
        button.setTextColor(getColor(R.color.text));
        button.setBackgroundResource(group == selectedHeroGroup
                ? R.drawable.bg_button_gold : R.drawable.bg_button);
        UiFeedbackHelper.bindClick(button, () -> {
            selectedHeroGroup = group;
            populateHeroGroups();
            populateHeroList();
        });
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                getResources().getDimensionPixelSize(R.dimen.hero_group_tab_width),
                getResources().getDimensionPixelSize(R.dimen.battle_button_height));
        params.setMarginEnd(getResources().getDimensionPixelSize(R.dimen.space_xs));
        button.setLayoutParams(params);
        return button;
    }

    private void populateHeroList() {
        binding.llHeroList.removeAllViews();
        for (HeroType heroType : HeroType.values()) {
            if (selectedHeroGroup.matches(heroType)) {
                binding.llHeroList.addView(createHeroCard(heroType));
            }
        }
        refreshHeroSelectionStyles();
    }

    private View createHeroCard(HeroType heroType) {
        View card = LayoutInflater.from(this).inflate(R.layout.item_hero_pick, binding.llHeroList, false);
        card.setTag(heroType);
        ImageView portraitView = card.findViewById(R.id.ivHeroPortrait);
        TextView nameView = card.findViewById(R.id.tvHeroName);
        TextView roleView = card.findViewById(R.id.tvHeroRole);
        nameView.setText(heroType.label);
        roleView.setVisibility(View.VISIBLE);
        roleView.setText(getString(R.string.hero_role_faction_format, heroType.role, heroType.faction));
        portraitView.setImageBitmap(HeroPortraitCache.thumb(heroType));
        UiFeedbackHelper.bindClick(card, () -> {
            HeroType selected = (HeroType) card.getTag();
            if (activePickSide == PickSide.BLUE) {
                selectedBlueHero = selected;
            } else {
                selectedRedHero = selected;
                redRandom = false;
            }
            refreshSideButtons();
            refreshHeroSelectionStyles();
            updateShowcase();
            playHeroPickVoice(selected);
        });
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                getResources().getDimensionPixelSize(R.dimen.hero_pick_card_width),
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMarginEnd(getResources().getDimensionPixelSize(R.dimen.space_xs));
        card.setLayoutParams(params);
        card.setClickable(true);
        card.setFocusable(true);
        return card;
    }

    private void refreshHeroSelectionStyles() {
        HeroType selected = currentSelectedHero();
        for (int i = 0; i < binding.llHeroList.getChildCount(); i++) {
            View child = binding.llHeroList.getChildAt(i);
            HeroType heroType = (HeroType) child.getTag();
            boolean picked = selected != null && heroType == selected;
            child.setBackgroundResource(picked ? R.drawable.bg_hero_pick_selected : R.drawable.bg_hero_pick);
            child.setAlpha(picked ? 1f : 0.88f);
        }
    }

    private HeroType currentSelectedHero() {
        if (activePickSide == PickSide.BLUE) {
            return selectedBlueHero;
        }
        return redRandom ? null : selectedRedHero;
    }

    private void updateShowcase() {
        HeroType displayHero;
        if (activePickSide == PickSide.BLUE) {
            displayHero = selectedBlueHero;
            binding.tvShowcaseSide.setText(R.string.showcase_side_blue);
            binding.tvShowcaseSide.setTextColor(getColor(R.color.blue));
        } else if (redRandom) {
            displayHero = selectedBlueHero;
            binding.tvShowcaseSide.setText(R.string.showcase_side_red_random);
            binding.tvShowcaseSide.setTextColor(getColor(R.color.red));
        } else {
            displayHero = selectedRedHero;
            binding.tvShowcaseSide.setText(R.string.showcase_side_red);
            binding.tvShowcaseSide.setTextColor(getColor(R.color.red));
        }
        binding.ivHeroShowcase.setImageBitmap(HeroPortraitCache.showcase(displayHero));
        if (activePickSide == PickSide.RED && redRandom) {
            binding.tvShowcaseName.setText(getString(R.string.red_hero_random));
            binding.tvShowcaseMeta.setText(getString(selectedHeroGroup.titleRes));
            binding.tvShowcaseSkill.setText(getString(R.string.hero_selected_random_format,
                    getString(selectedHeroGroup.titleRes)));
            return;
        }
        binding.tvShowcaseName.setText(displayHero.label);
        binding.tvShowcaseMeta.setText(getString(R.string.showcase_meta_format,
                displayHero.role, displayHero.faction));
        binding.tvShowcaseSkill.setText(displayHero.skillDescription);
    }

    private HeroType randomRedHero() {
        List<HeroType> candidates = new ArrayList<>();
        for (HeroType heroType : HeroType.values()) {
            if (heroType != selectedBlueHero && selectedHeroGroup.matches(heroType)) {
                candidates.add(heroType);
            }
        }
        if (candidates.isEmpty()) {
            for (HeroType heroType : HeroType.values()) {
                if (heroType != selectedBlueHero) {
                    candidates.add(heroType);
                }
            }
        }
        if (candidates.isEmpty()) {
            return selectedRedHero;
        }
        return candidates.get(random.nextInt(candidates.size()));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private enum PickSide {
        BLUE,
        RED
    }

    private enum HeroGroup {
        ALL(R.string.hero_group_all, null),
        FIGHTER(R.string.hero_archetype_fighter, HeroArchetype.FIGHTER),
        MAGE(R.string.hero_archetype_mage, HeroArchetype.MAGE),
        TANK(R.string.hero_archetype_tank, HeroArchetype.TANK),
        ASSASSIN(R.string.hero_archetype_assassin, HeroArchetype.ASSASSIN),
        MARKSMAN(R.string.hero_archetype_marksman, HeroArchetype.MARKSMAN),
        SUPPORT(R.string.hero_archetype_support, HeroArchetype.SUPPORT);

        private final int titleRes;
        private final HeroArchetype archetype;

        HeroGroup(int titleRes, HeroArchetype archetype) {
            this.titleRes = titleRes;
            this.archetype = archetype;
        }

        private boolean matches(HeroType heroType) {
            return archetype == null || heroType.archetype() == archetype;
        }
    }
}
