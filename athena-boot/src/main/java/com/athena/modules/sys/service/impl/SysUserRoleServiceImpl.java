package com.athena.modules.sys.service.impl;

import com.athena.modules.sys.entity.SysUserRole;
import com.athena.modules.sys.mapper.SysUserRoleMapper;
import com.athena.modules.sys.service.SysUserRoleService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户与角色对应关系
 *
 * @author Mr.sun
 */
@Service
public class SysUserRoleServiceImpl extends ServiceImpl<SysUserRoleMapper, SysUserRole> implements SysUserRoleService {

	@Override
	public void saveOrUpdate(String userId, List<String> roleIdList) {
		//先删除用户与角色关系
		this.remove(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));

		if(roleIdList == null || roleIdList.size() == 0){
			return ;
		}

		//保存用户与角色关系
		for(String roleId : roleIdList){
			SysUserRole sysUserRole = new SysUserRole();
			sysUserRole.setUserId(userId);
			sysUserRole.setRoleId(roleId);
			this.save(sysUserRole);
		}
	}

	@Override
	public List<String> queryRoleIdList(String userId) {
		return baseMapper.queryRoleIdList(userId);
	}

	@Override
	public List<String> queryUserIdList(String roleId) {
		return baseMapper.queryUserIdList(roleId);
	}

	@Override
	public int deleteBatch(List<String> roleIds){
		return baseMapper.deleteBatch(roleIds);
	}

	@Override
	public void saveUserRole(String userId, String roleId) {
		long exists = this.count(new LambdaQueryWrapper<SysUserRole>()
				.eq(SysUserRole::getUserId, userId)
				.eq(SysUserRole::getRoleId, roleId));
		if (exists > 0) {
			return;
		}
		SysUserRole sysUserRole = new SysUserRole();
		sysUserRole.setUserId(userId);
		sysUserRole.setRoleId(roleId);
		this.save(sysUserRole);
	}

	@Override
	public void deleteUserRole(String userId, String roleId) {
		baseMapper.deleteByRoleAndUserId(roleId, userId);
	}

	@Override
	public void batchDeleteUserRole(String roleId, List<String> userIds) {
		baseMapper.batchDeleteByRoleAndUserIds(roleId, userIds);
	}
}
