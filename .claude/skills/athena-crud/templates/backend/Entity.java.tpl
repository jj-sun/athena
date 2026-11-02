package com.athena.modules.{{module}}.entity;

import com.athena.common.annotation.Dict;
import com.athena.common.base.po.BasePo;
import com.athena.common.validator.group.AddGroup;
import com.athena.common.validator.group.UpdateGroup;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * {{entityLabel}}
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("{{tableName}}")
public class {{Entity}} extends BasePo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    // <GENERATE: 每个业务字段一段；不要声明 id/creator/ctime/editor/mtime/delFlag>

    // 必填字符串
    @NotBlank(message = "名称不能为空", groups = {AddGroup.class, UpdateGroup.class})
    @Size(max = 128, message = "名称不能超过128个字符", groups = {AddGroup.class, UpdateGroup.class})
    private String name;

    // 可选字符串
    private String remark;

    // 必填整数/状态
    @NotNull(message = "状态不能为空", groups = {AddGroup.class, UpdateGroup.class})
    private Integer status;

    // 必填金额
    @NotNull(message = "金额不能为空", groups = {AddGroup.class, UpdateGroup.class})
    @DecimalMin(value = "0", message = "金额不能小于0", groups = {AddGroup.class, UpdateGroup.class})
    private BigDecimal amount;

    // 字典码（值到标签的展示翻译）
    @Dict(dicCode = "{{dictCode}}")
    private Integer type;

    // 外键（表字典）；目标表必须加入 athena.dict.allowed-tables
    @Dict(dicCode = "id", dicText = "{{textColumn}}", dictTable = "{{targetTable}}")
    private String categoryId;

    // 日期（date）
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date businessDate;

    // 日期时间（datetime）沿用项目默认 yyyy-MM-dd HH:mm:ss
    private Date happenedAt;

    // 仅 query: between 时生成两个瞬态字段
    @TableField(exist = false)
    private Date businessDateStart;

    @TableField(exist = false)
    private Date businessDateEnd;
}
