package com.maxvision.tech.robot.ui.fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.dou361.dialogui.DialogUIUtils;
import com.google.gson.Gson;
import com.maxvision.tech.mqtt.MQTTManager;
import com.maxvision.tech.mqtt.RobotState;
import com.maxvision.tech.mqtt.entity.Heart;
import com.maxvision.tech.mqtt.entity.TaskControlEntity;
import com.maxvision.tech.mqtt.entity.state.RobotChargeState;
import com.maxvision.tech.mqtt.entity.state.RobotCmdType;
import com.maxvision.tech.mqtt.entity.state.RobotIntercomState;
import com.maxvision.tech.mqtt.entity.state.RobotStateHint;
import com.maxvision.tech.robot.AppHolder;
import com.maxvision.tech.robot.MainActivity;
import com.maxvision.tech.robot.R;
import com.maxvision.tech.robot.aop.FastClick;
import com.maxvision.tech.robot.entity.cons.SpConstants;
import com.maxvision.tech.robot.manager.RobotDataManager;
import com.maxvision.tech.robot.ui.activity.DebugActivity;
import com.maxvision.tech.robot.ui.dialog.HintDialogFragment;
import com.maxvision.tech.robot.ui.dialog.NavigateDialogFragment;
import com.maxvision.tech.robot.map.NavigationView;
import com.maxvision.tech.robot.map.OnClickNavigateListener;
import com.maxvision.tech.robot.map.PointEntity;
import com.maxvision.tech.robot.ui.dialog.TipDialogFragment;
import com.maxvision.tech.robot.ui.view.BatteryImageView;
import com.maxvision.tech.robot.ui.view.MyRockerView;
import com.maxvision.tech.robot.utils.CustomToast;
import com.maxvision.tech.robot.utils.FileConstant;
import com.maxvision.tech.robot.utils.RobotUtils;
import com.maxvision.tech.robot.utils.SpUtils;
import com.maxvision.tech.webrtc.utils.Utils;

import java.io.File;
import java.util.List;

/**
 * name: wy
 * date: 2021/4/1
 * desc:
 */
public class NavigationFragment extends Fragment implements OnClickNavigateListener, View.OnClickListener {

    public static String roomID = "232343";

    private NavigationView navigationView;
    private String sn;
    private Dialog mapDialog;

    private ImageView iv_net; //网络信号
    private TextView tv_battery; //电量百分比
    private BatteryImageView iv_battery; //电量动画
    private TextView tv_sn; //机器人编号
    private ImageView iv_jt;
    private TextView tv_xy;

    //是否列表是否展开
    private boolean isUnfold = false;

    //-------------可见光和遥感-----------------
    private ViewStub view_stub;
    private View layout_control;
    private FrameLayout fl_surface;
    private MainActivity mainActivity;
    private RemoteVideoFragment remoteVideoFragment;

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
        roomID = sn + System.currentTimeMillis();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_navigation, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navigationView = view.findViewById(R.id.nv_view);
        navigationView.setOnClickNavigateListener(this);
        view.findViewById(R.id.btn_big).setOnClickListener(this);
        view.findViewById(R.id.btn_small).setOnClickListener(this);
        view.findViewById(R.id.btn_stop_navigation).setOnClickListener(this);
        view.findViewById(R.id.ll_unfold).setOnClickListener(this);

        tv_xy = view.findViewById(R.id.tv_xy);
        view_stub = view.findViewById(R.id.view_stub);
        iv_net = view.findViewById(R.id.iv_net);
        tv_battery = view.findViewById(R.id.tv_battery);
        iv_battery = view.findViewById(R.id.iv_battery);
        tv_sn = view.findViewById(R.id.tv_sn);
        iv_jt = view.findViewById(R.id.iv_jt);
        tv_sn.setText("ID:" + sn);
        onConfigActivity();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadMap();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) {
            //隐藏
            isUnfold = false;
            iv_jt.setRotation(180);
            view_stub.setVisibility(View.GONE);
        } else {
            //显示
            loadMap();
        }
    }

    private void loadMap() {
        Heart heart = RobotDataManager.getInstance().get(sn);
        String path = FileConstant.getInstance().getPathMapImage() + heart.mapId + ".png";
        File file = new File(path);
        if (file.exists()) {
            //已存在
            setMap(path);
        } else {
            //不存在
            //需要去加载图片
            AppHolder.getInstance().getMqtt().getMap(sn, heart.mapId);
            mapDialog = DialogUIUtils.showLoading(
                    getContext(),
                    getString(R.string.text_loading_mapdata),
                    true,
                    true,
                    false,
                    true).show();
        }
    }

    /**
     * 是否重新加载
     *
     * @param path 地图路径
     */
    public void setMap(String path) {
        //设置地图，加载地图
        hideMapLoadingDialog();
        //加载地图
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        navigationView.setImageBitmap(bitmap);
        //获取导航点坐标
        AppHolder.getInstance().getMqtt().getNavigation(sn, MQTTManager.PAD_SN);
        //获取任务列表
        AppHolder.getInstance().getMqtt().getTaskList(sn, MQTTManager.PAD_SN);
    }

    private void hideMapLoadingDialog() {
        if (mapDialog != null && mapDialog.isShowing()) {
            mapDialog.dismiss();
        }
    }

    @Override
    public void onClickNavigate(PointEntity point) {
        TipDialogFragment tipDialogFragment = TipDialogFragment.getInstance(sn, point.x, point.y, point.angle, point.name);
        tipDialogFragment.show(getChildFragmentManager());
    }

    @Override
    public void onLongNavigate(PointEntity point) {
        //长按
        NavigateDialogFragment navigateDialogFragment = NavigateDialogFragment.getInstance(sn, point.x, point.y, point.angle);
        navigateDialogFragment.show(getChildFragmentManager());
    }

    @Override
    public void onTouchXy(double x, double y) {
        String str = "X=" + ((int) x) + "  Y=" + ((int) y);
        tv_xy.setText(str);
    }


    public void addNavigationList(List<PointEntity> list) {
        navigationView.addNavigationList(list);
    }

    public void updatePostion(double x, double y) {
        navigationView.updatePostion(x, y);
    }


    /**
     * 设置链接网络信号强度
     *
     * @param wifi 强度
     */
    private void setWifiState(int wifi) {
        if (wifi >= -50 && wifi < 0) {//最强
            iv_net.setImageResource(R.mipmap.ic_net_four);
        } else if (wifi >= -70 && wifi < -50) {//较强
            iv_net.setImageResource(R.mipmap.ic_net_three);
        } else if (wifi >= -80 && wifi < -70) {//较弱
            iv_net.setImageResource(R.mipmap.ic_net_two);
        } else if (wifi >= -100 && wifi < -80) {//微弱
            iv_net.setImageResource(R.mipmap.ic_net_one);
        } else {
            iv_net.setImageResource(R.mipmap.ic_net_none);
        }
    }

    /**
     * 开启充电
     */
    private void startBat() {
        iv_battery.startBat();
    }

    /**
     * 关闭充电
     */
    private void closeBat() {
        iv_battery.closeBat();
    }

    /**
     * 设置电量百分比
     *
     * @param battery 百分比
     */
    private void setBattery(int battery) {
        tv_battery.setText(battery + "%");
        setCurBatteryImage(battery);
    }

    private void setCurBatteryImage(int curBattery) {
        iv_battery.setCurBatteryImage(curBattery);
    }

    public void setData(Heart heart) {
        navigationView.post(() -> {
            setWifiState(heart.wifi);
            setBattery(heart.bat);
            Log.i("wangyin_充电", "充电状态：" + heart.chargeState + "     " + heart.sn);
            //设置充电状态
            switch (heart.chargeState) {
                case RobotChargeState.EMPTY: //空闲
                case RobotChargeState.FULL: //充电已充满
                case RobotChargeState.NAV: //正在前往充电
                    closeBat();
                    break;
                case RobotChargeState.ING: //正在充电
                    startBat();
                    break;
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_big) {
            //放大
            navigationView.big();
        } else if (v.getId() == R.id.btn_small) {
            //缩小
            navigationView.small();
        } else if (v.getId() == R.id.btn_stop_navigation) {
            Heart heart = RobotDataManager.getInstance().get(sn);
            if (RobotUtils.isLine(heart.isLine)) return;
            //停止导航
            stopMap();
        } else if (v.getId() == R.id.ll_unfold) {
            loadLayoutControl();
            isUnfold = !isUnfold;
            iv_jt.setRotation(isUnfold ? 0 : 180);
            view_stub.setVisibility(isUnfold ? View.VISIBLE : View.GONE);
        }
    }

    @FastClick(filterTime = 2000)
    private void stopMap() {
        RobotStateHint canStop = RobotState.isCanStop(RobotDataManager.getInstance().get(sn));
        TaskControlEntity taskControlEntity = new TaskControlEntity();
        taskControlEntity.sn = MQTTManager.PAD_SN;
        if (canStop != null) {
            if (canStop.isDialog) {
                taskControlEntity.firstCmd = canStop.firstCmd;
                HintDialogFragment tipDialogFragment = HintDialogFragment.getInstance(sn, canStop, taskControlEntity);
                tipDialogFragment.show(getFragmentManager());
            } else {
                CustomToast.toast(true, CustomToast.TIP_ERROR, canStop.msg);
            }
        } else {
            taskControlEntity.firstCmd = RobotCmdType.CMD_STOP_MAP;
            AppHolder.getInstance().getMqtt().getTaskControl(sn, new Gson().toJson(taskControlEntity));
            CustomToast.toast(false, CustomToast.TIP_SUCCESS, "操作成功");
        }
    }

    private void loadLayoutControl() {
        if (layout_control == null) {
            layout_control = view_stub.inflate();
            MyRockerView rockerXYView = layout_control.findViewById(R.id.rockerXY_View);
            fl_surface = layout_control.findViewById(R.id.fl_surface);
            showRemoteVideoFragment();
            rockerXYView.setSn(sn);
        }
    }

    //显示小视频Fragment
    private void showRemoteVideoFragment() {
        //赋值给Mainactivity
        remoteVideoFragment = new RemoteVideoFragment();
        Bundle bundle = new Bundle();
        bundle.putString("sn", sn);
        bundle.putString("roomid", roomID);
        remoteVideoFragment.setArguments(bundle);
        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(R.id.fl_surface, remoteVideoFragment).show(remoteVideoFragment).addToBackStack(null).commitAllowingStateLoss();
        //点击全屏按钮
        remoteVideoFragment.setListener(isFull -> {
            setFullVideo(isFull);
        });
    }

    //视频全屏
    private void setFullVideo(boolean isfull) {
        if (fl_surface == null) return;
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) fl_surface.getLayoutParams();
        if (isfull) {
            layoutParams.width = RelativeLayout.LayoutParams.MATCH_PARENT;
            layoutParams.height = RelativeLayout.LayoutParams.MATCH_PARENT;
            layoutParams.setMargins(0, 0, 0, 0);
        } else {
            layoutParams.width = Utils.dip2px(mainActivity, 320);
            layoutParams.height = Utils.dip2px(mainActivity, 180);
            layoutParams.setMargins(0, 0, Utils.dip2px(mainActivity, 60), Utils.dip2px(mainActivity, 30));
        }
        fl_surface.setLayoutParams(layoutParams);

        if (mainActivity != null) {
            mainActivity.setFullVideo(isfull);
        }
    }

    //跳转设置界面
    private void onConfigActivity() {
        tv_sn.setOnClickListener(new View.OnClickListener() {
            final static int COUNTS = 5;//点击次数
            final static long DURATION = 1000;//规定有效时间
            long[] mHits = new long[COUNTS];

            @Override
            public void onClick(View v) {
                System.arraycopy(mHits, 1, mHits, 0, mHits.length - 1);
                //实现左移，然后最后一个位置更新距离开机的时间，如果最后一个时间和最开始时间小于DURATION，即连续5次点击
                mHits[mHits.length - 1] = SystemClock.uptimeMillis();
                if (mHits[0] >= (SystemClock.uptimeMillis() - DURATION)) {
                    //开发者模式
                    long t = System.currentTimeMillis();
                    long time = SpUtils.getLong(SpConstants.SP_DEBUG_TIME, 0);
                    if (t - time > 5 * 1000) {
                        SpUtils.putLong(SpConstants.SP_DEBUG_TIME, System.currentTimeMillis());
                        startActivity(new Intent(getActivity(), DebugActivity.class));
                    }
                }
            }
        });
    }


}