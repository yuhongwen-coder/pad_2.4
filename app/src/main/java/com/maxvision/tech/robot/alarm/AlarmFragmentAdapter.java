package com.maxvision.tech.robot.alarm;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.maxvision.tech.mqtt.entity.AlarmTypeEntity;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by yuhongwen
 * on 2021/4/8
 * 报警列表 Adapter
 */
public class AlarmFragmentAdapter extends FragmentPagerAdapter {
    private final List<AlarmTypeEntity> alarmType;
    private String robotSn;
    private Map<String,BaseAlarmFragment> mTypeFragment = new LinkedHashMap<>();
    private List<String> mTitles = new ArrayList<>();

    public AlarmFragmentAdapter(@NonNull FragmentManager fm, List<AlarmTypeEntity> listType,String robotSn) {
        super(fm);
        this.alarmType = listType;
        this.robotSn = robotSn;
        initFragment();
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        String title = mTitles.get(position);
        return mTypeFragment.get(title);
    }

    @Override
    public int getCount() {
        return mTypeFragment.size();
    }

    private void initFragment() {
        for (AlarmTypeEntity mTypeModel : alarmType) {
            String title = mTypeModel.getAlarmMessage();
            int robotFunType = mTypeModel.getAlarmType();
            // [{"title":"人脸识别","type":100},{"title":"红外测温","type":102},{"title":"口罩检测","type":103},{"title":"区域布控","type":101}]
            if (robotFunType == 101){
                mTypeFragment.put(title,AreaMonitorNotifyFragment.newInstance(robotSn));
                mTitles.add(title);
            }else if (robotFunType == 102){
                mTypeFragment.put(title,TempNotifyFragment.newInstance(robotSn));
                mTitles.add(title);
            }else if (robotFunType == 100){
                mTypeFragment.put(title,FaceNotifyFragment.newInstance(robotSn));
                mTitles.add(title);
            }else if (robotFunType == 103) {
                mTypeFragment.put(title,MaskNotifyFragment.newInstance(robotSn));
                mTitles.add(title);
            }
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mTitles.get(position);
    }
}
