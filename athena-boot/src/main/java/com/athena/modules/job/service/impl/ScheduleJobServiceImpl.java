package com.athena.modules.job.service.impl;

import com.athena.common.base.dto.PageDto;
import com.athena.common.constant.Constant;
import com.athena.common.utils.PageUtils;
import com.athena.common.utils.Query;
import com.athena.modules.job.entity.ScheduleJob;
import com.athena.modules.job.mapper.ScheduleJobMapper;
import com.athena.modules.job.service.ScheduleJobService;
import com.athena.modules.job.task.ITask;
import com.athena.modules.job.utils.ScheduleUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.PostConstruct;
import org.apache.commons.lang3.StringUtils;
import org.quartz.CronTrigger;
import org.quartz.Scheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Mr.sun
 */
@Service
public class ScheduleJobServiceImpl extends ServiceImpl<ScheduleJobMapper, ScheduleJob> implements ScheduleJobService {
	@Autowired
    private Scheduler scheduler;

	@Autowired
	private ApplicationContext applicationContext;
	
	/**
	 * 项目启动时，初始化定时器
	 */
	@PostConstruct
	public void init(){
		List<ScheduleJob> scheduleJobList = this.list();
		for(ScheduleJob scheduleJob : scheduleJobList){
			CronTrigger cronTrigger = ScheduleUtils.getCronTrigger(scheduler, scheduleJob.getId());
            //如果不存在，则创建
            if(cronTrigger == null) {
                ScheduleUtils.createScheduleJob(scheduler, scheduleJob);
            }else {
                ScheduleUtils.updateScheduleJob(scheduler, scheduleJob);
            }
		}
	}

	@Override
	public PageUtils queryPage(ScheduleJob scheduleJob, PageDto pageDto) {

		IPage<ScheduleJob> page = this.page(
			new Query<ScheduleJob>().getPage(pageDto, "ctime", false),
			new LambdaQueryWrapper<ScheduleJob>().like(StringUtils.isNotBlank(scheduleJob.getBeanName()), ScheduleJob::getBeanName, scheduleJob.getBeanName())
					.eq(Objects.nonNull(scheduleJob.getStatus()), ScheduleJob::getStatus, scheduleJob.getStatus())
		);

		return new PageUtils(page);
	}


	@Override
	@Transactional(rollbackFor = Exception.class)
	public void saveJob(ScheduleJob scheduleJob) {
		scheduleJob.setCtime(new Date());
		scheduleJob.setStatus(Constant.ScheduleStatus.NORMAL.getValue());
        this.save(scheduleJob);
        
        ScheduleUtils.createScheduleJob(scheduler, scheduleJob);
    }
	
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void update(ScheduleJob scheduleJob) {
        ScheduleUtils.updateScheduleJob(scheduler, scheduleJob);
                
        this.updateById(scheduleJob);
    }

	@Override
	@Transactional(rollbackFor = Exception.class)
    public void deleteBatch(List<String> jobIds) {
    	for(String jobId : jobIds){
    		ScheduleUtils.deleteScheduleJob(scheduler, jobId);
    	}
    	
    	//删除数据
    	this.removeByIds(jobIds);
	}

	@Override
    public int updateBatch(List<String> jobIds, int status){
    	Map<String, Object> map = new HashMap<>(2);
    	map.put("list", jobIds);
    	map.put("status", status);
    	return baseMapper.updateBatch(map);
    }
    
	@Override
	@Transactional(rollbackFor = Exception.class)
    public void run(List<String> jobIds) {
    	for(String jobId : jobIds){
    		ScheduleUtils.run(scheduler, this.getById(jobId));
    	}
    }

	@Override
	@Transactional(rollbackFor = Exception.class)
    public void pause(List<String> jobIds) {
        for(String jobId : jobIds){
    		ScheduleUtils.pauseJob(scheduler, jobId);
    	}
        
    	updateBatch(jobIds, Constant.ScheduleStatus.PAUSE.getValue());
    }

	@Override
	@Transactional(rollbackFor = Exception.class)
    public void resume(List<String> jobIds) {
    	for(String jobId : jobIds){
    		ScheduleUtils.resumeJob(scheduler, jobId);
    	}

    	updateBatch(jobIds, Constant.ScheduleStatus.NORMAL.getValue());
    }

	@Override
	public List<Map<String, Object>> getTaskBeanList(String excludeId) {
		// 1. 获取所有 ITask 实现类的 Bean 名称
		Map<String, ITask> taskBeans = applicationContext.getBeansOfType(ITask.class);

		// 2. 查询数据库中已使用的 beanName（排除 excludeId 对应的记录）
		List<ScheduleJob> allJobs = this.list();
		Set<String> usedBeanNames = allJobs.stream()
				.filter(job -> {
					if (StringUtils.isBlank(excludeId)) {
						return true;
					}
					// 编辑模式下，排除自身这条记录的 beanName
					return !excludeId.equals(job.getId());
				})
				.map(ScheduleJob::getBeanName)
				.filter(StringUtils::isNotBlank)
				.collect(Collectors.toSet());

		// 3. 构造返回结果
		List<Map<String, Object>> result = new ArrayList<>();
		for (Map.Entry<String, ITask> entry : taskBeans.entrySet()) {
			String beanName = entry.getKey();
			Class<?> beanClass = entry.getValue().getClass();
			// 如果是 CGLIB 代理类，取父类
			String className = beanClass.getSimpleName();
			if (className.contains("$$")) {
				className = beanClass.getSuperclass().getSimpleName();
			}

			Map<String, Object> item = new HashMap<>(3);
			item.put("beanName", beanName);
			item.put("className", className);
			item.put("disabled", usedBeanNames.contains(beanName));
			result.add(item);
		}

		return result;
	}
    
}
