

package com.athena.modules.sys.service;


import com.athena.modules.sys.entity.SysPermission;
import com.athena.modules.sys.vo.SysMenuTree;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;


/**
 * 菜单管理
 *
 * @author Mr.sun
 */
public interface SysPermissionService extends IService<SysPermission> {

	List<SysPermission> permissionTree();

	/**
	 * 根据父菜单，查询子菜单
	 * @param parentId 父菜单ID
	 * @param menuIdList  用户菜单ID
	 */
	List<SysPermission> queryListParentId(String parentId, List<String> menuIdList);

	/**
	 * 根据父菜单，查询子菜单
	 * @param parentId 父菜单ID
	 */
	List<SysPermission> queryListParentId(String parentId);
	
	/**
	 * 获取菜单
	 */
	List<SysMenuTree> treeSelect();
	
	/**
	 * 获取用户菜单列表
	 */
	List<SysMenuTree> getUserPermissionTree(String username);

	/**
	 * 删除
	 */
	void delete(String permissionId);

	/**
	 * 所有角色强制拥有的菜单 ID（仪表盘目录 + 概览首页，不含统计报表等其他页面）。
	 * 即使角色没有分配任何其他菜单，登录后也必须能进入仪表盘首页。
	 */
	List<String> getMandatoryMenuIds();

}
