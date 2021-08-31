package com.maxvision.tech.mqtt;

/**
 * name: wy
 * date: 2021/2/8
 * desc:
 */
public interface MessageHandlerCallBack {

    void messageSuccess(String topicName,String message);
    void connectSuccess();

}
