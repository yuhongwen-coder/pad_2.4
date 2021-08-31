package com.maxvision.tech.robot.db;

import androidx.paging.DataSource;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import io.reactivex.Completable;

/**
 * 区域布控数据库操作
 */
@Dao
public interface AreaMontiorAlarmDao {

    @Insert
    void insert(AreaMontiorAlarmDb areaMontiorAlarm);

    @Insert
    void insert(List<AreaMontiorAlarmDb> list);

    @Query("SELECT * FROM area_montior_tb WHERE robotSn=:sn ORDER BY id DESC")
    DataSource.Factory<Integer,AreaMontiorAlarmDb> queryData(String sn);

}
