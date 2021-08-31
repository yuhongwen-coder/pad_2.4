package com.maxvision.tech.robot.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.maxvision.tech.robot.R;
import com.maxvision.tech.robot.entity.FunctionEntity;
import java.util.List;

/**
 * name: wy
 * date: 2021/4/9
 * desc:
 */
public class FunctionAdapter extends RecyclerView.Adapter<FunctionAdapter.MyViewHolder>{

    private final LayoutInflater mInflater;
    private final List<FunctionEntity> list;
    private final boolean isAlarm;
    private OnClickItemListener onClickItemListener;

    public FunctionAdapter(Context context,List<FunctionEntity> list,boolean isAlarm){
        mInflater = LayoutInflater.from(context);
        this.list = list;
        this.isAlarm = isAlarm;
    }

    @NonNull
    @Override
    public FunctionAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_function,parent,false);
        return new FunctionAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FunctionAdapter.MyViewHolder holder, int position) {
        FunctionEntity entity = list.get(position);
        holder.iv_function_bg.setImageResource(entity.imageId);
        holder.tv_function_name.setText(entity.name);
        // 点击后，直接退回到主页，Fragment不用更新
        if (entity.id ==1 && isAlarm) {
            holder.tv_function_tip.setVisibility(View.VISIBLE);
        } else {
            holder.tv_function_tip.setVisibility(View.GONE);
        }
        holder.itemView.setOnClickListener(v -> {
            if(onClickItemListener != null){
                onClickItemListener.onClickItem(entity);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder{
        private final ImageView iv_function_bg;
        private final TextView tv_function_name;
        private final ImageView tv_function_tip;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            iv_function_bg = itemView.findViewById(R.id.iv_function_bg);
            tv_function_name = itemView.findViewById(R.id.tv_function_name);
            tv_function_tip = itemView.findViewById(R.id.tv_function_tip);
        }
    }

    public void setOnClickItemListener(OnClickItemListener onClickItemListener) {
        this.onClickItemListener = onClickItemListener;
    }

    public interface OnClickItemListener{
        void onClickItem(FunctionEntity entity);
    }

}