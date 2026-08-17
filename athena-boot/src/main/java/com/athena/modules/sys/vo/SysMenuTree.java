package com.athena.modules.sys.vo;

import com.athena.common.base.tree.BaseTree;
import com.athena.common.constant.Constant;
import com.athena.modules.sys.entity.SysPermission;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * @author Mr.sun
 * @date 2021/12/20 16:16
 * @description
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SysMenuTree extends BaseTree<SysPermission> {

    @Serial
    private static final long serialVersionUID = -2798455242241355324L;

    public SysMenuTree(SysPermission sysPermission) {
        super.setInfo(sysPermission);
        this.toTree();
    }

    @Override
    public void toTree() {
        SysPermission sysPermission = super.getInfo();
        super.setKey(sysPermission.getId());
        super.setParentKey(sysPermission.getParentId());
        super.setTitle(sysPermission.getName());
        super.setDisabled(sysPermission.getName().equals(Constant.BASE_MENU_NAME));
    }
}
