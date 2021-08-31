package com.maxvision.tech.robot.ui.fragment;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.dou361.dialogui.DialogUIUtils;
import com.maxvision.tech.mqtt.MQTTManager;
import com.maxvision.tech.mqtt.entity.Heart;
import com.maxvision.tech.robot.AppHolder;
import com.maxvision.tech.robot.R;
import com.maxvision.tech.robot.manager.RobotDataManager;
import com.maxvision.tech.robot.map.NavigationView;
import com.maxvision.tech.robot.map.OnClickNavigateListener;
import com.maxvision.tech.robot.map.PointEntity;
import com.maxvision.tech.robot.ui.dialog.NavigateDialogFragment;
import com.maxvision.tech.robot.ui.dialog.TipDialogFragment;
import com.maxvision.tech.robot.ui.view.MyRockerView;
import com.maxvision.tech.robot.utils.FileConstant;
import java.io.File;
import java.util.List;

/**
 * @author MyPC
 * date: 2021/4/14
 * desc:拨号界面的地图
 */
public class CallNavigationFragment extends Fragment implements OnClickNavigateListener {
    private static final String TAG = "qs_CallNavigationFragment";
    private NavigationView mNvView;
    private String mSn = "";

    private Dialog mapDialog;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle == null) {
            return;
        }
        mSn = bundle.getString("sn");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_call_navigation, container, false);
        initView(view);
        return view;
    }

    private void initView(View view) {
        mNvView = view.findViewById(R.id.nv_view);
        //控制器
        MyRockerView mRvView = view.findViewById(R.id.rv_view);
        mRvView.setSn(mSn);

        mNvView.setOnClickNavigateListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadMap();
    }

    //加载地图
    private void loadMap() {
        Heart heart = RobotDataManager.getInstance().get(mSn);
        String path = FileConstant.getInstance().getPathMapImage() + heart.mapId + ".png";
        File file = new File(path);
        if (file.exists()) {
            //已存在
            setMap(path);
        } else {
            //不存在
            //需要去加载图片
            AppHolder.getInstance().getMqtt().getMap(mSn, heart.mapId);
            mapDialog = DialogUIUtils.showLoading(
                    getContext(),
                    getString(R.string.text_loading_mapdata),
                    true,
                    true,
                    false,
                    true).show();
        }
    }

    /**
     * 是否重新加载
     *
     * @param path 地图路径
     */
    public void setMap(String path) {
        //设置地图，加载地图
        hideMapLoadingDialog();
        //加载地图
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        mNvView.setImageBitmap(bitmap);
        //获取导航点坐标
        AppHolder.getInstance().getMqtt().getNavigation(mSn, MQTTManager.PAD_SN);
        //获取任务列表
        //AppHolder.getInstance().getMqtt().getTaskList(sn,MQTTManager.PAD_SN);
    }

    private void hideMapLoadingDialog() {
        if (mapDialog != null && mapDialog.isShowing()) {
            mapDialog.dismiss();
        }
    }

    @Override
    public void onClickNavigate(PointEntity point) {
        //点击导航点
        TipDialogFragment tipDialogFragment = TipDialogFragment.getInstance(mSn, point.x, point.y, point.angle, point.name);
        tipDialogFragment.show(getChildFragmentManager());
    }

    @Override
    public void onLongNavigate(PointEntity point) {
        //长按
        NavigateDialogFragment navigateDialogFragment = NavigateDialogFragment.getInstance(mSn, point.x, point.y, point.angle);
        navigateDialogFragment.show(getChildFragmentManager());
    }

    @Override
    public void onTouchXy(double x, double y) {

    }

    public void addNavigationList(List<PointEntity> list) {
        mNvView.addNavigationList(list);
    }

    public void updatePostion(double x,double y) {
        mNvView.updatePostion(x,y);
    }
}