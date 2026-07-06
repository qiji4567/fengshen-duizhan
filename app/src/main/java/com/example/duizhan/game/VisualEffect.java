package com.example.duizhan.game;

public class VisualEffect {
    public final EffectKind kind;
    public float x;
    public float y;
    public float x2;
    public float y2;
    public float radius;
    public float ttl;
    public float maxTtl;
    public float textScale = 1f;
    public float intensity = 1f;
    public float angleRad;
    public int color;
    public String text;
    public DamageType damageType;

    public VisualEffect(EffectKind kind, float x, float y, float radius, float ttl, int color, String text) {
        this.kind = kind;
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.ttl = ttl;
        this.maxTtl = ttl;
        this.color = color;
        this.text = text;
    }

    public static VisualEffect line(float x, float y, float x2, float y2, float ttl, int color) {
        VisualEffect effect = new VisualEffect(EffectKind.LINE, x, y, 0f, ttl, color, "");
        effect.x2 = x2;
        effect.y2 = y2;
        return effect;
    }
}
