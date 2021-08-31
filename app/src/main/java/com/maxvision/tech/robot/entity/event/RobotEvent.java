package com.maxvision.tech.robot.entity.event;

/**
 * name: wy
 * date: 2021/4/8
 * desc:
 */
public class RobotEvent {

    //机器人更新
    public static final int UPDATE_ROBOT = 1;
    //刷新机器人在线状态
    public static final int UPDATE_LINE_STATE = 2;

    public int type;

    public Object object;
    public RobotEvent() {
    }
    public RobotEvent(int type) {
        this.type = type;
    }
    public RobotEvent(int type, Object object) {
        this.type = type;
        this.object = object;
    }
}