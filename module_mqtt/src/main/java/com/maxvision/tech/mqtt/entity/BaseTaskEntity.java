package com.maxvision.tech.mqtt.entity;

/**
 * Created by yuhongwen
 * on 2021/4/19
 */
public class BaseTaskEntity extends BaseEntity implements Comparable<BaseTaskEntity>{

    public String robotType;

    public String robotSn;

    public String taskId;

    public String taskName;

    public int taskStatus;

    public boolean isTimeTask;


    @Override
    public int compareTo(BaseTaskEntity o) {
        return o.taskStatus - this.taskStatus;
    }

}
