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
        //???????????????
        if (!MQTTManager.getInstance().isConnected()) {
            MQTTManager.getInstance().connect();
            MQTTManager.getInstance().setMessageHandlerCallBack(this);
            Log.i("wangyin_service", "onStartCommand MQTT?????????");
        }
        Log.i("wangyin_service", "onStartCommand MQTT?????????");
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
        // ????????????
        stopForeground(true);
    }

    public class LocalBinder extends Binder {
        public MqttService getService() {
            return MqttService.this;
        }
    }

    private void subscribe() {
        executor.execute(() -> {
            //????????????
            MQTTManager.getInstance().subscribeMsg(getSubscribe(R.string.mq_pad_heart,null));
            //??????????????????
            MQTTManager.getInstance().subscribeMsg(getSubscribe(R.string.mq_sn_pad_set_map, MQTTManager.PAD_SN));
            //?????????????????????
            MQTTManager.getInstance().subscribeMsg(getSubscribe(R.string.mq_sn_pad_set_navigation, MQTTManager.PAD_SN));
            //?????????????????????????????????
            MQTTManager.getInstance().subscribeMsg(getSubscribe(R.string.mq_pad_location,null));
            //??????????????????
            MQTTManager.getInstance().subscribeMsg(getSubscribe(R.string.mq_pad_alarm,null));
            //??????????????????????????????
            MQTTManager.getInstance().subscribeMsg(getSubscribe(R.string.mq_sn_pad_upload_return, MQTTManager.PAD_SN));
            //???????????????????????????
            MQTTManager.getInstance().subscribeMsg(getSubscribe(R.string.mq_pad_call,null));
            //??????????????????
            MQTTManager.getInstance().subscribeMsg(getSubscribe(R.string.mq_sn_pad_set_task, MQTTManager.PAD_SN));
            //??????????????????????????????
            MQTTManager.getInstance().subscribeMsg(getSubscribe(R.string.mq_sn_pad_task_return, MQTTManager.PAD_SN));
            //??????????????????????????????
            MQTTManager.getInstance().subscribeMsg(getSubscribe(R.string.mq_pad_call_deal,null));
            // ????????????
            MQTTManager.getInstance().subscribeMsg(getSubscribe(R.string.mq_sn_pad_control_function_return, MQTTManager.PAD_SN));
            //????????????
            MQTTManager.getInstance().subscribeMsg(getSubscribe(R.string.mq_sn_pad_set_dialogue, MQTTManager.PAD_SN));
        });
    }

    //????????????
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
     * ?????????
     */
    private void onMessage(String topicName, String message) {
        if (callbackInterface == null) return;
        Log.i("wangyin_service", " topicName:" + topicName + " ??????Map???????????????" + message);
        if (topicName.equals(getSubscribe(R.string.mq_pad_heart,null))) {
            //??????
            Heart heart = gson.fromJson(message, Heart.class);
            if(heart == null) return;
//            Log.e("ZJJHEART", message);
            callbackInterface.setHeart(heart);
        } else if (topicName.equals(getSubscribe(R.string.mq_sn_pad_set_map, MQTTManager.PAD_SN))) {
            //??????
            Log.i("wangyin_service", "??????Map???????????????" + message);
            MapEntity data = gson.fromJson(message, MapEntity.class);
            callbackInterface.setMap(data);
        } else if (topicName.equals(getSubscribe(R.string.mq_sn_pad_set_navigation, MQTTManager.PAD_SN))) {
            //???????????????
            NavigationEntity data = gson.fromJson(message, NavigationEntity.class);
            callbackInterface.setNavigation(data);
        } else if (topicName.equals(getSubscribe(R.string.mq_pad_location,null))) {
            //?????????????????????
            LocationEntity data = gson.fromJson(message, LocationEntity.class);
            callbackInterface.setLocation(data);
        } else if (topicName.equals(getSubscribe(R.string.mq_pad_alarm,null))) {
            //????????????
            callbackInterface.onNotifyAlarm(message);
        } else if(topicName.equals(getSubscribe(R.string.mq_sn_pad_upload_return,MQTTManager.PAD_SN))){
            //?????????????????? 1 ?????? 0 ??????
            EventBus.getDefault().post(message);
        } else if (topicName.equals(getSubscribe(R.string.mq_pad_call,null))) {
            //???????????????????????????
            //?????????????????????
            VideoCallEntity data = gson.fromJson(message, VideoCallEntity.class);
            callbackInterface.setVideo(data);
        } else if (topicName.equals(getSubscribe(R.string.mq_sn_pad_set_task, MQTTManager.PAD_SN))) {
            //??????????????????
            JsonObject jsonObject = JsonParser.parseString(message).getAsJsonObject();
            String robotSn = jsonObject.get("robotSn").getAsString();
            String json = jsonObject.get("taskList").getAsString();
            List<BaseTaskEntity> baseTaskEntity= jsonToArrayList(json,BaseTaskEntity.class);
            callbackInterface.setTaskList(robotSn,baseTaskEntity);
        } else if (topicName.equals(getSubscribe(R.string.mq_sn_pad_task_return, MQTTManager.PAD_SN))) {
            //??????????????????????????????
            Log.e("ZJJRESPONSE ??????",message);
            callbackInterface.setControlResponse(message);
        } else if (topicName.equals(getSubscribe(R.string.mq_pad_call_deal,null))) {
            //?????????????????????
            CallDealEntity data = gson.fromJson(message, CallDealEntity.class);
            callbackInterface.setCallDeal(data);
        } else if (topicName.equals(getSubscribe(R.string.mq_sn_pad_control_function_return,MQTTManager.PAD_SN))) {
            // ??????????????????
            AlarmSwitchEntity switchEntity = gson.fromJson(message, AlarmSwitchEntity.class);
            EventBus.getDefault().post(switchEntity);
        }else if (topicName.equals(getSubscribe(R.string.mq_sn_pad_set_dialogue,MQTTManager.PAD_SN))) {
            // ????????????
            DialogueEntity dialogueEntity = gson.fromJson(message, DialogueEntity.class);
            EventBus.getDefault().post(dialogueEntity);
        }
    }

    /**
     * ??????Map
     *
     * @param sn    ?????????SN???
     * @param mapId ??????ID
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
     * ?????????????????????
     *
     * @param sn    ?????????SN???
     * @param padSn ??????SN
     */
    public void getNavigation(String sn, String padSn) {
        executor.execute(() -> {
            MQTTManager.getInstance().publish(getPublish(R.string.mq_sn_rob_get_navigation, sn), padSn);
        });
    }

    /**
     * ????????????
     *
     * @param sn   ?????????SN???
     * @param data ??????
     */
    public void getUploadFace(String sn, String data) {
        executor.execute(() -> {
            MQTTManager.getInstance().publish(getPublish(R.string.mq_sn_rob_upload_face, sn), data);
        });
    }


    /**
     * ?????????????????????
     *
     * @param sn   ?????????SN???
     * @param type ????????????
     * @param data ????????????
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
     * ????????????
     *
     * @param sn     ?????????SN???
     * @param padsn  ??????SN???
     * @param type   ??????  1???????????????????????????????????????  2???????????? ?????????????????????????????????????????????
     * @param roomId ?????????
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
     * ??????????????????
     *
     * @param sn   ?????????SN???
     * @param data ??????
     */
    public void getTaskList(String sn, String data) {
        executor.execute(() -> {
            MQTTManager.getInstance().publish(getPublish(R.string.mq_sn_rob_get_task, sn), data);
        });
    }

    /**
     * ????????????
     *
     * @param sn   ?????????SN???
     * @param data ??????
     */
    public void setDialogue(String sn, DialogueEntity data) {
        executor.execute(() -> {
            MQTTManager.getInstance().publish(getPublish(R.string.mq_sn_pad_get_dialogue, sn), gson.toJson(data));
        });
    }


    /**
     * ????????????
     *
     * @param sn   ?????????SN???
     * @param data ??????
     */
    public void getTaskControl(String sn, String data) {
        executor.execute(() -> {
            MQTTManager.getInstance().publish(getPublish(R.string.mq_sn_rob_task_control, sn), data);
        });
    }


    /**
     * padSn ?????????????????????
     *
     * @param robotsn ?????????SN???
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
     * ????????????????????????
     * @param robotSn ?????????sn
     * @param json ???????????????????????????sn???????????????????????????
     */
    public void publishFunctionState(String robotSn,String json) {
        executor.execute(() -> {
            MQTTManager.getInstance().publish(getPublish(R.string.mq_sn_pad_control_function, robotSn), json);
        });
    }

    /**
     * ?????????????????????????????????
     * @param robotSn ?????????sn
     * @param doorType ????????????
     */
    public void publishDoorFun(String robotSn,int doorType) {
        DoorControlEntity doorControlEntity = new DoorControlEntity(doorType,MQTTManager.PAD_SN);
        executor.execute(() -> MQTTManager.getInstance().publish(getPublish(R.string.mq_sn_pad_control_door, robotSn),
                new Gson().toJson(doorControlEntity)));
    }

    /**
     * ??????????????????
     */
    private Notification createForegroundNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // ????????????????????????id.
        String notificationChannelId = "notification_channel_id_01";

        // Android8.0????????????????????????????????????
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //???????????????????????????
            String channelName = "Foreground Service Notification";
            //?????????????????????
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel notificationChannel = new NotificationChannel(notificationChannelId, channelName, importance);
            notificationChannel.setDescription("Channel description");
            //LED???
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            //??????
            notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            notificationChannel.enableVibration(true);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, notificationChannelId);
        //???????????????
        builder.setSmallIcon(R.drawable.app_logo);
        //????????????
        builder.setContentTitle("?????????????????????");
        //????????????
        builder.setContentText("???????????????...");
        //???????????????????????????
        builder.setWhen(System.currentTimeMillis());
        //?????????????????????
        Intent activityIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, activityIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);

        //?????????????????????
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