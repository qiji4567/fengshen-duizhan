package com.example.duizhan.ui;

import android.app.Activity;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.duizhan.R;
import com.example.duizhan.data.HeroBattleStats;
import com.example.duizhan.data.StatsDashboard;
import com.example.duizhan.model.StatsModel;

public class StatsActivity extends Activity {
    private StatsModel model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);
        model = new StatsModel(this);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        loadStatsAsync();
    }

    private void loadStatsAsync() {
        TextView playerStatsView = findViewById(R.id.tvPlayerStats);
        LinearLayout heroStatsContainer = findViewById(R.id.llHeroStats);
        playerStatsView.setText(R.string.last_record_loading);
        heroStatsContainer.removeAllViews();
        model.loadDashboard(dashboard -> renderDashboard(playerStatsView, heroStatsContainer, dashboard));
    }

    private void renderDashboard(TextView playerStatsView, LinearLayout heroStatsContainer,
                                 StatsDashboard dashboard) {
        if (dashboard == null) {
            playerStatsView.setText(R.string.no_record);
            return;
        }
        playerStatsView.setText(getString(R.string.stats_player_format,
                dashboard.playerStats.totalBattles,
                dashboard.playerStats.wins,
                dashboard.playerStats.losses,
                dashboard.playerStats.winRate,
                dashboard.playerStats.avgDurationMs / 1000,
                dashboard.playerStats.totalKills,
                dashboard.playerStats.totalDeaths,
                dashboard.playerStats.avgKda));
        heroStatsContainer.removeAllViews();
        if (dashboard.heroStats.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText(R.string.stats_no_hero_data);
            empty.setTextColor(getColor(R.color.muted));
            heroStatsContainer.addView(empty);
            return;
        }
        for (HeroBattleStats heroStats : dashboard.heroStats) {
            addHeroStatRow(heroStatsContainer, heroStats);
        }
    }

    private void addHeroStatRow(LinearLayout container, HeroBattleStats heroStats) {
        TextView row = new TextView(this);
        row.setBackgroundResource(R.drawable.bg_panel);
        row.setTextColor(getColor(R.color.text));
        row.setText(getString(R.string.stats_hero_item_format,
                heroStats.heroLabel,
                heroStats.battles,
                heroStats.wins,
                heroStats.winRate,
                heroStats.avgKills));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.topMargin = getResources().getDimensionPixelSize(R.dimen.space_xs);
        container.addView(row, params);
    }

    @Override
    protected void onDestroy() {
        model.close();
        super.onDestroy();
    }
}
