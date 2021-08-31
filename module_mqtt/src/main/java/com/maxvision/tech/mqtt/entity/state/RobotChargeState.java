package com.maxvision.tech.mqtt.entity.state;

/**
 * name: wy
 * date: 2021/4/1
 * desc:充电状态
 *
 * // 硬件返回充电状态 1：充电中  2：充满   其他：非充电状态 0
 * // 上层定义添加 3:正在前往充电状态
 */
public class RobotChargeState {
    // 空闲
    public static final int EMPTY = 0;
    // 正在充电
    public static final int ING = 1;
    // 充电已充满
    public static final int FULL = 2;
    // 正在前往充电
    public static final int NAV = 3;
}