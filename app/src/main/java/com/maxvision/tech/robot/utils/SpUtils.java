package com.maxvision.tech.robot.utils;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Base64;
import com.maxvision.tech.robot.AppHolder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * name: zjj
 * date: 2019/04/02
 * time: 上午 11:10
 * desc: sharedPreferences工具类
 */
public class SpUtils {

    private static SharedPreferences sharedPreferences;

    private static SharedPreferences.Editor editor;

    private SpUtils() {

    }

    private static SharedPreferences getInstance() {
        if (sharedPreferences == null) {
            synchronized (SharedPreferences.class) {
                if (sharedPreferences == null) {
                    sharedPreferences = PreferenceManager.getDefaultSharedPreferences(AppHolder.getInstance());
                }
            }
        }
        return sharedPreferences;
    }

    private static SharedPreferences.Editor getEditor() {
        if (editor == null) {
            editor = getInstance().edit();
        }
        return editor;
    }

    /**
     * 获取string
     */
    public static String getString(String strKey, String defValue) {
        return getInstance().getString(strKey, defValue);
    }

    public static String getString(String strKey) {
        return getString(strKey, "");
    }

    /**
     * 存放string
     */
    public static void putString(String strKey, String strData) {
        getEditor().putString(strKey, strData).apply();
    }

    /**
     * 获取Boolean
     */
    public static Boolean getBoolean(String strKey) {
        return getBoolean(strKey, false);
    }

    public static Boolean getBoolean(String strKey, boolean defValue) {
        return getInstance().getBoolean(strKey, defValue);
    }

    public static void putBoolean(String strKey, Boolean strData) {
        getEditor().putBoolean(strKey, strData).apply();
    }

    public static int getInt(String strKey) {
        return getInt(strKey, -1);
    }

    public static int getInt(String strKey, int strDefault) {
        return getInstance().getInt(strKey, strDefault);
    }

    public static void putInt(String strKey, int strData) {
        getEditor().putInt(strKey, strData).apply();
    }

    public static long getLong(String strKey) {
        return getLong(strKey, -1);
    }

    public static long getLong(String strKey, long strDefault) {
        return getInstance().getLong(strKey, strDefault);
    }

    public static void putLong(String strKey, long strData) {
        getEditor().putLong(strKey, strData).apply();
    }

    public static float getFloat(String strKey) {
        return getFloat(strKey, (float) 1.0);
    }

    public static float getFloat(String strKey, float strDefault) {
        return getInstance().getFloat(strKey, strDefault);
    }

    public static void putFloat(String strKey, float strData) {
        getEditor().putFloat(strKey, strData).apply();
    }

    /**
     * 保存对象
     *
     * @param key     键
     * @param obj     要保存的对象（Serializable的子类）
     * @param <T>     泛型定义
     */
    public static <T extends Serializable> void putObject(String key, T obj) {
        try {
            put(key, obj);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取对象
     *
     * @param key     键
     * @param <T>     指定泛型
     * @return 泛型对象
     */
    public static <T extends Serializable> T getObject(String key) {
        try {
            return (T) get(key);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 存储List集合
     *
     * @param key     存储的键
     * @param list    存储的集合
     */
    public static void putList(String key, List<? extends Serializable> list) {
        try {
            put(key, list);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取List集合
     *
     * @param key     键
     * @param <E>     指定泛型
     * @return List集合
     */
    public static <E extends Serializable> List<E> getList(String key) {
        try {
            return (List<E>) get(key);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 存储Map集合
     *
     * @param key     键
     * @param map     存储的集合
     * @param <K>     指定Map的键
     * @param <V>     指定Map的值
     */
    public static <K extends Serializable, V extends Serializable> void putMap(
            String key, Map<K, V> map) {
        try {
            put( key, map);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static <K extends Serializable, V extends Serializable> Map<K, V> getMap(
                                                                                    String key) {
        try {
            return (Map<K, V>) get(key);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 存储对象
     */
    private static void put(String key, Object obj)
            throws IOException {
        if (obj == null) {//判断对象是否为空
            return;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = null;
        oos = new ObjectOutputStream(baos);
        oos.writeObject(obj);
        // 将对象放到OutputStream中
        // 将对象转换成byte数组，并将其进行base64编码
        String objectStr = new String(Base64.encode(baos.toByteArray(), Base64.DEFAULT));
        baos.close();
        oos.close();

        putString(key, objectStr);
    }

    /**
     * 获取对象
     */
    private static Object get(String key)
            throws IOException, ClassNotFoundException {
        String wordBase64 = getString(key, "");
        // 将base64格式字符串还原成byte数组
        if (TextUtils.isEmpty(wordBase64)) { //不可少，否则在下面会报java.io.StreamCorruptedException
            return null;
        }
        byte[] objBytes = Base64.decode(wordBase64.getBytes(), Base64.DEFAULT);
        ByteArrayInputStream bais = new ByteArrayInputStream(objBytes);
        ObjectInputStream ois = new ObjectInputStream(bais);
        // 将byte数组转换成product对象
        Object obj = ois.readObject();
        bais.close();
        ois.close();
        return obj;
    }


    /**
     * 清除SP存储的数据
     *
     * @param key     键字段
     */
    public static void cleanSPData(String key) {
        getEditor().remove(key).apply();
    }

    /**
     * 清除所有数据
     */
    public static void clear() {
        getEditor().clear().apply();
    }

}
