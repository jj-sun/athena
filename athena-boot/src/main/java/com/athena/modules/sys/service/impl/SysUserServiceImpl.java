package com.athena.modules.sys.service.impl;

import cn.dev33.satoken.secure.SaSecureUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.athena.common.base.dto.PageDto;
import com.athena.common.constant.Constant;
import com.athena.common.excel.ExcelUtils;
import com.athena.common.exception.RRException;
import com.athena.common.utils.PageUtils;
import com.athena.common.utils.PasswordUtils;
import com.athena.common.utils.Query;
import com.athena.common.validator.Assert;
import com.athena.common.validator.ValidatorUtils;
import com.athena.common.validator.group.AddGroup;
import com.athena.modules.sys.dto.SysUserExportExcel;
import com.athena.modules.sys.dto.SysUserImportExcel;
import com.athena.modules.sys.entity.SysDept;
import com.athena.modules.sys.entity.SysRole;
import com.athena.modules.sys.entity.SysUser;
import com.athena.modules.sys.entity.SysUserRole;
import com.athena.modules.sys.mapper.SysUserMapper;
import com.athena.modules.sys.service.SysDeptService;
import com.athena.modules.sys.service.SysRoleService;
import com.athena.modules.sys.service.SysUserRoleService;
import com.athena.modules.sys.service.SysUserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.spring.repository.CrudRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * 系统用户
 *
 * @author Mr.sun
 */
@Service
public class SysUserServiceImpl extends CrudRepository<SysUserMapper, SysUser> implements SysUserService {

	/**
	 * 用户导入单次上限。每行都要执行一次 BCrypt（约 50~100ms），
	 * 行数过多会让事务和请求长时间占用资源。
	 */
	private static final int MAX_IMPORT_USERS = 1000;

	/**
	 * 用户导出单次上限，避免一次性把整张表读入内存。
	 */
	private static final long MAX_EXPORT_ROWS = 50000L;

	@Autowired
	private SysUserRoleService sysUserRoleService;
	@Autowired
	private SysDeptService sysDeptService;
	@Autowired
	private SysRoleService sysRoleService;

	@Override
	public PageUtils queryPage(SysUser user, PageDto pageDto) {
		IPage<SysUser> page = this.page(
			new Query<SysUser>().getPage(pageDto, "ctime", false),
			buildQueryWrapper(user)
		);

		return new PageUtils(page);
	}

	/**
	 * 构建用户列表查询条件，分页与导出共用，避免两处条件漂移。
	 */
	private LambdaQueryWrapper<SysUser> buildQueryWrapper(SysUser user) {
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
		return wrapper;
	}

	@Override
	public SysUser queryByUserName(String username) {
		return this.getOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void saveUser(SysUser user) {
		// 用户名/手机号是用户的关键身份标识，必须非空且全局唯一
		user.setUsername(StringUtils.trimToNull(user.getUsername()));
		user.setMobile(StringUtils.trimToNull(user.getMobile()));
		if (StringUtils.isBlank(user.getUsername())) {
			throw new RRException("用户名不能为空");
		}
		if (StringUtils.isBlank(user.getMobile())) {
			throw new RRException("手机号不能为空");
		}
		if (existsByUsername(user.getUsername(), null)) {
			throw new RRException("用户名已存在");
		}
		if (existsByMobile(user.getMobile(), null)) {
			throw new RRException("手机号已存在");
		}

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
		user.setUsername(StringUtils.trimToNull(user.getUsername()));
		user.setMobile(StringUtils.trimToNull(user.getMobile()));

		SysUser existing = this.getById(user.getId());
		if (Objects.isNull(existing)) {
			throw new RRException("用户不存在");
		}
		if (StringUtils.isBlank(user.getMobile())) {
			throw new RRException("手机号不能为空");
		}
		if (existsByMobile(user.getMobile(), user.getId())) {
			throw new RRException("手机号已存在");
		}

		if (Constant.SUPER_ADMIN.equals(existing.getUsername())) {
			// 超管账号的用户名与角色绑定不允许经用户管理接口改动：改名会让其失去超管身份，
			// 而清空角色绑定等同于把超管降级。其余资料（邮箱、手机号等）仍可正常编辑。
			user.setUsername(null);
			user.setRoleIdList(null);
		} else {
			if (StringUtils.isBlank(user.getUsername())) {
				throw new RRException("用户名不能为空");
			}
			if (existsByUsername(user.getUsername(), user.getId())) {
				throw new RRException("用户名已存在");
			}
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
	public boolean existsByUsername(String username, String excludeId) {
		LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<SysUser>()
			.eq(SysUser::getUsername, username);
		if (StringUtils.isNotBlank(excludeId)) {
			wrapper.ne(SysUser::getId, excludeId);
		}
		return this.count(wrapper) > 0;
	}

	@Override
	public boolean existsByMobile(String mobile, String excludeId) {
		LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<SysUser>()
			.eq(SysUser::getMobile, mobile);
		if (StringUtils.isNotBlank(excludeId)) {
			wrapper.ne(SysUser::getId, excludeId);
		}
		return this.count(wrapper) > 0;
	}

	@Override
	public boolean deleteEntity(String id) {
		assertNotSuperAdmin(id);
		this.removeById(id);
		sysUserRoleService.saveOrUpdate(id, null);
		return true;
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

	@Override
	public List<SysUserExportExcel> queryExportList(SysUser user) {
		LambdaQueryWrapper<SysUser> wrapper = buildQueryWrapper(user);
		long total = this.count(wrapper);
		if (total > MAX_EXPORT_ROWS) {
			throw new RRException("导出数据量超过 " + MAX_EXPORT_ROWS + " 条，请缩小筛选范围");
		}

		List<SysUser> users = this.list(wrapper.orderByDesc(SysUser::getCtime));
		if (users.isEmpty()) {
			return new ArrayList<>();
		}

		// 部门 id -> 部门名称（注意：sys_user.dept_code 存的是部门 id）
		Map<String, String> deptNameMap = sysDeptService.list().stream()
			.filter(dept -> StringUtils.isNotBlank(dept.getId()))
			.collect(Collectors.toMap(SysDept::getId,
				dept -> StringUtils.defaultString(dept.getDeptName()), (a, b) -> a));

		// 角色 id -> 角色名称
		Map<String, String> roleNameMap = sysRoleService.list().stream()
			.filter(role -> StringUtils.isNotBlank(role.getId()))
			.collect(Collectors.toMap(SysRole::getId,
				role -> StringUtils.defaultString(role.getRoleName()), (a, b) -> a));

		// SysUser.roleIdList 是 @TableField(exist=false)，list() 不会填充，必须单独查关系表
		List<String> userIds = users.stream().map(SysUser::getId).toList();
		Map<String, List<String>> roleIdsByUser = sysUserRoleService.list(
				new LambdaQueryWrapper<SysUserRole>().in(SysUserRole::getUserId, userIds))
			.stream()
			.collect(Collectors.groupingBy(SysUserRole::getUserId,
				Collectors.mapping(SysUserRole::getRoleId, Collectors.toList())));

		List<SysUserExportExcel> result = new ArrayList<>(users.size());
		for (SysUser item : users) {
			SysUserExportExcel row = new SysUserExportExcel();
			row.setUsername(safeExcelText(item.getUsername()));
			row.setRealname(safeExcelText(item.getRealname()));
			row.setEmail(safeExcelText(item.getEmail()));
			row.setMobile(safeExcelText(item.getMobile()));
			row.setSex(sexText(item.getSex()));
			row.setDeptName(safeExcelText(deptNameMap.get(item.getDeptCode())));
			row.setRoleNames(safeExcelText(roleIdsByUser.getOrDefault(item.getId(), List.of()).stream()
				.map(roleNameMap::get)
				.filter(StringUtils::isNotBlank)
				.collect(Collectors.joining(","))));
			row.setStatus(Integer.valueOf(Constant.DelFlag.DEL_FLAG_0.getValue()).equals(item.getDelFlag())
				? "正常" : "禁用");
			row.setCtime(item.getCtime());
			result.add(row);
		}
		return result;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public int importUsers(List<SysUserImportExcel> rows) {
		if (CollectionUtils.isEmpty(rows)) {
			throw new RRException("Excel 中没有可导入的数据");
		}
		long dataRows = rows.stream().filter(row -> !ExcelUtils.isBlankRow(row)).count();
		if (dataRows > MAX_IMPORT_USERS) {
			throw new RRException("单次最多导入 " + MAX_IMPORT_USERS + " 个用户");
		}

		// 导入列中的部门/角色使用业务编码，落库时分别转换为部门 id 与角色 id
		Map<String, String> deptIdByCode = sysDeptService.list().stream()
			.filter(dept -> StringUtils.isNotBlank(dept.getDeptCode()) && StringUtils.isNotBlank(dept.getId()))
			.collect(Collectors.toMap(SysDept::getDeptCode, SysDept::getId, (a, b) -> a));
		Map<String, String> roleIdByCode = sysRoleService.list().stream()
			.filter(role -> StringUtils.isNotBlank(role.getRoleCode()) && StringUtils.isNotBlank(role.getId()))
			.collect(Collectors.toMap(SysRole::getRoleCode, SysRole::getId, (a, b) -> a));

		Set<String> importUsernames = rows.stream()
			.map(SysUserImportExcel::getUsername)
			.filter(StringUtils::isNotBlank)
			.map(String::trim)
			.collect(Collectors.toSet());
		Set<String> existingUsernames = importUsernames.isEmpty()
			? Set.of()
			: this.list(new LambdaQueryWrapper<SysUser>()
					.select(SysUser::getUsername)
					.in(SysUser::getUsername, importUsernames))
				.stream()
				.map(SysUser::getUsername)
				.filter(Objects::nonNull)
				.map(name -> name.toLowerCase(Locale.ROOT))
				.collect(Collectors.toSet());

		Set<String> importMobiles = rows.stream()
			.map(SysUserImportExcel::getMobile)
			.filter(StringUtils::isNotBlank)
			.map(String::trim)
			.collect(Collectors.toSet());
		Set<String> existingMobiles = importMobiles.isEmpty()
			? Set.of()
			: this.list(new LambdaQueryWrapper<SysUser>()
					.select(SysUser::getMobile)
					.in(SysUser::getMobile, importMobiles))
				.stream()
				.map(SysUser::getMobile)
				.filter(Objects::nonNull)
				.map(String::trim)
				.collect(Collectors.toSet());

		List<String> errors = new ArrayList<>();
		List<SysUser> usersToSave = new ArrayList<>(rows.size());
		Set<String> seenUsernames = new HashSet<>();
		Set<String> seenMobiles = new HashSet<>();
		for (int i = 0; i < rows.size(); i++) {
			int rowNum = i + 2;
			SysUserImportExcel row = rows.get(i);
			// read 阶段关闭了 ignoreEmptyRow，空行会以全空对象返回，这里跳过
			if (ExcelUtils.isBlankRow(row)) {
				continue;
			}

			String username = StringUtils.trimToNull(row.getUsername());
			if (username == null) {
				errors.add("第 " + rowNum + " 行：用户名不能为空");
				continue;
			}
			String usernameKey = username.toLowerCase(Locale.ROOT);
			if (!seenUsernames.add(usernameKey)) {
				errors.add("第 " + rowNum + " 行：用户名【" + username + "】在文件中重复");
				continue;
			}
			if (existingUsernames.contains(usernameKey)) {
				errors.add("第 " + rowNum + " 行：用户名【" + username + "】已存在");
				continue;
			}

			String mobile = StringUtils.trimToNull(row.getMobile());
			if (mobile == null) {
				errors.add("第 " + rowNum + " 行：手机号不能为空");
				continue;
			}
			if (!seenMobiles.add(mobile)) {
				errors.add("第 " + rowNum + " 行：手机号【" + mobile + "】在文件中重复");
				continue;
			}
			if (existingMobiles.contains(mobile)) {
				errors.add("第 " + rowNum + " 行：手机号【" + mobile + "】已存在");
				continue;
			}

			String password = row.getPassword();
			if (StringUtils.isBlank(password)) {
				errors.add("第 " + rowNum + " 行：密码不能为空");
				continue;
			}
			if (!PasswordUtils.isComplex(password)) {
				errors.add("第 " + rowNum + " 行：密码需包含大写、小写、数字和特殊字符，且不少于8位");
				continue;
			}

			SysUser user = new SysUser();
			user.setUsername(username);
			user.setRealname(StringUtils.trimToNull(row.getRealname()));
			user.setPassword(password);
			user.setEmail(StringUtils.trimToNull(row.getEmail()));
			user.setMobile(mobile);
			try {
				// 复用实体上的用户名/邮箱校验规则，避免两套规则漂移
				ValidatorUtils.validateEntity(user, AddGroup.class);
			} catch (RRException e) {
				errors.add("第 " + rowNum + " 行：" + StringUtils.defaultString(e.getMsg()));
				continue;
			}

			try {
				user.setSex(parseSex(row.getSex()));
			} catch (RRException e) {
				errors.add("第 " + rowNum + " 行：" + e.getMsg());
				continue;
			}

			String deptCode = StringUtils.trimToNull(row.getDeptCode());
			if (deptCode != null) {
				String deptId = deptIdByCode.get(deptCode);
				if (deptId == null) {
					errors.add("第 " + rowNum + " 行：部门编码【" + deptCode + "】不存在");
					continue;
				}
				user.setDeptCode(deptId);
			}

			List<String> roleIds = new ArrayList<>();
			String roleCodes = StringUtils.trimToNull(row.getRoleCodes());
			if (roleCodes != null) {
				boolean roleError = false;
				for (String code : roleCodes.split("[,，、;；]")) {
					String roleCode = code.trim();
					if (roleCode.isEmpty()) {
						continue;
					}
					String roleId = roleIdByCode.get(roleCode);
					if (roleId == null) {
						errors.add("第 " + rowNum + " 行：角色编码【" + roleCode + "】不存在");
						roleError = true;
						break;
					}
					if (!roleIds.contains(roleId)) {
						roleIds.add(roleId);
					}
				}
				if (roleError) {
					continue;
				}
			}
			user.setRoleIdList(roleIds);
			usersToSave.add(user);
		}

		if (!errors.isEmpty()) {
			int shown = Math.min(errors.size(), 20);
			StringBuilder message = new StringBuilder(String.join("；", errors.subList(0, shown)));
			if (errors.size() > shown) {
				message.append("；等共 ").append(errors.size()).append(" 条错误");
			}
			throw new RRException("导入失败：" + message);
		}
		if (usersToSave.isEmpty()) {
			throw new RRException("Excel 中没有可导入的数据");
		}

		try {
			for (SysUser user : usersToSave) {
				// saveUser 负责密码哈希与角色绑定；外层事务保证整批要么全部成功、要么全部回滚
				saveUser(user);
			}
		} catch (DuplicateKeyException e) {
			// 预检查与写入之间存在并发窗口，唯一索引是最后一道防线
			throw new RRException("导入失败：用户名已存在（可能被并发创建），请重试");
		}
		return usersToSave.size();
	}

	/**
	 * 解析导入的性别：支持 男/女/1/2/0/未知/保密，空白视为 0。
	 */
	private Integer parseSex(String sex) {
		if (StringUtils.isBlank(sex)) {
			return 0;
		}
		String value = sex.trim();
		if ("男".equals(value) || "1".equals(value) || "M".equalsIgnoreCase(value) || "male".equalsIgnoreCase(value)) {
			return 1;
		}
		if ("女".equals(value) || "2".equals(value) || "F".equalsIgnoreCase(value) || "female".equalsIgnoreCase(value)) {
			return 2;
		}
		if ("0".equals(value) || "未知".equals(value) || "保密".equals(value)) {
			return 0;
		}
		throw new RRException("性别【" + value + "】无法识别，请填写男/女或1/2");
	}

	private String sexText(Integer sex) {
		if (Integer.valueOf(1).equals(sex)) {
			return "男";
		}
		if (Integer.valueOf(2).equals(sex)) {
			return "女";
		}
		return "未知";
	}

	/**
	 * 防止 Excel 公式注入：以 = + - @ 或制表符/回车开头的文本会被 Excel 当作公式执行。
	 */
	private String safeExcelText(String value) {
		if (StringUtils.isEmpty(value)) {
			return value;
		}
		char first = value.charAt(0);
		if (first == '=' || first == '+' || first == '-' || first == '@' || first == '\t' || first == '\r') {
			return "'" + value;
		}
		return value;
	}

}