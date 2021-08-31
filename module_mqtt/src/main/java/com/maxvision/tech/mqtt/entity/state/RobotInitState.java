package com.maxvision.tech.mqtt.entity.state;

/**
 * name: zjj
 * date: 2021/04/09
 * time: 9:05
 * desc: 机器人初始化状态
 */
public class RobotInitState {

    // 未初始化
    public static final int EMPTY = 0;

    // 初始化失败
    public static final int FAIL = -1;

    // 正在初始化
    public static final int ROTATING = 1;

    // 初始化成功
    public static final int SUCCESS = 2;
}
