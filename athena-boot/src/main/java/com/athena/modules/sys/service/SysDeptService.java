package com.athena.modules.sys.service;

import com.athena.modules.sys.entity.SysDept;
import com.athena.modules.sys.vo.SysDeptTree;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author Mr.sun
 * @date 2022/1/24 9:01
 * @description
 */
public interface SysDeptService extends IService<SysDept> {
    /**
     * 部门树形列表
     * @return
     */
    List<SysDept> deptTreeList();

    /**
     * 树形选择
     * @return
     */
    List<SysDeptTree> treeSelect();

    boolean deleteBatch(List<String> ids);

    List<String> getSubDeptIds(String deptCode);

    /**
     * 校验上级部门：必须存在，且不能选择自己或自己的下级（防止形成环导致节点从树中消失）。
     *
     * @param id       当前部门 ID，新增时可为 null
     * @param parentId 上级部门 ID
     */
    void validateParent(String id, String parentId);
}
