package com.maxvision.tech.robot.alarm;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.widget.TextView;

import com.maxvision.tech.robot.R;
import com.maxvision.tech.robot.utils.DensityUtil;

import q.rorbin.verticaltablayout.VerticalTabLayout;
import q.rorbin.verticaltablayout.widget.TabView;

/**
 * Created by yuhongwen
 * on 2021/4/8
 * 自定义垂直的导航选择器
 */
public class CustomVerticalTab extends VerticalTabLayout {
    private final Context mContext;

    public CustomVerticalTab(Context context) {
        this(context, null);
    }

    public CustomVerticalTab(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomVerticalTab(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        initListener();
        setNormalTextView();
    }

    private void initListener() {
        addOnTabSelectedListener(new VerticalTabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabView tab, int position) {
                for (int i = 0; i < getTabCount(); i++) {
                    TextView titleView;
                    if (i != position) {
                        titleView = getTabAt(i).getTitleView();
                        if (titleView == null) continue;
                        titleView.setTextColor(mContext.getColor(R.color.white));
                    } else {
                        titleView = getTabAt(position).getTitleView();
                        if (titleView == null) continue;
                        titleView.setTextColor(mContext.getColor(R.color.title_text));
                    }
                }
            }

            @Override
            public void onTabReselected(TabView tab, int position) {
            }
        });
    }

    /**
     * 将改变颜色的消息排在框架绘制控件的后面
     */
    public void setNormalTextView() {
        new Handler(Looper.getMainLooper()).post(() -> {
            for (int i = 0; i < getTabCount(); i++) {
                TextView titleView = getTabAt(i).getTitleView();
                if (titleView == null) continue;
                if (i == 0) {
                    titleView.setTextColor(mContext.getColor(R.color.title_text));
                } else {
                    titleView.setTextColor(mContext.getColor(R.color.white));
                }
                titleView.setTextSize(DensityUtil.sp2px(mContext,10));
            }
        });
    }
}
