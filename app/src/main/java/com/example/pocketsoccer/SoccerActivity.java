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

    public static Vec2 ballPos = new Vec2(150, 150);
    public static Vec2 ballSize = new Vec2(40, 40);
    public static Vec2 ballVelocity = new Vec2(0, 0);

    public static float deltaTime = 0.02f;

    MyTask mTask;
    View mCustomView;



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
        mCustomView = findViewById(R.id.soccerFieldView);

        Log.i(MainActivity.LOG_TAG, "created custom view, class: " + mCustomView.getClass().getName());

        //customView.requestLayout();

        // create async task which will update custom view
        mTask = new MyTask(new Runnable() {
            @Override
            public void run() {
                updateGame();
            }
        });
        mTask.execute();

        // set random velocity for the ball
        ballVelocity = new Vec2((float) Math.random() * 500, (float) Math.random() * 500);

    }

    void updateGame() {

        //Log.i(MainActivity.LOG_TAG, "update");

        ballPos.add(Vec2.multiply(ballVelocity, deltaTime));

        // constrain position

        if (ballPos.x < 0) {
            ballPos.x = 0;
            ballVelocity.x = Math.abs(ballVelocity.x);
        }
        else if (ballPos.x > mCustomView.getWidth()) {
            ballPos.x = mCustomView.getWidth();
            ballVelocity.x = - Math.abs(ballVelocity.x);
        }

        if (ballPos.y < 0) {
            ballPos.y = 0;
            ballVelocity.y = Math.abs(ballVelocity.y);
        }
        else if(ballPos.y > mCustomView.getHeight()) {
            ballPos.y = mCustomView.getHeight();
            ballVelocity.y = - Math.abs(ballVelocity.y);
        }



        mCustomView.invalidate();

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
