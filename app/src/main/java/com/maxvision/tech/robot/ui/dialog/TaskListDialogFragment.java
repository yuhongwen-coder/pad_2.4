package com.maxvision.tech.robot.ui.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.gson.Gson;
import com.jcodecraeer.xrecyclerview.XRecyclerView;
import com.maxvision.tech.mqtt.MQTTManager;
import com.maxvision.tech.mqtt.RobotState;
import com.maxvision.tech.mqtt.entity.BaseTaskEntity;
import com.maxvision.tech.mqtt.entity.Heart;
import com.maxvision.tech.mqtt.entity.TaskControlEntity;
import com.maxvision.tech.mqtt.entity.TaskControlResponse;
import com.maxvision.tech.mqtt.entity.XyTaskControlResponse;
import com.maxvision.tech.mqtt.entity.state.PadIntercomState;
import com.maxvision.tech.mqtt.entity.state.RobotCmdType;
import com.maxvision.tech.mqtt.entity.state.RobotIntercomState;
import com.maxvision.tech.mqtt.entity.state.RobotStateHint;
import com.maxvision.tech.mqtt.entity.state.RobotTaskState;
import com.maxvision.tech.mqtt.entity.state.RobotXdzyState;
import com.maxvision.tech.mqtt.entity.state.RobotZnxyState;
import com.maxvision.tech.robot.AppHolder;
import com.maxvision.tech.robot.R;
import com.maxvision.tech.robot.alarm.AlarmNotifyActivity;
import com.maxvision.tech.robot.alarm.AlarmSwitchActivity;
import com.maxvision.tech.robot.aop.FastClick;
import com.maxvision.tech.robot.entity.event.RobotEvent;
import com.maxvision.tech.robot.entity.event.TaskEvent;
import com.maxvision.tech.robot.ui.activity.ChargeManageActivity;
import com.maxvision.tech.robot.face.FaceUploadActivity;
import com.maxvision.tech.robot.base.BaseDialogFragment;
import com.maxvision.tech.robot.entity.FunctionEntity;
import com.maxvision.tech.robot.manager.RobotDataManager;
import com.maxvision.tech.robot.ui.activity.DialogueActivity;
import com.maxvision.tech.robot.ui.activity.RobotSettingActivity;
import com.maxvision.tech.robot.ui.adapter.FunctionAdapter;
import com.maxvision.tech.robot.ui.adapter.TaskAdapter;
import com.maxvision.tech.robot.utils.CameraUtil;
import com.maxvision.tech.robot.utils.CustomToast;
import com.maxvision.tech.robot.utils.NetworkUtils;
import com.maxvision.tech.robot.utils.RobotUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import java.util.List;

import static com.maxvision.tech.mqtt.entity.BaseAlarmEntity.ALARM_FACE_STRING;

/**
 * name: wy
 * date: 2021/3/25
 * desc:机器人任务列表
 */
public class TaskListDialogFragment extends BaseDialogFragment implements View.OnClickListener, FunctionAdapter.OnClickItemListener, TaskAdapter.OnClickItemListener {

    private Context mContext;
    private TaskAdapter taskAdapter;
    private TextView tvEmpty;
    private XRecyclerView rvTask;
    private final Handler handler = new Handler(Looper.getMainLooper());

    public static TaskListDialogFragment getInstance(String sn){
        TaskListDialogFragment fragment = new TaskListDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString("sn",sn);
        fragment.setArguments(bundle);
        return fragment;
    }

    private Heart heart;
    private OnDismissListener onDismissListener;
    private String sn;
    private final List<BaseTaskEntity> taskList = new ArrayList<>();

    @Override
    public void onStart() {
        super.onStart();
        Window window = getDialog().getWindow();
        WindowManager.LayoutParams windowParams = window.getAttributes();
        int w = getResources().getDisplayMetrics().widthPixels;
        int h = getResources().getDisplayMetrics().heightPixels;
        windowParams.width = w / 2;
        windowParams.height = (int) (h * 0.8);
        window.setAttributes(windowParams);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("ZJJCONTEXT","onCreate");
        Bundle bundle = getArguments();
        if(bundle == null) return;
        sn = bundle.getString("sn");
        heart = RobotDataManager.getInstance().get(sn);
        if(heart == null){
            dismiss();
        }
        EventBus.getDefault().register(this);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Log.e("ZJJCONTEXT","onAttach:"+context);
        mContext = context;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.e("ZJJCONTEXT","onCreateView");
        return inflater.inflate(R.layout.dialog_tasklist_view,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.e("ZJJCONTEXT","onViewCreated");
        view.findViewById(R.id.iv_close).setOnClickListener(this);

        TextView tv_title = view.findViewById(R.id.tv_title);

        String title = heart.name+"("+ heart.sn +")";
        tv_title.setText(title);

        //功能项
        RecyclerView rc_function = view.findViewById(R.id.rc_function);
        FunctionAdapter adapter = new FunctionAdapter(getContext(),configRobotFunction(),heart.isAlarm);
        adapter.setOnClickItemListener(this);
        rc_function.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL,false));
        rc_function.setAdapter(adapter);
        rvTask = view.findViewById(R.id.rc_task);

        // 初始化数据
        tvEmpty = view.findViewById(R.id.tv_task_empty);
        if (TextUtils.equals(heart.type,"2") && heart.xyMode == 0) {
            tvEmpty.setText(R.string.text_task_list_empty_xy);
        } else {
            tvEmpty.setText(R.string.text_task_list_empty);
        }
        taskAdapter = new TaskAdapter(getContext(),taskList,heart.xyMode);
        taskAdapter.setOnClickItemListener(this);
        rvTask.setLayoutManager(new LinearLayoutManager(getContext()));
        rvTask.setAdapter(taskAdapter);
        rvTask.setPullRefreshEnabled(true);
        rvTask.setLoadingMoreEnabled(false);
        rvTask.setLoadingListener(new XRecyclerView.LoadingListener() {
            @Override
            public void onRefresh() {
                refreshData();
            }

            @Override
            public void onLoadMore() {

            }
        });
        loadData();
        tvEmpty.setOnClickListener(v -> refreshData());
    }

    private void refreshData() {
        AppHolder.getInstance().getMqtt().getTaskList(sn, MQTTManager.PAD_SN);
        handler.postDelayed(() -> rvTask.refreshComplete(), 15 * 1000);
    }

    public void loadData() {
        rvTask.refreshComplete();
        List<BaseTaskEntity> taskList = RobotDataManager.getInstance().getTaskList(sn);
        // 任务项
        if (null == taskList || taskList.size() == 0) {
            rvTask.setVisibility(View.INVISIBLE);
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            rvTask.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(View.GONE);
            taskAdapter.setAllData(taskList);
        }
    }

    private List<FunctionEntity> configRobotFunction() {
        List<FunctionEntity> allFunction = FunctionEntity.getAllData();
        //如果算法为空
        if(heart.sufaState == null){
            allFunction.remove(FunctionEntity.alarmEntity);
            allFunction.remove(FunctionEntity.alarmSwitchEntity);
            allFunction.remove(FunctionEntity.faceEntity);
            return allFunction;
        }
        //判断人脸是否存在
        if(heart.sufaState.face == -1){
            allFunction.remove(FunctionEntity.faceEntity);
        }
        //判断报警列表或报警开关是否存在
        if(heart.sufaState.face == -1 && heart.sufaState.qybk == -1
                && heart.sufaState.hwcw == -1 && heart.sufaState.kzjc == -1){
            allFunction.remove(FunctionEntity.alarmEntity);
            allFunction.remove(FunctionEntity.alarmSwitchEntity);
        }

        int robotType = 2;
        try {
            robotType = Integer.valueOf(heart.type);
        } catch (NumberFormatException e) {

        }
        if (robotType != 2 && robotType != 3) {
            allFunction.remove(FunctionEntity.robotEntity);
        }
        return allFunction;
    }


    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.iv_close){
            dismiss();
        }
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        if (onDismissListener != null) {
            onDismissListener.onDismiss();
        }
    }

    public void setOnDismissListener(OnDismissListener onDismissListener) {
        this.onDismissListener = onDismissListener;
    }

    //功能项点击事件
    @Override
    public void onClickItem(FunctionEntity entity) {
        if (!heart.isLine) {
            CustomToast.toastLong(CustomToast.TIP_NORMAL,"您的机器人已经离线，请启动机器人上线");
            return;
        }
        switch (entity.id){
            case 1: //报警列表
                AlarmNotifyActivity.startActivity(mContext, ALARM_FACE_STRING,sn);
                //点击报警列表,消失红点
                if(heart.isAlarm){
                    heart.isAlarm = false;
                    EventBus.getDefault().post(new RobotEvent(RobotEvent.UPDATE_LINE_STATE,heart));
                }
                break;
            case 2: //人脸上传
                FaceUploadActivity.startActivity(mContext,sn);
                break;
            case 3: //自主充电
                ChargeManageActivity.startActivity(mContext,sn);
                break;
            case 4:
                AlarmSwitchActivity.startActivity(mContext,sn);
                break;
            case 5:
                try {
                    RobotSettingActivity.startActivity(mContext,Integer.parseInt(heart.type),sn);
                } catch (NumberFormatException e) {
                    Log.e("yhw_robotSetting","请注意机器人类型异常了");
                    RobotSettingActivity.startActivity(mContext,2,sn);
                }
                break;
            case 6: //智能对话
                //断网提示
                if(!NetworkUtils.isNetworkConnected(mContext)){
                    CustomToast.toast(false,CustomToast.TIP_NORMAL,getString(R.string.no_network));
                    return;
                }
                // 判断是否在执行任务
                Heart heart = RobotDataManager.getInstance().get(sn);
                int taskState = heart.taskState;
                int xdzyState = heart.xdzyState;
                int znxyState = heart.znxyState;
                if (RobotTaskState.ING == taskState || RobotXdzyState.ING == xdzyState
                        || RobotZnxyState.TASK_EXECUTING == znxyState) {
                    // 执行中,给出提示
                    CustomToast.toastLong(CustomToast.TIP_ERROR,getString(R.string.tip_zldh2));
                    return;
                }
                int intercomState = heart.intercomState;
                //平板是非空闲状态 或机器人是非空闲状态 不能进入智能对话界面
                if (AppHolder.getInstance().getPadCallState() != PadIntercomState.INTERCOM_NULL || intercomState != RobotIntercomState.INTERCOM_NULL) {
                    CustomToast.toast(false,CustomToast.TIP_NORMAL,getString(R.string.tip_zldh));
                    return;
                }
                String type = heart.type;
                boolean isNoCamera = RobotUtils.isNoCamera(type);
                //非协运 没有可用摄像头 提示
                if(!isNoCamera&& !CameraUtil.isCameraCanUse()){
                    CustomToast.toast(false,CustomToast.TIP_NORMAL,getString(R.string.camera_enable));
                    return;
                }
                DialogueActivity.startActivity(mContext,sn);
                break;
        }
        dismiss();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
    }

    // 任务项
    @FastClick(filterTime = 2000)
    @Override
    public void onClickItem(BaseTaskEntity entity) {
        if (!heart.isLine) {
            CustomToast.toastLong(CustomToast.TIP_NORMAL,"您的机器人已经离线，请启动机器人上线");
            return;
        }
        TaskControlEntity taskControlEntity = new TaskControlEntity();
        taskControlEntity.sn = MQTTManager.PAD_SN;
        taskControlEntity.taskId = entity.taskId;
        taskControlEntity.isTimeTask = entity.isTimeTask;
        RobotStateHint robotStateHint;
        if (1 == heart.xyMode) {
            if (RobotXdzyState.ING == entity.taskStatus) {
                // 执行中 --> 暂停
                robotStateHint = null;
                taskControlEntity.firstCmd = RobotCmdType.CMD_BREAK_XDZY;
            } else if (RobotXdzyState.EMPTY == entity.taskStatus) {
                // 空闲 --> 判断执行
                robotStateHint = RobotState.isCanXdzy(heart);
                taskControlEntity.firstCmd = RobotCmdType.CMD_XDZY;
            } else  {
                // 恢复/结束 -- 判断是恢复还是结束
                robotStateHint = new RobotStateHint(true,"请选择恢复任务还是结束任务,点击确定恢复,点击取消结束",RobotCmdType.CMD_RESUME_XDZY,RobotCmdType.CMD_STOP_XDZY,true);
            }
        } else if ("2".equals(heart.type)) {
            Log.e("yhw_znxy","select taskId = " + entity.taskId + "  running id = " + heart.runningId
                    + "  robot state = " + heart.znxyState + "  door state = " + heart.doorState);
            if (TextUtils.equals(entity.taskId,heart.runningId)) {
                robotStateHint = RobotState.isCanRunningZnxy(false,heart);
                if (heart.znxyState == RobotZnxyState.TASK_EXECUTING) {
                    taskControlEntity.firstCmd = RobotCmdType.CMD_PAUSE_ZNXY;
                } else {
                    taskControlEntity.firstCmd = RobotCmdType.CMD_ZNXY;
                }
            } else {
                // 初始化或者不是同一个任务
                robotStateHint = RobotState.isCanRunningZnxy(true,heart);
                taskControlEntity.firstCmd = RobotCmdType.CMD_ZNXY;
            }
        } else {
            if (RobotTaskState.ING == entity.taskStatus) {
                robotStateHint = null;
                taskControlEntity.firstCmd = RobotCmdType.CMD_STOP_TASK;
            } else {
                robotStateHint = RobotState.isCanTask(heart);
                taskControlEntity.firstCmd = RobotCmdType.CMD_TASK;
            }
        }
        if (robotStateHint != null) {
            if (robotStateHint.isDialog) {
                taskControlEntity.firstCmd = robotStateHint.firstCmd;
                HintDialogFragment tipDialogFragment = HintDialogFragment.getInstance(sn,robotStateHint,taskControlEntity);
                tipDialogFragment.show(getFragmentManager());
            } else {
                CustomToast.toast(true,CustomToast.TIP_ERROR,robotStateHint.msg);
            }
        } else {
            AppHolder.getInstance().getMqtt().getTaskControl(sn,new Gson().toJson(taskControlEntity));
        }
    }

    public interface OnDismissListener{
        void onDismiss();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(TaskEvent taskEvent) {
        TaskControlResponse commonResponse = taskEvent.getCommonResponse();
        if (commonResponse != null && TextUtils.equals(commonResponse.robotType,"2")) {
            refreshNotifyTask((XyTaskControlResponse) taskEvent.getCommonResponse());
            return;
        }
        loadData();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(this);
    }

    /**
     * 差异化定点刷新任务
     * @param response 上报的任务类型
     *  目前协运任务 ： 密码弹框，证件弹框，延时弹框 平板先不做，但是 应该让 response有 上报类型，便于后面需求改变和实现
     */
    private void refreshNotifyTask(XyTaskControlResponse response) {
        String notifyType = response.listenerType;
        Log.e("refreshNotifyTask", "notifyType = " + notifyType);
        if (TextUtils.equals(notifyType, "task_state")) {
            // 刷新状态 执行 ---执行中，执行中--暂停中
            refreshNotifyState();
        } else if (TextUtils.equals(notifyType,"stop_task_success")) {
            // 任务已经停止
            CustomToast.toast(true, CustomToast.TIP_SUCCESS, "本次协运任务已经停止");
            refreshNotifyState();
        } else if (TextUtils.equals(notifyType,"stop_task_fail")) {
            CustomToast.toast(true, CustomToast.TIP_SUCCESS, "您的协运任务已经停止，请取出物品");
            refreshNotifyState();
        }
    }

    private void refreshNotifyState() {
        List<BaseTaskEntity> taskList = RobotDataManager.getInstance().getTaskList(sn);
        if (null == taskList || taskList.size() == 0) {
            rvTask.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            rvTask.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(View.GONE);
            taskAdapter.setAllData(taskList);
        }
    }
}