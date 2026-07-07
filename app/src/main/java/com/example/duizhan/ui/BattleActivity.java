package com.example.duizhan.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.duizhan.R;
import com.example.duizhan.databinding.ActivityBattleBinding;
import com.example.duizhan.game.BattleDifficulty;
import com.example.duizhan.game.EquipmentSlot;
import com.example.duizhan.game.GameSnapshot;
import com.example.duizhan.game.HeroType;
import com.example.duizhan.game.ItemType;
import com.example.duizhan.game.SkillSlot;
import com.example.duizhan.game.Team;
import com.example.duizhan.game.guide.BuildGuideResolver;
import com.example.duizhan.game.guide.BuildPlan;
import com.example.duizhan.mvp.BattleContract;
import com.example.duizhan.mvp.BattlePresenter;
import com.example.duizhan.ui.audio.GameSpeech;
import com.example.duizhan.ui.audio.SoundEffectPlayer;
import com.example.duizhan.ui.base.BaseMvpActivity;
import com.example.duizhan.ui.util.UiFeedbackHelper;
import com.example.duizhan.ui.util.UiTextUtils;
import com.example.duizhan.view.SkillButton;

import java.util.Random;

public class BattleActivity extends BaseMvpActivity<ActivityBattleBinding, BattleContract.View, BattleContract.Presenter>
        implements BattleContract.View {
    public static final String EXTRA_BLUE_HERO = "extra_blue_hero";
    public static final String EXTRA_RED_HERO = "extra_red_hero";
    public static final String EXTRA_DIFFICULTY = "extra_difficulty";
    public static final String EXTRA_SKIP_ENTRY_VOICE = "extra_skip_entry_voice";

    private boolean finishDialogShown;
    private boolean battleStarted;
    private boolean entryVoiceFinished;
    private SoundEffectPlayer soundEffectPlayer;
    private GameSpeech speech;
    private LinearLayout shopListContainer;
    private String lastBattleToastMessage = "";
    private HeroType blueHero;
    private boolean skipEntryVoice;
    private int currentGold;
    private EquipmentSlot activeShopSlot;
    private int lastRenderedGold = -1;
    private boolean shopListDirty = true;
    private int lastRecordedFrames = -1;
    private final Handler ambientHandler = new Handler(Looper.getMainLooper());
    private final Random ambientRandom = new Random();
    private final Runnable ambientLineRunnable = () -> {
        if (!isFinishing() && !finishDialogShown && speech != null && speech.tts() != null) {
            speech.tts().maybePlayAmbientLine(blueHero);
        }
        scheduleAmbientLine();
    };

    @Override
    protected ActivityBattleBinding inflateBinding(LayoutInflater inflater) {
        return ActivityBattleBinding.inflate(inflater);
    }

    @Override
    protected BattleContract.Presenter createPresenter() {
        return new BattlePresenter(this);
    }

    @Override
    protected BattleContract.View getMvpView() {
        return this;
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        soundEffectPlayer = new SoundEffectPlayer(this);
        speech = GameSpeech.get(this);
        HeroType blueHero = readHero(EXTRA_BLUE_HERO, HeroType.SUN_WUKONG);
        HeroType redHero = readHero(EXTRA_RED_HERO, HeroType.ERLANG_SHEN);
        BattleDifficulty difficulty = readDifficulty();
        this.blueHero = blueHero;
        skipEntryVoice = getIntent().getBooleanExtra(EXTRA_SKIP_ENTRY_VOICE, false);
        presenter.setBattleSetup(blueHero, redHero, difficulty);
        binding.bvBattle.setActionListener(new BattleActionListener());
        binding.bvBattle.requestFocus();
        setupSkillButtons();
        UiFeedbackHelper.bindClick(binding.btnAttack, () -> onCombatButton(() -> {
            presenter.basicAttack();
            playHeroActionLine();
        }));
        UiFeedbackHelper.bindClick(binding.btnRecall, () -> onCombatButton(() -> presenter.recall()));
        UiFeedbackHelper.bindClick(binding.btnTalent, () -> onCombatButton(() -> {
            presenter.castTalent();
            playHeroActionLine();
        }));
        UiFeedbackHelper.bindClick(binding.btnSkillPrimary, () -> onCombatButton(() -> {
            presenter.castSkill(SkillSlot.PRIMARY);
            playHeroActionLine();
        }));
        UiFeedbackHelper.bindClick(binding.btnSkillSecondary, () -> onCombatButton(() -> {
            presenter.castSkill(SkillSlot.SECONDARY);
            playHeroActionLine();
        }));
        UiFeedbackHelper.bindClick(binding.btnSkillUltimate, () -> onCombatButton(() -> {
            presenter.castSkill(SkillSlot.ULTIMATE);
            playHeroActionLine();
        }));
        bindSkillPreview(binding.btnSkillPrimary, SkillSlot.PRIMARY);
        bindSkillPreview(binding.btnSkillSecondary, SkillSlot.SECONDARY);
        bindSkillPreview(binding.btnSkillUltimate, SkillSlot.ULTIMATE);
        setupShop();
        binding.tvHeroBanner.setText(getString(R.string.battle_hero_banner_format, 1, blueHero.label));
        binding.tvTips.setVisibility(View.GONE);
        if (skipEntryVoice) {
            binding.bvBattle.postDelayed(this::startBattleAfterEntryVoice, 250);
        } else {
            binding.bvBattle.postDelayed(this::playBattleEntryVoices, 350);
        }
        UiFeedbackHelper.bindClick(binding.btnPause, () -> {
            presenter.togglePause();
            binding.btnPause.setText(presenter.isPaused() ? R.string.resume : R.string.pause);
        });
        UiFeedbackHelper.bindClick(binding.btnShop, this::toggleShopPanel);
        UiFeedbackHelper.bindClick(binding.btnRecord, () -> {
            presenter.toggleRecording();
            updateRecordingUi();
        });
        UiFeedbackHelper.bindClick(binding.btnSaveReplay, () -> {
            binding.btnSaveReplay.setEnabled(false);
            binding.btnSaveReplay.setText(R.string.record_saving);
            presenter.saveReplayNow();
        });
        updateRecordingUi();
    }

    private void startBattleAfterEntryVoice() {
        if (battleStarted || isFinishing()) {
            return;
        }
        battleStarted = true;
        entryVoiceFinished = true;
        presenter.start();
        scheduleAmbientLine();
        binding.bvBattle.postDelayed(() -> {
            if (!isFinishing() && speech != null && speech.tts() != null) {
                speech.tts().playActionLine(blueHero);
            }
        }, 5000L);
    }

    private void playBattleEntryVoices() {
        if (isFinishing() || speech == null) {
            startBattleAfterEntryVoice();
            return;
        }
        binding.bvBattle.postDelayed(this::startBattleAfterEntryVoice, 9000L);
        speech.wav().playWelcome(this, this::playHeroEntryPickLine, () -> {
            speech.tts().playWelcome(this::playHeroEntryPickLine);
        });
    }

    private void playHeroEntryPickLine() {
        if (isFinishing() || speech == null) {
            startBattleAfterEntryVoice();
            return;
        }
        speech.wav().playHeroPick(this, blueHero, this::startBattleAfterEntryVoice,
                () -> speech.tts().playPickLine(blueHero, false, this::startBattleAfterEntryVoice));
    }

    private void scheduleAmbientLine() {
        ambientHandler.removeCallbacks(ambientLineRunnable);
        ambientHandler.postDelayed(ambientLineRunnable, 16000L + ambientRandom.nextInt(12000));
    }

    private void toggleShopPanel() {
        if (binding.svShopPanel.getVisibility() == View.VISIBLE) {
            binding.svShopPanel.setVisibility(View.GONE);
            binding.skillFanPanel.setVisibility(View.VISIBLE);
        } else {
            binding.svShopPanel.setVisibility(View.VISIBLE);
            binding.skillFanPanel.setVisibility(View.GONE);
            shopListDirty = true;
            refreshShopPanel();
        }
    }

    private void updateRecordingUi() {
        boolean recording = presenter.isRecording();
        binding.btnRecord.setText(recording ? R.string.record_stop : R.string.record_start);
        binding.btnRecord.setBackgroundResource(recording ? R.drawable.bg_button_gold : R.drawable.bg_button);
        binding.btnRecord.setTextColor(recording ? 0xFF111827 : getColor(R.color.text));
        int frames = presenter.getReplayFrameCount();
        binding.btnSaveReplay.setEnabled(frames > 0);
        binding.btnSaveReplay.setText(getString(R.string.record_save_with_frames, frames));
    }

    private String formatScoreWithRecord(GameSnapshot snapshot) {
        String status = getString(R.string.record_status_format,
                getString(presenter.isRecording() ? R.string.record_on : R.string.record_off),
                presenter.getReplayFrameCount());
        return UiTextUtils.battleStatus(this, snapshot) + "  |  " + status;
    }

    private void onCombatButton(Runnable action) {
        binding.bvBattle.releaseCameraGesture();
        binding.bvBattle.requestFocus();
        action.run();
        binding.bvBattle.syncMoveInput();
    }

    private void playHeroActionLine() {
        if (speech != null && speech.tts() != null) {
            speech.tts().playActionLine(blueHero);
        }
    }

    @Override
    public void render(GameSnapshot snapshot) {
        soundEffectPlayer.play(snapshot.soundEffects);
        if (speech != null && !snapshot.voiceSteps.isEmpty()) {
            speech.playVoiceSteps(this, snapshot.voiceSteps);
        }
        binding.bvBattle.setSnapshot(snapshot);
        binding.tvHeroBanner.setText(getString(R.string.battle_hero_banner_format,
                snapshot.blueLevel, blueHero.label));
        currentGold = snapshot.blueGold;
        if (binding.svShopPanel.getVisibility() == View.VISIBLE
                && (shopListDirty || lastRenderedGold != currentGold)) {
            refreshShopPanel();
        }
        renderSkillButton(binding.btnSkillPrimary, snapshot.blueSkillCooldown,
                snapshot.blueSkillCooldownMax, snapshot.bluePrimarySkillName);
        renderSkillButton(binding.btnSkillSecondary, snapshot.blueSecondarySkillCooldown,
                snapshot.blueSecondarySkillCooldownMax, snapshot.blueSecondarySkillName);
        renderSkillButton(binding.btnSkillUltimate, snapshot.blueUltimateCooldown,
                snapshot.blueUltimateCooldownMax, snapshot.blueUltimateSkillName);
        renderSkillButton(binding.btnTalent, snapshot.blueTalentCooldown,
                snapshot.blueTalentCooldownMax, snapshot.blueTalentName);
        binding.btnAttack.setSkillState(getString(R.string.attack), 0f, 0f);
        renderRecallButton(snapshot.blueRecallChannel);
        binding.tvScore.setText(formatScoreWithRecord(snapshot));
        int frames = presenter.getReplayFrameCount();
        if (frames != lastRecordedFrames) {
            lastRecordedFrames = frames;
            updateRecordingUi();
        }
    }

    private void maybeShowBattleToast(String message) {
        if (message == null || message.length() == 0 || message.equals(lastBattleToastMessage)) {
            return;
        }
        lastBattleToastMessage = message;
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void setupSkillButtons() {
        binding.btnAttack.setGold(true);
        binding.btnSkillPrimary.setGold(false);
        binding.btnSkillSecondary.setGold(false);
        binding.btnSkillUltimate.setGold(true);
        binding.btnTalent.setGold(false);
        binding.btnAttack.setSkillState(getString(R.string.attack), 0f, 0f);
        binding.btnSkillPrimary.setSkillState(getString(R.string.skill_primary), 0f, 0f);
        binding.btnSkillSecondary.setSkillState(getString(R.string.skill_secondary), 0f, 0f);
        binding.btnSkillUltimate.setSkillState(getString(R.string.skill_ultimate), 0f, 0f);
        binding.btnTalent.setSkillState(getString(R.string.skill_talent), 0f, 0f);
    }

    private void bindSkillPreview(View button, SkillSlot slot) {
        button.setOnTouchListener((v, event) -> {
            if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                binding.bvBattle.showSkillPreview(slot);
            }
            return false;
        });
    }

    private void renderSkillButton(SkillButton button, float cooldown, float maxCooldown, String readyText) {
        button.setSkillState(readyText, cooldown, maxCooldown);
        button.setAlpha(cooldown > 0f ? 0.82f : 1f);
    }

    private void renderRecallButton(float channelRemaining) {
        if (channelRemaining > 0f) {
            binding.btnRecall.setText(UiTextUtils.skillCooldown(this, channelRemaining));
            binding.btnRecall.setAlpha(0.85f);
        } else {
            binding.btnRecall.setText(R.string.recall);
            binding.btnRecall.setAlpha(1f);
        }
    }

    @Override
    public void showFinish(Team winner, long durationMs, long recordId) {
        if (finishDialogShown) {
            return;
        }
        finishDialogShown = true;
        String winnerName = winner == Team.BLUE ? getString(R.string.side_blue) : getString(R.string.side_red);
        String message = getString(R.string.battle_finish_message_format, durationMs / 1000);
        if (recordId > 0L) {
            message = message + "\n\n" + getString(R.string.record_auto_saved_message, recordId);
        } else {
            message = message + "\n\n" + getString(R.string.record_save_error);
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.battle_finish_title_format, winnerName))
                .setMessage(message)
                .setPositiveButton(R.string.battle_restart, (dialog, which) -> recreate())
                .setNegativeButton(R.string.back, (dialog, which) -> finish());
        if (recordId > 0L) {
            builder.setNeutralButton(R.string.view_battle_record, (dialog, which) -> {
                Intent intent = new Intent(this, BattleDetailActivity.class);
                intent.putExtra(BattleDetailActivity.EXTRA_RECORD_ID, recordId);
                startActivity(intent);
                finish();
            });
        } else {
            builder.setNeutralButton(R.string.nav_history, (dialog, which) -> {
                startActivity(new Intent(this, HistoryActivity.class));
                finish();
            });
        }
        builder.show();
    }

    @Override
    public void showShopResult(String message) {
        maybeShowBattleToast(message);
        shopListDirty = true;
        if (binding.svShopPanel.getVisibility() == View.VISIBLE) {
            refreshShopPanel();
        }
    }

    @Override
    public void onRecordingStateChanged(boolean recording, int frameCount) {
        lastRecordedFrames = frameCount;
        updateRecordingUi();
    }

    @Override
    public void onReplaySaved(long recordId) {
        updateRecordingUi();
        if (recordId > 0L) {
            Toast.makeText(this, getString(R.string.record_saved_format, recordId), Toast.LENGTH_SHORT).show();
            new AlertDialog.Builder(this)
                    .setTitle(R.string.record_saved_title)
                    .setMessage(getString(R.string.record_saved_message, recordId))
                    .setPositiveButton(R.string.view_battle_record, (dialog, which) -> {
                        Intent intent = new Intent(this, BattleDetailActivity.class);
                        intent.putExtra(BattleDetailActivity.EXTRA_RECORD_ID, recordId);
                        startActivity(intent);
                    })
                    .setNegativeButton(R.string.nav_history, (dialog, which) ->
                            startActivity(new Intent(this, HistoryActivity.class)))
                    .setNeutralButton(R.string.shop_cancel, null)
                    .show();
        } else {
            Toast.makeText(this, recordId == -1L
                    ? R.string.record_save_failed
                    : R.string.record_save_error, Toast.LENGTH_LONG).show();
        }
    }

    private void refreshShopPanel() {
        if (shopListContainer == null) {
            return;
        }
        lastRenderedGold = currentGold;
        shopListDirty = false;
        showShopList(activeShopSlot);
    }

    @Override
    protected void onDestroy() {
        ambientHandler.removeCallbacks(ambientLineRunnable);
        if (binding != null) {
            binding.bvBattle.setActionListener(null);
        }
        if (soundEffectPlayer != null) {
            soundEffectPlayer.release();
            soundEffectPlayer = null;
        }
        if (speech != null) {
            speech.stopSpeaking();
        }
        super.onDestroy();
    }

    private HeroType readHero(String key, HeroType fallback) {
        String value = getIntent().getStringExtra(key);
        if (value == null) {
            return fallback;
        }
        try {
            return HeroType.valueOf(value);
        } catch (IllegalArgumentException ignored) {
            return fallback;
        }
    }

    private BattleDifficulty readDifficulty() {
        return BattleDifficulty.fromName(
                getIntent().getStringExtra(EXTRA_DIFFICULTY),
                BattleDifficulty.MEDIUM);
    }

    private void setupShop() {
        addShopItems();
    }

    private void addShopItems() {
        android.widget.HorizontalScrollView tabScrollView = new android.widget.HorizontalScrollView(this);
        tabScrollView.setHorizontalScrollBarEnabled(false);
        LinearLayout tabContainer = new LinearLayout(this);
        tabContainer.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams tabParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        tabParams.topMargin = getResources().getDimensionPixelSize(R.dimen.space_sm);
        tabScrollView.addView(tabContainer, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        binding.llShopPanel.addView(tabScrollView, tabParams);

        addShopTab(tabContainer, R.string.shop_tab_recommended, null);
        addShopTab(tabContainer, R.string.game_slot_weapon, EquipmentSlot.WEAPON);
        addShopTab(tabContainer, R.string.game_slot_armor, EquipmentSlot.ARMOR);
        addShopTab(tabContainer, R.string.game_slot_boots, EquipmentSlot.BOOTS);
        addShopTab(tabContainer, R.string.game_slot_hat, EquipmentSlot.HAT);
        addShopTab(tabContainer, R.string.game_slot_consumable, EquipmentSlot.CONSUMABLE);

        shopListContainer = new LinearLayout(this);
        shopListContainer.setOrientation(LinearLayout.VERTICAL);
        binding.llShopPanel.addView(shopListContainer, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        showShopList(null);
    }

    private void addShopTab(LinearLayout tabContainer, int titleRes, EquipmentSlot slot) {
        Button button = new Button(this);
        button.setAllCaps(false);
        button.setMinHeight(0);
        button.setMinimumHeight(0);
        button.setText(titleRes);
        button.setTextSize(11f);
        button.setTextColor(getColor(R.color.text));
        button.setBackgroundResource(R.drawable.bg_button);
        UiFeedbackHelper.bindClick(button, () -> showShopList(slot));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                getResources().getDimensionPixelSize(R.dimen.battle_shop_tab_width),
                getResources().getDimensionPixelSize(R.dimen.battle_button_height));
        params.setMarginEnd(getResources().getDimensionPixelSize(R.dimen.space_xs));
        tabContainer.addView(button, params);
    }

    private void showShopList(EquipmentSlot slot) {
        if (shopListContainer == null) {
            return;
        }
        activeShopSlot = slot;
        shopListContainer.removeAllViews();
        int titleRes = slot == null ? R.string.shop_tab_recommended : slotTitleRes(slot);
        TextView titleView = new TextView(this);
        titleView.setText(titleRes);
        titleView.setTextColor(getColor(R.color.gold));
        titleView.setTextSize(15f);
        titleView.setTypeface(titleView.getTypeface(), android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        titleParams.topMargin = getResources().getDimensionPixelSize(R.dimen.space_sm);
        shopListContainer.addView(titleView, titleParams);

        if (slot == null) {
            addRecommendedBuildPanel();
            lastRenderedGold = currentGold;
            shopListDirty = false;
            return;
        }

        for (ItemType itemType : ItemType.values()) {
            if (itemType.slot != slot) {
                continue;
            }
            addShopItemButton(itemType, false);
        }
        lastRenderedGold = currentGold;
        shopListDirty = false;
    }

    private int slotTitleRes(EquipmentSlot slot) {
        switch (slot) {
            case WEAPON:
                return R.string.game_slot_weapon;
            case ARMOR:
                return R.string.game_slot_armor;
            case BOOTS:
                return R.string.game_slot_boots;
            case HAT:
                return R.string.game_slot_hat;
            case CONSUMABLE:
                return R.string.game_slot_consumable;
            default:
                return R.string.game_slot_relic;
        }
    }

    private void addRecommendedBuildPanel() {
        BuildPlan plan = presenter.getBuildPlan();
        TextView summaryView = new TextView(this);
        summaryView.setText(buildRecommendedSummary(plan));
        summaryView.setTextColor(getColor(R.color.muted));
        summaryView.setTextSize(12f);
        LinearLayout.LayoutParams summaryParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        summaryParams.topMargin = getResources().getDimensionPixelSize(R.dimen.space_xs);
        shopListContainer.addView(summaryView, summaryParams);

        addShopActionButton(R.string.shop_apply_recommended, false,
                () -> showBuildPlanDialog(false));
        addShopActionButton(R.string.shop_apply_next, true,
                () -> showBuildPlanDialog(true));

        for (ItemType itemType : BuildGuideResolver.recommendedItems(
                blueHero != null ? blueHero : HeroType.SUN_WUKONG)) {
            addShopItemButton(itemType, true);
        }
    }

    private String buildRecommendedSummary(BuildPlan plan) {
        if (plan.alreadyOptimal) {
            return getString(R.string.shop_build_dialog_hint_optimal);
        }
        StringBuilder builder = new StringBuilder();
        builder.append(getString(R.string.shop_build_dialog_message,
                blueHero == null ? "" : blueHero.label,
                formatTargetLoadout(plan),
                plan.purchasesInOrder.size(),
                plan.totalCost,
                currentGold,
                plan.affordablePurchases.size(),
                plan.affordableCost,
                getString(R.string.shop_build_dialog_hint_ready)));
        builder.append('\n').append('\n').append(formatSlotDiff(plan));
        if (plan.nextPurchase != null && plan.nextPurchaseReason.length() > 0) {
            builder.append('\n').append(getString(R.string.shop_smart_reason_format, plan.nextPurchaseReason));
        }
        if (plan.consumableSuggestion != null && plan.consumableReason.length() > 0) {
            builder.append('\n').append(plan.consumableReason)
                    .append("：").append(plan.consumableSuggestion.label);
        }
        return builder.toString();
    }

    private String formatSlotDiff(BuildPlan plan) {
        StringBuilder builder = new StringBuilder();
        appendSlotDiff(builder, R.string.game_slot_weapon, EquipmentSlot.WEAPON, plan);
        appendSlotDiff(builder, R.string.game_slot_armor, EquipmentSlot.ARMOR, plan);
        appendSlotDiff(builder, R.string.game_slot_boots, EquipmentSlot.BOOTS, plan);
        appendSlotDiff(builder, R.string.game_slot_hat, EquipmentSlot.HAT, plan);
        return builder.toString();
    }

    private void appendSlotDiff(StringBuilder builder, int slotTitleRes, EquipmentSlot slot, BuildPlan plan) {
        ItemType target = plan.targetLoadout.get(slot);
        if (target == null) {
            return;
        }
        ItemType current = currentEquipped(slot);
        if (builder.length() > 0) {
            builder.append('\n');
        }
        if (current == target) {
            builder.append(getString(R.string.shop_build_slot_owned,
                    getString(slotTitleRes), current.label));
            return;
        }
        builder.append(getString(R.string.shop_build_slot_line,
                getString(slotTitleRes),
                current == null ? getString(R.string.battle_view_equipment_empty) : current.label,
                target.label,
                target.cost));
    }

    private ItemType currentEquipped(EquipmentSlot slot) {
        GameSnapshot snapshot = binding.bvBattle.getSnapshot();
        if (snapshot == null) {
            return null;
        }
        for (com.example.duizhan.game.GameEntity entity : snapshot.entities) {
            if (entity.team == Team.BLUE && entity.kind == com.example.duizhan.game.UnitKind.HERO) {
                switch (slot) {
                    case WEAPON:
                        return entity.weapon;
                    case ARMOR:
                        return entity.armor;
                    case BOOTS:
                        return entity.boots;
                    case HAT:
                        return entity.hat;
                    case RELIC:
                        return entity.relic;
                    default:
                        return null;
                }
            }
        }
        return null;
    }

    private String formatTargetLoadout(BuildPlan plan) {
        StringBuilder builder = new StringBuilder();
        appendLoadoutLine(builder, R.string.game_slot_weapon, plan.targetLoadout.get(EquipmentSlot.WEAPON));
        appendLoadoutLine(builder, R.string.game_slot_armor, plan.targetLoadout.get(EquipmentSlot.ARMOR));
        appendLoadoutLine(builder, R.string.game_slot_boots, plan.targetLoadout.get(EquipmentSlot.BOOTS));
        appendLoadoutLine(builder, R.string.game_slot_hat, plan.targetLoadout.get(EquipmentSlot.HAT));
        appendLoadoutLine(builder, R.string.game_slot_relic, plan.targetLoadout.get(EquipmentSlot.RELIC));
        return builder.toString();
    }

    private void appendLoadoutLine(StringBuilder builder, int slotTitleRes, ItemType itemType) {
        if (itemType == null) {
            return;
        }
        if (builder.length() > 0) {
            builder.append('\n');
        }
        builder.append(getString(slotTitleRes))
                .append("：")
                .append(itemType.label);
    }

    private void showBuildPlanDialog(boolean replaceAll) {
        BuildPlan plan = presenter.getBuildPlan();
        if (plan.alreadyOptimal) {
            Toast.makeText(this, R.string.game_build_already_optimal, Toast.LENGTH_SHORT).show();
            return;
        }
        if (replaceAll && plan.affordablePurchases.isEmpty()) {
            Toast.makeText(this, getString(R.string.game_gold_not_enough, plan.totalCost - currentGold),
                    Toast.LENGTH_SHORT).show();
            return;
        }
        if (!replaceAll && plan.nextPurchase == null) {
            Toast.makeText(this, R.string.game_build_already_optimal, Toast.LENGTH_SHORT).show();
            return;
        }
        if (!replaceAll && plan.nextPurchase.cost > currentGold) {
            Toast.makeText(this, getString(R.string.game_gold_not_enough,
                    plan.nextPurchase.cost - currentGold), Toast.LENGTH_SHORT).show();
            return;
        }

        String message = replaceAll
                ? buildReplaceDialogMessage(plan)
                : buildNextPurchaseDialogMessage(plan.nextPurchase);
        new AlertDialog.Builder(this)
                .setTitle(R.string.shop_build_dialog_title)
                .setMessage(message)
                .setPositiveButton(R.string.shop_confirm, (dialog, which) ->
                        presenter.applyBuildPlan(replaceAll))
                .setNegativeButton(R.string.shop_cancel, null)
                .show();
    }

    private String buildReplaceDialogMessage(BuildPlan plan) {
        StringBuilder builder = new StringBuilder();
        builder.append(getString(R.string.shop_build_dialog_hint_fountain)).append("\n\n");
        for (ItemType itemType : plan.affordablePurchases) {
            builder.append("• ")
                    .append(itemType.label)
                    .append("（")
                    .append(itemType.cost)
                    .append("金）\n");
        }
        builder.append('\n')
                .append(getString(R.string.shop_build_dialog_message,
                        blueHero == null ? "" : blueHero.label,
                        formatTargetLoadout(plan),
                        plan.purchasesInOrder.size(),
                        plan.totalCost,
                        currentGold,
                        plan.affordablePurchases.size(),
                        plan.affordableCost,
                        getString(R.string.shop_build_dialog_hint_ready)));
        return builder.toString();
    }

    private String buildNextPurchaseDialogMessage(ItemType itemType) {
        BuildPlan plan = presenter.getBuildPlan();
        String reason = plan.nextPurchaseReason.length() > 0
                ? "\n\n" + getString(R.string.shop_smart_reason_format, plan.nextPurchaseReason)
                : "";
        return getString(R.string.shop_buy_confirm_message,
                itemType.label, itemType.cost, currentGold)
                + reason
                + "\n\n"
                + getString(R.string.shop_build_dialog_hint_fountain);
    }

    private void addShopActionButton(int titleRes, boolean primary, Runnable action) {
        Button button = new Button(this);
        button.setAllCaps(false);
        button.setMinHeight(0);
        button.setMinimumHeight(0);
        button.setText(titleRes);
        button.setTextSize(12f);
        button.setTextColor(primary ? getColor(R.color.text) : getColor(R.color.text));
        button.setBackgroundResource(primary ? R.drawable.bg_button : R.drawable.bg_button_gold);
        UiFeedbackHelper.bindClick(button, action);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                getResources().getDimensionPixelSize(R.dimen.battle_button_height));
        params.topMargin = getResources().getDimensionPixelSize(R.dimen.space_xs);
        shopListContainer.addView(button, params);
    }

    private void addShopItemButton(ItemType itemType, boolean confirmBeforeBuy) {
        Button button = new Button(this);
        button.setAllCaps(false);
        button.setMinHeight(0);
        button.setMinimumHeight(0);
        button.setTextColor(getColor(R.color.text));
        button.setTextSize(12f);
        button.setBackgroundResource(R.drawable.bg_button);
        bindShopItem(button, itemType, confirmBeforeBuy);
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                getResources().getDimensionPixelSize(R.dimen.battle_shop_item_height));
        buttonParams.topMargin = getResources().getDimensionPixelSize(R.dimen.space_xs);
        shopListContainer.addView(button, buttonParams);
    }

    private void bindShopItem(Button button, ItemType itemType, boolean confirmBeforeBuy) {
        BuildPlan plan = presenter.getBuildPlan();
        ItemType target = plan.targetLoadout.get(itemType.slot);
        boolean isTarget = target == itemType;
        String suffix = isTarget ? "  ★" : "";
        button.setText(UiTextUtils.shopItem(this, itemType) + suffix);
        UiFeedbackHelper.bindClick(button, () -> {
            if (confirmBeforeBuy) {
                showBuyConfirmDialog(itemType);
            } else {
                presenter.buyItem(itemType);
            }
        });
    }

    private void showBuyConfirmDialog(ItemType itemType) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.shop_buy_confirm_title)
                .setMessage(getString(R.string.shop_buy_confirm_message,
                        itemType.label, itemType.cost, currentGold)
                        + "\n\n"
                        + getString(R.string.shop_build_dialog_hint_fountain))
                .setPositiveButton(R.string.shop_confirm, (dialog, which) ->
                        presenter.buyItem(itemType))
                .setNegativeButton(R.string.shop_cancel, null)
                .show();
    }

    private class BattleActionListener implements com.example.duizhan.view.BattleView.ActionListener {
        @Override
        public void onMove(float x, float y) {
            BattleContract.Presenter activePresenter = presenter;
            if (activePresenter != null) {
                activePresenter.move(x, y);
            }
            if (speech != null && speech.tts() != null) {
                speech.tts().maybePlayMoveLine(blueHero, x, y);
            }
        }
    }
}
