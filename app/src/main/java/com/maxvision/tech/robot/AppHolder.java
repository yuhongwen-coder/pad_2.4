package com.maxvision.tech.robot;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.cl.voice.VoiceSDK;
import com.cl.voice.VoiceSDKConfig;
import com.maxvision.tech.mqtt.MQTTManager;
import com.maxvision.tech.mqtt.entity.state.PadIntercomState;
import com.maxvision.tech.robot.server.MqttService;
import com.maxvision.tech.robot.entity.cons.SpConstants;
import com.maxvision.tech.robot.manager.RobotDataManager;
import com.maxvision.tech.robot.utils.ActivityLifeRecord;
import com.maxvision.tech.robot.utils.FileConstant;
import com.maxvision.tech.robot.utils.SpUtils;
import com.maxvision.tech.webrtc.manager.WebrtcUtil;
import com.tencent.bugly.crashreport.CrashReport;

/**
 * name: wy
 * date: 2021/3/30
 * desc:
 */
public class AppHolder extends Application {

    private static AppHolder instance;
    //平板端状态
    private int padCallState = PadIntercomState.INTERCOM_NULL;
    private MqttService mqttService;
    private ActivityLifeRecord activityRecord;

    public static synchronized AppHolder getInstance() {
        return instance;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        String ip = "tcp://"+ SpUtils.getString(SpConstants.SP_MQTT_IP,"open.maxvision.com.cn")+":"+SpUtils.getString(SpConstants.SP_MQTT_PORT,"1883");
        MQTTManager.getInstance().setIP(ip);
        // 设置webRtc地址
        String webRtcIp = SpUtils.getString(SpConstants.SP_WEBRTC_IP,"47.111.68.11");
        String webRtcPort = SpUtils.getString(SpConstants.SP_WEBRTC_PORT,"3000");
        WebrtcUtil.setIpAndPort(webRtcIp,webRtcPort);

        //FitUtils.setCustomDensity(this,960);
        FileConstant.getInstance().initPath(this);

        RobotDataManager.getInstance().startTimer();
//        CrashReport.initCrashReport(this, BuildConfig.BUGLY_APP_ID, true);
        activityRecord = new ActivityLifeRecord();
        registerActivityLifecycleCallbacks(activityRecord);

        getMakIp();
        startServer();
        initVoice();
    }



    public void initVoice(){
        VoiceSDKConfig.getConfig()
                .setDebug("translation", false, false)
                .xfKey("5e9d71b3")
                .xWrKey("299785965373486f9fdfc23747c4a674")
                .xVoiceParameters(30, 60 * 1000, 60 * 1000)   //端点检测语音识别长度，后端点设置
                .xInternalAlgorithm(false)   //true开启算法端点检测   false开启讯飞端点检测
                .xBaiduParameter("4","16731546","yGLcP0ovgAXY87tvAMSvlxf2","Ingft0FtdaEnc4KR2T7wE1WjGfy2Soli")//百度合成
                .xEncodingType(2)     //编码类型   1--三字码    2---code码    默认三字码
                .xPronunciationPeople("xiaoyan");     //发音人 小燕：xiaoyan   小婧:aisjinger
        VoiceSDK.initSdk(AppHolder.getInstance());
    }

    private void getMakIp(){
        String sn = SpUtils.getString(SpConstants.SP_PAD_SN,"");
        if(TextUtils.isEmpty(sn)){
            String s = String.valueOf(System.currentTimeMillis());
            SpUtils.putString(SpConstants.SP_PAD_SN,s);
            MQTTManager.getInstance().setPadSn(s);
            MQTTManager.getInstance().setArea(BuildConfig.FLAVOR);
           return;
        }
        MQTTManager.getInstance().setPadSn(sn);
        MQTTManager.getInstance().setArea(BuildConfig.FLAVOR);
        Log.e("MQTTManager","MAK唯一标识："+sn);
    }


    public MqttService getMqtt(){
        return mqttService;
    }

    private void startServer(){
        /*Intent intent = new Intent(this, MqttService.class);
        startService(intent);
        bindService(intent,new MqttServiceConnection() , Context.BIND_AUTO_CREATE);*/
        //启动服务
        if (!MqttService.serviceIsLive) {
            // Android 8.0使用startForegroundService在前台启动新服务
            Intent intent = new Intent(this, MqttService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent);
            } else {
                startService(intent);
            }
            bindService(intent,new MqttServiceConnection() , Context.BIND_AUTO_CREATE);
        } else {
            Toast.makeText(this, "前台服务正在运行中...", Toast.LENGTH_SHORT).show();
        }
    }


    private final class MqttServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mqttService = ((MqttService.LocalBinder) service).getService();
            Log.i("wangyin_service","MainActivity 已连接");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i("wangyin_service","MainActivity 断开");
            startServer();
        }
    }

    public void stopMqttService(){
        stopService(new Intent(this, MqttService.class));
    }

    public ActivityLifeRecord getActivityRecord() {
        return activityRecord;
    }

    public int getPadCallState() {
        return padCallState;
    }

    public void setPadCallState(int state) {
        padCallState = state;
    }

}