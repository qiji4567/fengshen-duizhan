package com.example.duizhan.view.render3d;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;

import java.util.ArrayList;
import java.util.List;

final class MeshRasterizer {
    private static final float LIGHT_X = -0.42f;
    private static final float LIGHT_Y = 0.88f;
    private static final float LIGHT_Z = -0.22f;
    private static final float TILT = 0.52f;

    private final Path path = new Path();
    private final Vector3 p0 = new Vector3();
    private final Vector3 p1 = new Vector3();
    private final Vector3 p2 = new Vector3();
    private final List<DrawEntry> drawList = new ArrayList<>();

    private static final class DrawEntry {
        final MeshTriangle tri;
        final float depth;

        DrawEntry(MeshTriangle tri, float depth) {
            this.tri = tri;
            this.depth = depth;
        }
    }

    void draw(Canvas canvas, Paint paint, List<MeshTriangle> mesh, float worldX, float worldY,
              float facingRad, float pixelScale, float alpha, float extraPitchRad, boolean mirror) {
        if (mesh == null || mesh.isEmpty() || pixelScale <= 0.001f) {
            return;
        }
        float cosY = (float) Math.cos(facingRad);
        float sinY = (float) Math.sin(facingRad);
        float cosP = (float) Math.cos(extraPitchRad);
        float sinP = (float) Math.sin(extraPitchRad);

        drawList.clear();
        int limit = Math.min(mesh.size(), 640);
        for (int i = 0; i < limit; i++) {
            MeshTriangle tri = mesh.get(i);
            float depth = 0f;
            depth += projectVertex(tri.v0, p0, cosY, sinY, cosP, sinP, mirror, worldX, worldY, pixelScale);
            depth += projectVertex(tri.v1, p1, cosY, sinY, cosP, sinP, mirror, worldX, worldY, pixelScale);
            depth += projectVertex(tri.v2, p2, cosY, sinY, cosP, sinP, mirror, worldX, worldY, pixelScale);
            drawList.add(new DrawEntry(tri, depth * 0.333f));
        }
        drawList.sort((a, b) -> Float.compare(a.depth, b.depth));

        paint.setStyle(Paint.Style.FILL);
        for (int i = 0; i < drawList.size(); i++) {
            MeshTriangle tri = drawList.get(i).tri;
            projectVertex(tri.v0, p0, cosY, sinY, cosP, sinP, mirror, worldX, worldY, pixelScale);
            projectVertex(tri.v1, p1, cosY, sinY, cosP, sinP, mirror, worldX, worldY, pixelScale);
            projectVertex(tri.v2, p2, cosY, sinY, cosP, sinP, mirror, worldX, worldY, pixelScale);
            if (!finite(p0) || !finite(p1) || !finite(p2)) {
                continue;
            }

            float srcNx = mirror ? -tri.nx : tri.nx;
            float srcNz = tri.nz;
            float nx = srcNx * cosY + srcNz * sinY;
            float ny = tri.ny;
            float nz = -srcNx * sinY + srcNz * cosY;
            if (extraPitchRad != 0f) {
                float ny2 = ny * cosP - nz * sinP;
                float nz2 = ny * sinP + nz * cosP;
                ny = ny2;
                nz = nz2;
            }
            float light = 0.32f + 0.68f * Math.max(0f, nx * LIGHT_X + ny * LIGHT_Y + nz * LIGHT_Z);
            int color = MeshBuilder.shade(tri.baseColor, light);
            if (alpha < 0.99f) {
                color = Color.argb(Math.round(255f * alpha), Color.red(color), Color.green(color), Color.blue(color));
            }

            path.reset();
            path.moveTo(p0.x, p0.y);
            path.lineTo(p1.x, p1.y);
            path.lineTo(p2.x, p2.y);
            path.close();
            paint.setColor(color);
            canvas.drawPath(path, paint);
        }
    }

    private static boolean finite(Vector3 v) {
        return Float.isFinite(v.x) && Float.isFinite(v.y) && Float.isFinite(v.z);
    }

    private float projectVertex(Vector3 local, Vector3 out, float cosY, float sinY,
                                float cosP, float sinP, boolean mirror,
                                float worldX, float worldY, float pixelScale) {
        float lx = mirror ? -local.x : local.x;
        float x = lx * cosY + local.z * sinY;
        float z = -lx * sinY + local.z * cosY;
        float y = local.y;
        if (cosP != 1f || sinP != 0f) {
            float y2 = y * cosP - z * sinP;
            float z2 = y * sinP + z * cosP;
            y = y2;
            z = z2;
        }
        out.x = worldX + x * pixelScale;
        out.y = worldY + (-y + z * TILT) * pixelScale;
        return z;
    }
}
