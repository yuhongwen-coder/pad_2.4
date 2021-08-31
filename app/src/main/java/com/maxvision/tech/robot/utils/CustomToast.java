package com.maxvision.tech.robot.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.maxvision.tech.robot.AppHolder;
import com.maxvision.tech.robot.R;


/**
 * Author: ml
 * Date: 2019/4/22 15:55
 * Description:
 */
public class CustomToast {
    private static final String TAG = "CustomToast";

    public static final int TIP_NORMAL = 1;
    public static final int TIP_SUCCESS = 2;
    public static final int TIP_ERROR = 3;

    private static Toast toast;

    private static Handler handler = new Handler(Looper.getMainLooper());

    public static void toastLong(int type, String message){
        toast(false,type,message);
    }

    public static void toast(boolean isLong, int type, String message){
        // 判断是否是主线程
        if (handler.getLooper() != Looper.myLooper()) {
            handler.post(() -> {
                myToast(isLong,type,message);
            });
        } else {
            myToast(isLong,type,message);
        }
    }

    /**
     * 自定义toast
     *
     * @param isLong toast时长
     * @param messages toast内容
     */
    private static void myToast(boolean isLong, int type, String messages) {
        if (null == toast) {
            LayoutInflater layoutInflater = (LayoutInflater) AppHolder.getInstance().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            assert layoutInflater != null;
            View view = layoutInflater.inflate(R.layout.layout_custom_toast, null);
            TextView text = view.findViewById(R.id.txt_toast);
            ImageView tip = view.findViewById(R.id.iv_tip);

            switch (type){
                case TIP_NORMAL:
                    tip.setImageResource(R.mipmap.ic_normal_tip);
                    break;
                case TIP_SUCCESS:
                    tip.setImageResource(R.mipmap.ic_success_tip);
                    break;
                case TIP_ERROR:
                    tip.setImageResource(R.mipmap.ic_error_tip);
                    break;
            }
            text.setText(messages); //toast内容
            toast = new Toast(AppHolder.getInstance());
            toast.setGravity(Gravity.CENTER, 0, 0);//setGravity用来设置Toast显示的位置，相当于xml中的android:gravity或android:layout_gravity
            toast.setDuration(isLong ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT);
            toast.setView(view); //添加视图文件
        } else {
            ((TextView) toast.getView().findViewById(R.id.txt_toast)).setText(messages); //toast内容
            switch (type){
                case TIP_NORMAL:
                    ((ImageView) toast.getView().findViewById(R.id.iv_tip)).setImageResource(R.mipmap.ic_normal_tip);
                    break;
                case TIP_SUCCESS:
                    ((ImageView) toast.getView().findViewById(R.id.iv_tip)).setImageResource(R.mipmap.ic_success_tip);
                    break;
                case TIP_ERROR:
                    ((ImageView) toast.getView().findViewById(R.id.iv_tip)).setImageResource(R.mipmap.ic_error_tip);
                    break;
            }
        }
        toast.show();

    }
}
