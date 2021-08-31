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
import com.maxvision.tech.robot.db.FaceListAlarmDb;
import com.maxvision.tech.robot.ui.dialog.ImageDialogFragment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by yuhongwen
 * on 2021/4/9
 */
public class FaceAlarmAdapter extends PagedListAdapter<FaceListAlarmDb, FaceAlarmAdapter.FaceAlarmViewHolder> {



    private Context mContext;

    public FaceAlarmAdapter(Context context){
        super(diff);
        this.mContext = context;
    }

    private static final DiffUtil.ItemCallback<FaceListAlarmDb> diff = new DiffUtil.ItemCallback<FaceListAlarmDb>() {
        @Override
        public boolean areItemsTheSame(@NonNull FaceListAlarmDb oldItem, @NonNull FaceListAlarmDb newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @SuppressLint("DiffUtilEquals")
        @Override
        public boolean areContentsTheSame(@NonNull FaceListAlarmDb oldItem, @NonNull FaceListAlarmDb newItem) {
            return oldItem.equals(newItem);
        }
    };

    @NonNull
    @Override
    public FaceAlarmViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_face_alarm,parent,false);
        return new FaceAlarmViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FaceAlarmViewHolder holder, int position) {
        FaceListAlarmDb model = getItem(position);
        if (model == null)return;
        holder.alarmTime.setText(formatTime(model.getTime() == null ? System.currentTimeMillis() : model.getTime(),"yyyy-MM-dd HH:mm:ss"));
        holder.alarmName.setText(model.getName());
        holder.alarmSex.setText(model.getSex());
        ImageEngineUtils.createImageEngine().loadImage(mContext,model.getImagePath(),holder.alarmImage);
        ImageEngineUtils.createImageEngine().loadImage(mContext,model.getCapturePath(),holder.alarmCaptureImage);
        holder.alarmCaptureImage.setOnClickListener(v -> {
            if(mContext instanceof AppCompatActivity){
                AppCompatActivity activity = (AppCompatActivity) mContext;
                ImageDialogFragment.getInstance(model.getCapturePath()).show(activity.getSupportFragmentManager(),ImageDialogFragment.class.getSimpleName());
            }
        });
        holder.alarmImage.setOnClickListener(v -> {
            if(mContext instanceof AppCompatActivity){
                AppCompatActivity activity = (AppCompatActivity) mContext;
                ImageDialogFragment.getInstance(model.getImagePath()).show(activity.getSupportFragmentManager(),ImageDialogFragment.class.getSimpleName());
            }
        });
    }

    public class FaceAlarmViewHolder extends RecyclerView.ViewHolder {

        ImageView alarmImage;   //本地图库
        ImageView alarmCaptureImage;  //实时抓拍图片
        TextView alarmTime;
        TextView alarmName;
        TextView alarmSex;

        public FaceAlarmViewHolder(@NonNull View itemView) {
            super(itemView);
            alarmImage = itemView.findViewById(R.id.black_alarm_iamge);
            alarmCaptureImage = itemView.findViewById(R.id.black_alarm_iamge1);
            alarmTime = itemView.findViewById(R.id.black_alarm_time);
            alarmName = itemView.findViewById(R.id.black_alarm_name);
            alarmSex = itemView.findViewById(R.id.black_alarm_sex);
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
