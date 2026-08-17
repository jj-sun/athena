package com.athena.modules.job.controller;

import com.athena.common.annotation.Log;
import com.athena.common.base.dto.PageDto;
import com.athena.common.utils.PageUtils;
import com.athena.common.utils.Result;
import com.athena.common.validator.ValidatorUtils;
import com.athena.modules.job.entity.ScheduleJob;
import com.athena.modules.job.service.ScheduleJobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 定时任务
 *
 * @author Mr.sun
 */
@RestController
@RequestMapping("/sys/schedule")
public class ScheduleJobController {
	@Autowired
	private ScheduleJobService scheduleJobService;
	
	/**
	 * 定时任务列表
	 */
	@GetMapping("/list")
	//@RequiresPermissions("sys:schedule:list")
	public Result<PageUtils> list(ScheduleJob scheduleJob, PageDto pageDto){
		PageUtils page = scheduleJobService.queryPage(scheduleJob, pageDto);

		return Result.ok(page);
	}
	
	/**
	 * 定时任务信息
	 */
	@GetMapping("/info/{jobId}")
	//@RequiresPermissions("sys:schedule:info")
	public Result<ScheduleJob> info(@PathVariable("jobId") Long jobId){
		ScheduleJob schedule = scheduleJobService.getById(jobId);
		
		return Result.ok(schedule);
	}

	/**
	 * 获取可用的定时任务 Bean 列表
	 */
	@GetMapping("/taskBeanList")
	public Result<List<Map<String, Object>>> taskBeanList(@RequestParam(name = "excludeId", required = false) String excludeId) {
		List<Map<String, Object>> list = scheduleJobService.getTaskBeanList(excludeId);
		return Result.ok(list);
	}
	
	/**
	 * 保存定时任务
	 */
	@Log("保存定时任务")
	@PostMapping("/save")
	//@RequiresPermissions("sys:schedule:save")
	public Result<Object> save(@RequestBody ScheduleJob scheduleJob){
		ValidatorUtils.validateEntity(scheduleJob);
		
		scheduleJobService.saveJob(scheduleJob);
		
		return Result.ok();
	}
	
	/**
	 * 修改定时任务
	 */
	@Log("修改定时任务")
	@PutMapping("/update")
	//@RequiresPermissions("sys:schedule:update")
	public Result<Object> update(@RequestBody ScheduleJob scheduleJob){
		ValidatorUtils.validateEntity(scheduleJob);
				
		scheduleJobService.update(scheduleJob);
		
		return Result.ok();
	}
	
	/**
	 * 删除定时任务
	 */
	@Log("删除定时任务")
	@DeleteMapping("/delete")
	//@RequiresPermissions("sys:schedule:delete")
	public Result<Object> delete(@RequestParam(name = "id") String id){
		scheduleJobService.deleteBatch(List.of(id));
		return Result.ok();
	}
	/**
	 * 批量删除定时任务
	 */
	@Log("批量删除定时任务")
	@DeleteMapping("/deleteBatch")
	//@RequiresPermissions("sys:schedule:delete")
	public Result<Object> deleteBatch(@RequestParam(name = "ids") String ids){
		scheduleJobService.deleteBatch(List.of(ids.split(",")));
		return Result.ok();
	}
	
	/**
	 * 立即执行任务
	 */
	@Log("立即执行任务")
	@GetMapping("/run")
	//@RequiresPermissions("sys:schedule:run")
	public Result<Object> run(@RequestParam(name = "id") String id){
		scheduleJobService.run(List.of(id));
		
		return Result.ok();
	}
	
	/**
	 * 暂停定时任务
	 */
	@Log("暂停定时任务")
	@GetMapping("/pause")
	//@RequiresPermissions("sys:schedule:pause")
	public Result<Object> pause(@RequestParam(name = "id") String id){
		scheduleJobService.pause(List.of(id));
		
		return Result.ok();
	}
	
	/**
	 * 恢复定时任务
	 */
	@Log("恢复定时任务")
	@GetMapping("/resume")
	//@RequiresPermissions("sys:schedule:resume")
	public Result<Object> resume(@RequestParam(name = "id") String id){
		scheduleJobService.resume(List.of(id));
		
		return Result.ok();
	}

}
