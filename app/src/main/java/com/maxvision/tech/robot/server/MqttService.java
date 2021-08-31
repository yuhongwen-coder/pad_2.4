package com.maxvision.tech.robot.server;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.maxvision.tech.mqtt.ContextCallbackInterface;
import com.maxvision.tech.mqtt.MQTTManager;
import com.maxvision.tech.mqtt.MessageHandlerCallBack;
import com.maxvision.tech.mqtt.entity.AlarmSwitchEntity;
import com.maxvision.tech.mqtt.entity.BaseTaskEntity;
import com.maxvision.tech.mqtt.entity.CallDealEntity;
import com.maxvision.tech.mqtt.entity.ControlEntity;
import com.maxvision.tech.mqtt.entity.DialogueEntity;
import com.maxvision.tech.mqtt.entity.DoorControlEntity;
import com.maxvision.tech.mqtt.entity.Heart;
import com.maxvision.tech.mqtt.entity.LocationEntity;
import com.maxvision.tech.mqtt.entity.MapEntity;
import com.maxvision.tech.mqtt.entity.NavigationEntity;
import com.maxvision.tech.mqtt.entity.RemoteVideoEntity;
import com.maxvision.tech.mqtt.entity.VideoCallEntity;
import com.maxvision.tech.robot.MainActivity;
import com.maxvision.tech.robot.R;
import org.greenrobot.eventbus.EventBus;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import static com.maxvision.tech.mqtt.utils.JsonUtils.jsonToArrayList;


/**
 * name: wy
 * date: 2021/3/30
 * desc:
 */
public class MqttService extends Service implements MessageHandlerCallBack {

    public static boolean serviceIsLive;

    private ExecutorService executor;
    private IBinder binder;
    private ContextCallbackInterface callbackInterface;
    private final Gson gson = new Gson();

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("wangyin_service", "onCreate");
        executor = Executors.newCachedThreadPool();
        binder = new LocalBinder();

        Notification notification = createForegroundNotification();
        startForeground(100, notification);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        MqttService.serviceIsLive = true;
        //绑定是调用
        if (!MQTTManager.getInstance().isConnected()) {
            MQTTManager.getInstance().connect();
            MQTTManager.getInstance().setMessageHandlerCallBack(this);
            Log.i("wangyin_service", "onStartCommand MQTT未连接");
        }
        Log.i("wangyin_service", "onStartCommand MQTT已连接");
        return super.onStartCommand(intent, flags, startId);
    }

    public void setCallbackInterface(ContextCallbackInterface callbackInterface) {
        this.callbackInterface = callbackInterface;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.i("wangyin_service", "onBind");
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i("wangyin_service", "onUnbind");
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("wangyin_service", "onDestroy");
        MqttService.serviceIsLive = false;
        MQTTManager.getInstance().disconnect();
        // 移除通知
        stopForeground(true);
    }

    public class LocalBinder extends Binder {
        public MqttService getService() {
            return MqttService.this;
        }
    }

    private void subscribe() {
        executor.execute(() -> {
            //接收心跳
            MQTTManager.getInstance().subscribeMsg(getSubscribe(R.string.mq_pad_heart,null));
            //接收获取地图
            MQTTManager.getInstance().subscribeMsg(getSubscribe(R.string.mq_sn_pad_set_map, MQTTManager.PAD_SN));
            //接收导航点数据
            MQTTManager.getInstance().subscribeMsg(getSubscribe(R.string.mq_sn_pad_set_navigation, MQTTManager.PAD_SN));
            //接收当前机器人实时位置
            MQTTManager.getInstance().subscribeMsg(getSubscribe(R.string.mq_pad_location,null));
            //接收报警推送
            MQTTManager.getInstance().subscribeMsg(getSubscribe(R.string.mq_pad_alarm,null));
            //接收人脸上传返回结果
            MQTTManager.getInstance().subscribeMsg(getSubscribe(R.string.mq_sn_pad_upload_return, MQTTManager.PAD_SN));
            //接收机器人呼叫终端
            MQTTManager.getInstance().subscribeMsg(getSubscribe(R.string.mq_pad_call,null));
            //接收任务列表
            MQTTManager.getInstance().subscribeMsg(getSubscribe(R.string.mq_sn_pad_set_task, MQTTManager.PAD_SN));
            //接收任务控制返回结果
            MQTTManager.getInstance().subscribeMsg(getSubscribe(R.string.mq_sn_pad_task_return, MQTTManager.PAD_SN));
            //接收终端处理来电结果
            MQTTManager.getInstance().subscribeMsg(getSubscribe(R.string.mq_pad_call_deal,null));
            // 算法返回
            MQTTManager.getInstance().subscribeMsg(getSubscribe(R.string.mq_sn_pad_control_function_return, MQTTManager.PAD_SN));
            //智能对话
            MQTTManager.getInstance().subscribeMsg(getSubscribe(R.string.mq_sn_pad_set_dialogue, MQTTManager.PAD_SN));
        });
    }

    //数据回调
    @Override
    public void messageSuccess(String topicName, String message) {
        executor.execute(() -> {
            try {
                onMessage(topicName, message);
            } catch (Exception e) {
                Log.e("mqtt","mqtt onMessage exception =" + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    @Override
    public void connectSuccess() {
        subscribe();
    }

    /**
     * 平板端
     */
    private void onMessage(String topicName, String message) {
        if (callbackInterface == null) return;
        Log.i("wangyin_service", " topicName:" + topicName + " 收到Map真实数据：" + message);
        if (topicName.equals(getSubscribe(R.string.mq_pad_heart,null))) {
            //心跳
            Heart heart = gson.fromJson(message, Heart.class);
            if(heart == null) return;
//            Log.e("ZJJHEART", message);
            callbackInterface.setHeart(heart);
        } else if (topicName.equals(getSubscribe(R.string.mq_sn_pad_set_map, MQTTManager.PAD_SN))) {
            //地图
            Log.i("wangyin_service", "收到Map真实数据：" + message);
            MapEntity data = gson.fromJson(message, MapEntity.class);
            callbackInterface.setMap(data);
        } else if (topicName.equals(getSubscribe(R.string.mq_sn_pad_set_navigation, MQTTManager.PAD_SN))) {
            //导航点数据
            NavigationEntity data = gson.fromJson(message, NavigationEntity.class);
            callbackInterface.setNavigation(data);
        } else if (topicName.equals(getSubscribe(R.string.mq_pad_location,null))) {
            //机器人实时位置
            LocationEntity data = gson.fromJson(message, LocationEntity.class);
            callbackInterface.setLocation(data);
        } else if (topicName.equals(getSubscribe(R.string.mq_pad_alarm,null))) {
            //报警推送
            callbackInterface.onNotifyAlarm(message);
        } else if(topicName.equals(getSubscribe(R.string.mq_sn_pad_upload_return,MQTTManager.PAD_SN))){
            //人脸返回结果 1 成功 0 失败
            EventBus.getDefault().post(message);
        } else if (topicName.equals(getSubscribe(R.string.mq_pad_call,null))) {
            //接收机器人呼叫终端
            //机器人实时位置
            VideoCallEntity data = gson.fromJson(message, VideoCallEntity.class);
            callbackInterface.setVideo(data);
        } else if (topicName.equals(getSubscribe(R.string.mq_sn_pad_set_task, MQTTManager.PAD_SN))) {
            //接收任务列表
            JsonObject jsonObject = JsonParser.parseString(message).getAsJsonObject();
            String robotSn = jsonObject.get("robotSn").getAsString();
            String json = jsonObject.get("taskList").getAsString();
            List<BaseTaskEntity> baseTaskEntity= jsonToArrayList(json,BaseTaskEntity.class);
            callbackInterface.setTaskList(robotSn,baseTaskEntity);
        } else if (topicName.equals(getSubscribe(R.string.mq_sn_pad_task_return, MQTTManager.PAD_SN))) {
            //接收任务控制返回结果
            Log.e("ZJJRESPONSE 接收",message);
            callbackInterface.setControlResponse(message);
        } else if (topicName.equals(getSubscribe(R.string.mq_pad_call_deal,null))) {
            //终端处理了来电
            CallDealEntity data = gson.fromJson(message, CallDealEntity.class);
            callbackInterface.setCallDeal(data);
        } else if (topicName.equals(getSubscribe(R.string.mq_sn_pad_control_function_return,MQTTManager.PAD_SN))) {
            // 算法控制结果
            AlarmSwitchEntity switchEntity = gson.fromJson(message, AlarmSwitchEntity.class);
            EventBus.getDefault().post(switchEntity);
        }else if (topicName.equals(getSubscribe(R.string.mq_sn_pad_set_dialogue,MQTTManager.PAD_SN))) {
            // 智能对话
            DialogueEntity dialogueEntity = gson.fromJson(message, DialogueEntity.class);
            EventBus.getDefault().post(dialogueEntity);
        }
    }

    /**
     * 获取Map
     *
     * @param sn    机器人SN号
     * @param mapId 地图ID
     */
    public void getMap(String sn, String mapId) {
        executor.execute(() -> {
            MapEntity mapEntity = new MapEntity();
            mapEntity.mapId = mapId;
            mapEntity.sn = MQTTManager.PAD_SN;
            MQTTManager.getInstance().publish(getPublish(R.string.mq_sn_rob_get_map, sn), gson.toJson(mapEntity));
        });
    }


    /**
     * 获取导航点数据
     *
     * @param sn    机器人SN号
     * @param padSn 平板SN
     */
    public void getNavigation(String sn, String padSn) {
        executor.execute(() -> {
            MQTTManager.getInstance().publish(getPublish(R.string.mq_sn_rob_get_navigation, sn), padSn);
        });
    }

    /**
     * 人脸上传
     *
     * @param sn   机器人SN号
     * @param data 数据
     */
    public void getUploadFace(String sn, String data) {
        executor.execute(() -> {
            MQTTManager.getInstance().publish(getPublish(R.string.mq_sn_rob_upload_face, sn), data);
        });
    }


    /**
     * 机器人运动控制
     *
     * @param sn   机器人SN号
     * @param type 控制类型
     * @param data 指令数据
     */
    public void getControl(String sn, int type, int data) {
        executor.execute(() -> {
            ControlEntity control = new ControlEntity();
            control.type = type;
            control.data = data;
            MQTTManager.getInstance().publish(getPublish(R.string.mq_sn_rob_control, sn), gson.toJson(control));
        });
    }

    /**
     * 查看视频
     *
     * @param sn     机器人SN号
     * @param padsn  平板SN号
     * @param type   类型  1：音频视频全开（默认模式）  2：音频关 视频开（智能对话关闭音频模式）
     * @param roomId 房间号
     */
    public void getLookVideo(String sn, String padsn, int type, String roomId) {
        executor.execute(() -> {
            RemoteVideoEntity remoteVideoEntity = new RemoteVideoEntity();
            remoteVideoEntity.roomId = roomId;
            remoteVideoEntity.sn = padsn;
            remoteVideoEntity.type = type;
            MQTTManager.getInstance().publish(getPublish(R.string.mq_sn_rob_p2p, sn), gson.toJson(remoteVideoEntity));
        });
    }

    /**
     * 获取任务列表
     *
     * @param sn   机器人SN号
     * @param data 数据
     */
    public void getTaskList(String sn, String data) {
        executor.execute(() -> {
            MQTTManager.getInstance().publish(getPublish(R.string.mq_sn_rob_get_task, sn), data);
        });
    }

    /**
     * 智能对话
     *
     * @param sn   机器人SN号
     * @param data 数据
     */
    public void setDialogue(String sn, DialogueEntity data) {
        executor.execute(() -> {
            MQTTManager.getInstance().publish(getPublish(R.string.mq_sn_pad_get_dialogue, sn), gson.toJson(data));
        });
    }


    /**
     * 任务调度
     *
     * @param sn   机器人SN号
     * @param data 数据
     */
    public void getTaskControl(String sn, String data) {
        executor.execute(() -> {
            MQTTManager.getInstance().publish(getPublish(R.string.mq_sn_rob_task_control, sn), data);
        });
    }


    /**
     * padSn 终端处理了来电
     *
     * @param robotsn 机器人SN号
     */
    public void publishCallDeal(String padSn, String robotsn, int deal) {
        executor.execute(() -> {
            CallDealEntity callDealEntity = new CallDealEntity();
            callDealEntity.padSn = padSn;
            callDealEntity.robotSn = robotsn;
            callDealEntity.deal = deal;
            MQTTManager.getInstance().publish(getPublish(R.string.mq_pad_call_deal,null), gson.toJson(callDealEntity));
        });
    }

    /**
     * 算法发布开关状态
     * @param robotSn 机器人sn
     * @param json 机器人推送通过这个sn定向推或者不定向推
     */
    public void publishFunctionState(String robotSn,String json) {
        executor.execute(() -> {
            MQTTManager.getInstance().publish(getPublish(R.string.mq_sn_pad_control_function, robotSn), json);
        });
    }

    /**
     * 平板向机器人推送门功能
     * @param robotSn 机器人sn
     * @param doorType 推送信息
     */
    public void publishDoorFun(String robotSn,int doorType) {
        DoorControlEntity doorControlEntity = new DoorControlEntity(doorType,MQTTManager.PAD_SN);
        executor.execute(() -> MQTTManager.getInstance().publish(getPublish(R.string.mq_sn_pad_control_door, robotSn),
                new Gson().toJson(doorControlEntity)));
    }

    /**
     * 创建服务通知
     */
    private Notification createForegroundNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // 唯一的通知通道的id.
        String notificationChannelId = "notification_channel_id_01";

        // Android8.0以上的系统，新建消息通道
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //用户可见的通道名称
            String channelName = "Foreground Service Notification";
            //通道的重要程度
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel notificationChannel = new NotificationChannel(notificationChannelId, channelName, importance);
            notificationChannel.setDescription("Channel description");
            //LED灯
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            //震动
            notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            notificationChannel.enableVibration(true);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, notificationChannelId);
        //通知小图标
        builder.setSmallIcon(R.drawable.app_logo);
        //通知标题
        builder.setContentTitle("机器人平板终端");
        //通知内容
        builder.setContentText("正在运行中...");
        //设定通知显示的时间
        builder.setWhen(System.currentTimeMillis());
        //设定启动的内容
        Intent activityIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, activityIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);

        //创建通知并返回
        return builder.build();
    }

    private String getSubscribe(int id,String obj){
        if (null == obj) {
            return getString(id,MQTTManager.AREA);
        }
        return getString(id,MQTTManager.AREA,obj);
    }
    private String getPublish(int rids,String obj){
        if (null == obj) {
            return getString(rids,MQTTManager.AREA);
        }
        return getString(rids,MQTTManager.AREA,obj);
    }
}