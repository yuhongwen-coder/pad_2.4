package com.maxvision.tech.mqtt.entity.state;

import java.io.Serializable;

/**
 * name: zjj
 * date: 2021/04/13
 * time: 19:19
 * desc: 机器人提示类型
 */
public class RobotStateHint implements Serializable {

    // 提示
    public String msg;

    // 显示弹窗
    public boolean isDialog;

    // 指令类型,默认没有指令(第一个指令)
    public String firstCmd = "-1";

    // 第二个指令
    public String secondCmd = "-1";

    // 第一个指令和第二个指令是否互斥(二选一执行还是二选二顺序执行)
    // 默认顺序执行
    public boolean cmdSync;

    public RobotStateHint() {

    }

    public RobotStateHint(boolean isDialog,String msg,String firstCmd) {
        this(isDialog,msg,firstCmd,RobotCmdType.CMD_NONE);
    }

    public RobotStateHint(boolean isDialog,String msg,String firstCmd,String secondCmd) {
        this(isDialog,msg,firstCmd,secondCmd,false);
    }

    public RobotStateHint(boolean isDialog,String msg,String firstCmd,String secondCmd,boolean cmdSync) {
        this.isDialog = isDialog;
        this.msg = msg;
        this.firstCmd = firstCmd;
        this.secondCmd = secondCmd;
        this.cmdSync = cmdSync;
    }

    @Override
    public String toString() {
        return "RobotStateHint{" +
                "msg='" + msg + '\'' +
                ", isDialog=" + isDialog +
                ", firstCmd='" + firstCmd + '\'' +
                ", secondCmd='" + secondCmd + '\'' +
                ", cmdSync=" + cmdSync +
                '}';
    }
}
