package com.athena.modules.{{module}}.mapper;

import com.athena.modules.{{module}}.entity.{{Entity}};
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * {{entityLabel}}
 */
@Mapper
public interface {{Entity}}Mapper extends BaseMapper<{{Entity}}> {
}
