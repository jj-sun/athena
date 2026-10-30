package com.athena.modules.sys.validator;

import com.athena.common.utils.SpringContextUtils;
import com.athena.modules.sys.entity.SysUser;
import com.athena.modules.sys.service.SysUserService;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;

/**
 * {@link UniqueUser} 的校验器。
 *
 * <p>{@code ValidatorUtils} 使用 Hibernate Validator 的默认工厂创建校验器，无法直接注入 Spring Bean，
 * 因此通过项目已有的 {@link SpringContextUtils} 在首次校验时取到 {@link SysUserService}。
 * 非 Spring 环境（如不启动容器的纯单元测试）下跳过唯一性校验，由数据库唯一索引兜底。
 *
 * @author Mr.sun
 */
public class UniqueUserValidator implements ConstraintValidator<UniqueUser, SysUser> {

	private volatile SysUserService sysUserService;

	@Override
	public boolean isValid(SysUser user, ConstraintValidatorContext context) {
		if (user == null) {
			return true;
		}
		SysUserService service = getSysUserService();
		if (service == null) {
			return true;
		}

		boolean valid = true;
		context.disableDefaultConstraintViolation();
		if (StringUtils.isNotBlank(user.getUsername())
				&& service.existsByUsername(user.getUsername(), user.getId())) {
			context.buildConstraintViolationWithTemplate("用户名已存在")
					.addPropertyNode("username")
					.addConstraintViolation();
			valid = false;
		}
		if (StringUtils.isNotBlank(user.getMobile())
				&& service.existsByMobile(user.getMobile(), user.getId())) {
			context.buildConstraintViolationWithTemplate("手机号已存在")
					.addPropertyNode("mobile")
					.addConstraintViolation();
			valid = false;
		}
		return valid;
	}

	private SysUserService getSysUserService() {
		if (sysUserService == null && SpringContextUtils.applicationContext != null) {
			sysUserService = SpringContextUtils.getBean(SysUserService.class);
		}
		return sysUserService;
	}
}
