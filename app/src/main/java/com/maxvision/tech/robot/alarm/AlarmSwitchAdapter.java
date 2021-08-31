package com.maxvision.tech.robot.alarm;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.maxvision.tech.mqtt.RobotState;
import com.maxvision.tech.mqtt.entity.AlarmSwitchEntity;
import com.maxvision.tech.mqtt.entity.BaseAlarmEntity;
import com.maxvision.tech.mqtt.entity.state.RobotNavState;
import com.maxvision.tech.robot.R;
import com.maxvision.tech.robot.manager.RobotDataManager;
import com.maxvision.tech.robot.utils.CustomToast;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.maxvision.tech.mqtt.entity.BaseAlarmEntity.ALARM_AREA;
import static com.maxvision.tech.mqtt.entity.BaseAlarmEntity.ALARM_CLOSE;
import static com.maxvision.tech.mqtt.entity.BaseAlarmEntity.ALARM_FACE;
import static com.maxvision.tech.mqtt.entity.BaseAlarmEntity.ALARM_MASK;
import static com.maxvision.tech.mqtt.entity.BaseAlarmEntity.ALARM_OPEN;
import static com.maxvision.tech.mqtt.entity.BaseAlarmEntity.ALARM_TEMP;

/**
 * Created by yuhongwen
 * on 2021/4/14
 */
public class AlarmSwitchAdapter extends BaseQuickAdapter<AlarmSwitchEntity,BaseViewHolder> {

    private final Context mContext;
    private FunctionClickListener onFunctionClickListener;
    private long lastClickTime = 0;
    private String mSn = "";

    public AlarmSwitchAdapter(Context context, int layoutResId, @Nullable List data, String sn) {
        super(layoutResId, data);
        mContext = context;
        mSn = sn;
    }

    @Override
    protected void convert(@NotNull BaseViewHolder baseViewHolder, AlarmSwitchEntity entity) {
        Switch switchCompat = baseViewHolder.itemView.findViewById(R.id.item_alarm_switch_state_switch);
        TextView switchCompatState = baseViewHolder.itemView.findViewById(R.id.item_alarm_switch_state);
        TextView alarmName = baseViewHolder.itemView.findViewById(R.id.item_alarm_switch_text);
        ImageView alarmType = baseViewHolder.itemView.findViewById(R.id.item_alarm_switch_image);
        alarmName.setText(entity.alarmMessage);
        setOnClickListener(baseViewHolder,entity);
        refreshStatus(entity,switchCompat,switchCompatState,alarmName,alarmType);
    }

    private void refreshStatus(AlarmSwitchEntity entity,Switch switchCompat,TextView switchCompatState,TextView alarmName,ImageView alarmType) {
        alarmType.setImageResource(entity.getIsOpenSwitch() == 1 ? entity.getTypeImage():getAlarmImage(entity.getAlarmType()));
        boolean isChecked;
        String switchState;
        int closeOpenColor;
        Drawable thumb;
        Drawable track;
        switch (entity.getIsOpenSwitch()) {
            case BaseAlarmEntity.ALARM_CLOSE:
                switchState = mContext.getString(R.string.function_state_off);
                isChecked = false;
                closeOpenColor = mContext.getColor(R.color.fun_state_close);
                thumb = ContextCompat.getDrawable(mContext,R.drawable.item_alarm_switch_thumb_off);
                track = ContextCompat.getDrawable(mContext,R.drawable.item_alarm_switch_track_off);
                break;
            case BaseAlarmEntity.ALARM_EXCEPTION:
                switchState = mContext.getString(R.string.function_state_warn);
                isChecked = false;
                closeOpenColor = mContext.getColor(R.color.fun_state_exception);
                thumb = ContextCompat.getDrawable(mContext,R.drawable.item_alarm_switch_exception);
                track = ContextCompat.getDrawable(mContext,R.drawable.item_alarm_switch_track_off);

                break;

            default:
                switchState = mContext.getString(R.string.function_state_normal);
                isChecked = true;
                closeOpenColor = mContext.getColor(R.color.fun_state_open);
                thumb = ContextCompat.getDrawable(mContext,R.drawable.item_alarm_switch_thumb_on);
                track = ContextCompat.getDrawable(mContext,R.drawable.item_alarm_switch_track_on);
                break;
        }
        alarmName.setTextColor(closeOpenColor);
        switchCompatState.setText(switchState);
        switchCompatState.setTextColor(closeOpenColor);
        switchCompat.setThumbDrawable(thumb);
        switchCompat.setTrackDrawable(track);
        switchCompat.setChecked(isChecked);
    }

    private void setOnClickListener(BaseViewHolder holder,AlarmSwitchEntity entity) {
        //开关控制
        LinearLayout imageViewParent = holder.itemView.findViewById(R.id.item_alarm_switch_state_switch_parent);
        imageViewParent.setOnClickListener(v -> {
            long now = System.currentTimeMillis();
            if (now - lastClickTime < 800) {
                lastClickTime = now;
                Log.e("yhw_alarmSwitch", "user click too quick");
                return;
            }
            Log.e("yhw_alarmSwitch", "user click ");
            lastClickTime = now;
            Switch imageView = holder.itemView.findViewById(R.id.item_alarm_switch_state_switch);
            //区域布控开关 与跟随模式互斥
            if(RobotState.isFollow(RobotDataManager.getInstance().get(mSn))&&entity.getAlarmType()== ALARM_AREA){
                CustomToast.toast(false,CustomToast.TIP_NORMAL,mContext.getString(R.string.alarm_area_tip2));
                return;
            }

            entity.setIsOpenSwitch(imageView.isChecked() ? ALARM_CLOSE : ALARM_OPEN);
            TextView switchCompatState = holder.itemView.findViewById(R.id.item_alarm_switch_state);
            TextView alarmName = holder.itemView.findViewById(R.id.item_alarm_switch_text);
            ImageView alarmType = holder.itemView.findViewById(R.id.item_alarm_switch_image);
            AlarmSwitchAdapter.this.refreshStatus(entity, imageView, switchCompatState, alarmName, alarmType);
            if (onFunctionClickListener != null) {
                onFunctionClickListener.onSwitchClick(entity);
            }
        });
    }

    public interface FunctionClickListener {
        void onSwitchClick(AlarmSwitchEntity entity);
    }

    public void setOnFunctionClickListener(FunctionClickListener listener) {
        this.onFunctionClickListener = listener;
    }

    private int getAlarmImage(int alarmType) {
        int resId = 0;
        switch (alarmType) {
            case ALARM_AREA:
                resId = R.mipmap.area_switch_close;
                break;
            case ALARM_FACE:
                resId = R.mipmap.face_switch_close;
                break;
            case ALARM_MASK:
                resId = R.mipmap.mask_switch_close;
                break;
            case ALARM_TEMP:
                resId = R.mipmap.temp_switch_close;
                break;
            default:
                break;
        }
        return resId;
    }
}
