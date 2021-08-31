package com.maxvision.tech.robot.ui.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.maxvision.tech.robot.R;

/**
 * name: wy
 * date: 2020/9/1
 * desc: 语音播报动画View
 */
public class WaveFrameLayout extends FrameLayout {

    private Context mContext;

    private ImageView ivWave;
    private ImageView ivWave1;
    private ImageView ivWave2;

    private ValueAnimator valueAnimator;
    private ValueAnimator valueAnimator2;
    private ValueAnimator valueAnimator3;

    public WaveFrameLayout(@NonNull Context context) {
        this(context,null);
    }
    public WaveFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }
    public WaveFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        initView();
    }

    private void initView() {
        View view = View.inflate(mContext, R.layout.layout_wave_view,this);
        ivWave = view.findViewById(R.id.iv_wave);
        ivWave1 = view.findViewById(R.id.iv_wave_1);
        ivWave2 = view.findViewById(R.id.iv_wave_2);

        valueAnimator3 = ValueAnimator.ofFloat(1, 1.25f, 1);
        valueAnimator3.addUpdateListener(animation -> {
            Float value = (Float) animation.getAnimatedValue();
            ivWave.setScaleX(value);
            ivWave.setScaleY(value);
            ivWave.setAlpha((1.3f-value)*5);

        });
        valueAnimator3.setDuration(800);
        valueAnimator3.setRepeatCount(Animation.INFINITE);
        valueAnimator3.setInterpolator(new DecelerateInterpolator());

        valueAnimator2 = ValueAnimator.ofFloat(1, 1.25f, 1);
        valueAnimator2.addUpdateListener(animation -> {
            Float value = (Float) animation.getAnimatedValue();
            ivWave1.setScaleX(value);
            ivWave1.setScaleY(value);
            ivWave1.setAlpha((1.4f-value)*5);
            if (value > 1.08f) {
                if (!valueAnimator3.isRunning()) {
                    valueAnimator3.start();

                }
            }
        });
        valueAnimator2.setInterpolator(new DecelerateInterpolator());
        valueAnimator2.setRepeatCount(Animation.INFINITE);
        valueAnimator2.setDuration(800);

        valueAnimator = ValueAnimator.ofFloat(1, 1.2f, 1);
        valueAnimator.addUpdateListener(animation -> {
            Float value = (Float) animation.getAnimatedValue();
            ivWave2.setScaleX(value);
            ivWave2.setScaleY(value);
            ivWave2.setAlpha((1.5f-value)*5);

            if (value > 1.06f) {
                if (!valueAnimator2.isRunning()) {
                    valueAnimator2.start();
                }
            }
        });
        valueAnimator.setRepeatCount(Animation.INFINITE);
        valueAnimator.setInterpolator(new DecelerateInterpolator());
        valueAnimator.setDuration(800);

    }


    public void startAnimator() {
        post(() -> {
            if(valueAnimator.isStarted()) return;
            valueAnimator.start();
        });
    }

    public void stopAnimator(){
        post(() -> {
            valueAnimator.cancel();
            valueAnimator2.cancel();
            valueAnimator3.cancel();
            ivWave.setScaleX(1);
            ivWave.setScaleY(1);
            ivWave.setAlpha(1f);

            ivWave1.setScaleX(1);
            ivWave1.setScaleY(1);
            ivWave1.setAlpha(1f);

            ivWave2.setScaleX(1);
            ivWave2.setScaleY(1);
            ivWave2.setAlpha(1f);
        });
    }



}