package com.example.pocketsoccer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;

public class SettingsActivity extends AppCompatActivity {

    Spinner mTerrainSpinner, mGameEndConditionSpinner;
    SeekBar mGameSpeedSeekBar;
    TextView mGameSpeedTextView;

    static int mTerrainType = -1, mGameEndCondition = -1, mGameSpeed = 10;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mTerrainSpinner = this.findViewById(R.id.spinnerTerrain);
        mGameEndConditionSpinner = this.findViewById(R.id.spinnerGameEndCondition);
        mGameSpeedSeekBar = this.findViewById(R.id.seekBarGameSpeed);
        mGameSpeedTextView = this.findViewById(R.id.textViewGameSpeed);

        ArrayList<String> terrainTypes = new ArrayList<>(Arrays.asList("Grass", "Concrete", "Parquet"));
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, terrainTypes);
        mTerrainSpinner.setAdapter(adapter);

        String[] gameEndTypes = new String[]{"30 s", "1 min", "2 min", "5 min", "1 goal", "2 goals", "4 goals", "10 goals"};
        adapter = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, gameEndTypes);
        mGameEndConditionSpinner.setAdapter(adapter);

        mGameSpeedSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (seekBar.getProgress() < 0)
                    seekBar.setProgress(0);
                updateSeekBarText();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        this.findViewById(R.id.buttonSaveSettings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                savePreferences();
            }
        });

        this.findViewById(R.id.buttonResetSettings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetPreferences();
            }
        });


        //this.resetPreferences();

    }

    @Override
    protected void onStart() {
        super.onStart();
        // load preferences and display them
        this.loadPreferences();
    }

    void updateSeekBarText() {
        mGameSpeedTextView.setText("Game speed: " + (mGameSpeedSeekBar.getProgress() / 10f));
    }

    void updateUI() {
        mTerrainSpinner.setSelection(mTerrainType);
        mGameEndConditionSpinner.setSelection(mGameEndCondition);
        mGameSpeedSeekBar.setProgress(mGameSpeed);
        updateSeekBarText();
    }

    void loadPreferences() {

        SharedPreferences sharedPref = this.getSharedPreferences("main", Context.MODE_PRIVATE);

        mTerrainType = sharedPref.getInt("terrainType", 0);
        mGameEndCondition = sharedPref.getInt("gameEndCondition", 3);
        mGameSpeed = sharedPref.getInt("gameSpeed", 10);

        updateUI();

    }

    void savePreferences() {

        SharedPreferences sharedPref = this.getSharedPreferences("main", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        mTerrainType = mTerrainSpinner.getSelectedItemPosition();
        mGameEndCondition = mGameEndConditionSpinner.getSelectedItemPosition();
        mGameSpeed = mGameSpeedSeekBar.getProgress();

        editor.putInt("terrainType", mTerrainType);
        editor.putInt("gameEndCondition", mGameEndCondition);
        editor.putInt("gameSpeed", mGameSpeed);

        editor.commit();

    }

    void resetPreferences() {

        mTerrainSpinner.setSelection(0);
        mGameEndConditionSpinner.setSelection(3);
        mGameSpeedSeekBar.setProgress(10);
        updateSeekBarText();

    }


    public static Drawable getTerrainDrawable() {

    }

    public static boolean isGameLimitedWithTime() {

    }

    public static boolean isGameLimitedWithNumGoals() {
        return ! isGameLimitedWithTime();
    }

    public static float getGameTimeLimit() {

    }

    public static int getGoalLimit() {

    }

    public static float getGameSpeed() {

    }

}
