package com.maxvision.tech.robot.ui.adapter;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.maxvision.tech.robot.R;
import com.maxvision.tech.robot.entity.RobotOperationEntity;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Created by yuhongwen
 * on 2021/4/16
 */
public class RobotOperationAdapter extends BaseQuickAdapter<RobotOperationEntity, BaseViewHolder> {
    private OperationListener listener;

    public RobotOperationAdapter(int layoutResId, @Nullable List<RobotOperationEntity> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder baseViewHolder, RobotOperationEntity robotOperationEntity) {
        ImageView operationView = baseViewHolder.itemView.findViewById(R.id.robot_setting_fun_image);
        TextView operationText = baseViewHolder.itemView.findViewById(R.id.robot_setting_fun_text);
        baseViewHolder.itemView.findViewById(R.id.robot_setting_fun_text_title).setVisibility(View.GONE);
        ImageView operationBg = baseViewHolder.itemView.findViewById(R.id.robot_setting_fun_bg);
        operationView.setImageResource(robotOperationEntity.getOperationImageRs());
        operationText.setText(robotOperationEntity.getOperationString());
        initListener(operationBg,robotOperationEntity);
    }

    private void initListener(ImageView imageView,RobotOperationEntity entity) {
        imageView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onOperation(entity);
            }
        });
    }

    public interface OperationListener{
        void onOperation(RobotOperationEntity operationEntity);
    }

    public void setListener(OperationListener listener){
        this.listener = listener;
    }

}
