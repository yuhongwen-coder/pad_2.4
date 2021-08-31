package com.maxvision.tech.robot.config;

import java.util.HashMap;
import java.util.Map;

/**
 * name: qs
 * date: 2020/8/26
 * desc: 功能配置
 */
public class Function {

    //视频通话的功能配置 0:false 1:true
    public static final int FUNCTION_DEFAULT = 1000;    //开启机器人音频 开启平板视频 1,1
    public static final int FUNCTION_XY = 1001;         //开启机器人音频 关闭平板视频1,0
    public static final int FUNCTION_ZLDH = 1002;       //关闭机器人音频 开启平板视频0,1
    public static final int FUNCTION_XY_ZLDH = 1003;    //关闭机器人音频 关闭平板视频0,0

    private static int[][] fun3 = {
            {FUNCTION_DEFAULT, 1,1},
            {FUNCTION_XY, 1,0},
            {FUNCTION_ZLDH, 0,1},
            {FUNCTION_XY_ZLDH, 0,0}
    };

    public static Map<Integer,CallConfig> getFun3(){
        Map<Integer,CallConfig> list = new HashMap<>();
        for (int[] ints : fun3) {
            CallConfig call = new CallConfig(ints[1]==1,ints[2]==1);
            list.put(ints[0],call);
        }
        return list;
    }

}