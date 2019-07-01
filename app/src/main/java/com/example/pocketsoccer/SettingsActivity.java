package com.example.pocketsoccer;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
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
        mTerrainSpinner.setSelection(0);

        String[] gameEndTypes = new String[]{"30 s", "1 min", "2 min", "5 min", "1 goal", "2 goals", "4 goals", "10 goals"};
        adapter = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, gameEndTypes);
        mGameEndConditionSpinner.setAdapter(adapter);
        mGameEndConditionSpinner.setSelection(3);

        mGameSpeedSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (seekBar.getProgress() < 0)
                    seekBar.setProgress(0);
                mGameSpeedTextView.setText("Game speed: " + seekBar.getProgress());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        // load preferences and display them
        this.loadPreferences();

    }

    void loadPreferences() {

    }

}
