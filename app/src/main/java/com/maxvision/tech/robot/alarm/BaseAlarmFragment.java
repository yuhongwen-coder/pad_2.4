package com.maxvision.tech.robot.alarm;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProviders;

import org.greenrobot.eventbus.EventBus;

import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by yuhongwen
 * on 2021/4/8
 *
 * 报警类型Fragment 抽象类
 * 每个具体的报警列表，只需要实现这个Fragemt就可以了，在每个具体的报警列表中，完成各自的 ui 布局和 数据请求
 */
public abstract class BaseAlarmFragment extends Fragment {
    private Context mContext;
    private FragmentActivity mActivity;
    private Unbinder mUnbinder;
    protected String robotSn;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
        mActivity = getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.i("yhw_alarm","onCreateView inflater view ");
        View view = inflater.inflate(getFragmentView(),null,true);
        mUnbinder = ButterKnife.bind(this,view);
        return view;
    }

    public abstract int getFragmentView();

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Log.i("yhw_alarm","onViewCreated init data");
        super.onViewCreated(view, savedInstanceState);
        AlarmNotifyViewModel mAlarmTableModel = ViewModelProviders.of(mActivity).get(AlarmNotifyViewModel.class);
        initData(mAlarmTableModel,mContext);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("yhw_alarm","onCreate init enter");
        Bundle bundle = getArguments();
        if (bundle == null) return;
        this.robotSn = bundle.getString("robotSn");
    }

    @Override
    public void onResume() {
        Log.i("yhw_alarm","onResume interactive");
        super.onResume();
    }

    protected abstract void initData(AlarmNotifyViewModel alarmNotifyViewModel,Context context);

    @Override
    public void onDestroyView() {
        Log.i("yhw_alarm","onDestroyView enter");
        super.onDestroyView();
        if (mUnbinder != null){mUnbinder.unbind();}
    }
}
