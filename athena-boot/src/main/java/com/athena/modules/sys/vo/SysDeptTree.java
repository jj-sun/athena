package com.athena.modules.sys.vo;

import com.athena.common.base.tree.BaseTree;
import com.athena.modules.sys.entity.SysDept;

import java.io.Serial;

/**
 * @author Mr.sun
 * @date 2022/1/24 9:27
 * @description
 */
public class SysDeptTree extends BaseTree<SysDept>{

    @Serial
    private static final long serialVersionUID = 8200279045996987226L;

    public SysDeptTree(SysDept dept) {
        super.setInfo(dept);
        this.toTree();
    }

    @Override
    public void toTree() {
        SysDept dept = super.getInfo();
        super.setKey(dept.getId());
        super.setParentKey(dept.getParentId());
        super.setTitle(dept.getDeptName());
    }
}
