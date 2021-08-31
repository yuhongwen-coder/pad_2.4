package com.maxvision.tech.mqtt;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

/**
 * name: wy
 * date: 2021/3/30
 * desc:
 * qos：服务器质量
 *      0：“至多一次”，消息发布完全依赖底层 TCP/IP 网络。会发生消息丢失或重复。这一级别可用于如下情况，环境传感器数据，丢失一次读记录无所谓，因为不久后还会有第二次发送
 *      1：“至少一次”，确保消息到达，但消息重复可能会发生。这一级别可用于如下情况，你需要获得每一条消息，并且消息重复发送对你的使用场景无影响
 *      2：“只有一次”，确保消息到达一次。这一级别可用于如下情况，在计费系统中，消息重复或丢失会导致不正确的结果
 */
public class MQTTManager {

    //生成临时序列号
    public static String PAD_SN;

    //地区
    public static String AREA;

    private static final String TAG = "MQTTManager";
    //private String SERVER_HOST = "tcp://192.168.111.10:1883";
    private String SERVER_HOST = "tcp://open.maxvision.com.cn:1883";
    private static MQTTManager mqttManager = null;
    private MqttAsyncClient client;

    private MessageHandlerCallBack callBack;

    private final Handler handler = new Handler(Looper.myLooper());

    private MQTTManager() {
        handler.postDelayed(myRunnable,5000);
    }

    public void setPadSn(String sn) {
        MQTTManager.PAD_SN = sn;
    }

    public void setArea(String area) {
        MQTTManager.AREA = area;
    }

    /**
     * 获取一个MQTTManager单例
     *
     * @return 返回一个MQTTManager的实例对象
     */
    public static MQTTManager getInstance() {
        if (mqttManager == null) {
            mqttManager = new MQTTManager();
            synchronized (MQTTManager.class) {
                return mqttManager;
            }
        }
        return mqttManager;
    }

    public void setIP(String ip){
        this.SERVER_HOST = ip;
    }

    /**
     * 连接服务器
     */
    public void connect() {
        Log.d(TAG, "开始连接MQtt");
        try {
            // host为主机名，clientid即连接MQTT的客户端ID，一般以唯一标识符表示，MemoryPersistence设置clientid的保存形式，默认为以内存保存
            client = new MqttAsyncClient(SERVER_HOST, MQTTManager.PAD_SN, new MemoryPersistence());

            // MQTT的连接设置
            MqttConnectOptions options = new MqttConnectOptions();
            // 设置是否清空session,这里如果设置为false表示服务器会保留客户端的连接记录，这里设置为true表示每次连接到服务器都以新的身份连接
            options.setCleanSession(true);
            // 设置连接的用户名
            options.setUserName("admin");
            // 设置连接的密码
            options.setPassword("123456".toCharArray());
            // 设置超时时间 单位为秒
            options.setConnectionTimeout(30);
            // 设置会话心跳时间 单位为秒 服务器会每隔1.5*20秒的时间向客户端发送个消息判断客户端是否在线，但这个方法并没有重连的机制
            options.setKeepAliveInterval(30);
            // 设置回调
            //MqttTopic topic = client.getTopic(TOPIC);
            //setWill方法，如果项目中需要知道客户端是否掉线可以调用该方法。设置最终端口的通知消息
            //options.setWill(topic, "close".getBytes(), 2, true);
            /*SSLSocketFactory sslSocketFactory = null;
              try {
                  sslSocketFactory = sslContextFromStream(mContext.getAssets().open("server.pem")).getSocketFactory();
              } catch (Exception e) {
                  e.printStackTrace();
              }
            options.setSocketFactory(sslSocketFactory);*/
            client.setCallback(new PushCallback());
            client.connect(options);
            Log.d(TAG, "ClientId=" + client.getClientId());
        } catch (MqttException e) {
            e.printStackTrace();
            Log.e(TAG, "connect: " + e);
        }
    }

    /**
     * 订阅消息
     */
    public void subscribeMsg(String topic) {
        if (client != null) {
            try {
                if(!client.isConnected()) return;
                client.subscribe(topic, 1);
                Log.d(TAG, "开始订阅topic=" + topic);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 发布消息
     *
     * @param topic      发布消息主题
     * @param msg        消息体
     */
    public void publish(String topic, String msg) {
        try {
            if (client != null) {
                if(!client.isConnected()) return;
                MqttMessage message = new MqttMessage();
                message.setQos(1);
                message.setRetained(false);
                message.setPayload(msg.getBytes());
                client.publish(topic, message);
                Log.d(TAG, "topic=" + topic + "--msg=" + msg);
            }
        } catch (MqttException e) {
            connect();
        }
    }

    /**
     * 发布和订阅消息的回调
     */
    public class PushCallback implements MqttCallbackExtended {

        public void connectionLost(Throwable cause) {
            Log.e(TAG, "connectionLost: 断开连接,重新连接  " + cause);
            try {
                client.close();
                connect();
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }

        /**
         * 发布消息的回调
         */
        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {
            //publish后会执行到这里
            Log.d(TAG, "发布消息 已送达" + token.isComplete());
        }

        /**
         * 接收消息的回调方法
         */
        @Override
        public void messageArrived(final String topicName, final MqttMessage message) throws Exception {
            //subscribe后得到的消息会执行到这里面
            Log.d(TAG, "接收消息==" + new String(message.getPayload()));
            if (callBack != null) {
                callBack.messageSuccess(topicName, new String(message.getPayload()));
            }
        }

        @Override
        public void connectComplete(boolean reconnect, String serverURI) {
            Log.e(TAG, "MQTT链接服务器成功 :"+reconnect+"     "+serverURI );
            if (callBack != null) {
                callBack.connectSuccess();
            }
        }
    }

    /**
     * 设置接收消息的回调方法
     */
    public void setMessageHandlerCallBack(MessageHandlerCallBack callBack) {
        this.callBack = callBack;
    }

    public MessageHandlerCallBack getMessageHandlerCallBack() {
        if (callBack != null) {
            return callBack;
        }
        return null;
    }

    /**
     * 断开链接
     */
    public void disconnect() {
        if (client != null && client.isConnected()) {
            try {
                client.disconnect();
                mqttManager = null;
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 释放资源
     */
    public void release() {
        if (mqttManager != null) {
            mqttManager = null;
        }
    }

    /**
     * 判断服务是否连接
     */
    public boolean isConnected() {
        if (client != null) {
            return client.isConnected();
        }
        return false;
    }

    public SSLContext sslContextFromStream(InputStream inputStream) throws Exception {
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        Certificate certificate = certificateFactory.generateCertificate(inputStream);
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(null, null);
        keyStore.setCertificateEntry("ca", certificate);
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(keyStore);
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustManagerFactory.getTrustManagers(), null);
        return sslContext;
    }
    private final Runnable myRunnable = new Runnable() {
        @Override
        public void run() {
            handler.postDelayed(myRunnable,5 * 1000);
            if(client != null && client.isConnected()) return;
            connect();
        }
    };
}