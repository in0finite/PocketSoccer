package com.example.pocketsoccer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;

import com.example.pocketsoccer.db.AppDatabase;
import com.example.pocketsoccer.db.Game;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Date;

public class SoccerActivity extends AppCompatActivity {

    public static SoccerActivity instance = null;

    long mStartingTime = 0;
    float mElapsedTimeFromLastTime = 0;

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

    boolean mIsGameOver = false;

    boolean mGameStartedSinceStartup = false;
    public boolean isGameStartedSinceStartup() { return mGameStartedSinceStartup; }

    public boolean isCelebratingGoal = false;
    public float timeWhenStartedCelebratingGoal = 0f;
    public static final float kTimeToCelebrateGoal = 3f;
    int mNextPlayerWhenCelebrationFinishes;

    public int currentTurnPlayer = 0;
    float mTimeWhenTurnStarted = 0;
    public static final float kTurnTime = 3f;
    public int numTurnsPassed = 0;

    public Movable selectedMovable = null;

    public Drawable flagDrawable1,flagDrawable2;

    MyTask mTask;
    View mCustomView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        System.out.println("onCreate() started");

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

        byte[] continueGameData = intent.getByteArrayExtra("continueGameData");

        if (savedInstanceState != null)
            this.loadGameState(savedInstanceState);
        else if (continueGameData != null)
            this.loadGameState(continueGameData);

        // load flag drawables
        this.flagDrawable1 = getResources().getDrawable(this.flagIdPlayer1);
        this.flagDrawable2 = getResources().getDrawable(this.flagIdPlayer2);


        super.onCreate(savedInstanceState);


        // assign image ids
        ballImageId = R.drawable.football_ball_2426_2380_resized;
        fieldImageId = SettingsActivity.getTerrainDrawableId();

        setContentView(R.layout.activity_soccer);

        mCustomView = findViewById(R.id.soccerFieldView);

        System.out.println("onCreate() finished");


    }

    void startGame(int nextPlayer) {

        this.isCelebratingGoal = false;
        mWasBallInsideGoalLastTime = false;
        this.currentTurnPlayer = nextPlayer;
        mTimeWhenTurnStarted = getTimeSinceStartup();
        this.numTurnsPassed = 0;
        this.selectedMovable = null;

        this.movables.clear();


        // reset ball position
        this.ballMovable.pos = new Vec2(getFieldWidth() / 2f, getFieldHeight() / 2f);

        // set random velocity for the ball
//        Vec2 ballVelocity = Vec2.randomNormalized();
//        ballVelocity.multiply(800f);
//        this.ballMovable.velocity = ballVelocity;

        // reset ball velocity
        this.ballMovable.velocity = Vec2.zero();

        // set mass
        this.ballMovable.mass = 80f;

        this.movables.add(this.ballMovable);

        // create players
        createPlayers(this.flagIdPlayer1, this.flagIdPlayer2);

    }

    void createPlayers(int flagId1, int flagId2) {

        Vec2 playerSize = new Vec2(100, 100);

        for (int i=0; i < 3; i++) {

            // left side
            float x = getFieldWidth() / 3f;
            float y = (i + 1) / 4f * getFieldHeight();

            Movable movable = new Movable(new Vec2(x, y), playerSize, Vec2.zero());
            movable.drawable = getResources().getDrawable(flagId1);
            movable.mass = 80f;
            movable.player = 0;
            this.movables.add(movable);

            // right side
            x = getFieldWidth() * 2f / 3f;

            movable = new Movable(new Vec2(x, y), playerSize, Vec2.zero());
            movable.drawable = getResources().getDrawable(flagId2);
            movable.mass = 80f;
            movable.player = 1;
            this.movables.add(movable);

        }

    }

    void updateGame() {

        if (mIsGameOver)
            return;

        // check if game is over - time expired
        if (SettingsActivity.isGameLimitedWithTime()) {
            float gameTimeLimit = SettingsActivity.getGameTimeLimit();
            if (this.getTimeSinceStartup() >= gameTimeLimit) {
                // time expired
                this.onGameOver();
                return;
            }
        }

        RectF[] goalRects = new RectF[]{getLeftGoalRect(), getRightGoalRect()};

        // perform additional steps for more precise collision

        for (int i=0; i < kNumPhysicsSteps; i++) {
            updateGameSingleStep(goalRects);
        }

        if (!mGameStartedSinceStartup) {
            if (getTimeSinceStartup() > 1f) {
                mGameStartedSinceStartup = true;
                startGame(0);
            }
        }

        // check if AI should make a move
        if (mGameStartedSinceStartup && this.isPlayerAI(this.currentTurnPlayer)) {
            // AI is on the move
            float moveTimeForAI = Math.min(kTurnTime * 0.75f, 1.5f);    // simulate latency
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
            startGame(mNextPlayerWhenCelebrationFinishes);
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
        movable.pos.add(Vec2.multiply(movable.velocity, deltaTime * SettingsActivity.getGameSpeed()));

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
                mNextPlayerWhenCelebrationFinishes = isLeftGoal ? 0 : 1 ;
            }

            // check for game over
            if (SettingsActivity.isGameLimitedWithNumGoals()) {
                int goalLimit = SettingsActivity.getGoalLimit();
                if (this.scorePlayer1 >= goalLimit || this.scorePlayer2 >= goalLimit) {
                    // game over
                    this.onGameOver();
                }
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


    void onGameOver() {

        if (mIsGameOver)
            return;

        mIsGameOver = true;

        // insert game into db

        Game game = new Game();
        game.player1Name = this.namePlayer1;
        game.player2Name = this.namePlayer2;
        game.player1Score = this.scorePlayer1;
        game.player2Score = this.scorePlayer2;
        game.timeWhenFinished = new Date().getTime();
        game.timeElapsed = this.getTimeSinceStartup();

        try {
            AppDatabase.getInstance(this).gameDao().insertAll(game);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // delete saved game if it exists
        try {
            File savedGameFile = MainActivity.instance.getSavedGameFile();
            if (savedGameFile.exists())
                savedGameFile.delete();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // start stats activity

        Intent intent = new Intent(this, StatsForSingleGameActivity.class);
        intent.putExtra("player1Name", this.namePlayer1);
        intent.putExtra("player2Name", this.namePlayer2);
        this.startActivity(intent);

        // finish this activity
        this.finish();

    }

    void nextTurn() {
        this.currentTurnPlayer = (this.currentTurnPlayer + 1) % 2;
        mTimeWhenTurnStarted = getTimeSinceStartup();
        this.numTurnsPassed ++;
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

        if (0 == this.numTurnsPassed) {
            // this is the first move in the game
            // do some random stuff

            // choose random disk
            Movable movable = disks.get(Util.randomInt(0, disks.size() - 1));

            // shoot him towards random position on the ball
            Vec2 pos = Vec2.add( this.ballMovable.pos, Vec2.randomWithMaxLength(this.ballMovable.getRadius()) );

            Vec2 pushPos = Vec2.multiply(Vec2.add(pos, opponentGoalCenter), 0.5f);

            this.performAIMove(movable, pos, pushPos);

            return;
        }

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
            this.performAIMove(bestMovable, hitPos, opponentGoalCenter);
        }
        else {
            // try to hit opponent's disk
            if (bestMovable != null) {
                Movable opponentDisk = this.getClosestPlayerDisk(bestMovable.pos, this.getNonTurnPlayer());
                if (opponentDisk != null) {
                    this.performAIMove(bestMovable, opponentDisk.pos, opponentDisk.pos);
                }
            }
        }

    }

    void performAIMove(Movable sourceMovable, Vec2 targetPos, Vec2 pushTargetPos) {

        float maxStrength = 1200f;
        float minStrength = 400f;
        float distanceToPushTarget = Vec2.distance(sourceMovable.pos, pushTargetPos);
        float diagonalLength = new Vec2(this.getFieldWidth(), this.getFieldHeight()).length();

        float strength = distanceToPushTarget / (diagonalLength * 0.5f) * maxStrength;

        if (strength > maxStrength)
            strength = maxStrength;
        if (strength < minStrength)
            strength = minStrength;

        sourceMovable.velocity = Vec2.multiply(Vec2.substract(targetPos, sourceMovable.pos).normalized(), strength);

        System.out.printf("AI performed move - strength %f\n", strength);

        this.nextTurn();
    }


    float getTimeSinceStartup() {
        return (SystemClock.elapsedRealtime() - mStartingTime) / 1000f + mElapsedTimeFromLastTime;
    }


    void saveGameState(File file) {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            this.saveGameState(new DataOutputStream(byteArrayOutputStream));
            Util.writeToFile(file, byteArrayOutputStream.toByteArray());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    void saveGameState(DataOutputStream out) throws IOException {

        // time
        out.writeFloat(this.getTimeSinceStartup());
        out.writeLong(mStartingTime);

        // flag, name, is AI
        out.writeInt(this.flagIdPlayer1);
        out.writeInt(this.flagIdPlayer2);
        out.writeUTF(this.namePlayer1);
        out.writeUTF(this.namePlayer2);
        out.writeBoolean(this.isPlayer1AI);
        out.writeBoolean(this.isPlayer2AI);

        // ball
        //this.saveMovableState(this.ballMovable, out);

        // movables
        out.writeInt(this.movables.size());
        for (Movable movable : this.movables) {
            this.saveMovableState(movable, out);
        }

        out.writeBoolean(mWasBallInsideGoalLastTime);

        // score
        out.writeInt(this.scorePlayer1);
        out.writeInt(this.scorePlayer2);

        out.writeBoolean(mGameStartedSinceStartup);

        // celebration
        out.writeBoolean(this.isCelebratingGoal);
        out.writeFloat(this.timeWhenStartedCelebratingGoal);
        out.writeInt(mNextPlayerWhenCelebrationFinishes);

        // turn
        out.writeInt(this.currentTurnPlayer);
        out.writeFloat(mTimeWhenTurnStarted);
        out.writeInt(this.numTurnsPassed);


    }

    void saveMovableState(Movable m, DataOutputStream out) throws IOException {

        this.saveVec2(m.pos, out);
        this.saveVec2(m.size, out);
        this.saveVec2(m.velocity, out);
        out.writeFloat(m.mass);
        out.writeInt(m.player);

    }

    void saveVec2(Vec2 v, DataOutputStream out) throws IOException {
        out.writeFloat(v.x);
        out.writeFloat(v.y);
    }

    void loadGameState(Bundle bundle) {
        System.out.println("loadGameState(Bundle)");
        byte[] data = bundle.getByteArray("data");
        if (data != null)
            this.loadGameState(data);
    }

    void loadGameState(byte[] data) {
        try {
            System.out.println("loadGameState(byte[])");
            this.loadGameState(new DataInputStream(new ByteArrayInputStream(data)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void loadGameState(DataInputStream in) throws IOException {

        // time
        float elapsedGameTime = in.readFloat();
        mElapsedTimeFromLastTime = elapsedGameTime;
        long startingTimeAtSaving = in.readLong();

        // flag, name, is AI
        this.flagIdPlayer1 = in.readInt();
        this.flagIdPlayer2 = in.readInt();
        this.namePlayer1 = in.readUTF();
        this.namePlayer2 = in.readUTF();
        this.isPlayer1AI = in.readBoolean();
        this.isPlayer2AI = in.readBoolean();

        // ball
        //loadMovable(in);

        // movables
        this.movables.clear();
        int numMovables = in.readInt();
        for (int i=0; i < numMovables; i++) {
            Movable m = loadMovable(in, this.flagIdPlayer1, this.flagIdPlayer2);
            this.movables.add(m);
        }

        // assign ball movable
        this.ballMovable = this.movables.get(0);

        mWasBallInsideGoalLastTime = in.readBoolean();

        // score
        this.scorePlayer1 = in.readInt();
        this.scorePlayer2 = in.readInt();

        mGameStartedSinceStartup = in.readBoolean();

        // celebration
        this.isCelebratingGoal = in.readBoolean();
        this.timeWhenStartedCelebratingGoal = in.readFloat();
        mNextPlayerWhenCelebrationFinishes = in.readInt();

        // turn
        this.currentTurnPlayer = in.readInt();
        mTimeWhenTurnStarted = in.readFloat();
        this.numTurnsPassed = in.readInt();

        // restore drawables
        this.flagDrawable1 = getResources().getDrawable(this.flagIdPlayer1);
        this.flagDrawable2 = getResources().getDrawable(this.flagIdPlayer2);

        // adjust times
        float myCurrentTime = this.getTimeSinceStartup();

        float timeDiff = elapsedGameTime - this.timeWhenStartedCelebratingGoal;
        this.timeWhenStartedCelebratingGoal = myCurrentTime - timeDiff;

        timeDiff = elapsedGameTime - mTimeWhenTurnStarted;
        mTimeWhenTurnStarted = myCurrentTime - timeDiff;


        System.out.println("loadGameState() ok");

    }

    Movable loadMovable(DataInputStream in, int flagId1, int flagId2) throws IOException {
        Movable m = new Movable();
        m.pos = loadVec2(in);
        m.size = loadVec2(in);
        m.velocity = loadVec2(in);
        m.mass = in.readFloat();
        m.player = in.readInt();
        if (0 == m.player)
            m.drawable = this.getResources().getDrawable(flagId1);
        else if (1 == m.player)
            m.drawable = this.getResources().getDrawable(flagId2);
        return m;
    }

    Vec2 loadVec2(DataInputStream in) throws IOException {
        return new Vec2(in.readFloat(), in.readFloat());
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        System.out.println("onSaveInstanceState()");

        super.onSaveInstanceState(outState);

        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            this.saveGameState(new DataOutputStream(byteArrayOutputStream));
            outState.putByteArray("data", byteArrayOutputStream.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onStart() {

        System.out.println("onStart()");

        super.onStart();

        if (null == mTask) {
            // create async task which will update the game
            mTask = new MyTask(new Runnable() {
                @Override
                public void run() {
                    updateGame();
                }
            });
            mTask.execute();
        }

    }

    @Override
    protected void onStop() {

        System.out.println("onStop()");

        if (mTask != null) {
            mTask.cancel(false);
            mTask = null;
        }

        // save the game
        File file = MainActivity.instance.getSavedGameFile();
        this.saveGameState(file);

        super.onStop();
    }

}
