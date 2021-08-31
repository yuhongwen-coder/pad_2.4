package com.maxvision.tech.robot.entity;

import com.maxvision.tech.robot.R;

import java.util.ArrayList;
import java.util.List;

/**
 * name: wy
 * date: 2021/4/9
 * desc:
 */
public class FunctionEntity {

    public int id;
    public String name;
    public int imageId;
    public static final FunctionEntity alarmEntity = new FunctionEntity(1,"报警列表", R.mipmap.alarm_list);
    public static final FunctionEntity alarmSwitchEntity = new FunctionEntity(4,"算法设置", R.mipmap.alarm_switch);
    public static final FunctionEntity faceEntity = new FunctionEntity(2,"人脸上传", R.mipmap.face_upload);
    public static final FunctionEntity chargeEntity = new FunctionEntity(3,"自助充电", R.mipmap.ic_fun_self_charge);
    public static final FunctionEntity robotEntity = new FunctionEntity(5,"小车设置", R.mipmap.ic_fun_robot_setting);
    public static final FunctionEntity dialogueEntity = new FunctionEntity(6,"智能对话", R.mipmap.ic_fun_robot_chat);

    public FunctionEntity(int id, String name, int imageId) {
        this.id = id;
        this.name = name;
        this.imageId = imageId;
    }

    public static List<FunctionEntity> getAllData(){
        List<FunctionEntity> list = new ArrayList<>();
        list.add(alarmEntity);
        list.add(alarmSwitchEntity);
        list.add(faceEntity);
        list.add(chargeEntity);
        list.add(robotEntity);
        //list.add(dialogueEntity);
        return list;
    }
}