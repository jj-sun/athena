package com.athena.modules.sys.service.impl;


import com.athena.common.constant.Constant;
import com.athena.common.utils.SecurityUtil;
import com.athena.common.utils.TreeUtils;
import com.athena.modules.sys.entity.SysPermission;
import com.athena.modules.sys.entity.SysRolePermission;
import com.athena.modules.sys.mapper.SysPermissionMapper;
import com.athena.modules.sys.service.SysPermissionService;
import com.athena.modules.sys.service.SysRolePermissionService;
import com.athena.modules.sys.vo.SysMenuTree;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


/**
 * @author Mr.sun
 */
@Service
public class SysPermissionServiceImpl extends ServiceImpl<SysPermissionMapper, SysPermission> implements SysPermissionService {

	@Autowired
	private SysRolePermissionService sysRolePermissionService;

	@Override
	public List<SysPermission> permissionTree() {
		List<SysPermission> sysPermissionEntities = this.list();
		if(!CollectionUtils.isEmpty(sysPermissionEntities)) {
			List<SysPermission> roots = sysPermissionEntities.stream().filter(root -> root.getParentId().equals(Constant.TREE_ROOT)).collect(Collectors.toList());
			buildTree(roots, sysPermissionEntities);
			return roots;
		}
		return sysPermissionEntities;
	}
	private void buildTree(List<SysPermission> roots, List<SysPermission> all) {
		if(CollectionUtils.isEmpty(roots)) {
			return;
		}
		roots.forEach(root -> {
			List<SysPermission> childrenList = all.stream().filter(tree -> tree.getParentId().equals(root.getId())).collect(Collectors.toList());
			if(!CollectionUtils.isEmpty(childrenList)) {
				root.setChildren(childrenList);
				buildTree(childrenList, all);
			}
		});
	}

	@Override
	public List<SysPermission> queryListParentId(String parentId, List<String> menuIdList) {
		List<SysPermission> menuList = queryListParentId(parentId);
		if(menuIdList == null){
			return menuList;
		}
		
		List<SysPermission> userMenuList = new ArrayList<>();
		for(SysPermission menu : menuList){
			if(menuIdList.contains(menu.getId())){
				userMenuList.add(menu);
			}
		}
		return userMenuList;
	}

	@Override
	public List<SysPermission> queryListParentId(String parentId) {
		return this.list(new LambdaQueryWrapper<SysPermission>().eq(SysPermission::getParentId, parentId).orderByAsc(SysPermission::getOrderNum));
	}

	@Override
	public List<SysMenuTree> treeSelect() {
		List<SysPermission> sysPermissionEntities = this.list();
		return this.getTree(sysPermissionEntities);
	}

	@Override
	public List<SysMenuTree> getUserPermissionTree(String username) {
		List<SysPermission> sysPermissionEntities;

		//系统管理员，拥有最高权限
		if(username.equals(Constant.SUPER_ADMIN)){
			sysPermissionEntities = this.list(new LambdaQueryWrapper<SysPermission>().ne(SysPermission::getType, Constant.PermissionType.BUTTON.getValue()));
		} else {
			//用户菜单列表
			List<String> menuIdList = this.getBaseMapper().queryAllMenuId(SecurityUtil.getUserId());
			sysPermissionEntities = this.list(new LambdaQueryWrapper<SysPermission>().in(SysPermission::getId, menuIdList).ne(SysPermission::getType, Constant.PermissionType.BUTTON.getValue()));
		}
		return this.getTree(sysPermissionEntities);
	}

	private List<SysMenuTree> getTree(List<SysPermission> sysPermissionEntities) {
		List<SysMenuTree> menuTreeList = Lists.newArrayList();
		if(!CollectionUtils.isEmpty(sysPermissionEntities)) {
			sysPermissionEntities.forEach(sysPermissionEntity -> menuTreeList.add(new SysMenuTree(sysPermissionEntity)));
			List<SysMenuTree> root = menuTreeList.stream().filter(sysMenuTree -> sysMenuTree.getParentKey().equals(Constant.TREE_ROOT)).collect(Collectors.toList());
			if(!CollectionUtils.isEmpty(root)) {
				TreeUtils.buildTree(root, menuTreeList);
				return root;
			}
		}
		return null;
	}

	@Override
	public void delete(String permissionId){
		//删除菜单
		this.removeById(permissionId);
		//删除菜单与角色关联
		sysRolePermissionService.remove(new LambdaQueryWrapper<SysRolePermission>().eq(SysRolePermission::getPermissionId, permissionId));
	}

}
