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
import com.athena.modules.sys.entity.SysDictItem;
import com.athena.modules.sys.service.SysDictItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author Mr.sun
 * @date 2022/1/21 9:40
 * @description
 */
@RestController
@RequestMapping(value = "/sys/dictItem")
public class SysDictItemController {

    @Autowired
    private SysDictItemService dictItemService;

    /**
     * 所有字典列表
     */
    @GetMapping("/list")
    @SaCheckPermission("sys:dictItem:list")
    public Result<PageUtils> list(SysDictItem dictItem, PageDto pageDto){
        PageUtils page = dictItemService.queryPage(dictItem, pageDto);
        return Result.ok(page);
    }

    /**
     * 字典信息
     */
    @GetMapping("/info/{id}")
    @SaCheckPermission("sys:dictItem:info")
    public Result<SysDictItem> info(@PathVariable("id") String id){
        SysDictItem dictItem = dictItemService.getById(id);
        return Result.ok(dictItem);
    }

    /**
     * 保存字典
     */
    @Log("保存字典")
    @PostMapping("/save")
    @SaCheckPermission("sys:dictItem:save")
    public Result<Object> save(@RequestBody SysDictItem dictItem){
        ValidatorUtils.validateEntity(dictItem, AddGroup.class);

        dictItemService.save(dictItem);

        String dictCode = DictCache.getDictCodeById(dictItem.getDictId());
        if (dictCode != null) {
            DictCache.refreshDict(dictCode);
        }

        return Result.ok();
    }

    /**
     * 修改字典
     */
    @Log("修改字典")
    @PutMapping("/update")
    @SaCheckPermission("sys:dictItem:update")
    public Result<Object> update(@RequestBody SysDictItem dictItem){
        ValidatorUtils.validateEntity(dictItem, UpdateGroup.class);

        dictItemService.updateById(dictItem);

        String dictCode = DictCache.getDictCodeById(dictItem.getDictId());
        if (dictCode != null) {
            DictCache.refreshDict(dictCode);
        }

        return Result.ok();
    }

    /**
     * 删除字典
     */
    @Log("删除字典")
    @DeleteMapping("/delete")
    @SaCheckPermission("sys:dictItem:delete")
    public Result<Object> delete(@RequestParam(name = "id") String id){
        SysDictItem dictItem = dictItemService.getById(id);
        if (dictItem != null) {
            String dictCode = DictCache.getDictCodeById(dictItem.getDictId());
            dictItemService.removeById(id);
            if (dictCode != null) {
                DictCache.refreshDict(dictCode);
            }
        }
        return Result.ok();
    }

}
