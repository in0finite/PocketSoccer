package com.example.pocketsoccer;

public class Movable {
    public Vec2 pos = new Vec2();
    public Vec2 size = new Vec2();
    public Vec2 velocity = new Vec2();
    public boolean hadCollisionWithStaticObject = false;

    public Movable(Vec2 pos, Vec2 size, Vec2 velocity) {
        this.pos = pos;
        this.size = size;
        this.velocity = velocity;
    }
}
