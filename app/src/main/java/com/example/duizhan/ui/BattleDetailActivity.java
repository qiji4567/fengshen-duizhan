package com.example.duizhan.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

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
    private LinearLayout contentContainer;
    private Button replayButton;
    private BattleRecordDetail record;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_battle_detail);
        model = new DetailModel(this);
        contentContainer = findViewById(R.id.llDetailContent);
        replayButton = findViewById(R.id.btnReplay);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        replayButton.setOnClickListener(v -> openReplay());
        loadDetailAsync(getIntent().getLongExtra(EXTRA_RECORD_ID, -1L));
    }

    private void loadDetailAsync(long recordId) {
        contentContainer.removeAllViews();
        contentContainer.addView(statusCard(getString(R.string.last_record_loading)));
        model.loadDetail(recordId, this::renderDetail);
    }

    private void renderDetail(BattleDetailPayload payload) {
        contentContainer.removeAllViews();
        if (payload == null || payload.record == null) {
            contentContainer.addView(statusCard("战绩不存在或已损坏"));
            replayButton.setVisibility(View.GONE);
            return;
        }
        record = payload.record;
        BattleRecordDetail detail = payload.record;
        HeroType blueHero = parseHero(detail.blueHeroKey);
        HeroType redHero = parseHero(detail.redHeroKey);
        replayButton.setVisibility(payload.hasReplay ? View.VISIBLE : View.GONE);

        contentContainer.addView(matchHeader(detail));
        contentContainer.addView(statGrid(detail, payload));
        contentContainer.addView(buildSection("本局出装",
                "蓝方  " + emptyIfNull(detail.blueBuild) + "\n红方  " + emptyIfNull(detail.redBuild)));
        contentContainer.addView(buildSection("蓝方英雄建议",
                "加点  " + BuildGuideResolver.skillOrder(blueHero)
                        + "\n\n出装\n" + buildGuideText(blueHero)
                        + "\n\n技巧\n" + HeroTipsProvider.heroSpecificTips(blueHero)));
        contentContainer.addView(buildSection("红方情报",
                redHero.label + " · " + redHero.faction + " · " + redHero.role
                        + "\n" + redHero.skillDescription));
    }

    private View matchHeader(BattleRecordDetail detail) {
        LinearLayout card = card();
        LinearLayout top = horizontal();
        top.addView(chip(detail.playerWon ? "蓝方胜利" : detail.winnerLabel + "胜利",
                detail.playerWon ? color(R.color.blue) : color(R.color.red)));
        TextView time = text(formatTime(detail.createdAt) + "  ·  " + Math.max(0, detail.durationMs / 1000) + "秒",
                13, R.color.muted, Typeface.NORMAL);
        time.setGravity(Gravity.END);
        top.addView(time, weightParams());
        card.addView(top);

        LinearLayout matchup = horizontal();
        matchup.setPadding(0, dp(16), 0, dp(8));
        matchup.addView(heroPanel(detail.blueHeroLabel, true), weightParams());
        TextView score = text(detail.blueKills + " : " + detail.redKills, 30, R.color.text, Typeface.BOLD);
        score.setGravity(Gravity.CENTER);
        matchup.addView(score, new LinearLayout.LayoutParams(dp(126), LinearLayout.LayoutParams.WRAP_CONTENT));
        matchup.addView(heroPanel(detail.redHeroLabel, false), weightParams());
        card.addView(matchup);
        return card;
    }

    private View statGrid(BattleRecordDetail detail, BattleDetailPayload payload) {
        LinearLayout card = card();
        LinearLayout rowOne = horizontal();
        rowOne.addView(metric("录像帧", String.valueOf(payload.replayFrameCount), R.color.gold), weightParams());
        rowOne.addView(metric("经济", detail.blueGold + " / " + detail.redGold, R.color.text), weightParams());
        rowOne.addView(metric("等级", detail.blueLevel + " / " + detail.redLevel, R.color.text), weightParams());
        card.addView(rowOne);

        LinearLayout rowTwo = horizontal();
        rowTwo.setPadding(0, dp(10), 0, 0);
        rowTwo.addView(metric("胜者", detail.winnerLabel, detail.playerWon ? R.color.blue : R.color.red), weightParams());
        rowTwo.addView(metric("录像", payload.hasReplay ? "可回放" : "缺失", payload.hasReplay ? R.color.gold : R.color.red), weightParams());
        rowTwo.addView(metric("类型", "完整保存", R.color.muted), weightParams());
        card.addView(rowTwo);
        return card;
    }

    private View heroPanel(String label, boolean blueSide) {
        LinearLayout panel = horizontal();
        panel.setGravity(blueSide ? Gravity.CENTER_VERTICAL : Gravity.CENTER_VERTICAL | Gravity.RIGHT);
        TextView avatar = avatar(label, blueSide);
        LinearLayout texts = new LinearLayout(this);
        texts.setOrientation(LinearLayout.VERTICAL);
        TextView name = text(shortHero(label), 18, R.color.text, Typeface.BOLD);
        TextView full = text(label, 12, R.color.muted, Typeface.NORMAL);
        texts.addView(name);
        texts.addView(full);
        if (blueSide) {
            panel.addView(avatar);
            panel.addView(texts, weightParams());
        } else {
            texts.setGravity(Gravity.END);
            panel.addView(texts, weightParams());
            panel.addView(avatar);
        }
        return panel;
    }

    private View buildSection(String title, String body) {
        LinearLayout card = card();
        card.addView(text(title, 16, R.color.gold, Typeface.BOLD));
        TextView bodyView = text(body, 14, R.color.text, Typeface.NORMAL);
        bodyView.setPadding(0, dp(8), 0, 0);
        bodyView.setLineSpacing(dp(3), 1f);
        card.addView(bodyView);
        return card;
    }

    private View statusCard(String message) {
        LinearLayout card = card();
        card.setGravity(Gravity.CENTER);
        card.setMinimumHeight(dp(150));
        TextView view = text(message, 16, R.color.muted, Typeface.BOLD);
        view.setGravity(Gravity.CENTER);
        card.addView(view);
        return card;
    }

    private View metric(String label, String value, int valueColor) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(10), dp(9), dp(10), dp(9));
        box.setBackground(round(Color.rgb(15, 23, 42), Color.rgb(51, 65, 85), dp(8)));
        box.addView(text(label, 12, R.color.muted, Typeface.NORMAL));
        box.addView(text(value, 16, valueColor, Typeface.BOLD));
        return box;
    }

    private TextView avatar(String label, boolean blueSide) {
        TextView view = text(heroInitial(label), 22, R.color.text, Typeface.BOLD);
        view.setGravity(Gravity.CENTER);
        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.OVAL);
        bg.setColor(blueSide ? Color.rgb(37, 99, 235) : Color.rgb(190, 18, 60));
        bg.setStroke(dp(1), Color.argb(190, 248, 250, 252));
        view.setBackground(bg);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(68), dp(68));
        params.setMargins(blueSide ? 0 : dp(14), 0, blueSide ? dp(14) : 0, 0);
        view.setLayoutParams(params);
        return view;
    }

    private LinearLayout card() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16), dp(14), dp(16), dp(14));
        card.setBackground(round(Color.rgb(31, 41, 55), Color.rgb(51, 65, 85), dp(8)));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, dp(10));
        card.setLayoutParams(params);
        return card;
    }

    private LinearLayout horizontal() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        return row;
    }

    private TextView chip(String value, int bgColor) {
        TextView chip = text(value, 12, R.color.text, Typeface.BOLD);
        chip.setPadding(dp(10), dp(5), dp(10), dp(5));
        chip.setBackground(round(bgColor, Color.TRANSPARENT, dp(8)));
        return chip;
    }

    private TextView text(String value, int sp, int colorRes, int style) {
        TextView view = new TextView(this);
        view.setText(value == null ? "" : value);
        view.setTextColor(color(colorRes));
        view.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp);
        view.setTypeface(Typeface.DEFAULT, style);
        return view;
    }

    private GradientDrawable round(int fill, int stroke, int radius) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(fill);
        drawable.setCornerRadius(radius);
        if (stroke != Color.TRANSPARENT) {
            drawable.setStroke(dp(1), stroke);
        }
        return drawable;
    }

    private LinearLayout.LayoutParams weightParams() {
        return new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
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

    private String formatTime(long createdAt) {
        return new SimpleDateFormat(getString(R.string.last_record_time_pattern), Locale.CHINA)
                .format(new Date(createdAt));
    }

    private String shortHero(String label) {
        if (label == null || label.length() == 0) {
            return "未知英雄";
        }
        int index = label.indexOf('·');
        return index >= 0 && index + 1 < label.length() ? label.substring(index + 1) : label;
    }

    private String heroInitial(String label) {
        String name = shortHero(label);
        return name.length() == 0 ? "?" : name.substring(0, 1);
    }

    private int dp(int value) {
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value,
                getResources().getDisplayMetrics()));
    }

    private int color(int colorRes) {
        return getColor(colorRes);
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
