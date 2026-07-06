package com.example.duizhan.view.render3d;

import android.graphics.Color;

import java.util.ArrayList;
import java.util.List;

final class MeshBuilder {
    private final List<MeshTriangle> triangles = new ArrayList<>();

    List<MeshTriangle> build() {
        return new ArrayList<>(triangles);
    }

    void clear() {
        triangles.clear();
    }

    void addBox(float cx, float cy, float cz, float w, float h, float d, int color) {
        float hw = w * 0.5f;
        float hh = h * 0.5f;
        float hd = d * 0.5f;
        Vector3[] c = {
                new Vector3(cx - hw, cy - hh, cz - hd),
                new Vector3(cx + hw, cy - hh, cz - hd),
                new Vector3(cx + hw, cy + hh, cz - hd),
                new Vector3(cx - hw, cy + hh, cz - hd),
                new Vector3(cx - hw, cy - hh, cz + hd),
                new Vector3(cx + hw, cy - hh, cz + hd),
                new Vector3(cx + hw, cy + hh, cz + hd),
                new Vector3(cx - hw, cy + hh, cz + hd),
        };
        addQuad(c[0], c[1], c[2], c[3], color);
        addQuad(c[5], c[4], c[7], c[6], color);
        addQuad(c[4], c[0], c[3], c[7], color);
        addQuad(c[1], c[5], c[6], c[2], color);
        addQuad(c[3], c[2], c[6], c[7], color);
        addQuad(c[4], c[5], c[1], c[0], color);
    }

    void addSphere(float cx, float cy, float cz, float radius, int color, int rings, int segments) {
        for (int r = 0; r < rings; r++) {
            float v0 = (float) r / rings;
            float v1 = (float) (r + 1) / rings;
            float phi0 = (float) (v0 * Math.PI);
            float phi1 = (float) (v1 * Math.PI);
            for (int s = 0; s < segments; s++) {
                float u0 = (float) s / segments;
                float u1 = (float) (s + 1) / segments;
                float t0 = (float) (u0 * Math.PI * 2);
                float t1 = (float) (u1 * Math.PI * 2);
                Vector3 a = spherePoint(cx, cy, cz, radius, phi0, t0);
                Vector3 b = spherePoint(cx, cy, cz, radius, phi1, t0);
                Vector3 c = spherePoint(cx, cy, cz, radius, phi1, t1);
                Vector3 d = spherePoint(cx, cy, cz, radius, phi0, t1);
                addQuad(a, b, c, d, color);
            }
        }
    }

    private Vector3 spherePoint(float cx, float cy, float cz, float r, float phi, float theta) {
        float sinPhi = (float) Math.sin(phi);
        return new Vector3(
                cx + r * sinPhi * (float) Math.cos(theta),
                cy + r * (float) Math.cos(phi),
                cz + r * sinPhi * (float) Math.sin(theta));
    }

    void addQuad(Vector3 a, Vector3 b, Vector3 c, Vector3 d, int color) {
        addTri(a, b, c, color);
        addTri(a, c, d, color);
    }

    void addTri(Vector3 a, Vector3 b, Vector3 c, int color) {
        MeshTriangle tri = new MeshTriangle();
        tri.v0.set(a.x, a.y, a.z);
        tri.v1.set(b.x, b.y, b.z);
        tri.v2.set(c.x, c.y, c.z);
        tri.baseColor = color;
        computeNormal(tri);
        triangles.add(tri);
    }

    private void computeNormal(MeshTriangle tri) {
        float ax = tri.v1.x - tri.v0.x;
        float ay = tri.v1.y - tri.v0.y;
        float az = tri.v1.z - tri.v0.z;
        float bx = tri.v2.x - tri.v0.x;
        float by = tri.v2.y - tri.v0.y;
        float bz = tri.v2.z - tri.v0.z;
        tri.nx = ay * bz - az * by;
        tri.ny = az * bx - ax * bz;
        tri.nz = ax * by - ay * bx;
        float len = (float) Math.sqrt(tri.nx * tri.nx + tri.ny * tri.ny + tri.nz * tri.nz);
        if (len > 0.0001f) {
            tri.nx /= len;
            tri.ny /= len;
            tri.nz /= len;
        }
    }

    static int shade(int color, float light) {
        float clamped = Math.max(0.22f, Math.min(1.15f, light));
        return Color.rgb(
                clamp(Color.red(color) * clamped),
                clamp(Color.green(color) * clamped),
                clamp(Color.blue(color) * clamped));
    }

    private static int clamp(float v) {
        return Math.max(0, Math.min(255, Math.round(v)));
    }
}
