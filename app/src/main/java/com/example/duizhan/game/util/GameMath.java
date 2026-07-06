package com.example.duizhan.game.util;

import com.example.duizhan.game.GameEntity;

public final class GameMath {
    private GameMath() {
    }

    public static float distance(float x1, float y1, float x2, float y2) {
        float dx = x1 - x2;
        float dy = y1 - y2;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    public static float vectorLength(float x, float y) {
        return (float) Math.sqrt(x * x + y * y);
    }

    public static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    public static void clampEntity(GameEntity entity, float minX, float maxX, float minY, float maxY) {
        entity.x = clamp(entity.x, minX, maxX);
        entity.y = clamp(entity.y, minY, maxY);
    }

    public static void moveToward(GameEntity entity, float targetX, float targetY, float dt, float speed) {
        float dx = targetX - entity.x;
        float dy = targetY - entity.y;
        float len = Math.max(1f, vectorLength(dx, dy));
        entity.x += dx / len * speed * dt;
        entity.y += dy / len * speed * dt;
    }

    public static float distancePointToSegment(float px, float py, float x1, float y1, float x2, float y2) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        if (Math.abs(dx) < 0.001f && Math.abs(dy) < 0.001f) {
            return distance(px, py, x1, y1);
        }
        float t = ((px - x1) * dx + (py - y1) * dy) / (dx * dx + dy * dy);
        t = clamp(t, 0f, 1f);
        float closestX = x1 + t * dx;
        float closestY = y1 + t * dy;
        return distance(px, py, closestX, closestY);
    }
}
