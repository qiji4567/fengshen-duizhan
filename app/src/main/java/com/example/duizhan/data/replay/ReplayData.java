package com.example.duizhan.data.replay;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public final class ReplayData {
    public static final int FORMAT_VERSION = 2;
    public int version = FORMAT_VERSION;
    public int intervalMs = 150;
    public final List<ReplayFrame> frames = new ArrayList<>();

    public JSONObject toJson() {
        JSONObject root = new JSONObject();
        try {
            root.put("version", version);
            root.put("intervalMs", intervalMs);
            JSONArray array = new JSONArray();
            for (ReplayFrame frame : frames) {
                array.put(frame.toJson());
            }
            root.put("frames", array);
        } catch (JSONException ignored) {
        }
        return root;
    }

    public static ReplayData fromJson(String json) {
        ReplayData data = new ReplayData();
        if (json == null || json.length() == 0) {
            return data;
        }
        try {
            JSONObject root = new JSONObject(json);
            data.version = root.optInt("version", 1);
            data.intervalMs = root.optInt("intervalMs", 150);
            JSONArray array = root.optJSONArray("frames");
            if (array == null) {
                return data;
            }
            for (int i = 0; i < array.length(); i++) {
                ReplayFrame frame = ReplayFrame.fromJson(array.optJSONObject(i));
                if (frame != null) {
                    data.frames.add(frame);
                }
            }
        } catch (JSONException ignored) {
        }
        return data;
    }

    public String toJsonString() {
        return toJson().toString();
    }
}
