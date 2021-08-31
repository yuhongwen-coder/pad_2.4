package com.maxvision.tech.robot.ui.view;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.maxvision.tech.mqtt.entity.AlarmTypeEntity;
import com.maxvision.tech.mqtt.entity.Heart;
import com.maxvision.tech.robot.R;
import com.maxvision.tech.robot.alarm.AlarmNotifyActivity;
import com.maxvision.tech.robot.manager.RobotDataManager;
import com.maxvision.tech.robot.utils.RobotUtils;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;

import static com.maxvision.tech.mqtt.entity.BaseAlarmEntity.ALARM_AREA_STRING;
import static com.maxvision.tech.mqtt.entity.BaseAlarmEntity.ALARM_FACE_STRING;
import static com.maxvision.tech.mqtt.entity.BaseAlarmEntity.ALARM_MASK_STRING;
import static com.maxvision.tech.mqtt.entity.BaseAlarmEntity.ALARM_TEMP_STRING;

/**
 * Created by yuhongwen
 * on 2021/4/22
 */
public class AlarmPopupWindow extends PopupWindow {
    private static final int Y_OFFSET = -700;
    private final View locationView;
    private Context mContext;
    private String robotSn;
    private String robotName;
    private String alarmTypeString;
    private int mRobotTypeRes;
    private View floatView;
    private final static int ANIM_DURATION = 600;
    private final static int ANIM_CLOSE = 0;
    private ObjectAnimator animator;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NotNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == ANIM_CLOSE) {
                dismissFloatWindow();
            }
        }
    };
    private String currentType;
    public static volatile boolean isShowAlarm;

    private void dismissFloatWindow() {
        animator = null;
        floatView = null;
        isShowAlarm = false;
        dismiss();
    }

    public AlarmPopupWindow(Context context,View locationView,String message) {
        super(context);
        mContext = context;
        this.locationView = locationView;
        initView();
        configAlarmMessage(message);
    }

    private void configAlarmMessage(String onMessage) {
        JSONObject jsonObject = (JSONObject) JSON.parse(onMessage);
        if (jsonObject == null) return;
        int alarmType = (int) jsonObject.get("alarmType");
        robotSn = (String) jsonObject.get("robotSn");
        robotName = (String) jsonObject.get("robotName");
        configAlarmType(alarmType);
        configRobotType();
        setHeadFloatViewAnim();
        configAllData();
    }

    private void configAllData() {
        ImageView alarmImage = floatView.findViewById(R.id.header_float_image);
        alarmImage.setImageResource(mRobotTypeRes);
        TextView alarmTitle = floatView.findViewById(R.id.header_float_title);
        alarmTitle.setText(TextUtils.isEmpty(robotName) ? robotSn : robotName);
        TextView alarmContent = floatView.findViewById(R.id.header_float_name);
        alarmContent.setText(alarmTypeString);
    }

    private void setHeadFloatViewAnim() {
        if (animator == null) {
            animator = ObjectAnimator.ofFloat(floatView, "translationY", Y_OFFSET, 0);
            animator.setDuration(ANIM_DURATION);
        }
    }

    private void configRobotType() {
        Heart heart = RobotDataManager.getInstance().get(robotSn);
        mRobotTypeRes = RobotUtils.getRobotType(heart.type);
    }

    private void configAlarmType(int alarmType) {
        if (alarmType == AlarmTypeEntity.ALARM_AREA) {
            alarmTypeString = mContext.getString(R.string.alarm_area_tip);
            currentType = ALARM_AREA_STRING;
        } else if (alarmType == AlarmTypeEntity.ALARM_FACE) {
            alarmTypeString = mContext.getString(R.string.alarm_face_tip);
            currentType = ALARM_FACE_STRING;
        } else if (alarmType == AlarmTypeEntity.ALARM_TEMP) {
            alarmTypeString = mContext.getString(R.string.alarm_temp_tip);
            currentType = ALARM_TEMP_STRING;
        } else if (alarmType == AlarmTypeEntity.ALARM_MASK) {
            alarmTypeString = mContext.getString(R.string.alarm_mask_tip);
            currentType = ALARM_MASK_STRING;
        }
    }

    @SuppressLint({"InflateParams", "UseCompatLoadingForDrawables"})
    private void initView() {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (floatView == null) {
            floatView = inflater.inflate(R.layout.alarm_float_view, null);
        }
        this.setContentView(floatView);
        this.setWidth(getScreenWidthPix(mContext)/3);
        this.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        this.setFocusable(false);
        this.setTouchable(true);
        this.setOutsideTouchable(false);
        // 设置透明防止动画出现黑影
        this.setBackgroundDrawable(mContext.getDrawable(R.drawable.alarm_notify_window_transparent));
        floatView.setOnClickListener(v -> AlarmNotifyActivity.startActivity(mContext, currentType,robotSn));
        // 覆盖状态栏
        try {
            Field mLayoutInScreen = PopupWindow.class.getDeclaredField("mLayoutInScreen");
            mLayoutInScreen.setAccessible(true);
            mLayoutInScreen.set(this, true);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public void showAsPopupWindow() {
        animator.start();
        showAtLocation(locationView, Gravity.TOP| Gravity.CENTER_HORIZONTAL, 0, -1000);
        isShowAlarm = true;
        mHandler.sendEmptyMessageDelayed(ANIM_CLOSE, 2000);
    }

    /**
     * 获取屏幕的宽
     *
     * @param context 当前上下文
     * @return 屏幕宽
     */
    private static int getScreenWidthPix(Context context) {
        if (context == null) {
            return 0;
        }
        return context.getResources().getDisplayMetrics().widthPixels;
    }
}
