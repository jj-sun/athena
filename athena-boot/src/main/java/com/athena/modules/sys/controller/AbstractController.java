package com.athena.modules.sys.controller;

import com.athena.common.utils.SecurityUtil;
import com.athena.modules.sys.entity.SysUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller公共组件
 *
 * @author Mr.sun
 */
public abstract class AbstractController {
	protected Logger logger = LoggerFactory.getLogger(getClass());


	protected String getUsername() {
		return SecurityUtil.getLoginUsername();
	}
}
