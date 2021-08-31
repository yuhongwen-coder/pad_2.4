package com.maxvision.tech.robot.alarm;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.maxvision.tech.mqtt.MQTTManager;
import com.maxvision.tech.mqtt.entity.AlarmSwitchEntity;
import com.maxvision.tech.mqtt.entity.Heart;
import com.maxvision.tech.robot.AppHolder;
import com.maxvision.tech.robot.R;
import com.maxvision.tech.robot.manager.RobotDataManager;
import com.maxvision.tech.robot.utils.CustomToast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static com.maxvision.tech.mqtt.entity.BaseAlarmEntity.ALARM_AREA_STRING;
import static com.maxvision.tech.mqtt.entity.BaseAlarmEntity.ALARM_FACE_STRING;
import static com.maxvision.tech.mqtt.entity.BaseAlarmEntity.ALARM_MASK_STRING;
import static com.maxvision.tech.mqtt.entity.BaseAlarmEntity.ALARM_TEMP_STRING;
import static com.maxvision.tech.mqtt.entity.AlarmTypeEntity.ALARM_AREA;
import static com.maxvision.tech.mqtt.entity.AlarmTypeEntity.ALARM_FACE;
import static com.maxvision.tech.mqtt.entity.AlarmTypeEntity.ALARM_MASK;
import static com.maxvision.tech.mqtt.entity.AlarmTypeEntity.ALARM_TEMP;

/**
 * Created by yuhongwen
 * on 2021/4/14
 *
 */
public class AlarmSwitchActivity extends AppCompatActivity implements AlarmSwitchAdapter.FunctionClickListener {
    private Unbinder binder;
    @BindView(R.id.alarm_switch_rl)
    RecyclerView alarmSwitchRl;
    @BindView(R.id.alarm_switch_back)
    ImageView backImage;
    private List<AlarmSwitchEntity> alarmTypeList = new ArrayList<>();
    private String sn;
    private Disposable timeOutDispose;
    private AlarmSwitchAdapter adapter;
    private int mFace;
    private int mArea;
    private int mTemp;
    private int mMask;
    private int checkSwitch;
    private AlarmSwitchEntity alarmFaceEntity = new AlarmSwitchEntity(ALARM_FACE,1,ALARM_FACE_STRING,R.mipmap.face_switch_open);
    private AlarmSwitchEntity alarmAreaEntity = new AlarmSwitchEntity(ALARM_AREA,1,ALARM_AREA_STRING,R.mipmap.area_switch_open);
    private AlarmSwitchEntity alarmTempEntity = new AlarmSwitchEntity(ALARM_TEMP,1,ALARM_TEMP_STRING,R.mipmap.temp_switch_open);
    private AlarmSwitchEntity alarmMaskEntity = new AlarmSwitchEntity(ALARM_MASK,1,ALARM_MASK_STRING,R.mipmap.mask_switch_open);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alarm_switch_activity_2);
        binder = ButterKnife.bind(this);
        sn = getIntent().getStringExtra("sn");
        Heart heart = RobotDataManager.getInstance().get(sn);
        initData(heart);
        configRecyclerView();
        backImage.setOnClickListener(v -> finish());
        looperSufaState();
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(AlarmSwitchEntity switchEntity) {
        Log.i("alarm_switch","算法控制开关返回了 = " + switchEntity);
        if (switchEntity == null) {
            return;
        }
        if (switchEntity.getErrorCode() == -1) {
            // 控制失败回退结果
            if (checkSwitch == 1) {
                // 选择开，提示开启失败
                switch (switchEntity.alarmType) {
                    case ALARM_FACE:
                        CustomToast.toastLong(CustomToast.TIP_ERROR,"人脸开启失败");
                        break;
                    case ALARM_AREA:
                        CustomToast.toastLong(CustomToast.TIP_ERROR,"区域布控开启失败");
                        break;
                    case ALARM_MASK:
                        CustomToast.toastLong(CustomToast.TIP_ERROR,"口罩检测开启失败");
                        break;
                    case ALARM_TEMP:
                        CustomToast.toastLong(CustomToast.TIP_ERROR,"红外测温开启失败");
                        break;
                    default:
                        break;
                }
            } else {
                // 选择关闭，提示关闭失败
                switch (switchEntity.alarmType) {
                    case ALARM_FACE:
                        CustomToast.toastLong(CustomToast.TIP_ERROR,"人脸关闭失败");
                        break;
                    case ALARM_AREA:
                        CustomToast.toastLong(CustomToast.TIP_ERROR,"区域布控关闭失败");
                        break;
                    case ALARM_MASK:
                        CustomToast.toastLong(CustomToast.TIP_ERROR,"口罩检测关闭失败");
                        break;
                    case ALARM_TEMP:
                        CustomToast.toastLong(CustomToast.TIP_ERROR,"红外测温关闭失败");
                        break;
                    default:
                        break;
                }
            }
        }
    }

    private void configRecyclerView() {
        adapter = new AlarmSwitchAdapter(this,R.layout.item_alarm_switch_2,alarmTypeList,sn);
        adapter.setOnFunctionClickListener(this);
        alarmSwitchRl.setAdapter(adapter);
        int spanCount = alarmTypeList.size();
        // 扩展宫格数量 大于4个 或者小于4个
        if (spanCount >= 4) spanCount = 4;
        alarmSwitchRl.setLayoutManager(new GridLayoutManager(this,spanCount));
    }

    private void initData(Heart heart) {
        if (heart == null || heart.sufaState == null) {
            Log.i("yhw_alarm","get " + sn + "的 heart is null");
            return;
        }
        alarmTypeList.clear();
        if (heart.sufaState.face != -1) {
            alarmFaceEntity.setIsOpenSwitch(heart.sufaState.face);
            alarmTypeList.add(alarmFaceEntity);
        }
        if (heart.sufaState.qybk != -1) {
            alarmAreaEntity.setIsOpenSwitch(heart.sufaState.qybk);
            alarmTypeList.add(alarmAreaEntity);
        }
        if (heart.sufaState.hwcw != -1) {
            alarmTempEntity.setIsOpenSwitch(heart.sufaState.hwcw);
            alarmTypeList.add(alarmTempEntity);
        }
        if (heart.sufaState.kzjc != -1) {
            alarmMaskEntity.setIsOpenSwitch(heart.sufaState.kzjc);
            alarmTypeList.add(alarmMaskEntity);
        }
        mFace = heart.sufaState.face;
        mMask = heart.sufaState.kzjc;
        mTemp = heart.sufaState.hwcw;
        mArea = heart.sufaState.qybk;
    }

    private void looperSufaState() {
        timeOutDispose = Observable.interval(1200, TimeUnit.MILLISECONDS).observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(aLong -> {
                    Heart heart = RobotDataManager.getInstance().get(sn);
                    if (heart == null || heart.sufaState == null) {
                        Log.i("yhw_alarm","robotSn =" + sn + "的 heart is null");
                        return;
                    }
                    if (heart.sufaState.face == mFace && heart.sufaState.qybk == mArea
                         && heart.sufaState.hwcw == mTemp && heart.sufaState.kzjc == mMask) {
                        // 上报的和保存的都相同 就不需要刷新了
                        return;
                    }
                    if (heart.sufaState.face != mFace) {
                        mFace = heart.sufaState.face;
                        alarmFaceEntity.setIsOpenSwitch(mFace);
                        refreshSwitch(ALARM_FACE_STRING);
                    }
                    if (heart.sufaState.qybk != mArea) {
                        mArea = heart.sufaState.qybk;
                        alarmAreaEntity.setIsOpenSwitch(mArea);
                        refreshSwitch(ALARM_AREA_STRING);
                    }
                    if (heart.sufaState.hwcw != mTemp) {
                        mTemp = heart.sufaState.hwcw;
                        alarmTempEntity.setIsOpenSwitch(mTemp);
                        refreshSwitch(ALARM_TEMP_STRING);
                    }
                    if (heart.sufaState.kzjc != mMask) {
                        mMask = heart.sufaState.kzjc;
                        alarmMaskEntity.setIsOpenSwitch(mMask);
                        refreshSwitch(ALARM_MASK_STRING);
                    }
                });
    }

    private void refreshSwitch(String alarmString) {
        for (int i = 0; i < alarmTypeList.size(); i++) {
            if (TextUtils.equals(alarmString, alarmTypeList.get(i).getAlarmMessage())) {
                adapter.notifyItemChanged(i);
                break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (binder != null){binder.unbind();}
        if (timeOutDispose != null && !timeOutDispose.isDisposed()) {
            timeOutDispose.dispose();
        }
    }

    public static void startActivity(Context context, String sn) {
        Intent intent = new Intent(context, AlarmSwitchActivity.class);
        intent.putExtra("sn",sn);
        context.startActivity(intent);
    }

    @Override
    public void onSwitchClick(AlarmSwitchEntity entity) {
        if (entity == null) return;
        Log.d("onSwitchClick", "AlarmSwitchEntity:"+entity.toString());
        checkSwitch = entity.getIsOpenSwitch();
        entity.setPadSn(MQTTManager.PAD_SN);
        AppHolder.getInstance().getMqtt().publishFunctionState(sn,new Gson().toJson(entity));
    }


}
