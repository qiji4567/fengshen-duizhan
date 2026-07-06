package com.example.duizhan.view.render3d;

public final class Vector3 {
    public float x;
    public float y;
    public float z;

    public Vector3() {
    }

    public Vector3(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector3 set(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    public Vector3 copy() {
        return new Vector3(x, y, z);
    }

    public Vector3 add(Vector3 other) {
        x += other.x;
        y += other.y;
        z += other.z;
        return this;
    }

    public Vector3 sub(Vector3 other) {
        x -= other.x;
        y -= other.y;
        z -= other.z;
        return this;
    }

    public Vector3 scale(float s) {
        x *= s;
        y *= s;
        z *= s;
        return this;
    }

    public Vector3 rotateY(float rad) {
        float cos = (float) Math.cos(rad);
        float sin = (float) Math.sin(rad);
        float nx = x * cos + z * sin;
        float nz = -x * sin + z * cos;
        x = nx;
        z = nz;
        return this;
    }

    public Vector3 rotateX(float rad) {
        float cos = (float) Math.cos(rad);
        float sin = (float) Math.sin(rad);
        float ny = y * cos - z * sin;
        float nz = y * sin + z * cos;
        y = ny;
        z = nz;
        return this;
    }

    public static Vector3 lerp(Vector3 a, Vector3 b, float t, Vector3 out) {
        out.x = a.x + (b.x - a.x) * t;
        out.y = a.y + (b.y - a.y) * t;
        out.z = a.z + (b.z - a.z) * t;
        return out;
    }
}
