package com.maxvision.tech.robot.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cl.voice.Voice;
import com.cl.voice.call.DictationCallback;
import com.maxvision.tech.mqtt.entity.CallDealEntity;
import com.maxvision.tech.mqtt.entity.DialogueEntity;
import com.maxvision.tech.robot.AppHolder;
import com.maxvision.tech.robot.R;
import com.maxvision.tech.robot.ui.adapter.AnswerAdapter;
import com.maxvision.tech.robot.ui.adapter.RobotControlFunAdapter;
import com.maxvision.tech.robot.ui.dialog.WaitingDialogFragment;
import com.maxvision.tech.robot.ui.fragment.ZldhVideoFragment;
import com.maxvision.tech.robot.ui.view.MyRockerView;
import com.maxvision.tech.robot.ui.view.RecyclerItemLine;
import com.maxvision.tech.robot.ui.view.RobotControlRecyclerView;
import com.maxvision.tech.robot.ui.view.SpreadView;
import com.maxvision.tech.robot.utils.DensityUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * name: wy
 * date: 2021/4/30
 * desc: 智能对话
 */
public class DialogueActivity extends AppCompatActivity implements SpreadView.OnVisibilityListener, View.OnClickListener,
        AnswerAdapter.OnClickItemListener, RobotControlFunAdapter.FunButtonListener {

    private View view_cancel;
    private SpreadView spreadView;
    private RelativeLayout rl_text;
    private EditText edt_text;
    private FrameLayout fl_video;
    private TextView tv_question, tv_answer;
    private ImageView iv_jt;
    private ConstraintLayout rl_kz;
    private LinearLayout ll_wd;

    //保证链接心跳计时器
    private Disposable disposable;

    //机器人SN
    private String sn;
    //消息ID
    private String msgId;

    //消息内容
    private String messageContent;
    private ZldhVideoFragment zldhVideoFragment;

    //是否列表是否展开
    private boolean isUnfold = false;
    private String roomID;
    private Switch sw_aduio;

    //模式：true：音频模式  false：文本模式
    private boolean isMode = true;
    private RobotControlRecyclerView controlFunRl;

    public static void startActivity(Context context, String sn) {
        Intent intent = new Intent(context, DialogueActivity.class);
        intent.putExtra("sn", sn);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialogue);
        EventBus.getDefault().register(this);
        fl_video = findViewById(R.id.fl_video);
        view_cancel = findViewById(R.id.view_cancel);
        spreadView = findViewById(R.id.spreadView);
        tv_question = findViewById(R.id.tv_question);
        tv_answer = findViewById(R.id.tv_answer);
        rl_kz = findViewById(R.id.rl_kz);
        sw_aduio = findViewById(R.id.sw_aduio);
        ll_wd = findViewById(R.id.ll_wd);
        RecyclerView recyclerView = findViewById(R.id.recyclerview);
        controlFunRl = findViewById(R.id.robot_control_rl);
        configControlFun();

        spreadView.setOnVisibilityListener(this);

        roomID = sn + System.currentTimeMillis();
        sn = getIntent().getStringExtra("sn");
        if (TextUtils.isEmpty(sn)) {
            finish();
            return;
        }

        rl_text = findViewById(R.id.rl_text);
        edt_text = findViewById(R.id.edt_text);
        findViewById(R.id.alarm_switch_back).setOnClickListener(this);
        findViewById(R.id.ll_unfold).setOnClickListener(this);
        AppHolder.getInstance().getMqtt().setDialogue(sn, DialogueEntity.sendInit(1));

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        AnswerAdapter adapter = new AnswerAdapter(this);
        adapter.setOnClickItemListener(this);
        recyclerView.setAdapter(adapter);

        iv_jt = findViewById(R.id.iv_jt);
        MyRockerView rockerXYView = findViewById(R.id.rockerXY_View);
        rockerXYView.setSn(sn);

        sw_aduio.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isMode = isChecked;
            updateType();
            removeFragment();
            //弹框提示
            WaitingDialogFragment.getInstance(isChecked ? getString(R.string.tip_switch) : getString(R.string.tip_switch2))
                    .setListener(() -> {
                        //切换视频模式
                        showRemoteVideoFragment();
                    })
                    .show(getSupportFragmentManager());
        });
        //更新模式
        updateType();
        //显示视频
        showRemoteVideoFragment();
        createThread();
    }

    private void configControlFun() {
        List<String> funList = Arrays.asList(getResources().getStringArray(R.array.control_fun_list));
        controlFunRl.setLayoutManager(new GridLayoutManager(this, 2));
        RecyclerItemLine itemLine = new RecyclerItemLine(DensityUtil.dip2px(this, 30), "robot_setting", funList.size());
        itemLine.setTopBottomOffset(DensityUtil.dip2px(this, 0));
        controlFunRl.addItemDecoration(itemLine);
        RobotControlFunAdapter funAdapter = new RobotControlFunAdapter(R.layout.robot_control_fun_item, funList);
        controlFunRl.setAdapter(funAdapter);
        funAdapter.setListener(this);
    }

    @Override
    public void onVisibility(boolean isShow) {
        view_cancel.setVisibility(isShow ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onCancelRecording() {
        //取消录音
        Log.i("wangyin_Voice", "取消录音 : ");
        spreadView.stopAnimator();
        Voice.getInstance().destroy();
        clearVoiceText();
    }

    @Override
    public void onEndRecording() {
        //结束录音
        Log.i("wangyin_Voice", "结束录音 : ");
        tv_answer.setText(getString(R.string.text_answer, messageContent));
        sendMessage();
        spreadView.stopAnimator();
        Voice.getInstance().destroy();
        clearVoiceText();
    }

    @Override
    public void onStartRecording() {
        spreadView.startAnimator();
        edt_text.setText("");
        //开始录音
        Voice.getInstance().xfRealTimeDictation(new DictationCallback() {
            @Override
            public void onNext(String result) {
                Log.i("wangyin_Voice", "结果：" + result);
                messageContent = result;
                edt_text.setText(result);
            }

            @Override
            public void onError(Throwable e) {
                Log.i("wangyin_Voice", "出错了" + e.toString());
                messageContent = "";
                clearVoiceText();
            }

            @Override
            public void onComplete() {
                Log.i("wangyin_Voice", "开始了");
                rl_text.setVisibility(View.VISIBLE);
                messageContent = "";
            }

            @Override
            public void onEnd() {
                Log.i("wangyin_Voice", "结束了");
            }
        });

    }

    private void clearVoiceText() {
        rl_text.setVisibility(View.GONE);
        edt_text.setText("");
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.alarm_switch_back) {
            finish();
        } else if (id == R.id.ll_unfold) {
            isUnfold = !isUnfold;
            iv_jt.setRotation(isUnfold ? 0 : 180);
            rl_kz.setVisibility(isUnfold ? View.VISIBLE : View.GONE);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(DialogueEntity event) {
        if (TextUtils.equals(msgId, event.msgId)) {
            tv_question.setText(getString(R.string.text_question, event.content));
            return;
        }
        msgId = event.msgId;
        tv_question.setText(getString(R.string.text_question, event.content));
        tv_answer.setText("");
    }

    //结束录音，消息发送
    private void sendMessage() {
        //entity = DialogueEntity.sendMessage(t, msgId, messageContent);
        DialogueEntity entity = DialogueEntity.sendMessage(isMode ? 2 : 1, "-1", messageContent);
        AppHolder.getInstance().getMqtt().setDialogue(sn, entity);
    }

    private void updateType() {
        if (isMode) {
            AppHolder.getInstance().getMqtt().setDialogue(sn, DialogueEntity.sendInit(2));
            ll_wd.setVisibility(View.GONE);
            sw_aduio.setText(R.string.text_mode);
        } else {
            AppHolder.getInstance().getMqtt().setDialogue(sn, DialogueEntity.sendInit(1));
            ll_wd.setVisibility(View.VISIBLE);
            sw_aduio.setText(R.string.text_mode2);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        AppHolder.getInstance().getMqtt().setDialogue(sn, DialogueEntity.sendInit(0));
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
    }


    //显示视频Fragment
    private void showRemoteVideoFragment() {
        //赋值给Mainactivity
        zldhVideoFragment = new ZldhVideoFragment();
        Bundle bundle = new Bundle();
        bundle.putString("sn", sn);
        bundle.putString("roomid", roomID);
        bundle.putBoolean("isMode", isMode);
        zldhVideoFragment.setArguments(bundle);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(R.id.fl_video, zldhVideoFragment).commitAllowingStateLoss();
    }

    //删视频Fragment
    public void removeFragment() {
        if (zldhVideoFragment == null) return;
        if (!zldhVideoFragment.isAdded()) return;
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.remove(zldhVideoFragment).commitAllowingStateLoss();
    }

    //收到终端已处理了来电
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(CallDealEntity bean) {
        //平板挂断
        if (TextUtils.equals(bean.padSn, "-1")&&bean.deal ==0) {
            finish();
        }
    }

    @Override
    public void onClickItem(String content) {
        messageContent = content;
        tv_answer.setText(getString(R.string.text_answer, content));
        sendMessage();
    }

    @Override
    public void funBtnClick(String fun) {
        if (TextUtils.equals(fun, getResources().getString(R.string.tz))) {
            sendHandData((100 + 45) << 8 | 04);
        } else if (TextUtils.equals(fun, getResources().getString(R.string.ty))) {
            sendHandData((100 + 45) << 16 | 05);
        } else if (TextUtils.equals(fun, getResources().getString(R.string.fx))) {
            sendHandData(0x00);
        } else if (TextUtils.equals(fun, getResources().getString(R.string.dt))) {
            sendHeadData(0x01);
        } else if (TextUtils.equals(fun, getResources().getString(R.string.yt))) {
            sendHeadData(0x02);
        } else if (TextUtils.equals(fun, getResources().getString(R.string.hy))) {
            sendHeadData(0x00);
        } else if (TextUtils.equals(fun, getResources().getString(R.string.tt))) {
            sendHeadData((100 + 5) << 8 | 03);
        }
    }

    private void sendHeadData(int data) {
        AppHolder.getInstance().getMqtt().getControl(sn, 1, data);

    }

    private void sendHandData(int data) {
        AppHolder.getInstance().getMqtt().getControl(sn, 2, data);
    }


    private void createThread() {
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
        disposable = Observable.interval(0, 3, TimeUnit.SECONDS)
                .observeOn(Schedulers.io())
                .subscribeOn(Schedulers.io()).subscribe(aLong -> AppHolder.getInstance().getMqtt().setDialogue(sn, DialogueEntity.sendInit(100)));
    }

}