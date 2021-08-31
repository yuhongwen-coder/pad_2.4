package com.maxvision.tech.robot.ui.view;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.Nullable;
import com.google.gson.Gson;
import com.maxvision.tech.mqtt.MQTTManager;
import com.maxvision.tech.mqtt.entity.Heart;
import com.maxvision.tech.mqtt.entity.LocationEntity;
import com.maxvision.tech.mqtt.entity.TaskControlEntity;
import com.maxvision.tech.mqtt.entity.state.RobotCmdType;
import com.maxvision.tech.robot.AppHolder;
import com.maxvision.tech.robot.R;
import com.maxvision.tech.robot.entity.cons.SpConstants;
import com.maxvision.tech.robot.utils.CustomToast;
import com.maxvision.tech.robot.utils.RobotDispatch;
import com.maxvision.tech.robot.utils.RobotUtils;
import com.maxvision.tech.robot.utils.SpUtils;

/**
 * name: wy
 * date: 2021/4/21
 * desc: 机器人呼叫调度代码
 */
public class RobotDispatcheLinearLayout extends LinearLayout implements RobotDispatch.RobotDispatchListener {

    //机器人呼叫按钮
    private final Button btn_robot_call;
    //机器人呼叫提示排队
    private final TextView tv_robot_tip;

    //机器人调度
    private RobotDispatch robotDispatch;
    //调度机器人SN号
    private String despatchSN;
    //当前SN设置的X、Y坐标
    private double x,y,a;
    private Handler mHandler = new Handler(Looper.getMainLooper());

    public RobotDispatcheLinearLayout(Context context) {
        this(context,null);
    }

    public RobotDispatcheLinearLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public RobotDispatcheLinearLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View view = View.inflate(context, R.layout.item_dispatche_robot,null);
        btn_robot_call = view.findViewById(R.id.btn_robot_call);
        tv_robot_tip = view.findViewById(R.id.tv_robot_tip);

        //机器人调度
        btn_robot_call.setOnClickListener(v -> {
            String x = SpUtils.getString(SpConstants.SP_CALL_X);
            String y = SpUtils.getString(SpConstants.SP_CALL_Y);
            String a = SpUtils.getString(SpConstants.SP_CALL_A);
            if(TextUtils.isEmpty(x) || TextUtils.isEmpty(y) || TextUtils.isEmpty(a)){
                CustomToast.toastLong(CustomToast.TIP_ERROR,"请配置需要呼叫导航点坐标");
                return;
            }

            if(robotDispatch != null){
                //判断是否取消导航
                robotDispatch.addDispatch();
                setRobotTip();
            }
        });

        addView(view);
    }

    public void setCallVisibility(boolean isShow){
        //功能是否开启
        if(isShow){
            if(robotDispatch == null){
                robotDispatch = new RobotDispatch();
            }
            setRobotTip();
        }else{ //功能关闭
            if(robotDispatch == null) return;
            robotDispatch.disposeDispatch();
            robotDispatch = null;
            despatchSN = "";
        }
    }

    //机器人呼叫，实时计算当前机器人距离
    public void despatchCall(LocationEntity navigation){
        if(!TextUtils.equals(navigation.sn,despatchSN)) return;
        double d = RobotUtils.getDistance(navigation.x,navigation.y,x,y);
        mHandler.post(() -> {
            if(d <= 1.5d){
                despatchSN = "";
                btn_robot_call.setText("呼叫");
                btn_robot_call.setBackgroundResource(R.drawable.shape_backgroung_round_call);
                tv_robot_tip.setText("机器人已到达");
                //机器人已到达
                robotDispatch.disposeDispatch();
            }else{
                String s = "机器人距离"+((int)(d))+"米";
                tv_robot_tip.setText(s);
            }
        });

    }

    private void setRobotTip(){
        robotDispatch.setListener(this);
        if(robotDispatch == null) return;
        String str;
        if(robotDispatch.isCall()){
            str = "请等待，正在调遣机器人";
            btn_robot_call.setText("取消");
            btn_robot_call.setBackgroundResource(R.drawable.shape_backgroung_round_call2);
        }else{
            str = "点击呼叫机器人";
            btn_robot_call.setText("呼叫");
            btn_robot_call.setBackgroundResource(R.drawable.shape_backgroung_round_call);
            despatchSN = "";
        }
        tv_robot_tip.setText(str);
    }

    @Override
    public void onDispatch(Heart heart) {
        if(robotDispatch == null) return;
        Log.e("wangyin_调度","有空闲机器人："+heart.sn);
        despatchSN = heart.sn;
        x = Double.parseDouble(SpUtils.getString(SpConstants.SP_CALL_X));
        y = Double.parseDouble(SpUtils.getString(SpConstants.SP_CALL_Y));
        a = Double.parseDouble(SpUtils.getString(SpConstants.SP_CALL_A));
        navigateCall(true);
    }

    @Override
    public void onTimeOut() {
        navigateCall(false);
        despatchSN = "";
        btn_robot_call.setText("呼叫");
        btn_robot_call.setBackgroundResource(R.drawable.shape_backgroung_round_call);
        tv_robot_tip.setText("机器人呼叫超时");
        robotDispatch.disposeDispatch();
    }

    @Override
    public void onCancelNavigation() {
        //取消导航
        navigateCall(false);
    }

    private void navigateCall(boolean isCall){
        if(TextUtils.isEmpty(despatchSN)) return;
        TaskControlEntity taskControlEntity = new TaskControlEntity();
        taskControlEntity.sn = MQTTManager.PAD_SN;
        taskControlEntity.firstCmd = isCall ? RobotCmdType.CMD_NAV : RobotCmdType.CMD_STOP_NAV;
        taskControlEntity.x = x;
        taskControlEntity.y = y;
        taskControlEntity.angel = a;
        AppHolder.getInstance().getMqtt().getTaskControl(despatchSN,new Gson().toJson(taskControlEntity));
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (null != mHandler) {
            mHandler.removeCallbacksAndMessages(this);
            mHandler = null;
        }
    }
}