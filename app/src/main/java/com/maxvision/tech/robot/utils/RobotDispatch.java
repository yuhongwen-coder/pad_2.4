package com.maxvision.tech.robot.utils;

import android.util.Log;

import com.maxvision.tech.mqtt.RobotState;
import com.maxvision.tech.mqtt.entity.Heart;
import com.maxvision.tech.robot.manager.RobotDataManager;
import java.util.List;
import java.util.concurrent.TimeUnit;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * name: wy
 * date: 2021/4/20
 * desc: 机器人调度类
 */
public class RobotDispatch {

    //机器人调度次数
    private boolean isCall = false;

    private Disposable disposable;
    private Disposable timerDisposable;
    private RobotDispatchListener listener;


    //添加调度
    public void addDispatch(){
        if(!isCall){
            isCall = true;
            startTimer();
            timer();
            Log.e("wangyin_调度","开始调度机器人");
        }else{
            if(listener != null){
                listener.onCancelNavigation();
            }
            disposeDispatch();
            Log.e("wangyin_调度","取消调度");
        }
    }

    public void setListener(RobotDispatchListener listener) {
        this.listener = listener;
    }

    //实时调用当前可用机器人
    public void startTimer(){
        if (disposable != null) disposable.dispose();
        if(!isCall) return;
        disposable = Observable.interval(0, 3, TimeUnit.SECONDS).subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(aLong -> {
                    if(!isCall){
                        Log.i("wangyin_调度","来了22："+isCall);
                        return;
                    }
                    Log.i("wangyin_调度","来了："+isCall);
                    List<Heart> list = RobotDataManager.getInstance().getSelectRobot();
                    for (Heart heart : list) {
                        String state = RobotState.state(heart);
                        if(heart.isSava && heart.isLine && "空闲".equals(state)){
                            if (listener != null){
                                listener.onDispatch(heart);
                                timer();
                                if (disposable != null) disposable.dispose();
                            }
                            return;
                        }
                    }
                });
    }

    //调度最大时长
    private void timer(){
        //120秒未到达，当前调度超时
        if (timerDisposable != null) timerDisposable.dispose();
        timerDisposable = Observable.timer(120, TimeUnit.SECONDS).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aLong -> {
                    Log.i("wangyin_调度","超时：");
                    if (listener != null){
                        listener.onTimeOut();
                        disposeDispatch();
                    }
                });
    }

    //获取当前机器人数量
    public boolean isCall() {
        return isCall;
    }

    public void disposeDispatch(){
        isCall = false;
        listener = null;
        if (disposable != null) disposable.dispose();
        disposeTimer();
    }

    private void disposeTimer(){
        if (timerDisposable != null) timerDisposable.dispose();
    }

    public interface RobotDispatchListener{
        //有机器人可调度调度机器人
        void onDispatch(Heart heart);
        //超时
        void onTimeOut();
        //取消导航
        void onCancelNavigation();
    }

}