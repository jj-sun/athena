package com.athena.modules.sys.service.impl;

import com.athena.common.base.dto.PageDto;
import com.athena.common.utils.PageUtils;
import com.athena.common.utils.Query;
import com.athena.modules.sys.entity.SysLog;
import com.athena.modules.sys.mapper.SysLogMapper;
import com.athena.modules.sys.service.SysLogService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;


/**
 * @author Mr.sun
 */
@Service
public class SysLogServiceImpl extends ServiceImpl<SysLogMapper, SysLog> implements SysLogService {

    @Override
    public PageUtils queryPage(SysLog sysLog, PageDto pageDto) {

        IPage<SysLog> page = this.page(
            new Query<SysLog>().getPage(pageDto),
                new LambdaQueryWrapper<SysLog>().like(StringUtils.isNotBlank(sysLog.getOperation()), SysLog::getOperation, sysLog.getOperation())
        );

        return new PageUtils(page);
    }
}
