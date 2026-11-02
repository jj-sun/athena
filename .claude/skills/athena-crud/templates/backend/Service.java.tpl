package com.athena.modules.{{module}}.service;

import com.athena.common.base.dto.PageDto;
import com.athena.common.utils.PageUtils;
import com.athena.modules.{{module}}.dto.{{Entity}}ExportExcel;
import com.athena.modules.{{module}}.dto.{{Entity}}ImportExcel;
import com.athena.modules.{{module}}.entity.{{Entity}};
import com.baomidou.mybatisplus.extension.repository.IRepository;

import java.util.List;

/**
 * {{entityLabel}}
 */
public interface {{Entity}}Service extends IRepository<{{Entity}}> {

    PageUtils queryPage({{Entity}} {{entity}}, PageDto pageDto);

    void saveEntity({{Entity}} {{entity}});

    void updateEntity({{Entity}} {{entity}});

    boolean deleteEntity(String id);

    void deleteBatch(List<String> ids);

    List<{{Entity}}ExportExcel> queryExportList({{Entity}} {{entity}});

    int import{{Entity}}s(List<{{Entity}}ImportExcel> rows);

    // 需要被其他模块下拉选择时再生成：
    // List<BaseSelect> listOptions(String keyword);
}
