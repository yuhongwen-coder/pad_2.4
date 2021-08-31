package com.maxvision.tech.robot.ui.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.Nullable;
import com.maxvision.tech.robot.R;
import com.maxvision.tech.robot.utils.CustomToast;

/**
 * name: zjj
 * date: 2021/05/08
 * time: 13:48
 * desc: IP&Port布局
 */
public class IpLayout extends LinearLayout {

    // 标题
    TextView tvName;
    // ip输入
    EditText etIp;
    // port输入
    EditText etPort;
    // 确定按钮
    Button btnSure;
    // 标题文字,ip,port
    private String name,ip,port;
    // rootView id
    private int viewId;

    public IpLayout(Context context) {
        this(context,null);
    }

    public IpLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public IpLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        getAttr(context,attrs,defStyleAttr);
        initView();
    }

    /**
     * 获取自定义的属性值
     */
    private void getAttr(Context context, AttributeSet attrs, int defStyle) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.IpLayout, defStyle, 0);
        name = a.getString(R.styleable.IpLayout_name);
        ip = a.getString(R.styleable.IpLayout_ip);
        port = a.getString(R.styleable.IpLayout_port);
        a.recycle();
    }

    private void initView() {
        View rootView = View.inflate(getContext(), R.layout.layout_ip,null);
        tvName = rootView.findViewById(R.id.tv_name);
        etIp = rootView.findViewById(R.id.edt_ip);
        etPort = rootView.findViewById(R.id.edt_port);
        btnSure = rootView.findViewById(R.id.btn_sure);
        initData();
        btnSure.setOnClickListener(v -> {
            if (checkIpAndPort()) {
                return;
            }
            if (null != sureListener) {
                sureListener.onSure(viewId,etIp.getText().toString().replace(" ",""),etPort.getText().toString().replace(" ",""));
            }
        });
        addView(rootView);
    }

    // 初始化数据
    private void initData() {
        if (!TextUtils.isEmpty(name)) {
            tvName.setText(name);
        }
        if (!TextUtils.isEmpty(ip)) {
            etIp.setText(ip);
        }
        if (!TextUtils.isEmpty(port)) {
            etPort.setText(port);
        }
    }

    // 校验
    private boolean checkIpAndPort() {
        if (TextUtils.isEmpty(etIp.getText().toString().trim())) {
            CustomToast.toastLong(CustomToast.TIP_ERROR,"Ip不能为空");
            return true;
        }
        if (TextUtils.isEmpty(etPort.getText().toString().trim())) {
            CustomToast.toastLong(CustomToast.TIP_ERROR,"端口Port不能为空");
            return true;
        }
        return false;
    }

    public interface SureListener {
        void onSure(int viewId,String ip,String port);
    }

    private SureListener sureListener;

    public void setSureListener(int viewId,SureListener sureListener) {
        this.viewId = viewId;
        this.sureListener = sureListener;
    }

    // 设置ip
    public void setIp(String ip) {
        etIp.setText(ip);
    }

    // 设置port
    public void setPort(String port) {
        etPort.setText(port);
    }

}
