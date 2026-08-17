package com.athena.modules.sys.service;

import com.athena.common.base.dto.PageDto;
import com.athena.common.utils.PageUtils;
import com.athena.modules.sys.entity.SysUser;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;


/**
 * 系统用户
 *
 * @author Mr.sun
 */
public interface SysUserService extends IService<SysUser> {

	PageUtils queryPage(SysUser user, PageDto pageDto);

	/**
	 * 查询用户的所有权限
	 * @param username  用户username
	 */
	List<String> queryAllPerms(String username);

	/**
	 * 根据用户名，查询系统用户
	 */
	SysUser queryByUserName(String username);

	/**
	 * 保存用户
	 */
	void saveUser(SysUser user);
	
	/**
	 * 修改用户
	 */
	void update(SysUser user);

	/**
	 * 删除用户，同时删除用户和角色的关系
	 * @param id
	 * @return
	 */
	boolean deleteEntity(String id);

	/**
	 * 批量删除用户,同时删除用户和角色的关系
	 */
	void deleteBatch(List<String> ids);

	/**
	 * 修改密码
	 * @param username       用户ID
	 * @param newPassword  新密码
	 */
	boolean updatePassword(String username, String newPassword);

	/**
	 * 管理员重置用户密码
	 * @param id           用户ID
	 * @param newPassword  新密码
	 */
	boolean resetPassword(String id, String newPassword);

}
