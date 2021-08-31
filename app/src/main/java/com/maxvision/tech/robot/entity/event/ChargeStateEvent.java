package com.maxvision.tech.robot.entity.event;

/**
 * name: wy
 * date: 2021/4/14
 * desc:
 */
public class ChargeStateEvent {

    public String sn;
    public int bat;
    public int state;

    public ChargeStateEvent(String sn, int bat,int state) {
        this.sn = sn;
        this.bat = bat;
        this.state = state;
    }
}