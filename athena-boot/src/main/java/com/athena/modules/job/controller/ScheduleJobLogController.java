package com.athena.modules.job.controller;

import com.athena.common.base.dto.PageDto;
import com.athena.common.utils.PageUtils;
import com.athena.common.utils.Result;
import com.athena.modules.job.entity.ScheduleJobLog;
import com.athena.modules.job.service.ScheduleJobLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 定时任务日志
 *
 * @author Mr.sun
 */
@RestController
@RequestMapping("/sys/scheduleLog")
public class ScheduleJobLogController {
	@Autowired
	private ScheduleJobLogService scheduleJobLogService;
	
	/**
	 * 定时任务日志列表
	 */
	@GetMapping("/list")
	//@RequiresPermissions("sys:schedule:log")
	public Result<PageUtils> list(ScheduleJobLog scheduleJobLog, PageDto pageDto){
		PageUtils page = scheduleJobLogService.queryPage(scheduleJobLog, pageDto);
		
		return Result.ok(page);
	}
	
	/**
	 * 定时任务日志信息
	 */
	@GetMapping("/info/{logId}")
	public Result<ScheduleJobLog> info(@PathVariable("logId") Long logId){
		ScheduleJobLog log = scheduleJobLogService.getById(logId);
		return Result.ok(log);
	}
}
