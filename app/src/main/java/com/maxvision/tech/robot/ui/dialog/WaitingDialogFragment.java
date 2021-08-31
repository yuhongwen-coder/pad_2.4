package com.maxvision.tech.robot.ui.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.maxvision.tech.robot.R;
import com.maxvision.tech.robot.base.BaseDialogFragment;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;


/**
 * name: qs
 * date: 2021/5/6
 * desc:切换机器人 挂断视频
 */
public class WaitingDialogFragment extends BaseDialogFragment {
    private static final String TAG = "qs_HangingDialogFragment";
    private Disposable disposable;
    private TextView tv_tip;
    OnDissmissListener listener;
    String tiitle ="";

    public WaitingDialogFragment(String tiitle) {
        this.tiitle = tiitle;
    }

    public WaitingDialogFragment setListener(OnDissmissListener listener) {
        this.listener = listener;
        return this;
    }

    public static WaitingDialogFragment getInstance(String tiitle) {
        return new WaitingDialogFragment(tiitle);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_hanging_view, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tv_tip = view.findViewById(R.id.tv_tip);
        tv_tip.setText(tiitle);
        disposable = Observable.timer(3, TimeUnit.SECONDS).subscribe(l -> dismiss());
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        dispose(disposable);
        if(listener!=null) {
            listener.dismiss();
        }
    }

    public interface OnDissmissListener{
        void dismiss();
    }

}