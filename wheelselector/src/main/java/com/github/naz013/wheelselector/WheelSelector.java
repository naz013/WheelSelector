package com.github.naz013.wheelselector;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * Copyright 2017 Nazar Suhovich
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public class WheelSelector extends View implements GestureDetector.OnGestureListener {

    private String[] mItems = new String[]{};
    private Rect[] mRects = new Rect[]{};
    private Paint mPaint;

    private Path mPointerPath;
    private Paint mPointerPaint;

    private int mMargin;
    private int mSelectedItem;
    private float mX;
    private float mY;
    private long mills;
    private int mMiddleY;
    private int mMiddleX;

    private GestureDetectorCompat mDetector;
    private OnWheelScrollListener mListener;

    public WheelSelector(Context context) {
        super(context);
        init(context, null);
    }

    public WheelSelector(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public WheelSelector(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public int getSelectedPosition() {
        return mSelectedItem;
    }

    public String getSelectedValue() {
        return mItems[mSelectedItem];
    }

    public void setListener(OnWheelScrollListener listener) {
        this.mListener = listener;
    }

    private void init(Context context, AttributeSet attrs) {
        this.mMargin = (int) getResources().getDimension(R.dimen.default_margin);

        this.mPaint = new Paint();
        this.mPaint.setStyle(Paint.Style.STROKE);
        this.mPaint.setColor(Color.RED);
        this.mPaint.setTextSize(30f);
        this.mPaint.setStrokeWidth(3f);
        this.mPaint.setTextAlign(Paint.Align.CENTER);

        this.mPointerPaint = new Paint();
        this.mPointerPaint.setStyle(Paint.Style.STROKE);
        this.mPointerPaint.setColor(Color.BLACK);
        this.mPointerPaint.setStrokeWidth(3f);
        this.mPointerPath = new Path();

        this.mItems = new String[25];
        for (int i = 0; i < 25; i++) {
            this.mItems[i] = String.valueOf(i + 1);
        }

        mDetector = new GestureDetectorCompat(context, this);
        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mDetector.onTouchEvent(event);
                return processTouch(event);
            }
        });
    }

    private boolean processTouch(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            this.mX = event.getX();
            this.mY = event.getY();
            return true;
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            moveWheel(event.getX(), event.getY());
            return true;
        }
        return false;
    }

    private void centreSelected(float x) {
        for (int i = 0; i < mRects.length; i++) {
            if (mRects[i].contains(mMiddleX, mMiddleY)) {
                mSelectedItem = i;
                Rect rect = mRects[i];
                int targetLeft = mMiddleX - (mMargin / 2);
                if (rect.left != targetLeft) {
                    int diff = rect.left - targetLeft;
                    moveWheel(x - diff, 0);
                }
                notifyChanged();
                break;
            }
        }
    }

    private void moveWithForce(float x, float y) {
        float distance = (float) Math.sqrt((x - mX) * (x - mX) + (y - mY) * (y - mY));
        float speed = distance / (System.nanoTime() - mills);
        float tmp = x;
        while (speed > 0) {
            moveWheel(tmp, 0);
            tmp -= speed;
            speed--;
        }
    }

    private void moveWheel(float x, float y) {
        float dist = mX - x;
        if (mRects[0].left - (int) dist >= mMiddleX || mRects[mRects.length - 1].right - dist <= mMiddleX) {
            this.mX = x;
            this.mY = y;
            this.mills = System.nanoTime();
            this.invalidate();
            return;
        }
        for (int i = 0; i < mRects.length; i++) {
            Rect rect = mRects[i];
            rect.left = rect.left - (int) dist;
            rect.right = rect.right - (int) dist;
            mRects[i] = rect;
            if (rect.contains(mMiddleX, mMiddleY) && mSelectedItem != i) {
                mSelectedItem = i;
                notifyScroll();
            }
        }
        this.mX = x;
        this.mY = y;
        this.mills = System.nanoTime();
        this.invalidate();
    }

    private void notifyChanged() {
        if (mListener != null) {
            mListener.onValueSelected(mSelectedItem, mItems[mSelectedItem]);
        }
    }

    private void notifyScroll() {
        if (mListener != null) {
            mListener.onWheelScrolled(mSelectedItem, mItems[mSelectedItem]);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(width, height);
        calculateRectangles();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (this.mItems.length > 0) {
            drawItems(canvas);
        }
        canvas.drawPath(mPointerPath, mPointerPaint);
    }

    private void drawItems(Canvas canvas) {
        for (int i = 0; i < mItems.length; i++) {
            canvas.drawText(mItems[i], mRects[i].centerX(), mRects[i].centerY(), mPaint);
        }
    }

    private void calculateRectangles() {
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        mMiddleX = width / 2;
        mMiddleY = height / 2;

        int selectorHeight = height / 6;
        mPointerPath.reset();
        mPointerPath.moveTo(mMiddleX, 0);
        mPointerPath.lineTo(mMiddleX, selectorHeight);
        mPointerPath.moveTo(mMiddleX, height);
        mPointerPath.lineTo(mMiddleX, height - selectorHeight);

        this.mRects = new Rect[mItems.length];
        int stX = mMiddleX - (this.mMargin / 2);
        for (int i = 0; i < mItems.length; i++) {
            int end = stX + mMargin;
            this.mRects[i] = new Rect(stX, 0, end, height);
            stX = end;
        }
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        moveWithForce(e2.getX(), e2.getY());
        centreSelected(e2.getX());
        return false;
    }

    public interface OnWheelScrollListener {
        void onValueSelected(int position, String value);

        void onWheelScrolled(int position, String value);
    }
}
