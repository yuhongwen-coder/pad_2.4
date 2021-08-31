package com.maxvision.tech.robot.map;

/**
 * name: wy
 * date: 2021/2/20
 * desc:
 */
public class PointEntity {

    public double x;
    public double y;
    public double angle;
    /**
     * 类型
     * 1：导航点
     * 2：当前位置
     * 3：地图标点
     */
    public int type;
    public String name;

    public PointEntity() {
    }
    public PointEntity(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public PointEntity(double x, double y,double angle, String name) {
        this.x = x;
        this.y = y;
        this.angle = angle;
        this.type = 1;
        this.name = name;
    }


    @Override
    public String toString() {
        return "PointEntity{" +
                "x=" + x +
                ", y=" + y +
                ", name='" + name + '\'' +
                '}';
    }
}