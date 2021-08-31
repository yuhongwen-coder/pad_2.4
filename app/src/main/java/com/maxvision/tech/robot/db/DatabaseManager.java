package com.maxvision.tech.robot.db;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.maxvision.tech.robot.AppHolder;

/**
 * Created by yuhongwen
 * on 2021/4/9
 * AndroidX.room 集成数据库了解一下
 *  Room is a Database Object Mapping library that makes it easy to access database on Android applications
 */
@Database(entities = {
        AreaMontiorAlarmDb.class,
        FaceListAlarmDb.class,
        TempAlarmDb.class,
        MaskListAlarmDb.class,
        },
        version = 1,exportSchema = true)
public abstract class DatabaseManager extends RoomDatabase {

    private static final String DB_NAME = "robot_app";
    private static volatile DatabaseManager mDbManager;

    public static DatabaseManager getInstance(){
        if (mDbManager == null){
            synchronized (DatabaseManager.class) {
                if (mDbManager == null) {
                    mDbManager = create();
                }
            }
        }
        return mDbManager;
    }

    private static DatabaseManager create(){
        return Room.databaseBuilder(
                AppHolder.getInstance(),
                DatabaseManager.class, DB_NAME)
                .allowMainThreadQueries()
                .build();
    }

    public abstract AreaMontiorAlarmDao getAreaMontiorAlarmDao();
    public abstract FaceListAlarmDao getFaceAlarmDao();
    public abstract MaskAlarmDao getMaskAlarmDao();
    public abstract TempAlarmDao getTemoAlarmDao();

}
