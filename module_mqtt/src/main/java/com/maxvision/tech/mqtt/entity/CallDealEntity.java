package com.maxvision.tech.mqtt.entity;

/**
 * name: qs
 * date: 2021/4/8
 * desc:某一台终端处理了接听，挂断来电
 */
public class CallDealEntity {

    //平板唯一号
    public String padSn;
    //机器人设备唯一号
    public String robotSn;
    //0挂断 1接听
    public int deal;
}