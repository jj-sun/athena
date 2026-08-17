

package com.athena.modules.sys.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.athena.modules.sys.entity.SysUserRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户与角色对应关系
 *
 * @author Mr.sun
 */
@Mapper
public interface SysUserRoleMapper extends BaseMapper<SysUserRole> {
	
	/**
	 * 根据用户ID，获取角色ID列表
	 */
	List<String> queryRoleIdList(String userId);

	/**
	 * 根据角色ID，获取用户ID列表
	 */
	List<String> queryUserIdList(String roleId);

	/**
	 * 根据角色ID数组，批量删除
	 */
	int deleteBatch(List<String> roleIds);

	/**
	 * 根据角色ID和用户ID删除
	 */
	int deleteByRoleAndUserId(@Param("roleId") String roleId, @Param("userId") String userId);

	/**
	 * 根据角色ID和用户ID列表批量删除
	 */
	int batchDeleteByRoleAndUserIds(@Param("roleId") String roleId, @Param("userIds") List<String> userIds);
}
