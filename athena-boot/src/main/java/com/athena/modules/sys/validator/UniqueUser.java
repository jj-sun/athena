package com.athena.modules.sys.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用户唯一性校验：用户名、手机号在库中不能重复。
 *
 * <p>编辑时通过实体自身的 id 排除当前用户。
 *
 * @author Mr.sun
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UniqueUserValidator.class)
public @interface UniqueUser {

	String message() default "用户信息已存在";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
