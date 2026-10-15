package com.athena.modules.sys.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.secure.BCrypt;
import cn.dev33.satoken.secure.SaSecureUtil;
import com.athena.common.annotation.Log;
import com.athena.common.base.dto.PageDto;
import com.athena.common.utils.PageUtils;
import com.athena.common.utils.PasswordUtils;
import com.athena.common.utils.Result;
import com.athena.common.validator.Assert;
import com.athena.common.validator.ValidatorUtils;
import com.athena.common.validator.group.AddGroup;
import com.athena.common.validator.group.UpdateGroup;
import com.athena.modules.sys.entity.SysUser;
import com.athena.modules.sys.form.PasswordForm;
import com.athena.modules.sys.service.SysUserRoleService;
import com.athena.modules.sys.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 系统用户
 *
 * @author Mr.sun
 */
@RestController
@RequestMapping("/sys/user")
public class SysUserController extends AbstractController {
	@Autowired
	private SysUserService sysUserService;
	@Autowired
	private SysUserRoleService sysUserRoleService;

	@Value("${athena.rsa.private-key}")
	private String rsaPrivateKey;


	/**
	 * 所有用户列表
	 */
	@GetMapping("/list")
	@SaCheckPermission("sys:user:list")
	public Result<PageUtils> list(SysUser user, PageDto pageDto){
		PageUtils page = sysUserService.queryPage(user, pageDto);
		return Result.ok(page);
	}
	
	/**
	 * 获取登录的用户信息
	 */
	@GetMapping("/info")
	public Result<SysUser> info(){

		return Result.ok(null);
	}
	
	/**
	 * 修改登录用户密码
	 */
	@Log("修改密码")
	@PostMapping("/password")
	public Result<Object> password(@RequestBody PasswordForm form){
		Assert.isBlank(form.getPassword(), "原密码不能为空");
		Assert.isBlank(form.getNewPassword(), "新密码不能为空");

		String oldPassword;
		String newPassword;
		try {
			// 前端 RSA 公钥加密，此处私钥解密
			oldPassword = SaSecureUtil.rsaDecryptByPrivate(rsaPrivateKey, form.getPassword());
			newPassword = SaSecureUtil.rsaDecryptByPrivate(rsaPrivateKey, form.getNewPassword());
		} catch (Exception e) {
			return Result.error("密码格式错误！");
		}

		// 新密码复杂度校验
		if(!PasswordUtils.isComplex(newPassword)){
			return Result.error("新密码需包含大写、小写、数字和特殊字符，且不少于8位");
		}

		String username = getUsername();
		SysUser user = sysUserService.queryByUserName(username);
		if(user == null){
			return Result.error("用户不存在");
		}

		// 校验原密码
		String oldMd5 = SaSecureUtil.md5(username + ":" + oldPassword);
		if(!BCrypt.checkpw(oldMd5, user.getPassword())){
			return Result.error("原密码不正确");
		}

		//sha256加密并更新密码
		String newMd5 = SaSecureUtil.md5(username + ":" + newPassword);
		String encodedPassword = BCrypt.hashpw(newMd5, BCrypt.gensalt());
		boolean flag = sysUserService.updatePassword(username, encodedPassword);
		if(!flag){
			return Result.error("修改密码失败");
		}

		return Result.ok();
	}

	/**
	 * 管理员重置用户密码
	 */
	@Log("重置密码")
	@PostMapping("/resetPassword")
	@SaCheckPermission("sys:user:update")
	public Result<Object> resetPassword(@RequestBody Map<String, String> params){
		String id = params.get("id");
		String password = params.get("password");
		Assert.isBlank(id, "用户ID不能为空");
		Assert.isBlank(password, "新密码不能为空");

		String plainPassword;
		try {
			// 前端 RSA 公钥加密，此处私钥解密
			plainPassword = SaSecureUtil.rsaDecryptByPrivate(rsaPrivateKey, password);
		} catch (Exception e) {
			return Result.error("新密码格式错误！");
		}

		// 新密码复杂度校验
		if(!PasswordUtils.isComplex(plainPassword)){
			return Result.error("新密码需包含大写、小写、数字和特殊字符，且不少于8位");
		}

		boolean flag = sysUserService.resetPassword(id, plainPassword);
		if(!flag){
			return Result.error("重置密码失败");
		}
		return Result.ok();
	}
	
	/**
	 * 用户信息
	 */
	@GetMapping("/info/{id}")
	//@PreAuthorize("hasAuthority('sys:user:info')")
	public Result<SysUser> info(@PathVariable("id") String id){
		SysUser user = sysUserService.getById(id);
		
		//获取用户所属的角色列表
		List<String> roleIdList = sysUserRoleService.queryRoleIdList(id);
		user.setRoleIdList(roleIdList);
		
		return Result.ok(user);
	}
	
	/**
	 * 保存用户
	 */
	@Log("保存用户")
	@PostMapping("/save")
	@SaCheckPermission("sys:user:save")
	public Result<Object> save(@RequestBody SysUser user){
		ValidatorUtils.validateEntity(user, AddGroup.class);
		
		sysUserService.saveUser(user);
		
		return Result.ok();
	}
	
	/**
	 * 修改用户
	 */
	@Log("修改用户")
	@PutMapping("/update")
	@SaCheckPermission("sys:user:update")
	public Result<Object> update(@RequestBody SysUser user){
		ValidatorUtils.validateEntity(user, UpdateGroup.class);

		sysUserService.update(user);
		
		return Result.ok();
	}
	
	/**
	 * 删除用户
	 */
	@Log("删除用户")
	@DeleteMapping("/delete")
	@SaCheckPermission("sys:user:delete")
	public Result<Object> delete(@RequestParam(name = "id") String id){
		sysUserService.deleteEntity(id);
		return Result.ok();
	}

	@Log("批量删除用户")
	@DeleteMapping("/deleteBatch")
	@SaCheckPermission("sys:user:deleteBatch")
	public Result<Object> deleteBatch(@RequestParam(name = "ids") String ids) {
		sysUserService.deleteBatch(Arrays.asList(ids.split(",")));
		return Result.ok();
	}
}
