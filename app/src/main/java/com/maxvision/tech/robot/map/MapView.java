package com.maxvision.tech.robot.map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewTreeObserver;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.viewpager.widget.ViewPager;
import com.maxvision.tech.robot.AppHolder;

/**
 * name: wy
 * date: 2021/3/24
 * desc: 地图
 */
public class MapView extends AppCompatImageView implements ViewTreeObserver.OnGlobalLayoutListener,
        ScaleGestureDetector.OnScaleGestureListener, View.OnTouchListener{
    //是否第一次进入
    private boolean mOnce;
    //初始化时缩放的值
    private float mInitScale;
    //放大的最大值
    private float mMaxScale;
    //图片缩放矩阵
    private final Matrix mScaleMatrix;

    //捕获用户多指触控时缩放的比例
    private final ScaleGestureDetector mScaleGestureDetector;

    //记录上一次多点触控的数量
    private int mLastPointerCount;

    //记录最后一直触摸的坐标
    private float mLastX,mLastY;
    //最小触摸距离
    private final int mTouchSlop;
    //判断是否可以拖拽布局界限
    private boolean isCanDrag;
    //检测左右是否超过边界
    private boolean isCheckLeftAndRight;
    //检测上下是否超过边界
    private boolean isCheckTopAndBottom;

    public MapView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mScaleMatrix = new Matrix();
        setScaleType(ScaleType.MATRIX);
        setOnTouchListener(this);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mScaleGestureDetector = new ScaleGestureDetector(context, this);
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);
    }

    /**
     * 获取ImageView加载完成的图片
     */
    @Override
    public void onGlobalLayout() {
        if (!mOnce) {
            // 得到控件的宽和高
            int width = getWidth();
            int height = getHeight();

            // 得到我们的图片，以及宽和高
            Drawable drawable = getDrawable();
            if (drawable == null) {
                return;
            }
            int dh = drawable.getIntrinsicHeight();
            int dw = drawable.getIntrinsicWidth();
            //默认不缩放，
            float scale = 1.0f;

            // 图片的宽度大于控件的宽度，图片的高度小于空间的高度，我们将其缩小
            if (dw > width && dh < height) {
                scale = width * 1.0f / dw;
            }

            // 图片的宽度小于控件的宽度，图片的高度大于空间的高度，我们将其缩小
            if (dh > height && dw < width) {
                scale = height * 1.0f / dh;
            }

            // 缩小值
            if (dw > width && dh > height) {
                scale = Math.min(width * 1.0f / dw, height * 1.0f / dh);
            }

            // 放大值
            if (dw < width && dh < height) {
                scale = Math.min(width * 1.0f / dw, height * 1.0f / dh);
            }

            /*
             * 得到了初始化时缩放的比例
             */
            mInitScale = scale;
            mMaxScale = mInitScale * 8;

            // 将图片移动至控件的中间
            int dx = getWidth() / 2 - dw / 2;
            int dy = getHeight() / 2 - dh / 2;

            mScaleMatrix.postTranslate(dx, dy);
            mScaleMatrix.postScale(mInitScale, mInitScale, width / 2, height / 2);
            setImageMatrix(mScaleMatrix);

            mOnce = true;
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        getViewTreeObserver().addOnGlobalLayoutListener(this);
    }

    /**
     * 取消OnGlobalLayoutListener这个接口
     */
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        getViewTreeObserver().removeOnGlobalLayoutListener(this);
    }

    /**
     * 获取当前图片的缩放值
     */
    protected float getScale() {
        float[] values = new float[9];
        mScaleMatrix.getValues(values);
        return values[Matrix.MSCALE_X] * getDpi();
    }

    private float getDpi(){
        //真实的DPI
        float realDPI = getResources().getConfiguration().densityDpi;
        //改变后的DPI
        float changeDPI = AppHolder.getInstance().getResources().getDisplayMetrics().densityDpi;
        Log.e("wangyin_缩放","：真实的DPI="+realDPI +"     改变后的DPI="+changeDPI);
        //TODO 伪代码，后期想办法解决,先强行适配M3和M5的平板
        if((realDPI == 360 && changeDPI == 320) || (realDPI == 360 && changeDPI == 306)){
            return 1;
        } else{
            return changeDPI / realDPI;
        }
    }

    // 缩放区间时initScale maxScale
    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        Log.e("导航-缩放","开始回调缩放");
        float scale = getScale();
        float scaleFactor = detector.getScaleFactor();
        if (getDrawable() == null) {
            return true;
        }

        // 缩放范围的控制
        if ((scale < mMaxScale && scaleFactor > 1.0f)
                || (scale > mInitScale && scaleFactor < 1.0f)) {
            if (scale * scaleFactor < mInitScale) {
                scaleFactor = mInitScale / scale;
            }

            // 缩放
            mScaleMatrix.postScale(scaleFactor, scaleFactor,
                    detector.getFocusX(), detector.getFocusY());
            checkBorderAndCenterWhenScale();
            setImageMatrix(mScaleMatrix);
        }

        return true;
    }

    /**
     * 获得图片放大缩小以后的宽和高，以及left，right，top，bottom
     */
    protected RectF getMatrixRectF() {
        RectF rectF = new RectF();
        Drawable d = getDrawable();
        Log.e("导航","地图原始宽 " + d.getIntrinsicWidth() + "  地图原始高 = " + d.getIntrinsicHeight());
        if (d != null) {
            rectF.set(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
            mScaleMatrix.mapRect(rectF);
        }
        return rectF;
    }

    /**
     * 在缩放的时候进行边界以及我们的位置的控制
     */
    private void checkBorderAndCenterWhenScale() {
        RectF rectF = getMatrixRectF();
        float deltaX = 0;
        float deltaY = 0;

        int width = getWidth();
        int height = getHeight();

        // 缩放时进行边界检测，防止出现白边
        if (rectF.width() >= width) {
            if (rectF.left > 0) {
                deltaX = -rectF.left;
            }
            if (rectF.right < width) {
                deltaX = width - rectF.right;
            }
        }

        if (rectF.height() >= height) {
            if (rectF.top > 0) {
                deltaY = -rectF.top;
            }
            if (rectF.bottom < height) {
                deltaY = height - rectF.bottom;
            }
        }

        /*
         * 如果宽度或高度小于空间的宽或者高，则让其居中
         */
        if (rectF.width() < width) {
            deltaX = width / 2f - rectF.right + rectF.width() / 2f;
        }

        if (rectF.height() < height) {
            deltaY = height / 2f - rectF.bottom + rectF.height() / 2f;
        }

        mScaleMatrix.postTranslate(deltaX, deltaY);
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        mScaleGestureDetector.onTouchEvent(event);

        float x = 0;
        float y = 0;
        // 拿到多点触控的数量
        int pointerCount = event.getPointerCount();
        for (int i = 0; i < pointerCount; i++) {
            x += event.getX(i);
            y += event.getY(i);
        }

        x /= pointerCount;
        y /= pointerCount;

        if (mLastPointerCount != pointerCount) {
            isCanDrag = false;
            mLastX = x;
            mLastY = y;
        }
        mLastPointerCount = pointerCount;
        RectF rectF = getMatrixRectF();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //Drawable drawable = getDrawable();
                //Log.i("wangyin2", "按下  X2=" + event.getX() + "    Y2=" + event.getY() +"   宽:"+drawable.getIntrinsicWidth() + "  高:"+drawable.getIntrinsicHeight());
                if (rectF.width()>getWidth() +0.01|| rectF.height()>getHeight()+0.01) {
                    if(getParent() instanceof ViewPager)
                        getParent().requestDisallowInterceptTouchEvent(true);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (rectF.width()>getWidth()+0.01 || rectF.height()>getHeight()+0.01) {
                    if(getParent() instanceof ViewPager)
                        getParent().requestDisallowInterceptTouchEvent(true);
                }
                float dx = x - mLastX;
                float dy = y - mLastY;

                if (!isCanDrag) {
                    isCanDrag = isMoveAction(dx, dy);
                }

                if (isCanDrag) {
                    if (getDrawable() != null) {
                        isCheckLeftAndRight = isCheckTopAndBottom = true;
                        // 如果宽度小于控件宽度，不允许横向移动
                        if (rectF.width() < getWidth()) {
                            isCheckLeftAndRight = false;
                            dx = 0;
                        }
                        // 如果高度小于控件高度，不允许纵向移动
                        if (rectF.height() < getHeight()) {
                            isCheckTopAndBottom = false;
                            dy = 0;
                        }
                        mScaleMatrix.postTranslate(dx, dy);

                        checkBorderWhenTranslate();

                        setImageMatrix(mScaleMatrix);
                    }
                }
                mLastX = x;
                mLastY = y;
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mLastPointerCount = 0;
                break;

            default:
                break;
        }

        return true;
    }

    /**
     * 当移动时进行边界检查
     */
    private void checkBorderWhenTranslate() {
        RectF rectF = getMatrixRectF();
        float deltaX = 0;
        float deltaY = 0;

        int width = getWidth();
        int heigth = getHeight();

        if (rectF.top > 0 && isCheckTopAndBottom) {
            deltaY = -rectF.top;
        }
        if (rectF.bottom < heigth && isCheckTopAndBottom) {
            deltaY = heigth - rectF.bottom;
        }
        if (rectF.left > 0 && isCheckLeftAndRight) {
            deltaX = -rectF.left;
        }
        if (rectF.right < width && isCheckLeftAndRight) {
            deltaX = width - rectF.right;
        }
        mScaleMatrix.postTranslate(deltaX, deltaY);

    }

    /**
     * 判断是否是move
     */
    protected boolean isMoveAction(float dx, float dy) {
        return Math.sqrt(dx * dx + dy * dy) > mTouchSlop;
    }

    /**
     * 放大
     */
    public void big(){
        float scale = getScale();
        if(scale >= mMaxScale) return;
        mScaleMatrix.postScale(1.2f,1.2f);
        checkBorderAndCenterWhenScale();
        setImageMatrix(mScaleMatrix);
    }

    /**
     * 缩小
     */
    public void small(){
        float scale = getScale();
        if(scale <= mInitScale) return;
        // 缩小
        mScaleMatrix.postScale(0.8f,0.8f);
        checkBorderAndCenterWhenScale();
        setImageMatrix(mScaleMatrix);
    }
}