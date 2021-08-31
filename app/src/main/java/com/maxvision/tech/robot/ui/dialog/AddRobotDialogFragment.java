package com.maxvision.tech.robot.ui.dialog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import com.google.gson.Gson;
import com.jcodecraeer.xrecyclerview.XRecyclerView;
import com.maxvision.tech.mqtt.entity.Heart;
import com.maxvision.tech.robot.R;
import com.maxvision.tech.robot.base.BaseDialogFragment;
import com.maxvision.tech.robot.entity.event.RobotEvent;
import com.maxvision.tech.robot.manager.RobotDataManager;
import com.maxvision.tech.robot.ui.adapter.AddRobotAdapter;
import com.maxvision.tech.robot.utils.CustomToast;
import com.maxvision.tech.robot.utils.SpUtils;
import org.greenrobot.eventbus.EventBus;
import java.util.ArrayList;
import java.util.List;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static com.maxvision.tech.robot.manager.RobotDataManager.ROBOT_SELECT_DATA;


/**
 * name: wy
 * date: 2021/3/25
 * desc:机器人添加
 */
public class AddRobotDialogFragment extends BaseDialogFragment implements View.OnClickListener {

    public static AddRobotDialogFragment getInstance(){
        return new AddRobotDialogFragment();
    }

    private XRecyclerView recyclerView;
    private AddRobotAdapter adapter;
    private final List<Heart> searchList = new ArrayList<>();

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
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_addrobot_view,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.recyclerview);
        view.findViewById(R.id.iv_close).setOnClickListener(this);
        view.findViewById(R.id.btn_ok).setOnClickListener(this);
        view.findViewById(R.id.btn_close).setOnClickListener(this);

        adapter = new AddRobotAdapter(getContext(),searchList);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(),2));
        recyclerView.setAdapter(adapter);
        recyclerView.setPullRefreshEnabled(true);
        recyclerView.setLoadingMoreEnabled(false);
        recyclerView.setLoadingListener(new XRecyclerView.LoadingListener() {
            @Override
            public void onRefresh() {
                loadData();
            }

            @Override
            public void onLoadMore() {

            }
        });
        loadData();
    }

    private void loadData(){
        searchList.clear();
        List<Heart> list = RobotDataManager.getInstance().getHeartRobot();
        for (Heart heart : list) {
            heart.isSel = heart.isSava;
        }
        searchList.addAll(list);
        adapter.notifyDataSetChanged();
        recyclerView.refreshComplete();
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.iv_close){
            dismiss();
        }else if(v.getId() == R.id.btn_ok){
            //确认
            List<Heart> list = new ArrayList<>();
            for(Heart h : searchList){
                h.isSava = h.isSel;
                if(h.isSel){ //选中的数据
                    list.add(h);
                }
            }
            SpUtils.putString(ROBOT_SELECT_DATA,new Gson().toJson(list));
            EventBus.getDefault().post(new RobotEvent(RobotEvent.UPDATE_ROBOT));
            CustomToast.toastLong(CustomToast.TIP_SUCCESS,"保存成功");
            dismiss();
        }else if(v.getId() == R.id.btn_close){
            dismiss();
        }
    }
}