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
import com.maxvision.tech.robot.utils.RobotUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * name: wy
 * date: 2021/4/6
 * desc:快捷回复
 */
public class AnswerAdapter extends RecyclerView.Adapter<AnswerAdapter.MyViewHolder> {

    private final LayoutInflater mInflater;
    private final List<String> strList = new ArrayList<>();
    private OnClickItemListener onClickItemListener;

    public AnswerAdapter(Context context){
        mInflater = LayoutInflater.from(context);
        strList.add("您好，您可以问我屏幕上的问题");
        strList.add("您好，您说的问题太复杂了");
        strList.add("您好，能换个问题吗");
        strList.add("我能不回答您这个问题吗");
        strList.add("这个问题像我这种高智商的人一定会知道吗");
        strList.add("我觉得这个问题可以让我记下来");
        strList.add("你看天空那是啥，说实话吧，这个问题我回答不了");
        strList.add("等下，我要拿出我的小本本记下你，还有你这个问题。");
        strList.add("一天天的，就知道问这么难的问题，有本事问个简单的问题");
        strList.add("你知不知道你在拿高中题目考一个刚刚读幼儿园的小可爱");
        strList.add("严重怀疑我三万六的智商是伪造的，这个问题竟然没想到答案");
        strList.add("你这个人好坏的讲，拿我不涉及的领域考我");
        strList.add("别欺负我，就知道问我不会的问题");
        strList.add("你这人好坏的讲，拿我不清楚的领域问我");
        strList.add("我上知五千年，下知五千年，可唯独你这个问题我回答不了");
        strList.add("我很好奇你这个帅气的脑袋你怎么会问这么复杂的问题");
        strList.add("我现在也有个问题，您为啥会问这么难搞的问题");
        strList.add("您这个问题，我的算法似乎回答不了了");
    }

    public void setOnClickItemListener(OnClickItemListener onClickItemListener) {
        this.onClickItemListener = onClickItemListener;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_answer,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        String str = strList.get(position);
        holder.tv_wt.setText(str);
        holder.itemView.setOnClickListener(v -> {
            if(onClickItemListener != null){
                onClickItemListener.onClickItem(str);
            }
        });
    }

    @Override
    public int getItemCount() {
        return strList.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder{
        private final TextView tv_wt;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_wt = itemView.findViewById(R.id.tv_wt);
        }
    }

    public interface OnClickItemListener{
        void onClickItem(String content);
    }

}