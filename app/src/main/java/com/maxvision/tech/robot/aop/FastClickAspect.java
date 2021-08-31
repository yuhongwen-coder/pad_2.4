package com.maxvision.tech.robot.aop;

import android.util.ArrayMap;
import android.util.Log;
import android.util.SparseArray;
import android.widget.Toast;

import com.maxvision.tech.robot.utils.CustomToast;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * name: zjj
 * date: 2021/05/06
 * time: 10:13
 * desc: 快速点击切面
 */
@Aspect
public class FastClickAspect {

    private static final HashMap<String,Long> hashMap = new LinkedHashMap<>();

    @Pointcut("execution(@com.maxvision.tech.robot.aop.FastClick * *(..))")
    public void executeFastClick() {

    }

    @Around("executeFastClick()")
    public Object fastClick(ProceedingJoinPoint joinPoint) throws Throwable{
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        FastClick fastClick = signature.getMethod().getAnnotation(FastClick.class);
        if (fastClick != null) {
            long filterTime = fastClick.filterTime();
            String clickPos = joinPoint.toShortString();
            Long existTime = hashMap.get(clickPos);
            long lastTime;
            if (null == existTime) {
                // 之前也没有点过
                lastTime = System.currentTimeMillis();
                hashMap.put(clickPos, lastTime);
                Log.e("ZJJAOP","之前没有点击过 点击:"+clickPos);
                return joinPoint.proceed();
            } else {
                if (System.currentTimeMillis() - existTime > filterTime) {
                    lastTime = System.currentTimeMillis();
                    hashMap.put(clickPos, lastTime);
                    Log.e("ZJJAOP","相同事件 点击:"+clickPos);
                    return joinPoint.proceed();
                } else {
                    Log.e("ZJJAOP","过滤:"+clickPos);
                    CustomToast.toastLong(CustomToast.TIP_ERROR,"操作过快,请稍候再试");
                    return null;
                }
            }
        }
        return joinPoint.proceed();
    }

}
