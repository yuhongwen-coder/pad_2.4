package com.maxvision.tech.robot.ui.view;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.maxvision.tech.robot.R;


/**
 * name: wy
 * date: 2020/8/25
 * desc:
 */
public class BatteryImageView extends androidx.appcompat.widget.AppCompatImageView {

    /**
     * 是否正在充电
     */
    private boolean isBatAnimate = false;

    /**
     * 当前电量，当前位置，临时进度位置
     */
    private int curBat = 0,curIndex = 0,index = 0;

    private Handler handler = new Handler();

    private int[] bat = new int[]{
            R.mipmap.ic_battery_one_charging,
            R.mipmap.ic_battery_two_charging,
            R.mipmap.ic_battery_three,
            R.mipmap.ic_battery_four,
            R.mipmap.ic_battery_five,
    };


    public BatteryImageView(@NonNull Context context) {
        this(context, null);
    }

    public BatteryImageView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BatteryImageView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    public void setCurBatteryImage(int curBattery) {
        this.curBat = curBattery;
        if (isBatAnimate) return;
        if (curBattery >= 0 && curBattery <= 20) {
            setImageResource(R.mipmap.ic_battery_one);
        } else if (curBattery > 20 && curBattery <= 40) {
            setImageResource(R.mipmap.ic_battery_two);
        } else if (curBattery > 40 && curBattery <= 60) {
            setImageResource(R.mipmap.ic_battery_three);
        } else if (curBattery > 60 && curBattery <= 80) {
            setImageResource(R.mipmap.ic_battery_four);
        } else if (curBattery > 80 && curBattery <= 100) {
            setImageResource(R.mipmap.ic_battery_five);
        } else {
            setImageResource(R.mipmap.ic_battery_none);
        }
    }

    /**
     * 开始充电
     */
    public void startBat() {
        if(isBatAnimate) return;
        isBatAnimate = true;
        handler.post(batRunnable);
    }

    /**
     * 结束充电
     */
    public void closeBat() {
        isBatAnimate = false;
        handler.removeCallbacks(batRunnable);
        setCurBatteryImage(curBat);
    }

    Runnable batRunnable = new Runnable() {
        @Override
        public void run() {
            countIndex();
            index ++;
            handler.postDelayed(batRunnable, 500);
            if(index == bat.length)
                index = curIndex;
            setImageResource(bat[index]);
        }
    };

    private void countIndex() {
        if (curBat >= 0 && curBat <= 20) {
            curIndex = 0;
        } else if (curBat > 20 && curBat <= 40) {
            curIndex = 1;
        } else if (curBat > 40 && curBat <= 60) {
            curIndex = 2;
        } else if (curBat > 60 && curBat <= 80) {
            curIndex = 3;
        } else if (curBat > 80 && curBat <= 100) {
            curIndex = 4;
        }
    }

}