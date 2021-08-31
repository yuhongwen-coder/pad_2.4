package com.maxvision.tech.robot.db;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * 人脸报警
 */
@Entity(tableName = "face_list_tb")
public class FaceListAlarmDb {
    //唯一ID
    @PrimaryKey(autoGenerate = true)
    @NonNull
    private Long id;
    @ColumnInfo(name = "faceId")
    private String faceId;
    //机器人唯一编号
    @ColumnInfo(name = "robotSn")
    private String robotSn;
    //时间 时间戳
    @ColumnInfo(name = "time")
    private Long time;
    //黑、白名单
    @ColumnInfo(name = "type")
    private String type;
    //图片路径
    @ColumnInfo(name = "image_path")
    private String imagePath;
    //实时抓拍图片
    @ColumnInfo(name = "capture_path")
    private String capturePath;
    //人脸姓名
    @ColumnInfo(name = "name")
    private String name;
    //性别
    @ColumnInfo(name = "sex")
    private String sex;

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

    public String getRobotSn() {
        return robotSn;
    }

    public void setRobotSn(String robotSn) {
        this.robotSn = robotSn;
    }

    @NonNull
    public Long getId() {
        return id;
    }

    public void setId(@NonNull Long id) {
        this.id = id;
    }

    public String getFaceId() {
        return faceId;
    }

    public void setFaceId(String faceId) {
        this.faceId = faceId;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getCapturePath() {
        return capturePath;
    }

    public void setCapturePath(String capturePath) {
        this.capturePath = capturePath;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

}
