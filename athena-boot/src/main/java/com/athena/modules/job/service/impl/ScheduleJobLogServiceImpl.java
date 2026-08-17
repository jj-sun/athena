package com.athena.modules.job.service.impl;

import com.athena.common.base.dto.PageDto;
import com.athena.common.utils.PageUtils;
import com.athena.common.utils.Query;
import com.athena.modules.job.entity.ScheduleJobLog;
import com.athena.modules.job.mapper.ScheduleJobLogMapper;
import com.athena.modules.job.service.ScheduleJobLogService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * @author Mr.sun
 */
@Service("scheduleJobLogService")
public class ScheduleJobLogServiceImpl extends ServiceImpl<ScheduleJobLogMapper, ScheduleJobLog> implements ScheduleJobLogService {

	@Override
	public PageUtils queryPage(ScheduleJobLog scheduleJobLog, PageDto pageDto) {

		IPage<ScheduleJobLog> page = this.page(
			new Query<ScheduleJobLog>().getPage(pageDto),
			new QueryWrapper<ScheduleJobLog>()
		);

		return new PageUtils(page);
	}

}
