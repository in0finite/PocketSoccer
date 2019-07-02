package com.example.pocketsoccer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.pocketsoccer.db.AppDatabase;
import com.example.pocketsoccer.db.Game;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class StatsForSingleGameActivity extends AppCompatActivity {

    String mPlayer1Name = null, mPlayer2Name = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats_for_single_game);

        Intent intent = this.getIntent();

        mPlayer1Name = intent.getStringExtra("player1Name");
        mPlayer2Name = intent.getStringExtra("player2Name");

        int player1Score = intent.getIntExtra("player1Score", 0);
        int player2Score = intent.getIntExtra("player2Score", 0);

        float timeElapsed = intent.getFloatExtra("timeElapsed", 0f);
        long timeWhenFinished = intent.getLongExtra("timeWhenFinished", 0);

        List<Game> games = new ArrayList<>();
        try {
            games = AppDatabase.getInstance(this).gameDao().find(mPlayer1Name, mPlayer2Name);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // populate list of games

        ViewGroup container = this.findViewById(R.id.gameStatsContainer);

        for (Game game : games) {
            TextView textView = new TextView(this);
            textView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            textView.setText(game.player1Score + " : " + game.player2Score + "     " + new Date(game.timeWhenFinished));
            textView.setGravity(Gravity.CENTER_HORIZONTAL);
            textView.setTextColor(Color.YELLOW);
            container.addView(textView);
        }

        // set player names

        TextView textViewName1 = this.findViewById(R.id.textViewStatsPlayer1Name);
        textViewName1.setText(mPlayer1Name);

        TextView textViewName2 = this.findViewById(R.id.textViewStatsPlayer2Name);
        textViewName2.setText(mPlayer2Name);

        // set num wins

        int numWins1 = 0, numWins2 = 0;
        for (Game game : games) {
            if (game.player1Score > game.player2Score)
                numWins1 ++;
            else if(game.player1Score < game.player2Score)
                numWins2 ++;
        }

        ((TextView)this.findViewById(R.id.textViewNumWins1)).setText("" + numWins1);
        ((TextView)this.findViewById(R.id.textViewNumWins2)).setText("" + numWins2);

        // setup buttons

        this.findViewById(R.id.buttonRemoveGameStats).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeGameStats();
            }
        });

        this.findViewById(R.id.buttonExitSingleGameStats).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onOkButton();
            }
        });


    }

    void removeGameStats() {

        try {
            AppDatabase.getInstance(this).gameDao().delete(mPlayer1Name, mPlayer2Name);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        this.finish();

    }

    void onOkButton() {

        this.finish();

    }

}
