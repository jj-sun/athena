package com.athena.modules.sys.dto;

import lombok.Data;
import org.apache.fesod.sheet.annotation.ExcelProperty;
import org.apache.fesod.sheet.annotation.format.DateTimeFormat;
import org.apache.fesod.sheet.annotation.write.style.ColumnWidth;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 用户导出 Excel 行。
 *
 * <p>刻意不包含密码字段：导出的用户清单可能被下载、转发或落盘，
 * 不应携带任何可用于离线爆破的密码哈希。
 *
 * @author Mr.sun
 */
@Data
public class SysUserExportExcel implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@ColumnWidth(20)
	@ExcelProperty("用户名")
	private String username;

	@ColumnWidth(15)
	@ExcelProperty("姓名")
	private String realname;

	@ColumnWidth(25)
	@ExcelProperty("邮箱")
	private String email;

	@ColumnWidth(18)
	@ExcelProperty("手机号")
	private String mobile;

	@ColumnWidth(10)
	@ExcelProperty("性别")
	private String sex;

	@ColumnWidth(20)
	@ExcelProperty("部门")
	private String deptName;

	@ColumnWidth(24)
	@ExcelProperty("角色")
	private String roleNames;

	@ColumnWidth(10)
	@ExcelProperty("状态")
	private String status;

	@ColumnWidth(22)
	@DateTimeFormat("yyyy-MM-dd HH:mm:ss")
	@ExcelProperty("创建时间")
	private Date ctime;
}
