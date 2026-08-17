package com.athena.modules.sys.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * @author Mr.sun
 * @date 2022/1/25 11:01
 * @description
 */
@Data
public class SysRolePermissionDto implements Serializable {
    @Serial
    private static final long serialVersionUID = -807931590578312248L;

    private String roleId;

    private List<String> permissionIdList;

}
