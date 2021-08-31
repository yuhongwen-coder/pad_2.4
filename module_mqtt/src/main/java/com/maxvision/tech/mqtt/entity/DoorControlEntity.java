package com.maxvision.tech.mqtt.entity;

/**
 * Created by yuhongwen
 * on 2021/4/17
 */
public class DoorControlEntity extends BaseEntity {
    public int doorType;

    public DoorControlEntity(int doorType, String padSn) {
        this.doorType = doorType;
        this.padSn = padSn;
    }
}
