package com.maxvision.tech.robot.map;

import android.app.Service;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Vibrator;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import com.maxvision.tech.robot.R;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * name: wy
 * date: 2021/3/24
 * desc:导航
 */
public class NavigationView extends MapView {

    //定位图标大小
    private final int POS_SIZE = 0;
    //设置长按最大时间
    private final int SIZE_LONG_CLICK = 1500;

    //所有导航点数据
    private final Vector<PointEntity> navigationList = new Vector<>();
    //当前点位置数据
    private PointEntity pointEntity;

    //是否点击事件
    private boolean isClickPoint = false;

    private final Paint paint;
    //记录第一次点击数据
    private float mFirstX,mFirstY;

    //记录第一次触摸时间
    private long mFirstTime = 0;

    private OnClickNavigateListener onClickNavigateListener;

    public NavigationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();
        paint.setColor(context.getColor(R.color.black));
        paint.setTextSize(30);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(1);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //导航点
        for (int i = 0; i < navigationList.size(); i++) {
            PointEntity entity = navigationList.get(i);
            if(entity == null) continue;
            if(entity.type == 1){
                drawPoint(canvas, entity.x, entity.y, entity.name,R.drawable.poi);
            }
        }
        //实时位置
        if (pointEntity != null) {
            drawPoint(canvas, pointEntity.x, pointEntity.y, pointEntity.name,R.drawable.running);
        }
    }

    /**
     * 根据资源获取Bitmap
     * @param resId 资源ID
     * @return 图片
     */
    public Bitmap getPoiBitmap(int resId) {
        return BitmapFactory.decodeResource(getResources(), resId);
    }

    /**
     * 导航点
     * @param canvas 画板
     * @param x x坐标
     * @param y y坐标
     * @param n 点名称
     */
    private void drawPoint(Canvas canvas, double x, double y, String n,int rid) {
        RectF rectF = getMatrixRectF();
        Log.e("导航"," X轴 = " + x+ " Y轴 = " + y + " 导航点名称 = " + n +
                "  left= " + rectF.left + " top = " + rectF.top +
                  " right = " + rectF.right + " bottom = " + rectF.bottom +
                "缩放比例 = " + getScale());
        float h = rectF.bottom - rectF.top;
        //测试
        /*float l_x = rectF.left + x * getScale() - POS_SIZE / 2;
        float l_y = rectF.top + h - y * getScale() - POS_SIZE / 2;

        float r_x = rectF.left + x * getScale() + POS_SIZE / 2;
        float r_y = rectF.top + h - y * getScale() + POS_SIZE / 2;
        canvas.drawRect(l_x,l_y,r_x,r_y,paint);*/

        //图标中心位置
        float pos_2 = POS_SIZE / 2;

//        float p_x = (float) (rectF.left - pos_2 + x * getScale());
        float p_x = (float) (rectF.left + x * getScale());

//        float a = (float) (h - y * getScale());// 左下角原点坐标系
        float a = (float) (rectF.top + y * getScale()); // 左上角原点坐标系
//        float p_y = rectF.top - pos_2 + a;
        float p_y = rectF.top + a;
        Bitmap bitmap = getPoiBitmap(rid);
        Log.e("drawBitmap"," p_x = " + p_x + "  p_y = " + p_y + " 导航点 = " +n);
        canvas.drawBitmap(bitmap, p_x, p_y, null);
        if(!TextUtils.isEmpty(n)){
            Rect rect = new Rect();
            paint.getTextBounds(n, 0, n.length(), rect);
            //文字宽度
            int wTxt = rect.width();
            canvas.drawText(n , (p_x + pos_2) - wTxt / 2, p_y + POS_SIZE + pos_2, paint);
        }

    }

    /**
     * 添加单个导航点
     */
    public void addNavigation(PointEntity postion) {
        if (postion == null) return;
        navigationList.add(postion);
        postInvalidate();
    }

    /**
     * 添加所有导航点
     */
    public void addNavigationList(List<PointEntity> postion) {
        if (postion == null) return;
        navigationList.clear();
        navigationList.addAll(postion);
        postInvalidate();
    }

    /**
     * 更新当前位置坐标点
     */
    public void updatePostion(double x,double y) {
        if(pointEntity == null){
            pointEntity = new PointEntity();
        }
        pointEntity.x = x;
        pointEntity.y = y;
        postInvalidate();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        Log.e("导航-触摸", "X轴 = " + event.getX() + " Y轴 = " + event.getY() + "  缩放比例 = " + getScale());
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isClickPoint = true;
                mFirstX = event.getX();
                mFirstY = event.getY();
                mFirstTime = System.currentTimeMillis();

                /*RectF rectF = getMatrixRectF();
                float ax = (event.getX() - rectF.left) / getScale();
                float ay = (rectF.bottom - rectF.top) /getScale() - (event.getY() - rectF.top) / getScale();
                //float ay = (rectF.bottom - rectF.top - event.getY() - rectF.top) / getScale();
                Log.e("wangyin2", "触摸：X="+ax+"     Y="+ay);*/
                RectF rectF = getMatrixRectF();
                float ax = (event.getX() - rectF.left) / getScale();
                float ay = (event.getY() - rectF.top) / getScale();
                if(onClickNavigateListener != null){
                    onClickNavigateListener.onTouchXy(ax,ay);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                float dx = event.getX() - mFirstX;
                float dy = event.getY() - mFirstY;
                isClickPoint = isMoveAction(dx, dy);
                Log.e("wangyin2", "触摸："+isClickPoint);
                break;
            case MotionEvent.ACTION_UP:
                if(!isClickPoint){
                    long mLastTime = System.currentTimeMillis();
                    if(mLastTime - mFirstTime >= SIZE_LONG_CLICK){
                        //长按
                        longClick(event.getX(),event.getY());
                    }else{
                        //单机
                        inInterval(event.getX(),event.getY());
                    }
                }
                break;
        }
        return super.onTouch(v, event);
    }

    private void longClick(float x,float y){
        //第一步：首先要判断触摸区域是否在地图里面
        RectF rectF = getMatrixRectF();
        if(x > rectF.left && y > rectF.top && x < rectF.right && y < rectF.bottom){
            Log.e("wangyin2", "长按事件");
            //第二步：触摸坐标要转换为真实的地图坐标
            float ax = (x - rectF.left) / getScale();
            float ay = (rectF.bottom - rectF.top) / getScale() - (y - rectF.top) / getScale();
            if(onClickNavigateListener != null){
                onClickNavigateListener.onLongNavigate(new PointEntity(ax,ay));
            }
        }else{
            Log.e("wangyin2", "点到外面去了");
        }
    }

    private void inInterval(float x,float y){
//        Log.e("导航", "X轴 = " + x + " Y轴 = " + y);
        RectF rectF = getMatrixRectF();
        //图片真实高度
        float h = rectF.bottom - rectF.top;
        for(PointEntity point : navigationList){
            if(point.type != 1) continue;
            //导航点在地图上的左上角位置和右下角的真实点击位置
            //真实左上角位置
            //第一步：拿到图片区域位于左上角的位置
            float l_x = (float) (rectF.left + point.x * getScale() - POS_SIZE / 2);
            float l_y = (float) (rectF.top + h - point.y * getScale() - POS_SIZE / 2);

            float r_x = (float) (rectF.left + point.x * getScale() + POS_SIZE / 2);
            float r_y = (float) (rectF.top + h - point.y * getScale() + POS_SIZE / 2);


            //手指点击地图上的真实位置
            float a_x = x;
            float a_y = y;

            if(a_x > l_x && a_y > l_y && a_x < r_x && a_y < r_y){
                Log.e("wangyin2", "点击="+point.name);
                if(onClickNavigateListener != null){
                    onClickNavigateListener.onClickNavigate(point);
                }
                break;
            }
        }
    }

    /**
     * 线性马达震动
     * @param milliseconds 毫秒
     */
    private void vibrate(long milliseconds) {
        Vibrator vib = (Vibrator) getContext().getSystemService(Service.VIBRATOR_SERVICE);
        vib.vibrate(milliseconds);
    }

    public void setOnClickNavigateListener(OnClickNavigateListener onClickNavigateListener) {
        this.onClickNavigateListener = onClickNavigateListener;
    }
}