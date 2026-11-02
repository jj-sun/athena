package com.athena.modules.sys.dto;

import lombok.Data;
import org.apache.fesod.sheet.annotation.ExcelProperty;
import org.apache.fesod.sheet.annotation.write.style.ColumnWidth;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户批量导入 Excel 行。
 *
 * <p>密码列只出现在导入模板中；导出使用 {@link SysUserExportExcel}，不包含密码。
 *
 * @author Mr.sun
 */
@Data
public class SysUserImportExcel implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@ColumnWidth(20)
	@ExcelProperty("用户名")
	private String username;

	@ColumnWidth(15)
	@ExcelProperty("姓名")
	private String realname;

	@ColumnWidth(20)
	@ExcelProperty("密码")
	private String password;

	@ColumnWidth(25)
	@ExcelProperty("邮箱")
	private String email;

	@ColumnWidth(18)
	@ExcelProperty("手机号")
	private String mobile;

	@ColumnWidth(10)
	@ExcelProperty("性别")
	private String sex;

	/**
	 * 部门业务编码，对应 sys_dept.dept_code（不是 sys_user.dept_id 中保存的部门 id）。
	 */
	@ColumnWidth(18)
	@ExcelProperty("部门编码")
	private String deptCode;

	/**
	 * 角色业务编码，多个使用逗号、顿号或分号分隔。
	 */
	@ColumnWidth(24)
	@ExcelProperty("角色编码")
	private String roleCodes;
}
