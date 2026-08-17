package com.athena.modules.sys.entity;

import com.athena.common.base.po.BasePo;
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
public class SysDict extends BasePo implements Serializable {
    @Serial
    private static final long serialVersionUID = -1541013999225745949L;

    private String dictName;
    private String dictCode;
    private Integer type;
    private String description;

}
