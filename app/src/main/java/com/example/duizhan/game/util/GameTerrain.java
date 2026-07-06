package com.example.duizhan.game.util;

import com.example.duizhan.game.GameEntity;
import com.example.duizhan.game.HeroArchetype;
import com.example.duizhan.game.HeroType;
import com.example.duizhan.game.SkillStyle;
import com.example.duizhan.game.Team;

public final class GameTerrain {
    private static final float BRUSH_REVEAL_DISTANCE = 360f;

    public static final class EllipseRegion {
        public final float cx;
        public final float cy;
        public final float rx;
        public final float ry;

        public EllipseRegion(float cx, float cy, float rx, float ry) {
            this.cx = cx;
            this.cy = cy;
            this.rx = rx;
            this.ry = ry;
        }
    }

    public static final class RectRegion {
        public final float left;
        public final float top;
        public final float right;
        public final float bottom;

        public RectRegion(float left, float top, float right, float bottom) {
            this.left = left;
            this.top = top;
            this.right = right;
            this.bottom = bottom;
        }
    }

    public static final EllipseRegion[] BRUSHES = {
            new EllipseRegion(1440f, 2946f, 340f, 184f),
            new EllipseRegion(2320f, 4394f, 380f, 172f),
            new EllipseRegion(3680f, 2422f, 410f, 180f),
            new EllipseRegion(4940f, 3420f, 370f, 168f),
            new EllipseRegion(5880f, 4656f, 340f, 184f),
            new EllipseRegion(1240f, 4656f, 290f, 156f),
            new EllipseRegion(6400f, 2470f, 310f, 164f),
    };

    public static final RectRegion[] WALLS = {
            new RectRegion(2020f, 3516f, 2520f, 3898f),
            new RectRegion(3460f, 4916f, 4110f, 5296f),
            new RectRegion(4810f, 2398f, 5460f, 2778f),
            new RectRegion(2960f, 3230f, 3170f, 3610f),
    };

    public static final EllipseRegion[] PONDS = {
            new EllipseRegion(2760f, 1996f, 236f, 136f),
            new EllipseRegion(4100f, 5606f, 264f, 144f),
            new EllipseRegion(5360f, 4228f, 208f, 124f),
            new EllipseRegion(1960f, 5606f, 192f, 116f),
    };

    public static final EllipseRegion[] GRASS_PATCHES = {
            new EllipseRegion(860f, 2470f, 420f, 240f),
            new EllipseRegion(2360f, 5606f, 480f, 260f),
            new EllipseRegion(3960f, 3610f, 520f, 280f),
            new EllipseRegion(5720f, 5606f, 440f, 250f),
            new EllipseRegion(6240f, 3230f, 360f, 220f),
    };

    public static final float[] TREE_X = {
            1040f, 1680f, 3020f, 4480f, 6180f, 1360f, 2640f, 3520f, 5160f, 6680f
    };
    public static final float[] TREE_Y = {
            1948f, 5272f, 1710f, 5510f, 2042f, 3610f, 4940f, 2186f, 4656f, 3610f
    };

    public static final float RIVER_X1 = 1580f;
    public static final float RIVER_Y1 = 2232f;
    public static final float RIVER_X2 = 5680f;
    public static final float RIVER_Y2 = 5130f;

    private GameTerrain() {
    }

    public static boolean isInBrush(float x, float y) {
        for (EllipseRegion brush : BRUSHES) {
            if (inEllipse(x, y, brush)) {
                return true;
            }
        }
        return false;
    }

    public static boolean blocksGround(float x, float y) {
        for (RectRegion wall : WALLS) {
            if (inRect(x, y, wall)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isInWater(float x, float y) {
        for (EllipseRegion pond : PONDS) {
            if (inEllipse(x, y, pond)) {
                return true;
            }
        }
        return nearRiver(x, y, 96f);
    }

    public static boolean canLeapWall(HeroType heroType) {
        if (heroType == null) {
            return false;
        }
        if (heroType == HeroType.SUN_WUKONG) {
            return true;
        }
        if (heroType.archetype() == HeroArchetype.ASSASSIN) {
            return true;
        }
        SkillStyle style = heroType.skillStyle;
        return style == SkillStyle.FIRE_WHEEL || style == SkillStyle.DASH_SWEEP;
    }

    public static void applyDash(GameEntity hero, float distance) {
        float dir = hero.team == Team.BLUE ? 1f : -1f;
        float targetX = hero.x + dir * distance;
        float targetY = hero.y + (hero.team == Team.BLUE ? -36f : 36f);
        moveHeroToDashTarget(hero, targetX, targetY);
    }

    public static void applyDashToward(GameEntity hero, GameEntity target, float distance) {
        if (hero == null || target == null) {
            if (hero != null) {
                applyDash(hero, distance);
            }
            return;
        }
        float dx = target.x - hero.x;
        float dy = target.y - hero.y;
        float dist = GameMath.distance(0f, 0f, dx, dy);
        if (dist < 1f) {
            applyDash(hero, distance);
            return;
        }
        float stopGap = Math.max(24f, target.radius + 18f);
        float moveDistance = Math.min(distance, Math.max(0f, dist - stopGap));
        if (moveDistance <= 0f) {
            return;
        }
        float targetX = hero.x + dx / dist * moveDistance;
        float targetY = hero.y + dy / dist * moveDistance;
        moveHeroToDashTarget(hero, targetX, targetY);
    }

    private static void moveHeroToDashTarget(GameEntity hero, float targetX, float targetY) {
        float dir = targetX >= hero.x ? 1f : -1f;
        if (canLeapWall(hero.heroType) && blocksGround(targetX, targetY)) {
            for (int step = 1; step <= 10; step++) {
                float leapX = hero.x + dir * (Math.abs(targetX - hero.x) + step * 42f);
                float leapY = targetY + (hero.team == Team.BLUE ? -step * 4f : step * 4f);
                if (!blocksGround(leapX, leapY)) {
                    targetX = leapX;
                    targetY = leapY;
                    break;
                }
            }
        }
        if (!blocksGround(targetX, targetY) || canLeapWall(hero.heroType)) {
            hero.x = targetX;
            hero.y = targetY;
        }
    }

    /**
     * When movement hits a wall, slide along it by trying horizontal then vertical components.
     */
    public static float[] resolveSlide(float x, float y, float dx, float dy) {
        float targetX = x + dx;
        float targetY = y + dy;
        if (!blocksGround(targetX, targetY)) {
            return new float[]{targetX, targetY};
        }
        if (Math.abs(dx) > 0.001f && !blocksGround(targetX, y)) {
            return new float[]{targetX, y};
        }
        if (Math.abs(dy) > 0.001f && !blocksGround(x, targetY)) {
            return new float[]{x, targetY};
        }
        return new float[]{x, y};
    }

    public static boolean canSee(GameEntity from, GameEntity target) {
        if (from == null || target == null) {
            return false;
        }
        if (from.team == target.team || target.team == Team.NEUTRAL) {
            return true;
        }
        if (target.stealthTimer > 0f || target.mimicTimer > 0f) {
            return from.distanceTo(target) <= BRUSH_REVEAL_DISTANCE * 0.72f;
        }
        if (!isInBrush(target.x, target.y)) {
            return true;
        }
        return isInBrush(from.x, from.y) || from.distanceTo(target) <= BRUSH_REVEAL_DISTANCE;
    }

    private static boolean nearRiver(float x, float y, float threshold) {
        float dx = RIVER_X2 - RIVER_X1;
        float dy = RIVER_Y2 - RIVER_Y1;
        float lenSq = dx * dx + dy * dy;
        if (lenSq <= 0f) {
            return false;
        }
        float t = ((x - RIVER_X1) * dx + (y - RIVER_Y1) * dy) / lenSq;
        t = Math.max(0f, Math.min(1f, t));
        float px = RIVER_X1 + t * dx;
        float py = RIVER_Y1 + t * dy;
        float dist = GameMath.distance(x, y, px, py);
        return dist <= threshold;
    }

    private static boolean inRect(float x, float y, RectRegion rect) {
        return x >= rect.left && x <= rect.right && y >= rect.top && y <= rect.bottom;
    }

    private static boolean inEllipse(float x, float y, EllipseRegion region) {
        float dx = (x - region.cx) / region.rx;
        float dy = (y - region.cy) / region.ry;
        return dx * dx + dy * dy <= 1f;
    }
}
