package com.example.pocketsoccer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;

public class SoccerActivity extends AppCompatActivity {

    public static SoccerActivity instance = null;

    long mStartingTime = 0;

    public static int ballImageId;
    public static int fieldImageId;

    public int flagIdPlayer1 = 0, flagIdPlayer2 = 0;
    public String namePlayer1 = "", namePlayer2 = "";
    public boolean isPlayer1AI = false, isPlayer2AI = false;

    public boolean isDeathMatchModeOn = false;

    public Movable ballMovable = new Ball(new Vec2(150, 150), new Vec2(40, 40), Vec2.zero());
    //public Vec2 ballPos = new Vec2(0, 0);
    //public Vec2 ballSize = new Vec2(40, 40);
    //public Vec2 ballVelocity = new Vec2(0, 0);

    public ArrayList<Movable> movables = new ArrayList<>();

    static final int kNumPhysicsSteps = 10;

    public float deltaTime = 0.05f / kNumPhysicsSteps;

    boolean mWasBallInsideGoalLastTime = false;
    float mPosDeltaAfterCollision = 0f;

    int scorePlayer1 = 0, scorePlayer2 = 0;

    boolean mGameStartedSinceStartup = false;

    public boolean isCelebratingGoal = false;
    public float timeWhenStartedCelebratingGoal = 0f;
    public static final float kTimeToCelebrateGoal = 3f;

    public int currentTurnPlayer = 0;
    float mTimeWhenTurnStarted = 0;
    public static final float kTurnTime = 3f;

    public Movable selectedMovable = null;

    public Drawable flagDrawable1,flagDrawable2;

    MyTask mTask;
    View mCustomView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        instance = this;

        mStartingTime = SystemClock.elapsedRealtime();

        // get args from intent
        Intent intent = getIntent();
        this.flagIdPlayer1 = intent.getIntExtra("flagId1", 0);
        this.flagIdPlayer2 = intent.getIntExtra("flagId2", 0);
        this.namePlayer1 = intent.getStringExtra("name1");
        this.namePlayer2 = intent.getStringExtra("name2");
        this.isPlayer1AI = intent.getBooleanExtra("isAI1", false);
        this.isPlayer2AI = intent.getBooleanExtra("isAI2", false);

        this.flagDrawable1 = getResources().getDrawable(this.flagIdPlayer1);
        this.flagDrawable2 = getResources().getDrawable(this.flagIdPlayer2);


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


    }

    void startGame() {

        this.isCelebratingGoal = false;
        mWasBallInsideGoalLastTime = false;
        this.currentTurnPlayer = 0;
        mTimeWhenTurnStarted = getTimeSinceStartup();
        this.selectedMovable = null;

        this.movables.clear();


        // reset ball position
        this.ballMovable.pos = new Vec2(getFieldWidth() / 2f, getFieldHeight() / 2f);

        // set random velocity for the ball
        Vec2 ballVelocity = Vec2.randomNormalized();
        ballVelocity.multiply(800f);
        this.ballMovable.velocity = ballVelocity;

        // set mass
        this.ballMovable.mass = 80f;

        this.movables.add(this.ballMovable);

        // create players
        createPlayers(this.flagIdPlayer1, this.flagIdPlayer2);

    }

    void createPlayers(int flagId1, int flagId2) {

        Vec2 playerSize = new Vec2(80, 80);

        for (int i=0; i < 2; i++) {

            // left side
            float x = getFieldWidth() / 3f;
            float y = (i + 1) / 4f * getFieldHeight();

            Movable movable = new Movable(new Vec2(x, y), playerSize, Vec2.randomWithMaxLength(600));
            movable.drawable = getResources().getDrawable(flagId1);
            movable.mass = 80f;
            movable.player = 0;
            this.movables.add(movable);

            // right side
            x = getFieldWidth() * 2f / 3f;

            movable = new Movable(new Vec2(x, y), playerSize, Vec2.randomWithMaxLength(600));
            movable.drawable = getResources().getDrawable(flagId2);
            movable.mass = 80f;
            movable.player = 1;
            this.movables.add(movable);

        }

    }

    void updateGame() {

        RectF[] goalRects = new RectF[]{getLeftGoalRect(), getRightGoalRect()};

        // perform additional steps for more precise collision

        for (int i=0; i < kNumPhysicsSteps; i++) {
            updateGameSingleStep(goalRects);
        }

        if (!mGameStartedSinceStartup) {
            if (getTimeSinceStartup() > 2f) {
                mGameStartedSinceStartup = true;
                startGame();
            }
        }

        // check if AI should make a move
        if (mGameStartedSinceStartup && this.isPlayerAI(this.currentTurnPlayer)) {
            // AI is on the move
            float moveTimeForAI = Math.min(kTurnTime * 0.75f, 2.5f);    // simulate latency
            if (getTimeSinceStartup() - mTimeWhenTurnStarted >= moveTimeForAI) {
                // AI should make a move
                this.performAIMove(goalRects);
            }
        }

        // check if new turn should start
        if (mGameStartedSinceStartup && getTimeSinceStartup() - mTimeWhenTurnStarted >= kTurnTime) {
            // time for this turn expired
            nextTurn();
        }

        // check if we should stop celebrating
        if (this.isCelebratingGoal && getTimeSinceStartup() - this.timeWhenStartedCelebratingGoal >= kTimeToCelebrateGoal) {
            this.isCelebratingGoal = false;
            startGame();
        }

        // update graphics
        mCustomView.invalidate();

    }

    void updateGameSingleStep(RectF[] goalRects) {

        // for each movable: move it, constrain position, check for collision with goal posts

        for (Movable movable : this.movables) {
            updateMovable(movable, goalRects);
        }

        // check for collision between movables, but only for those who didn't have collision with static object
        checkCollisionBetweenMovables();

        // at the end, check if ball entered goal
        if (!this.isCelebratingGoal)
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

        if (movable.pos.x <= 0) {
            movable.pos.x = 1;
            movable.velocity.x = Math.abs(movable.velocity.x);
        }
        else if (movable.pos.x >= fieldWidth) {
            movable.pos.x = fieldWidth - 1;
            movable.velocity.x = - Math.abs(movable.velocity.x);
        }

        if (movable.pos.y <= 0) {
            movable.pos.y = 1;
            movable.velocity.y = Math.abs(movable.velocity.y);
        }
        else if(movable.pos.y >= fieldHeight) {
            movable.pos.y = fieldHeight - 1;
            movable.velocity.y = - Math.abs(movable.velocity.y);
        }

        if (! posBeforeConstraining.equals(movable.pos)) {
            // movable hit an edge of the field
            movable.hadCollisionWithStaticObject = true;
        }

        // check for collision between goal posts/goal corners and movable

        goals_for: for (RectF goalRect : goalRects) {

            RectF[] goalPostRects = new RectF[]{getUpperGoalPost(goalRect), getLowerGoalPost(goalRect)};

            // goal corners
            // create 2 corners for every post - to make it easier

            for (RectF goalPostRect : goalPostRects) {
                Movable[] cornersForGoalPost = getCornersForGoalPost(goalPostRect);
                for (Movable cornerMovable : cornersForGoalPost) {
                    if (checkCollisionBetweenCircles(movable, cornerMovable)) {
                        break goals_for;
                    }
                }
            }

            // goal posts

            for (RectF goalPostRect : goalPostRects) {
                RectF movableRect = rectFromPosAndSize(movable.pos, movable.size);
                RectF movableRectClone = new RectF(movableRect);
                if (checkCollisionBetweenCircleAndGoalPost(movableRect, goalPostRect, movable.velocity)) {
                    // collision happened

                    movable.hadCollisionWithStaticObject = true;

                    // apply new position
                    //movable.pos = new Vec2(movableRect.centerX(), movableRect.centerY());
                    movable.pos.y += mPosDeltaAfterCollision;
//                    System.out.printf("ball position after collision: %s, original ball rect %s, new ball rect %s\n",
//                            movable.pos.toString(), movableRectClone.toString(), movableRect.toString());

                    break goals_for;
                }
            }

        }

    }

    boolean checkCollisionBetweenCircleAndGoalPost(RectF circleRect, RectF goalPostRect, Vec2 velocity) {

        //if (circleRect.intersects(goalPostRect.left, goalPostRect.top, goalPostRect.right, goalPostRect.bottom)) {
        if (circleIntersectsRect(new Vec2(circleRect.centerX(), circleRect.centerY()), circleRect.width() * 0.5f, goalPostRect)) {

            boolean isFromUpperSide = false;

            Vec2 originalVelocity = velocity.clone();
            float delta = 0f;

            //float circleTop = circlePos.y + circleRadius;
            //float circleBottom = circlePos.y - circleRadius;

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

            delta += Math.signum(delta) * 2.0f;

//            circleRect.top += delta;
//            circleRect.bottom += delta;
            //circleRect = new RectF(circleRect.left, circleRect.top + delta, circleRect.right, circleRect.bottom + delta);
            mPosDeltaAfterCollision = delta;

//            System.out.printf("collision between circle and goal post - circle rect: %s, goal post rect: %s, original velocity %s, new velocity: %s, delta %f, is from upper side: %b\n",
//                    circleRect.toString(), goalPostRect.toString(), originalVelocity.toString(), velocity.toString(), delta, isFromUpperSide);

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

            if (isLeftGoal)
                scorePlayer2 ++;
            else
                scorePlayer1 ++;

            // start celebration
            if (!this.isDeathMatchModeOn) {
                this.isCelebratingGoal = true;
                this.timeWhenStartedCelebratingGoal = getTimeSinceStartup();
            }
        }

        mWasBallInsideGoalLastTime = isInsideAnyGoal;

    }

    void checkCollisionBetweenMovables() {

        for (int i=0; i < this.movables.size(); i++) {
            Movable movableA = this.movables.get(i);
            if (movableA.hadCollisionWithStaticObject)
                continue;

            for (int j=i+1; j < this.movables.size(); j++) {
                Movable movableB = this.movables.get(j);
                if (movableB.hadCollisionWithStaticObject)
                    continue;

                if (checkCollisionBetweenCircles(movableA, movableB)) {

                    break;  // only 1 collision per frame for a single movable, so break
                }

            }
        }

    }

    boolean checkCollisionBetweenCircles(Movable movableA, Movable movableB) {

        if (movableA.mass == 0 && movableB.mass == 0)
            return false;

        boolean oneHasZeroMass = (movableA.mass == 0 || movableB.mass == 0);

        float distance = Vec2.distance(movableA.pos, movableB.pos);

        if (distance < movableA.getRadius() + movableB.getRadius()) {
            // 2 circles intersect
            // resolve collision

            Vec2 diff = Vec2.substract(movableA.pos, movableB.pos);
            Vec2 diffNormalized = diff.normalized();
            float delta = movableA.getRadius() + movableB.getRadius() - distance + 0.01f;

            float relativeVelocity = Vec2.substract(movableA.velocity, movableB.velocity).length();

            float velocityFactor = oneHasZeroMass ? 2f : 1f;
            movableA.velocity.add( Vec2.multiply(diffNormalized, relativeVelocity / 2f * velocityFactor));
            movableB.velocity.add( Vec2.multiply(diffNormalized, - relativeVelocity / 2f * velocityFactor));

            float positionFactor = oneHasZeroMass ? 2f : 1f;
            movableA.pos.add( Vec2.multiply(diffNormalized, delta / 2f * positionFactor) );
            movableB.pos.add( Vec2.multiply(diffNormalized, - delta / 2f * positionFactor) );

            movableA.hadCollisionWithStaticObject = true;
            movableB.hadCollisionWithStaticObject = true;

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

    Movable[] getCornersForGoalPost(RectF goalPostRect) {

        // 2 corners

        Movable[] movables = new Movable[2];

        Vec2 pos, size;

        pos = new Vec2(goalPostRect.left - 4f, goalPostRect.centerY());
        size = new Vec2(getGoalPostHeight(), getGoalPostHeight());

        movables[0] = new Movable(pos, size, Vec2.zero(), 0f);

        pos = new Vec2(goalPostRect.right + 4f, goalPostRect.centerY());
        size = new Vec2(getGoalPostHeight(), getGoalPostHeight());

        movables[1] = new Movable(pos, size, Vec2.zero(), 0f);

        return movables;
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

    static boolean circleIntersectsRect(Vec2 center, float radius, RectF rect) {

//        // test all 4 points
//        Vec2[] points = new Vec2[]{new Vec2(rect.left, rect.top), new Vec2(rect.right, rect.top), new Vec2(rect.left, rect.bottom),
//                new Vec2(rect.right, rect.bottom)};
//        for (Vec2 point : points) {
//            if (isPointInCircle(center, radius, point))
//                return true;
//        }
//        return false;

        Vec2 circleDistance = new Vec2();
        circleDistance.x = Math.abs(center.x - rect.centerX());
        circleDistance.y = Math.abs(center.y - rect.centerY());

        if (circleDistance.x > (rect.width()/2 + radius)) { return false; }
        if (circleDistance.y > (rect.height()/2 + radius)) { return false; }

        if (circleDistance.x <= (rect.width()/2)) { return true; }
        if (circleDistance.y <= (rect.height()/2)) { return true; }

        double cornerDistance_sq = Math.pow(circleDistance.x - rect.width()/2f, 2f) +
                Math.pow(circleDistance.y - rect.height()/2f, 2f);

        return ( cornerDistance_sq <= Math.pow(radius, 2f) );
    }

    static boolean isPointInCircle(Vec2 center, float radius, Vec2 point) {
        return Vec2.distance(center, point) < radius;
    }

    static RectF rectFromPosAndSize(Vec2 pos, Vec2 size) {
        return new RectF((pos.x - size.x / 2f),
                (pos.y - size.y / 2f),
                (pos.x + size.x / 2f),
                (pos.y + size.y / 2f));
    }


    void nextTurn() {
        this.currentTurnPlayer = (this.currentTurnPlayer + 1) % 2;
        mTimeWhenTurnStarted = getTimeSinceStartup();
    }

    int getNonTurnPlayer() {
        return (this.currentTurnPlayer + 1) % 2;
    }

    boolean isPlayerAI(int player) {
        if (0 == player)
            return this.isPlayer1AI;
        else if (1 == player)
            return this.isPlayer2AI;
        else
            return false;
    }

    boolean isPlayerHuman(int player) {
        return ! isPlayerAI(player);
    }

    Movable getClosestPlayerDisk(Vec2 pos, int player) {

        Movable closestMovable = null;
        float smallestDistance = Float.MAX_VALUE;

        for (Movable movable : this.movables) {

            if (movable instanceof Ball)    // skip ball
                continue;
            if (movable.player != player)
                continue;

            float distance = Vec2.distance(movable.pos, pos);
            if (distance <= smallestDistance) {
                smallestDistance = distance;
                closestMovable = movable;
            }
        }

        return closestMovable;
    }

    ArrayList<Movable> getPlayerDisks(int playerId) {
        ArrayList<Movable> list = new ArrayList<>();
        for (Movable movable : this.movables) {
            if (movable instanceof Ball)    // skip ball
                continue;
            if (movable.player != playerId)
                continue;
            list.add(movable);
        }
        return list;
    }

    void performAIMove(RectF[] goalRects) {

        RectF opponentGoalRect = goalRects[this.getNonTurnPlayer()];
        Vec2 opponentGoalCenter = Vec2.fromRectCenter(opponentGoalRect);
        ArrayList<Movable> disks = this.getPlayerDisks(this.currentTurnPlayer);
        float diskRadius = disks.get(0).getRadius();

        Vec2 dirFromBallToGoal = Vec2.substract(opponentGoalCenter, this.ballMovable.pos).normalized();

        Vec2 hitPos = Vec2.substract( this.ballMovable.pos, Vec2.multiply( dirFromBallToGoal, (this.ballMovable.getRadius() + diskRadius) ) );

        // find disk with best direction

        float smallestAngle = Float.MAX_VALUE;
        Movable bestMovable = null;

        for (Movable movable : disks) {
            Vec2 moveDir = Vec2.substract(hitPos, movable.pos).normalized();
            float angle = Vec2.angle(dirFromBallToGoal, moveDir);
            if (angle <= smallestAngle) {
                smallestAngle = angle;
                bestMovable = movable;
            }
        }

        System.out.printf("AI - smallest angle: %f\n", smallestAngle);

        if (bestMovable != null && smallestAngle < 120) {
            this.performAIMove(bestMovable, hitPos);
        }
        else {
            // try to hit opponent's disk
            if (bestMovable != null) {
                Movable opponentDisk = this.getClosestPlayerDisk(bestMovable.pos, this.getNonTurnPlayer());
                if (opponentDisk != null) {
                    this.performAIMove(bestMovable, opponentDisk.pos);
                }
            }
        }

    }

    void performAIMove(Movable sourceMovable, Vec2 targetPos) {

        float maxStrength = 1000f;
        float minStrength = 400f;
        float distance = Vec2.distance(sourceMovable.pos, targetPos);
        float diagonalLength = new Vec2(this.getFieldWidth(), this.getFieldHeight()).length();

        float strength = distance / (diagonalLength * 0.5f) * maxStrength;

        if (strength > maxStrength)
            strength = maxStrength;
        if (strength < minStrength)
            strength = minStrength;

        sourceMovable.velocity = Vec2.multiply(Vec2.substract(targetPos, sourceMovable.pos).normalized(), strength);

        System.out.printf("AI performed move - strength %f\n", strength);

        this.nextTurn();
    }


    float getTimeSinceStartup() {
        return (SystemClock.elapsedRealtime() - mStartingTime) / 1000f;
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
