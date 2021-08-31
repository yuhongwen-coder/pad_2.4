package com.maxvision.tech.robot.utils;

import com.maxvision.tech.mqtt.entity.Heart;
import com.maxvision.tech.mqtt.entity.state.RobotFunctionState;
import com.maxvision.tech.robot.R;

/**
 * name: wy
 * date: 2021/4/8
 * desc:
 */
public class RobotUtils {


    /**
     * 机器人类型
     * 1：通用机器人
     * 2：协运机器人
     * 3：消毒机器人
     * 4：新测温机器人
     * 5：边检机器人
     * 6：海关机器人
     */
    public static int getRobotType(String type) {
        int rid;
        switch (type) {
            case "5":
                rid = R.mipmap.robot4;
                break;
            case "2":
            case "3":
                rid = R.mipmap.robot2;
                break;
            case "4":
                rid = R.mipmap.robot3;
                break;
            case "1":
            case "6":
            default:
                rid = R.mipmap.robot1;
                break;
        }
        return rid;
    }

    /**
     * 获取在线信息
     */
    public static int getLine(Heart heart) {
        int rid;
        if (heart.isLine) {
            //在线
            if (heart.isAlarm) {
                //有报警
                rid = R.drawable.shape_backgroung_round_red;
            } else {
                rid = R.drawable.shape_backgroung_round_bule;
            }
        } else {
            //不在线
            rid = R.drawable.shape_backgroung_round_gray;
        }
        return rid;
    }

    public static boolean isLine(boolean isLine) {
        if (!isLine) {
            CustomToast.toastLong(CustomToast.TIP_ERROR, "设备离线，无法操作");
            return true;
        }
        return false;
    }

    /**
     * 计算两点之前的距离
     * @param x 机器人当前位置X坐标
     * @param y 机器人当前位置Y坐标
     * @param x1 距离实际X坐标
     * @param y1 距离实际Y坐标
     * @return 单位返回米
     */
    public static double getDistance(double x, double y, double x1, double y1) {
        double x_1 = x1 * 0.05;
        double y_1 = y1 * 0.05;
        double _x = x * 0.05;
        double _y = y * 0.05;
        return Math.abs(Math.sqrt((x_1 - _x) * (x_1 - _x) + (y_1 - _y) * (y_1 - _y)));
    }

    // 是否有USB摄像头
    public static boolean isNoCamera(String type) {
        return type.equals("2") || type.equals("3") || type.equals("4")|| type.equals("5");
    }

    // 界面是否占用USB摄像头
    public static boolean isCameraUsed(int functionShow) {
        return functionShow == RobotFunctionState.FOLLOW || functionShow == RobotFunctionState.LOGIN
                || functionShow == RobotFunctionState.QYBK || functionShow == RobotFunctionState.RLSB
                || functionShow == RobotFunctionState.HWCW || functionShow == RobotFunctionState.KZJC;
    }

}