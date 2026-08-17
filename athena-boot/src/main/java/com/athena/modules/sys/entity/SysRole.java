package com.athena.modules.sys.entity;

import com.athena.common.base.po.BasePo;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 角色
 *
 * @author Mr.sun
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("sys_role")
public class SysRole extends BasePo implements Serializable {
	@Serial
	private static final long serialVersionUID = 1L;


	/**
	 * 角色名称
	 */
	@NotBlank(message="角色名称不能为空")
	private String roleName;

	/**
	 * 角色编码
	 */
	private String roleCode;

	/**
	 * 备注
	 */
	private String remark;

	@TableField(exist=false)
	private List<String> menuIdList;

}
