# 后端生成规范

## 1. 包结构

```
com.athena.modules.{{module}}
├── controller/{{Entity}}Controller.java
├── service/{{Entity}}Service.java
├── service/impl/{{Entity}}ServiceImpl.java
├── mapper/{{Entity}}Mapper.java
├── mapper/xml/{{Entity}}Mapper.xml      # 仅自定义 SQL
├── entity/{{Entity}}.java
└── dto/
    ├── {{Entity}}ImportExcel.java
    └── {{Entity}}ExportExcel.java
```

源码根：`athena-boot/src/main/java/`。XML 放在 `mapper/xml`，`mapper-locations` 已配置为 `classpath*:com/athena/modules/**/xml/*Mapper.xml`。

## 2. Entity

继承 `com.athena.common.base.po.BasePo`，**不要重复声明** `id/creator/ctime/editor/mtime/delFlag`。

```java
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("{{tableName}}")
public class {{Entity}} extends BasePo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "名称不能为空", groups = {AddGroup.class, UpdateGroup.class})
    @Size(max = 128, message = "名称不能超过128个字符", groups = {AddGroup.class, UpdateGroup.class})
    private String name;

    @NotNull(message = "状态不能为空", groups = {AddGroup.class, UpdateGroup.class})
    private Integer status;

    @Dict(dicCode = "id", dicText = "category_name", dictTable = "product_category")
    private String categoryId;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date publishDate;

    @TableField(exist = false)
    private Date publishDateStart;

    @TableField(exist = false)
    private Date publishDateEnd;
}
```

规则：

- 必填校验必须带 `groups`。
- `@Dict` 只用于展示型翻译。
- `query: between` 生成两个瞬态字段。
- 不添加 `@TableLogic`、`@UniqueUser`、密码/角色/部门字段。
- 审计字段由 `MybatisPlusMetaObjectHandler` 填充，不手动赋值。

## 3. Mapper

```java
@Mapper
public interface {{Entity}}Mapper extends BaseMapper<{{Entity}}> {
}
```

无自定义 SQL 时不需要 XML。需要动态选项/联表查询时再加 XML。

## 4. Service 接口

```java
public interface {{Entity}}Service extends IRepository<{{Entity}}> {
    PageUtils queryPage({{Entity}} {{entity}}, PageDto pageDto);
    void saveEntity({{Entity}} {{entity}});
    void updateEntity({{Entity}} {{entity}});
    boolean deleteEntity(String id);
    void deleteBatch(List<String> ids);
    List<{{Entity}}ExportExcel> queryExportList({{Entity}} {{entity}});
    int import{{Entity}}s(List<{{Entity}}ImportExcel> rows);
}
```

需要被其他模块下拉选择时，再加：

```java
List<BaseSelect> listOptions(String keyword);
```

## 5. Service 实现

```java
@Service
public class {{Entity}}ServiceImpl
        extends CrudRepository<{{Entity}}Mapper, {{Entity}}>
        implements {{Entity}}Service {

    private static final long MAX_EXPORT_ROWS = 50000L;
    private static final int MAX_IMPORT_ROWS = 5000;
```

### 分页

```java
@Override
public PageUtils queryPage({{Entity}} {{entity}}, PageDto pageDto) {
    IPage<{{Entity}}> page = this.page(
        new Query<{{Entity}}>().getPage(pageDto, "ctime", false),
        buildQueryWrapper({{entity}})
    );
    return new PageUtils(page);
}
```

`buildQueryWrapper` 必须同时供分页与导出使用：

```java
private LambdaQueryWrapper<{{Entity}}> buildQueryWrapper({{Entity}} {{entity}}) {
    LambdaQueryWrapper<{{Entity}}> wrapper = new LambdaQueryWrapper<>();
    wrapper.like(StringUtils.isNotBlank({{entity}}.getName()),
        {{Entity}}::getName, {{entity}}.getName());
    wrapper.eq({{entity}}.getStatus() != null,
        {{Entity}}::getStatus, {{entity}}.getStatus());
    wrapper.ge({{entity}}.getPublishDateStart() != null,
        {{Entity}}::getPublishDate, {{entity}}.getPublishDateStart());
    wrapper.le({{entity}}.getPublishDateEnd() != null,
        {{Entity}}::getPublishDate, {{entity}}.getPublishDateEnd());
    return wrapper;
}
```

### 新增/更新

```java
@Override
@Transactional(rollbackFor = Exception.class)
public void saveEntity({{Entity}} {{entity}}) {
    normalize({{entity}});
    checkUnique({{entity}}, null);
    this.save({{entity}});
}

@Override
@Transactional(rollbackFor = Exception.class)
public void updateEntity({{Entity}} {{entity}}) {
    Assert.isBlank({{entity}}.getId(), "ID不能为空");
    if (this.getById({{entity}}.getId()) == null) {
        throw new RRException("数据不存在");
    }
    normalize({{entity}});
    checkUnique({{entity}}, {{entity}}.getId());
    this.updateById({{entity}});
}
```

- `normalize` 对所有 String 字段 `trimToNull`。
- `checkUnique` 对每个 `unique: true` 字段查询，更新时排除自身 id，重复抛 `RRException("<label>已存在")`。
- 不复制用户管理的密码哈希、超管保护、角色绑定逻辑。

### 删除

```java
@Override
public boolean deleteEntity(String id) {
    return this.removeById(id);
}

@Override
public void deleteBatch(List<String> ids) {
    if (ids == null || ids.isEmpty()) {
        return;
    }
    this.removeByIds(ids);
}
```

项目是物理删除。`del_flag` 只是审计/状态字段。

### 导出

```java
@Override
public List<{{Entity}}ExportExcel> queryExportList({{Entity}} {{entity}}) {
    LambdaQueryWrapper<{{Entity}}> wrapper = buildQueryWrapper({{entity}});
    long total = this.count(wrapper);
    if (total > MAX_EXPORT_ROWS) {
        throw new RRException("导出数据量超过 " + MAX_EXPORT_ROWS + " 条，请缩小筛选范围");
    }
    List<{{Entity}}> list = this.list(wrapper.orderByDesc({{Entity}}::getCtime));
    List<{{Entity}}ExportExcel> result = new ArrayList<>(list.size());
    for ({{Entity}} item : list) {
        {{Entity}}ExportExcel row = new {{Entity}}ExportExcel();
        row.setName(safeExcelText(item.getName()));
        row.setStatus(statusText(item.getStatus()));
        row.setCategoryName(safeExcelText(resolveCategoryName(item.getCategoryId())));
        row.setPublishDate(item.getPublishDate());
        row.setCtime(item.getCtime());
        result.add(row);
    }
    return result;
}
```

- 导出 DTO 的字段必须与列表列一致（可略去操作列），追加 `ctime`。
- 字典/外键/状态在 service 中解析为中文文本；`@Dict` 不作用于 DTO。
- 所有文本都经过 `safeExcelText`。
- 敏感字段绝不进入导出 DTO。

公式注入转义（从 `SysUserServiceImpl` 复制）：

```java
private String safeExcelText(String value) {
    if (StringUtils.isEmpty(value)) {
        return value;
    }
    char first = value.charAt(0);
    if (first == '=' || first == '+' || first == '-' || first == '@'
            || first == '\t' || first == '\r') {
        return "'" + value;
    }
    return value;
}
```

### 导入

```java
@Override
@Transactional(rollbackFor = Exception.class)
public int import{{Entity}}s(List<{{Entity}}ImportExcel> rows) {
    if (rows == null || rows.isEmpty()) {
        throw new RRException("Excel 中没有可导入的数据");
    }
    long dataRows = rows.stream().filter(row -> !ExcelUtils.isBlankRow(row)).count();
    if (dataRows > MAX_IMPORT_ROWS) {
        throw new RRException("单次最多导入 " + MAX_IMPORT_ROWS + " 条");
    }

    List<String> errors = new ArrayList<>();
    List<{{Entity}}> entities = new ArrayList<>(rows.size());
    for (int i = 0; i < rows.size(); i++) {
        int rowNum = i + 2;
        {{Entity}}ImportExcel row = rows.get(i);
        if (ExcelUtils.isBlankRow(row)) {
            continue;
        }
        {{Entity}} entity = new {{Entity}}();
        try {
            entity.setName(StringUtils.trimToNull(row.getName()));
            entity.setStatus(parseInteger(row.getStatus(), "状态", rowNum));
            entity.setPrice(parseDecimal(row.getPrice(), "价格", rowNum));
            entity.setPublishDate(parseDate(row.getPublishDate(), "yyyy-MM-dd", "发布日期", rowNum));
            // 每个 unique 字段先做数据库存在性预检查，保证错误带行号
            // if (existsByName(entity.getName(), null)) {
            //     throw new RRException("第 " + rowNum + " 行：名称已存在");
            // }
            ValidatorUtils.validateEntity(entity, AddGroup.class);
        } catch (RRException e) {
            errors.add("第 " + rowNum + " 行：" + StringUtils.defaultString(e.getMsg()));
            continue;
        }
        entities.add(entity);
    }

    if (!errors.isEmpty()) {
        int shown = Math.min(errors.size(), 20);
        StringBuilder message = new StringBuilder(String.join("；", errors.subList(0, shown)));
        if (errors.size() > shown) {
            message.append("；等共 ").append(errors.size()).append(" 条错误");
        }
        throw new RRException("导入失败：" + message);
    }
    if (entities.isEmpty()) {
        throw new RRException("Excel 中没有可导入的数据");
    }

    try {
        for ({{Entity}} entity : entities) {
            saveEntity(entity);
        }
    } catch (DuplicateKeyException e) {
        throw new RRException("导入失败：数据已存在（可能被并发创建），请重试");
    }
    return entities.size();
}
```

解析辅助方法（按需生成）：

```java
private Integer parseInteger(String value, String label, int rowNum) {
    if (StringUtils.isBlank(value)) {
        return null;
    }
    try {
        return Integer.valueOf(value.trim());
    } catch (NumberFormatException e) {
        throw new RRException("第 " + rowNum + " 行：" + label + "格式不正确");
    }
}

private BigDecimal parseDecimal(String value, String label, int rowNum) {
    if (StringUtils.isBlank(value)) {
        return null;
    }
    try {
        return new BigDecimal(value.trim());
    } catch (NumberFormatException e) {
        throw new RRException("第 " + rowNum + " 行：" + label + "格式不正确");
    }
}

private Date parseDate(String value, String pattern, String label, int rowNum) {
    if (StringUtils.isBlank(value)) {
        return null;
    }
    try {
        return new SimpleDateFormat(pattern).parse(value.trim());
    } catch (ParseException e) {
        throw new RRException("第 " + rowNum + " 行：" + label + "格式应为 " + pattern);
    }
}
```

状态/字典/外键：

- 状态：接受数字或规格中的 label，映射为 value；非法值抛带行号的 `RRException`。
- 字典：导入列写存储值（如 `0/1` 或编码），按选项校验。
- 外键：导入列写业务编码或 id；按目标表查询映射，找不到时抛「<label>不存在」。

## 6. Controller

**直接使用 `ExcelUtils`，不要继承 `com.athena.modules.sys.controller.AbstractController`**（它在 sys 包内，跨模块继承会引入不必要的耦合）。

```java
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
}
```

### 端点与权限

| # | 方法 | 路径 | 权限 | `@Log` |
|---|---|---|---|---|
| 1 | GET | `/list` | `:list` | 否 |
| 2 | GET | `/info/{id}` | `:info` | 否 |
| 3 | POST | `/save` | `:save` | 是 |
| 4 | PUT | `/update` | `:update` | 是 |
| 5 | DELETE | `/delete?id=` | `:delete` | 是 |
| 6 | DELETE | `/deleteBatch?ids=` | `:deleteBatch` | 是 |
| 7 | GET | `/export` | `:export` | 否 |
| 8 | GET | `/importTemplate` | `:import` | 否 |
| 9 | POST | `/import` | `:import` | 是 |

### 可选端点

仅当规格或页面需要时生成，且仍要加 `@SaCheckPermission`：

```java
@GetMapping("/select")
@SaCheckPermission("{{permissionPrefix}}:list")
public Result<List<BaseSelect>> select(@RequestParam(required = false) String keyword) {
    return Result.ok({{entity}}Service.listOptions(keyword));
}
```

`listOptions` 限制返回 50–100 条、按 label 排序、支持关键字过滤。

状态切换默认不生成（项目没有现成的状态切换模式）。如确需，单独设计部分更新接口，且**不要**用 `UpdateGroup` 校验整个表单。

## 7. Excel DTO

### 导入 DTO

- 所有列用 `String`，便于逐行报错。
- 只包含 `import != never` 的字段。
- 表头文案与字段规格一致，前端模板说明也用它。

```java
@Data
public class {{Entity}}ImportExcel implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @ColumnWidth(20)
    @ExcelProperty("名称")
    private String name;

    @ColumnWidth(12)
    @ExcelProperty("状态")
    private String status;
}
```

### 导出 DTO

- 数值/日期用强类型，字典/外键/状态用 `String` 文本。
- 只包含 `export: true` 的字段，追加 `ctime`。
- 不含敏感字段。

```java
@Data
public class {{Entity}}ExportExcel implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @ColumnWidth(20)
    @ExcelProperty("名称")
    private String name;

    @ColumnWidth(12)
    @ExcelProperty("状态")
    private String status;

    @ColumnWidth(22)
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    @ExcelProperty("创建时间")
    private Date ctime;
}
```

注解包名：`org.apache.fesod.sheet.annotation.ExcelProperty`、`org.apache.fesod.sheet.annotation.write.style.ColumnWidth`、`org.apache.fesod.sheet.annotation.format.DateTimeFormat`。

## 8. 字典白名单

`@Dict(dictTable=...)` 的表必须出现在：

```yaml
athena:
  dict:
    allowed-tables: sys_dept,sys_user,<targetTable>
```

用 `Edit` 追加，先检查是否已存在；不重复、不删除已有表名。

## 9. 禁止复制的用户管理逻辑

- 密码：`BCrypt`、`SaSecureUtil`、`PasswordUtils`、`RsaUtils`、`WRITE_ONLY`
- 角色：`roleIdList`、`SysUserRoleService`
- 部门：`deptId`、`SysDeptService`、部门树、子部门递归
- 安全：`Constant.SUPER_ADMIN`、`assertNotSuperAdmin`、`@UniqueUser`
- 用户资料：头像、性别、用户类型、改密、重置密码
