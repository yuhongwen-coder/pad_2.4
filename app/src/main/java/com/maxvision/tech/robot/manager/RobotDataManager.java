package com.maxvision.tech.robot.manager;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.maxvision.tech.mqtt.entity.BaseTaskEntity;
import com.maxvision.tech.mqtt.entity.Heart;
import com.maxvision.tech.mqtt.entity.TaskControlResponse;
import com.maxvision.tech.mqtt.entity.XieyunState;
import com.maxvision.tech.mqtt.entity.XyTaskControlResponse;
import com.maxvision.tech.mqtt.entity.state.RobotZnxyState;
import com.maxvision.tech.robot.entity.event.RobotEvent;
import com.maxvision.tech.robot.entity.event.TaskEvent;
import com.maxvision.tech.robot.utils.SpUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * name: wy
 * date: 2021/4/6
 * desc: 机器人管理
 */
public class RobotDataManager{

    public static final String ROBOT_SELECT_DATA = "robot_select_data";
    private final Gson gson = new Gson();
    private Disposable disposable;

    private static RobotDataManager robotData = null;

    private final Map<String, Heart> robotMap = new HashMap<>();

    private final Map<String, List<BaseTaskEntity>> taskMap = new HashMap<>();

    public static RobotDataManager getInstance() {
        if (robotData == null) {
            synchronized (RobotDataManager.class) {
                if (robotData == null) {
                    robotData = new RobotDataManager();
                }
            }
        }
        return robotData;
    }

    public RobotDataManager(){
        //已保存的选择的机器人
        String json = SpUtils.getString(ROBOT_SELECT_DATA);
        List<Heart> list = gson.fromJson(json,new TypeToken<List<Heart>>() {}.getType());
        if(list == null) return;
        for(Heart heart : list){
            heart.isSava = true;
            heart.isAlarm = false;
            heart.isLine = false;
            heart.isSel = true;
            Heart h = robotMap.get(heart.sn);
            if(h == null){
                robotMap.put(heart.sn, heart);
            }else{
                h.set(heart);
            }
        }
    }

    //添加已选择的机器人
    public synchronized Heart add(Heart heart){
        if(!robotMap.containsKey(heart.sn)){
            robotMap.put(heart.sn, heart);
        }
        Heart h = robotMap.get(heart.sn);
        if(h == null){
            heart.createTime = System.currentTimeMillis();
            robotMap.put(heart.sn, heart);
        }else{
            h.createTime = System.currentTimeMillis();
            h.set(heart);
        }
        return h;
    }

    public synchronized void remove(String sn){
        robotMap.remove(sn);
    }

    public synchronized Heart get(String sn){
        return robotMap.get(sn);
    }

    /**
     * 获取所有机器人
     */
    public synchronized List<Heart> getHeartRobot(){
        List<Heart> list = new ArrayList<>();
        for (Map.Entry<String, Heart> map : robotMap.entrySet()) {
            list.add(map.getValue());
        }
        return list;
    }

    /**
     * 获取被选中的机器人
     */
    public synchronized List<Heart> getSelectRobot(){
        List<Heart> list = new ArrayList<>();
        for (Map.Entry<String, Heart> map : robotMap.entrySet()) {
            Heart h = map.getValue();
            if(h.isSel){
                list.add(map.getValue());
            }
        }
        return list;
    }

    /**
     * 设置任务列表
     */
    public synchronized void setTaskList(String sn,List<BaseTaskEntity> task) {
        Heart heart = RobotDataManager.getInstance().get(sn);
        if (heart != null) {
            if (heart.xyMode == XieyunState.MODE_ZNXY && "2".equals(heart.type)) {
                for (int i = 0; i < task.size(); i++) {
                    if (task.get(i).taskStatus == RobotZnxyState.TASK_EXECUTING) {
                        Collections.sort(task, (o1, o2) -> {
                            // 升序
                            return o1.taskStatus - o2.taskStatus;
                        });
                        taskMap.put(sn, task);
                        EventBus.getDefault().post(new TaskEvent());
                        return;
                    }
                }
            }
        }
        // 降序
        Collections.sort(task);
        taskMap.put(sn, task);
        EventBus.getDefault().post(new TaskEvent());
    }

    /**
     * 任务列表数据
     */
    public synchronized List<BaseTaskEntity> getTaskList(String sn) {
        if (taskMap.size() <= 0) {
            return null;
        }
        return taskMap.get(sn);
    }

    /**
     * 更新任务列表
     */
    public synchronized void updateTaskList(TaskControlResponse response) {
        // 判断类型
        String taskId = response.taskId;
        if (TextUtils.isEmpty(taskId)) {
            return;
        }
        update(response);
    }


    private void update(TaskControlResponse response) {
        List<BaseTaskEntity> list = taskMap.get(response.sn);
        String taskId = response.taskId;
        if (list != null && list.size() > 0) {
            for (int i = 0; i < list.size();i++) {
                BaseTaskEntity taskEntity = list.get(i);
                if (taskId.equals(taskEntity.taskId)) {
                    taskEntity.taskStatus = response.taskStatus;
                    list.set(i,taskEntity);
                    setTaskList(response.sn,list);
                    break;
                }
            }
        }
    }


    public void startTimer(){
        if (disposable != null) disposable.dispose();
        disposable = Observable.interval(0, 2, TimeUnit.SECONDS).subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(aLong -> {
                    if(robotMap.size() == 0) return;
                    synchronized(RobotDataManager.class) {
                        long t = System.currentTimeMillis();
                        for (Map.Entry<String, Heart> map : robotMap.entrySet()) {
                            Heart heart = map.getValue();
                            if (!heart.isSava) continue;
                            boolean line = heart.isLine;
                            boolean is = t - heart.createTime < 4000;
                            //如果上次状态和这次不同，则通知界面刷新
                            if(line != is){
                                EventBus.getDefault().post(new RobotEvent(RobotEvent.UPDATE_LINE_STATE,heart));
                            }
                            heart.isLine = is;
                        }
                    }
                });
    }

    /**
     *  机器人客户端 多次点击 单个任务定点更新列表
     * @param data 单个任务
     */
    public synchronized void notifyUpdateSingleTask(XyTaskControlResponse data) {
        if (!TextUtils.isEmpty(data.taskId)) {
            // 更新下
            List<BaseTaskEntity> list = taskMap.get(data.sn);
            String taskId = data.taskId;
            if (list != null && list.size() > 0) {
                for (int i = 0; i < list.size();i++) {
                    BaseTaskEntity taskEntity = list.get(i);
                    if (taskId.equals(taskEntity.taskId)) {
                        taskEntity.taskStatus = data.taskStatus;
                        sortTask(data.taskStatus,list);
                        taskMap.put(data.sn,list);
                        break;
                    }
                }
            }
            TaskEvent taskEvent = new TaskEvent();
            taskEvent.setCommonResponse(data);
            EventBus.getDefault().post(taskEvent);
        }
    }

    private void sortTask(int taskStatus, List<BaseTaskEntity> list) {
        if (taskStatus == RobotZnxyState.TASK_EXECUTING) {
            Collections.sort(list, (o1, o2) -> {
                // 升序
                return o1.taskStatus - o2.taskStatus;
            });
        } else {
            Collections.sort(list, (o1, o2) -> {
                // 将序
                return o2.taskStatus - o1.taskStatus;
            });
        }
    }
}