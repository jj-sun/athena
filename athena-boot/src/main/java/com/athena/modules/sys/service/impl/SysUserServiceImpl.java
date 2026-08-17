package com.athena.modules.sys.service.impl;

import cn.dev33.satoken.secure.BCrypt;
import cn.dev33.satoken.secure.SaSecureUtil;
import com.athena.common.base.dto.PageDto;
import com.athena.common.utils.PageUtils;
import com.athena.common.utils.Query;
import com.athena.modules.sys.entity.SysUser;
import com.athena.modules.sys.entity.SysUserRole;
import com.athena.modules.sys.mapper.SysUserMapper;
import com.athena.modules.sys.service.SysDeptService;
import com.athena.modules.sys.service.SysUserRoleService;
import com.athena.modules.sys.service.SysUserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;


/**
 * 系统用户
 *
 * @author Mr.sun
 */
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {
	@Autowired
	private SysUserRoleService sysUserRoleService;
	@Autowired
	private SysDeptService sysDeptService;

	@Override
	public PageUtils queryPage(SysUser user, PageDto pageDto) {
		LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<SysUser>()
			.like(StringUtils.isNotBlank(user.getUsername()), SysUser::getUsername, user.getUsername())
			.like(StringUtils.isNotBlank(user.getRealname()), SysUser::getRealname, user.getRealname());

		if (StringUtils.isNotBlank(user.getDeptCode())) {
			List<String> deptIds = sysDeptService.getSubDeptIds(user.getDeptCode());
			if (!deptIds.isEmpty()) {
				wrapper.in(SysUser::getDeptCode, deptIds);
			}
		}

		IPage<SysUser> page = this.page(
			new Query<SysUser>().getPage(pageDto),
			wrapper
		);

		return new PageUtils(page);
	}

	@Override
	public List<String> queryAllPerms(String username) {
		return baseMapper.queryAllPerms(username);
	}

	@Override
	public SysUser queryByUserName(String username) {
		return this.getOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void saveUser(SysUser user) {
		//sha256加密
		//BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		if(StringUtils.isBlank(user.getPassword())) {
			String md5 = SaSecureUtil.md5(user.getUsername() + ":123456");
			user.setPassword(BCrypt.hashpw(md5, BCrypt.gensalt()));
		} else {
			String md5 = SaSecureUtil.md5(user.getUsername() + ":" + user.getPassword());
			user.setPassword(BCrypt.hashpw(md5, BCrypt.gensalt()));
		}
		this.save(user);

		//保存用户与角色关系
		sysUserRoleService.saveOrUpdate(user.getId(), user.getRoleIdList());
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void update(SysUser user) {

		this.updateById(user);
		
		//保存用户与角色关系
		sysUserRoleService.saveOrUpdate(user.getId(), user.getRoleIdList());
	}

	@Override
	public boolean deleteEntity(String id) {
		this.removeById(id);
		sysUserRoleService.saveOrUpdate(id, null);
		return false;
	}

	@Override
	public void deleteBatch(List<String> ids) {
		this.removeByIds(ids);
		sysUserRoleService.remove(new LambdaQueryWrapper<SysUserRole>().in(SysUserRole::getUserId, ids));
	}

	@Override
	public boolean updatePassword(String username, String newPassword) {
		SysUser userEntity = new SysUser();
		userEntity.setPassword(newPassword);
		return this.update(userEntity,
				new QueryWrapper<SysUser>().eq("username", username));
	}

	@Override
	public boolean resetPassword(String id, String newPassword) {
		SysUser user = this.getById(id);
		if (user == null) {
			return false;
		}
		String md5 = SaSecureUtil.md5(user.getUsername() + ":" + newPassword);
		String encodedPassword = BCrypt.hashpw(md5, BCrypt.gensalt());
		SysUser userEntity = new SysUser();
		userEntity.setId(id);
		userEntity.setPassword(encodedPassword);
		return this.updateById(userEntity);
	}


}