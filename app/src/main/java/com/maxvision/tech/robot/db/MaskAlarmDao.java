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
public interface MaskAlarmDao {
    @Insert
    void insert(MaskListAlarmDb kzjcListAlarmDb);

    @Query("select * from kzjc_list_tb where robotSn=:sn order by id desc")
    DataSource.Factory<Integer,MaskListAlarmDb> queryMaskAlarm(String sn);
}
