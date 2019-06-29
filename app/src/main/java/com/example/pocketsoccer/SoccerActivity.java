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

    public static SoccerActivity instance = null;

    public static int ballImageId;
    public static int fieldImageId;

    public Vec2 ballPos = new Vec2(0, 0);
    public Vec2 ballSize = new Vec2(40, 40);
    public Vec2 ballVelocity = new Vec2(0, 0);

    static final int kNumPhysicsSteps = 10;

    public float deltaTime = 0.05f / kNumPhysicsSteps;

    boolean mWasBallInsideGoalLastTime = false;
    float mPosDeltaAfterCollision = 0f;

    MyTask mTask;
    View mCustomView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        instance = this;

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

        // perform additional steps for more precise collision

        for (int i=0; i < kNumPhysicsSteps; i++) {
            updateGameSingleStep();
        }

    }

    void updateGameSingleStep() {

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

        // check for collision between goal posts and ball

        RectF[] goalRects = new RectF[]{getLeftGoalRect(), getRightGoalRect()};

        goals_for: for (RectF goalRect : goalRects) {
            RectF[] goalPostRects = new RectF[]{getUpperGoalPost(goalRect), getLowerGoalPost(goalRect)};
            for (RectF goalPostRect : goalPostRects) {
                RectF ballRect = rectFromPosAndSize(ballPos, ballSize);
                RectF ballRectClone = new RectF(ballRect);
                if (resolveCollisionBetweenCircleAndGoalPost(ballRect, goalPostRect, ballVelocity)) {
                    // collision happened

                    // apply position
                    //ballPos = new Vec2(ballRect.centerX(), ballRect.centerY());
                    ballPos.y += mPosDeltaAfterCollision;
                    System.out.printf("ball position after collision: %s, original ball rect %s, new ball rect %s\n",
                            ballPos.toString(), ballRectClone.toString(), ballRect.toString());

                    break goals_for;
                }
            }
        }

        // check if ball is inside of any goal

        boolean isInsideAnyGoal = false;
        RectF insideGoalRect = null;

        for (RectF goalRect : goalRects) {
            if (isPointInsideRect(ballPos, goalRect)) {
                // IT'S A GOAL !

                isInsideAnyGoal = true;
                insideGoalRect = goalRect;

                break;
            }
        }

        if (isInsideAnyGoal && !mWasBallInsideGoalLastTime) {
            boolean isLeftGoal = (insideGoalRect == goalRects[0]);
            System.out.printf("GOAL ! is left: %b\n", isLeftGoal);
        }

        mWasBallInsideGoalLastTime = isInsideAnyGoal;



        // update graphics
        mCustomView.invalidate();

    }

    boolean resolveCollisionBetweenCircleAndGoalPost(RectF circleRect, RectF goalPostRect, Vec2 velocity) {

        if (circleRect.intersect(goalPostRect)) {
            boolean isFromUpperSide = false;

            Vec2 originalVelocity = velocity.clone();
            float delta = 0f;

            if (velocity.y > 0) {
                // collision from upper side
                delta = goalPostRect.top - circleRect.bottom;
                //circleRect.offset(0f, delta);
                velocity.y = - Math.abs(velocity.y);
                isFromUpperSide = true;
            }
            else {
                // collision from down side
                delta = goalPostRect.bottom - circleRect.top;
                //circleRect.offset(0f, delta);
                velocity.y = Math.abs(velocity.y);
                isFromUpperSide = false;
            }

            delta += Math.signum(delta) * 40f;

//            circleRect.top += delta;
//            circleRect.bottom += delta;
            //circleRect = new RectF(circleRect.left, circleRect.top + delta, circleRect.right, circleRect.bottom + delta);
            mPosDeltaAfterCollision = delta;

            System.out.printf("collision between circle and goal post - circle rect: %s, goal post rect: %s, original velocity %s, new velocity: %s, delta %f, is from upper side: %b\n",
                    circleRect.toString(), goalPostRect.toString(), originalVelocity.toString(), velocity.toString(), delta, isFromUpperSide);

            return true;
        }

        return false;
    }

    RectF getLeftGoalRect() {
        return new RectF(0, getFieldHeight() / 3f, getGoalWidth(), getFieldHeight() * 2f / 3f);
    }

    RectF getRightGoalRect() {
        return new RectF(getFieldWidth() - getGoalWidth(), getFieldHeight() / 3f, getFieldWidth(), getFieldHeight() * 2f / 3f);
    }

    RectF getUpperGoalPost(RectF goalRect) {
        RectF rect = new RectF(goalRect);
        rect.bottom = rect.top;
        rect.top -= getGoalPostHeight();
        return rect;
    }

    RectF getLowerGoalPost(RectF goalRect) {
        RectF rect = new RectF(goalRect);
        rect.top = rect.bottom;
        rect.bottom += getGoalPostHeight();
        return rect;
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

    static boolean isPointInsideRect(Vec2 point, RectF rect) {
        return rect.contains(point.x, point.y);
    }

    static RectF rectFromPosAndSize(Vec2 pos, Vec2 size) {
        return new RectF((pos.x - size.x / 2f),
                (pos.y - size.y / 2f),
                (pos.x + size.x / 2f),
                (pos.y + size.y / 2f));
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
