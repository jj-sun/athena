package com.athena.modules.sys.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.athena.common.annotation.Log;
import com.athena.common.constant.Constant;
import com.athena.common.exception.RRException;
import com.athena.common.utils.Result;
import com.athena.modules.sys.entity.SysPermission;
import com.athena.modules.sys.service.SecurityService;
import com.athena.modules.sys.service.SysPermissionService;
import com.athena.modules.sys.vo.SysMenuTree;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 系统菜单
 *
 * @author Mr.sun
 */
@RestController
@RequestMapping("/sys/permission")
public class SysPermissionController extends AbstractController {
	@Autowired
	private SysPermissionService sysPermissionService;
	@Autowired
	private SecurityService securityService;

	/**
	 * 导航菜单
	 */
	@GetMapping("/nav")
	public Result<Map<String, Object>> nav(){
		List<SysMenuTree> menuTree = sysPermissionService.getUserPermissionTree(getUsername());
		Set<String> permissions = securityService.getUserPermissions(getUsername());
		Map<String, Object> result = Maps.newHashMap();
		result.put("menuTree", menuTree);
		result.put("permissions", permissions);
		return Result.ok(result);
	}
	
	/**
	 * 所有菜单列表
	 */
	@GetMapping("/list")
	@SaCheckPermission("sys:menu:list")
	public Result<List<SysPermission>> list(){
		List<SysPermission> menuList = sysPermissionService.permissionTree();
		return Result.ok(menuList);
	}
	
	/**
	 * 选择菜单(添加、修改菜单)
	 */
	@GetMapping("/select")
	@SaCheckPermission("sys:menu:select")
	public Result<List<SysMenuTree>> select(){
		//查询列表数据
		List<SysMenuTree> menuList = sysPermissionService.treeSelect();
		
		return Result.ok(menuList);
	}
	
	/**
	 * 菜单信息
	 */
	@GetMapping("/info/{id}")
	@SaCheckPermission("sys:menu:info")
	public Result<SysPermission> info(@PathVariable("id") String id){
		SysPermission menu = sysPermissionService.getById(id);
		return Result.ok(menu);
	}
	
	/**
	 * 保存
	 */
	@Log("保存菜单")
	@PostMapping("/save")
	@SaCheckPermission("sys:menu:save")
	public Result<Object> save(@RequestBody SysPermission menu){
		//数据校验
		verifyForm(menu);
		
		sysPermissionService.save(menu);
		
		return Result.ok();
	}
	
	/**
	 * 修改
	 */
	@Log("修改菜单")
	@PutMapping("/update")
	@SaCheckPermission("sys:menu:update")
	public Result<Object> update(@RequestBody SysPermission menu){
		//数据校验
		verifyForm(menu);
				
		sysPermissionService.updateById(menu);
		
		return Result.ok();
	}
	
	/**
	 * 删除
	 */
	@Log("删除菜单")
	@DeleteMapping("/delete")
	@SaCheckPermission("sys:menu:delete")
	public Result<Object> delete(@RequestParam(name = "id") String id){

		//判断是否有子菜单或按钮
		List<SysPermission> menuList = sysPermissionService.queryListParentId(id);
		if(menuList.size() > 0){
			return Result.error("请先删除子菜单或按钮");
		}

		sysPermissionService.delete(id);

		return Result.ok();
	}
	
	/**
	 * 验证参数是否正确
	 */
	private void verifyForm(SysPermission menu){
		if(StringUtils.isBlank(menu.getName())){
			throw new RRException("菜单名称不能为空");
		}
		
		if(org.apache.commons.lang3.StringUtils.isBlank(menu.getParentId())){
			menu.setParentId(Constant.TREE_ROOT);
		}
		
		//菜单
		if(menu.getType() == Constant.MenuType.MENU.getValue()){
			if(StringUtils.isBlank(menu.getUrl())){
				throw new RRException("菜单URL不能为空");
			}
		}
		
		//上级菜单类型
		int parentType = Constant.MenuType.CATALOG.getValue();
		if(!menu.getParentId().equals("0")){
			SysPermission parentMenu = sysPermissionService.getById(menu.getParentId());
			parentType = parentMenu.getType();
		}
		
		//目录、菜单
		if(menu.getType() == Constant.MenuType.CATALOG.getValue() ||
				menu.getType() == Constant.MenuType.MENU.getValue()){
			if(parentType != Constant.MenuType.CATALOG.getValue()){
				throw new RRException("上级菜单只能为目录类型");
			}
			return ;
		}
		
		//按钮
		if(menu.getType() == Constant.MenuType.BUTTON.getValue()){
			if(parentType != Constant.MenuType.MENU.getValue()){
				throw new RRException("上级菜单只能为菜单类型");
			}
			return ;
		}
	}
}
