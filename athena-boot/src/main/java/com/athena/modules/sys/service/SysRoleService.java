package com.athena.modules.sys.service;

import com.athena.common.base.dto.PageDto;
import com.athena.common.utils.PageUtils;
import com.athena.modules.sys.entity.SysRole;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;


/**
 * 角色
 *
 * @author Mr.sun
 */
public interface SysRoleService extends IService<SysRole> {

	PageUtils queryPage(SysRole role, PageDto pageDto);

	boolean deleteEntity(String id);

	void deleteBatch(List<String> ids);

	/**
	 * 查询用户创建的角色ID列表
	 */
	List<String> queryRoleIdList(String username);


	List<String> queryRoleCodeById(List<String> ids);
}
