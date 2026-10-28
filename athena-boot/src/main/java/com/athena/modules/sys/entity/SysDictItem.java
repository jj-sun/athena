package com.athena.modules.sys.entity;

import com.athena.common.base.po.BasePo;
import com.athena.common.validator.group.AddGroup;
import com.athena.common.validator.group.UpdateGroup;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author Mr.sun
 * @date 2022/1/19 19:53
 * @description
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysDictItem extends BasePo implements Serializable {
    @Serial
    private static final long serialVersionUID = 913097975486211620L;

    @NotBlank(message = "字典ID不能为空", groups = {AddGroup.class, UpdateGroup.class})
    private String dictId;

    @NotBlank(message = "字典项标签不能为空", groups = {AddGroup.class, UpdateGroup.class})
    private String itemText;

    @NotBlank(message = "字典项值不能为空", groups = {AddGroup.class, UpdateGroup.class})
    private String itemValue;

    private String description;

    /**
     * 排序号（对应 sort_order 列）。前端添加字典项时会传入，原先因实体缺少该字段而被静默丢弃。
     */
    private Integer sortOrder;

}
