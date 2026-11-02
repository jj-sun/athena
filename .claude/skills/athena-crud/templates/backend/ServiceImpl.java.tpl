package com.athena.modules.{{module}}.service.impl;

import com.athena.common.base.dto.PageDto;
import com.athena.common.excel.ExcelUtils;
import com.athena.common.exception.RRException;
import com.athena.common.utils.PageUtils;
import com.athena.common.utils.Query;
import com.athena.common.validator.Assert;
import com.athena.common.validator.ValidatorUtils;
import com.athena.common.validator.group.AddGroup;
import com.athena.modules.{{module}}.dto.{{Entity}}ExportExcel;
import com.athena.modules.{{module}}.dto.{{Entity}}ImportExcel;
import com.athena.modules.{{module}}.entity.{{Entity}};
import com.athena.modules.{{module}}.mapper.{{Entity}}Mapper;
import com.athena.modules.{{module}}.service.{{Entity}}Service;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.spring.repository.CrudRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * {{entityLabel}}
 */
@Service
public class {{Entity}}ServiceImpl extends CrudRepository<{{Entity}}Mapper, {{Entity}}>
        implements {{Entity}}Service {

    private static final long MAX_EXPORT_ROWS = 50000L;

    private static final int MAX_IMPORT_ROWS = 5000;

    @Override
    public PageUtils queryPage({{Entity}} {{entity}}, PageDto pageDto) {
        IPage<{{Entity}}> page = this.page(
                new Query<{{Entity}}>().getPage(pageDto, "ctime", false),
                buildQueryWrapper({{entity}})
        );
        return new PageUtils(page);
    }

    /**
     * 分页与导出共用查询条件，避免两处条件漂移。
     */
    private LambdaQueryWrapper<{{Entity}}> buildQueryWrapper({{Entity}} {{entity}}) {
        LambdaQueryWrapper<{{Entity}}> wrapper = new LambdaQueryWrapper<>();

        // <GENERATE: 每个 query != none 的字段一条条件>
        // string + like
        wrapper.like(StringUtils.isNotBlank({{entity}}.getName()),
                {{Entity}}::getName, {{entity}}.getName());
        // status / dict / foreignKey + eq
        wrapper.eq({{entity}}.getStatus() != null,
                {{Entity}}::getStatus, {{entity}}.getStatus());
        // date/datetime/decimal + between（瞬态字段）
        wrapper.ge({{entity}}.getBusinessDateStart() != null,
                {{Entity}}::getBusinessDate, {{entity}}.getBusinessDateStart());
        wrapper.le({{entity}}.getBusinessDateEnd() != null,
                {{Entity}}::getBusinessDate, {{entity}}.getBusinessDateEnd());

        return wrapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveEntity({{Entity}} {{entity}}) {
        normalize({{entity}});
        checkUnique({{entity}}, null);
        this.save({{entity}});
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateEntity({{Entity}} {{entity}}) {
        Assert.isBlank({{entity}}.getId(), "{{entityLabel}}ID不能为空");
        if (this.getById({{entity}}.getId()) == null) {
            throw new RRException("{{entityLabel}}不存在");
        }
        normalize({{entity}});
        checkUnique({{entity}}, {{entity}}.getId());
        this.updateById({{entity}});
    }

    @Override
    public boolean deleteEntity(String id) {
        return this.removeById(id);
    }

    @Override
    public void deleteBatch(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        this.removeByIds(ids);
    }

    @Override
    public List<{{Entity}}ExportExcel> queryExportList({{Entity}} {{entity}}) {
        LambdaQueryWrapper<{{Entity}}> wrapper = buildQueryWrapper({{entity}});
        long total = this.count(wrapper);
        if (total > MAX_EXPORT_ROWS) {
            throw new RRException("导出数据量超过 " + MAX_EXPORT_ROWS + " 条，请缩小筛选范围");
        }

        List<{{Entity}}> list = this.list(wrapper.orderByDesc({{Entity}}::getCtime));
        List<{{Entity}}ExportExcel> result = new ArrayList<>(list.size());
        for ({{Entity}} item : list) {
            {{Entity}}ExportExcel row = new {{Entity}}ExportExcel();
            // <GENERATE: 每个 export: true 的字段一行；文本必须经过 safeExcelText>
            row.setName(safeExcelText(item.getName()));
            row.setStatus(statusText(item.getStatus()));
            row.setCategoryName(safeExcelText(resolveCategoryName(item.getCategoryId())));
            row.setBusinessDate(item.getBusinessDate());
            row.setAmount(item.getAmount());
            row.setCtime(item.getCtime());
            result.add(row);
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int import{{Entity}}s(List<{{Entity}}ImportExcel> rows) {
        if (rows == null || rows.isEmpty()) {
            throw new RRException("Excel 中没有可导入的数据");
        }
        long dataRows = rows.stream().filter(row -> !ExcelUtils.isBlankRow(row)).count();
        if (dataRows > MAX_IMPORT_ROWS) {
            throw new RRException("单次最多导入 " + MAX_IMPORT_ROWS + " 条{{entityLabel}}");
        }

        List<String> errors = new ArrayList<>();
        List<{{Entity}}> entities = new ArrayList<>(rows.size());
        for (int i = 0; i < rows.size(); i++) {
            // Excel 第 1 行为表头，数据从第 2 行开始
            int rowNum = i + 2;
            {{Entity}}ImportExcel row = rows.get(i);
            if (ExcelUtils.isBlankRow(row)) {
                continue;
            }

            {{Entity}} entity = new {{Entity}}();
            try {
                // <GENERATE: 每个 import != never 的字段一段解析>
                // 字符串
                entity.setName(StringUtils.trimToNull(row.getName()));
                // 整数 / 状态
                entity.setStatus(parseStatus(row.getStatus(), rowNum));
                // 金额
                entity.setAmount(parseDecimal(row.getAmount(), "金额", rowNum));
                // 日期
                entity.setBusinessDate(parseDate(row.getBusinessDate(), "yyyy-MM-dd", "业务日期", rowNum));
                // 外键：按业务编码或 id 解析
                entity.setCategoryId(resolveCategoryId(row.getCategoryId(), rowNum));

                // <GENERATE: 对每个 unique 字段做数据库存在性预检查，保证错误带行号>
                // if (existsByName(entity.getName(), null)) {
                //     errors.add("第 " + rowNum + " 行：名称已存在");
                //     continue;
                // }

                // 复用实体上的校验规则
                ValidatorUtils.validateEntity(entity, AddGroup.class);
            } catch (RRException e) {
                errors.add("第 " + rowNum + " 行：" + StringUtils.defaultString(e.getMsg()));
                continue;
            }
            entities.add(entity);
        }

        if (!errors.isEmpty()) {
            int shown = Math.min(errors.size(), 20);
            StringBuilder message = new StringBuilder(String.join("；", errors.subList(0, shown)));
            if (errors.size() > shown) {
                message.append("；等共 ").append(errors.size()).append(" 条错误");
            }
            throw new RRException("导入失败：" + message);
        }
        if (entities.isEmpty()) {
            throw new RRException("Excel 中没有可导入的数据");
        }

        try {
            // 全部行校验通过后再写库，外层事务保证要么全部成功要么全部回滚
            for ({{Entity}} entity : entities) {
                saveEntity(entity);
            }
        } catch (DuplicateKeyException e) {
            throw new RRException("导入失败：{{entityLabel}}已存在（可能被并发创建），请重试");
        }
        return entities.size();
    }

    private void normalize({{Entity}} {{entity}}) {
        // <GENERATE: 所有 String 字段 trimToNull>
        {{entity}}.setName(StringUtils.trimToNull({{entity}}.getName()));
    }

    private void checkUnique({{Entity}} {{entity}}, String excludeId) {
        // <GENERATE: 每个 unique: true 的字段一段；没有唯一字段时删除此方法及调用>
        LambdaQueryWrapper<{{Entity}}> wrapper = new LambdaQueryWrapper<{{Entity}}>()
                .eq({{Entity}}::getName, {{entity}}.getName());
        if (StringUtils.isNotBlank(excludeId)) {
            wrapper.ne({{Entity}}::getId, excludeId);
        }
        if (this.count(wrapper) > 0) {
            throw new RRException("名称已存在");
        }
    }

    private Integer parseInteger(String value, String label, int rowNum) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        try {
            return Integer.valueOf(value.trim());
        } catch (NumberFormatException e) {
            throw new RRException("第 " + rowNum + " 行：" + label + "格式不正确");
        }
    }

    private BigDecimal parseDecimal(String value, String label, int rowNum) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException e) {
            throw new RRException("第 " + rowNum + " 行：" + label + "格式不正确");
        }
    }

    private Date parseDate(String value, String pattern, String label, int rowNum) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        try {
            return new SimpleDateFormat(pattern).parse(value.trim());
        } catch (ParseException e) {
            throw new RRException("第 " + rowNum + " 行：" + label + "格式应为 " + pattern);
        }
    }

    /**
     * <GENERATE: 按字段规格的 options 生成；上面的状态值只是示例>
     */
    private Integer parseStatus(String value, int rowNum) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        String text = value.trim();
        if ("1".equals(text) || "启用".equals(text)) {
            return 1;
        }
        if ("0".equals(text) || "停用".equals(text)) {
            return 0;
        }
        throw new RRException("第 " + rowNum + " 行：状态格式不正确");
    }

    private String statusText(Integer status) {
        if (Integer.valueOf(1).equals(status)) {
            return "启用";
        }
        if (Integer.valueOf(0).equals(status)) {
            return "停用";
        }
        return status == null ? "" : String.valueOf(status);
    }

    /**
     * <GENERATE: 按目标表/目标模块查询名称；没有外键字段时删除此方法及调用>
     */
    private String resolveCategoryName(String categoryId) {
        if (StringUtils.isBlank(categoryId)) {
            return "";
        }
        return categoryId;
    }

    /**
     * <GENERATE: 按业务编码或 id 解析外键；找不到时抛带行号的 RRException>
     */
    private String resolveCategoryId(String value, int rowNum) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        return value.trim();
    }

    /**
     * 防止 Excel 公式注入：以 = + - @ 或制表符/回车开头的文本会被 Excel 当作公式执行。
     */
    private String safeExcelText(String value) {
        if (StringUtils.isEmpty(value)) {
            return value;
        }
        char first = value.charAt(0);
        if (first == '=' || first == '+' || first == '-' || first == '@'
                || first == '\t' || first == '\r') {
            return "'" + value;
        }
        return value;
    }
}
