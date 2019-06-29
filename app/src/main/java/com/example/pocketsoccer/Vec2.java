package com.example.pocketsoccer;


public class Vec2 {
    public float x, y;


    public Vec2() {

    }

    public Vec2(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public Vec2(double x, double y) {
        this.x = (float) x;
        this.y = (float) y;
    }


    public static Vec2 zero() { return new Vec2(0, 0); }


    @Override
    public String toString() {
        return "Vec2{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vec2 vec2 = (Vec2) o;
        return Float.compare(vec2.x, x) == 0 &&
                Float.compare(vec2.y, y) == 0;
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

    public float length() {
        return (float) Math.sqrt( this.x * this.x + this.y * this.y );
    }

    public Vec2 normalized() {
        float length = this.length();
        if (0 == length)
            return new Vec2(0, 0);
        return new Vec2(this.x / length, this.y / length);
    }

    public static Vec2 randomNormalized() {
        double x = (Math.random() - 0.5);
        double y = (Math.random() - 0.5);
        return new Vec2(x, y).normalized();
    }

    public static Vec2 randomWithMaxLength(float maxLength) {
        Vec2 v = randomNormalized();
        v.multiply((float) (Math.random() * maxLength));
        return v;
    }

}
