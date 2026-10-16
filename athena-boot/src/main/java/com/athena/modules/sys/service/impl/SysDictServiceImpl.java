package com.athena.modules.sys.service.impl;

import com.athena.common.base.dto.PageDto;
import com.athena.common.exception.RRException;
import com.athena.common.utils.PageUtils;
import com.athena.common.utils.Query;
import com.athena.common.vo.DictModel;
import com.athena.common.vo.DictModelMany;
import com.athena.common.xss.SQLFilter;
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
import org.springframework.beans.factory.annotation.Value;
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

    /**
     * 允许通过 queryTableDictTextByKey 动态翻译的字典表白名单（对应 @Dict(dictTable = ...)）。
     * 新增字典表时需在此处配置，否则会被拒绝，避免任意表名进入 ${} 拼接。
     */
    @Value("${athena.dict.allowed-tables:sys_dept}")
    private List<String> allowedTables;

    @Override
    public PageUtils queryPage(SysDict dict, PageDto pageDto) {
        IPage<SysDict> page = this.page(
                new Query<SysDict>().getPage(pageDto, "ctime", false),
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
        // SysDictMapper.queryTableDictTextByKey 用 ${table}/${text}/${code} 直接拼接 SQL，
        // 这三个参数必须经过校验才能进入 mapper。
        // 目前它们来自 @Dict 注解的编译期常量，但注解属性是 String，一旦将来被配置化或
        // 由请求参数透传，未校验的拼接会立刻变成注入点，所以在此处统一收口。
        String safeTable = SQLFilter.requireIdentifier(table, "字典表名");
        if (!allowedTables.contains(safeTable)) {
            throw new RRException("非法的字典表名：" + safeTable);
        }
        String safeText = SQLFilter.requireIdentifier(text, "字典文本字段");
        String safeCode = SQLFilter.requireIdentifier(code, "字典值字段");
        return this.getBaseMapper().queryTableDictTextByKey(safeTable, safeText, safeCode, key);
    }

    @Override
    public DictModel queryDictTextByKey(String code, String key) {
        return this.getBaseMapper().queryDictTextByKey(code, key);
    }

}
