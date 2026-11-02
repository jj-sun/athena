package com.athena.modules.{{module}}.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.athena.common.annotation.Log;
import com.athena.common.base.dto.PageDto;
import com.athena.common.exception.RRException;
import com.athena.common.excel.ExcelUtils;
import com.athena.common.utils.DateUtils;
import com.athena.common.utils.PageUtils;
import com.athena.common.utils.Result;
import com.athena.common.validator.ValidatorUtils;
import com.athena.common.validator.group.AddGroup;
import com.athena.common.validator.group.UpdateGroup;
import com.athena.modules.{{module}}.dto.{{Entity}}ExportExcel;
import com.athena.modules.{{module}}.dto.{{Entity}}ImportExcel;
import com.athena.modules.{{module}}.entity.{{Entity}};
import com.athena.modules.{{module}}.service.{{Entity}}Service;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * {{entityLabel}}
 */
@RestController
@RequestMapping("/{{module}}/{{entity}}")
public class {{Entity}}Controller {

    @Autowired
    private {{Entity}}Service {{entity}}Service;

    @GetMapping("/list")
    @SaCheckPermission("{{permissionPrefix}}:list")
    public Result<PageUtils> list({{Entity}} {{entity}}, PageDto pageDto) {
        return Result.ok({{entity}}Service.queryPage({{entity}}, pageDto));
    }

    @GetMapping("/info/{id}")
    @SaCheckPermission("{{permissionPrefix}}:info")
    public Result<{{Entity}}> info(@PathVariable("id") String id) {
        {{Entity}} item = {{entity}}Service.getById(id);
        if (item == null) {
            throw new RRException("{{entityLabel}}不存在");
        }
        return Result.ok(item);
    }

    @Log("保存{{entityLabel}}")
    @PostMapping("/save")
    @SaCheckPermission("{{permissionPrefix}}:save")
    public Result<Object> save(@RequestBody {{Entity}} {{entity}}) {
        ValidatorUtils.validateEntity({{entity}}, AddGroup.class);
        {{entity}}Service.saveEntity({{entity}});
        return Result.ok();
    }

    @Log("修改{{entityLabel}}")
    @PutMapping("/update")
    @SaCheckPermission("{{permissionPrefix}}:update")
    public Result<Object> update(@RequestBody {{Entity}} {{entity}}) {
        ValidatorUtils.validateEntity({{entity}}, UpdateGroup.class);
        {{entity}}Service.updateEntity({{entity}});
        return Result.ok();
    }

    @Log("删除{{entityLabel}}")
    @DeleteMapping("/delete")
    @SaCheckPermission("{{permissionPrefix}}:delete")
    public Result<Object> delete(@RequestParam(name = "id") String id) {
        {{entity}}Service.deleteEntity(id);
        return Result.ok();
    }

    @Log("批量删除{{entityLabel}}")
    @DeleteMapping("/deleteBatch")
    @SaCheckPermission("{{permissionPrefix}}:deleteBatch")
    public Result<Object> deleteBatch(@RequestParam(name = "ids") String ids) {
        {{entity}}Service.deleteBatch(Arrays.asList(ids.split(",")));
        return Result.ok();
    }

    @GetMapping("/export")
    @SaCheckPermission("{{permissionPrefix}}:export")
    public void export({{Entity}} {{entity}}, HttpServletResponse response) {
        List<{{Entity}}ExportExcel> list = {{entity}}Service.queryExportList({{entity}});
        String fileName = "{{entityLabel}}列表_" + DateUtils.format(new Date(), "yyyyMMddHHmmss") + ".xlsx";
        ExcelUtils.write(response, fileName, "{{entityLabel}}列表", {{Entity}}ExportExcel.class, list);
    }

    @GetMapping("/importTemplate")
    @SaCheckPermission("{{permissionPrefix}}:import")
    public void importTemplate(HttpServletResponse response) {
        ExcelUtils.write(response, "{{entityLabel}}导入模板.xlsx", "{{entityLabel}}导入模板",
                {{Entity}}ImportExcel.class, Collections.emptyList());
    }

    @Log("导入{{entityLabel}}")
    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @SaCheckPermission("{{permissionPrefix}}:import")
    public Result<Object> importData(@RequestParam("file") MultipartFile file) {
        List<{{Entity}}ImportExcel> rows = ExcelUtils.read(file, {{Entity}}ImportExcel.class);
        int count = {{entity}}Service.import{{Entity}}s(rows);
        return Result.ok("成功导入 " + count + " 条{{entityLabel}}", count);
    }

    // <OPTIONAL: 需要被其他模块下拉选择时生成>
    // @GetMapping("/select")
    // @SaCheckPermission("{{permissionPrefix}}:list")
    // public Result<List<BaseSelect>> select(@RequestParam(required = false) String keyword) {
    //     return Result.ok({{entity}}Service.listOptions(keyword));
    // }
}
