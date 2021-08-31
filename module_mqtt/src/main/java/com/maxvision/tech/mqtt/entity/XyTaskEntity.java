package com.maxvision.tech.mqtt.entity;

/**
 * Created by yuhongwen
 * on 2021/4/19
 */
public class XyTaskEntity extends BaseTaskEntity {
    //任务密码（json格式：任务密码+密码类型）
    public String passWord;
    // 任务线路(json)
    public String taskLocation;
}
