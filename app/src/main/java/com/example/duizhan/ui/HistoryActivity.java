package com.example.duizhan.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        model = new HistoryModel(this);
        listContainer = findViewById(R.id.llHistoryList);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadHistoryAsync();
    }

    private void loadHistoryAsync() {
        listContainer.removeAllViews();
        TextView loadingView = new TextView(this);
        loadingView.setText(R.string.last_record_loading);
        loadingView.setTextColor(getColor(R.color.muted));
        listContainer.addView(loadingView);
        model.loadHistory(this::renderHistory);
    }

    private void renderHistory(List<BattleRecordDetail> records) {
        listContainer.removeAllViews();
        TextView titleView = findViewById(R.id.tvTitle);
        if (records == null || records.isEmpty()) {
            titleView.setText(R.string.history_title_empty);
            addEmptyView();
            return;
        }
        titleView.setText(getString(R.string.history_title_format, records.size()));
        TextView hintView = new TextView(this);
        hintView.setText(R.string.history_persist_hint);
        hintView.setTextColor(getColor(R.color.muted));
        hintView.setTextSize(getResources().getDimension(R.dimen.text_xs));
        hintView.setPadding(0, getResources().getDimensionPixelSize(R.dimen.space_xs), 0, 0);
        listContainer.addView(hintView);
        for (BattleRecordDetail record : records) {
            addRecordButton(record);
        }
    }

    private void addEmptyView() {
        TextView emptyView = new TextView(this);
        emptyView.setText(R.string.no_record);
        emptyView.setTextColor(getColor(R.color.muted));
        listContainer.addView(emptyView);
    }

    private void addRecordButton(BattleRecordDetail record) {
        Button button = new Button(this);
        button.setAllCaps(false);
        button.setBackgroundResource(R.drawable.bg_panel);
        button.setTextColor(getColor(R.color.text));
        button.setText(buildRecordLine(record));
        button.setOnClickListener(v -> openDetail(record.id));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.topMargin = getResources().getDimensionPixelSize(R.dimen.space_xs);
        listContainer.addView(button, params);
    }

    private String buildRecordLine(BattleRecordDetail record) {
        String time = new SimpleDateFormat(getString(R.string.last_record_time_pattern), Locale.CHINA)
                .format(new Date(record.createdAt));
        String resultLabel = isSnapshotRecord(record)
                ? getString(R.string.detail_result_snapshot)
                : (record.playerWon ? getString(R.string.detail_result_win) : getString(R.string.detail_result_loss));
        return getString(R.string.history_item_format,
                resultLabel,
                record.blueHeroLabel,
                record.redHeroLabel,
                record.blueKills,
                record.redKills,
                record.durationMs / 1000,
                time);
    }

    private boolean isSnapshotRecord(BattleRecordDetail record) {
        return record != null && "SNAPSHOT".equals(record.winnerTeam);
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
