package com.example.duizhan.game.util;

import android.graphics.Color;

import com.example.duizhan.game.Team;

public final class TeamStyle {
    private TeamStyle() {
    }

    public static int color(Team team) {
        if (team == Team.BLUE) {
            return Color.rgb(96, 165, 250);
        }
        if (team == Team.RED) {
            return Color.rgb(248, 113, 113);
        }
        return Color.rgb(245, 158, 11);
    }
}
