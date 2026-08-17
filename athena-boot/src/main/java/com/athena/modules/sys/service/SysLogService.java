package com.athena.modules.sys.service;


import com.athena.common.base.dto.PageDto;
import com.athena.common.utils.PageUtils;
import com.athena.modules.sys.entity.SysLog;
import com.baomidou.mybatisplus.extension.service.IService;


/**
 * 系统日志
 *
 * @author Mr.sun
 */
public interface SysLogService extends IService<SysLog> {

    PageUtils queryPage(SysLog sysLog, PageDto pageDto);

}
