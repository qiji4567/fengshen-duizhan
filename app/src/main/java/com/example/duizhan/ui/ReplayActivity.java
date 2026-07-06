package com.example.duizhan.ui;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.duizhan.R;
import com.example.duizhan.data.BattleRecordDetail;
import com.example.duizhan.data.ReplaySession;
import com.example.duizhan.data.replay.ReplayData;
import com.example.duizhan.data.replay.ReplayFrame;
import com.example.duizhan.data.replay.ReplayPlayer;
import com.example.duizhan.game.GameSnapshot;
import com.example.duizhan.game.HeroType;
import com.example.duizhan.model.ReplayModel;
import com.example.duizhan.ui.util.UiFeedbackHelper;
import com.example.duizhan.view.BattleView;

public class ReplayActivity extends Activity {
    public static final String EXTRA_RECORD_ID = "extra_record_id";

    private final Handler handler = new Handler(Looper.getMainLooper());
    private ReplayModel model;
    private BattleView battleView;
    private SeekBar seekBar;
    private TextView timeView;
    private ReplayPlayer replayPlayer;
    private ReplayData replayData;
    private BattleRecordDetail record;
    private HeroType blueHero;
    private HeroType redHero;
    private boolean playing;
    private long replayNextAdvanceMs;
    private final Runnable tickRunnable = this::advanceReplay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_replay);
        model = new ReplayModel(this);
        battleView = findViewById(R.id.bvReplay);
        seekBar = findViewById(R.id.sbReplay);
        timeView = findViewById(R.id.tvReplayTime);
        battleView.setReplayMode(true);
        UiFeedbackHelper.bindClick(findViewById(R.id.btnBack), this::finish);
        UiFeedbackHelper.bindClick(findViewById(R.id.btnPlayPause), this::togglePlay);
        UiFeedbackHelper.bindClick(findViewById(R.id.btnCamBlue), () ->
                battleView.setCameraFollowMode(BattleView.CameraFollowMode.BLUE));
        UiFeedbackHelper.bindClick(findViewById(R.id.btnCamRed), () ->
                battleView.setCameraFollowMode(BattleView.CameraFollowMode.RED));
        UiFeedbackHelper.bindClick(findViewById(R.id.btnCamThird), () ->
                battleView.setCameraFollowMode(BattleView.CameraFollowMode.THIRD));
        UiFeedbackHelper.bindClick(findViewById(R.id.btnCamFree), () ->
                battleView.setCameraFollowMode(BattleView.CameraFollowMode.FREE));
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && replayPlayer != null) {
                    replayPlayer.seek(progress);
                    renderCurrentFrame();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                playing = false;
                replayNextAdvanceMs = 0L;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        long recordId = getIntent().getLongExtra(EXTRA_RECORD_ID, -1L);
        loadReplayAsync(recordId);
    }

    private void loadReplayAsync(long recordId) {
        timeView.setText(R.string.last_record_loading);
        model.loadSession(recordId, this::renderSession);
    }

    private void renderSession(ReplaySession session) {
        if (session == null || session.record == null || session.replayPlayer == null) {
            finish();
            return;
        }
        record = session.record;
        replayData = session.replayData;
        replayPlayer = session.replayPlayer;
        blueHero = parseHero(record.blueHeroKey);
        redHero = parseHero(record.redHeroKey);
        battleView.clearTrails();
        for (ReplayFrame frame : replayData.frames) {
            battleView.appendTrailPoint(frame.blueX, frame.blueY, frame.redX, frame.redY);
        }
        seekBar.setMax(Math.max(0, replayPlayer.frameCount() - 1));
        renderCurrentFrame();
        playing = true;
        replayNextAdvanceMs = 0L;
        scheduleNextFrame();
    }

    private void renderCurrentFrame() {
        if (replayPlayer == null || record == null) {
            return;
        }
        GameSnapshot snapshot = replayPlayer.buildSnapshot(
                blueHero,
                redHero,
                record.blueHeroLabel,
                record.redHeroLabel);
        battleView.setSnapshot(snapshot);
        ReplayFrame frame = replayPlayer.currentFrame();
        if (frame != null) {
            timeView.setText(getString(R.string.replay_time_format,
                    frame.timeMs / 1000,
                    record.durationMs / 1000,
                    replayPlayer.currentIndex() + 1,
                    replayPlayer.frameCount()));
        }
        seekBar.setProgress(replayPlayer.currentIndex());
    }

    private void togglePlay() {
        playing = !playing;
        Button playPause = findViewById(R.id.btnPlayPause);
        playPause.setText(playing ? R.string.replay_pause : R.string.replay_play);
        if (playing) {
            scheduleNextFrame();
        } else {
            handler.removeCallbacks(tickRunnable);
        }
    }

    private void scheduleNextFrame() {
        handler.removeCallbacks(tickRunnable);
        if (playing && replayData != null) {
            battleView.postOnAnimation(tickRunnable);
        }
    }

    private void advanceReplay() {
        if (replayPlayer == null || replayData == null) {
            return;
        }
        if (!playing) {
            return;
        }
        long now = System.currentTimeMillis();
        if (replayNextAdvanceMs == 0L) {
            replayNextAdvanceMs = now;
        }
        if (now < replayNextAdvanceMs) {
            scheduleNextFrame();
            return;
        }
        replayNextAdvanceMs = now + Math.max(16L, replayData.intervalMs);
        if (!replayPlayer.advance()) {
            playing = false;
            ((Button) findViewById(R.id.btnPlayPause)).setText(R.string.replay_play);
            return;
        }
        renderCurrentFrame();
        scheduleNextFrame();
    }

    private HeroType parseHero(String key) {
        try {
            return HeroType.valueOf(key);
        } catch (IllegalArgumentException ignored) {
            return HeroType.SUN_WUKONG;
        }
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(tickRunnable);
        model.close();
        super.onDestroy();
    }
}
