package com.athena.modules.sys.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.athena.common.annotation.Log;
import com.athena.common.base.dto.PageDto;
import com.athena.common.cache.DictCache;
import com.athena.common.utils.PageUtils;
import com.athena.common.utils.Result;
import com.athena.common.validator.ValidatorUtils;
import com.athena.common.validator.group.AddGroup;
import com.athena.common.validator.group.UpdateGroup;
import com.athena.modules.sys.entity.SysDict;
import com.athena.modules.sys.service.SysDictService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author Mr.sun
 * @date 2022/1/20 16:23
 * @description
 */
@RestController
@RequestMapping(value = "/sys/dict")
public class SysDictController {

    @Autowired
    private SysDictService dictService;

    /**
     * 所有字典列表
     */
    @GetMapping("/list")
    @SaCheckPermission("sys:dict:list")
    public Result<PageUtils> list(SysDict dict, PageDto pageDto){
        PageUtils page = dictService.queryPage(dict, pageDto);
        return Result.ok(page);
    }

    /**
     * 字典信息
     */
    @GetMapping("/info/{id}")
    @SaCheckPermission("sys:dict:info")
    public Result<SysDict> info(@PathVariable("id") String id){
        SysDict dict = dictService.getById(id);

        return Result.ok(dict);
    }

    /**
     * 保存字典
     */
    @Log("保存字典")
    @PostMapping("/save")
    @SaCheckPermission("sys:dict:save")
    public Result<Object> save(@RequestBody SysDict dict){
        ValidatorUtils.validateEntity(dict, AddGroup.class);

        dictService.save(dict);

        DictCache.refreshAll();

        return Result.ok();
    }

    /**
     * 修改字典
     */
    @Log("修改字典")
    @PutMapping("/update")
    @SaCheckPermission("sys:dict:update")
    public Result<Object> update(@RequestBody SysDict dict){
        ValidatorUtils.validateEntity(dict, UpdateGroup.class);

        dictService.updateById(dict);

        DictCache.refreshAll();

        return Result.ok();
    }

    /**
     * 删除字典
     */
    @Log("删除字典")
    @DeleteMapping("/delete")
    @SaCheckPermission("sys:dict:delete")
    public Result<Object> delete(@RequestParam(name = "id") String id){
        dictService.deleteEntity(id);

        DictCache.refreshAll();

        return Result.ok();
    }

}
