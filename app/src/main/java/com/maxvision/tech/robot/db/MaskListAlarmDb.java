package com.maxvision.tech.robot.db;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * 口罩检测报警
 */
@Entity(tableName = "kzjc_list_tb")
public class MaskListAlarmDb {
    //唯一ID
    @PrimaryKey(autoGenerate = true)
    @NonNull
    private Long id;

    @ColumnInfo(name = "kzjc_id")
    private Long kzjcId;

    @ColumnInfo(name = "kzjc_image")
    private String imagePath;

    @ColumnInfo(name = "kzjc_content")
    //描述
    private String alarmDescribe;

    @ColumnInfo(name = "kzjc_time")
    //报警时间
    private Long time;


    //机器人唯一编号
    @ColumnInfo(name = "robotSn")
    private String robotSn;

    @ColumnInfo(name = "appType")
    private String appType;
    @ColumnInfo(name = "alarmType")
    private int alarmType;

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

    @NonNull
    public Long getId() {
        return id;
    }

    public void setId(@NonNull Long id) {
        this.id = id;
    }

    public Long getKzjcId() {
        return kzjcId;
    }

    public void setKzjcId(Long kzjcId) {
        this.kzjcId = kzjcId;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getAlarmDescribe() {
        return alarmDescribe;
    }

    public void setAlarmDescribe(String alarmDescribe) {
        this.alarmDescribe = alarmDescribe;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public String getRobotSn() {
        return robotSn;
    }

    public void setRobotSn(String robotSn) {
        this.robotSn = robotSn;
    }

    @Override
    public String toString() {
        return "KzjcListAlarmDb{" +
                "id=" + id +
                ", kzjcId='" + kzjcId + '\'' +
                ", imagePath='" + imagePath + '\'' +
                ", alarmDescribe='" + alarmDescribe + '\'' +
                ", time=" + time +
                ", sn='" + robotSn + '\'' +
                '}';
    }
}
