package com.athena.modules.sys.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.athena.common.annotation.Log;
import com.athena.common.exception.RRException;
import com.athena.common.utils.Result;
import com.athena.common.validator.ValidatorUtils;
import com.athena.common.validator.group.AddGroup;
import com.athena.common.validator.group.UpdateGroup;
import com.athena.modules.sys.entity.SysDept;
import com.athena.modules.sys.service.SysDeptService;
import com.athena.modules.sys.vo.SysDeptTree;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

/**
 * @author Mr.sun
 * @date 2022/1/24 9:06
 * @description 部门描述
 */
@RestController
@RequestMapping(value = "/sys/dept")
public class SysDeptController {

    @Autowired
    private SysDeptService deptService;

    /**
     * 所有部门列表树
     */
    @GetMapping("/list")
    @SaCheckPermission("sys:dept:list")
    public Result<List<SysDept>> list(SysDept dept){
        List<SysDept> deptTreeList = deptService.deptTreeList();
        return Result.ok(deptTreeList);
    }

    /**
     * 选择菜单(添加、修改菜单)
     */
    @GetMapping("/select")
    @SaCheckPermission("sys:dept:select")
    public Result<List<SysDeptTree>> select(){
        //查询列表数据
        List<SysDeptTree> menuList = deptService.treeSelect();

        return Result.ok(menuList);
    }

    /**
     * 部门信息
     */
    @GetMapping("/info/{id}")
    @SaCheckPermission("sys:dept:info")
    public Result<SysDept> info(@PathVariable("id") String id){
        SysDept dept = deptService.getById(id);

        return Result.ok(dept);
    }

    /**
     * 保存部门
     */
    @Log("保存部门")
    @PostMapping("/save")
    @SaCheckPermission("sys:dept:save")
    public Result<Object> save(@RequestBody SysDept dept){
        ValidatorUtils.validateEntity(dept, AddGroup.class);
        if (StringUtils.isBlank(dept.getParentId())) {
            throw new RRException("上级部门不能为空");
        }
        deptService.validateParent(null, dept.getParentId());

        deptService.save(dept);

        return Result.ok();
    }

    /**
     * 修改部门
     */
    @Log("修改部门")
    @PutMapping("/update")
    @SaCheckPermission("sys:dept:update")
    public Result<Object> update(@RequestBody SysDept dept){
        ValidatorUtils.validateEntity(dept, UpdateGroup.class);
        deptService.validateParent(dept.getId(), dept.getParentId());

        deptService.updateById(dept);
        return Result.ok();
    }

    /**
     * 删除部门
     */
    @Log("删除部门")
    @DeleteMapping("/delete")
    @SaCheckPermission("sys:dept:delete")
    public Result<Object> delete(@RequestParam(name = "id") String id){

        // 复用批量删除的校验：有子部门时拒绝删除
        deptService.deleteBatch(List.of(id));
        return Result.ok();
    }

    @Log("批量删除部门")
    @DeleteMapping("/deleteBatch")
    @SaCheckPermission("sys:dept:delete")
    public Result<Object> deleteBatch(@RequestParam(name = "ids") String ids) {
        deptService.deleteBatch(Arrays.asList(ids.split(",")));
        return Result.ok();
    }

}
