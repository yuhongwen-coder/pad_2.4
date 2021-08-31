package com.maxvision.tech.robot.db;

import androidx.paging.DataSource;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

/**
 * Created by yuhongwen
 * on 2021/4/9
 */
@Dao
public interface TempAlarmDao {
    @Insert
    void insert(TempAlarmDb dwAlarm);

    @Query("SELECT * FROM temp_alarm_tb WHERE robotSn=:sn ORDER BY id DESC")
    DataSource.Factory<Integer,TempAlarmDb> queryTempAlarm(String sn);
}
