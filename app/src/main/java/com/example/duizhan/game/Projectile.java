package com.example.duizhan.game;

public class Projectile {
    public final Team team;
    public final long ownerId;
    public final long targetId;
    public float x;
    public float y;
    public float speed;
    public float damage;
    public DamageType damageType;
    public float radius;
    public float splashRadius;
    public int color;
    public String label;
    public ProjectileVisual visual = ProjectileVisual.BOLT;
    public float angleRad;
    public boolean alive = true;

    public Projectile(Team team, long ownerId, long targetId, float x, float y, float speed,
                      float damage, float radius, float splashRadius, int color, String label) {
        this(team, ownerId, targetId, x, y, speed, damage, DamageType.PHYSICAL, radius, splashRadius, color, label);
    }

    public Projectile(Team team, long ownerId, long targetId, float x, float y, float speed,
                      float damage, DamageType damageType, float radius, float splashRadius, int color,
                      ProjectileVisual visual) {
        this.team = team;
        this.ownerId = ownerId;
        this.targetId = targetId;
        this.x = x;
        this.y = y;
        this.speed = speed;
        this.damage = damage;
        this.damageType = damageType;
        this.radius = radius;
        this.splashRadius = splashRadius;
        this.color = color;
        this.visual = visual == null ? ProjectileVisual.BOLT : visual;
        this.label = "";
    }

    public Projectile(Team team, long ownerId, long targetId, float x, float y, float speed,
                      float damage, DamageType damageType, float radius, float splashRadius, int color, String label) {
        this(team, ownerId, targetId, x, y, speed, damage, damageType, radius, splashRadius, color,
                ProjectileVisual.BOLT);
        this.label = label == null ? "" : label;
        this.visual = inferVisual(label, damageType);
    }

    private static ProjectileVisual inferVisual(String label, DamageType damageType) {
        if (label == null) {
            return damageType == DamageType.MAGIC ? ProjectileVisual.MAGIC_ORB : ProjectileVisual.BOLT;
        }
        if (label.contains("箭") || label.contains("弓")) {
            return ProjectileVisual.ARROW;
        }
        if (label.contains("符") || label.contains("妖")) {
            return ProjectileVisual.TALISMAN;
        }
        if (label.contains("塔") || label.contains("火")) {
            return ProjectileVisual.TOWER_BLAST;
        }
        if (label.contains("榜") || label.contains("印")) {
            return ProjectileVisual.GOLD_SEAL;
        }
        if (label.contains("星")) {
            return ProjectileVisual.STAR;
        }
        if (damageType == DamageType.MAGIC) {
            return ProjectileVisual.MAGIC_ORB;
        }
        return ProjectileVisual.BOLT;
    }
}
