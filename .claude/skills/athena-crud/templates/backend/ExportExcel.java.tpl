package com.athena.modules.{{module}}.dto;

import lombok.Data;
import org.apache.fesod.sheet.annotation.ExcelProperty;
import org.apache.fesod.sheet.annotation.format.DateTimeFormat;
import org.apache.fesod.sheet.annotation.write.style.ColumnWidth;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * {{entityLabel}}导出 Excel 行。
 *
 * <p>只包含 export: true 的字段；字典/外键/状态导出为中文文本；
 * 敏感字段与密码类字段不得出现在此 DTO 中。
 */
@Data
public class {{Entity}}ExportExcel implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    // <GENERATE: 每个 export: true 的字段一段>

    @ColumnWidth(20)
    @ExcelProperty("名称")
    private String name;

    @ColumnWidth(12)
    @ExcelProperty("状态")
    private String status;

    @ColumnWidth(18)
    @ExcelProperty("分类")
    private String categoryName;

    @ColumnWidth(15)
    @DateTimeFormat("yyyy-MM-dd")
    @ExcelProperty("业务日期")
    private Date businessDate;

    @ColumnWidth(15)
    @ExcelProperty("金额")
    private BigDecimal amount;

    @ColumnWidth(22)
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    @ExcelProperty("创建时间")
    private Date ctime;
}
