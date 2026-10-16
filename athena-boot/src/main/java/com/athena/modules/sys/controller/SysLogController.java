package com.athena.modules.sys.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.athena.common.base.dto.PageDto;
import com.athena.common.utils.PageUtils;
import com.athena.common.utils.Result;
import com.athena.modules.sys.entity.SysLog;
import com.athena.modules.sys.service.SysLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * 系统日志
 *
 * @author Mr.sun
 */
@RestController
@RequestMapping("/sys/log")
public class SysLogController {
	@Autowired
	private SysLogService sysLogService;
	
	/**
	 * 列表
	 */
	@GetMapping("/list")
	@SaCheckPermission("sys:log:list")
	public Result<PageUtils> list(SysLog sysLog, PageDto pageDto){
		PageUtils page = sysLogService.queryPage(sysLog, pageDto);
		return Result.ok(page);
	}
	
}
