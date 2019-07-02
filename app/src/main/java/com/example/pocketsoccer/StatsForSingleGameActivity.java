package com.example.pocketsoccer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.example.pocketsoccer.db.AppDatabase;
import com.example.pocketsoccer.db.Game;

import java.util.List;

public class StatsForSingleGameActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats_for_single_game);

        Intent intent = this.getIntent();

        String player1Name = intent.getStringExtra("player1Name");
        String player2Name = intent.getStringExtra("player2Name");

        int player1Score = intent.getIntExtra("player1Score", 0);
        int player2Score = intent.getIntExtra("player2Score", 0);

        float timeElapsed = intent.getFloatExtra("timeElapsed", 0f);
        long timeWhenFinished = intent.getLongExtra("timeWhenFinished", 0);

        List<Game> games = AppDatabase.getInstance(this).gameDao().find(player1Name, player2Name);

        // populate UI



    }
}
