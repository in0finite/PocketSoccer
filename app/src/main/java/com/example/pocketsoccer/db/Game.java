package com.example.pocketsoccer.db;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity
public class Game {

    @PrimaryKey(autoGenerate = true)
    public int uid;

    @ColumnInfo(name = "player1Name")
    public String player1Name;

    @ColumnInfo(name = "player2Name")
    public String player2Name;

    @ColumnInfo(name = "player1Score")
    public int player1Score;

    @ColumnInfo(name = "player2Score")
    public int player2Score;

    @ColumnInfo(name = "timeElapsed")
    public float timeElapsed;

    @ColumnInfo(name = "timeWhenFinished")
    public long timeWhenFinished;

}
