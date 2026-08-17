package com.athena.modules.sys.service;

import com.athena.common.base.dto.PageDto;
import com.athena.common.utils.PageUtils;
import com.athena.modules.sys.entity.SysDictItem;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author Mr.sun
 * @date 2022/1/19 19:58
 * @description
 */
public interface SysDictItemService extends IService<SysDictItem> {

    PageUtils queryPage(SysDictItem dictItem, PageDto pageDto);

}
