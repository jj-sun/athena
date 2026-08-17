package com.athena.common.annotation;

import java.lang.annotation.*;

/**
 * 字典翻译开关
 * 用于控制Controller方法或类的返回值是否进行字典翻译
 *
 * @author Mr.sun
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DictTranslate {

    /**
     * 是否启用字典翻译
     * true：启用翻译（默认）
     * false：禁用翻译
     */
    boolean value() default true;
}