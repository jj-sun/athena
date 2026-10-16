

package com.athena.modules.job.utils;

import com.athena.common.utils.SpringContextUtils;
import com.athena.modules.job.entity.ScheduleJobLog;
import com.athena.modules.job.service.ScheduleJobLogService;
import com.athena.modules.job.task.ITask;
import org.apache.commons.lang3.StringUtils;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.util.Date;


/**
 * 定时任务
 *
 * @author Mr.sun
 */
public class ScheduleJob extends QuartzJobBean {
	private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        com.athena.modules.job.entity.ScheduleJob scheduleJob = (com.athena.modules.job.entity.ScheduleJob) context.getMergedJobDataMap()
        		.get(com.athena.modules.job.entity.ScheduleJob.JOB_PARAM_KEY);
        
        //获取spring bean
        ScheduleJobLogService scheduleJobLogService = (ScheduleJobLogService) SpringContextUtils.getBean("scheduleJobLogService");
        
        //数据库保存执行记录
        ScheduleJobLog log = new ScheduleJobLog();
        log.setJobId(scheduleJob.getId());
        log.setBeanName(scheduleJob.getBeanName());
        log.setParameter(scheduleJob.getParameter());
        log.setCtime(new Date());
        
        //任务开始时间
        long startTime = System.currentTimeMillis();
        
        try {
            //执行任务
        	logger.debug("任务准备执行，任务ID：" + scheduleJob.getId());

			// 只允许调用实现了 ITask 的任务 Bean。
			// 原实现是 getBean(beanName) 后再用反射 getDeclaredMethod("run") 调用任意对象，
			// 而 beanName 来自客户端提交的任务配置，等于把容器内任意 Bean 的方法调用能力
			// 开放给了能创建/触发定时任务的人。这里按类型收窄，非 ITask 的 Bean 直接拒绝。
			ITask target = SpringContextUtils.getBean(scheduleJob.getBeanName(), ITask.class);
			target.run(scheduleJob.getParameter());
			
			//任务执行总时长
			long times = System.currentTimeMillis() - startTime;
			log.setTimes((int)times);
			//任务状态    0：成功    1：失败
			log.setStatus(0);
			
			logger.debug("任务执行完毕，任务ID：" + scheduleJob.getId() + "  总共耗时：" + times + "毫秒");
		} catch (Exception e) {
			logger.error("任务执行失败，任务ID：" + scheduleJob.getId(), e);
			
			//任务执行总时长
			long times = System.currentTimeMillis() - startTime;
			log.setTimes((int)times);
			
			//任务状态    0：成功    1：失败
			log.setStatus(1);
			log.setError(StringUtils.substring(e.toString(), 0, 2000));
		}finally {
			scheduleJobLogService.save(log);
		}
    }
}
