package com.maxvision.tech.robot;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.maxvision.tech.mqtt.ContextCallbackInterface;
import com.maxvision.tech.mqtt.entity.BaseTaskEntity;
import com.maxvision.tech.mqtt.entity.CallDealEntity;
import com.maxvision.tech.mqtt.entity.Heart;
import com.maxvision.tech.mqtt.entity.LocationEntity;
import com.maxvision.tech.mqtt.entity.MapEntity;
import com.maxvision.tech.mqtt.entity.NavigationEntity;
import com.maxvision.tech.mqtt.entity.TaskControlResponse;
import com.maxvision.tech.mqtt.entity.VideoCallEntity;
import com.maxvision.tech.mqtt.entity.XyTaskControlResponse;
import com.maxvision.tech.mqtt.entity.state.PadIntercomState;
import com.maxvision.tech.mqtt.utils.BitmapUtils;
import com.maxvision.tech.robot.alarm.NotifyManager;
import com.maxvision.tech.robot.entity.event.ChargeStateEvent;
import com.maxvision.tech.robot.entity.event.HangEvent;
import com.maxvision.tech.robot.entity.event.RobotEvent;
import com.maxvision.tech.robot.manager.RobotDataManager;
import com.maxvision.tech.robot.map.PointEntity;
import com.maxvision.tech.robot.server.MqttService;
import com.maxvision.tech.robot.ui.dialog.WaitingDialogFragment;
import com.maxvision.tech.robot.ui.fragment.NavigationFragment;
import com.maxvision.tech.robot.ui.view.MyConstraintLayout;
import com.maxvision.tech.robot.ui.view.RobotFrameLayout;
import com.maxvision.tech.robot.utils.CustomToast;
import com.maxvision.tech.robot.utils.FileConstant;
import com.maxvision.tech.robot.ui.activity.AnswerActivity;
import com.maxvision.tech.webrtc.manager.WebRTCManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements ContextCallbackInterface, MyConstraintLayout.OnClickRobotListener {

    private RobotFrameLayout fl_robot;

    private final Map<String, NavigationFragment> fragmentMap = new LinkedHashMap<>();

    private NavigationFragment currentFragment;
    private ImageView iv_jt;
    //????????????????????????
    private boolean isUnfold = true;
    private LinearLayout ll_unfold;
    //???????????????????????????sn
    public String mSn = "";
    private HeadsetPlugReceiver headsetPlugReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EventBus.getDefault().register(this);

        fl_robot = findViewById(R.id.fl_robot);
        iv_jt = findViewById(R.id.iv_jt);
        ll_unfold = findViewById(R.id.ll_unfold);

        MqttService service = AppHolder.getInstance().getMqtt();
        if (service != null) {
            service.setCallbackInterface(MainActivity.this);
        }
        ll_unfold.setOnClickListener(v -> {
            setRobotVisibility(isUnfold);
        });
        fl_robot.setOnClickRobotListener(this);
        loadRobot();

        headsetPlugReceiver = new HeadsetPlugReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_HEADSET_PLUG);
        registerReceiver(headsetPlugReceiver, filter);

    }

    @Override
    protected void onResume() {
        super.onResume();
        fl_robot.setCallVisibility();
    }

    private void defaultSelectRobot() {
        List<Heart> list = RobotDataManager.getInstance().getSelectRobot();
        if (list.size() == 0) {
            if (currentFragment == null) return;
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.hide(currentFragment);
            transaction.commitAllowingStateLoss();
            return;
        }
        onClickRobotItem(list.get(0));
    }

    private void loadRobot() {
        List<Heart> list = RobotDataManager.getInstance().getSelectRobot();
        fl_robot.clearRobot();
        for (Heart heart : list) {
            fl_robot.addRobot(heart);
        }
        fl_robot.postDelayed(this::defaultSelectRobot, 500);
    }

    private void selectRobot(String sn) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        NavigationFragment targetFragment;
        NavigationFragment f = fragmentMap.get(sn);
        if (f == null) {
            targetFragment = new NavigationFragment();
            Bundle bundle = new Bundle();
            bundle.putString("sn", sn);
            targetFragment.setArguments(bundle);
            fragmentMap.put(sn, targetFragment);
        } else {
            targetFragment = f;
        }
        if (!targetFragment.isAdded()) {
            if (currentFragment != null) {
                transaction.hide(currentFragment);
            }
            transaction.add(R.id.frameLayout, targetFragment);
            transaction.commitAllowingStateLoss();
        } else {
            if (currentFragment != null) {
                transaction.hide(currentFragment);
            }
            transaction.show(targetFragment);
            transaction.commitAllowingStateLoss();
        }
        currentFragment = targetFragment;
    }

    //??????
    @Override
    public void setHeart(Heart heart) {
        //??????????????????
        EventBus.getDefault().post(new ChargeStateEvent(heart.sn, heart.bat, heart.chargeState));
        //????????????
        EventBus.getDefault().post(heart);
        //????????????????????????????????????true
        heart.isLine = true;
        RobotDataManager.getInstance().add(heart);
        //????????????
        fl_robot.setStateContent(heart);
        //??????????????????????????????
        NavigationFragment fragment = fragmentMap.get(heart.sn);
        if (fragment == null) return;
        if (fragment.isHidden()) return;
        fragment.setData(heart);
    }

    //????????????????????????
    @Override
    public void setNavigation(NavigationEntity navigation) {
        //???????????????
        EventBus.getDefault().post(navigation);
        List<PointEntity> list = new ArrayList<>();
        for (NavigationEntity.ListData data : navigation.listData) {
            if (data.nType == 1) {
                list.add(new PointEntity(data.nX, data.nY, data.angle,data.nName));
            }
        }
        NavigationFragment fragment = fragmentMap.get(navigation.sn);
        if (fragment == null) return;
        fragment.addNavigationList(list);
    }

    //?????????????????????
    @Override
    public void setLocation(LocationEntity navigation) {
        //???????????????
        EventBus.getDefault().post(navigation);
        fl_robot.despatchCall(navigation);
        NavigationFragment fragment = fragmentMap.get(navigation.sn);
        if (fragment == null) return;
        if (fragment.isHidden()) return;
        fragment.updatePostion(navigation.x, navigation.y);
    }

    @Override
    public void setMap(MapEntity data) {
        //??????????????????
        String path = FileConstant.getInstance().getPathMapImage() + data.mapId + ".png";
        BitmapUtils.saveBitmapFile(path, BitmapUtils.base64ToBitmap(data.image));
        NavigationFragment fragment = fragmentMap.get(data.sn);
        if (fragment == null) return;
        fl_robot.post(() -> fragment.setMap(path));
    }

    //??????????????????
    @Override
    public void setVideo(VideoCallEntity data) {
        Heart heart = RobotDataManager.getInstance().get(data.sn);
        //???????????????????????????????????? ???????????????????????????????????????????????????
        if (!heart.isSava || AppHolder.getInstance().getPadCallState() != PadIntercomState.INTERCOM_NULL) {
            return;
        }
        AnswerActivity.startActivity(this, data.sn, data.roomId);
        Log.d("qs_qs", " setVideo");
    }

    @Override
    public synchronized void onNotifyAlarm(String message) {
        runOnUiThread(() -> {
            JSONObject jsonObject = (JSONObject) JSON.parse(message);
            if (jsonObject == null) return;
            String robotSn = (String) jsonObject.get("robotSn");
            if (TextUtils.isEmpty(robotSn)) return;
            // ???????????????????????????
            NotifyManager.getInstance().insertData(message);
            // ????????????????????????
            Heart heart = RobotDataManager.getInstance().get(robotSn);
            if (heart == null) return;
            Log.i("yhw_alarm", "onNotifyAlarm sn = " + heart.sn + "  is add " + heart.isSava + "  is online = " + heart.isLine);
            if (!heart.isSava || !heart.isLine) return;
            heart.isAlarm = true;
            fl_robot.setLineState(heart);
            NotifyManager.getInstance().showAlarmFloat(message, MainActivity.this);
        });
    }

    @Override
    public void setTaskList(String robotSn, List<BaseTaskEntity> task) {
        Log.e("ZJJTASK", "???????????????:" + task.size());
        RobotDataManager.getInstance().setTaskList(robotSn, task);
    }

    @Override
    public void setControlResponse(String notifyControlJson) {
        // ????????????????????????
        JsonObject jsonObject = JsonParser.parseString(notifyControlJson).getAsJsonObject();
        if (jsonObject == null) return;
        JsonElement element = jsonObject.get("sn");
        String robotSn = "";
        if (element != null && element.getAsString() != null) {
            robotSn = element.getAsString();
        }
        if (!TextUtils.isEmpty(robotSn)) {
            Heart heart = RobotDataManager.getInstance().get(robotSn);
            if (heart != null && TextUtils.equals(heart.type, "2") && heart.xyMode == 0) {
                XyTaskControlResponse data = new Gson().fromJson(notifyControlJson, XyTaskControlResponse.class);
                RobotDataManager.getInstance().notifyUpdateSingleTask(data);
                return;
            }
        }
        TaskControlResponse data = new Gson().fromJson(notifyControlJson, TaskControlResponse.class);
        if (200 != data.code) {
            CustomToast.toastLong(CustomToast.TIP_ERROR, data.msg);
        } else {
            if (!TextUtils.isEmpty(data.taskId)) {
                RobotDataManager.getInstance().updateTaskList(data);
            }
            CustomToast.toastLong(CustomToast.TIP_SUCCESS, data.msg);
        }
    }

    //??????????????????????????????
    @Override
    public void setCallDeal(CallDealEntity data) {
        EventBus.getDefault().post(data);
    }

    //????????????????????????
    @Override
    public void onClickRobotItem(Heart heart) {
        //??????????????? ??????????????????
        if (!mSn.equals(heart.sn) && AppHolder.getInstance().getPadCallState() != PadIntercomState.INTERCOM_NULL) {
            //????????????
            WaitingDialogFragment.getInstance(getString(R.string.tip_hanging)).show(getSupportFragmentManager());
            EventBus.getDefault().post(new HangEvent(mSn));
        }
        fl_robot.clearBackground(heart.sn);
        selectRobot(heart.sn);
        fl_robot.setMarquee(heart.sn);
        mSn = heart.sn;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(RobotEvent event) {
        switch (event.type) {
            case RobotEvent.UPDATE_ROBOT: //??????
                loadRobot();
                break;
            case RobotEvent.UPDATE_LINE_STATE: //??????????????????
                Heart h = (Heart) event.object;
                if (h == null) return;
                if (!h.isSava) return;
                fl_robot.addRobot(h);
                break;
        }
    }

    private void setRobotVisibility(boolean isFold) {
        isUnfold = !isFold;
        iv_jt.setRotation(isUnfold ? 0 : 180);
        fl_robot.setVisibility(isUnfold ? View.VISIBLE : View.GONE);
    }

    public void setFullVideo(boolean isFull) {
        setRobotVisibility(isFull);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AppHolder.getInstance().stopMqttService();
        EventBus.getDefault().unregister(this);
        try {
            if (headsetPlugReceiver != null) {
                unregisterReceiver(headsetPlugReceiver);
            }
        } catch (Exception e) {
        }
    }

    //??????????????????
    private class HeadsetPlugReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra("state")) {
                if (intent.getIntExtra("state", 0) == 0) { //????????????
                    WebRTCManager.getInstance().toggleHeadset(false);
                } else if (intent.getIntExtra("state", 0) == 1) { //????????????
                    WebRTCManager.getInstance().toggleHeadset(true);
                }
            }
        }
    }

     /*public void test(View view) {
        Toast.makeText(this, "padCallState???" + AppHolder.getInstance().getPadCallState(), Toast.LENGTH_SHORT).show();
    }*/

}