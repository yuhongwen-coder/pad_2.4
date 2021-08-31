package com.maxvision.tech.robot.utils;

import android.app.Activity;
import android.app.Application;
import android.content.ComponentCallbacks;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.DisplayMetrics;

/**
 * name: zjj
 * date: 2020/08/28
 * time: 下午 15:10
 * desc: 适配工具类
 */
public class FitUtils {

    /**
     * 一. 基本概念
     * 1. dp : 单位
     * 2. px : 单位
     * 3. density : 密度
     * 4. dpi:
     * 5. 分辨率 : 1920*1080(单位就是px)
     * 6. 尺寸 :
     * <p>
     * <p>
     * 二. 基本公式
     * 1. Android渲染采用的单位是px,我们在使用dp时,也会先转为px
     * px = dp * density
     * 2. 屏幕的density计算方式
     * density = dpi / 160;
     * 3. dpi计算方式
     * dpi = Math.sqrt(宽度*宽度+高度*高度)/屏幕的尺寸
     */

    public static float modifyDensity;

    public static float modifyScaledDensity;

    /**
     * 在每个activity中初始化即可
     *
     * @param application
     * @param activity
     */
    public static void setCustomDensity(final Application application, Activity activity, float uiWidth) {
        DisplayMetrics appMetrics = activity.getResources().getDisplayMetrics();
        if (modifyDensity == 0) {
            modifyDensity = appMetrics.density;
            modifyScaledDensity = appMetrics.scaledDensity;
//            // 监听用户修改系统字体大小
//            application.registerComponentCallbacks(new ComponentCallbacks() {
//                @Override
//                public void onConfigurationChanged(Configuration newConfig) {
//                    if (null != newConfig && newConfig.fontScale > 0) {
//                        modifyScaledDensity = application.getResources().getDisplayMetrics().scaledDensity;
//                    }
//                }
//
//                @Override
//                public void onLowMemory() {
//
//                }
//            });
        }
        // 1. 以宽度为统一维度来设计
        // 目标像素密度 =  宽度 / 我们现在设计图的宽度
        float targetDensity = appMetrics.widthPixels / uiWidth;
        float targetScaledDensity = appMetrics.scaledDensity * (modifyScaledDensity / modifyDensity);
        // 目标dpi
        int targetDpi = (int) (targetDensity * 160);

        appMetrics.density = targetDensity;
        appMetrics.scaledDensity = targetScaledDensity;
        appMetrics.densityDpi = targetDpi;


        // 2. 设置当前系统的像素密度和dpi
        DisplayMetrics activityMetrics = activity.getResources().getDisplayMetrics();
        activityMetrics.density = targetDensity;
        activityMetrics.scaledDensity = targetScaledDensity;
        activityMetrics.densityDpi = targetDpi;
    }

    /**
     * 在application初始化
     * @param application
     */
    public static void setCustomDensity(final Application application, float uiWidth) {
        DisplayMetrics appMetrics = application.getResources().getDisplayMetrics();
        registerActivityLifecycleCallbacks(application, appMetrics);
        if (modifyDensity == 0) {
            modifyDensity = appMetrics.density;
            modifyScaledDensity = appMetrics.scaledDensity;
            // 监听用户修改系统字体大小
            application.registerComponentCallbacks(new ComponentCallbacks() {
                @Override
                public void onConfigurationChanged(Configuration newConfig) {
                    if (null != newConfig && newConfig.fontScale > 0) {
                        modifyScaledDensity = application.getResources().getDisplayMetrics().scaledDensity;
                    }
                }

                @Override
                public void onLowMemory() {

                }
            });
        }
        // 1. 以宽度为统一维度来设计
        // 目标像素密度 =  宽度 / 我们现在设计图的宽度
        float targetDensity = appMetrics.widthPixels / uiWidth;
        float targetScaledDensity = appMetrics.scaledDensity * (modifyScaledDensity / modifyDensity);
        // 目标dpi
        int targetDpi = (int) (targetDensity * 160);

        appMetrics.density = targetDensity;
        appMetrics.scaledDensity = targetScaledDensity;
        appMetrics.densityDpi = targetDpi;


        // 2. 设置当前系统的像素密度和dpi
        DisplayMetrics activityMetrics = application.getResources().getDisplayMetrics();
        activityMetrics.density = targetDensity;
        activityMetrics.scaledDensity = targetScaledDensity;
        activityMetrics.densityDpi = targetDpi;
    }

    private static void setActivity(Activity activity, DisplayMetrics appMetrics) {
        // 1. 以宽度为统一维度来设计
        // 目标像素密度 =  宽度 / 我们现在设计图的宽度
        float targetDensity = appMetrics.widthPixels / 960f;
        float targetScaledDensity = appMetrics.scaledDensity * (modifyScaledDensity / modifyDensity);
        // 目标dpi
        int targetDpi = (int) (targetDensity * 160);

        appMetrics.density = targetDensity;
        appMetrics.scaledDensity = targetScaledDensity;
        appMetrics.densityDpi = targetDpi;


        // 2. 设置当前系统的像素密度和dpi
        DisplayMetrics activityMetrics = activity.getResources().getDisplayMetrics();
        activityMetrics.density = targetDensity;
        activityMetrics.scaledDensity = targetScaledDensity;
        activityMetrics.densityDpi = targetDpi;
    }


    private static void registerActivityLifecycleCallbacks(Application application, final DisplayMetrics appMetrics) {
        application.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                setActivity(activity, appMetrics);
            }

            @Override
            public void onActivityStarted(Activity activity) {
            }

            @Override
            public void onActivityResumed(Activity activity) {
            }

            @Override
            public void onActivityPaused(Activity activity) {
            }

            @Override
            public void onActivityStopped(Activity activity) {
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
            }

            @Override
            public void onActivityDestroyed(Activity activity) {

            }
        });


    }
}
