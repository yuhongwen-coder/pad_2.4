package com.maxvision.tech.robot.alarm;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

/**
 * Created by yuhongwen
 * on 2021/4/8
 */
public class CustomScrollViewPager extends ViewPager {
    private boolean defaultScrollable = false;

    public CustomScrollViewPager(@NonNull Context context) {
        super(context);
    }

    public CustomScrollViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return defaultScrollable;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return defaultScrollable;
    }
}
