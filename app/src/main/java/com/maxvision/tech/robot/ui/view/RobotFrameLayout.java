package com.maxvision.tech.robot.ui.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import com.maxvision.tech.mqtt.RobotState;
import com.maxvision.tech.mqtt.entity.Heart;
import com.maxvision.tech.mqtt.entity.LocationEntity;
import com.maxvision.tech.robot.R;
import com.maxvision.tech.robot.entity.cons.SpConstants;
import com.maxvision.tech.robot.ui.dialog.AddRobotDialogFragment;
import com.maxvision.tech.robot.utils.SpUtils;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * name: wy
 * date: 2021/4/6
 * desc:
 */
public class RobotFrameLayout extends FrameLayout{

    private final Context mContext;
    //机器人包裹空间
    private final LinearLayout linearLayout;
    //机器人呼叫最外层View
    private final RobotDispatcheLinearLayout ll_call;

    private MyConstraintLayout.OnClickRobotListener onClickRobotListener;

    private final Map<String,MyConstraintLayout> layoutMap = new LinkedHashMap<>();

    public RobotFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        View view = View.inflate(context, R.layout.item_robot,null);
        linearLayout = view.findViewById(R.id.ll_robot);
        ll_call = view.findViewById(R.id.ll_call);

        view.findViewById(R.id.ll_add_robot).setOnClickListener(v -> {
            AddRobotDialogFragment dialogFragment = AddRobotDialogFragment.getInstance();
            AppCompatActivity activity = (AppCompatActivity) getContext();
            dialogFragment.show(activity.getSupportFragmentManager());
        });

        addView(view);
    }

    public void setCallVisibility(){
        boolean is = SpUtils.getBoolean(SpConstants.SP_CALL_SWITCH);
        ll_call.setVisibility(is ? VISIBLE : GONE);
        ll_call.setCallVisibility(is);
    }

    public void setOnClickRobotListener(MyConstraintLayout.OnClickRobotListener onClickRobotListener) {
        this.onClickRobotListener = onClickRobotListener;
    }

    public synchronized void clearRobot(){
        if(layoutMap == null || layoutMap.size() == 0)return;
        for (Map.Entry<String, MyConstraintLayout> map : layoutMap.entrySet()) {
            linearLayout.removeView(map.getValue());
        }
        layoutMap.clear();
    }

    public synchronized void addRobot(Heart heart){
        linearLayout.post(() -> {
            MyConstraintLayout l = layoutMap.get(heart.sn);
            if(l != null) {
                l.setData(heart);
                return;
            }
            MyConstraintLayout layout = new MyConstraintLayout(mContext);
            layout.setOnClickRobotListener(onClickRobotListener);
            layout.setData(heart);
            linearLayout.addView(layout);
            layoutMap.put(heart.sn,layout);
        });
    }

    public void setStateContent(Heart heart){
        linearLayout.post(() -> {
            MyConstraintLayout l = layoutMap.get(heart.sn);
            if(l == null) return;
            String state = RobotState.state(heart);
            //状态不同，更新
            if(!TextUtils.equals(l.getContent(), state)){
                l.setContent(state);
            }
            //机器人名称不同，更新
            if(!TextUtils.equals(l.getName(), heart.name)){
                l.setName(heart.name);
            }
        });
    }

    //机器人呼叫，实时计算当前机器人距离
    public void despatchCall(LocationEntity navigation){
        ll_call.despatchCall(navigation);
    }

    public void setLineState(Heart heart){
        linearLayout.post(() -> {
            MyConstraintLayout l = layoutMap.get(heart.sn);
            if(l == null) return;
            if(!heart.isLine) return;
            l.setLineState(heart);
        });
    }

    public void clearBackground(String sn){
        if(layoutMap == null || layoutMap.size() == 0)return;
        for (Map.Entry<String, MyConstraintLayout> map : layoutMap.entrySet()) {
            map.getValue().setSelectItem(TextUtils.equals(map.getKey(), sn));
        }
    }

    /**
     * 开始进行跑马灯
     */
    public void setMarquee(String sn){
        MyConstraintLayout l = layoutMap.get(sn);
        if(l == null) return;
        l.setMarquee();
    }
}