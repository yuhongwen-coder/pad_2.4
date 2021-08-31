package com.maxvision.tech.robot.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import androidx.annotation.Nullable;
import com.maxvision.tech.robot.R;


/**
 * name: wy
 * date: 2020/9/1
 * desc:
 */
public class SpreadView extends LinearLayout {

    private final Context mContext;
    private WaveFrameLayout fl_wave_view;
    private OnVisibilityListener onVisibilityListener;

    public SpreadView(Context context) {
        this(context,null);
    }
    public SpreadView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }
    public SpreadView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        initView();
    }

    private void initView() {
        View view = View.inflate(mContext, R.layout.layout_spread_view,this);
        fl_wave_view = view.findViewById(R.id.fl_wave_view);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Log.e("wangyin_aadd","按下");
                startAnimator();
                if(onVisibilityListener != null){
                    onVisibilityListener.onStartRecording();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                onVisibility(event.getX() < 0 || event.getY() < 0);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                stopAnimator();
                onVisibility(false);
                if(onVisibilityListener != null){
                    if(event.getX() < 0 || event.getY() < 0){
                        Log.e("wangyin_aadd","取消录音");
                        onVisibilityListener.onCancelRecording();
                    }else{
                        Log.e("wangyin_aadd","录音结束");
                        onVisibilityListener.onEndRecording();
                    }
                }

                break;
        }
        return true;
    }


    /**
     * 开启正在识别动画
     */
    public void startAnimator(){
        post(() -> fl_wave_view.startAnimator());
    }

    /**
     * 停止识别动画
     */
    public void stopAnimator(){
        post(() -> fl_wave_view.stopAnimator());
    }

    private void onVisibility(boolean isShow){
        if(onVisibilityListener == null) return;
        onVisibilityListener.onVisibility(isShow);
    }

    public void setOnVisibilityListener(OnVisibilityListener onVisibilityListener) {
        this.onVisibilityListener = onVisibilityListener;
    }

    public interface OnVisibilityListener{
        void onVisibility(boolean isShow);
        //取消录音
        void onCancelRecording();
        //结束录音
        void onEndRecording();
        //开始录音
        void onStartRecording();
    }

}