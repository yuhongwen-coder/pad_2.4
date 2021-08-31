package com.maxvision.tech.mqtt.entity.state;

/**
 * name: zjj
 * date: 2021/04/13
 * time: 17:26
 * desc: 机器人门状态
 */
public class RobotDoorState {
    /*
     * 0:  关門
     * 1： 开門
     * 2： 关門中
     * 3： 开門中
     */
    public static final int OPEN_ING = 3;
    public static final String OPEN_ING_STRING = "车门开启中";

    public static final int CLOSE_ING = 2;
    public static final String CLOSE_ING_STRING = "车门关闭中";

    public static final int CLOSED = 0;
    public static final String CLOSED_STRING = "车门已关闭";

    public static final int OPEND = 1;
    public static final String OPEND_STRING = "车门已开启";

    public static String getDoorState(int doorType) {
        String doorString;
        switch (doorType) {
            case 3:
                doorString = OPEN_ING_STRING;
                break;
            case 2:
                doorString = CLOSE_ING_STRING;
                break;
            case 1:
                doorString = OPEND_STRING;
                break;
            default:
                doorString = CLOSED_STRING;
                break;

        }
        return doorString;
    }
}
