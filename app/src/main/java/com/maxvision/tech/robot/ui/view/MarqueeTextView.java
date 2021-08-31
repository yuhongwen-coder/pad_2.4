package com.maxvision.tech.robot.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import androidx.annotation.Nullable;

/**
 * name: wy
 * date: 2021/4/12
 * desc:
 */
public class MarqueeTextView extends androidx.appcompat.widget.AppCompatTextView {
    public MarqueeTextView(Context context) {
        super(context);
    }

    public MarqueeTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MarqueeTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean isFocused(){
        return true;
    }


}