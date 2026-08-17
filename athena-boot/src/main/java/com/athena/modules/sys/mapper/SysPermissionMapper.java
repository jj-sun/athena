package com.athena.modules.sys.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.athena.modules.sys.entity.SysPermission;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 菜单管理
 *
 * @author Mr.sun
 */
@Mapper
public interface SysPermissionMapper extends BaseMapper<SysPermission> {

	/**
	 * 查询用户的所有菜单ID
	 */
	List<String> queryAllMenuId(String userId);

}
