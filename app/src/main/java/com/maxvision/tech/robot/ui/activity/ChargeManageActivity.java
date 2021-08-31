package com.maxvision.tech.robot.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.google.gson.Gson;
import com.maxvision.tech.mqtt.MQTTManager;
import com.maxvision.tech.mqtt.RobotState;
import com.maxvision.tech.mqtt.entity.Heart;
import com.maxvision.tech.mqtt.entity.TaskControlEntity;
import com.maxvision.tech.mqtt.entity.state.RobotChargeState;
import com.maxvision.tech.mqtt.entity.state.RobotCmdType;
import com.maxvision.tech.mqtt.entity.state.RobotStateHint;
import com.maxvision.tech.robot.AppHolder;
import com.maxvision.tech.robot.R;
import com.maxvision.tech.robot.entity.event.ChargeStateEvent;
import com.maxvision.tech.robot.manager.RobotDataManager;
import com.maxvision.tech.robot.ui.dialog.HintDialogFragment;
import com.maxvision.tech.robot.ui.view.LD_WaveView;
import com.maxvision.tech.robot.utils.CustomToast;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;


/**
 * Created by yuhongwen
 * on 2021/4/9
 * 充电管理
 */
public class ChargeManageActivity extends AppCompatActivity {

    private LD_WaveView waveViewCircle;
    private TextView tvChargeLevel;
    private TextView tvChargeState;
    private CheckBox cbSwitch;

    private String sn;

    public static void startActivity(Context context,String sn) {
        Intent intent = new Intent(context, ChargeManageActivity.class);
        intent.putExtra("sn",sn);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_charge);
        EventBus.getDefault().register(this);
        sn = getIntent().getStringExtra("sn");
        initView();
        initData();
    }

    private void initView() {
        waveViewCircle = findViewById(R.id.waveViewCircle);
        tvChargeLevel = findViewById(R.id.tv_charge_level);
        tvChargeState = findViewById(R.id.tv_charge_state);
        cbSwitch = findViewById(R.id.cb_switch);

        findViewById(R.id.back).setOnClickListener(v -> finish());
        cbSwitch.setOnClickListener(v -> {
            TaskControlEntity controlEntity = new TaskControlEntity();
            controlEntity.sn = MQTTManager.PAD_SN;
            if (cbSwitch.isChecked()) {
                // 去充电
                RobotStateHint canCharge = RobotState.isCanCharge(RobotDataManager.getInstance().get(sn));
                if (canCharge != null) {
                    if (canCharge.isDialog) {
                        controlEntity.firstCmd = canCharge.firstCmd;
                        HintDialogFragment tipDialogFragment = HintDialogFragment.getInstance(sn, canCharge, controlEntity);
                        tipDialogFragment.show(getSupportFragmentManager());
                    } else {
                        CustomToast.toastLong(CustomToast.TIP_ERROR,canCharge.msg);
                    }
                } else {
                    controlEntity.firstCmd = RobotCmdType.CMD_CHARGE;
                    AppHolder.getInstance().getMqtt().getTaskControl(sn,new Gson().toJson(controlEntity));
                }
            } else {
                // 停止充电
                RobotStateHint canStopCharge = RobotState.isCanStopCharge(RobotDataManager.getInstance().get(sn));
                if (canStopCharge != null) {
                    if (canStopCharge.isDialog) {
                        controlEntity.firstCmd = canStopCharge.firstCmd;
                        HintDialogFragment tipDialogFragment = HintDialogFragment.getInstance(sn, canStopCharge, controlEntity);
                        tipDialogFragment.show(getSupportFragmentManager());
                    } else {
                        CustomToast.toastLong(CustomToast.TIP_ERROR,canStopCharge.msg);
                    }
                } else {
                    controlEntity.firstCmd = RobotCmdType.CMD_STOP_CHARGE;
                    AppHolder.getInstance().getMqtt().getTaskControl(sn,new Gson().toJson(controlEntity));
                }
            }

        });
    }
    private void initData() {
        Heart heart = RobotDataManager.getInstance().get(sn);
        if(heart == null){
            finish();
            return;
        }
        setBat(heart.bat);
        updateUi(heart.chargeState);
    }
    private void setBat(int curBat) {
        if (tvChargeLevel == null || tvChargeState == null || waveViewCircle == null) {
            return;
        }
        if (curBat <= 20) {
            tvChargeLevel.setTextColor(ContextCompat.getColor(this, R.color.text_low_battery));
            tvChargeState.setTextColor(ContextCompat.getColor(this, R.color.text_low_battery));
        } else {
            tvChargeLevel.setTextColor(ContextCompat.getColor(this, R.color.white));
            tvChargeState.setTextColor(ContextCompat.getColor(this, R.color.white));
        }
        tvChargeLevel.setText(String.format(getString(R.string.text_bat), curBat));
        waveViewCircle.setProgress(curBat);
    }

    // 更新UI
    private void updateUi(int currentState) {
        Log.e("wangyin_充电状态","机器人状态:" + currentState);
        if (RobotChargeState.EMPTY == currentState) {
            // 非充电充
            cbSwitch.setChecked(false);
            tvChargeState.setText(R.string.text_dump_energy);
        } else if (RobotChargeState.FULL == currentState) {
            // 充满
            cbSwitch.setChecked(true);
            tvChargeState.setText(R.string.text_charging);
        } else if (RobotChargeState.ING == currentState) {
            // 充电中
            cbSwitch.setChecked(true);
            tvChargeState.setText(R.string.text_charging);
        } else if (RobotChargeState.NAV == currentState) {
            cbSwitch.setChecked(true);
            tvChargeState.setText(R.string.text_go_to_charge);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ChargeStateEvent event) {
        if(!TextUtils.equals(event.sn,sn)) return;
        setBat(event.bat);
        updateUi(event.state);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
