package com.example.duizhan.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.widget.TextView;
import android.widget.Toast;

import com.example.duizhan.R;
import com.example.duizhan.databinding.ActivityBattlePrologueBinding;
import com.example.duizhan.game.HeroType;
import com.example.duizhan.game.story.BattlePrologue;
import com.example.duizhan.game.story.BattlePrologueBuilder;
import com.example.duizhan.ui.audio.GameSpeech;
import com.example.duizhan.ui.base.BaseActivity;

public class BattlePrologueActivity extends BaseActivity<ActivityBattlePrologueBinding> {
    public static final String EXTRA_BLUE_HERO = BattleActivity.EXTRA_BLUE_HERO;
    public static final String EXTRA_RED_HERO = BattleActivity.EXTRA_RED_HERO;
    public static final String EXTRA_DIFFICULTY = BattleActivity.EXTRA_DIFFICULTY;

    private static final long AUTO_ADVANCE_MS = 32000L;
    private static final long LINE_FADE_MS = 520L;
    private static final long TTS_WAIT_TIMEOUT_MS = 4500L;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private GameSpeech speech;
    private boolean launchedBattle;
    private boolean ttsHintShown;

    @Override
    protected ActivityBattlePrologueBinding inflateBinding(LayoutInflater inflater) {
        return ActivityBattlePrologueBinding.inflate(inflater);
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        HeroType blueHero = readHero(EXTRA_BLUE_HERO, HeroType.SUN_WUKONG);
        HeroType redHero = readHero(EXTRA_RED_HERO, HeroType.ERLANG_SHEN);
        BattlePrologue prologue = BattlePrologueBuilder.build(blueHero, redHero);

        binding.tvPrologueEra.setText(prologue.eraTitle);
        binding.tvPrologueMatchup.setText(prologue.matchupLine);
        binding.tvPrologueBlue.setText(prologue.blueLine);
        binding.tvPrologueRed.setText(prologue.redLine);
        binding.tvPrologueTheme.setText(prologue.themeLine);
        binding.tvPrologueClosing.setText(prologue.closingLine);

        speech = GameSpeech.get(this);
        scheduleTtsUnavailableHint();
        if (speech != null && speech.tts() != null) {
            speech.tts().runWhenReady(() -> playPrologueNarration(prologue));
        }

        revealLine(binding.tvPrologueEra, 0L);
        revealLine(binding.tvPrologueMatchup, LINE_FADE_MS);
        revealLine(binding.tvPrologueBlue, LINE_FADE_MS * 2L);
        revealLine(binding.tvPrologueRed, LINE_FADE_MS * 3L);
        revealLine(binding.tvPrologueTheme, LINE_FADE_MS * 4L);
        revealLine(binding.tvPrologueClosing, LINE_FADE_MS * 5L);

        binding.flPrologueRoot.setOnClickListener(v -> launchBattle());
        handler.postDelayed(this::launchBattle, AUTO_ADVANCE_MS);
    }

    private void playPrologueNarration(BattlePrologue prologue) {
        String[] lines = {
                prologue.eraTitle,
                prologue.matchupLine,
                prologue.blueLine,
                prologue.redLine,
                prologue.themeLine,
                prologue.closingLine
        };
        speakPrologueLine(lines, 0);
    }

    private void speakPrologueLine(String[] lines, int index) {
        if (index >= lines.length || isFinishing() || speech == null || speech.tts() == null) {
            return;
        }
        final int nextIndex = index + 1;
        speech.tts().playNarrationLine(lines[index] + "。", index == 0, () -> {
            if (!isFinishing() && nextIndex < lines.length) {
                handler.postDelayed(() -> speakPrologueLine(lines, nextIndex), 180L);
            }
        });
    }

    private void scheduleTtsUnavailableHint() {
        handler.postDelayed(() -> {
            if (isFinishing() || ttsHintShown || speech == null) {
                return;
            }
            if (speech.isTtsReady()) {
                return;
            }
            ttsHintShown = true;
            Toast.makeText(this, R.string.tts_unavailable_hint, Toast.LENGTH_LONG).show();
            speech.openTtsInstall(this);
        }, TTS_WAIT_TIMEOUT_MS);
    }

    private void revealLine(TextView view, long delayMs) {
        view.animate().cancel();
        view.setAlpha(0f);
        view.animate()
                .alpha(1f)
                .setStartDelay(delayMs)
                .setDuration(LINE_FADE_MS)
                .start();
    }

    private void launchBattle() {
        if (launchedBattle || isFinishing()) {
            return;
        }
        launchedBattle = true;
        handler.removeCallbacksAndMessages(null);
        if (speech != null) {
            speech.stopSpeaking();
        }
        Intent intent = new Intent(this, BattleActivity.class);
        intent.putExtra(EXTRA_BLUE_HERO, getIntent().getStringExtra(EXTRA_BLUE_HERO));
        intent.putExtra(EXTRA_RED_HERO, getIntent().getStringExtra(EXTRA_RED_HERO));
        intent.putExtra(EXTRA_DIFFICULTY, getIntent().getStringExtra(EXTRA_DIFFICULTY));
        startActivity(intent);
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
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

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
