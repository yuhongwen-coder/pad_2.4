package com.maxvision.tech.robot.ui.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.maxvision.tech.mqtt.MQTTManager;
import com.maxvision.tech.mqtt.entity.Heart;
import com.maxvision.tech.mqtt.entity.state.PadIntercomState;
import com.maxvision.tech.mqtt.entity.state.RobotIntercomState;
import com.maxvision.tech.robot.AppHolder;
import com.maxvision.tech.robot.MainActivity;
import com.maxvision.tech.robot.R;
import com.maxvision.tech.robot.config.CallConfig;
import com.maxvision.tech.robot.config.Function;
import com.maxvision.tech.robot.entity.event.HangEvent;
import com.maxvision.tech.robot.manager.RobotDataManager;
import com.maxvision.tech.robot.utils.CameraUtil;
import com.maxvision.tech.robot.utils.CustomToast;
import com.maxvision.tech.robot.utils.NetworkUtils;
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

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static com.luck.picture.lib.thread.PictureThreadUtils.runOnUiThread;
import static com.maxvision.tech.robot.config.Function.FUNCTION_DEFAULT;
import static com.maxvision.tech.robot.config.Function.FUNCTION_XY;

/**
 * name: qs
 * date: 2021/4/21
 * desc:小窗口视频Fragment
 */
public class RemoteVideoFragment extends Fragment {
    private static final String TAG = "qs_RemoteVideoFragment";

    public String roomID = "232343";
    private String sn;

    private WebRTCManager manager;
    private EglBase rootEglBase;
    private ProxyVideoSink localRender;
    private ProxyVideoSink remoteRender;

    private boolean enableMic = true;
    private boolean isFull = false;
    @BindView(R.id.svr_remote)
    SurfaceViewRenderer mSvrRemote;
    @BindView(R.id.iv_mute)
    ImageView mIvMute;
    @BindView(R.id.iv_hangup)
    ImageView mIvHangup;
    @BindView(R.id.iv_fullscreen)
    ImageView mIvFullscreen;
    @BindView(R.id.tv_call)
    TextView mTvCall;
    @BindView(R.id.tv_calling)
    TextView mTvCalling;
    @BindView(R.id.tv_tip)
    TextView mTvTip;
    private Disposable mDisposable;
    private MainActivity mainActivity;
    private boolean isNoCamera = false;//是否没有摄像头
    private boolean isCameraUsed = false;//是否界面占用摄像头

    OnFullClickListener listener;
    private Unbinder bind;
    private CallConfig callConfig;
    private boolean isHangup = false;//是否点击挂断
    private Disposable timeDisposable;
    private long time = 30;

    public void setListener(OnFullClickListener listener) {
        this.listener = listener;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivity = (MainActivity) context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle == null) return;
        this.sn = bundle.getString("sn");
        this.roomID = bundle.getString("roomid");
        roomID = sn + System.currentTimeMillis();
        EventBus.getDefault().register(this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_remote_video, container, false);
        bind = ButterKnife.bind(this, view);
        //设置空闲状态
        setCallStatus(PadIntercomState.INTERCOM_NULL);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onDestroy() {
        //设置空闲状态
        setCallStatus(PadIntercomState.INTERCOM_NULL);
        disConnect();
        super.onDestroy();
        cancelDelayHangup();
        if (bind != null) {
            bind.unbind();
        }
        EventBus.getDefault().unregister(this);
    }

    //点击连接
    private void connect() {
        //断网提示
        if (!NetworkUtils.isNetworkConnected(mainActivity)) {
            CustomToast.toast(false, CustomToast.TIP_NORMAL, getString(R.string.no_network));
            return;
        }
        getCallConfig(sn);
        //非协运 没有可用摄像头 提示
        if (!isNoCamera && !CameraUtil.isCameraCanUse()) {
            CustomToast.toast(false, CustomToast.TIP_NORMAL, getString(R.string.camera_enable));
            return;
        }
        int intercomState = RobotDataManager.getInstance().get(sn).intercomState;
        //平板的非空闲状态 或机器人是非空闲状态 不能拨号
        if (AppHolder.getInstance().getPadCallState() != PadIntercomState.INTERCOM_NULL || intercomState != RobotIntercomState.INTERCOM_NULL) {
            CustomToast.toast(false, CustomToast.TIP_NORMAL, getString(R.string.tip_calling));
            return;
        }
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

    //获得音视频模式的配置
    private void getCallConfig(String sn) {
        String type = RobotDataManager.getInstance().get(sn).type;
        int functionShow = RobotDataManager.getInstance().get(sn).functionShow;
        isNoCamera = RobotUtils.isNoCamera(type);
        isCameraUsed = RobotUtils.isCameraUsed(functionShow);
        Log.d(TAG, " isNoCamera:" + isNoCamera + " isCameraUsed:" + isCameraUsed);
        if (isNoCamera) {
            callConfig = Function.getFun3().get(FUNCTION_XY);
        } else if (isCameraUsed) {
            callConfig = Function.getFun3().get(FUNCTION_XY);
        } else {
            callConfig = Function.getFun3().get(FUNCTION_DEFAULT);
        }
    }

    //初始化视频通话
    private void startCall() {
        //发布拨号通知 连接机器人的视频 机器人自动接听
        AppHolder.getInstance().getMqtt().getLookVideo(sn, MQTTManager.PAD_SN, 1, roomID);

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
        }
        //显示视频
        mSvrRemote.setVisibility(View.VISIBLE);

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
                runOnUiThread(() -> setCallStatus(PadIntercomState.INTERCOM_CALL_ON));

            }

            @Override
            public void onCloseWithId(String socketId) {
                Log.d(TAG, " onCloseWithId: thread:" + Thread.currentThread() + isHangup);
                if (isHangup) return;
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

    @SuppressLint("NonConstantResourceId")
    @OnClick({R.id.tv_call, R.id.iv_mute, R.id.iv_hangup, R.id.iv_fullscreen})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_call:
                //点击拨号
                connect();
                break;
            case R.id.iv_mute:
                //静音按钮
                enableMic = !enableMic;
                mIvMute.setImageResource(enableMic ? R.mipmap.call_mute_no : R.mipmap.call_mute);
                if (manager != null) {
                    manager.toggleMute(enableMic);
                }
                break;
            case R.id.iv_hangup:
                isHangup = true;
                //设置挂断中状态
                setCallStatus(PadIntercomState.INTERCOM_HANGEUP_ING);
                //断开连接
                disConnect();
                setDelayHangup();
                break;
            case R.id.iv_fullscreen:
                //全屏按钮
                Log.d(TAG, "isFull:" + isFull);
                isFull = !isFull;
                mIvFullscreen.setImageResource(isFull ? R.mipmap.call_fullscreen_no : R.mipmap.call_fullscreen);
                if (listener != null) {
                    listener.onFullClick(isFull);
                }
                break;
            default:
                break;
        }
    }

    //延时2s,显示空闲状态 防止连续拨号导致的崩溃
    private void setDelayHangup() {
        cancelDelayHangup();
        //延时2s,显示空闲状态 防止连续拨号导致的崩溃
        mDisposable = Observable.timer(2, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(l -> {
                    //设置空闲状态
                    setCallStatus(PadIntercomState.INTERCOM_NULL);
                    isHangup = false;
                });
    }

    private void cancelDelayHangup() {
        if (mDisposable != null && !mDisposable.isDisposed()) {
            mDisposable.dispose();
        }
    }

    //收到切换机器人 挂断当前视频
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(HangEvent bean) {
        String sn2 = bean.getSn();
        if (sn2.equals(sn) && mIvHangup != null) {
            mIvHangup.performClick();
        }
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

    /**
     * 设置拨号的状态和UI
     *
     * @param callStatus 拨号状态
     */
    private void setCallStatus(int callStatus) {
        //取消自动挂断
        cancelAutoHangup();
        //设置通话中状态
        AppHolder.getInstance().setPadCallState(callStatus);
        if(mTvCall==null||mIvHangup==null||mIvMute==null||mTvCalling==null||mTvTip==null) return;
        //空闲状态
        if (callStatus == PadIntercomState.INTERCOM_NULL) {
            mTvCall.setVisibility(View.VISIBLE);
            mIvHangup.setVisibility(View.GONE);
            mIvMute.setVisibility(View.GONE);
            mTvCalling.setVisibility(View.GONE);
            mTvTip.setVisibility(View.GONE);
        } else if (callStatus == PadIntercomState.INTERCOM_CALL_ING) {
            mTvCall.setVisibility(View.GONE);
            mTvCalling.setVisibility(View.VISIBLE);
            mIvHangup.setVisibility(View.VISIBLE);
            mTvCalling.setText(R.string.text_call);
            //30S无接通自动挂断拨号
            setAutoHangup();
        } else if (callStatus == PadIntercomState.INTERCOM_CALL_ON) {
            mTvCall.setVisibility(View.GONE);
            mTvCalling.setVisibility(View.GONE);
            mIvHangup.setVisibility(View.VISIBLE);
            mIvMute.setVisibility(View.VISIBLE);
            if (isCameraUsed) {
                mTvTip.setVisibility(View.VISIBLE);
            }
        } else if (callStatus == PadIntercomState.INTERCOM_HANGEUP_ING) {
            mTvCall.setVisibility(View.GONE);
            mIvHangup.setVisibility(View.GONE);
            mIvMute.setVisibility(View.GONE);
            mTvTip.setVisibility(View.GONE);
            mTvCalling.setVisibility(View.VISIBLE);
            mTvCalling.setText(R.string.text_hanging);
        }
    }

    //拨号30S无接通 自动挂断
    private void setAutoHangup() {
        Log.d(TAG, " setAutoHangup:");
        cancelAutoHangup();
        timeDisposable = Observable.timer(time, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aLong -> {
                    Log.d(TAG, " setAutoHangup: aLong:" + aLong);
                    CustomToast.toast(false, CustomToast.TIP_NORMAL, getString(R.string.call_fail));
                    mIvHangup.performClick();
                });
    }

    //取消自动挂断
    private void cancelAutoHangup() {
        if (timeDisposable != null && !timeDisposable.isDisposed()) {
            timeDisposable.dispose();
        }
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

    public interface OnFullClickListener {
        void onFullClick(boolean isFull);
    }

}