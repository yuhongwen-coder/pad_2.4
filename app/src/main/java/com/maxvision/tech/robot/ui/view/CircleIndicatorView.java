package com.maxvision.tech.robot.ui.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import com.maxvision.tech.robot.R;
import java.util.concurrent.TimeUnit;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

/**
 * User: wy
 * Date: 2019/4/24
 * Time: 10:16
 * 初始化系统圆形指示器
 */
public class CircleIndicatorView extends View {

    //圆的大小
    private       int   radius         = 4;
    //圆之间的间距
    private       int   circleInterval;
    //选中圆的画笔
    private final Paint mPaintFill     = new Paint(Paint.ANTI_ALIAS_FLAG);
    //未选中圆的画笔
    private final Paint mPaintStroke   = new Paint(Paint.ANTI_ALIAS_FLAG);
    //圆点总个数
    private       int   circleCount    = 3;
    //当前页数
    private int currentPage = 0;

    private Disposable mDisposable;

    public CircleIndicatorView(Context context) {
        this(context, null);
    }

    public CircleIndicatorView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleIndicatorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CircleIndicatorView);
        int fillColor = typedArray.getColor(R.styleable.CircleIndicatorView_fillColor, 0xb3ffffff);
        int strokeColor = typedArray.getColor(R.styleable.CircleIndicatorView_strokeColor, 0x4dffffff);
        radius = (int) typedArray.getDimension(R.styleable.CircleIndicatorView_radius2, radius);
        circleInterval = (int) typedArray.getDimension(R.styleable.CircleIndicatorView_circleInterval, radius);
        initColors(fillColor, strokeColor);
        typedArray.recycle();
    }

    private void initColors(int fillColor, int strokeColor) {
        mPaintFill.setStyle(Paint.Style.FILL);
        mPaintFill.setColor(fillColor);
        mPaintStroke.setStyle(Paint.Style.FILL);
        mPaintStroke.setColor(strokeColor);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthRes, heightRes;
        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);

        if (widthSpecMode == MeasureSpec.EXACTLY) {
            widthRes = widthSpecSize;
        } else {
            widthRes = getPaddingLeft() + getPaddingRight() + (circleCount * 2 * radius) + (circleCount - 1) * circleInterval;
            if (widthMeasureSpec == MeasureSpec.AT_MOST) {
                widthRes = Math.min(widthRes, widthSpecSize);
            }
        }

        if (heightSpecMode == MeasureSpec.EXACTLY) {
            heightRes = heightSpecSize;
        } else {
            heightRes = 2 * radius + getPaddingTop() + getPaddingBottom();
            if (heightSpecMode == MeasureSpec.AT_MOST) {
                heightRes = Math.min(heightRes, heightSpecSize);
            }
        }

        setMeasuredDimension(widthRes, heightRes);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (int i = 0; i < circleCount; i++) {
            canvas.drawCircle(getPaddingLeft() + radius + (i * (2 * radius + circleInterval)), getPaddingTop() + radius, radius, mPaintStroke);
        }

        int cx;
        cx = (2 * radius + circleInterval) * currentPage;
        canvas.drawCircle(getPaddingLeft() + radius + cx, getPaddingTop() + radius, radius, mPaintFill);
    }

    private void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
        invalidate();
    }

    //开启进度条
    public void start(){
        mDisposable = Observable.interval(300, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aLong -> {
                    int index = (int) (aLong % circleCount);
                    setCurrentPage(index);
                });
    }

    //停止进度条
    public void stop(){
        if(mDisposable != null && !mDisposable.isDisposed()){
            mDisposable.dispose();
        }
    }

    public int getCircleCount() {
        return circleCount;
    }
}
