package com.maxvision.tech.mqtt.entity;

/**
 * name: zjj
 * date: 2021/04/16
 * time: 14:00
 * desc:
 */
public class TaskControlResponse {

    public String sn;

    public String msg;

    // code == 200表示成功,其余表示失败
    public int code;

    public int taskStatus;  // 任务状态

    public String taskId;   // 任务Id

    public String robotType;

    @Override
    public String toString() {
        return "TaskControlResponse{" +
                "sn='" + sn + '\'' +
                ", msg='" + msg + '\'' +
                ", code=" + code +
                ", taskStatus=" + taskStatus +
                ", taskId='" + taskId + '\'' +
                ", robotType='" + robotType + '\'' +
                '}';
    }
}
