package com.maxvision.tech.robot.db;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * 红外测温
 */
@Entity(tableName = "temp_alarm_tb")
public class TempAlarmDb {

    //消息ID
    @PrimaryKey(autoGenerate = true)
    @NonNull
    private Long id;
    //机器人唯一编号
    @ColumnInfo(name = "robotSn")
    private String robotSn;
    //实况截图url
    @ColumnInfo(name = "image_path")
    private String liveImgbytes;
    //红外图片url
    @ColumnInfo(name = "hwimage_path")
    private String tempImgbytes;
    //温度
    @ColumnInfo(name = "temperature")
    private String temperature;
    //报警时间
    @ColumnInfo(name = "time")
    private Long time;
    //报警类型
    @ColumnInfo(name = "temp_alarm_type")
    private int tempAlarmType;  //0：低温  1：高温

    @ColumnInfo(name = "appType")
    private String appType;

    @ColumnInfo(name = "alarmType")
    private int alarmType;  //报警类型

    public String getAppType() {
        return appType;
    }

    public void setAppType(String appType) {
        this.appType = appType;
    }

    public int getAlarmType() {
        return alarmType;
    }

    public void setAlarmType(int alarmType) {
        this.alarmType = alarmType;
    }

    public int getTempAlarmType() {
        return tempAlarmType;
    }

    public void setTempAlarmType(int tempAlarmType) {
        this.tempAlarmType = tempAlarmType;
    }

    @NonNull
    public Long getId() {
        return id;
    }

    public void setId(@NonNull Long id) {
        this.id = id;
    }

    public String getRobotSn() {
        return robotSn;
    }

    public void setRobotSn(String robotSn) {
        this.robotSn = robotSn;
    }

    public String getLiveImgbytes() {
        return liveImgbytes;
    }

    public void setLiveImgbytes(String liveImgbytes) {
        this.liveImgbytes = liveImgbytes;
    }

    public String getTempImgbytes() {
        return tempImgbytes;
    }

    public void setTempImgbytes(String tempImgbytes) {
        this.tempImgbytes = tempImgbytes;
    }

    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }
}
