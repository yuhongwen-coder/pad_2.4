package com.maxvision.tech.mqtt.entity.state;

/**
 * name: wy
 * date: 2021/4/1
 * desc: 机器人对讲状态
 */
public class RobotIntercomState {

    //空闲
    public static final int INTERCOM_NULL = 0;
    //呼叫中（机器人呼叫的）
    public static final int INTERCOM_CALL_ING = 1;
    //通话中（机器人呼叫的）
    public static final int INTERCOM_CALL_ON = 2;
    //挂断中（机器人挂断的）
    public static final int INTERCOM_HANGEUP_ING = 3;

    //接听中（机器人接听的）
    public static final int INTERCOM_ANSWER_ING = 4;
    //通话中（机器人接听的）
    public static final int INTERCOM_ANSWER_ON = 5;
}