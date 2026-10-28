package com.athena.modules.sys.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.athena.common.annotation.Log;
import com.athena.common.base.dto.PageDto;
import com.athena.common.base.select.BaseSelect;
import com.athena.common.utils.PageUtils;
import com.athena.common.utils.Query;
import com.athena.common.utils.Result;
import com.athena.common.validator.ValidatorUtils;
import com.athena.modules.sys.entity.SysRole;
import com.athena.modules.sys.entity.SysUser;
import com.athena.modules.sys.service.PrivilegeGuardService;
import com.athena.modules.sys.service.SysRoleService;
import com.athena.modules.sys.service.SysUserRoleService;
import com.athena.modules.sys.service.SysUserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 角色管理
 *
 * @author Mr.sun
 */
@RestController
@RequestMapping("/sys/role")
public class SysRoleController extends AbstractController {
	@Autowired
	private SysRoleService sysRoleService;
	@Autowired
	private SysUserRoleService sysUserRoleService;
	@Autowired
	private SysUserService sysUserService;
	@Autowired
	private PrivilegeGuardService privilegeGuard;

	/**
	 * 角色列表
	 */
	@GetMapping("/list")
	@SaCheckPermission("sys:role:list")
	public Result<PageUtils> list(SysRole role, PageDto pageDto){

		PageUtils page = sysRoleService.queryPage(role, pageDto);

		return Result.ok(page);
	}
	
	/**
	 * 角色列表
	 */
	@GetMapping("/select")
	@SaCheckPermission("sys:role:select")
	public Result<List<BaseSelect>> select(){
		List<SysRole> list = sysRoleService.list();
		List<BaseSelect> selectList = Lists.newArrayList();
		if(!CollectionUtils.isEmpty(list)) {
			list.forEach(role -> selectList.add(new BaseSelect(role.getRoleName(), role.getId())));
		}
		return Result.ok(selectList);
	}
	
	/**
	 * 角色信息
	 */
	@GetMapping("/info/{roleId}")
	@SaCheckPermission("sys:role:info")
	public Result<SysRole> info(@PathVariable("roleId") String roleId){
		SysRole role = sysRoleService.getById(roleId);
		
		return Result.ok(role);
	}
	
	/**
	 * 保存角色
	 */
	@Log("保存角色")
	@PostMapping("/save")
	@SaCheckPermission("sys:role:save")
	public Result<Object> save(@RequestBody SysRole role){
		ValidatorUtils.validateEntity(role);
		privilegeGuard.checkRoleCodeAssignable(role.getRoleCode());

		sysRoleService.save(role);
		
		return Result.ok();
	}
	
	/**
	 * 修改角色
	 */
	@Log("修改角色")
	@PutMapping("/update")
	@SaCheckPermission("sys:role:update")
	public Result<Object> update(@RequestBody SysRole role){
		ValidatorUtils.validateEntity(role);
		privilegeGuard.checkRoleManageable(role.getId());
		privilegeGuard.checkRoleCodeAssignable(role.getRoleCode());

		sysRoleService.updateById(role);
		
		return Result.ok();
	}
	
	/**
	 * 删除角色
	 */
	@Log("删除角色")
	@DeleteMapping("/delete")
	@SaCheckPermission("sys:role:delete")
	public Result<Object> delete(@RequestParam(name = "id") String id){
		privilegeGuard.checkRoleManageable(id);
		sysRoleService.deleteEntity(id);
		return Result.ok();
	}

	@Log("批量删除角色")
	@DeleteMapping("/deleteBatch")
	@SaCheckPermission("sys:role:delete")
	public Result<Object> deleteBatch(@RequestParam(name = "ids") String ids) {
		List<String> idList = Arrays.asList(ids.split(","));
		for (String id : idList) {
			privilegeGuard.checkRoleManageable(id);
		}
		sysRoleService.deleteBatch(idList);
		return Result.ok();
	}

	/**
	 * 获取角色关联的用户列表
	 */
	@GetMapping("/listUsers")
	@SaCheckPermission("sys:role:list")
	public Result<PageUtils> listUsers(@RequestParam("roleId") String roleId, PageDto pageDto){
		List<String> userIds = sysUserRoleService.queryUserIdList(roleId);
		if (userIds == null || userIds.isEmpty()) {
			return Result.ok(new PageUtils(new ArrayList<>(), 0,
					pageDto.getPageSize() != null ? pageDto.getPageSize().intValue() : 10,
					pageDto.getPage() != null ? pageDto.getPage().intValue() : 1));
		}
		IPage<SysUser> page = sysUserService.page(
				new Query<SysUser>().getPage(pageDto, "ctime", false),
				new LambdaQueryWrapper<SysUser>().in(SysUser::getId, userIds)
		);
		return Result.ok(new PageUtils(page));
	}

	/**
	 * 添加用户到角色
	 */
	@Log("添加用户到角色")
	@PostMapping("/addUser")
	@SaCheckPermission("sys:role:update")
	public Result<Object> addUser(@RequestBody Map<String, Object> params){
		String roleId = (String) params.get("roleId");
		@SuppressWarnings("unchecked")
		List<String> userIdList = (List<String>) params.get("userIdList");
		if (roleId == null || userIdList == null || userIdList.isEmpty()) {
			return Result.error("参数不完整");
		}
		privilegeGuard.checkCanAddUsers(roleId, userIdList);
		for (String userId : userIdList) {
			sysUserRoleService.saveUserRole(userId, roleId);
		}
		return Result.ok();
	}

	/**
	 * 从角色移除用户
	 */
	@Log("从角色移除用户")
	@DeleteMapping("/removeUser")
	@SaCheckPermission("sys:role:update")
	public Result<Object> removeUser(@RequestParam("roleId") String roleId, @RequestParam("userId") String userId){
		privilegeGuard.checkCanRemoveUser(roleId);
		sysUserRoleService.deleteUserRole(userId, roleId);
		return Result.ok();
	}
}
