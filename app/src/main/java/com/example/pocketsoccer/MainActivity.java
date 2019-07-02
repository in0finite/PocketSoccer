package com.example.pocketsoccer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.pocketsoccer.db.AppDatabase;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;

public class MainActivity extends AppCompatActivity {

    public static final String LOG_TAG = "PocketSoccer";
    public static final String savedGameFileName = "SavedGame.bin";

    public static MainActivity instance = null;

    Button mContinueGameButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        instance = this;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Button newGameButton = this.findViewById(R.id.newGameButton);
        newGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startNewGame();
            }
        });

        mContinueGameButton = this.findViewById(R.id.continueGameButton);
        mContinueGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                continueGame();
            }
        });

        this.findViewById(R.id.statsButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openStats();
            }
        });

        this.findViewById(R.id.settingsButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openSettings();
            }
        });


        // init database at start, so it doesn't block later
        try {
            AppDatabase.getInstance(this).gameDao();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // load settings on start
        SettingsActivity.loadPreferencesWithoutUIUpdate(this);


    }

    @Override
    protected void onStart() {

        super.onStart();

        // disable button if saved game does not exist
        if (! getSavedGameFile().exists()) {
            mContinueGameButton.setEnabled(false);
        } else {
            mContinueGameButton.setEnabled(true);
        }

    }

    void startNewGame() {

        Intent intent = new Intent(this, NewGameActivity.class);
        startActivity(intent);

    }

    void continueGame() {

        try {
            File file = getSavedGameFile();
            if (file.exists()) {
                byte[] data = Util.readFile(file);

                Intent intent = new Intent(this, SoccerActivity.class);
                intent.putExtra("continueGameData", data);
                startActivity(intent);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public File getSavedGameFile() {
        return new File(getFilesDir().getPath() + "/" + savedGameFileName);
    }

    void openSettings() {

        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);

    }

    void openStats() {

        Intent intent = new Intent(this, StatsActivity.class);
        this.startActivity(intent);

    }

}
