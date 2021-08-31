package com.maxvision.tech.robot.ui.view;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.maxvision.tech.mqtt.RobotState;
import com.maxvision.tech.mqtt.entity.Heart;
import com.maxvision.tech.robot.R;
import com.maxvision.tech.robot.ui.dialog.TaskListDialogFragment;
import com.maxvision.tech.robot.utils.RobotUtils;

/**
 * name: wy
 * date: 2021/4/6
 * desc:
 */
public class MyConstraintLayout extends LinearLayout {

    //机器人类型
    private ImageView iv_type;
    //机器人名称
    private TextView tv_name;
    //机器人状态
    private TextView tv_content;
    //机器人是否在线
    private View view_line;
    //选择
    private LinearLayout ll_select;
    private LinearLayout cl_content;

    TaskListDialogFragment fragment;
    private OnClickRobotListener onClickRobotListener;
    private Heart heart;

    public MyConstraintLayout(Context context) {
        super(context);
        View view = View.inflate(context, R.layout.item_robot_data,null);
        tv_name = view.findViewById(R.id.tv_name);
        iv_type = view.findViewById(R.id.iv_type);
        tv_content = view.findViewById(R.id.tv_content);
        view_line = view.findViewById(R.id.view_line);
        ll_select = view.findViewById(R.id.ll_select);
        cl_content = view.findViewById(R.id.cl_content);
        cl_content.setOnClickListener(v -> {
            if(onClickRobotListener != null){
                onClickRobotListener.onClickRobotItem(heart);
            }
        });
        ll_select.setOnClickListener(v -> {
            if(RobotUtils.isLine(heart.isLine)) return;
            if (null == fragment){
                fragment = TaskListDialogFragment.getInstance(heart.sn);
            }
            AppCompatActivity activity = (AppCompatActivity) getContext();
            fragment.setOnDismissListener(() -> openlSelect(false));
            fragment.show(activity.getSupportFragmentManager());
            openlSelect(true);
        });
        addView(view);
    }

    public void setData(Heart heart){
        if(heart==null) return;
        this.heart = heart;
        setName(heart.name);
        tv_content.setText(RobotState.state(heart));
        iv_type.setImageResource(RobotUtils.getRobotType(heart.type));
        setLineState(heart);
    }

    public void setLineState(Heart heart){
        view_line.setBackgroundResource(RobotUtils.getLine(heart));
    }

    public void setOnClickRobotListener(OnClickRobotListener onClickRobotListener) {
        this.onClickRobotListener = onClickRobotListener;
    }

    public interface OnClickRobotListener{
        void onClickRobotItem(Heart heart);
    }

    /**
     * 功能展开
     * @param is
     */
    public void openlSelect(boolean is){
        if(is){
            ll_select.setBackgroundResource(R.drawable.shape_backgroung_t_b);
        }else{
            ll_select.setBackground(null);
        }
    }

    /**
     * 选中
     * @param is
     */
    public void setSelectItem(boolean is){
        if(is){
            cl_content.setBackgroundResource(R.drawable.shape_backgroung_radius10_4);
        }else{
            cl_content.setBackgroundResource(R.drawable.shape_backgroung_radius10_2);
        }
    }

    public void setMarquee(){
        tv_name.setFocusable(true);
        tv_name.setFocusableInTouchMode(true);
        tv_name.requestFocus();
    }

    public String getContent() {
        return tv_content.getText().toString();
    }
    public String getName() {
        return tv_name.getText().toString();
    }
    public void setContent(String content) {
        tv_content.setText(content);
    }
    public void setName(String name) {
        tv_name.setText(name);
    }
}