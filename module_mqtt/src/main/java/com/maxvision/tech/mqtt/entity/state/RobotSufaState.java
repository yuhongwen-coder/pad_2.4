package com.maxvision.tech.mqtt.entity.state;

/**
 * name: wy
 * date: 2021/4/1
 * desc: 算法功能开启状态
 * -1：不显示
 * 0：关闭
 * 1：开启
 */
public class RobotSufaState {

    // 所有状态默认为-1,不显示,根据机器人发送心跳判断;

    //人脸识别(黑名单报警)
    public int face = -1;
    //区域布控
    public int qybk = -1;
    //红外测温
    public int hwcw = -1;
    //口罩检测
    public int kzjc = -1;

    @Override
    public String toString() {
        return "RobotSufaState{" +
                "face=" + face +
                ", qybk=" + qybk +
                ", hwcw=" + hwcw +
                ", kzjc=" + kzjc +
                '}';
    }
}