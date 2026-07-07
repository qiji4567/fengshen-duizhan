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

    private static final long NARRATION_START_TIMEOUT_MS = 12000L;
    private static final long MAX_PROLOGUE_MS = 90000L;
    private static final long LINE_FADE_MS = 520L;
    private static final long TTS_WAIT_TIMEOUT_MS = 6000L;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private GameSpeech speech;
    private boolean launchedBattle;
    private boolean ttsHintShown;
    private boolean narrationStarted;

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
            speech.tts().runWhenReady(() -> {
                if (!isFinishing() && !launchedBattle) {
                    narrationStarted = true;
                    speech.tts().playNarration(prologue.fullNarration(), this::launchBattleAfterNarration);
                }
            });
        }

        revealLine(binding.tvPrologueEra, 0L);
        revealLine(binding.tvPrologueMatchup, LINE_FADE_MS);
        revealLine(binding.tvPrologueBlue, LINE_FADE_MS * 2L);
        revealLine(binding.tvPrologueRed, LINE_FADE_MS * 3L);
        revealLine(binding.tvPrologueTheme, LINE_FADE_MS * 4L);
        revealLine(binding.tvPrologueClosing, LINE_FADE_MS * 5L);

        binding.flPrologueRoot.setOnClickListener(v -> launchBattle());
        handler.postDelayed(() -> {
            if (!narrationStarted) {
                launchBattle();
            }
        }, NARRATION_START_TIMEOUT_MS);
        handler.postDelayed(this::launchBattle, MAX_PROLOGUE_MS);
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
        }, TTS_WAIT_TIMEOUT_MS);
    }

    private void launchBattleAfterNarration() {
        if (!launchedBattle) {
            handler.postDelayed(this::launchBattle, 650L);
        }
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
        intent.putExtra(BattleActivity.EXTRA_SKIP_ENTRY_VOICE, true);
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
