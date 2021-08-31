package com.maxvision.tech.robot.utils;

import android.hardware.Camera;

/**
 * @author qs
 * @time 2021/4/26 11:13
 * @describe $
 */
public class CameraUtil {

    public static boolean isCameraCanUse() {
        boolean canUse = false;
        Camera mCamera = null;
        try {
            mCamera = Camera.open(0);
            Camera.Parameters mParameters = mCamera.getParameters();
            mCamera.setParameters(mParameters);
        } catch (Exception e) {
            canUse = false;
        }
        if (mCamera != null) {
            mCamera.release();
            canUse = true;
        }
        return canUse;
    }

}
