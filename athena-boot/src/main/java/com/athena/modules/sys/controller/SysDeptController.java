package com.athena.modules.sys.controller;

import com.athena.common.annotation.Log;
import com.athena.common.utils.Result;
import com.athena.common.validator.ValidatorUtils;
import com.athena.common.validator.group.AddGroup;
import com.athena.common.validator.group.UpdateGroup;
import com.athena.modules.sys.entity.SysDept;
import com.athena.modules.sys.service.SysDeptService;
import com.athena.modules.sys.vo.SysDeptTree;
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
    //@PreAuthorize("hasAuthority('sys:user:list')")
    public Result<List<SysDept>> list(SysDept dept){
        List<SysDept> deptTreeList = deptService.deptTreeList();
        return Result.ok(deptTreeList);
    }

    /**
     * 选择菜单(添加、修改菜单)
     */
    @GetMapping("/select")
    //@PreAuthorize("hasAuthority('sys:menu:select')")
    public Result<List<SysDeptTree>> select(){
        //查询列表数据
        List<SysDeptTree> menuList = deptService.treeSelect();

        return Result.ok(menuList);
    }

    /**
     * 部门信息
     */
    @GetMapping("/info/{id}")
    //@PreAuthorize("hasAuthority('sys:user:info')")
    public Result<SysDept> info(@PathVariable("id") String id){
        SysDept dept = deptService.getById(id);

        return Result.ok(dept);
    }

    /**
     * 保存部门
     */
    @Log("保存部门")
    @PostMapping("/save")
    //@PreAuthorize("hasAuthority('sys:user:save')")
    public Result<Object> save(@RequestBody SysDept dept){
        ValidatorUtils.validateEntity(dept, AddGroup.class);

        deptService.save(dept);

        return Result.ok();
    }

    /**
     * 修改部门
     */
    @Log("修改部门")
    @PutMapping("/update")
    //@PreAuthorize("hasAuthority('sys:user:update')")
    public Result<Object> update(@RequestBody SysDept dept){
        ValidatorUtils.validateEntity(dept, UpdateGroup.class);

        deptService.updateById(dept);
        return Result.ok();
    }

    /**
     * 删除部门
     */
    @Log("删除部门")
    @DeleteMapping("/delete")
    //@PreAuthorize("hasAuthority('sys:user:delete')")
    public Result<Object> delete(@RequestParam(name = "id") String id){

        deptService.removeById(id);
        return Result.ok();
    }

    @Log("批量删除部门")
    @DeleteMapping("/deleteBatch")
    public Result<Object> deleteBatch(@RequestParam(name = "ids") String ids) {
        deptService.deleteBatch(Arrays.asList(ids.split(",")));
        return Result.ok();
    }

}
