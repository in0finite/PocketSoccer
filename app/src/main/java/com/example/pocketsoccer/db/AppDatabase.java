package com.example.pocketsoccer.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Game.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase sDatabase;

    public static AppDatabase getInstance(Context context) {
        if (null == sDatabase) {
            sDatabase = Room.databaseBuilder(context, AppDatabase.class, "games").allowMainThreadQueries().build();
        }
        return sDatabase;
    }

    public abstract GameDao gameDao();

}
