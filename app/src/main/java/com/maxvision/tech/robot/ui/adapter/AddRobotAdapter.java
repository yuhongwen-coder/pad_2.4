package com.maxvision.tech.robot.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.maxvision.tech.mqtt.entity.Heart;
import com.maxvision.tech.robot.R;
import com.maxvision.tech.robot.entity.SearchRobotEntity;
import com.maxvision.tech.robot.utils.RobotUtils;

import java.util.List;

/**
 * name: wy
 * date: 2021/4/6
 * desc:
 */
public class AddRobotAdapter extends RecyclerView.Adapter<AddRobotAdapter.MyViewHolder> {

    private final LayoutInflater mInflater;
    private final List<Heart> heartList;

    public AddRobotAdapter(Context context, List<Heart> list){
        mInflater = LayoutInflater.from(context);
        this.heartList = list;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_add_robot,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Heart heart = heartList.get(position);
        holder.iv_robot_type.setImageResource(RobotUtils.getRobotType(heart.type));
        holder.tv_name.setText(heart.name);
        holder.tv_sn.setText(heart.sn);
        holder.rb_select.setChecked(heart.isSel);
        holder.itemView.setOnClickListener(v -> onClickSel(heart,holder.rb_select));
        holder.rb_select.setOnClickListener(v -> onClickSel(heart,holder.rb_select));
    }

    private void onClickSel(Heart heart,RadioButton radioButton){
        heart.isSel = !heart.isSel;
        radioButton.setChecked(heart.isSel);
    }

    @Override
    public int getItemCount() {
        return heartList.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder{

        private final ImageView iv_robot_type;
        private final RadioButton rb_select;
        private final TextView tv_sn;
        private final TextView tv_name;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            iv_robot_type = itemView.findViewById(R.id.iv_robot_type);
            rb_select = itemView.findViewById(R.id.rb_select);
            tv_sn = itemView.findViewById(R.id.tv_sn);
            tv_name = itemView.findViewById(R.id.tv_name);
        }
    }
}