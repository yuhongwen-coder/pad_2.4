package com.maxvision.tech.robot.alarm;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;

import com.maxvision.tech.mqtt.entity.Heart;
import com.maxvision.tech.robot.R;
import com.maxvision.tech.mqtt.entity.AlarmTypeEntity;
import com.maxvision.tech.robot.manager.RobotDataManager;

import java.util.ArrayList;
import java.util.List;

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
 * on 2021/4/6
 * 报警列表页面
 */
public class AlarmNotifyActivity extends AppCompatActivity {
    private List<AlarmTypeEntity> alarmTypeList = new ArrayList<>();
    private String mFragmentType = "";
    private CustomScrollViewPager tabPager;
    private CustomVerticalTab tabLayout;
    private ImageView back;
    private String robotSn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alarm_notify_activity);
        tabPager = findViewById(R.id.tab_pager);
        tabLayout = findViewById(R.id.tab_layout);
        back = findViewById(R.id.back);
        back.setOnClickListener(v -> finish());
        Intent intent = getIntent();
        mFragmentType = intent.getStringExtra("alarmType");
        robotSn = intent.getStringExtra("RobotSn");
        ViewModelProviders.of(this).get(AlarmNotifyViewModel.class);
        getAlarmTypeNum();
    }

    private void getAlarmTypeNum() {
        Heart heart = RobotDataManager.getInstance().get(robotSn);
        if (heart == null || heart.sufaState == null) {
            Log.i("yhw_alarm","get " + robotSn + "的 heart is null");
            return;
        }
        if (heart.sufaState.face != -1) {
            alarmTypeList.add(new AlarmTypeEntity(ALARM_FACE,ALARM_FACE_STRING));
        }
        if (heart.sufaState.qybk != -1) {
            alarmTypeList.add(new AlarmTypeEntity(ALARM_AREA,ALARM_AREA_STRING));
        }
        if (heart.sufaState.hwcw != -1) {
            alarmTypeList.add(new AlarmTypeEntity(ALARM_TEMP,ALARM_TEMP_STRING));
        }
        if (heart.sufaState.kzjc != -1) {
            alarmTypeList.add(new AlarmTypeEntity(ALARM_MASK,ALARM_MASK_STRING));
        }
        showAlarmFragment();
    }

    private void showAlarmFragment() {
        AlarmFragmentAdapter alarmFragmentAdapter =
                new AlarmFragmentAdapter(getSupportFragmentManager(),alarmTypeList,robotSn);
        tabPager.setAdapter(alarmFragmentAdapter);
        tabLayout.setupWithViewPager(tabPager);
        for (int i = 0 ;i< alarmTypeList.size();i++) {
            AlarmTypeEntity entity = alarmTypeList.get(i);
            if (TextUtils.equals(mFragmentType,entity.alarmMessage)) {
                tabLayout.setTabSelected(i);
                return;
            }
            continue;
        }
        tabLayout.setTabSelected(0);
    }

    public static void startActivity(Context context, String type,String sn) {
        Intent intent = new Intent(context, AlarmNotifyActivity.class);
        intent.putExtra("alarmType", type);
        intent.putExtra("RobotSn", sn);
        context.startActivity(intent);
    }
}
