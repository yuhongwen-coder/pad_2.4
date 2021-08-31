package com.maxvision.tech.mqtt.entity;

import com.maxvision.tech.mqtt.entity.state.RobotDeviceState;
import com.maxvision.tech.mqtt.entity.state.RobotSufaState;

/**
 * name: wy
 * date: 2021/4/6
 * desc:
 */
public class Heart {

    //机器人设备唯一号
    public String sn;
    /**
     * 机器人类型
     * 1：通用机器人
     * 2：协运机器人
     * 3：消毒机器人
     * 4：新测温机器人
     * 5：边检机器人
     * 6：海关机器人
     */
    public String type;
    //机器人名称
    public String name;
    //电量
    public int bat;
    //网络
    public int wifi;
    /**
     * 急停
     * 0：正常
     * 1：急停按下
     */
    public int stop;
    //地图ID
    public String mapId;
    //加密狗
    public boolean hasUsb;
    //初始化状态
    public int initState;
    //充电状态
    public int chargeState;
    //导航状态
    public int navState;
    //设备状态
    public RobotDeviceState deviceState;
    //页面状态
    public int functionShow;
    //报警开关状态(算法)
    public RobotSufaState sufaState;
    //语音状态
    public int voiceState;
    //任务调度状态
    public int taskState;
    //消毒作业状态
    public int xdzyState;
    //智能协运状态
    public int znxyState;
    //对讲状态
    public int intercomState;
    //上药状态
    public int drugState;
    // 药量
    public int drugValue;
    //上水状态
    public int waterState;
    //水量
    public int waterValue;
    //开门状态
    public int doorState;
    //任务Id
    public String runningId;
    // 协运模式-->默认协运模式
    public int xyMode = 0;

    //-----------临时变量----------------------------------------------------
    //创建时间
    public long createTime;
    //是否保存
    public boolean isSava = false;
    //是否在线
    public boolean isLine = false;
    //是否报警
    public boolean isAlarm = false;
    //是否选中
    public boolean isSel = false;
    // 协运下一拍状态
    public String znxyNextState ="";

    public void set(Heart h){
        sn = h.sn;
        type = h.type;
        name = h.name;
        bat = h.bat;
        wifi = h.wifi;
        stop = h.stop;
        mapId = h.mapId;
        hasUsb = h.hasUsb;
        initState = h.initState;
        chargeState = h.chargeState;
        navState = h.navState;
        deviceState = h.deviceState;
        functionShow = h.functionShow;
        sufaState = h.sufaState;
        voiceState = h.voiceState;
        taskState = h.taskState;
        xdzyState = h.xdzyState;
        znxyState = h.znxyState;
        intercomState = h.intercomState;
        drugValue = h.drugValue;
        drugState = h.drugState;
        waterValue = h.waterValue;
        waterState = h.waterState;
        doorState = h.doorState;
        runningId = h.runningId;
        znxyNextState = h.znxyNextState;
        xyMode = h.xyMode;
    }

}