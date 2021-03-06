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

    private ImageView iv_net; //????????????
    private TextView tv_battery; //???????????????
    private BatteryImageView iv_battery; //????????????
    private TextView tv_sn; //???????????????
    private ImageView iv_jt;
    private TextView tv_xy;

    //????????????????????????
    private boolean isUnfold = false;

    //-------------??????????????????-----------------
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
            //??????
            isUnfold = false;
            iv_jt.setRotation(180);
            view_stub.setVisibility(View.GONE);
        } else {
            //??????
            loadMap();
        }
    }

    private void loadMap() {
        Heart heart = RobotDataManager.getInstance().get(sn);
        String path = FileConstant.getInstance().getPathMapImage() + heart.mapId + ".png";
        File file = new File(path);
        if (file.exists()) {
            //?????????
            setMap(path);
        } else {
            //?????????
            //?????????????????????
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
     * ??????????????????
     *
     * @param path ????????????
     */
    public void setMap(String path) {
        //???????????????????????????
        hideMapLoadingDialog();
        //????????????
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        navigationView.setImageBitmap(bitmap);
        //?????????????????????
        AppHolder.getInstance().getMqtt().getNavigation(sn, MQTTManager.PAD_SN);
        //??????????????????
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
        //??????
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
     * ??????????????????????????????
     *
     * @param wifi ??????
     */
    private void setWifiState(int wifi) {
        if (wifi >= -50 && wifi < 0) {//??????
            iv_net.setImageResource(R.mipmap.ic_net_four);
        } else if (wifi >= -70 && wifi < -50) {//??????
            iv_net.setImageResource(R.mipmap.ic_net_three);
        } else if (wifi >= -80 && wifi < -70) {//??????
            iv_net.setImageResource(R.mipmap.ic_net_two);
        } else if (wifi >= -100 && wifi < -80) {//??????
            iv_net.setImageResource(R.mipmap.ic_net_one);
        } else {
            iv_net.setImageResource(R.mipmap.ic_net_none);
        }
    }

    /**
     * ????????????
     */
    private void startBat() {
        iv_battery.startBat();
    }

    /**
     * ????????????
     */
    private void closeBat() {
        iv_battery.closeBat();
    }

    /**
     * ?????????????????????
     *
     * @param battery ?????????
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
            Log.i("wangyin_??????", "???????????????" + heart.chargeState + "     " + heart.sn);
            //??????????????????
            switch (heart.chargeState) {
                case RobotChargeState.EMPTY: //??????
                case RobotChargeState.FULL: //???????????????
                case RobotChargeState.NAV: //??????????????????
                    closeBat();
                    break;
                case RobotChargeState.ING: //????????????
                    startBat();
                    break;
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_big) {
            //??????
            navigationView.big();
        } else if (v.getId() == R.id.btn_small) {
            //??????
            navigationView.small();
        } else if (v.getId() == R.id.btn_stop_navigation) {
            Heart heart = RobotDataManager.getInstance().get(sn);
            if (RobotUtils.isLine(heart.isLine)) return;
            //????????????
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
            CustomToast.toast(false, CustomToast.TIP_SUCCESS, "????????????");
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

    //???????????????Fragment
    private void showRemoteVideoFragment() {
        //?????????Mainactivity
        remoteVideoFragment = new RemoteVideoFragment();
        Bundle bundle = new Bundle();
        bundle.putString("sn", sn);
        bundle.putString("roomid", roomID);
        remoteVideoFragment.setArguments(bundle);
        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(R.id.fl_surface, remoteVideoFragment).show(remoteVideoFragment).addToBackStack(null).commitAllowingStateLoss();
        //??????????????????
        remoteVideoFragment.setListener(isFull -> {
            setFullVideo(isFull);
        });
    }

    //????????????
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

    //??????????????????
    private void onConfigActivity() {
        tv_sn.setOnClickListener(new View.OnClickListener() {
            final static int COUNTS = 5;//????????????
            final static long DURATION = 1000;//??????????????????
            long[] mHits = new long[COUNTS];

            @Override
            public void onClick(View v) {
                System.arraycopy(mHits, 1, mHits, 0, mHits.length - 1);
                //?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????DURATION????????????5?????????
                mHits[mHits.length - 1] = SystemClock.uptimeMillis();
                if (mHits[0] >= (SystemClock.uptimeMillis() - DURATION)) {
                    //???????????????
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