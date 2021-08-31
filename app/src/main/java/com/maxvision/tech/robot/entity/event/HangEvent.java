package com.maxvision.tech.robot.entity.event;

/**
 * name: qs
 * date: 2021/5/6
 * desc:
 */
public class HangEvent {

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    private String sn;

    public HangEvent(String sn) {
        this.sn = sn;
    }
}