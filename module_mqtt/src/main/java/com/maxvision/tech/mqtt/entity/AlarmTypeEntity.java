package com.maxvision.tech.mqtt.entity;

import com.maxvision.tech.mqtt.entity.BaseAlarmEntity;

/**
 * Created by yuhongwen
 * on 2021/4/8
 * 报警种类
 */
public class AlarmTypeEntity extends BaseAlarmEntity {

    public AlarmTypeEntity(int alarmType, String alarmMessage) {
        this.alarmType = alarmType;
        this.alarmMessage = alarmMessage;
    }

    public int getAlarmType() {
        return alarmType;
    }

    public void setAlarmType(int alarmType) {
        this.alarmType = alarmType;
    }

    public String getAlarmMessage() {
        return alarmMessage;
    }

    public void setAlarmMessage(String alarmMessage) {
        this.alarmMessage = alarmMessage;
    }

    @Override
    public String toString() {
        return "AlarmTypeEntity{" +
                "alatmType=" + alarmType +
                ", alatmMessage='" + alarmMessage + '\'' +
                '}';
    }
}
