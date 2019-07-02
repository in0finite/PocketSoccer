package com.example.pocketsoccer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.pocketsoccer.db.AppDatabase;
import com.example.pocketsoccer.db.Game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class StatsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);


        // setup buttons

        this.findViewById(R.id.buttonExitStats).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onExitButtonPressed();
            }
        });

        this.findViewById(R.id.buttonDeleteAllStats).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteAllStats();
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        this.populateGameList();
    }

    void populateGameList() {

        // get all games
        List<Game> games = new ArrayList<>();
        try {
            games = AppDatabase.getInstance(this).gameDao().getAll();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // find all player pairs, and their relative number of wins

        HashMap<Pair<String, String>, Pair<Integer, Integer>> hashMap = new HashMap<>();

        for (Game game : games) {

            int increment1 = 0, increment2 = 0;
            if (game.player1Score > game.player2Score)
                increment1 = 1;
            else if (game.player1Score < game.player2Score)
                increment2 = 1;

            Pair<String, String> pair = new Pair<>(game.player1Name, game.player2Name);
            if (hashMapContainsKey(hashMap, pair)) {
                Pair<Integer, Integer> resultPair = hashMap.get(pair);
                resultPair.valueA += increment1;
                resultPair.valueB += increment2;
            } else {
                hashMap.put(pair, new Pair<>(increment1, increment2));
            }

        }

        // populate UI

        ViewGroup container = this.findViewById(R.id.gamesContainer);
        container.removeAllViews();

        Set<Pair<String, String>> keySet = hashMap.keySet();
        for (final Pair<String, String> pair : keySet) {
            Pair<Integer, Integer> resultPair = hashMap.get(pair);
            TextView textView = new TextView(this);
            textView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            textView.setText(pair.valueA + "     " + resultPair.valueA + " : " + resultPair.valueB + "     " + pair.valueB);
            textView.setGravity(Gravity.CENTER_HORIZONTAL);
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onGameClicked(pair);
                }
            });
            container.addView(textView);
        }


    }

    static boolean hashMapContainsKey(HashMap<Pair<String, String>, Pair<Integer, Integer>> hashMap, Pair<String, String> key) {
        return hashMap.keySet().contains(key);
    }

    void onGameClicked(Pair<String, String> playerPair) {

        // start stats activity for single player pair

        Intent intent = new Intent(this, StatsForSingleGameActivity.class);
        intent.putExtra("player1Name", playerPair.valueA);
        intent.putExtra("player2Name", playerPair.valueB);
        this.startActivity(intent);

    }

    void onExitButtonPressed() {

        this.finish();

    }

    void deleteAllStats() {

        try {
            AppDatabase.getInstance(this).gameDao().deleteAll();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        this.populateGameList();

    }

}
