package com.maxvision.tech.robot.alarm;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.maxvision.tech.robot.utils.ImageEngineUtils;
import com.maxvision.tech.robot.R;
import com.maxvision.tech.robot.db.TempAlarmDb;
import com.maxvision.tech.robot.ui.dialog.ImageDialogFragment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by yuhongwen
 * on 2021/4/9
 */
public class TempAlarmAdapter extends PagedListAdapter<TempAlarmDb, TempAlarmAdapter.TempAlarmHodler> {


    private final Context mContext;

    public TempAlarmAdapter(Context context) {
        super(diff);
        mContext = context;
    }

    public static final DiffUtil.ItemCallback<TempAlarmDb> diff = new DiffUtil.ItemCallback<TempAlarmDb>() {
        @Override
        public boolean areItemsTheSame(@NonNull TempAlarmDb oldItem, @NonNull TempAlarmDb newItem) {
            return oldItem.getId().equals(newItem.getId());
        }

        @SuppressLint("DiffUtilEquals")
        @Override
        public boolean areContentsTheSame(@NonNull TempAlarmDb oldItem, @NonNull TempAlarmDb newItem) {
            return oldItem.equals(newItem);
        }
    };

    @NonNull
    @Override
    public TempAlarmHodler onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_temp_alarm,parent,false);
        return new TempAlarmHodler(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TempAlarmHodler holder, int position) {
        TempAlarmDb item = getItem(position);
        if (item == null)return;
        String imagePath = item.getLiveImgbytes();
        int alarmType = item.getTempAlarmType();
        String hwImagePath = item.getTempImgbytes();
        String alrmTypeStr = alarmType == 0?"低温报警":"高温报警";
        String s =formatTime(item.getTime(), "yyyy-MM-dd HH:mm:ss");
        String ss = alrmTypeStr + "  "+ s;
        holder.lowtempAlarmTime.setText(ss);
        ImageEngineUtils.createImageEngine().loadImage(mContext,imagePath,holder.ivInfraredPic);
        ImageEngineUtils.createImageEngine().loadImage(mContext,hwImagePath,holder.ivCapturePic);
        holder.ivCapturePic.setOnClickListener(v -> {
            if(mContext instanceof AppCompatActivity){
                AppCompatActivity activity = (AppCompatActivity) mContext;
                ImageDialogFragment.getInstance(item.getLiveImgbytes()).show(activity.getSupportFragmentManager(),ImageDialogFragment.class.getSimpleName());
            }
        });
        holder.ivInfraredPic.setOnClickListener(v -> {
            if(mContext instanceof AppCompatActivity){
                AppCompatActivity activity = (AppCompatActivity) mContext;
                ImageDialogFragment.getInstance(item.getTempImgbytes()).show(activity.getSupportFragmentManager(),ImageDialogFragment.class.getSimpleName());
            }
        });
    }

    public static class TempAlarmHodler extends RecyclerView.ViewHolder {

        ImageView ivInfraredPic;
        ImageView ivCapturePic;
        TextView lowtempAlarmTime;
        LinearLayout llImages;
        public TempAlarmHodler(@NonNull View itemView) {
            super(itemView);
            ivInfraredPic = itemView.findViewById(R.id.iv_infrared_pic);
            ivCapturePic = itemView.findViewById(R.id.iv_capture_pic);
            lowtempAlarmTime = itemView.findViewById(R.id.lowtemp_alarm_time);
            llImages = itemView.findViewById(R.id.ll_image);
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


