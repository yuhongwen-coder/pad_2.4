package com.maxvision.tech.robot.ui.adapter;

import android.view.View;

import androidx.appcompat.widget.AppCompatButton;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.maxvision.tech.robot.R;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Created by yuhongwen
 * on 2021/5/8
 */
public class RobotControlFunAdapter extends BaseQuickAdapter<String, BaseViewHolder> {


    private FunButtonListener listener;

    public RobotControlFunAdapter(int layoutResId, @Nullable List<String> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder baseViewHolder, String s) {
        AppCompatButton funBtn = baseViewHolder.itemView.findViewById(R.id.robot_control_fun);
        funBtn.setText(s);
        funBtn.setOnClickListener(v -> {
            if (listener == null) return;
            listener.funBtnClick(s);
        });
    }

    public interface FunButtonListener {
        void funBtnClick(String fun);
    }

    public void setListener(FunButtonListener listener) {
        this.listener = listener;
    }
}
