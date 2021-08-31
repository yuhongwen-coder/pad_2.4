package com.maxvision.tech.robot.alarm;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.maxvision.tech.robot.utils.ImageEngineUtils;
import com.maxvision.tech.robot.R;
import com.maxvision.tech.robot.db.MaskListAlarmDb;
import com.maxvision.tech.robot.ui.dialog.ImageDialogFragment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by yuhongwen
 * on 2021/4/9
 */
public class MaskAlarmAdapter extends PagedListAdapter<MaskListAlarmDb, MaskAlarmAdapter.MaskAlarmViewHolder> {

    private Context mContext;

    public MaskAlarmAdapter(Context context){
        super(diff);
        this.mContext = context;
    }

    private static final DiffUtil.ItemCallback<MaskListAlarmDb> diff = new DiffUtil.ItemCallback<MaskListAlarmDb>() {
        @Override
        public boolean areItemsTheSame(@NonNull MaskListAlarmDb oldItem, @NonNull MaskListAlarmDb newItem) {
            try {
                return oldItem.getId() == newItem.getId();
            } catch (Exception e) {
                return false;
            }
        }

        @SuppressLint("DiffUtilEquals")
        @Override
        public boolean areContentsTheSame(@NonNull MaskListAlarmDb oldItem, @NonNull MaskListAlarmDb newItem) {
            try {
                return oldItem.equals(newItem);
            } catch (Exception e) {
                return false;
            }
        }
    };

    @NonNull
    @Override
    public MaskAlarmViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_mask_alarm,parent,false);
        return new MaskAlarmViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MaskAlarmViewHolder holder, int position) {
        MaskListAlarmDb model = getItem(position);
        if (model == null)return;
        holder.alarmTime.setText(formatTime(model.getTime() == null ? System.currentTimeMillis() : model.getTime(),"yyyy-MM-dd HH:mm:ss"));
        ImageEngineUtils.createImageEngine().loadImage(mContext,model.getImagePath(),holder.alarmImage);
        holder.alarmImage.setOnClickListener(v -> {
            if(mContext instanceof AppCompatActivity){
                AppCompatActivity activity = (AppCompatActivity) mContext;
                ImageDialogFragment.getInstance(model.getImagePath()).show(activity.getSupportFragmentManager(),ImageDialogFragment.class.getSimpleName());
            }
        });
    }

    public class MaskAlarmViewHolder extends RecyclerView.ViewHolder {
        private final ImageView alarmImage;
        private final TextView alarmTime;
        public MaskAlarmViewHolder(@NonNull View itemView) {
            super(itemView);
            alarmImage = itemView.findViewById(R.id.kzjc_alarm_iamge);
            alarmTime = itemView.findViewById(R.id.kzjc_alarm_time);
        }
    }

    private static String formatTime(long time, String format) {
        try {
            String str_time;
            SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
            str_time = (time + "").length() >= 10 ? sdf.format(new Date(time)) : sdf.format(new Date(time * 1000L));
            return str_time;
        }catch (Exception e){
            return "";
        }
    }
}
