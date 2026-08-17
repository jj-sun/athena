package com.athena.modules.job.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 定时任务日志
 *
 * @author Mr.sun
 */
@Data
@TableName("schedule_job_log")
public class ScheduleJobLog implements Serializable {
	@Serial
	private static final long serialVersionUID = 1L;
	
	/**
	 * 日志id
	 */
	@TableId
	private String id;
	
	/**
	 * 任务id
	 */
	private String jobId;
	
	/**
	 * spring bean名称
	 */
	private String beanName;
	
	/**
	 * 参数
	 */
	private String parameter;
	
	/**
	 * 任务状态    0：成功    1：失败
	 */
	private Integer status;
	
	/**
	 * 失败信息
	 */
	private String error;
	
	/**
	 * 耗时(单位：毫秒)
	 */
	private Integer times;
	
	/**
	 * 创建时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date ctime;
	
}
