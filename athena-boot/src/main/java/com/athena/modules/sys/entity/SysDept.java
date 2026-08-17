package com.athena.modules.sys.entity;

import com.athena.common.base.po.BasePo;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * @author Mr.sun
 * @date 2022/1/24 8:58
 * @description
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SysDept extends BasePo implements Serializable {
    @Serial
    private static final long serialVersionUID = -1456049284379334422L;
    private String parentId;
    private String deptName;
    private String deptCode;
    private String description;
    private Integer sortOrder;

    @TableField(exist = false)
    private List<SysDept> children;
}
