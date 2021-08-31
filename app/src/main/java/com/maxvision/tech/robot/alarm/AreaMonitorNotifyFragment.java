package com.maxvision.tech.robot.alarm;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jcodecraeer.xrecyclerview.XRecyclerView;
import com.maxvision.tech.robot.R;
import com.maxvision.tech.robot.db.AreaMontiorAlarmDb;
import com.maxvision.tech.robot.ui.view.RecyclerItemLine;
import com.maxvision.tech.robot.utils.DensityUtil;

import butterknife.BindView;

/**
 * Created by yuhongwen
 * on 2021/4/8
 */
public class AreaMonitorNotifyFragment extends BaseAlarmFragment{
    @BindView(R.id.rv_area_monitor)
    XRecyclerView rvAreaMonitor;

    private Context mContext;

    @Override
    public int getFragmentView() {
        return R.layout.fragment_area_monitor;
    }


    @Override
    protected void initData(AlarmNotifyViewModel viewModel,Context context) {
        this.mContext = context;
        rvAreaMonitor.setLayoutManager(new GridLayoutManager(mContext,3));
        AreaMonitorAlarmAdapter adapter = new AreaMonitorAlarmAdapter(mContext);
        rvAreaMonitor.addItemDecoration(new RecyclerItemLine(DensityUtil.dip2px(mContext,20),"",-1));
        Observer observer = new Observer<PagedList<AreaMontiorAlarmDb>>() {
            @Override
            public void onChanged(PagedList<AreaMontiorAlarmDb> pagedList) {
                adapter.submitList(pagedList);
            }
        };
        viewModel.getAreaMonitorData(robotSn).observe(this,observer);
        rvAreaMonitor.setAdapter(adapter);
        rvAreaMonitor.setPullRefreshEnabled(true);
        rvAreaMonitor.setLoadingMoreEnabled(false);
        rvAreaMonitor.setLoadingListener(new XRecyclerView.LoadingListener() {
            @Override
            public void onRefresh() {
                Log.e("yhw_refresh","下拉刷新");
//                LiveData<PagedList<AreaMontiorAlarmDb>> liveData = viewModel.getAreaMonitorData(robotSn);
//                adapter.submitList(liveData.getValue());
//                rvAreaMonitor.refreshComplete();
            }

            @Override
            public void onLoadMore() {
                Log.e("yhw_refresh","上拉加载更是");
            }
        });
    }

    /**
     * 构造Fragment
     * @return 返回实例
     */
    public static AreaMonitorNotifyFragment newInstance(String robotSn) {
        AreaMonitorNotifyFragment fragment = new AreaMonitorNotifyFragment();
        Bundle bundle = new Bundle();
        bundle.putString("robotSn", robotSn);
        fragment.setArguments(bundle);
        return fragment;
    }

}
