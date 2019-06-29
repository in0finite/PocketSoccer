package com.example.pocketsoccer;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.RectF;
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

    public static Vec2 ballPos = new Vec2(0, 0);
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

        // reset ball position
        ballPos = new Vec2(150, 150);

        // set random velocity for the ball
        ballVelocity = new Vec2((float) Math.random() * 500, (float) Math.random() * 500);

    }

    void updateGame() {

        //Log.i(MainActivity.LOG_TAG, "update");

        ballPos.add(Vec2.multiply(ballVelocity, deltaTime));

        float fieldWidth = mCustomView.getWidth();
        float fieldHeight = mCustomView.getHeight();

        // constrain position

        if (ballPos.x < 0) {
            ballPos.x = 0;
            ballVelocity.x = Math.abs(ballVelocity.x);
        }
        else if (ballPos.x > fieldWidth) {
            ballPos.x = fieldWidth;
            ballVelocity.x = - Math.abs(ballVelocity.x);
        }

        if (ballPos.y < 0) {
            ballPos.y = 0;
            ballVelocity.y = Math.abs(ballVelocity.y);
        }
        else if(ballPos.y > fieldHeight) {
            ballPos.y = fieldHeight;
            ballVelocity.y = - Math.abs(ballVelocity.y);
        }

        // check for collision between goals and ball




        mCustomView.invalidate();

    }

    RectF getLeftGoalRect() {
        return new RectF(0, getFieldHeight() / 3f, getGoalWidth(), getFieldHeight() * 2f / 3f);
    }

    float getGoalWidth() {
        return 80f;
    }

    float getGoalPostHeight() {
        return 15f;
    }

    float getFieldWidth() {
        return mCustomView.getWidth();
    }

    float getFieldHeight() {
        return mCustomView.getHeight();
    }

    boolean isPointInsideRect(Vec2 point, RectF rect) {

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
