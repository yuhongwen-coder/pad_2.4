package com.maxvision.tech.robot.ui.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.maxvision.tech.mqtt.entity.Heart;
import com.maxvision.tech.mqtt.entity.state.RobotDoorState;
import com.maxvision.tech.robot.AppHolder;
import com.maxvision.tech.robot.R;
import com.maxvision.tech.robot.entity.RobotOperationEntity;
import com.maxvision.tech.robot.manager.RobotDataManager;
import com.maxvision.tech.robot.ui.adapter.RobotOperationAdapter;
import com.maxvision.tech.robot.ui.view.RecyclerItemLine;
import com.maxvision.tech.robot.ui.view.RobotSettingView;
import com.maxvision.tech.robot.utils.CustomToast;
import com.maxvision.tech.robot.utils.DensityUtil;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static com.maxvision.tech.mqtt.entity.state.RobotStopState.EMPTY;
import static com.maxvision.tech.mqtt.entity.state.RobotStopState.STOP;
import static com.maxvision.tech.robot.entity.RobotOperationEntity.*;

/**
 * Created by yuhongwen
 * on 2021/4/16
 */
public class RobotSettingActivity extends AppCompatActivity implements RobotOperationAdapter.OperationListener {
    private RecyclerView operationRy;
    private int robotType;
    private ArrayList<RobotOperationEntity> listOperation = new ArrayList<>();
    private String robotSn;
    private int layoutId;
    private RobotSettingView stateFun1;
    private RobotSettingView stateFun2;
    private TextView doorType;
    private Disposable timeOutDispose;
    private ImageView robotImage;
    private AnimationDrawable openAnimator;
    private AnimationDrawable closeAnimator;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        robotType = getIntent().getIntExtra("robotType",-1);
        robotSn = getIntent().getStringExtra("robotSn");
        Heart heart = RobotDataManager.getInstance().get(robotSn);
        if (heart == null) {
            finish();
            return;
        }
        if (robotType == 2 && heart.xyMode == 0) {
            // 协运 开门 关门 复位
            listOperation = (ArrayList<RobotOperationEntity>) getXieyunOperation();
            layoutId = R.layout.robot_setting_xieyun_activity;
        } else {
            // 消毒  拆装  拆装结束
            listOperation = (ArrayList<RobotOperationEntity>) getXiaoduOperation();
            layoutId = R.layout.robot_setting_activity;
        }
        setContentView(layoutId);
        configAdapter();
        refreshWater(heart);
        findViewById(R.id.robot_setting_back).setOnClickListener(v -> finish());
        doorType = findViewById(R.id.robot_setting_root_door);
        robotImage = findViewById(R.id.robot_setting_root_left_parent);
        refreshDoorState(heart);
        initTime();
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private synchronized void refreshDoorState(Heart heart) {
        if (heart.doorState == RobotDoorState.OPEN_ING) {
            if (openAnimator == null) {
                robotImage.setBackground(getDrawable(R.drawable.open_door_animator));
                openAnimator = (AnimationDrawable) robotImage.getBackground();
            }
            openAnimator.start();
            if (closeAnimator != null) {
                closeAnimator.stop();
            }
        } else if (heart.doorState == RobotDoorState.CLOSE_ING) {
            if (closeAnimator == null) {
                robotImage.setBackground(getDrawable(R.drawable.close_door_animator));
                closeAnimator = (AnimationDrawable) robotImage.getBackground();
            }
            closeAnimator.start();
            if (openAnimator != null) {
                openAnimator.stop();
            }
        } else if (heart.doorState == RobotDoorState.OPEND) {
            robotImage.setBackground(getDrawable(R.mipmap.door_open_90));
            resetAnimator();
        }  else {
            robotImage.setBackground(getDrawable(R.mipmap.door_open_0));
            resetAnimator();
        }
        doorType.setText(RobotDoorState.getDoorState(heart.doorState));
    }

    private void resetAnimator() {
        if (openAnimator != null) {
            openAnimator.stop();
            openAnimator = null;
        }
        if (closeAnimator != null) {
            closeAnimator.stop();
            closeAnimator = null;
        }
    }

    private void refreshWater(Heart heart) {
        if (layoutId == R.layout.robot_setting_activity) {
            stateFun1 = findViewById(R.id.robot_setting_fun_1);
            stateFun2 = findViewById(R.id.robot_setting_fun_2);
            stateFun1.setHeart(R.id.robot_setting_fun_1,heart);
            stateFun2.setHeart(R.id.robot_setting_fun_2,heart);
        }
    }

    private void initTime() {
        timeOutDispose = Observable.interval(1000, TimeUnit.MILLISECONDS).observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(aLong -> {
                    Heart heart = RobotDataManager.getInstance().get(robotSn);
                    if (heart == null ) {
                        Log.i("yhw_alarm","robotSn =" + robotSn + "的 heart is null");
                        return;
                    }
                    refreshWater(heart);
                    refreshDoorState(heart);
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timeOutDispose != null && !timeOutDispose.isDisposed()) {
            timeOutDispose.dispose();
        }
        if (openAnimator != null) {
            openAnimator.stop();
            openAnimator = null;
        }
        if (closeAnimator != null) {
            closeAnimator.stop();
            closeAnimator = null;
        }
    }

    private void configAdapter() {
        RobotOperationAdapter adapter = new RobotOperationAdapter(R.layout.item_robot_recycler_setting,listOperation);
        operationRy = findViewById(R.id.robot_setting_operation_list);
        operationRy.setLayoutManager(new GridLayoutManager(this,2));
        operationRy.addItemDecoration(new RecyclerItemLine(DensityUtil.dip2px(this,23),"robot_setting",listOperation.size()));
        operationRy.setAdapter(adapter);
        operationRy.setHasFixedSize(true);
        adapter.setListener(this);
    }

    public static void startActivity(Context context,int robotType,String sn) {
        Intent intent = new Intent(context, RobotSettingActivity.class);
        intent.putExtra("robotType",robotType);
        intent.putExtra("robotSn",sn);
        context.startActivity(intent);
    }

    @Override
    public void onOperation(RobotOperationEntity operationEntity) {
        if (operationEntity == null) return;
        Heart heart = RobotDataManager.getInstance().get(robotSn);
        int operationType = operationEntity.getOperationType();
        switch (operationType) {
            case CHAI_ZHUANG_START:
                if (heart.stop == EMPTY) {
                    CustomToast.toast(false,CustomToast.TIP_NORMAL,"请先按下急停按钮");
                    return;
                }
                break;
            default:
                if (heart.stop == STOP) {
                    CustomToast.toast(false,CustomToast.TIP_NORMAL,"请先松开急停按钮");
                    return;
                }
                break;
        }
        AppHolder.getInstance().getMqtt().publishDoorFun(robotSn,operationEntity.getOperationType());
    }
}
