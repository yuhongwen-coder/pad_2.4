package com.maxvision.tech.mqtt.entity;

/**
 * Created by yuhongwen
 * on 2021/4/25
 */
public class XyTaskControlResponse extends TaskControlResponse{
    public int taskLocation; // 当前机器人位置
    public String listenerType;
    public int taskHaveObject; // 机器人是否有物体存在
    public String passWord;
    public String taskMap;
}
