package com.maxvision.tech.mqtt.entity;

import com.maxvision.tech.mqtt.MQTTManager;

/**
 * name: wy
 * date: 2021/5/6
 * desc:
 */
public class DialogueEntity {

    /**
     * 发送消息类型
     * 0：空状态
     * 1：文字
     * 2：语音
     * 100：心跳
     */
    public int type;

    //平板SN
    public String padSn;

    //消息ID
    public String msgId;

    //消息内容
    public String content;


    public static DialogueEntity sendInit(int type){
        DialogueEntity entity = new DialogueEntity();
        entity.padSn = MQTTManager.PAD_SN;
        entity.type = type;
        return entity;
    }

    public static DialogueEntity sendMessage(int type,String msgId,String content){
        DialogueEntity entity = new DialogueEntity();
        entity.padSn = MQTTManager.PAD_SN;
        entity.type = type;
        entity.msgId = msgId;
        entity.content = content;
        return entity;
    }

}