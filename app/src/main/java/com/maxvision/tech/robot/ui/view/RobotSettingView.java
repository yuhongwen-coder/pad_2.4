package com.maxvision.tech.robot.ui.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.maxvision.tech.mqtt.entity.Heart;
import com.maxvision.tech.robot.R;
import com.maxvision.tech.robot.utils.DensityUtil;

/**
 * Created by yuhongwen
 * on 2021/4/16
 */
public class RobotSettingView extends ConstraintLayout {
    private final int settingBgRes;
    private final int settingTextColor;
    private final String settingTextTitleRes;
    private final String settingTextRes;
    private final int settingImageRes;
    private final boolean hasSettingTitle;
    private final ImageView settingBg;
    private final ImageView settingImage;
    private final TextView settingTitle;
    private final TextView settingText;
    private final int settingTextTitleColor;
    private final Context mContext;
    private int settingBgWidth;
    private int settingBgHeight;

    public RobotSettingView(@NonNull Context context) {
        this(context,null);
    }

    public RobotSettingView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    @SuppressLint("ResourceAsColor")
    public RobotSettingView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        @SuppressLint("CustomViewStyleable") TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.Robot_Setting, defStyleAttr, 0);
        settingTextColor = array.getColor(R.styleable.Robot_Setting_setting_text_color,R.color.black);
        settingTextTitleColor = array.getColor(R.styleable.Robot_Setting_setting_text_title_color,R.color.black);
        settingTextTitleRes = array.getString(R.styleable.Robot_Setting_setting_text_title_value);
        settingTextRes = array.getString(R.styleable.Robot_Setting_setting_text_value);
        settingImageRes = array.getResourceId(R.styleable.Robot_Setting_setting_image,R.mipmap.robot_setting_water);
        settingBgRes = array.getResourceId(R.styleable.Robot_Setting_setting_bg,R.drawable.robot_setting_common_bg);
        hasSettingTitle = array.getBoolean(R.styleable.Robot_Setting_setting_text_title_boolean,false);
        array.recycle();
        @SuppressLint("InflateParams") View view = LayoutInflater.from(context).inflate(R.layout.item_robot_setting,this,true);
        settingBg = view.findViewById(R.id.robot_setting_fun_bg);
        settingImage = view.findViewById(R.id.robot_setting_fun_image);
        settingTitle = view.findViewById(R.id.robot_setting_fun_text_title);
        settingText = view.findViewById(R.id.robot_setting_fun_text);
        configData();
    }

    private void configData() {
        settingBg.setImageResource(settingBgRes);
        settingImage.setImageResource(settingImageRes);
        settingTitle.setTextColor(settingTextTitleColor);
        settingTitle.setText(settingTextTitleRes);
        settingText.setText(settingTextRes);
        settingText.setTextColor(settingTextColor);
        if (!hasSettingTitle) {
            settingTitle.setVisibility(GONE);
        } else {
            settingTitle.setVisibility(VISIBLE);
        }
    }

    /**
     * 刷新药计量或者水量
     * @param value 药计量或者水量的值
     */
    private void refreshText(String value) {
        settingText.setText(value);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // TODO 动态设置View的宽 高 暂时没效果，先用xml配置
//        ConstraintLayout.LayoutParams params = new LayoutParams(DensityUtil.px2dip(getContext(),settingBgWidth)
//                   ,DensityUtil.px2dip(getContext(),settingBgHeight));
//        settingBg.setLayoutParams(params);
//        setMeasuredDimension(widthMeasureSpec,heightMeasureSpec);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void setHeart(int resId,Heart heart) {
        if (resId == R.id.robot_setting_fun_1) {
            // 刷新水量
            refreshText(String.format(mContext.getResources().getString(R.string.robot_percent),heart.waterValue));
        } else if (resId == R.id.robot_setting_fun_2) {
            // 刷新药量
            refreshText(String.format(mContext.getResources().getString(R.string.robot_percent),heart.drugValue));
        }
    }
}
