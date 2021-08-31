package com.maxvision.tech.mqtt.entity.state;

/**
 * name: zjj
 * date: 2021/04/12
 * time: 16:26
 * desc: 消毒作业状态
 */
public class RobotXdzyState {

    // 空闲
    public static final int EMPTY = 0;

    // 执行中
    public static final int ING = 1;

    // 打断->(暂停)
    public static final int BREAK = 2;

}
