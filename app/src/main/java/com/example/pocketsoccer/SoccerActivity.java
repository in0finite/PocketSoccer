package com.example.pocketsoccer;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class SoccerActivity extends AppCompatActivity {

    public static int ballImageId;
    public static int fieldImageId;
    public static Vec2 ballPos = new Vec2(70, 70);
    public static Vec2 ballSize = new Vec2(40, 40);

    MyTask mTask;



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);


        // assign image ids
        ballImageId = R.drawable.football_ball_2426_2380_resized;
        fieldImageId = R.drawable.field;

        setContentView(R.layout.activity_soccer);

        // create custom view
        //View inflatedView = LayoutInflater.from(this).inflate(R.layout.sample_soccer_field_view, null);
        //final View customView = ((ViewGroup) inflatedView).getChildAt(0);
        final View customView = findViewById(R.id.soccerFieldView);

        Log.i(MainActivity.LOG_TAG, "created custom view, class: " + customView.getClass().getName());

        //customView.requestLayout();

        // create async task which will update custom view
        mTask = new MyTask(new Runnable() {
            @Override
            public void run() {
                //Log.i(MainActivity.LOG_TAG, "update");
                customView.invalidate();
            }
        });
        mTask.execute();

    }

    @Override
    protected void onStop() {

        if (mTask != null) {
            mTask.cancel(false);
            mTask = null;
        }

        super.onStop();
    }

}
