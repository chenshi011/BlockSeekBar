package com.cs.seekbar;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Region;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

import static android.R.attr.right;
import static android.R.attr.x;

/**
 * Created by chenshi on 2017/5/19.

 */

public class BlockSeekBar extends View {

    private int mBlockColor = Color.BLACK;
    private float mBlockWidth = 20;
    private float mBlockHeight = 45;
    private float mBlockSpace = 8;
    private float mTextSize = 20;
    private float mTextSpace = 5;
    private float mBlockLineWidth = 2; // 空心线宽
    private float mBlockRadius = 5;
    private boolean mDrawText;
    int mBlockCount;
    private Paint mPaint;

    // Block最小宽高
    private final static int BLOCK_MIN_WIDTH = 12;
    private final static int BLOCK_MIN_HEIGHT = 25;

    private int _progress = 0;
    private int _curblock = 0;
    private int _max = 100;
    List<Path> mInsidePaths,mOutsidePaths;
    private boolean mUseCustomBlock;
    private int mOrientation;

    public int getProgress() {
        return _progress;
    }

    public void setProgress(int progress) {
        setProgress(progress, true);
    }

    public void setProgress(int progress, boolean listener) {
        if (getWidth() != 0) { // 初始化
            int curblock = getCurblockByProgress(progress);
            setCurblock(curblock, listener);
        } else {
            _progress = progress;
        }
    }

    public int getCurBlock() {
        return _curblock;
    }

    public void setCurBlock(int block) {
        if (_curblock != block) {
            this._curblock = block;
            invalidate();
        }
    }

    public int getTotalBlock() {
        return mBlockCount;
    }

    public void setTotalBlock(int block) {
        this.mUseCustomBlock = true;
        this.mBlockCount = block;
    }

    public void setMax(int max) {
        this._max = max;
    }

    /**
     * @param block
     *            滑块个数
     */
    public void updateValue(int block) {
        int curblock = _curblock + block;
        if (curblock > mBlockCount) {
            curblock = mBlockCount;
        } else if (curblock < 0) {
            curblock = 0;
        }
        setCurblock(curblock, true);
    }

    int getCurblockByx(float x) {
        if (x > 0) {
            int block = Math.round((x + mBlockSpace) / (mBlockWidth + mBlockSpace));
            block = block > mBlockCount ? mBlockCount : block;
            return block;
        } else {
            return 0;
        }
    }

    int getCurblockByProgress(int progress) {
        float offset = (1.0f * progress / _max)
                * (getRwidth());
        return getCurblockByx(offset);
    }

    void setCurblock(int curblock, boolean listener) {
        if (curblock >= 0 && curblock <= mBlockCount
                && this._curblock != curblock) {
            this._curblock = curblock;
            _progress = getRealProgress(_curblock);
            postInvalidate();
        }
    }

    int getRealProgress(int block) {
        if (block == mBlockCount)
            return _max;
        else if (block == 0) {
            return 0;
        } else {
            return Math.round(1.0f * _max / mBlockCount * block);
        }
    }

    public BlockSeekBar(Context context) {
        this(context, null, R.style.BlockSeekBar_Default);
    }

    public BlockSeekBar(Context context, AttributeSet attrs) {
        this(context, attrs, R.style.BlockSeekBar_Default);
    }

    public BlockSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.BlockSeekBar, defStyleAttr,
                R.style.BlockSeekBar_Default);
        mBlockColor = a.getColor(R.styleable.BlockSeekBar_block_color,
                Color.BLACK);
        mBlockWidth = a.getDimension(R.styleable.BlockSeekBar_block_width,
                BLOCK_MIN_WIDTH);
        mBlockWidth = Math.max(mBlockWidth, BLOCK_MIN_WIDTH);
        mBlockHeight = a.getDimension(R.styleable.BlockSeekBar_block_height,
                BLOCK_MIN_HEIGHT);
        mBlockHeight = Math.max(mBlockHeight, BLOCK_MIN_HEIGHT);
        mTextSize = a.getDimension(R.styleable.BlockSeekBar_block_text_size,
                25);
        mTextSpace = a.getDimension(R.styleable.BlockSeekBar_block_text_space,
                5);
        mBlockLineWidth = a
                .getDimension(R.styleable.BlockSeekBar_block_line_width, 2);
        mBlockSpace = a.getDimension(R.styleable.BlockSeekBar_block_space, 8);
        mBlockRadius = a.getDimension(R.styleable.BlockSeekBar_block_radius, 5);
        mDrawText = a.getBoolean(R.styleable.BlockSeekBar_block_drawtext, true);
        _progress = a.getInt(R.styleable.BlockSeekBar_block_progress, 0);
        _max = a.getInt(R.styleable.BlockSeekBar_block_max, 0);
        mOrientation = a.getInt(R.styleable.BlockSeekBar_android_orientation, LinearLayout.HORIZONTAL);
        a.recycle();
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(mBlockColor);

    }

    public void setOrientation(int orientation) {
        if (mOrientation != orientation) {
            mOrientation = orientation;
            requestLayout();
        }
    }
    public int getOrientation() {
        return mOrientation;
    }
    OnBlockSeekBarChangedListener _onBlockSeekBarChangedListener;

    public void setOnBlockSeekBarChangedListener(
            OnBlockSeekBarChangedListener onBlockSeekBarChangedListener) {
        this._onBlockSeekBarChangedListener = onBlockSeekBarChangedListener;
    }

    public interface OnBlockSeekBarChangedListener {
        /**
         * @param progress
         */
        void onProgressChanged(BlockSeekBar blockSeekBar, int progress);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        float offset = event.getX() - getPaddingLeft();
        if (mOrientation == LinearLayout.VERTICAL) {
            offset = event.getY() - getPaddingTop();
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                setCurrentValueByLocation(offset);
                return true;
            case MotionEvent.ACTION_UP:
                onValueDone();
                break;
        }
        return super.dispatchTouchEvent(event);
    }
    private void setCurrentValueByLocation(float offset) {
        int block = getCurblockByx(offset);
        if (block != _curblock) {
            setCurblock(block, true);
        }
    }

    private void onValueDone() {
        if (_onBlockSeekBarChangedListener != null)
            _onBlockSeekBarChangedListener
                    .onProgressChanged(BlockSeekBar.this, _progress);
    }

    private boolean mInit = false;


    @TargetApi(Build.VERSION_CODES.FROYO)
    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mInit = false;
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        if (!mInit) {
            initBlock(canvas);
        }
        for (int i = 0; i < mBlockCount; i++) {
            if (mDrawText && i == _curblock - 1) {
                drawText(canvas, i);
            }
            if (i >= _curblock) {
                canvas.clipPath(mInsidePaths.get(i), Region.Op.DIFFERENCE);
            }
            canvas.drawPath(mOutsidePaths.get(i), mPaint);
        }
    }

    private void drawText(Canvas canvas,int block) {
        float left = 0;
        float offSetY = 0;
        if (mOrientation == LinearLayout.VERTICAL) {
            left = (getWidth() - getPaddingLeft() - getPaddingRight()) / 2  + mBlockHeight;
            offSetY = getPaddingTop() + (mBlockWidth + mBlockSpace) * (block + 1);
        }else{
            left = getPaddingLeft() + (mBlockWidth + mBlockSpace) * block;
            offSetY = (getHeight() - getPaddingTop() - getPaddingBottom() - mBlockHeight) / 2;
        }
        mPaint.setTextSize(mTextSize);
        String text = String.valueOf(_curblock);
        float textWidth = mPaint.measureText(text);
        if (mOrientation == LinearLayout.VERTICAL) {
            canvas.drawText(text, left - mTextSpace, offSetY + mBlockWidth / 2 - mTextSize / 2, mPaint);
        }else {
            canvas.drawText(text, left + mBlockWidth / 2 - textWidth / 2, offSetY - mTextSpace, mPaint);
        }

    }

    private int getRwidth(){
        if (mOrientation == LinearLayout.VERTICAL) {
            return getHeight() - getPaddingTop() - getPaddingBottom();
        }else {
            return getWidth() - getPaddingLeft() - getPaddingRight();
        }
    }

    private int getRheight(){
        if (mOrientation == LinearLayout.VERTICAL) {
            return getWidth() - getPaddingLeft() - getPaddingRight();
        }else {
            return getHeight() - getPaddingTop() - getPaddingBottom();
        }
    }
    private void initBlock(Canvas canvas) {
        int width = getRwidth();
        // ---两次计算校准适配屏幕的BlockCount和BlockSpace------
        if (!mUseCustomBlock) {
            mBlockCount = (int) ((width + mBlockSpace)
                    / (mBlockWidth + mBlockSpace));
            mBlockSpace = (width - mBlockWidth * mBlockCount) / (mBlockCount - 1);
            mBlockCount = (int) ((width + mBlockSpace)
                    / (mBlockWidth + mBlockSpace));
        }
        mBlockSpace = (width - mBlockWidth * mBlockCount) / (mBlockCount - 1);
        // -----end-----------------
        if (_progress > 0) {
            _curblock = getCurblockByProgress(_progress);
            if (_curblock > mBlockCount) {
                _curblock = mBlockCount;
            }
        }
        else if(_curblock > 0){
            _progress = getRealProgress(_curblock);
            if (_progress > _max) {
                _progress = _max;
            }
        }
        //block居中
        float half = (getRheight() - mBlockHeight) / 2;
        float height = mBlockHeight;
        mInsidePaths = new ArrayList<Path>();
        mOutsidePaths = new ArrayList<Path>();
        float left,top, right, bottom;
        for (int i = 0; i < mBlockCount; i++) {
            left = mOrientation == LinearLayout.VERTICAL ? half + getPaddingLeft() : getPaddingLeft() + (mBlockWidth + mBlockSpace) * i;
            right = mOrientation == LinearLayout.VERTICAL ? left + mBlockHeight : left + mBlockWidth;
            top = mOrientation == LinearLayout.VERTICAL ? getPaddingTop() + (mBlockWidth + mBlockSpace) * i : half;
            bottom =  mOrientation == LinearLayout.VERTICAL ? top + mBlockWidth : half + height;
            Path outsidePath = new Path();
            RectF outsideRect = new RectF(left, top, right, bottom);
            outsidePath.addRoundRect(outsideRect, mBlockRadius, mBlockRadius,
                    Path.Direction.CW);
            Path insidePath = new Path();
            RectF insidedeRect = null;
            if (mOrientation == LinearLayout.VERTICAL) {
                insidedeRect = new RectF(left + mBlockLineWidth,
                        top + mBlockLineWidth, right - mBlockLineWidth,
                        bottom - mBlockLineWidth);
            }else{
                insidedeRect = new RectF(left + mBlockLineWidth,
                        top + mBlockLineWidth, right - mBlockLineWidth,
                        bottom - mBlockLineWidth);
            }
            insidePath.addRoundRect(insidedeRect, mBlockRadius,
                    mBlockRadius, Path.Direction.CW);
            mInsidePaths.add(insidePath);
            mOutsidePaths.add(outsidePath);
        }
        mInit = true;
    }
}
