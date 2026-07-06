package com.example.duizhan.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.view.View;

import com.example.duizhan.R;
import com.example.duizhan.data.BattleDetailPayload;
import com.example.duizhan.data.BattleRecordDetail;
import com.example.duizhan.game.HeroType;
import com.example.duizhan.game.guide.BuildGuideResolver;
import com.example.duizhan.game.guide.HeroTipsProvider;
import com.example.duizhan.model.DetailModel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BattleDetailActivity extends Activity {
    public static final String EXTRA_RECORD_ID = "extra_record_id";

    private DetailModel model;
    private long recordId;
    private BattleRecordDetail record;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_battle_detail);
        model = new DetailModel(this);
        recordId = getIntent().getLongExtra(EXTRA_RECORD_ID, -1L);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnReplay).setOnClickListener(v -> openReplay());
        loadDetailAsync();
    }

    private void loadDetailAsync() {
        TextView contentView = findViewById(R.id.tvDetailContent);
        contentView.setText(R.string.last_record_loading);
        model.loadDetail(recordId, this::renderDetail);
    }

    private void renderDetail(BattleDetailPayload payload) {
        if (payload == null || payload.record == null) {
            finish();
            return;
        }
        record = payload.record;
        BattleRecordDetail detail = payload.record;
        HeroType blueHero = parseHero(detail.blueHeroKey);
        String time = new SimpleDateFormat(getString(R.string.last_record_time_pattern), Locale.CHINA)
                .format(new Date(detail.createdAt));
        String content = getString(R.string.detail_content_format,
                detail.playerWon ? getString(R.string.detail_result_win) : getString(R.string.detail_result_loss),
                detail.winnerLabel,
                detail.blueHeroLabel,
                detail.redHeroLabel,
                detail.durationMs / 1000,
                time,
                detail.blueKills,
                detail.redKills,
                detail.blueGold,
                detail.redGold,
                detail.blueLevel,
                detail.redLevel,
                emptyIfNull(detail.blueBuild),
                emptyIfNull(detail.redBuild),
                payload.analyzer.blueDistance,
                payload.analyzer.redDistance,
                payload.analyzer.blueAggressionRate,
                payload.analyzer.redAggressionRate,
                payload.replayData.frames.size(),
                BuildGuideResolver.skillOrder(blueHero),
                buildGuideText(blueHero),
                HeroTipsProvider.heroSpecificTips(blueHero),
                HeroTipsProvider.movementTips(blueHero),
                HeroTipsProvider.replayTips());
        TextView contentView = findViewById(R.id.tvDetailContent);
        contentView.setText(content);
        Button replayButton = findViewById(R.id.btnReplay);
        boolean hasReplay = payload.replayData != null && !payload.replayData.frames.isEmpty();
        replayButton.setVisibility(hasReplay ? View.VISIBLE : View.GONE);
    }

    private String buildGuideText(HeroType heroType) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < BuildGuideResolver.recommendedItems(heroType).size(); i++) {
            if (i > 0) {
                builder.append("\n");
            }
            builder.append(i + 1).append(". ")
                    .append(BuildGuideResolver.recommendedItems(heroType).get(i).label);
        }
        return builder.toString();
    }

    private HeroType parseHero(String key) {
        if (key == null) {
            return HeroType.SUN_WUKONG;
        }
        try {
            return HeroType.valueOf(key);
        } catch (IllegalArgumentException ignored) {
            for (HeroType heroType : HeroType.values()) {
                if (heroType.label.equals(key)) {
                    return heroType;
                }
            }
            return HeroType.SUN_WUKONG;
        }
    }

    private String emptyIfNull(String value) {
        return value == null || value.length() == 0 ? getString(R.string.battle_view_equipment_empty) : value;
    }

    private void openReplay() {
        if (record == null) {
            return;
        }
        Intent intent = new Intent(this, ReplayActivity.class);
        intent.putExtra(ReplayActivity.EXTRA_RECORD_ID, record.id);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        model.close();
        super.onDestroy();
    }
}
