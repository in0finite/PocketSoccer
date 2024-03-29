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

    static int mTerrainType = 0, mGameEndCondition = 3, mGameSpeed = 10;



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
        if (mGameSpeedTextView != null)
            mGameSpeedTextView.setText("Game speed: " + (mGameSpeedSeekBar.getProgress() / 10f));
    }

    void updateUI() {
        if (mTerrainSpinner != null)
            mTerrainSpinner.setSelection(mTerrainType);
        if (mGameEndConditionSpinner != null)
            mGameEndConditionSpinner.setSelection(mGameEndCondition);
        if (mGameSpeedSeekBar != null)
            mGameSpeedSeekBar.setProgress(mGameSpeed);
        updateSeekBarText();
    }

    static void loadPreferencesWithoutUIUpdate(Context context) {

        SharedPreferences sharedPref = context.getSharedPreferences("main", Context.MODE_PRIVATE);

        mTerrainType = sharedPref.getInt("terrainType", 0);
        mGameEndCondition = sharedPref.getInt("gameEndCondition", 3);
        mGameSpeed = sharedPref.getInt("gameSpeed", 10);

    }

    void loadPreferences() {

        loadPreferencesWithoutUIUpdate(this);

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

        this.finish();

    }

    void resetPreferences() {

        mTerrainSpinner.setSelection(0);
        mGameEndConditionSpinner.setSelection(3);
        mGameSpeedSeekBar.setProgress(10);
        updateSeekBarText();

    }


    public static int getTerrainDrawableId() {
        if (0 == mTerrainType)
            return R.drawable.field;
        else if (1 == mTerrainType)
            return R.drawable.concrete;
        else if (2 == mTerrainType)
            return R.drawable.parquet;
        else
            return 0;
    }

    public static boolean isGameLimitedWithTime() {
        return mGameEndCondition < 4;
    }

    public static boolean isGameLimitedWithNumGoals() {
        return ! isGameLimitedWithTime();
    }

    public static float getGameTimeLimit() {
        float[] times = new float[]{30, 60, 120, 300};
        return times[mGameEndCondition];
    }

    public static int getGoalLimit() {
        int[] nums = new int[]{1, 2, 4, 10};
        return nums[mGameEndCondition - 4];
    }

    public static float getGameSpeed() {
        return mGameSpeed / 10f;
    }

}
