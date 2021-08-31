package com.maxvision.tech.robot.alarm;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.maxvision.tech.robot.AppHolder;
import com.maxvision.tech.robot.utils.ImageEngineUtils;
import com.maxvision.tech.robot.MainActivity;
import com.maxvision.tech.robot.db.AreaMontiorAlarmDb;
import com.maxvision.tech.robot.db.DatabaseManager;
import com.maxvision.tech.robot.db.FaceListAlarmDb;
import com.maxvision.tech.robot.db.MaskListAlarmDb;
import com.maxvision.tech.robot.db.TempAlarmDb;
import com.maxvision.tech.mqtt.entity.AlarmTypeEntity;
import com.maxvision.tech.robot.ui.view.AlarmPopupWindow;
import com.maxvision.tech.robot.utils.FileConstant;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Created by yuhongwen
 * on 2021/4/6
 * 通知管理
 */
public class NotifyManager {
    private static NotifyManager notifyManager;
    private ExecutorService dbExecutor = null;
    private int iconId;
    private String title;
    private String content;
    private int id = 1;
    private Context mContext;
    private NotificationCompat.Builder mBuilder;

    public NotifyManager(Context context){
        this.mContext = context;
    }

    private NotifyManager() {
        dbExecutor = Executors.newCachedThreadPool();
    }

    public static NotifyManager getInstance() {
        if (notifyManager == null) {
            synchronized (NotifyManager.class) {
                if (notifyManager == null) {
                    notifyManager = new NotifyManager();
                }
            }
        }
        return notifyManager;
    }

    public void setNotifyMessage(int type, String title, String content){
        this.title = title;
        this.content = content;
        notification();
    }

    @SuppressLint("WrongConstant")
    private void notification(){
        String channelId = "alarm_notify";
        String channelName = "报警通知";
        android.app.NotificationManager notify = (android.app.NotificationManager) mContext.getSystemService(NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = notify.getNotificationChannel(channelId);
            if (channel == null) {
                channel = new NotificationChannel(channelId, channelName, NotificationManagerCompat.IMPORTANCE_MAX);
            }
            notify.createNotificationChannel(channel);
        }
        mBuilder = new NotificationCompat.Builder(mContext,channelId);
        mBuilder.setSmallIcon(iconId);
        mBuilder.setContentTitle(title);
        mBuilder.setContentText(content);
        mBuilder.setSubText(title);
        mBuilder.setAutoCancel(true);
        mBuilder.setNumber(1);
        mBuilder.setPriority(NotificationCompat.PRIORITY_MAX);
        mBuilder.setDefaults(NotificationCompat.DEFAULT_SOUND);
        mBuilder.setVibrate(new long[]{1000,1000,1000,1000});
        Intent intent = new Intent(mContext, AlarmNotifyActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivities(mContext,0,new Intent[]{intent},0);
        mBuilder.setContentIntent(pendingIntent);
        Notification build = mBuilder.build();
        if (notify == null){return;}
        notify.notify(id,build);
    }

    public synchronized void insertData(String onMessageJson) {
        dbExecutor.execute(() -> {
            JSONObject jsonObject = (JSONObject) JSON.parse(onMessageJson);
            if (jsonObject == null) return;
            int alarmType = (int) jsonObject.get("alarmType");
            if (alarmType == AlarmTypeEntity.ALARM_AREA) {
                AreaMontiorAlarmDb alarmDb = JSON.parseObject(onMessageJson, AreaMontiorAlarmDb.class);
                insertAreaDb(alarmDb);
            } else if (alarmType == AlarmTypeEntity.ALARM_FACE) {
                FaceListAlarmDb faceDb = JSON.parseObject(onMessageJson, FaceListAlarmDb.class);
                insertFaceDb(faceDb);
            } else if (alarmType == AlarmTypeEntity.ALARM_TEMP) {
                TempAlarmDb tempAlarmDb =  JSON.parseObject(onMessageJson, TempAlarmDb.class);
                insertTempDb(tempAlarmDb);
            } else if (alarmType == AlarmTypeEntity.ALARM_MASK) {
                MaskListAlarmDb maskAlarmDb = JSON.parseObject(onMessageJson, MaskListAlarmDb.class);
                insertMaskDb(maskAlarmDb);
            }
        });
    }

    private void insertAreaDb(AreaMontiorAlarmDb alarmDb) {
        // 解码图片写入到文件中，并将路径存入数据库
        Long time = alarmDb.getTime();
        String image = alarmDb.getAlarmImagePath();
        String areaMonitorImagePath = FileConstant.getInstance().getPathAlarmAreaImage();
        String areaImagePath = areaMonitorImagePath + time + ".jpg";
        ImageEngineUtils.createImageEngine().decodeImage(image, areaImagePath);
        alarmDb.setAlarmImagePath(areaImagePath);
        DatabaseManager.getInstance().getAreaMontiorAlarmDao().insert(alarmDb);
    }

    private void insertFaceDb(FaceListAlarmDb faceAlarmDb) {
        Long time = faceAlarmDb.getTime() == null ? System.currentTimeMillis() : faceAlarmDb.getTime();
        String image = faceAlarmDb.getImagePath();
        String captureImage = faceAlarmDb.getCapturePath();
        String faceImagePath = FileConstant.getInstance().getPathAlarmFaceImage();
        String imgPath = faceImagePath + "A" + time + ".jpg";
        String captureImgPath = faceImagePath + "B" + time + ".jpg";
        ImageEngineUtils.createImageEngine().decodeImage(image, imgPath);
        ImageEngineUtils.createImageEngine().decodeImage(captureImage, captureImgPath);
        faceAlarmDb.setImagePath(imgPath);
        faceAlarmDb.setCapturePath(captureImgPath);
        DatabaseManager.getInstance().getFaceAlarmDao().insert(faceAlarmDb);
    }

    private void insertTempDb(TempAlarmDb tempAlarmDb) {
        String alarmSavePath = FileConstant.getInstance().getPathAlarmTempImage();
        String liveImgbytes = tempAlarmDb.getLiveImgbytes();// 红外
        String tempImgbytes = tempAlarmDb.getTempImgbytes();
        Long timeMillis = tempAlarmDb.getTime();
        String liveImgPath = alarmSavePath + "A" + timeMillis + ".jpg";
        String tempImgPath = alarmSavePath + "B" + timeMillis + ".jpg";
        ImageEngineUtils.createImageEngine().decodeImage(liveImgbytes, liveImgPath);
        ImageEngineUtils.createImageEngine().decodeImage(tempImgbytes, tempImgPath);

        tempAlarmDb.setLiveImgbytes(liveImgPath);
        tempAlarmDb.setTempImgbytes(tempImgPath);
        DatabaseManager.getInstance().getTemoAlarmDao().insert(tempAlarmDb);
    }

    private void insertMaskDb(MaskListAlarmDb maskAlarmDb) {
        Long time = maskAlarmDb.getTime() == null ? System.currentTimeMillis() : maskAlarmDb.getTime();
        // 口罩检测根路径
        String maskImageRootPath = FileConstant.getInstance().getPathAlarmMaskImage();
        String maskFilePath = maskImageRootPath + time + ".jpg";
        String sourceImagePic = maskAlarmDb.getImagePath();
        ImageEngineUtils.createImageEngine().decodeImage(sourceImagePic, maskFilePath);
        maskAlarmDb.setImagePath(maskFilePath);
        DatabaseManager.getInstance().getMaskAlarmDao().insert(maskAlarmDb);
    }

    public void showAlarmFloat(String onMessage, Activity context) {
        if (!AppHolder.getInstance().getActivityRecord().currentIsParamsActivity(MainActivity.class)
                || context.isFinishing() || AlarmPopupWindow.isShowAlarm) return;
        AlarmPopupWindow popupWindow = new AlarmPopupWindow(context,context.getWindow().getDecorView(),onMessage);
        popupWindow.showAsPopupWindow();
    }
}
