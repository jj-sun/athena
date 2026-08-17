package com.athena.modules.sys.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 角色与菜单对应关系
 *
 * @author Mr.sun
 */
@Data
@TableName("sys_role_permission")
public class SysRolePermission implements Serializable {
	@Serial
	private static final long serialVersionUID = 1L;

	@TableId
	private String id;

	/**
	 * 角色ID
	 */
	private String roleId;

	/**
	 * 菜单ID
	 */
	private String permissionId;
	
}
