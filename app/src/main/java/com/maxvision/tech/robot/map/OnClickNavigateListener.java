package com.maxvision.tech.robot.map;


/**
 * name: wy
 * date: 2021/2/22
 * desc:
 */
public interface OnClickNavigateListener {

    //单机
    void onClickNavigate(PointEntity pointEntity);

    //长按
    void onLongNavigate(PointEntity pointEntity);

    void onTouchXy(double x,double y);
}
