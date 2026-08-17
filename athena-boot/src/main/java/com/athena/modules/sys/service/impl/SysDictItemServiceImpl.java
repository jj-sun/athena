package com.athena.modules.sys.service.impl;

import com.athena.common.base.dto.PageDto;
import com.athena.common.utils.PageUtils;
import com.athena.common.utils.Query;
import com.athena.modules.sys.entity.SysDictItem;
import com.athena.modules.sys.mapper.SysDictItemMapper;
import com.athena.modules.sys.service.SysDictItemService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @author Mr.sun
 * @date 2022/1/19 20:00
 * @description
 */
@Service
public class SysDictItemServiceImpl extends ServiceImpl<SysDictItemMapper, SysDictItem> implements SysDictItemService {

    @Override
    public PageUtils queryPage(SysDictItem dictItem, PageDto pageDto) {
        IPage<SysDictItem> page = this.page(
                new Query<SysDictItem>().getPage(pageDto),
                new LambdaQueryWrapper<SysDictItem>()
                        .eq(StringUtils.isNotBlank(dictItem.getDictId()), SysDictItem::getDictId, dictItem.getDictId())
                        .like(StringUtils.isNotBlank(dictItem.getItemText()), SysDictItem::getItemText, dictItem.getItemText())
                        .eq(Objects.nonNull(dictItem.getDelFlag()), SysDictItem::getDelFlag, dictItem.getDelFlag())
        );
        return new PageUtils(page);
    }
}
