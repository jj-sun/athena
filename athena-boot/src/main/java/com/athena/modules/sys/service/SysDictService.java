package com.athena.modules.sys.service;

import com.athena.common.base.dto.PageDto;
import com.athena.common.utils.PageUtils;
import com.athena.common.vo.DictModel;
import com.athena.modules.sys.entity.SysDict;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * @author Mr.sun
 * @date 2022/1/19 19:58
 * @description
 */
public interface SysDictService extends IService<SysDict> {

    PageUtils queryPage(SysDict dict, PageDto pageDto);

    boolean deleteEntity(String id);

    /**
     * 通过查询指定table的 text code key 获取字典值
     *
     * @param table
     * @param text
     * @param code
     * @param key
     * @return
     */
    DictModel queryTableDictTextByKey(String table, String text, String code, String key);

    DictModel queryDictTextByKey(String code, String key);

}
