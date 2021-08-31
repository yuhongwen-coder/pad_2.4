package com.maxvision.tech.robot.ui.fragment;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.maxvision.tech.mqtt.MQTTManager;
import com.maxvision.tech.mqtt.entity.Heart;
import com.maxvision.tech.mqtt.entity.state.PadIntercomState;
import com.maxvision.tech.robot.AppHolder;
import com.maxvision.tech.robot.R;
import com.maxvision.tech.robot.config.CallConfig;
import com.maxvision.tech.robot.config.Function;
import com.maxvision.tech.robot.manager.RobotDataManager;
import com.maxvision.tech.robot.utils.RobotUtils;
import com.maxvision.tech.webrtc.manager.IViewCallback;
import com.maxvision.tech.webrtc.manager.PeerConnectionHelper;
import com.maxvision.tech.webrtc.manager.ProxyVideoSink;
import com.maxvision.tech.webrtc.manager.WebRTCManager;
import com.maxvision.tech.webrtc.manager.WebrtcUtil;
import com.maxvision.tech.webrtc.utils.PermissionUtil;
import com.maxvision.tech.webrtc.ws.IConnectEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.webrtc.EglBase;
import org.webrtc.MediaStream;
import org.webrtc.RendererCommon;
import org.webrtc.SurfaceViewRenderer;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static com.luck.picture.lib.thread.PictureThreadUtils.runOnUiThread;
import static com.maxvision.tech.robot.config.Function.FUNCTION_XY_ZLDH;
import static com.maxvision.tech.robot.config.Function.FUNCTION_ZLDH;

/**
 * name: qs
 * date: 2021/4/21
 * desc:智能对话 视频Fragment
 */
public class ZldhVideoFragment extends Fragment {
    private static final String TAG = "qs_ZldhVideoFragment";

    public String roomID = "232343";
    private String sn = "";
    private boolean isMode = true;

    private WebRTCManager manager;
    private EglBase rootEglBase;
    private ProxyVideoSink localRender;
    private ProxyVideoSink remoteRender;

    @BindView(R.id.svr_remote)
    SurfaceViewRenderer mSvrRemote;
    @BindView(R.id.tv_tip)
    TextView mTvTip;
    private boolean isNoCamera = false;//是否没有摄像头
    private boolean isCameraUsed = false;//是否界面占用摄像头

    private Unbinder bind;
    private CallConfig callConfig;
    private int type = 1;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle == null) return;
        this.sn = bundle.getString("sn");
        this.roomID = bundle.getString("roomid");
        this.isMode = bundle.getBoolean("isMode");
        roomID = sn + System.currentTimeMillis();

        getCallConfig(sn);
        EventBus.getDefault().register(this);
    }

    //获得音视频模式的配置
    private void getCallConfig(String sn) {
        String type = RobotDataManager.getInstance().get(sn).type;
        int functionShow = RobotDataManager.getInstance().get(sn).functionShow;
        isNoCamera = RobotUtils.isNoCamera(type);
        isCameraUsed = RobotUtils.isCameraUsed(functionShow);
        Log.d(TAG, " isNoCamera:" + isNoCamera + " isCameraUsed:" + isCameraUsed);
        if (isNoCamera) {
            callConfig = Function.getFun3().get(FUNCTION_XY_ZLDH);
        } else if (isCameraUsed) {
            callConfig = Function.getFun3().get(FUNCTION_XY_ZLDH);
        } else {
            callConfig = Function.getFun3().get(FUNCTION_ZLDH);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_zldh_remote_video, container, false);
        bind = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        connect();
    }

    //收到心跳
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(Heart heart) {
        if (!TextUtils.equals(heart.sn, sn)) return;
        if (isCameraUsed) return;
        //通话过程中，机器人进入人脸等模式 提示用户
        if (RobotUtils.isCameraUsed(heart.functionShow) && AppHolder.getInstance().getPadCallState() == PadIntercomState.INTERCOM_CALL_ON) {
            mTvTip.setVisibility(View.VISIBLE);
            mSvrRemote.setVisibility(View.GONE);
        }
    }

    //连接
    private void connect() {
        WebRTCManager.getInstance().init(WebrtcUtil.WSS, WebrtcUtil.iceServers, new IConnectEvent() {
            @Override
            public void onSuccess() {
                //websocket连接成功
                Log.d(TAG, " onSuccess: thread:" + Thread.currentThread());
                startCall();
            }

            @Override
            public void onFailed(String msg) {
                //websocket连接连接失败
                Log.d(TAG, " onFailed:" + msg + " thread:" + Thread.currentThread());
            }
        });
        //开始连接
        WebRTCManager.getInstance().connect(callConfig.isAudioEnable(), callConfig.isVideoEnable(), roomID);
    }

    //初始化视频通话
    private void startCall() {
        //发布拨号通知 连接机器人的视频 机器人自动接听
        if (isMode) {
            type = 1;
        } else {
            type = 2;
        }
        AppHolder.getInstance().getMqtt().getLookVideo(sn, MQTTManager.PAD_SN, type, roomID);

        manager = WebRTCManager.getInstance();
        rootEglBase = EglBase.create();
        if (callConfig.isVideoEnable()) {
            localRender = new ProxyVideoSink();
            remoteRender = new ProxyVideoSink();
            //远端图像初始化
            mSvrRemote.init(rootEglBase.getEglBaseContext(), null);
            mSvrRemote.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_BALANCED);
            mSvrRemote.setMirror(true);
            //设置远端视频
            remoteRender.setTarget(mSvrRemote);
            //显示视频
            mSvrRemote.setVisibility(View.VISIBLE);
        }

        manager.setCallback(new IViewCallback() {
            @Override
            public void onSetLocalStream(MediaStream stream, String socketId) {
                Log.d(TAG, " onSetLocalStream: thread:" + Thread.currentThread());
                if (callConfig.isVideoEnable()) {
                    if (stream.videoTracks.size() > 0) {
                        stream.videoTracks.get(0).addSink(localRender);
                        stream.videoTracks.get(0).setEnabled(true);
                    }
                }
                //测试默认静音 静音需要在子线程操作
                //manager.toggleMute(false);
                //设置呼叫中状态
                runOnUiThread(() -> setCallStatus(PadIntercomState.INTERCOM_CALL_ING));
            }

            @Override
            public void onAddRemoteStream(MediaStream stream, String socketId) {
                Log.d(TAG, " onAddRemoteStream: thread:" + Thread.currentThread());
                if (callConfig.isVideoEnable()) {
                    if (stream.videoTracks.size() > 0) {
                        stream.videoTracks.get(0).addSink(remoteRender);
                        stream.videoTracks.get(0).setEnabled(true);
                    }
                    //设置远端视频
                    runOnUiThread(() -> remoteRender.setTarget(mSvrRemote));
                }
                //设置正在通话状态的UI
                runOnUiThread(() -> {
                    setCallStatus(PadIntercomState.INTERCOM_CALL_ON);
                    if (isCameraUsed) {
                        mTvTip.setVisibility(View.VISIBLE);
                    }
                });

            }

            @Override
            public void onCloseWithId(String socketId) {
                Log.d(TAG, " onCloseWithId: thread:" + Thread.currentThread());
                runOnUiThread(() -> {
                    //设置空闲状态
                    setCallStatus(PadIntercomState.INTERCOM_NULL);
                    disConnect();
                });
            }
        });
        if (!PermissionUtil.isNeedRequestPermission(getActivity())) {
            manager.joinRoom(AppHolder.getInstance(), rootEglBase);
        }

    }

    @Override
    public void onDestroy() {
        Log.d(TAG, " onDestroy:");
        //设置空闲状态
        setCallStatus(PadIntercomState.INTERCOM_NULL);
        disConnect();
        super.onDestroy();
        if (bind != null) {
            bind.unbind();
        }
        EventBus.getDefault().unregister(this);
    }

    /**
     * 设置拨号的状态和UI
     *
     * @param callStatus 拨号状态
     */
    private void setCallStatus(int callStatus) {
        //设置通话中状态
        AppHolder.getInstance().setPadCallState(callStatus);
    }

    //断开连接
    private void disConnect() {
        if (manager != null) {
            manager.exitRoom();
        }
        if (localRender != null) {
            localRender.setTarget(null);
            localRender = null;
        }
        if (remoteRender != null) {
            remoteRender.setTarget(null);
            remoteRender = null;
        }
        if (mSvrRemote != null) {
            mSvrRemote.clearImage();
            mSvrRemote.release();
            mSvrRemote.setVisibility(View.GONE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        for (int i = 0; i < permissions.length; i++) {
            Log.i(PeerConnectionHelper.TAG, "[Permission] " + permissions[i] + " is " + (grantResults[i] == PackageManager.PERMISSION_GRANTED ? "granted" : "denied"));
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                break;
            }
        }
        manager.joinRoom(AppHolder.getInstance(), rootEglBase);
    }

}