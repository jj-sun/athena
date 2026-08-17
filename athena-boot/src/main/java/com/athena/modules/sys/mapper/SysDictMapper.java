package com.athena.modules.sys.mapper;

import com.athena.common.vo.DictModel;
import com.athena.common.vo.DictModelMany;
import com.athena.modules.sys.entity.SysDict;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author Mr.sun
 * @date 2022/1/19 19:57
 * @description
 */
public interface SysDictMapper extends BaseMapper<SysDict> {

    DictModel queryTableDictTextByKey(@Param("table") String table, @Param("text") String text, @Param("code") String code, @Param("key") String key);

    DictModel queryDictTextByKey(@Param("code") String code,@Param("key") String key);


}
