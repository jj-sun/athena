package com.athena.modules.{{module}}.dto;

import lombok.Data;
import org.apache.fesod.sheet.annotation.ExcelProperty;
import org.apache.fesod.sheet.annotation.write.style.ColumnWidth;

import java.io.Serial;
import java.io.Serializable;

/**
 * {{entityLabel}}批量导入行。
 *
 * <p>所有列统一用 String 接收，服务层逐行解析并返回带 Excel 行号的错误，
 * 避免类型转换异常直接中断整批导入。敏感字段不在此 DTO 中出现。
 */
@Data
public class {{Entity}}ImportExcel implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    // <GENERATE: 每个 import != never 的字段一段；@ExcelProperty 文案必须与前端模板说明一致>

    @ColumnWidth(20)
    @ExcelProperty("名称")
    private String name;

    @ColumnWidth(12)
    @ExcelProperty("状态")
    private String status;

    @ColumnWidth(18)
    @ExcelProperty("分类ID")
    private String categoryId;

    @ColumnWidth(15)
    @ExcelProperty("业务日期")
    private String businessDate;

    @ColumnWidth(15)
    @ExcelProperty("金额")
    private String amount;
}
