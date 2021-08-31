package com.maxvision.tech.robot.utils;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


/**
 * Created by yuhongwen
 * on 2021/4/28
 */
public class ActivityLifeRecord implements Application.ActivityLifecycleCallbacks {
    private Activity currentActivity;

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
        currentActivity = activity;
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {

    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        currentActivity = activity;
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {

    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
    }

    /**
     * 当前Actiivty是否参数Activity
     * @param activity 参数Activity
     * @return
     */
    public boolean currentIsParamsActivity(Class<? extends Activity> activity) {
        if (activity == null || currentActivity == null) return false;
        if (TextUtils.equals(currentActivity.getClass().getName(), activity.getName())) {
            return true;
        }
        return false;
    }


}
