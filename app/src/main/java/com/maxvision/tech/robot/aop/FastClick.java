package com.maxvision.tech.robot.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * name: zjj
 * date: 2021/05/06
 * time: 10:22
 * desc: 注解
 */

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FastClick {
    long filterTime();
}
