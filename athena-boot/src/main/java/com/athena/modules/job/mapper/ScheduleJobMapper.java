package com.athena.modules.job.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.athena.modules.job.entity.ScheduleJob;
import org.apache.ibatis.annotations.Mapper;

import java.util.Map;

/**
 * 定时任务
 *
 * @author Mr.sun
 */
@Mapper
public interface ScheduleJobMapper extends BaseMapper<ScheduleJob> {
	
	/**
	 * 批量更新状态
	 */
	int updateBatch(Map<String, Object> map);
}
