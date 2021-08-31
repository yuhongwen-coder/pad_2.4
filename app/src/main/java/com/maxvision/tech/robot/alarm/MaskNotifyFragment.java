package com.maxvision.tech.robot.alarm;

import android.content.Context;
import android.os.Bundle;

import androidx.lifecycle.Observer;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jcodecraeer.xrecyclerview.XRecyclerView;
import com.maxvision.tech.robot.R;
import com.maxvision.tech.robot.db.MaskListAlarmDb;
import com.maxvision.tech.robot.ui.view.RecyclerItemLine;
import com.maxvision.tech.robot.utils.DensityUtil;

import butterknife.BindView;

/**
 * Created by yuhongwen
 * on 2021/4/8
 */
public class MaskNotifyFragment extends BaseAlarmFragment{

    @BindView(R.id.rl_face_alarm_view)
    RecyclerView rlFaceAlarm;

    @Override
    public int getFragmentView() {
        return R.layout.fragment_kzjc_alarm_list;
    }

    @Override
    protected void initData(AlarmNotifyViewModel alarmTableModel,Context context) {
        rlFaceAlarm.setLayoutManager(new GridLayoutManager(context,3));
        rlFaceAlarm.addItemDecoration(new RecyclerItemLine(DensityUtil.dip2px(context,20),"",-1));
        MaskAlarmAdapter adapter = new MaskAlarmAdapter(context);
        Observer<PagedList<MaskListAlarmDb>> observer = pagedList -> {
            // LiveData监听到时数据库数据变化后，直接回到到这里
            adapter.submitList(pagedList);
        };
        alarmTableModel.getMaskAlarmData(robotSn).observe(this, observer);
        rlFaceAlarm.setAdapter(adapter);
    }

    /**
     * 构造Fragment
     * @return 返回实例
     */
    public static MaskNotifyFragment newInstance(String robotSn) {
        MaskNotifyFragment fragment = new MaskNotifyFragment();
        Bundle bundle = new Bundle();
        bundle.putString("robotSn", robotSn);
        fragment.setArguments(bundle);
        return fragment;
    }
}
