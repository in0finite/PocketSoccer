package com.example.pocketsoccer;

public class Vec2 {
    public float x, y;

    public Vec2(float x, float y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        return "Vec2{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }

    public Vec2 clone() {
        return new Vec2(this.x, this.y);
    }

    Vec2 add(Vec2 other) {
        this.x += other.x;
        this.y += other.y;
        return this;
    }

    Vec2 multiply(float f) {
        this.x *= f;
        this.y *= f;
        return this;
    }

    public static Vec2 multiply(Vec2 vec2, float f) {
        return new Vec2(vec2.x * f, vec2.y * f);
    }

}
