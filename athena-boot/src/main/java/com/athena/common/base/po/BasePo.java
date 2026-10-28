package com.athena.common.base.po;

import com.athena.common.annotation.Dict;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * @author Mr.sun
 * @date 2021/12/14 12:45
 * @description
 */
@Data
public class BasePo implements Serializable {

    @Serial
    private static final long serialVersionUID = -8172686200993082466L;

    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    /**
     * 创建人。updateStrategy = NEVER：该字段不允许出现在任何 UPDATE 语句中，
     * 客户端即使传入也不会生效（批量赋值防护）。
     */
    @TableField(fill = FieldFill.INSERT, updateStrategy = FieldStrategy.NEVER)
    private String creator;
    /**
     * 创建时间。理由同 creator。
     */
    @TableField(fill = FieldFill.INSERT, updateStrategy = FieldStrategy.NEVER)
    private Date ctime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String editor;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date mtime;
    /**
     * 逻辑删除标记。理由同 creator；删除请走专门的删除接口。
     */
    @Dict(dicCode = "del_flag")
    @TableField(fill = FieldFill.INSERT, updateStrategy = FieldStrategy.NEVER)
    private Integer delFlag;
}
