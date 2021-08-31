package com.maxvision.tech.mqtt.entity;

import java.io.Serializable;
import java.util.List;

/**
 * name: zjj
 * date: 2021/04/13
 * time: 15:16
 * desc: 任务实体
 */
public class TaskControlEntity implements Serializable {

    public String sn;
    /**
     * 100：定位
     * 201：导航
     * 202: 停止导航
     * 301：充电
     * 302：取消充电
     * 401: 执行任务
     * 402: 停止任务
     * 501: 执行消毒作业
     * 502: 恢复消毒作业
     * 503: 结束消毒作业
     * 601: 执行协运作业
     * 602: 停止协运作业
     */
    public String firstCmd;    // 第一条命令
    public String secondCmd;    // 第二条命令
    public boolean isTimeTask;  // 是否是定时消毒作业或者任务
    public String taskId;
    public String taskName; // 任务名称或者导航点名称
    public double x;
    public double y;
    public double angel;

}