package com.cornflower1991.library;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.animation.LinearInterpolator;

/**
 * @author yexiuliang
 * @page
 * @data 2016/11/23.
 */
public class CountDownProgressView extends View {

    private MyCountDownTimer mMyCountDownTimer;

    public interface OnProgressListener {

        void onProgressChanged(long progress);

        void onProgressCompleted();
    }


    //默认进度
    private static final int DEFAULT_PROGRESS = 0;
    //默认开始角度
    private static final int DEFAULT_START_ANGLE = -90;
    //默认边框宽度
    private static final float DEFAULT_STROKE_WIDTH = 3f;
    //文字大小
    private static final float DEFAULT_TEXT_SIZE = 40f;
    //默认大小
    private static final int DEFAULT_VIEW_SIZE = 100;


    private OnProgressListener mListener;

    private DisplayMetrics mDisplayMetrics;

    //毫秒
    public static final int UNIT_MILLISECOND = 1000;
    //弧度
    public static final int ARC = 360;

    //角度
    private int mProgress = DEFAULT_PROGRESS;
    //开始角度
    private int mStartAngle = DEFAULT_START_ANGLE;
    //是否显示边框
    private boolean mShowStroke = true;

    //边框宽度
    private float mStrokeWidth = DEFAULT_STROKE_WIDTH;
    //是否显示文字
    private boolean mShowText = true;
    //文字大小
    private float mTextSize = DEFAULT_TEXT_SIZE;
    //文字
    private String mText;
    //是否显示图片
    private boolean mShowImage = true;
    //图片
    private Drawable mImage;
    //图片大小
    private Rect mImageRect;
    //边框
    private Paint mStrokePaint;
    //文字
    private Paint mTextPaint;
    //进度
    private Paint mProgressPaint;
    //背景
    private Paint mBackgroundPaint;
    //内框大小
    private RectF mInnerRectF;


    private int mViewSize;

    float currentAngle;

    public CountDownProgressView(Context context) {
        this(context, null);
    }

    public CountDownProgressView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CountDownProgressView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        mDisplayMetrics = context.getResources().getDisplayMetrics();

        mStrokeWidth = mStrokeWidth * mDisplayMetrics.density;
        mTextSize = mTextSize * mDisplayMetrics.scaledDensity;

        //获取属性
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CountDownProgressView);
        final Resources res = getResources();

        mProgress = a.getInteger(R.styleable.CountDownProgressView_cdpvProgress, mProgress);
        mStartAngle = a.getInt(R.styleable.CountDownProgressView_cdpvStartAngle, mStartAngle);
        mStrokeWidth = a.getDimension(R.styleable.CountDownProgressView_cdpvStrokeWidth, mStrokeWidth);
        mTextSize = a.getDimension(R.styleable.CountDownProgressView_android_textSize, mTextSize);
        mText = a.getString(R.styleable.CountDownProgressView_android_text);

        //是否显示边框
        mShowStroke = a.getBoolean(R.styleable.CountDownProgressView_cdpvShowStroke, mShowStroke);
        //是否显示文字
        mShowText = a.getBoolean(R.styleable.CountDownProgressView_cdpvShowText, mShowText);
        //是否显示图片
        mImage = a.getDrawable(R.styleable.CountDownProgressView_cdpvImage);

        int backgroundColor = res.getColor(R.color.default_background_color);
        backgroundColor = a.getColor(R.styleable.CountDownProgressView_cdpvBackgroundColor, backgroundColor);
        int progressColor = res.getColor(R.color.default_progress_color);
        progressColor = a.getColor(R.styleable.CountDownProgressView_cdpvProgressColor, progressColor);
        int strokeColor = res.getColor(R.color.default_stroke_color);
        strokeColor = a.getColor(R.styleable.CountDownProgressView_cdpvStrokeColor, strokeColor);
        int textColor = res.getColor(R.color.default_text_color);
        textColor = a.getColor(R.styleable.CountDownProgressView_android_textColor, textColor);
        a.recycle();

        //背景
        mBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBackgroundPaint.setColor(backgroundColor);
        mBackgroundPaint.setStyle(Paint.Style.FILL);

        //进度
        mProgressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mProgressPaint.setColor(progressColor);
        mProgressPaint.setStyle(Paint.Style.FILL);

        //边框颜色
        mStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mStrokePaint.setColor(strokeColor);
        mStrokePaint.setStyle(Paint.Style.STROKE);
        mStrokePaint.setStrokeWidth(mStrokeWidth);
        //文字
        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(textColor);
        mTextPaint.setTextSize(mTextSize);
        mTextPaint.setTextAlign(Paint.Align.CENTER);

        mInnerRectF = new RectF();
        mImageRect = new Rect();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //默认最小大小DEFAULT_VIEW_SIZE
        int width = resolveSize(DEFAULT_VIEW_SIZE, widthMeasureSpec);
        int height = resolveSize(DEFAULT_VIEW_SIZE, heightMeasureSpec);
        mViewSize = Math.min(width, height);

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mInnerRectF.set(0, 0, mViewSize, mViewSize);
        mInnerRectF.offset((getWidth() - mViewSize) / 2, (getHeight() - mViewSize) / 2);
        if (mShowStroke) {
            final int halfBorder = (int) (mStrokePaint.getStrokeWidth() / 2f + 0.5f);
            mInnerRectF.inset(halfBorder, halfBorder);
        }
        float centerX = mInnerRectF.centerX();
        float centerY = mInnerRectF.centerY();

        /**
         *  oval：圆弧所在的椭圆对象。
         *  startAngle：圆弧的起始角度。
         *  sweepAngle：圆弧的角度。
         *  useCenter：是否显示半径连线，true表示显示圆弧与圆心的半径连线，false表示不显示。
         *  paint：绘制时所使用的画笔。
         */
        canvas.drawArc(mInnerRectF, 0, ARC, true, mBackgroundPaint);
        canvas.drawArc(mInnerRectF, mStartAngle, -currentAngle * ARC, true, mProgressPaint);


        //设置文字
        if (!TextUtils.isEmpty(mText) && mShowText) {
            int xPos = (int) centerX;
            int yPos = (int) (centerY - (mTextPaint.descent() + mTextPaint.ascent()) / 2);
            canvas.drawText(mText, xPos, yPos, mTextPaint);
        }

        //设置图片
        if (null != mImage && mShowImage) {
            int drawableSize = mImage.getIntrinsicWidth();
            mImageRect.set(0, 0, drawableSize, drawableSize);
            mImageRect.offset((getWidth() - drawableSize) / 2, (getHeight() - drawableSize) / 2);
            mImage.setBounds(mImageRect);
            mImage.draw(canvas);
        }

        //设置边框
        if (mShowStroke) {
            canvas.drawOval(mInnerRectF, mStrokePaint);
        }
    }


    /**
     * 获取文本
     */
    public String getText() {
        return mText;
    }

    /**
     * 设置文本
     */
    public void setText(String text) {
        mText = text;
        invalidate();
    }


    /**
     * 文字是否显示
     */
    public boolean isTextShowing() {
        return mShowText;
    }

    /**
     * 文字是否显示状态
     *
     * @param showText show or hide text
     */
    public void setShowText(boolean showText) {
        mShowText = showText;
        invalidate();
    }

    /**
     * 获取图片
     */
    public Drawable getImageDrawable() {
        return mImage;
    }

    /**
     * 设置图片
     *
     * @param image drawable of the view
     */
    public void setImageDrawable(Drawable image) {
        mImage = image;
        invalidate();
    }

    /**
     * 设置图片
     *
     * @param resId resource id of the view's drawable
     */
    public void setImageResource(int resId) {
        if (null != getResources()) {
            mImage = getResources().getDrawable(resId);
            invalidate();
        }
    }

    /**
     * 是否显示图片
     */
    public boolean isImageShowing() {
        return mShowImage;
    }

    /**
     * 设置是否显示图片
     *
     * @param showImage show or hide image
     */
    public void setShowImage(boolean showImage) {
        mShowImage = showImage;
        invalidate();
    }


    /**
     * 设置监听
     *
     * @param listener progress listener
     * @see OnProgressListener
     */
    public void setOnProgressListener(OnProgressListener listener) {
        mListener = listener;
    }


    public void startCountDownTime(final long countdownTime) {
        ValueAnimator animator = ValueAnimator.ofFloat(1.0f, 0f);
        //动画时长，让进度条在CountDown时间内正好从360-0走完，这里由于用的是CountDownTimer定时器，倒计时要想减到0则总时长需要多加1000毫秒，所以这里时间也跟着+1000ms
        animator.setDuration(countdownTime);
        animator.setInterpolator(new LinearInterpolator());//匀速
        animator.setRepeatCount(0);//表示不循环，-1表示无限循环
        //值从0-1.0F 的动画，动画时长为countdownTime，ValueAnimator没有跟任何的控件相关联，那也正好说明ValueAnimator只是对值做动画运算，而不是针对控件的，我们需要监听ValueAnimator的动画过程来自己对控件做操作
        //添加监听器,监听动画过程中值的实时变化(animation.getAnimatedValue()得到的值就是0-1.0)
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                /**
                 * 这里我们已经知道ValueAnimator只是对值做动画运算，而不是针对控件的，因为我们设置的区间值为0-1.0f
                 * 所以animation.getAnimatedValue()得到的值也是在[0.0-1.0]区间，而我们在画进度条弧度时，设置的当前角度为360*currentAngle，
                 * 因此，当我们的区间值变为1.0的时候弧度刚好转了360度
                 */
                currentAngle = (float) animation.getAnimatedValue();
                invalidate();//实时刷新view，这样我们的进度条弧度就动起来了
            }
        });
        //开启动画
        animator.start();
        countdownMethod(countdownTime);
    }

    //倒计时的方法
    private void countdownMethod(final long countdownTime) {
        if (mMyCountDownTimer != null) {
            mMyCountDownTimer.cancel();
            mMyCountDownTimer = null;
        }
        mMyCountDownTimer = new MyCountDownTimer(countdownTime, UNIT_MILLISECOND);
        mMyCountDownTimer.start();
    }

    class MyCountDownTimer extends CountDownTimer {

        public MyCountDownTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onFinish() {
            if (mListener != null) {
                mListener.onProgressCompleted();
            }
            cancel();

        }

        @Override
        public void onTick(long millisUntilFinished) {
            if (mListener != null) {
                mListener.onProgressChanged(millisUntilFinished);
            }
            mText = "倒计时:" + millisUntilFinished / UNIT_MILLISECOND + "";
            invalidate();
        }
    }


}
