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
public interface FaceListAlarmDao {
    @Insert
    void insert(FaceListAlarmDb faceListAlarm);

    @Query("select * from face_list_tb where robotSn=:sn order by id desc")
    DataSource.Factory<Integer, FaceListAlarmDb> queryFaceAlarm(String sn);
}
