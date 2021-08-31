package com.maxvision.tech.mqtt.entity;

/**
 * Created by yuhongwen
 * on 2021/4/14
 */
public class AlarmSwitchEntity extends BaseAlarmEntity{

    private int isOpenSwitch;
    private int typeImage;
    private String padSn;
    public int errorCode;

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public AlarmSwitchEntity(int alarmType, int isOpenSwitch, String alarmMessage, int typeImage) {
        this.alarmType = alarmType;
        this.isOpenSwitch = isOpenSwitch;
        this.alarmMessage = alarmMessage;
        this.typeImage = typeImage;
    }

    public int getTypeImage() {
        return typeImage;
    }

    public void setTypeImage(int typeImage) {
        this.typeImage = typeImage;
    }

    public int getIsOpenSwitch() {
        return isOpenSwitch;
    }

    public void setIsOpenSwitch(int isOpenSwitch) {
        this.isOpenSwitch = isOpenSwitch;
    }

    public String getPadSn() {
        return padSn;
    }

    public void setPadSn(String padSn) {
        this.padSn = padSn;
    }

    @Override
    public String toString() {
        return "AlarmSwitchEntity{" +
                "isOpenSwitch=" + isOpenSwitch +
                ", typeImage=" + typeImage +
                ", padSn='" + padSn + '\'' +
                ", errorCode=" + errorCode +
                '}';
    }
}
