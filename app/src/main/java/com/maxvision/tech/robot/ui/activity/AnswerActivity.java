package com.maxvision.tech.robot.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.luck.picture.lib.tools.ToastUtils;
import com.maxvision.tech.mqtt.MQTTManager;
import com.maxvision.tech.mqtt.entity.CallDealEntity;
import com.maxvision.tech.mqtt.entity.state.PadIntercomState;
import com.maxvision.tech.robot.AppHolder;
import com.maxvision.tech.robot.R;
import com.maxvision.tech.robot.config.CallConfig;
import com.maxvision.tech.robot.config.Function;
import com.maxvision.tech.robot.manager.RobotDataManager;
import com.maxvision.tech.robot.utils.CameraUtil;
import com.maxvision.tech.robot.utils.CustomToast;
import com.maxvision.tech.robot.utils.RobotUtils;
import com.maxvision.tech.webrtc.manager.WebRTCManager;
import com.maxvision.tech.webrtc.manager.WebrtcUtil;
import com.maxvision.tech.webrtc.ws.IConnectEvent;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import static com.maxvision.tech.robot.config.Function.FUNCTION_DEFAULT;
import static com.maxvision.tech.robot.config.Function.FUNCTION_XY;


/**
 * @author qs
 * @time 2021/4/6 15:50
 * @describe $ 来电界面
 */
public class AnswerActivity extends AppCompatActivity {
    private static final String TAG = "qs_answerActivity";

    private String robotSn;
    private String roomId;
    @BindView(R.id.iv_robot)
    ImageView mIvRobot;//机器人图片
    @BindView(R.id.tv_status)
    TextView mTvStatus;//呼叫状态
    @BindView(R.id.tv_answer)
    TextView mTvAnswer;
    @BindView(R.id.tv_refuse)
    TextView mTvRefuse;
    private Unbinder bind;
    private boolean isNoCamera;//判断是否是协运
    private CallConfig callConfig;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);
        bind = ButterKnife.bind(this);
        EventBus.getDefault().register(this);
        //初始化拨号状态
        AppHolder.getInstance().setPadCallState(PadIntercomState.INTERCOM_NULL);
        initData();
    }

    private void initData() {
        robotSn = getIntent().getStringExtra("sn");
        roomId = getIntent().getStringExtra("roomId");
        mTvStatus.setText(robotSn + getString(R.string.text_call));

        String type = RobotDataManager.getInstance().get(robotSn).type;
        isNoCamera = RobotUtils.isNoCamera(type);
        //设置机器人图片
        mIvRobot.setImageResource(RobotUtils.getRobotType(type));

        //连接房间 根据类型配置音视频
        if (isNoCamera) {
            callConfig = Function.getFun3().get(FUNCTION_XY);
        } else {
            callConfig = Function.getFun3().get(FUNCTION_DEFAULT);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        initData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        bind.unbind();
    }

    //接听
    public void answer(View view) {
        //非协运 没有可用摄像头 提示
        if(!isNoCamera&& !CameraUtil.isCameraCanUse()){
            CustomToast.toast(false,CustomToast.TIP_NORMAL,getString(R.string.camera_enable));
            return;
        }
        WebRTCManager.getInstance().init(WebrtcUtil.WSS, WebrtcUtil.iceServers, new IConnectEvent() {
            @Override
            public void onSuccess() {
                Log.d(TAG, " onSuccess: thread:" + Thread.currentThread());
                //进入通话界面
                ChatingActivity.openActivity(AnswerActivity.this, robotSn, roomId, isNoCamera);
                finish();
            }

            @Override
            public void onFailed(String msg) {
                Log.d(TAG, " onFailed:" + msg + " thread:" + Thread.currentThread());
                finish();
            }
        });
        //连接房间 App.isNoCamera() 协运=true 语音通话 防疫=false视频通话
        WebRTCManager.getInstance().connect(callConfig.isAudioEnable(), callConfig.isVideoEnable(), roomId);
        //发布 当前设备处理了接听
        AppHolder.getInstance().getMqtt().publishCallDeal(MQTTManager.PAD_SN, robotSn,1);
    }

    //挂断
    public void refuse(View view) {
        //发布 当前设备处理了挂断
        AppHolder.getInstance().getMqtt().publishCallDeal(MQTTManager.PAD_SN, robotSn,0);
        finish();
    }

    public static void startActivity(Context context, String sn, String roomId) {
        Intent intent = new Intent(context, AnswerActivity.class);
        intent.putExtra("sn", sn);
        intent.putExtra("roomId", roomId);
        context.startActivity(intent);
    }

    //收到终端已处理了来电
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(CallDealEntity bean) {
        finish();
    }

    //返回 不处理接听事件
    public void toBack(View view) {
        finish();
    }

}
