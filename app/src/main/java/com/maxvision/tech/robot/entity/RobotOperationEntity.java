package com.maxvision.tech.robot.entity;

import com.maxvision.tech.robot.AppHolder;
import com.maxvision.tech.robot.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yuhongwen
 * on 2021/4/16
 */
public class RobotOperationEntity {
    private int operationType;
    private String operationString;
    private int operationImageRs;

    /**
     * 取放行李
     * @param data 类型
     *             1取物
     *             2放物
     *             3复位
     *             4拆装
     *             5拆装结束
     */
    public static final int GET_OBJECT = 1;
    public static final int PUT_OBJECT = 2;
    public static final int RESET_DOOR = 3;
    public static final int CHAI_ZHUANG_START = 4;
    public static final int CHAI_ZHUANG_END = 5;



    public RobotOperationEntity(int operationType, String operationString, int operationImageRs) {
        this.operationType = operationType;
        this.operationString = operationString;
        this.operationImageRs = operationImageRs;
    }

    public int getOperationType() {
        return operationType;
    }

    public void setOperationType(int operationType) {
        this.operationType = operationType;
    }

    public String getOperationString() {
        return operationString;
    }

    public void setOperationString(String operationString) {
        this.operationString = operationString;
    }

    public int getOperationImageRs() {
        return operationImageRs;
    }

    public void setOperationImageRs(int operationImageRs) {
        this.operationImageRs = operationImageRs;
    }

    public static List<RobotOperationEntity> getXieyunOperation() {
        List<RobotOperationEntity> list = new ArrayList<>();
        list.add(new RobotOperationEntity(PUT_OBJECT, AppHolder.getInstance().getString(R.string.put_object)
                ,R.mipmap.robot_setting_put_object));
        list.add(new RobotOperationEntity(GET_OBJECT, AppHolder.getInstance().getString(R.string.get_object)
                ,R.mipmap.robot_setting_get_object));
        list.add(new RobotOperationEntity(RESET_DOOR, AppHolder.getInstance().getString(R.string.reset)
                ,R.mipmap.robot_setting_reset));
        list.add(new RobotOperationEntity(CHAI_ZHUANG_START, AppHolder.getInstance().getString(R.string.chaizhuang_start)
                ,R.mipmap.robot_setting_chaizhuang));
        list.add(new RobotOperationEntity(CHAI_ZHUANG_END, AppHolder.getInstance().getString(R.string.chaizhuang_end)
                ,R.mipmap.robot_setting_chaizhuang));
        return list;
    }

    public static List<RobotOperationEntity> getXiaoduOperation() {
        List<RobotOperationEntity> list = new ArrayList<>();
        list.add(new RobotOperationEntity(CHAI_ZHUANG_START, AppHolder.getInstance().getString(R.string.chaizhuang_start)
                ,R.mipmap.robot_setting_chaizhuang));
        list.add(new RobotOperationEntity(CHAI_ZHUANG_END, AppHolder.getInstance().getString(R.string.chaizhuang_end)
                ,R.mipmap.robot_setting_chaizhuang));
        return list;
    }
}
