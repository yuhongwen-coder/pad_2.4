package com.maxvision.tech.robot.entity.event;

import com.maxvision.tech.mqtt.entity.TaskControlResponse;

/**
 * name: zjj
 * date: 2021/04/21
 * time: 19:33
 * desc:
 */
public class TaskEvent {
    private TaskControlResponse commonResponse;
    public TaskEvent() {
    }

    public TaskControlResponse getCommonResponse() {
        return commonResponse;
    }

    public void setCommonResponse(TaskControlResponse commonResponse) {
        this.commonResponse = commonResponse;
    }
}
