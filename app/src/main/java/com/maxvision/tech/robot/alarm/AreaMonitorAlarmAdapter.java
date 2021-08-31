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
import com.maxvision.tech.robot.db.AreaMontiorAlarmDb;
import com.maxvision.tech.robot.ui.dialog.ImageDialogFragment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by yuhongwen
 * on 2021/4/9
 */
public class AreaMonitorAlarmAdapter extends PagedListAdapter<AreaMontiorAlarmDb,AreaMonitorAlarmAdapter.AreaMonitorHodler > {
    private Context mContext;

    public AreaMonitorAlarmAdapter(Context context){
        super(DIFF_CALLBACK);
        this.mContext = context;
    }

    public static final DiffUtil.ItemCallback<AreaMontiorAlarmDb> DIFF_CALLBACK = new DiffUtil.ItemCallback<AreaMontiorAlarmDb>() {
        @Override
        public boolean areItemsTheSame(@NonNull AreaMontiorAlarmDb oldItem, @NonNull AreaMontiorAlarmDb newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @SuppressLint("DiffUtilEquals")
        @Override
        public boolean areContentsTheSame(@NonNull AreaMontiorAlarmDb oldItem, @NonNull AreaMontiorAlarmDb newItem) {
            return oldItem.equals(newItem);
        }
    };

    @NonNull
    @Override
    public AreaMonitorHodler onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_alarm_monitor,parent,false);
        return new AreaMonitorHodler(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AreaMonitorHodler holder, int position) {
        AreaMontiorAlarmDb model = getItem(position);
        if (model == null)return;
        Long time = model.getTime();
        String alarmImagePath = model.getAlarmImagePath();
        holder.monitorAlarmTime.setText(formatTime(time,"yyyy-MM-dd HH:mm:ss SSS"));
        ImageEngineUtils.createImageEngine().loadImage(mContext,alarmImagePath,holder.monitorCapturePic);
        holder.monitorCapturePic.setOnClickListener(v -> {
            if(mContext instanceof AppCompatActivity){
                AppCompatActivity activity = (AppCompatActivity) mContext;
                ImageDialogFragment.getInstance(model.getAlarmImagePath()).show(activity.getSupportFragmentManager(),ImageDialogFragment.class.getSimpleName());
            }
        });
    }

    public class AreaMonitorHodler extends RecyclerView.ViewHolder {
        ImageView monitorCapturePic;
        TextView monitorAlarmTime;
        public AreaMonitorHodler(@NonNull View itemView) {
            super(itemView);
            monitorCapturePic = itemView.findViewById(R.id.monitor_capture_pic);
            monitorAlarmTime = itemView.findViewById(R.id.monitor_alarm_time);
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
