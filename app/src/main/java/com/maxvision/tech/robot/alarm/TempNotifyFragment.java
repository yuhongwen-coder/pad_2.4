package com.maxvision.tech.robot.alarm;

import android.content.Context;
import android.os.Bundle;

import androidx.lifecycle.Observer;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.maxvision.tech.robot.R;
import com.maxvision.tech.robot.db.TempAlarmDb;
import com.maxvision.tech.robot.ui.view.RecyclerItemLine;
import com.maxvision.tech.robot.utils.DensityUtil;

import butterknife.BindView;

/**
 * Created by yuhongwen
 * on 2021/4/8
 */
public class TempNotifyFragment extends BaseAlarmFragment{
    @BindView(R.id.rl_lowtemp)
    RecyclerView tempAlarmRl;
    @Override
    public int getFragmentView() {
        return R.layout.fragment_low_temp;
    }

    @Override
    protected void initData(AlarmNotifyViewModel viewModel,Context context) {
        tempAlarmRl.setLayoutManager(new GridLayoutManager(context,2));
        tempAlarmRl.addItemDecoration(new RecyclerItemLine(DensityUtil.dip2px(context,20),"",-1));
        TempAlarmAdapter itemAdapter = new TempAlarmAdapter(context);
        Observer<PagedList<TempAlarmDb>> observer = pagedList -> {
            itemAdapter.submitList(pagedList);
        };
        viewModel.getTempAlarmData(robotSn).observe(this,observer);
        tempAlarmRl.setAdapter(itemAdapter);
    }

    /**
     * 构造Fragment
     * @return 返回实例
     */
    public static TempNotifyFragment newInstance(String robotSn) {
        TempNotifyFragment fragment = new TempNotifyFragment();
        Bundle bundle = new Bundle();
        bundle.putString("robotSn", robotSn);
        fragment.setArguments(bundle);
        return fragment;
    }
}
