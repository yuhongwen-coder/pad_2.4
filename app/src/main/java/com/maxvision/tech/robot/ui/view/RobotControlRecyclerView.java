package com.maxvision.tech.robot.ui.view;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by yuhongwen
 * on 2021/5/8
 * 定制是否可以水平绘制垂直滚动等其他功能列表
 */
public class RobotControlRecyclerView extends RecyclerView {
    public RobotControlRecyclerView(@NonNull Context context) {
        super(context);
    }

    public RobotControlRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public RobotControlRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
}
