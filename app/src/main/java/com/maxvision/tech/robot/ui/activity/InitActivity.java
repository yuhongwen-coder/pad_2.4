package com.maxvision.tech.robot.ui.activity;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.maxvision.tech.mqtt.MQTTManager;
import com.maxvision.tech.robot.AppHolder;
import com.maxvision.tech.robot.MainActivity;
import com.maxvision.tech.robot.R;
import com.maxvision.tech.robot.server.MqttService;
import com.maxvision.tech.robot.entity.cons.SpConstants;
import com.maxvision.tech.robot.ui.view.CircleIndicatorView;
import com.maxvision.tech.robot.utils.SpUtils;
import com.tbruyelle.rxpermissions2.RxPermissions;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * name: wy
 * date: 2021/4/8
 * desc:
 */
public class InitActivity extends AppCompatActivity {

    private CircleIndicatorView indicatorView;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        indicatorView = findViewById(R.id.view_circle_indicator);

        RxPermissions rxPermissions = new RxPermissions(this);
        rxPermissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.RECORD_AUDIO)
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onSubscribe(Disposable d) { }
                    @Override
                    public void onNext(Boolean aBoolean) {
                        if (aBoolean) {
                            indicatorView.start();
                            indicatorView.postDelayed(runnable,2000);
                        }
                    }
                    @Override
                    public void onError(Throwable e) {
                    }
                    @Override
                    public void onComplete() {
                    }
                });
        findViewById(R.id.iv_logo).setOnClickListener(new View.OnClickListener() {
            final static int COUNTS = 5;//点击次数
            final static long DURATION = 1000;//规定有效时间
            long[] mHits = new long[COUNTS];
            @Override
            public void onClick(View v) {
                System.arraycopy(mHits, 1, mHits, 0, mHits.length - 1);
                //实现左移，然后最后一个位置更新距离开机的时间，如果最后一个时间和最开始时间小于DURATION，即连续5次点击
                mHits[mHits.length - 1] = SystemClock.uptimeMillis();
                if (mHits[0] >= (SystemClock.uptimeMillis() - DURATION)) {
                    //开发者模式
                    long t = System.currentTimeMillis();
                    long time = SpUtils.getLong(SpConstants.SP_DEBUG_TIME,0);
                    if(t - time > 5 * 1000){
                        SpUtils.putLong(SpConstants.SP_DEBUG_TIME,System.currentTimeMillis());
                        startActivity(new Intent(InitActivity.this, DebugActivity.class));
                    }
                }
            }
        });
    }

    private final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            MqttService service = AppHolder.getInstance().getMqtt();
            if(service == null){
                indicatorView.postDelayed(runnable,1000);
            }else{
                //判断MQTT是否已连接
                if(!MQTTManager.getInstance().isConnected()){
                    indicatorView.postDelayed(runnable,2000);
                    MQTTManager.getInstance().connect();
                    return;
                }
                startActivity(new Intent(InitActivity.this, MainActivity.class));
                finish();
            }
            indicatorView.postDelayed(runnable,1000);
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (indicatorView != null) {
            indicatorView.stop();
        }
    }
}