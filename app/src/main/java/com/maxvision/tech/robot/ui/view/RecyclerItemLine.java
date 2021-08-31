package com.maxvision.tech.robot.ui.view;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by yuhongwen
 * on 2021/4/9
 * 给 RecylerView 条目画一个距离
 */
public class RecyclerItemLine extends RecyclerView.ItemDecoration{
    private final String type;
    private final int size;
    private int space;
    private int topBottomSize = -1;


    public RecyclerItemLine(int space,String type,int size) {
        this.space = space;
        this.type = type;
        this.size = size;
    }

    public void setTopBottomOffset(int size) {
        this.topBottomSize = size;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        if (!TextUtils.equals(type,"robot_setting")) {
            outRect.set(space,0,0,space);
        } else {
            int count = parent.getChildAdapterPosition(view) +1;
            if (count % 2 == 0) {
                // 偶数
                outRect.top = 0;
                if (count == size) {
                    // 最后一个偶数
                    outRect.bottom = 0;
                } else {
                    if (topBottomSize == -1) {
                        outRect.bottom = space;
                    } else {
                        outRect.bottom = topBottomSize;
                    }
                }
                outRect.left = space;
                outRect.right = 0;
            } else if (count %2 == 1) {
                // 基数
                outRect.top = 0;
                if (count == size-1) {
                    // 最后一个 基数
                    outRect.bottom = 0;
                } else {
                    if (topBottomSize == -1) {
                        outRect.bottom = space;
                    } else {
                        outRect.bottom = topBottomSize;
                    }
                }
                outRect.left = 0;
                outRect.right = 0;
            }
        }
    }
}
