package com.maxvision.tech.mqtt.entity.state;

/**
 * name: wy
 * date: 2021/4/1
 * desc: 在线语音状态
 */
public class RobotVoiceState {

    //空闲
    public static final int VOICE_NULL = 0;
    //正在识别
    public static final int VOICE_SB = 1;
    //正在合成
    public static final int VOICE_HC = 2;
    //正在播报
    public static final int VOICE_BB = 3;

}