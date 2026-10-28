package com.athena.modules.sys.service.impl;

import com.athena.common.base.dto.PageDto;
import com.athena.common.utils.PageUtils;
import com.athena.common.utils.Query;
import com.athena.modules.sys.entity.SysDictItem;
import com.athena.modules.sys.mapper.SysDictItemMapper;
import com.athena.modules.sys.service.SysDictItemService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
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
        // 字典项按 sort_order 升序展示（无 sort_order 的历史数据排在前面）
        IPage<SysDictItem> page = new Query<SysDictItem>().getPage(pageDto, "sort_order", true);
        // sort_order 存在重复值，追加 id 兜底，避免翻页时同一条记录跨页重复/遗漏
        page.orders().add(OrderItem.asc("id"));
        this.page(
                page,
                new LambdaQueryWrapper<SysDictItem>()
                        .eq(StringUtils.isNotBlank(dictItem.getDictId()), SysDictItem::getDictId, dictItem.getDictId())
                        .like(StringUtils.isNotBlank(dictItem.getItemText()), SysDictItem::getItemText, dictItem.getItemText())
                        .like(StringUtils.isNotBlank(dictItem.getItemValue()), SysDictItem::getItemValue, dictItem.getItemValue())
                        .eq(Objects.nonNull(dictItem.getDelFlag()), SysDictItem::getDelFlag, dictItem.getDelFlag())
        );
        return new PageUtils(page);
    }
}
