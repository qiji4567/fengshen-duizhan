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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.duizhan.R;
import com.example.duizhan.data.BattleRecordDetail;
import com.example.duizhan.model.HistoryModel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryActivity extends Activity {
    private HistoryModel model;
    private LinearLayout listContainer;
    private TextView titleView;
    private TextView metaView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        model = new HistoryModel(this);
        listContainer = findViewById(R.id.llHistoryList);
        titleView = findViewById(R.id.tvTitle);
        metaView = findViewById(R.id.tvHistoryMeta);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadHistoryAsync();
    }

    private void loadHistoryAsync() {
        listContainer.removeAllViews();
        titleView.setText(R.string.history_title);
        metaView.setText("");
        listContainer.addView(text(R.string.last_record_loading, 14, R.color.muted, Typeface.NORMAL));
        model.loadHistory(this::renderHistory);
    }

    private void renderHistory(List<BattleRecordDetail> records) {
        listContainer.removeAllViews();
        if (records == null || records.isEmpty()) {
            titleView.setText(R.string.history_title_empty);
            metaView.setText("");
            listContainer.addView(emptyState());
            return;
        }
        titleView.setText(getString(R.string.history_title_format, records.size()));
        metaView.setText(summaryLine(records));
        listContainer.addView(summaryCard(records));
        for (BattleRecordDetail record : records) {
            listContainer.addView(recordCard(record));
        }
    }

    private View emptyState() {
        LinearLayout card = card();
        card.setGravity(Gravity.CENTER);
        card.setMinimumHeight(dp(150));
        TextView title = text(R.string.no_record, 18, R.color.text, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        TextView hint = text("完成一局后会自动进入这里", 14, R.color.muted, Typeface.NORMAL);
        hint.setGravity(Gravity.CENTER);
        hint.setPadding(0, dp(8), 0, 0);
        card.addView(title);
        card.addView(hint);
        return card;
    }

    private View summaryCard(List<BattleRecordDetail> records) {
        int wins = 0;
        long totalDuration = 0L;
        int replayCount = 0;
        for (BattleRecordDetail record : records) {
            if (record.playerWon) {
                wins++;
            }
            totalDuration += Math.max(0L, record.durationMs);
            if (record.replayJson != null && record.replayJson.length() > 0) {
                replayCount++;
            }
        }
        LinearLayout row = card();
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.addView(metric("总场次", String.valueOf(records.size()), R.color.gold), weightParams());
        row.addView(metric("胜率", String.format(Locale.CHINA, "%.0f%%", wins * 100f / Math.max(1, records.size())), R.color.blue), weightParams());
        row.addView(metric("均时", (totalDuration / Math.max(1, records.size()) / 1000) + "秒", R.color.text), weightParams());
        row.addView(metric("回放", replayCount + "/" + records.size(), R.color.muted), weightParams());
        return row;
    }

    private View recordCard(BattleRecordDetail record) {
        LinearLayout card = card();
        card.setOnClickListener(v -> openDetail(record.id));

        LinearLayout top = horizontal();
        TextView result = chip(resultLabel(record), record.playerWon ? color(R.color.blue) : color(R.color.red), Color.WHITE);
        top.addView(result);
        TextView time = text(formatTime(record.createdAt) + "  ·  " + Math.max(0, record.durationMs / 1000) + "秒",
                13, R.color.muted, Typeface.NORMAL);
        time.setGravity(Gravity.END);
        top.addView(time, weightParams());
        card.addView(top);

        LinearLayout matchup = horizontal();
        matchup.setPadding(0, dp(12), 0, dp(10));
        matchup.addView(heroBlock(record.blueHeroLabel, true), weightParams());
        TextView score = text(record.blueKills + " : " + record.redKills, 24, R.color.text, Typeface.BOLD);
        score.setGravity(Gravity.CENTER);
        matchup.addView(score, new LinearLayout.LayoutParams(dp(104), LinearLayout.LayoutParams.WRAP_CONTENT));
        matchup.addView(heroBlock(record.redHeroLabel, false), weightParams());
        card.addView(matchup);

        LinearLayout stats = horizontal();
        stats.addView(compactStat("经济", record.blueGold + " / " + record.redGold), weightParams());
        stats.addView(compactStat("等级", record.blueLevel + " / " + record.redLevel), weightParams());
        stats.addView(compactStat("录像", hasReplay(record) ? "已保存" : "缺失"), weightParams());
        card.addView(stats);
        return card;
    }

    private View heroBlock(String label, boolean blueSide) {
        LinearLayout box = horizontal();
        box.setGravity(blueSide ? Gravity.CENTER_VERTICAL : Gravity.CENTER_VERTICAL | Gravity.RIGHT);
        TextView avatar = avatar(label, blueSide);
        TextView name = text(shortHero(label), 15, R.color.text, Typeface.BOLD);
        name.setSingleLine(false);
        name.setMaxLines(2);
        TextView side = text(blueSide ? "蓝方" : "红方", 12, blueSide ? R.color.blue : R.color.red, Typeface.BOLD);
        LinearLayout texts = new LinearLayout(this);
        texts.setOrientation(LinearLayout.VERTICAL);
        texts.addView(name);
        texts.addView(side);
        if (blueSide) {
            box.addView(avatar);
            box.addView(texts, weightParams());
        } else {
            texts.setGravity(Gravity.END);
            box.addView(texts, weightParams());
            box.addView(avatar);
        }
        return box;
    }

    private TextView avatar(String label, boolean blueSide) {
        TextView view = text(heroInitial(label), 18, R.color.text, Typeface.BOLD);
        view.setGravity(Gravity.CENTER);
        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.OVAL);
        bg.setColor(blueSide ? Color.rgb(37, 99, 235) : Color.rgb(190, 18, 60));
        bg.setStroke(dp(1), Color.argb(180, 248, 250, 252));
        view.setBackground(bg);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(52), dp(52));
        params.setMargins(blueSide ? 0 : dp(10), 0, blueSide ? dp(10) : 0, 0);
        view.setLayoutParams(params);
        return view;
    }

    private View metric(String label, String value, int valueColorRes) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setGravity(Gravity.CENTER);
        box.addView(text(label, 12, R.color.muted, Typeface.NORMAL));
        TextView valueView = text(value, 18, valueColorRes, Typeface.BOLD);
        valueView.setGravity(Gravity.CENTER);
        box.addView(valueView);
        return box;
    }

    private View compactStat(String label, String value) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(8), dp(7), dp(8), dp(7));
        box.setBackground(round(Color.rgb(15, 23, 42), Color.rgb(51, 65, 85), dp(8)));
        box.addView(text(label, 12, R.color.muted, Typeface.NORMAL));
        box.addView(text(value, 14, R.color.text, Typeface.BOLD));
        return box;
    }

    private LinearLayout card() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(14), dp(12), dp(14), dp(12));
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

    private TextView chip(String text, int bgColor, int textColor) {
        TextView chip = new TextView(this);
        chip.setText(text);
        chip.setTextColor(textColor);
        chip.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        chip.setTypeface(Typeface.DEFAULT_BOLD);
        chip.setPadding(dp(10), dp(5), dp(10), dp(5));
        chip.setBackground(round(bgColor, Color.TRANSPARENT, dp(8)));
        return chip;
    }

    private TextView text(int resId, int sp, int colorRes, int style) {
        return text(getString(resId), sp, colorRes, style);
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

    private String summaryLine(List<BattleRecordDetail> records) {
        return "最近 " + formatTime(records.get(0).createdAt);
    }

    private String resultLabel(BattleRecordDetail record) {
        if (isSnapshotRecord(record)) {
            return getString(R.string.detail_result_snapshot);
        }
        return record.playerWon ? getString(R.string.detail_result_win) : getString(R.string.detail_result_loss);
    }

    private boolean hasReplay(BattleRecordDetail record) {
        return record != null && record.replayJson != null && record.replayJson.length() > 0;
    }

    private String formatTime(long createdAt) {
        return new SimpleDateFormat(getString(R.string.last_record_time_pattern), Locale.CHINA)
                .format(new Date(createdAt));
    }

    private boolean isSnapshotRecord(BattleRecordDetail record) {
        return record != null && "SNAPSHOT".equals(record.winnerTeam);
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

    private void openDetail(long recordId) {
        Intent intent = new Intent(this, BattleDetailActivity.class);
        intent.putExtra(BattleDetailActivity.EXTRA_RECORD_ID, recordId);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        model.close();
        super.onDestroy();
    }
}
