package com.maxvision.tech.robot.base;

import android.app.Dialog;
import android.os.Bundle;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import io.reactivex.disposables.Disposable;


/**
 * name: wy
 * date: 2020/9/23
 * desc: 比较复杂的弹框，推荐用DialogFragment
 */
public class BaseDialogFragment extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(true);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);
        }
        return dialog;
    }

    public boolean isShowing() {
        Dialog dialog = getDialog();
        return dialog != null && dialog.isShowing();
    }

    public void show(FragmentManager fragmentManager) {
        if (fragmentManager == null || isShowing()) return;
        String tag = this.getClass().getSimpleName();
        if (fragmentManager.findFragmentByTag(tag) != null) return;
        fragmentManager.beginTransaction().add(this, tag).commitAllowingStateLoss();
    }

    protected void dispose(Disposable... disposables) {
        if (disposables == null || disposables.length == 0) return;
        for (Disposable disposable : disposables) {
            if (disposable == null || disposable.isDisposed()) continue;
            disposable.dispose();
        }
    }

    @Override
    public void dismiss() {
        super.dismissAllowingStateLoss();
    }
}