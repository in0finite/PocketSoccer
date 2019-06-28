package com.example.pocketsoccer;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
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
        setContentView(R.layout.activity_soccer);


        // assign image ids
        ballImageId = R.drawable.football_ball_2426_2380_resized;
        fieldImageId = R.drawable.field;

        // create custom view
        final View customView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.sample_soccer_field_view, null);

        // create async task which will update custom view
        mTask = new MyTask(new Runnable() {
            @Override
            public void run() {
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
