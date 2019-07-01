package com.example.pocketsoccer;

public class Util {

    public static int randomInt(int min, int max) {
        return min + (int) Math.round( Math.random() * (max - min) );
    }

}
