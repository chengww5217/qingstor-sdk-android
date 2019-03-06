package com.chengww.demo.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ProgressBar;

import com.chengww.demo.R;

/**
 * Created by chengww on 2019/3/4.<br/>
 * A custom view used for the download/upload list.
 * @see <a href="https://blog.chengww.com/archives/CircleProgressBar.html">https://blog.chengww.com/archives/CircleProgressBar.html</a>
 */
public class CircleProgressBar extends ProgressBar {
    private int mDefaultColor;
    private int mReachedColor;
    private float mDefaultHeight;
    private float mReachedHeight;
    private float mRadius;
    private Paint mPaint;

    private Status mStatus = Status.Waiting;

    public CircleProgressBar(Context context) {
        this(context, null);
    }

    public CircleProgressBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CircleProgressBar);
        mDefaultColor = typedArray.getColor(R.styleable.CircleProgressBar_defaultColor, Color.parseColor("#D8D8D8"));
        mReachedColor = typedArray.getColor(R.styleable.CircleProgressBar_reachedColor, Color.parseColor("#1296DB"));
        mDefaultHeight = typedArray.getDimension(R.styleable.CircleProgressBar_defaultHeight, dp2px(context, 2.5f));
        mReachedHeight = typedArray.getDimension(R.styleable.CircleProgressBar_reachedHeight, dp2px(context, 2.5f));
        mRadius = typedArray.getDimension(R.styleable.CircleProgressBar_radius, dp2px(context, 17));
        typedArray.recycle();

        setPaint();
    }

    private void setPaint() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
    }

    @Override
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        float paintHeight = Math.max(mReachedHeight, mDefaultHeight);

        if (heightMode != MeasureSpec.EXACTLY) {
            int exceptHeight = (int) (getPaddingTop() + getPaddingBottom() + mRadius * 2 + paintHeight);
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(exceptHeight, MeasureSpec.EXACTLY);
        }
        if (widthMode != MeasureSpec.EXACTLY) {
            int exceptWidth = (int) (getPaddingLeft() + getPaddingRight() + mRadius * 2 + paintHeight);
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(exceptWidth, MeasureSpec.EXACTLY);
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.save();
        canvas.translate(getPaddingStart(), getPaddingTop());

        int mDiameter = (int) (mRadius * 2);
        if (mStatus == Status.Loading) {
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setColor(mDefaultColor);
            mPaint.setStrokeWidth(mDefaultHeight);
            canvas.drawCircle(mRadius, mRadius, mRadius, mPaint);

            mPaint.setColor(mReachedColor);
            mPaint.setStrokeWidth(mReachedHeight);
            float sweepAngle = getProgress() * 1.0f / getMax() * 360;
            canvas.drawArc(new RectF(0, 0, mRadius * 2, mRadius * 2), -90, sweepAngle, false, mPaint);

            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeWidth(dp2px(getContext(), 2));
            mPaint.setColor(Color.parseColor("#667380"));
            canvas.drawLine(mRadius * 4 / 5, mRadius * 3 / 4, mRadius * 4 / 5, 2 * mRadius - (mRadius * 3 / 4), mPaint);
            canvas.drawLine(2 * mRadius - (mRadius * 4 / 5), mRadius * 3 / 4, 2 * mRadius - (mRadius * 4 / 5), 2 * mRadius - (mRadius * 3 / 4), mPaint);
        } else {
            int drawableInt;
            switch (mStatus) {
                case Waiting:
                default:
                    drawableInt = R.mipmap.ic_waiting;
                    break;
                case Pause:
                    drawableInt = R.mipmap.ic_pause;
                    break;
                case Finish:
                    drawableInt = R.mipmap.ic_finish;
                    break;
                case Error:
                    drawableInt = R.mipmap.ic_error;
                    break;
            }
            Drawable drawable = getContext().getResources().getDrawable(drawableInt);
            drawable.setBounds(0, 0, mDiameter, mDiameter);
            drawable.draw(canvas);
        }
        canvas.restore();
    }

    public Status getStatus() {
        return mStatus;
    }

    public void setStatus(Status status) {
        if (mStatus == status) return;
        mStatus = status;
        invalidate();
    }

    public enum Status {
        Waiting,
        Pause,
        Loading,
        Error,
        Finish
    }

    float dp2px(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return dp * scale + 0.5f;
    }
}
