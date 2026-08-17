package com.athena.modules.sys.service.impl;

import com.athena.common.base.dto.PageDto;
import com.athena.common.utils.PageUtils;
import com.athena.common.utils.Query;
import com.athena.common.vo.DictModel;
import com.athena.common.vo.DictModelMany;
import com.athena.modules.sys.entity.SysDict;
import com.athena.modules.sys.entity.SysDictItem;
import com.athena.modules.sys.mapper.SysDictMapper;
import com.athena.modules.sys.service.SysDictItemService;
import com.athena.modules.sys.service.SysDictService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Mr.sun
 * @date 2022/1/19 19:59
 * @description
 */
@Service
public class SysDictServiceImpl extends ServiceImpl<SysDictMapper, SysDict> implements SysDictService {

    @Autowired
    private SysDictItemService dictItemService;

    @Override
    public PageUtils queryPage(SysDict dict, PageDto pageDto) {
        IPage<SysDict> page = this.page(
                new Query<SysDict>().getPage(pageDto),
                new LambdaQueryWrapper<SysDict>()
                        .like(StringUtils.isNotBlank(dict.getDictName()), SysDict::getDictName, dict.getDictName())
                        .eq(StringUtils.isNotBlank(dict.getDictCode()), SysDict::getDictCode, dict.getDictCode())
        );
        return new PageUtils(page);
    }

    @Override
    public boolean deleteEntity(String id) {
        dictItemService.remove(new LambdaQueryWrapper<SysDictItem>().eq(SysDictItem::getDictId, id));
        return this.removeById(id);
    }

    @Override
    public DictModel queryTableDictTextByKey(String table, String text, String code, String key) {
        return this.getBaseMapper().queryTableDictTextByKey(table, text, code, key);
    }

    @Override
    public DictModel queryDictTextByKey(String code, String key) {
        return this.getBaseMapper().queryDictTextByKey(code, key);
    }

}
