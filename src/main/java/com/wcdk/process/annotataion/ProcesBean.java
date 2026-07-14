package com.wcdk.process.annotataion;


import jakarta.validation.constraints.NotNull;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @version 1.0
 * @auther WCDK
 * @date 2026/7/13
 **/
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ProcesBean {

    /**
     * 流程回调bean名称
     *
     * @return bean名称
     */
    @NotNull String value();
}
