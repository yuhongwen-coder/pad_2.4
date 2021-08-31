package com.maxvision.tech.robot.ui.dialog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.maxvision.tech.mqtt.MQTTManager;
import com.maxvision.tech.mqtt.RobotState;
import com.maxvision.tech.mqtt.entity.Heart;
import com.maxvision.tech.mqtt.entity.TaskControlEntity;
import com.maxvision.tech.mqtt.entity.state.RobotCmdType;
import com.maxvision.tech.mqtt.entity.state.RobotStateHint;
import com.maxvision.tech.robot.AppHolder;
import com.maxvision.tech.robot.R;
import com.maxvision.tech.robot.base.BaseDialogFragment;
import com.maxvision.tech.robot.entity.cons.SpConstants;
import com.maxvision.tech.robot.manager.RobotDataManager;
import com.maxvision.tech.robot.utils.CustomToast;
import com.maxvision.tech.robot.utils.SpUtils;


/**
 * name: wy
 * date: 2021/3/25
 * desc: 定位或者导航Fragment
 */
public class NavigateDialogFragment extends BaseDialogFragment {

    private String sn;
    private double x,y,angle;

    public static NavigateDialogFragment getInstance(String sn,double x,double y,double angle){
        NavigateDialogFragment fragment = new NavigateDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString("sn",sn);
        bundle.putDouble("x",x);
        bundle.putDouble("y",y);
        bundle.putDouble("angle",angle);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if(bundle == null){
            dismiss();
            return;
        }
        sn = bundle.getString("sn");
        x = bundle.getDouble("x");
        y = bundle.getDouble("y");
        angle = bundle.getDouble("angle");
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_navigate_view,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.ll_local).setOnClickListener(v -> local());
        view.findViewById(R.id.ll_navigate).setOnClickListener(v -> navigate());
        LinearLayout ll_biaoji = view.findViewById(R.id.ll_biaoji);
        boolean isOpen = SpUtils.getBoolean(SpConstants.SP_CALL_SWITCH);
        ll_biaoji.setVisibility(isOpen ? View.VISIBLE : View.GONE);
        ll_biaoji.setOnClickListener(v -> {
            SpUtils.putString(SpConstants.SP_CALL_X,String.valueOf(((int)x)));
            SpUtils.putString(SpConstants.SP_CALL_Y,String.valueOf(((int)y)));
            SpUtils.putString(SpConstants.SP_CALL_A,String.valueOf(((int)angle)));
            CustomToast.toastLong(CustomToast.TIP_SUCCESS,"标记成功");
            dismiss();
        });
    }

    //定位
    private void local(){
        dismiss();
        Heart heart = RobotDataManager.getInstance().get(sn);
        RobotStateHint canInit = RobotState.isCanInit(heart);
        TaskControlEntity taskControlEntity = new TaskControlEntity();
        taskControlEntity.sn = MQTTManager.PAD_SN;
        if (canInit != null) {
            if (canInit.isDialog) {
                taskControlEntity.firstCmd = canInit.firstCmd;
                taskControlEntity.x = x;
                taskControlEntity.y = y;
                HintDialogFragment tipDialogFragment = HintDialogFragment.getInstance(sn,canInit,taskControlEntity);
                tipDialogFragment.show(getParentFragmentManager());
            } else {
                CustomToast.toast(true,CustomToast.TIP_ERROR,canInit.msg);
            }
        } else {
            taskControlEntity.firstCmd = RobotCmdType.CMD_INIT;
            taskControlEntity.x = x;
            taskControlEntity.y = y;
            AppHolder.getInstance().getMqtt().getTaskControl(sn,new Gson().toJson(taskControlEntity));
            CustomToast.toast(false,CustomToast.TIP_SUCCESS,"操作成功");
        }
    }

    //导航
    private void navigate(){
        dismiss();
        Heart heart = RobotDataManager.getInstance().get(sn);
        RobotStateHint canNav = RobotState.isCanNav(heart);
        TaskControlEntity taskControlEntity = new TaskControlEntity();
        taskControlEntity.sn = MQTTManager.PAD_SN;
        if (canNav != null) {
            if (canNav.isDialog) {
                taskControlEntity.firstCmd = canNav.firstCmd;
                taskControlEntity.x = x;
                taskControlEntity.y = y;
                HintDialogFragment tipDialogFragment = HintDialogFragment.getInstance(sn,canNav,taskControlEntity);
                tipDialogFragment.show(getFragmentManager());
            } else {
                CustomToast.toast(true,CustomToast.TIP_ERROR,canNav.msg);
            }
        } else {
            taskControlEntity.firstCmd = RobotCmdType.CMD_NAV;
            taskControlEntity.x = x;
            taskControlEntity.y = y;
            taskControlEntity.angel = angle;
            AppHolder.getInstance().getMqtt().getTaskControl(sn,new Gson().toJson(taskControlEntity));
            CustomToast.toast(false,CustomToast.TIP_SUCCESS,"操作成功");
        }
    }
}