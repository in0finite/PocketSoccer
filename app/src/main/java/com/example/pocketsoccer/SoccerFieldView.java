package com.example.pocketsoccer;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * TODO: document your custom view class.
 */
public class SoccerFieldView extends View {
    private String mExampleString; // TODO: use a default from R.string...
    private int mExampleColor = Color.RED; // TODO: use a default from R.color...
    private float mExampleDimension = 0; // TODO: use a default from R.dimen...
    private Drawable mExampleDrawable;

    private TextPaint mTextPaint;
    private float mTextWidth;
    private float mTextHeight;

    Drawable ballDrawable;
    Drawable fieldDrawable;

    Paint ballPaint, fieldPaint, goalPostPaint, goalCornerPaint;



    public SoccerFieldView(Context context) {
        super(context);
        init(null, 0);
    }

    public SoccerFieldView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public SoccerFieldView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {

        /*
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.SoccerFieldView, defStyle, 0);

        mExampleString = a.getString(
                R.styleable.SoccerFieldView_exampleString);
        mExampleColor = a.getColor(
                R.styleable.SoccerFieldView_exampleColor,
                mExampleColor);
        // Use getDimensionPixelSize or getDimensionPixelOffset when dealing with
        // values that should fall on pixel boundaries.
        mExampleDimension = a.getDimension(
                R.styleable.SoccerFieldView_exampleDimension,
                mExampleDimension);

        if (a.hasValue(R.styleable.SoccerFieldView_exampleDrawable)) {
            mExampleDrawable = a.getDrawable(
                    R.styleable.SoccerFieldView_exampleDrawable);
            mExampleDrawable.setCallback(this);
        }

        a.recycle();
        */

        // Set up a default TextPaint object
        mTextPaint = new TextPaint();
        mTextPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setStrokeWidth(5f);
        mTextPaint.setTextSize(35);

        ballPaint = new Paint();
        fieldPaint = new Paint();

        goalPostPaint = new Paint();
        goalPostPaint.setColor(Color.WHITE);
        goalPostPaint.setStrokeWidth(3f);

        goalCornerPaint = new Paint();
        goalCornerPaint.setColor(Color.RED);
        goalCornerPaint.setStrokeWidth(3f);

        ballDrawable = getResources().getDrawable(SoccerActivity.ballImageId);
        fieldDrawable = getResources().getDrawable(SoccerActivity.fieldImageId);


        // Update TextPaint and text measurements from attributes
        invalidateTextPaintAndMeasurements();
    }

    private void invalidateTextPaintAndMeasurements() {

        //mTextPaint.setColor(mExampleColor);
        //mTextWidth = mTextPaint.measureText("0");

        Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
        mTextHeight = fontMetrics.bottom;

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int w = widthMeasureSpec;
        int h = heightMeasureSpec;

        System.out.printf("onMeasure(): %d, %d\n", w, h);
        System.out.println("onMeasure()");

        setMeasuredDimension(w, h);

    }

    @Override
    protected void onDraw(Canvas canvas) {

        //Log.i(MainActivity.LOG_TAG, "onDraw()");

        super.onDraw(canvas);

        // TODO: consider storing these as member variables to reduce
        // allocations per draw cycle.
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        int paddingRight = getPaddingRight();
        int paddingBottom = getPaddingBottom();

        int contentWidth = getWidth() - paddingLeft - paddingRight;
        int contentHeight = getHeight() - paddingTop - paddingBottom;

        /*
        // Draw the text.
        canvas.drawText(mExampleString,
                paddingLeft + (contentWidth - mTextWidth) / 2,
                paddingTop + (contentHeight + mTextHeight) / 2,
                mTextPaint);

        // Draw the example drawable on top of the text.
        if (mExampleDrawable != null) {
            mExampleDrawable.setBounds(paddingLeft, paddingTop,
                    paddingLeft + contentWidth, paddingTop + contentHeight);
            mExampleDrawable.draw(canvas);
        }
        */

        // draw field
        if (fieldDrawable != null) {
            drawDrawable(fieldDrawable, new Vec2(paddingLeft + contentWidth / 2f, paddingTop + contentHeight / 2f),
                    new Vec2(contentWidth, contentHeight), canvas);
        }

        // draw movables
        for (Movable movable : SoccerActivity.instance.movables) {
            if (movable.drawable != null)
                drawDrawable(movable.drawable, movable.pos, movable.size, canvas);
        }

        // draw ball
        if (ballDrawable != null) {
            drawDrawable(ballDrawable, SoccerActivity.instance.ballMovable.pos, SoccerActivity.instance.ballMovable.size, canvas);
        }

        // draw goal posts and goal corners
        RectF[] goalRects = new RectF[]{SoccerActivity.instance.getLeftGoalRect(), SoccerActivity.instance.getRightGoalRect()};
        for (RectF goalRect : goalRects) {
            RectF[] goalPostRects = new RectF[]{SoccerActivity.instance.getUpperGoalPost(goalRect), SoccerActivity.instance.getLowerGoalPost(goalRect)};
            for (RectF goalPostRect : goalPostRects) {
                canvas.drawRect(goalPostRect, goalPostPaint);
                for (Movable cornerMovable : SoccerActivity.instance.getCornersForGoalPost(goalPostRect)) {
                    canvas.drawCircle(cornerMovable.pos.x, cornerMovable.pos.y, cornerMovable.getRadius(), goalCornerPaint);
                }
            }
        }

        // draw score

        float scorePosY = 5 + 50;
        float scorePosX1 = getWidth() * 0.4f;
        float scorePosX2 = getWidth() * 0.6f;
        canvas.drawText(String.valueOf(SoccerActivity.instance.scorePlayer1), scorePosX1, scorePosY, mTextPaint);
        canvas.drawText(String.valueOf(SoccerActivity.instance.scorePlayer2), scorePosX2, scorePosY, mTextPaint);

        // draw rectangle around score of current player

        float scorePosXOfCurrentPlayer = SoccerActivity.instance.currentPlayerTurn == 0 ? scorePosX1 : scorePosX2;
        float rectAroundScoreSize = 100;
        canvas.drawRect(new RectF(scorePosXOfCurrentPlayer - rectAroundScoreSize * 0.5f, scorePosY - rectAroundScoreSize * 0.5f,
                scorePosXOfCurrentPlayer + rectAroundScoreSize * 0.5f, scorePosY + rectAroundScoreSize * 0.5f), mTextPaint);


    }

    static void drawDrawable(Drawable drawable, Vec2 pos, Vec2 size, Canvas canvas) {
        drawable.setBounds((int)(pos.x - size.x / 2f),
                (int)(pos.y - size.y / 2f),
                (int)(pos.x + size.x / 2f),
                (int)(pos.y + size.y / 2f));
        drawable.draw(canvas);
    }

    /**
     * Gets the example string attribute value.
     *
     * @return The example string attribute value.
     */
    public String getExampleString() {
        return mExampleString;
    }

    /**
     * Sets the view's example string attribute value. In the example view, this string
     * is the text to draw.
     *
     * @param exampleString The example string attribute value to use.
     */
    public void setExampleString(String exampleString) {
        mExampleString = exampleString;
        invalidateTextPaintAndMeasurements();
    }

    /**
     * Gets the example color attribute value.
     *
     * @return The example color attribute value.
     */
    public int getExampleColor() {
        return mExampleColor;
    }

    /**
     * Sets the view's example color attribute value. In the example view, this color
     * is the font color.
     *
     * @param exampleColor The example color attribute value to use.
     */
    public void setExampleColor(int exampleColor) {
        mExampleColor = exampleColor;
        invalidateTextPaintAndMeasurements();
    }

    /**
     * Gets the example dimension attribute value.
     *
     * @return The example dimension attribute value.
     */
    public float getExampleDimension() {
        return mExampleDimension;
    }

    /**
     * Sets the view's example dimension attribute value. In the example view, this dimension
     * is the font size.
     *
     * @param exampleDimension The example dimension attribute value to use.
     */
    public void setExampleDimension(float exampleDimension) {
        mExampleDimension = exampleDimension;
        invalidateTextPaintAndMeasurements();
    }

    /**
     * Gets the example drawable attribute value.
     *
     * @return The example drawable attribute value.
     */
    public Drawable getExampleDrawable() {
        return mExampleDrawable;
    }

    /**
     * Sets the view's example drawable attribute value. In the example view, this drawable is
     * drawn above the text.
     *
     * @param exampleDrawable The example drawable attribute value to use.
     */
    public void setExampleDrawable(Drawable exampleDrawable) {
        mExampleDrawable = exampleDrawable;
    }
}
