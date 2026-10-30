package com.athena.modules.sys.service;

import com.athena.common.base.dto.PageDto;
import com.athena.common.utils.PageUtils;
import com.athena.modules.sys.dto.SysUserExportExcel;
import com.athena.modules.sys.dto.SysUserImportExcel;
import com.athena.modules.sys.entity.SysUser;
import com.baomidou.mybatisplus.extension.repository.IRepository;

import java.util.List;


/**
 * 系统用户
 *
 * @author Mr.sun
 */
public interface SysUserService extends IRepository<SysUser> {

	PageUtils queryPage(SysUser user, PageDto pageDto);

	/**
	 * 根据用户名，查询系统用户
	 */
	SysUser queryByUserName(String username);

	/**
	 * 判断用户名是否已存在。
	 *
	 * @param username  用户名
	 * @param excludeId 需要排除的用户 id（编辑时传当前用户 id，新增时传 null）
	 */
	boolean existsByUsername(String username, String excludeId);

	/**
	 * 判断手机号是否已存在。
	 *
	 * @param mobile    手机号
	 * @param excludeId 需要排除的用户 id（编辑时传当前用户 id，新增时传 null）
	 */
	boolean existsByMobile(String mobile, String excludeId);

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

	/**
	 * 按列表查询条件导出用户（不分页），不包含密码。
	 *
	 * @param user 查询条件，与 {@link #queryPage(SysUser, PageDto)} 保持一致
	 */
	List<SysUserExportExcel> queryExportList(SysUser user);

	/**
	 * 批量导入用户。任一数据行校验失败都会整批回滚，并返回带行号的错误信息。
	 *
	 * @param rows Excel 数据行
	 * @return 成功导入的用户数
	 */
	int importUsers(List<SysUserImportExcel> rows);

}
