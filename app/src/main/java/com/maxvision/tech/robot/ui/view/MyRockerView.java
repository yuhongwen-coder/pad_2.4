package com.maxvision.tech.robot.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import com.maxvision.tech.robot.AppHolder;
import java.util.concurrent.TimeUnit;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * name: wy
 * date: 2021/4/26
 * desc:
 */
public class MyRockerView extends RockerView{

    private double y = 0;
    private double x = 0;
    private int mLevelXy;
    private Disposable subscribe;

    private String sn;

    public MyRockerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        initRockerClick();
    }

    private void initRockerClick() {
        //xy轴
        //方向
        setOnShakeListener(RockerView.DirectionMode.DIRECTION_8, new RockerView.OnShakeListener() {
            @Override
            public void onStart() {}

            @Override
            public void direction(RockerView.Direction direction) {
                if (direction == RockerView.Direction.DIRECTION_CENTER) {
                    //当前方向：中心
                    x = 0;
                    y = 0;
                }
            }

            @Override
            public void onFinish() {
                x = 0;
                y = 0;
                //延时关闭
                int xs = (int) (-x) + 100;
                int ys = (int) y + 100;
                AppHolder.getInstance().getMqtt().getControl(sn,0,(ys<<16|xs));
                if (!subscribe.isDisposed()) {
                    subscribe.dispose();
                }
            }
        });
        //角度
        setOnAngleChangeListener(new RockerView.OnAngleChangeListener() {
            @Override
            public void onStart() {
                startOfExercise();
            }

            @Override
            public void angle(double angle) {
                double radians = Math.toRadians(angle);
                double sin = Math.sin(radians);

                double radians1 = Math.toRadians(angle);
                double cos = Math.cos(radians1);

                double i = mLevelXy * 10;
                y = -(sin * i);
                x = cos * i;
            }

            @Override
            public void onFinish() { }
        });
        //级别
        setOnDistanceLevelListener(level -> mLevelXy = level);
    }
    /**
     * 摇杆运动控制
     */
    private void startOfExercise() {
        subscribe = Observable.interval(0, 100, TimeUnit.MILLISECONDS).doOnNext(aLong -> {
            int xs = (int) (-x) + 100;
            int ys = (int) y + 100;
            AppHolder.getInstance().getMqtt().getControl(sn,0,(ys<<16|xs));
        }).subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(aLong -> {});
    }
}