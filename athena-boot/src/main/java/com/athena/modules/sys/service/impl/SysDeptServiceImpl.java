package com.athena.modules.sys.service.impl;

import com.athena.common.constant.Constant;
import com.athena.common.utils.TreeUtils;
import com.athena.modules.sys.entity.SysDept;
import com.athena.modules.sys.mapper.SysDeptMapper;
import com.athena.modules.sys.service.SysDeptService;
import com.athena.modules.sys.vo.SysDeptTree;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Mr.sun
 * @date 2022/1/24 9:01
 * @description
 */
@Service
public class SysDeptServiceImpl extends ServiceImpl<SysDeptMapper, SysDept> implements SysDeptService {
    @Override
    public List<SysDept> deptTreeList() {
        List<SysDept> sysDeptList = this.list();
        if(!CollectionUtils.isEmpty(sysDeptList)) {
            List<SysDept> roots = sysDeptList.stream().filter(root -> root.getParentId().equals(Constant.TREE_ROOT)).collect(Collectors.toList());
            buildTree(roots, sysDeptList);
            return roots;
        }
        return sysDeptList;
    }
    private void buildTree(List<SysDept> roots, List<SysDept> all) {
        if(CollectionUtils.isEmpty(roots)) {
            return;
        }
        roots.forEach(root -> {
            List<SysDept> childrenList = all.stream().filter(tree -> tree.getParentId().equals(root.getId())).collect(Collectors.toList());
            if(!CollectionUtils.isEmpty(childrenList)) {
                root.setChildren(childrenList);
                buildTree(childrenList, all);
            }
        });
    }

    @Override
    public List<SysDeptTree> treeSelect() {
        List<SysDept> sysDeptList = this.list();
        List<SysDeptTree> menuTreeList = Lists.newArrayList();
        if(!CollectionUtils.isEmpty(sysDeptList)) {
            sysDeptList.forEach(sysDept -> menuTreeList.add(new SysDeptTree(sysDept)));
            List<SysDeptTree> root = menuTreeList.stream().filter(sysMenuTree -> sysMenuTree.getParentKey().equals(Constant.TREE_ROOT)).collect(Collectors.toList());
            if(!CollectionUtils.isEmpty(root)) {
                TreeUtils.buildTree(root, menuTreeList);
                return root;
            }
        }
        return null;
    }

    @Override
    public boolean deleteBatch(List<String> ids) {
        return this.removeByIds(ids);
    }

    @Override
    public List<String> getSubDeptIds(String deptCode) {
        if (StringUtils.isBlank(deptCode)) {
            return new ArrayList<>();
        }
        List<SysDept> allDepts = this.list();
        Set<String> result = new HashSet<>();
        String[] ids = deptCode.split(",");
        for (String id : ids) {
            if (StringUtils.isNotBlank(id)) {
                collectDeptAndChildren(id, allDepts, result);
            }
        }
        return new ArrayList<>(result);
    }

    private void collectDeptAndChildren(String deptId, List<SysDept> allDepts, Set<String> result) {
        result.add(deptId);
        for (SysDept dept : allDepts) {
            if (deptId.equals(dept.getParentId())) {
                collectDeptAndChildren(dept.getId(), allDepts, result);
            }
        }
    }
}
