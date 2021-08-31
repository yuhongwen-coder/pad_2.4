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
import com.maxvision.tech.mqtt.entity.TaskControlEntity;
import com.maxvision.tech.mqtt.entity.state.RobotCmdType;
import com.maxvision.tech.mqtt.entity.state.RobotStateHint;
import com.maxvision.tech.robot.AppHolder;
import com.maxvision.tech.robot.R;
import com.maxvision.tech.robot.base.BaseDialogFragment;



/**
 * name: wy
 * date: 2021/3/25
 * desc:
 */
public class HintDialogFragment extends BaseDialogFragment implements View.OnClickListener {

    private String sn;
    private TextView tv_title;
    private TextView tv_content;
    private RobotStateHint robotStateHint;
    private TaskControlEntity controlEntity;


    public static HintDialogFragment getInstance(String sn,RobotStateHint robotStateHint,TaskControlEntity controlEntity) {
        HintDialogFragment fragment = new HintDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("hint", robotStateHint);
        bundle.putSerializable("control", controlEntity);
        bundle.putString("sn",sn);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle == null) {
            dismiss();
            return;
        }
        sn = bundle.getString("sn");
        robotStateHint = (RobotStateHint) bundle.getSerializable("hint");
        controlEntity = (TaskControlEntity) bundle.getSerializable("control");
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_tip_view, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tv_title = view.findViewById(R.id.tv_title);
        tv_content = view.findViewById(R.id.tv_content);
        tv_title.setText("提示");
        tv_content.setText(robotStateHint.msg);
        view.findViewById(R.id.iv_close).setOnClickListener(this);
        view.findViewById(R.id.btn_close).setOnClickListener(this);
        view.findViewById(R.id.btn_ok).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (R.id.iv_close == v.getId()) {
            dismiss();
        } else if (R.id.btn_close == v.getId()) {
            // 判断命令是否同步
            if (robotStateHint.cmdSync) {
                // 同步命令
                if (RobotCmdType.CMD_NONE.equals(robotStateHint.secondCmd)) {
                    // 说明没有第二条命令
                    dismiss();
                } else {
                    controlEntity.firstCmd = robotStateHint.secondCmd;
                    handleCmd();
                }
            } else {
                dismiss();
            }
        } else if (R.id.btn_ok == v.getId()) {
            // 先处理第一条命令 --> 处理第二条命令
            controlEntity.firstCmd = robotStateHint.firstCmd;
            if (!robotStateHint.cmdSync && !RobotCmdType.CMD_NONE.equals(robotStateHint.secondCmd)) {
                controlEntity.secondCmd = robotStateHint.secondCmd;
            }
            handleCmd();
        }

    }

    private void handleCmd() {
        dismiss();
        controlEntity.sn = MQTTManager.PAD_SN;
        AppHolder.getInstance().getMqtt().getTaskControl(sn,new Gson().toJson(controlEntity));
    }
}