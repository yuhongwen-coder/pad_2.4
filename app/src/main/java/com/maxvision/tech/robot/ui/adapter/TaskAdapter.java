package com.maxvision.tech.robot.ui.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.maxvision.tech.mqtt.entity.BaseTaskEntity;
import com.maxvision.tech.mqtt.entity.XieyunState;
import com.maxvision.tech.robot.R;


import java.util.List;

import static com.maxvision.tech.mqtt.entity.state.RobotZnxyState.TASK_EXECUTING;
import static com.maxvision.tech.mqtt.entity.state.RobotZnxyState.TASK_PAUSE;

/**
 * name: zjj
 * date: 2021/04/15
 * time: 9:10
 * desc:
 */
public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.MyViewHolder> {

    private final LayoutInflater mInflater;
    private final List<BaseTaskEntity> list;
    private TaskAdapter.OnClickItemListener onClickItemListener;
    private final Context mContext;
    private final int xyMode;

    public TaskAdapter(Context context, List<BaseTaskEntity> list,int xyMode) {
        mInflater = LayoutInflater.from(context);
        mContext = context;
        this.list = list;
        this.xyMode = xyMode;
    }

    @NonNull
    @Override
    public TaskAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_task, parent, false);
        return new TaskAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskAdapter.MyViewHolder holder, int position) {
        BaseTaskEntity entity = list.get(position);
        if (TextUtils.equals(entity.robotType,"2") && XieyunState.MODE_ZNXY == xyMode) {
            // 智能协运
            holder.ivType.setImageDrawable(ContextCompat.getDrawable(mContext,R.mipmap.ic_task));
        } else {
            // 消毒和任务调度
            if (entity.isTimeTask) {
                holder.ivType.setImageDrawable(ContextCompat.getDrawable(mContext,R.mipmap.ic_time_task));
            } else {
                holder.ivType.setImageDrawable(ContextCompat.getDrawable(mContext,R.mipmap.ic_task));
            }
        }
        holder.tvName.setText(entity.taskName);
        setState(holder.tvState,holder.itemView,entity);
        holder.tvState.setOnClickListener(v -> {
            if (onClickItemListener != null) {
                onClickItemListener.onClickItem(entity);
            }
        });
    }

    private void setState(TextView tvState,View view ,BaseTaskEntity entity) {
        if (TextUtils.equals("2",entity.robotType) && XieyunState.MODE_ZNXY == xyMode) {
            switch (entity.taskStatus) {
                case TASK_EXECUTING:
                    tvState.setText(mContext.getString(R.string.text_task_ing));
                    view.setBackground(ContextCompat.getDrawable(mContext,R.drawable.shape_background_green_radius10));
                    break;
                case TASK_PAUSE: // 暂停中
                    tvState.setText(mContext.getString(R.string.text_task_pause));
                    view.setBackground(ContextCompat.getDrawable(mContext,R.drawable.shape_background_green_radius10));
                    break;
                default: // 空闲 --> 执行
                    tvState.setText(mContext.getString(R.string.text_task_empty));
                    view.setBackground(ContextCompat.getDrawable(mContext,R.drawable.shape_background_blue_radius10));
                    break;
            }
        } else {
            switch (entity.taskStatus) {
                case 1: // 执行中
                    tvState.setText(mContext.getString(R.string.text_task_ing));
                    view.setBackground(ContextCompat.getDrawable(mContext,R.drawable.shape_background_green_radius10));
                    break;
                case 2: // 恢复/结束
                    tvState.setText(mContext.getString(R.string.text_task_break));
                    view.setBackground(ContextCompat.getDrawable(mContext,R.drawable.shape_background_green_radius10));
                    break;
                default: // 空闲 --> 执行
                    tvState.setText(mContext.getString(R.string.text_task_empty));
                    view.setBackground(ContextCompat.getDrawable(mContext,R.drawable.shape_background_blue_radius10));
                    break;
            }
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvName;
        private final Button tvState;
        private final ImageView ivType;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_name);
            tvState = itemView.findViewById(R.id.tv_state);
            ivType = itemView.findViewById(R.id.iv_type);
        }
    }

    public void setAllData(List<? extends BaseTaskEntity> lists) {
        list.clear();
        list.addAll(lists);
        notifyDataSetChanged();
    }

    public void setOnClickItemListener(TaskAdapter.OnClickItemListener onClickItemListener) {
        this.onClickItemListener = onClickItemListener;
    }

    public List<BaseTaskEntity> getTaskList() {
        return list;
    }

    public interface OnClickItemListener {
        void onClickItem(BaseTaskEntity entity);
    }
}
