package com.maxvision.tech.mqtt.entity;

import java.util.List;

/**
 * name: wy
 * date: 2021/4/1
 * desc:
 */
public class NavigationEntity {

    public String sn;
    public List<ListData> listData;

    public static class ListData{
        public String nName;
        public double nX;
        public double nY;
        public double angle;
        public int nType;
    }

}