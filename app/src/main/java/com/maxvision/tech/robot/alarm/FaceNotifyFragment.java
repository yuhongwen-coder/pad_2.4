package com.maxvision.tech.robot.alarm;

import android.content.Context;
import android.os.Bundle;

import androidx.lifecycle.Observer;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.maxvision.tech.robot.R;
import com.maxvision.tech.robot.db.FaceListAlarmDb;

import butterknife.BindView;

/**
 * Created by yuhongwen
 * on 2021/4/8
 * 报警类型Fragment
 */
public class FaceNotifyFragment extends BaseAlarmFragment{

    @BindView(R.id.rl_black_view)
    RecyclerView rlFaceAlarm;

    /**
     * 初始哈 报警类型
     * @return 返回报警类型
     */
    @Override
    public int getFragmentView() {
        return R.layout.fragment_face_alarm_list;
    }

    /**
     * 获取报警数据
     */
    @Override
    protected void initData(AlarmNotifyViewModel viewModel,Context context) {
        rlFaceAlarm.setLayoutManager(new LinearLayoutManager(context));
        FaceAlarmAdapter listAdapter = new FaceAlarmAdapter(context);
        Observer<PagedList<FaceListAlarmDb>> observer = listAdapter::submitList;
        viewModel.getFaceAlarmData(robotSn).observe(this,observer);
        rlFaceAlarm.setAdapter(listAdapter);
    }

    /**
     * 构造Fragment
     * @return 返回实例
     */
    public static FaceNotifyFragment newInstance(String robotSn) {
        FaceNotifyFragment fragment = new FaceNotifyFragment();
        Bundle bundle = new Bundle();
        bundle.putString("robotSn", robotSn);
        fragment.setArguments(bundle);
        return fragment;
    }
}
