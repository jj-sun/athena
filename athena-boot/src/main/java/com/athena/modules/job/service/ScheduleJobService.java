

package com.athena.modules.job.service;

import com.athena.common.base.dto.PageDto;
import com.athena.common.utils.PageUtils;
import com.athena.modules.job.entity.ScheduleJob;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * 定时任务
 *
 * @author Mr.sun
 */
public interface ScheduleJobService extends IService<ScheduleJob> {

	PageUtils queryPage(ScheduleJob scheduleJob, PageDto pageDto);

	/**
	 * 保存定时任务
	 */
	void saveJob(ScheduleJob scheduleJob);
	
	/**
	 * 更新定时任务
	 */
	void update(ScheduleJob scheduleJob);
	
	/**
	 * 批量删除定时任务
	 */
	void deleteBatch(List<String> jobIds);

	/**
	 * 批量更新定时任务状态
	 */
	int updateBatch(List<String> jobIds, int status);
	
	/**
	 * 立即执行
	 */
	void run(List<String> jobIds);
	
	/**
	 * 暂停运行
	 */
	void pause(List<String> jobIds);
	
	/**
	 * 恢复运行
	 */
	void resume(List<String> jobIds);

	/**
	 * 获取可用的定时任务 Bean 列表
	 * @param excludeId 排除的任务ID（编辑时传入，自身beanName保持可选）
	 */
	List<Map<String, Object>> getTaskBeanList(String excludeId);
}
