package com.maxvision.tech.mqtt.entity;

/**
 * name: wy
 * date: 2021/4/1
 * desc:
 */
public class RemoteVideoEntity {

    //平板sn
    public String sn;
    //1：音频视频全开（默认模式）  2：音频关 视频开（智能对话关闭音频模式）
    public int type;
    //房间号
    public String roomId;
}