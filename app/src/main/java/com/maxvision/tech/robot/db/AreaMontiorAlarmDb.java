package com.maxvision.tech.robot.db;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * 区域布控
 */
@Entity(tableName = "area_montior_tb")
public class AreaMontiorAlarmDb {
    //报警ID
    @PrimaryKey(autoGenerate = true)
    @NonNull
    private Long id;
    @ColumnInfo(name = "robotSn")
    private String robotSn;
    //报警图片路径
    @ColumnInfo(name = "image_path")
    private String alarmImagePath;
    //报警时间
    @ColumnInfo(name = "time")
    private Long time;

    @ColumnInfo(name = "robotType")
    private String appType;
    @ColumnInfo(name = "alarmType")
    private int alarmType;

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

    public String getAlarmImagePath() {
        return alarmImagePath;
    }

    public void setAlarmImagePath(String alarmImagePath) {
        this.alarmImagePath = alarmImagePath;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

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
}
