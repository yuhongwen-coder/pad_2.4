package com.maxvision.tech.robot.ui.dialog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.maxvision.tech.mqtt.MQTTManager;
import com.maxvision.tech.mqtt.RobotState;
import com.maxvision.tech.mqtt.entity.TaskControlEntity;
import com.maxvision.tech.mqtt.entity.state.RobotCmdType;
import com.maxvision.tech.mqtt.entity.state.RobotStateHint;
import com.maxvision.tech.robot.AppHolder;
import com.maxvision.tech.robot.R;
import com.maxvision.tech.robot.base.BaseDialogFragment;
import com.maxvision.tech.robot.manager.RobotDataManager;
import com.maxvision.tech.robot.utils.CustomToast;


/**
 * name: wy
 * date: 2021/3/25
 * desc: 导航到点Fragment
 */
public class TipDialogFragment extends BaseDialogFragment {

    private String sn,name;
    private double x,y,angle;

    private TextView tv_title;
    private TextView tv_content;


    public static TipDialogFragment getInstance(String sn, double x, double y, double angle,String name){
        TipDialogFragment fragment = new TipDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString("sn",sn);
        bundle.putDouble("x",x);
        bundle.putDouble("y",y);
        bundle.putDouble("angle",angle);
        bundle.putString("name",name);
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
        name = bundle.getString("name");
        x = bundle.getDouble("x");
        y = bundle.getDouble("y");
        angle = bundle.getDouble("angle");
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_tip_view,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tv_title = view.findViewById(R.id.tv_title);
        tv_content = view.findViewById(R.id.tv_content);
        tv_title.setText("提示");
        String tip = "是否前往："+name;
        tv_content.setText(tip);

        view.findViewById(R.id.iv_close).setOnClickListener(v -> dismiss());
        view.findViewById(R.id.btn_close).setOnClickListener(v -> dismiss());
        view.findViewById(R.id.btn_ok).setOnClickListener(v -> {
            RobotStateHint canNav = RobotState.isCanNav(RobotDataManager.getInstance().get(sn));
            dismiss();
            TaskControlEntity taskControlEntity = new TaskControlEntity();
            taskControlEntity.sn= MQTTManager.PAD_SN;
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
                taskControlEntity.taskName = name;
                AppHolder.getInstance().getMqtt().getTaskControl(sn,new Gson().toJson(taskControlEntity));
                CustomToast.toast(false,CustomToast.TIP_SUCCESS,"操作成功");
            }
        });
    }
}