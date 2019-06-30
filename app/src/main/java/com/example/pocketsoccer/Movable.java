package com.example.pocketsoccer;

import android.graphics.drawable.Drawable;

public class Movable {
    public Vec2 pos = new Vec2();
    public Vec2 size = new Vec2();
    public Vec2 velocity = new Vec2();
    public float mass = 0f;
    //public int drawableId = 0;
    public Drawable drawable = null;
    public boolean hadCollisionWithStaticObject = false;

    public Movable(Vec2 pos, Vec2 size, Vec2 velocity) {
        this.pos = pos;
        this.size = size;
        this.velocity = velocity;
    }

    public Movable(Vec2 pos, Vec2 size, Vec2 velocity, float mass) {
        this.pos = pos;
        this.size = size;
        this.velocity = velocity;
        this.mass = mass;
    }

    public float getRadius() {
        return this.size.x * 0.5f;
    }

}
