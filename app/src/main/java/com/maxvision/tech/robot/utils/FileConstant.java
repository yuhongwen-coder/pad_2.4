package com.maxvision.tech.robot.utils;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;

import java.io.File;

/**
 * name: zjj
 * date: 2019/04/10
 * time: 下午 16:37
 * desc: 文件管理
 */
public class FileConstant {

    private static String ROOT_PATH = "Robot";//  根目录
    private static String PATH_MAP_IMAGE; //  地图
    private static String PATH_ALARM_AREA_IMAGE;
    private static String PATH_ALARM_MASK_IMAGE;
    private static String PATH_ALARM_FACE_IMAGE; //  口罩检测
    private static String PATH_ALARM_TEMP_IMAGE; //  红外测温


    private static FileConstant instance;

    private FileConstant() {
    }

    public static FileConstant getInstance() {
        if (instance == null) {
            synchronized (FileConstant.class) {
                if (instance == null) {
                    instance = new FileConstant();
                }
            }
        }
        return instance;
    }

    public void initPath(Context context) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            //String sdPath = Environment.getExternalStorageDirectory() + "/";
            String sdPath = context.getExternalFilesDir(null) + "/";
            ROOT_PATH = sdPath + ROOT_PATH;
            PATH_MAP_IMAGE = ROOT_PATH + "/map/image/";//地图图片路径
            PATH_ALARM_AREA_IMAGE = ROOT_PATH + "/alarm/area/";//区域布控报警
            PATH_ALARM_MASK_IMAGE = ROOT_PATH + "/alarm/mask/";//口罩检测
            PATH_ALARM_FACE_IMAGE = ROOT_PATH + "/alarm/face/";//人脸识别
            PATH_ALARM_TEMP_IMAGE = ROOT_PATH + "/alarm/temp/";//红外测温



            createFile(ROOT_PATH);
            createFile(PATH_MAP_IMAGE);
            createFile(PATH_ALARM_AREA_IMAGE);
            createFile(PATH_ALARM_MASK_IMAGE);
            createFile(PATH_ALARM_FACE_IMAGE);
            createFile(PATH_ALARM_TEMP_IMAGE);
        }
    }

    private void createFile(String path) {
        if (TextUtils.isEmpty(path)) {
            throw new RuntimeException("file path can not be null");
        }
        File file = new File(path);
        file.mkdirs();
    }

    /**
     * 获取app的根目录
     */
    private String getAppPath(Context context) {
        return context.getFilesDir().getParent() + "/";
    }

    public String getRootPath() {
        return ROOT_PATH;
    }

    public String getPathMapImage(){
        return PATH_MAP_IMAGE;
    }

    public String getPathAlarmAreaImage() {
        return PATH_ALARM_AREA_IMAGE;
    }

    public String getPathAlarmMaskImage() {
        return PATH_ALARM_MASK_IMAGE;
    }

    public String getPathAlarmFaceImage() {
        return PATH_ALARM_FACE_IMAGE;
    }

    public String getPathAlarmTempImage() {
        return PATH_ALARM_TEMP_IMAGE;
    }

}
