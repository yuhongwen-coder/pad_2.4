package com.maxvision.tech.mqtt.entity;

/**
 * Created by yuhongwen
 * on 2021/4/14
 */
public abstract class BaseAlarmEntity {
    public int alarmType;
    public String alarmMessage;

    //人脸报警
    public static final int ALARM_FACE = 100;
    public static final String ALARM_FACE_STRING = "人脸识别";
    //区域布控
    public static final int ALARM_AREA = 101;
    public static final String ALARM_AREA_STRING = "区域布控";
    //红外测温
    public static final int ALARM_TEMP = 102;
    public static final String ALARM_TEMP_STRING = "红外测温";
    //口罩检测
    public static final int ALARM_MASK = 103;
    public static final String ALARM_MASK_STRING = "口罩检测";

    // 0 关闭 1 开启 -1 异常
    public static final int ALARM_OPEN = 1;
    public static final int ALARM_CLOSE = 0;
    public static final int ALARM_EXCEPTION = -1;


    public int getAlarmType() {
        return alarmType;
    }

    public void setAlarmType(int alarmType) {
        this.alarmType = alarmType;
    }

    public String getAlarmMessage() {
        return alarmMessage;
    }

    public void setAlarmMessage(String alarmMessage) {
        this.alarmMessage = alarmMessage;
    }
}
