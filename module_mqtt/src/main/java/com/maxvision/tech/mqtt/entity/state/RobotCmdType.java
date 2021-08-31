package com.maxvision.tech.mqtt.entity.state;

/**
 * name: zjj
 * date: 2021/04/21
 * time: 13:38
 * desc:
 */
public class RobotCmdType {

    /**
     * 100：定位 (初始化)
     * 201：导航
     * 202: 停止导航
     * 301：充电
     * 302：取消充电
     * 401: 执行任务
     * 402: 停止任务
     * 501: 执行消毒作业
     * 502: 恢复消毒作业
     * 503: 结束消毒作业
     * 601: 执行协运作业
     * 602: 停止协运作业
     */
    // 无命令
    public static final String CMD_NONE = "-1";

    // 初始化
    public static final String CMD_INIT = "100";

    // 导航
    public static final String CMD_NAV = "201";
    // 停止导航
    public static final String CMD_STOP_NAV = "202";

    // 充电
    public static final String CMD_CHARGE = "301";
    // 停止充电
    public static final String CMD_STOP_CHARGE = "302";

    // 任务调度
    public static final String CMD_TASK = "401";
    // 停止任务调度
    public static final String CMD_STOP_TASK = "402";

    // 消毒作业
    public static final String CMD_XDZY = "501";
    // 打断消毒作业
    public static final String CMD_BREAK_XDZY = "502";
    // 恢复消毒作业
    public static final String CMD_RESUME_XDZY = "503";
    // 停止消毒作业
    public static final String CMD_STOP_XDZY = "504";

    // 智能协运
    public static final String CMD_ZNXY = "601";
    // 暂停
    public static final String CMD_PAUSE_ZNXY = "602";
    // 停止
    public static final String CMD_STOP_ZNXY = "603";

    // 停止所有操作
    public static final String CMD_STOP_MAP ="999";

}
