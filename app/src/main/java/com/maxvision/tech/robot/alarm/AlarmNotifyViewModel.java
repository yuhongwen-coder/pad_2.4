package com.maxvision.tech.robot.alarm;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.paging.DataSource;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

import com.maxvision.tech.robot.db.AreaMontiorAlarmDb;
import com.maxvision.tech.robot.db.DatabaseManager;
import com.maxvision.tech.robot.db.FaceListAlarmDb;
import com.maxvision.tech.robot.db.MaskListAlarmDb;
import com.maxvision.tech.robot.db.TempAlarmDb;

/**
 * Created by yuhongwen
 * on 2021/4/9
 * 使用LiveData结合ViewModel可以感知Activity或者Fragment生命周期，并且当持有的数据源发生改变时，能实时更新页面
 * 这样就避免报警数据 通过Mqtt上报后，又通过 接口回调或者 EventBus或者广播来通知页面刷新
 */
public class AlarmNotifyViewModel extends AndroidViewModel {
    private final static int PAGE_NUM = 10;

    public AlarmNotifyViewModel(@NonNull Application application) {
        super(application);
    }

    /**
     * 获取区域布控报警数据
     * @param sn
     * @return
     */
    public LiveData<PagedList<AreaMontiorAlarmDb>> getAreaMonitorData(String sn){
        DataSource.Factory<Integer, AreaMontiorAlarmDb> factory = DatabaseManager.getInstance()
                .getAreaMontiorAlarmDao()
                .queryData(sn);
        return new LivePagedListBuilder<>(factory, PAGE_NUM)
                .build();
    }

    /**
     * 获取口罩报警数据
     * @param sn
     * @return
     */
    public LiveData<PagedList<MaskListAlarmDb>> getMaskAlarmData(String sn){
        DataSource.Factory<Integer, MaskListAlarmDb> factory = DatabaseManager.getInstance()
                .getMaskAlarmDao()
                .queryMaskAlarm(sn);
        return new LivePagedListBuilder<>(factory, PAGE_NUM)
                .build();
    }

    /**
     * 查询红外测温报警
     * @param sn
     * @return
     */
    public LiveData<PagedList<TempAlarmDb>> getTempAlarmData(String sn){
        DataSource.Factory<Integer, TempAlarmDb> factory = DatabaseManager.getInstance()
                .getTemoAlarmDao()
                .queryTempAlarm(sn);
        return new LivePagedListBuilder<>(factory, PAGE_NUM)
                .build();
    }

    /**
     * 查询人脸报警
     * @param sn
     * @return
     */
    public LiveData<PagedList<FaceListAlarmDb>> getFaceAlarmData(String sn){
        DataSource.Factory<Integer, FaceListAlarmDb> factory = DatabaseManager.getInstance()
                .getFaceAlarmDao()
                .queryFaceAlarm(sn);
        return new LivePagedListBuilder<>(factory, PAGE_NUM)
                .build();
    }
}
