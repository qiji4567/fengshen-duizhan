package com.example.duizhan.data.replay;

import com.example.duizhan.game.DamageType;
import com.example.duizhan.game.EffectKind;
import com.example.duizhan.game.VisualEffect;

import org.json.JSONException;
import org.json.JSONObject;

final class ReplayEffectState {
    String kind;
    float x;
    float y;
    float x2;
    float y2;
    float radius;
    float ttl;
    float maxTtl;
    float textScale;
    float intensity;
    float angleRad;
    String damageType;
    int color;
    String text;

    static ReplayEffectState from(VisualEffect effect) {
        ReplayEffectState state = new ReplayEffectState();
        state.kind = effect.kind.name();
        state.x = effect.x;
        state.y = effect.y;
        state.x2 = effect.x2;
        state.y2 = effect.y2;
        state.radius = effect.radius;
        state.ttl = effect.ttl;
        state.maxTtl = effect.maxTtl;
        state.textScale = effect.textScale;
        state.intensity = effect.intensity;
        state.angleRad = effect.angleRad;
        state.color = effect.color;
        state.text = effect.text;
        if (effect.damageType != null) {
            state.damageType = effect.damageType.name();
        }
        return state;
    }

    VisualEffect toEffect() {
        EffectKind effectKind = EffectKind.RING;
        try {
            effectKind = EffectKind.valueOf(kind);
        } catch (IllegalArgumentException ignored) {
        }
        VisualEffect effect;
        if (effectKind == EffectKind.LINE) {
            effect = VisualEffect.line(x, y, x2, y2, ttl > 0f ? ttl : 0.18f, color);
        } else {
            effect = new VisualEffect(effectKind, x, y, radius, ttl > 0f ? ttl : 0.35f, color,
                    text == null ? "" : text);
            effect.x2 = x2;
            effect.y2 = y2;
        }
        effect.maxTtl = maxTtl > 0f ? maxTtl : effect.ttl;
        effect.textScale = textScale > 0f ? textScale : 1f;
        effect.intensity = intensity > 0f ? intensity : 1f;
        effect.angleRad = angleRad;
        if (damageType != null) {
            try {
                effect.damageType = DamageType.valueOf(damageType);
            } catch (IllegalArgumentException ignored) {
            }
        }
        return effect;
    }

    JSONObject toJson() throws JSONException {
        JSONObject object = new JSONObject();
        object.put("k", kind);
        object.put("x", x);
        object.put("y", y);
        object.put("x2", x2);
        object.put("y2", y2);
        object.put("r", radius);
        object.put("ttl", ttl);
        object.put("mt", maxTtl);
        object.put("ts", textScale);
        object.put("in", intensity);
        object.put("ar", angleRad);
        object.put("c", color);
        if (damageType != null) {
            object.put("dt", damageType);
        }
        if (text != null && text.length() > 0) {
            object.put("tx", text);
        }
        return object;
    }

    static ReplayEffectState fromJson(JSONObject object) {
        if (object == null) {
            return null;
        }
        ReplayEffectState state = new ReplayEffectState();
        state.kind = object.optString("k", EffectKind.RING.name());
        state.x = (float) object.optDouble("x");
        state.y = (float) object.optDouble("y");
        state.x2 = (float) object.optDouble("x2");
        state.y2 = (float) object.optDouble("y2");
        state.radius = (float) object.optDouble("r");
        state.ttl = (float) object.optDouble("ttl", 0.35f);
        state.maxTtl = (float) object.optDouble("mt", state.ttl);
        state.textScale = (float) object.optDouble("ts", 1f);
        state.intensity = (float) object.optDouble("in", 1f);
        state.angleRad = (float) object.optDouble("ar");
        state.color = object.optInt("c", 0xFFFFFFFF);
        state.damageType = object.optString("dt", null);
        if (state.damageType != null && state.damageType.length() == 0) {
            state.damageType = null;
        }
        state.text = object.optString("tx", null);
        if (state.text != null && state.text.length() == 0) {
            state.text = null;
        }
        return state;
    }
}
