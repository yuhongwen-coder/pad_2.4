package com.maxvision.tech.robot.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.maxvision.tech.mqtt.MQTTManager;
import com.maxvision.tech.mqtt.entity.Heart;
import com.maxvision.tech.mqtt.entity.LocationEntity;
import com.maxvision.tech.mqtt.entity.NavigationEntity;
import com.maxvision.tech.mqtt.entity.state.PadIntercomState;
import com.maxvision.tech.robot.AppHolder;
import com.maxvision.tech.robot.R;
import com.maxvision.tech.robot.config.CallConfig;
import com.maxvision.tech.robot.config.Function;
import com.maxvision.tech.robot.entity.event.ChargeStateEvent;
import com.maxvision.tech.robot.manager.RobotDataManager;
import com.maxvision.tech.robot.map.PointEntity;
import com.maxvision.tech.robot.ui.fragment.CallNavigationFragment;
import com.maxvision.tech.robot.ui.view.BatteryImageView;
import com.maxvision.tech.robot.utils.RobotUtils;
import com.maxvision.tech.webrtc.manager.IViewCallback;
import com.maxvision.tech.webrtc.manager.PeerConnectionHelper;
import com.maxvision.tech.webrtc.manager.ProxyVideoSink;
import com.maxvision.tech.webrtc.manager.WebRTCManager;
import com.maxvision.tech.webrtc.utils.PermissionUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.webrtc.EglBase;
import org.webrtc.MediaStream;
import org.webrtc.RendererCommon;
import org.webrtc.SurfaceViewRenderer;

import java.util.ArrayList;
import java.util.List;

import static com.maxvision.tech.robot.config.Function.FUNCTION_DEFAULT;
import static com.maxvision.tech.robot.config.Function.FUNCTION_XY;

/**
 * @author qs
 * @time 2021/4/13 19:30
 * @describe $ ????????????
 */
public class ChatingActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "qs_ChatingActivity";

    private View mViewBg;
    private SurfaceViewRenderer mSvrRemote;
    private ImageView mIvRobotImage;
    private TextView mTvRobotName;
    private BatteryImageView mIvRobotBattery;
    private TextView mTvRobotBattery;
    private ImageView mIvMute;
    private ImageView mIvHangup;
    private ImageView mIvFullscreen;
    private FrameLayout mFlMap;

    private ProxyVideoSink localRender;
    private ProxyVideoSink remoteRender;
    private WebRTCManager manager;
    private EglBase rootEglBase;
    private boolean isFull = false;
    private boolean enableMic = true;
    private String robotSn = "";
    private String roomId = "";
    private CallNavigationFragment callNavigationFragment;
    private int lastBat = 0;//??????????????????????????????
    private boolean isNoCamera = false;
    private CallConfig callConfig;

    public static void openActivity(Activity activity, String sn, String roomId, boolean isNoCamera) {
        Intent intent = new Intent(activity, ChatingActivity.class);
        intent.putExtra("sn", sn);
        intent.putExtra("roomId", roomId);
        intent.putExtra("isnoCamera", isNoCamera);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chating);
        initView();
        initData();
    }

    private void initView() {
        mViewBg = findViewById(R.id.view_bg);
        mSvrRemote = findViewById(R.id.svr_remote);
        mIvRobotImage = findViewById(R.id.iv_robot_image);
        mTvRobotName = findViewById(R.id.tv_robot_name);
        mIvRobotBattery = findViewById(R.id.iv_robot_battery);
        mTvRobotBattery = findViewById(R.id.tv_robot_battery);
        mIvMute = findViewById(R.id.iv_mute);
        mIvHangup = findViewById(R.id.iv_hangup);
        mIvFullscreen = findViewById(R.id.iv_fullscreen);
        mFlMap = findViewById(R.id.fl_map);

        mIvMute.setOnClickListener(this);
        mIvHangup.setOnClickListener(this);
        mIvFullscreen.setOnClickListener(this);
        //?????????????????????
        AppHolder.getInstance().getMqtt().getNavigation(robotSn, MQTTManager.PAD_SN);
    }

    private void initData() {
        EventBus.getDefault().register(this);
        robotSn = getIntent().getStringExtra("sn");
        roomId = getIntent().getStringExtra("roomId");
        isNoCamera = getIntent().getBooleanExtra("isnoCamera", false);
        String type = RobotDataManager.getInstance().get(robotSn).type;
        //???????????? ???????????????????????????
        if (isNoCamera) {
            callConfig = Function.getFun3().get(FUNCTION_XY);
        } else {
            callConfig = Function.getFun3().get(FUNCTION_DEFAULT);
        }
        //?????????????????????
        mTvRobotName.setText(robotSn + getString(R.string.text_calling));
        //?????????????????????
        mIvRobotImage.setImageResource(RobotUtils.getRobotType(type));
        //????????????Fragment
        showFragment();
        //?????????????????????
        startCall();
    }

    //????????????Fragment
    private void showFragment() {
        callNavigationFragment = new CallNavigationFragment();
        Bundle bundle = new Bundle();
        bundle.putString("sn", robotSn);
        callNavigationFragment.setArguments(bundle);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(R.id.fl_map, callNavigationFragment).show(callNavigationFragment).commitAllowingStateLoss();
    }

    //?????????????????????
    private void startCall() {

        manager = WebRTCManager.getInstance();
        rootEglBase = EglBase.create();
        if (callConfig.isVideoEnable()) {
            localRender = new ProxyVideoSink();
            //?????????????????????
            mSvrRemote.init(rootEglBase.getEglBaseContext(), null);
            mSvrRemote.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_BALANCED);
            mSvrRemote.setMirror(true);
            remoteRender = new ProxyVideoSink();
            //??????????????????
            remoteRender.setTarget(mSvrRemote);
        }

        manager.setCallback(new IViewCallback() {
            @Override
            public void onSetLocalStream(MediaStream stream, String socketId) {
                Log.d(TAG, " onSetLocalStream: thread:" + Thread.currentThread());
                if (callConfig.isVideoEnable()) {
                    //????????????????????????
                    if (stream.videoTracks.size() > 0) {
                        stream.videoTracks.get(0).addSink(localRender);
                        stream.videoTracks.get(0).setEnabled(true);
                    }
                }
                //?????????????????? ??????????????????????????????
                //manager.toggleMute(false);
                //?????????????????????
                AppHolder.getInstance().setPadCallState(PadIntercomState.INTERCOM_CALL_ING);
            }

            @Override
            public void onAddRemoteStream(MediaStream stream, String socketId) {
                Log.d(TAG, " onAddRemoteStream: thread:" + Thread.currentThread());
                if (callConfig.isVideoEnable()) {
                    //????????????????????????
                    if (stream.videoTracks.size() > 0) {
                        stream.videoTracks.get(0).addSink(remoteRender);
                        stream.videoTracks.get(0).setEnabled(true);
                    }
                    //??????????????????
                    runOnUiThread(() -> remoteRender.setTarget(mSvrRemote));
                }
                //?????????????????????
                AppHolder.getInstance().setPadCallState(PadIntercomState.INTERCOM_CALL_ON);
            }

            @Override
            public void onCloseWithId(String socketId) {
                Log.d(TAG, " onCloseWithId: thread:" + Thread.currentThread());
                runOnUiThread(() -> {
                    disConnect();
                    ChatingActivity.this.finish();
                });

            }
        });
        //????????????
        if (!PermissionUtil.isNeedRequestPermission(ChatingActivity.this)) {
            manager.joinRoom(getApplicationContext(), rootEglBase);
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_mute:
                //????????????
                enableMic = !enableMic;
                mIvMute.setImageResource(enableMic ? R.mipmap.call_mute_no : R.mipmap.call_mute);
                if (manager != null) {
                    manager.toggleMute(enableMic);
                }
                break;
            case R.id.iv_hangup:
                //??????
                disConnect();
                finish();
                break;
            case R.id.iv_fullscreen:
                isFull = !isFull;
                mIvFullscreen.setImageResource(isFull ? R.mipmap.call_fullscreen_no : R.mipmap.call_fullscreen);
                mFlMap.setVisibility(isFull ? View.GONE : View.VISIBLE);
                mViewBg.setBackgroundResource(isFull ? R.color.alph : R.color.black);
                Log.d(TAG, "isFull:" + isFull);
                break;
            default:
                break;
        }
    }

    //????????????
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ChargeStateEvent event) {
        if (!TextUtils.equals(event.sn, robotSn)) return;
        if (lastBat == event.bat) return;
        lastBat = event.bat;
        setBattery(event.bat);
    }


    //?????????????????????
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(NavigationEntity navigation) {
        if (!robotSn.equals(navigation.sn)) {
            return;
        }
        //?????? ????????????????????? ???
        List<PointEntity> list = new ArrayList<>();
        for (NavigationEntity.ListData data : navigation.listData) {
            if (data.nType == 1) {
                list.add(new PointEntity(data.nX, data.nY, data.angle,data.nName));
            }
        }
        if (callNavigationFragment == null) {
            return;
        }
        callNavigationFragment.addNavigationList(list);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(LocationEntity event) {
        if (!TextUtils.equals(event.sn, robotSn)) return;
        if (callNavigationFragment == null) return;
        callNavigationFragment.updatePostion(event.x, event.y);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return keyCode == KeyEvent.KEYCODE_BACK || super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disConnect();
        EventBus.getDefault().unregister(this);
    }

    /**
     * ?????????????????????
     *
     * @param battery ?????????
     */
    private void setBattery(int battery) {
        mTvRobotBattery.setText(battery + "%");
        mIvRobotBattery.setCurBatteryImage(battery);
    }

    private void disConnect() {
        manager.exitRoom();
        if (localRender != null) {
            localRender.setTarget(null);
            localRender = null;
        }
        if (remoteRender != null) {
            remoteRender.setTarget(null);
            remoteRender = null;
        }
        if (mSvrRemote != null) {
            mSvrRemote.release();
            //mSvrRemote = null;
        }
        //??????????????????
        AppHolder.getInstance().setPadCallState(PadIntercomState.INTERCOM_NULL);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        for (int i = 0; i < permissions.length; i++) {
            Log.i(PeerConnectionHelper.TAG, "[Permission] " + permissions[i] + " is " + (grantResults[i] == PackageManager.PERMISSION_GRANTED ? "granted" : "denied"));
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                finish();
                break;
            }
        }
        manager.joinRoom(getApplicationContext(), rootEglBase);
    }

}
