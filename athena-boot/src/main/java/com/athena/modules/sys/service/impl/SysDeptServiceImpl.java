package com.athena.modules.sys.service.impl;

import com.athena.common.constant.Constant;
import com.athena.common.exception.RRException;
import com.athena.common.utils.TreeUtils;
import com.athena.modules.sys.entity.SysDept;
import com.athena.modules.sys.entity.SysUser;
import com.athena.modules.sys.mapper.SysDeptMapper;
import com.athena.modules.sys.mapper.SysUserMapper;
import com.athena.modules.sys.service.SysDeptService;
import com.athena.modules.sys.vo.SysDeptTree;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private SysUserMapper sysUserMapper;

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
        List<String> validIds = ids.stream().filter(StringUtils::isNotBlank).distinct().collect(Collectors.toList());
        if (validIds.isEmpty()) {
            return true;
        }
        // 只允许删除“完整子树”：若某个被删除部门还有未选中的子部门，则拒绝，
        // 否则子部门会变成孤儿节点，从部门树中消失。
        Set<String> idSet = new HashSet<>(validIds);
        for (SysDept dept : this.list()) {
            if (idSet.contains(dept.getParentId()) && !idSet.contains(dept.getId())) {
                throw new RRException("部门【" + dept.getDeptName() + "】仍有未选中的子部门，不能删除");
            }
        }
        // 部门下有用户时拒绝删除，避免用户保留一个指向已删除部门的 deptCode
        Long userCount = sysUserMapper.selectCount(
                new LambdaQueryWrapper<SysUser>().in(SysUser::getDeptCode, validIds));
        if (userCount != null && userCount > 0) {
            throw new RRException("选中的部门下还有 " + userCount + " 个用户，请先调整用户所属部门");
        }
        return this.removeByIds(validIds);
    }

    @Override
    public void validateParent(String id, String parentId) {
        if (StringUtils.isBlank(parentId) || Constant.TREE_ROOT.equals(parentId)) {
            return;
        }
        if (parentId.equals(id)) {
            throw new RRException("上级部门不能是自己");
        }
        Set<String> visited = new HashSet<>();
        String current = parentId;
        while (StringUtils.isNotBlank(current) && !Constant.TREE_ROOT.equals(current) && visited.add(current)) {
            if (current.equals(id)) {
                throw new RRException("上级部门不能是自己的下级");
            }
            SysDept parent = this.getById(current);
            if (parent == null) {
                throw new RRException("上级部门不存在");
            }
            current = parent.getParentId();
        }
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
