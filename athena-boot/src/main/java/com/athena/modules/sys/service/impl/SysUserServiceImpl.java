package com.athena.modules.sys.service.impl;

import cn.dev33.satoken.secure.BCrypt;
import cn.dev33.satoken.secure.SaSecureUtil;
import com.athena.common.base.dto.PageDto;
import com.athena.common.constant.Constant;
import com.athena.common.exception.RRException;
import com.athena.common.utils.PageUtils;
import com.athena.common.utils.PasswordUtils;
import com.athena.common.utils.Query;
import com.athena.common.validator.Assert;
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
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


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
			.like(StringUtils.isNotBlank(user.getRealname()), SysUser::getRealname, user.getRealname())
			.like(StringUtils.isNotBlank(user.getEmail()), SysUser::getEmail, user.getEmail());

		if (StringUtils.isNotBlank(user.getDeptCode())) {
			List<String> deptIds = sysDeptService.getSubDeptIds(user.getDeptCode());
			if (!deptIds.isEmpty()) {
				wrapper.in(SysUser::getDeptCode, deptIds);
			}
		}

		IPage<SysUser> page = this.page(
			new Query<SysUser>().getPage(pageDto, "ctime", false),
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
		// 不再使用固定默认口令（原为 123456）：创建用户必须显式设置初始密码，
		// 并且无论是否显式设置，都执行复杂度校验。
		if(StringUtils.isBlank(user.getPassword())) {
			throw new RRException("新增用户必须设置初始密码");
		}
		if(!PasswordUtils.isComplex(user.getPassword())) {
			throw new RRException("密码需包含大写、小写、数字和特殊字符，且不少于8位");
		}
		String md5 = SaSecureUtil.md5(user.getUsername() + ":" + user.getPassword());
		user.setPassword(BCrypt.hashpw(md5, BCrypt.gensalt()));
		this.save(user);

		//保存用户与角色关系
		sysUserRoleService.saveOrUpdate(user.getId(), user.getRoleIdList());
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void update(SysUser user) {
		Assert.isBlank(user.getId(), "用户ID不能为空");

		// 防止批量赋值（mass assignment）：更新接口的请求体直接绑定到实体，若不收口，
		// 客户端可以塞入 password（明文，会被原样写库）、delFlag（复活已删除账号）、
		// creator/ctime（伪造审计信息）等字段。密码只能走 /sys/user/password 与
		// /sys/user/resetPassword，审计字段交回 MetaObjectHandler 填充。
		user.setPassword(null);
		user.setDelFlag(null);
		user.setCreator(null);
		user.setCtime(null);
		user.setEditor(null);
		user.setMtime(null);

		SysUser existing = this.getById(user.getId());
		if (Objects.isNull(existing)) {
			throw new RRException("用户不存在");
		}
		if (Constant.SUPER_ADMIN.equals(existing.getUsername())) {
			// 超管账号的用户名与角色绑定不允许经用户管理接口改动：改名会让其失去超管身份，
			// 而清空角色绑定等同于把超管降级。其余资料（邮箱、手机号等）仍可正常编辑。
			user.setUsername(null);
			user.setRoleIdList(null);
		}

		this.updateById(user);

		// 仅在显式提交角色列表时才改动角色绑定。原实现无条件调用，导致前端未传 roleIdList 时
		// saveOrUpdate(id, null) 会清空该用户的全部角色 —— 既是数据丢失，也是提权前置手法
		// （先清空自己的角色，再重新绑定到更有权的角色）。
		if (Objects.nonNull(user.getRoleIdList())) {
			sysUserRoleService.saveOrUpdate(user.getId(), user.getRoleIdList());
		}
	}

	@Override
	public boolean deleteEntity(String id) {
		assertNotSuperAdmin(id);
		this.removeById(id);
		sysUserRoleService.saveOrUpdate(id, null);
		return false;
	}

	@Override
	public void deleteBatch(List<String> ids) {
		if (CollectionUtils.isEmpty(ids)) {
			return;
		}
		// 超管账号不允许被批量删除
		List<String> deletable = new ArrayList<>(ids.size());
		for (String id : ids) {
			if (!isSuperAdmin(id)) {
				deletable.add(id);
			}
		}
		if (deletable.isEmpty()) {
			throw new RRException("超级管理员账号不允许删除");
		}
		this.removeByIds(deletable);
		sysUserRoleService.remove(new LambdaQueryWrapper<SysUserRole>().in(SysUserRole::getUserId, deletable));
	}

	/**
	 * 校验目标用户不是超级管理员账号。
	 */
	private void assertNotSuperAdmin(String id) {
		if (isSuperAdmin(id)) {
			throw new RRException("超级管理员账号不允许删除");
		}
	}

	private boolean isSuperAdmin(String id) {
		SysUser target = this.getById(id);
		return Objects.nonNull(target) && Constant.SUPER_ADMIN.equals(target.getUsername());
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
		// 管理员重置密码等价于接管该账号，因此不能作用于超管账号：
		// 否则任何持有该权限的人都能重置 admin 密码后直接登录为超管。
		if (Constant.SUPER_ADMIN.equals(user.getUsername())) {
			throw new RRException("超级管理员密码不允许通过重置方式修改");
		}
		String md5 = SaSecureUtil.md5(user.getUsername() + ":" + newPassword);
		String encodedPassword = BCrypt.hashpw(md5, BCrypt.gensalt());
		SysUser userEntity = new SysUser();
		userEntity.setId(id);
		userEntity.setPassword(encodedPassword);
		return this.updateById(userEntity);
	}


}