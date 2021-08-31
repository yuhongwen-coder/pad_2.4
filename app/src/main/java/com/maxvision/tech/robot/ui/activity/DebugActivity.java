package com.maxvision.tech.robot.ui.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Switch;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.maxvision.tech.mqtt.MQTTManager;
import com.maxvision.tech.robot.R;
import com.maxvision.tech.robot.entity.cons.SpConstants;
import com.maxvision.tech.robot.ui.view.IpLayout;
import com.maxvision.tech.robot.utils.CustomToast;
import com.maxvision.tech.robot.utils.KeyboardUtils;
import com.maxvision.tech.robot.utils.SpUtils;
import com.maxvision.tech.webrtc.manager.WebrtcUtil;


/**
 * name: wy
 * date: 2021/4/12
 * desc:
 */
public class DebugActivity extends AppCompatActivity implements IpLayout.SureListener {

    /**
     * 消息服务器IP设置,webrtc服务器设置
     */
    private IpLayout msgIP,webIp;

    /**
     * 呼叫设置
     */
    private EditText edt_x, edt_y,edt_a;
    private Switch item_call_switch;
    private Button btn_clickxy;

    private RelativeLayout rl_content;

    public DebugActivity() {
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);

        setIP();
        setCall();

        findViewById(R.id.back).setOnClickListener(v -> finish());

        rl_content = findViewById(R.id.rl_content);
        rl_content.setOnClickListener(v -> {
            rl_content.requestFocus();
            if (KeyboardUtils.isSoftInputShow(DebugActivity.this)) {
                KeyboardUtils.closeKeyboard(rl_content,DebugActivity.this);
            }
        });
    }

    private void setCall() {
        item_call_switch = findViewById(R.id.item_call_switch);
        edt_x = findViewById(R.id.edt_x);
        edt_y = findViewById(R.id.edt_y);
        edt_a = findViewById(R.id.edt_a);
        btn_clickxy = findViewById(R.id.btn_clickxy);


        boolean isOpen = SpUtils.getBoolean(SpConstants.SP_CALL_SWITCH);
        item_call_switch.setChecked(isOpen);

        edt_x.setVisibility(isOpen ? View.VISIBLE : View.GONE);
        edt_y.setVisibility(isOpen ? View.VISIBLE : View.GONE);
        edt_a.setVisibility(isOpen ? View.VISIBLE : View.GONE);
        btn_clickxy.setVisibility(isOpen ? View.VISIBLE : View.GONE);

        edt_x.setText(SpUtils.getString(SpConstants.SP_CALL_X,""));
        edt_y.setText(SpUtils.getString(SpConstants.SP_CALL_Y,""));
        edt_a.setText(SpUtils.getString(SpConstants.SP_CALL_A,""));

        item_call_switch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SpUtils.putBoolean(SpConstants.SP_CALL_SWITCH,isChecked);
            edt_x.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            edt_y.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            edt_a.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            btn_clickxy.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        btn_clickxy.setOnClickListener(v -> {
            String x = edt_x.getText().toString();
            String y = edt_y.getText().toString();
            String a = edt_a.getText().toString();
            if(TextUtils.isEmpty(x)){
                CustomToast.toastLong(CustomToast.TIP_ERROR,"请输入呼叫坐标X坐标");
                return;
            }
            if(TextUtils.isEmpty(y)){
                CustomToast.toastLong(CustomToast.TIP_ERROR,"请输入呼叫坐标Y坐标");
                return;
            }
            if(TextUtils.isEmpty(a)){
                CustomToast.toastLong(CustomToast.TIP_ERROR,"请输入呼叫坐标角度");
                return;
            }
            SpUtils.putString(SpConstants.SP_CALL_X,x);
            SpUtils.putString(SpConstants.SP_CALL_Y,y);
            SpUtils.putString(SpConstants.SP_CALL_A,a);
            CustomToast.toastLong(CustomToast.TIP_SUCCESS,"设置成功");
        });

    }

    private void setIP() {
        msgIP = findViewById(R.id.ip_msg);
        webIp = findViewById(R.id.ip_web);
        msgIP.setIp(SpUtils.getString(SpConstants.SP_MQTT_IP,"open.maxvision.com.cn"));
        msgIP.setPort(SpUtils.getString(SpConstants.SP_MQTT_PORT,"1883"));
        msgIP.setSureListener(msgIP.getId(),this);
        webIp.setSureListener(webIp.getId(),this);
        webIp.setIp(SpUtils.getString(SpConstants.SP_WEBRTC_IP,"47.111.68.11"));
        webIp.setPort(SpUtils.getString(SpConstants.SP_WEBRTC_PORT,"3000"));
    }

    @Override
    public void onSure(int viewId, String ip, String port) {
        if (msgIP.getId() == viewId) {
            SpUtils.putString(SpConstants.SP_MQTT_IP, ip);
            SpUtils.putString(SpConstants.SP_MQTT_PORT, port);
            String newIp = "tcp://" + ip + ":" + port;
            MQTTManager.getInstance().setIP(newIp);
        } else if (webIp.getId() == viewId) {
            SpUtils.putString(SpConstants.SP_WEBRTC_IP, ip);
            SpUtils.putString(SpConstants.SP_WEBRTC_PORT, port);
            WebrtcUtil.setIpAndPort(ip,port);
        }
        CustomToast.toastLong(CustomToast.TIP_SUCCESS, "设置成功");
    }
}