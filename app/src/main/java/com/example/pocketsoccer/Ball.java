package com.example.pocketsoccer;

public class Ball extends Movable {

    public Ball(Vec2 pos, Vec2 size, Vec2 velocity) {
        super(pos, size, velocity);
    }

    public Ball(Vec2 pos, Vec2 size, Vec2 velocity, float mass) {
        super(pos, size, velocity, mass);
    }
}
