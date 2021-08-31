package com.maxvision.tech.webrtc.manager;

import android.app.Activity;
import android.text.TextUtils;
import android.util.Log;

import com.maxvision.tech.webrtc.bean.MediaType;
import com.maxvision.tech.webrtc.bean.MyIceServer;
import com.maxvision.tech.webrtc.ui.ChatRoomActivity;
import com.maxvision.tech.webrtc.ui.ChatSingleActivity;
import com.maxvision.tech.webrtc.ws.IConnectEvent;


/**
 * Created by dds on 2019/1/7.
 * android_shuai@163.com
 */
public class WebrtcUtil {
    private static final String TAG = "qs_WebrtcUtil";

    // ip
    public static String HOST = "47.111.68.11";
    // port
    public static String PORT = "3000";

    // 设置ip和port
    public static void setIpAndPort(String ip,String port){
        HOST = ip;
        PORT = port;
        WSS = "ws://".concat(HOST).concat(":").concat(PORT);
    }

    // turn and stun
    public static MyIceServer[] iceServers = {
            new MyIceServer("stun:stun.l.google.com:19302"),

            // 测试地址1
            new MyIceServer("stun:" + HOST + ":3478?transport=udp"),
            new MyIceServer("turn:" + HOST + ":3478?transport=udp",
                    "wangyin",
                    "123456"),
            new MyIceServer("turn:" + HOST + ":3478?transport=tcp",
                    "wangyin",
                    "123456"),
    };

    // signalling
    //private static String WSS = "wss://" + HOST + "/wss";
    //本地测试信令地址
    public static String WSS = "ws://".concat(HOST).concat(":").concat(PORT);

    // one to one
    public static void callSingle(Activity activity, String wss, String roomId, boolean videoEnable) {
        if (TextUtils.isEmpty(wss)) {
            wss = WSS;
        }
        WebRTCManager.getInstance().init(wss, iceServers, new IConnectEvent() {
            @Override
            public void onSuccess() {
                Log.d(TAG, " onSuccess:");
                ChatSingleActivity.openActivity(activity, videoEnable);
                activity.finish();
            }

            @Override
            public void onFailed(String msg) {
                Log.d(TAG, " onFailed:" + msg);
                activity.finish();
            }
        });
        //WebRTCManager.getInstance().connect(videoEnable ? MediaType.TYPE_VIDEO : MediaType.TYPE_AUDIO, roomId);
    }

    // Videoconferencing
    public static void call(Activity activity, String wss, String roomId) {
        if (TextUtils.isEmpty(wss)) {
            wss = WSS;
        }
        WebRTCManager.getInstance().init(wss, iceServers, new IConnectEvent() {
            @Override
            public void onSuccess() {
                ChatRoomActivity.openActivity(activity);
            }

            @Override
            public void onFailed(String msg) {

            }
        });
        //WebRTCManager.getInstance().connect(MediaType.TYPE_MEETING, roomId);
    }

}
