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

import java.util.ArrayList;

public class SoccerActivity extends AppCompatActivity {

    public static SoccerActivity instance = null;

    public static int ballImageId;
    public static int fieldImageId;

    public Movable ballMovable = new Movable(new Vec2(150, 150), new Vec2(40, 40), Vec2.zero());
    //public Vec2 ballPos = new Vec2(0, 0);
    //public Vec2 ballSize = new Vec2(40, 40);
    //public Vec2 ballVelocity = new Vec2(0, 0);

    public ArrayList<Movable> movables = new ArrayList<>();

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


        // set random velocity for the ball
        Vec2 ballVelocity = Vec2.randomNormalized();
        ballVelocity.multiply(800f);
        this.ballMovable.velocity = ballVelocity;


        this.movables.add(this.ballMovable);

        createPlayers(R.drawable.br, R.drawable.ger);

    }

    void createPlayers(int flagId1, int flagId2) {

        Vec2 playerSize = new Vec2(80, 80);

        for (int i=0; i < 3; i++) {

            // left side
            float x = getFieldWidth() / 3f;
            float y = (i + 1) / 4f * getFieldHeight();

            Movable movable = new Movable(new Vec2(x, y), playerSize, Vec2.randomWithMaxLength(600));
            movable.drawableId = flagId1;
            this.movables.add(movable);

            // right side
            x = getFieldWidth() * 2f / 3f;

            movable = new Movable(new Vec2(x, y), playerSize, Vec2.randomWithMaxLength(600));
            movable.drawableId = flagId2;
            this.movables.add(movable);

        }

    }

    void updateGame() {

        RectF[] goalRects = new RectF[]{getLeftGoalRect(), getRightGoalRect()};

        // perform additional steps for more precise collision

        for (int i=0; i < kNumPhysicsSteps; i++) {
            updateGameSingleStep(goalRects);
        }

        // update graphics
        mCustomView.invalidate();

    }

    void updateGameSingleStep(RectF[] goalRects) {

        // for each movable: move it, constrain position, check for collision with goal posts

        updateMovable(ballMovable, goalRects);

        // check for collision between movables, but only for those who didn't have collision with static object


        // at the end, check if ball entered goal
        checkCollisionBetweenGoalsAndBall(goalRects);

    }

    void updateMovable(Movable movable, RectF[] goalRects) {

        movable.hadCollisionWithStaticObject = false;

        // update position based on velocity
        movable.pos.add(Vec2.multiply(movable.velocity, deltaTime));

        float fieldWidth = mCustomView.getWidth();
        float fieldHeight = mCustomView.getHeight();

        // constrain position

        Vec2 posBeforeConstraining = movable.pos.clone();

        if (movable.pos.x < 0) {
            movable.pos.x = 0;
            movable.velocity.x = Math.abs(movable.velocity.x);
        }
        else if (movable.pos.x > fieldWidth) {
            movable.pos.x = fieldWidth;
            movable.velocity.x = - Math.abs(movable.velocity.x);
        }

        if (movable.pos.y < 0) {
            movable.pos.y = 0;
            movable.velocity.y = Math.abs(movable.velocity.y);
        }
        else if(movable.pos.y > fieldHeight) {
            movable.pos.y = fieldHeight;
            movable.velocity.y = - Math.abs(movable.velocity.y);
        }

        if (! posBeforeConstraining.equals(movable.pos)) {
            // movable hit an edge of the field
            movable.hadCollisionWithStaticObject = true;
        }

        // check for collision between goal posts and movable

        goals_for: for (RectF goalRect : goalRects) {
            RectF[] goalPostRects = new RectF[]{getUpperGoalPost(goalRect), getLowerGoalPost(goalRect)};
            for (RectF goalPostRect : goalPostRects) {
                RectF movableRect = rectFromPosAndSize(movable.pos, movable.size);
                RectF movableRectClone = new RectF(movableRect);
                if (resolveCollisionBetweenCircleAndGoalPost(movableRect, goalPostRect, movable.velocity)) {
                    // collision happened

                    movable.hadCollisionWithStaticObject = true;

                    // apply new position
                    //movable.pos = new Vec2(movableRect.centerX(), movableRect.centerY());
                    movable.pos.y += mPosDeltaAfterCollision;
                    System.out.printf("ball position after collision: %s, original ball rect %s, new ball rect %s\n",
                            movable.pos.toString(), movableRectClone.toString(), movableRect.toString());

                    break goals_for;
                }
            }
        }

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

    void checkCollisionBetweenGoalsAndBall(RectF[] goalRects) {

        // check if ball is inside of any goal

        boolean isInsideAnyGoal = false;
        RectF insideGoalRect = null;

        for (RectF goalRect : goalRects) {
            if (isPointInsideRect(ballMovable.pos, goalRect)) {
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
        return 120f;
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
