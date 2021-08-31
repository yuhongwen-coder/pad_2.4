package com.maxvision.tech.mqtt.entity.state;

/**
 * name: qs
 * date: 2021/5/6
 * desc: 平板对讲状态
 */
public class PadIntercomState {

    //空闲
    public static final int INTERCOM_NULL = 0;
    //呼叫中（平板呼叫的）
    public static final int INTERCOM_CALL_ING = 1;
    //通话中（平板呼叫的）
    public static final int INTERCOM_CALL_ON = 2;
    //挂断中（平板挂断的）
    public static final int INTERCOM_HANGEUP_ING = 3;

    //接听中（平板接听的）
    public static final int INTERCOM_ANSWER_ING = 4;
    //通话中（平板接听的）
    public static final int INTERCOM_ANSWER_ON = 5;
}