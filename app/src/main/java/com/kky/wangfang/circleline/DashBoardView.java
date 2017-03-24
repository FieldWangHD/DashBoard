package com.kky.wangfang.circleline;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import java.lang.ref.WeakReference;

public class DashBoardView extends View {
    private static final String TAG = DashBoardView.class.getName();

    private int mWidth;
    private int mHeight;
    private int mCenterX;
    private int mCenterY;
    private int mRadius;

    private Paint mInCircleIndexPaint;
    private Paint mOutCircleIndexPaint;
    private Paint mBigGraduatedLinePaint;
    private Paint mSmallGraduatedLinePaint;

    private static final float FACTOR_INDEX_OUT_RADIUS = 0.92f;
    private static final float FACTOR_INDEX_IN_RADIUS = 0.8f;
    private static final float FACTOR_GRADUATED_BIG_RADIUS = 0.6f;
    private static final float FACTOR_GRADUATED_BIG_WIDTH = 0.2f;
    private static final float FACTOR_GRADUATED_SMALL_RADIUS = 0.65f;
    private static final float FACTOR_GRADUATED_SMALL_WIDTH = 0.07f;

    private float mIndexOutRadius;
    private float mIndexInRadius;
    private float mGraduatedBigRadius;
    private float mGraduatedBigWidth;
    private float mGraduatedSmallRadius;
    private float mGraduatedSmallWidth;

    private static final int COLOR_GRADUATED_INDEX = 0x66ffffff;
    private static final int COLOR_INDEX_IN = 0x9900d2ff;
    private static final int COLOR_INDEX_OUT = 0xFF00d2ff;

    private static final int WIDTH_STROKE_GRA_BIG = 2;
    private static final int WIDTH_STROKE_GRA_SMALL = 2;
    private static final int WIDTH_STROKE_INDEX_OUT = 20;

    private static final int ANGLE_START = 150;
    private static final int ANGLE_SWEEP = 240;
    private static final int ANGLE_TOTAL = 360;
    private static final int ANGLE_OFFSET = ANGLE_START + ANGLE_SWEEP - ANGLE_TOTAL;
    // 表示多少等分，这里big绘制时会多一个，small绘制时会少一个
    private static final int NUM_BIG_EQUAL = 6;
    private static final int NUM_SMALL_EQUAL = 10;
    private static final int NUM_TOTAL = NUM_BIG_EQUAL * NUM_SMALL_EQUAL;

    private float mSweepAngle;

    private RectF mOutRect;

    private static final int ANI_DEFAULT_DURING = 2000;
    private static final float ANI_START_OFFSET = 0.62f;
    private static final float ANI_START_OFFSET_HALF = ANI_START_OFFSET / 2;
    private ValueAnimator mEnterAni;
    private WeakReference<View> mRefView;


    public DashBoardView(Context context) {
        this(context, null);
    }

    public DashBoardView(Context context, AttributeSet attrs) {
        super(context, attrs);

        initPaint();
        mRefView = new WeakReference<View>(this);
        performEnterAni(70);
    }

    private void initPaint() {
        mInCircleIndexPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mOutCircleIndexPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBigGraduatedLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mSmallGraduatedLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        mOutCircleIndexPaint.setStyle(Paint.Style.STROKE);
        mBigGraduatedLinePaint.setStyle(Paint.Style.STROKE);

        mOutCircleIndexPaint.setStrokeWidth(WIDTH_STROKE_INDEX_OUT);
        mBigGraduatedLinePaint.setStrokeWidth(WIDTH_STROKE_GRA_BIG);
        mSmallGraduatedLinePaint.setStrokeWidth(WIDTH_STROKE_GRA_SMALL);

        mBigGraduatedLinePaint.setColor(COLOR_GRADUATED_INDEX);
        mSmallGraduatedLinePaint.setColor(COLOR_GRADUATED_INDEX);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);


        int designWidth;
        if (widthMode == MeasureSpec.EXACTLY) {
            designWidth = widthSize;
        } else {
            // TODO: 2016/12/20 wrap_content
            designWidth = 0;
        }

        setMeasuredDimension(designWidth, designWidth);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        if (changed) {
            mWidth = getMeasuredWidth();
            mHeight = getMeasuredHeight();
            mCenterX = mWidth / 2;
            mCenterY = mHeight / 2;
            mRadius = mWidth / 2;

            mIndexOutRadius = FACTOR_INDEX_OUT_RADIUS * mRadius;
            mIndexInRadius = FACTOR_INDEX_IN_RADIUS * mRadius;
            mGraduatedBigRadius = FACTOR_GRADUATED_BIG_RADIUS * mRadius;
            mGraduatedBigWidth = FACTOR_GRADUATED_BIG_WIDTH * mRadius;
            mGraduatedSmallRadius = FACTOR_GRADUATED_SMALL_RADIUS * mRadius;
            mGraduatedSmallWidth = FACTOR_GRADUATED_SMALL_WIDTH * mRadius;

            calOutRect();
        }
    }

    private void calOutRect() {
        float offset = WIDTH_STROKE_INDEX_OUT / 2;
        mOutRect = new RectF(mRadius - mIndexOutRadius + offset, mRadius - mIndexOutRadius + offset,
                mCenterX + mIndexOutRadius - offset, mCenterY + mIndexOutRadius - offset);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawOutIndex(canvas);
        drawInIndex(canvas);
        drawGraduated(canvas);
    }

    // 需要保证end<1
    private void drawOutIndex(Canvas canvas) {
        canvas.save();
        canvas.rotate(ANGLE_OFFSET, mCenterX, mCenterY);
        canvas.drawArc(mOutRect, ANGLE_START - ANGLE_OFFSET,
                mSweepAngle, false, mOutCircleIndexPaint);
        canvas.restore();
    }

    private void drawInIndex(Canvas canvas) {
        canvas.save();
        canvas.rotate(ANGLE_OFFSET, mCenterX, mCenterY);
        canvas.drawCircle(mCenterX, mCenterY, mIndexInRadius, mInCircleIndexPaint);
        canvas.restore();
    }

    private void drawGraduated(Canvas canvas) {
        int eachBig = ANGLE_SWEEP / NUM_BIG_EQUAL;
        // 大的标尺会多一个
        for (int i = 0; i < NUM_BIG_EQUAL + 1; i++) {
            int bigAngle = ANGLE_START + eachBig * i;
            canvas.save();
            canvas.rotate(bigAngle, mCenterX, mCenterY);
            canvas.drawLine(mCenterX + mGraduatedBigRadius, mCenterY,
                    mCenterX + mGraduatedBigRadius + mGraduatedBigWidth, mCenterY, mBigGraduatedLinePaint);
            canvas.restore();

            int eachSmall = eachBig / NUM_SMALL_EQUAL;
            // 小的标尺会少一个
            if (i == NUM_BIG_EQUAL)
                return;
            for (int j = 1; j < NUM_SMALL_EQUAL; j++) {
                int smallAngle = j * eachSmall + bigAngle;
                canvas.save();
                canvas.rotate(smallAngle, mCenterX, mCenterY);
                canvas.drawLine(mCenterX + mGraduatedSmallRadius, mCenterY,
                        mCenterX + mGraduatedSmallRadius + mGraduatedSmallWidth, mCenterY, mSmallGraduatedLinePaint);
                canvas.restore();
            }
        }
    }

    public void performEnterAni(final float angle) {
        post(new Runnable() {
            @Override
            public void run() {
                mEnterAni = ValueAnimator.ofFloat(0, 1);
                mEnterAni.setDuration(ANI_DEFAULT_DURING);
                mEnterAni.setInterpolator(new AccelerateDecelerateInterpolator());
                mEnterAni.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        DashBoardView view = (DashBoardView) mRefView.get();
                        if (view == null)
                            return;

                        float value = (float) animation.getAnimatedValue();
                        if (value < ANI_START_OFFSET_HALF) {
                            view.setSweepAngle(ANGLE_SWEEP * value / ANI_START_OFFSET_HALF);
                        } else if (value < ANI_START_OFFSET) {
                            view.setSweepAngle(ANGLE_SWEEP * (1 - (value - ANI_START_OFFSET_HALF) / ANI_START_OFFSET_HALF));
                        } else {
                            view.setSweepAngle(angle * (value - ANI_START_OFFSET) / (1 - ANI_START_OFFSET));
                        }
                    }
                });
                mEnterAni.start();
            }
        });
    }

    public void setSweepAngle(float angle) {
        if (mWidth == 0 || mHeight == 0)
            return;
        mSweepAngle = angle;

        float startFactor = (float) (ANGLE_START - ANGLE_OFFSET) / ANGLE_TOTAL;
        float endFactor = (ANGLE_START + mSweepAngle - ANGLE_OFFSET) / ANGLE_TOTAL;
        Log.e(TAG, endFactor + ".");


        SweepGradient inShader = new SweepGradient(mCenterX, mCenterY, new int[] {
                Color.TRANSPARENT, Color.TRANSPARENT, COLOR_INDEX_IN, Color.TRANSPARENT, Color.TRANSPARENT
        }, new float[]{
                0, startFactor, endFactor, endFactor, 1
        });
        SweepGradient outShader = new SweepGradient(mCenterX, mCenterY, new int[] {
                Color.TRANSPARENT, Color.TRANSPARENT, COLOR_INDEX_OUT, Color.TRANSPARENT, Color.TRANSPARENT
        }, new float[]{
                0, startFactor, endFactor, endFactor, 1
        });
        mOutCircleIndexPaint.setShader(outShader);
        mInCircleIndexPaint.setShader(inShader);

        invalidate();
    }

    public void destroy() {
        if (mEnterAni != null && mEnterAni.isRunning()) {
            mEnterAni.cancel();
            mEnterAni = null;
        }
    }
}
