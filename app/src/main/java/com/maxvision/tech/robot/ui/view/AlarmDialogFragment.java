package com.maxvision.tech.robot.ui.view;

import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.maxvision.tech.robot.AppHolder;
import com.maxvision.tech.robot.R;
import com.maxvision.tech.robot.utils.CustomToast;

/**
 * Created by yuhongwen
 * on 2021/4/22
 */
public class AlarmDialogFragment extends DialogFragment implements View.OnTouchListener, View.OnClickListener {

    public AlarmDialogFragment() {
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomToast.toast(false,CustomToast.TIP_SUCCESS,"View 可以响应窗口事件");
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.alarm_float_view, null);
    }

    @Override
    public void onStart() {
        super.onStart();
        Window window = getDialog().getWindow();
        WindowManager.LayoutParams windowParams = window.getAttributes();
        int w = getScreenWidthPix(AppHolder.getInstance())/3;
        int h = ViewGroup.LayoutParams.WRAP_CONTENT;
        windowParams.width = w;
        windowParams.height = h;
        windowParams.gravity = Gravity.TOP;
        setCancelable(true);
        window.setAttributes(windowParams);
    }

    private static int getScreenWidthPix(Context context) {
        if (context == null) {
            return 0;
        }
        return context.getResources().getDisplayMetrics().widthPixels;
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        CustomToast.toast(false,CustomToast.TIP_SUCCESS,"dialog onTouch 可以响应窗口事件");
        return false;
    }

    @Override
    public void onClick(View v) {
        CustomToast.toast(false,CustomToast.TIP_SUCCESS,"dialog onClick 可以响应窗口事件");
    }
}
