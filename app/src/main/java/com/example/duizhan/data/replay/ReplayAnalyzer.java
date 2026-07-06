package com.example.duizhan.data.replay;

import com.example.duizhan.game.GameConfig;
import com.example.duizhan.game.util.GameMath;

public final class ReplayAnalyzer {
    public float blueDistance;
    public float redDistance;
    public float blueAggressionRate;
    public float redAggressionRate;
    public int frameCount;

    public static ReplayAnalyzer analyze(ReplayData data) {
        ReplayAnalyzer analyzer = new ReplayAnalyzer();
        if (data == null || data.frames.size() < 2) {
            return analyzer;
        }
        analyzer.frameCount = data.frames.size();
        ReplayFrame previous = data.frames.get(0);
        float midX = GameConfig.WORLD_WIDTH / 2f;
        int blueAggressive = 0;
        int redAggressive = 0;
        for (int i = 1; i < data.frames.size(); i++) {
            ReplayFrame frame = data.frames.get(i);
            analyzer.blueDistance += GameMath.distance(previous.blueX, previous.blueY, frame.blueX, frame.blueY);
            analyzer.redDistance += GameMath.distance(previous.redX, previous.redY, frame.redX, frame.redY);
            if (frame.blueX > midX) {
                blueAggressive++;
            }
            if (frame.redX < midX) {
                redAggressive++;
            }
            previous = frame;
        }
        analyzer.blueAggressionRate = blueAggressive * 100f / Math.max(1, data.frames.size());
        analyzer.redAggressionRate = redAggressive * 100f / Math.max(1, data.frames.size());
        return analyzer;
    }
}
