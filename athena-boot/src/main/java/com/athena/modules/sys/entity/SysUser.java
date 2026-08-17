package com.athena.modules.sys.entity;

import com.athena.common.annotation.Dict;
import com.athena.common.base.po.BasePo;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.athena.common.validator.group.AddGroup;
import com.athena.common.validator.group.UpdateGroup;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;


import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 系统用户
 *
 * @author Mr.sun
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("sys_user")
public class SysUser extends BasePo implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 用户名
	 */
	@NotBlank(message="用户名不能为空", groups = {AddGroup.class, UpdateGroup.class})
	private String username;

	private String realname;

	/**
	 * 密码
	 */
	//@NotBlank(message="密码不能为空", groups = AddGroup.class)
	private String password;

	/**
	 * 盐
	 */
	private String salt;

	/**
	 * 邮箱
	 */
	@NotBlank(message="邮箱不能为空", groups = {AddGroup.class, UpdateGroup.class})
	@Email(message="邮箱格式不正确", groups = {AddGroup.class, UpdateGroup.class})
	private String email;

	/**
	 * 手机号
	 */
	private String mobile;

	/**
	 * 性别
	 */
	private Integer sex;

	/**
	 * 部门code
	 */
	@Dict(dicCode = "id", dicText = "dept_name", dictTable = "sys_dept")
	private String deptCode;

	private Integer userType;

	/**
	 * 角色ID列表
	 */
	@TableField(exist=false)
	private List<String> roleIdList;

}
