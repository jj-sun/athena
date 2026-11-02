# 字段规格与类型映射

## 1. 规格格式

优先使用 YAML。用户没有给 YAML 时，可以从 DDL 推断，或按表格收集。

```yaml
module: product
moduleLabel: 商品管理
entity: Product
entityKebab: product
entityLabel: 商品
tableName: product
route: /product/product
permissionPrefix: product:product
menu:
  parentId: "0"        # "0" = 新建一级目录；否则填已有 sys_permission.id
  icon: view-list
  orderNum: 10
fields:
  - name: productCode
    label: 商品编码
    type: string
    length: 64
    required: true
    unique: true
    query: like
    list: true
    form: true
    import: required
    export: true
```

字段必填项：`name`（lowerCamelCase）、`label`、`type`。其余按默认值：

| 属性 | 默认值 |
|---|---|
| `required` | `false` |
| `unique` | `false` |
| `query` | string → `like`；status/dict/foreignKey/boolean → `eq`；date/datetime → `between`；text → `none` |
| `list` | `true` |
| `form` | `true` |
| `import` | `required` if `required` else `optional`；`sensitive` → `never` |
| `export` | `true`；`sensitive` → `false` |

不要在 `fields` 中列出 `id`、`creator`、`ctime`、`editor`、`mtime`、`del_flag`。

## 2. 占位符

| 占位符 | 含义 | 示例 |
|---|---|---|
| `{{module}}` | 小写模块/包名 | `product` |
| `{{moduleLabel}}` | 模块中文名 | `商品管理` |
| `{{Entity}}` | PascalCase 实体 | `Product` |
| `{{entity}}` | lowerCamelCase 实体变量 | `product` |
| `{{entityKebab}}` | kebab-case 路由/文件名 | `product`、`product-category` |
| `{{entityLabel}}` | 实体中文名 | `商品` |
| `{{tableName}}` | 数据库表名 | `product` |
| `{{permissionPrefix}}` | 权限前缀 | `product:product` |
| `{{field}}` / `{{Field}}` | 字段名 lowerCamel / Pascal | `productCode` / `ProductCode` |
| `{{column}}` | 字段 snake_case 列名 | `product_code` |
| `{{getter}}` | 字段 getter 后缀 | `ProductCode` |
| `{{menuIcon}}` | 菜单图标 | `view-list` |
| `{{menuOrder}}` | 菜单排序 | `10` |
| `{{uniqueColumn}}` | 唯一索引列（snake_case） | `product_code` |
| `{{queryColumn}}` | 普通查询索引列（snake_case） | `status` |
| `{{dictCode}}` | 字典编码 | `product_type` |
| `{{targetTable}}` | 外键目标表 | `product_category` |
| `{{textColumn}}` | 外键目标显示列 | `category_name` |

## 3. 类型映射

| 规格类型 | Java 类型 | SQL 类型 | 表单控件 | 查询控件 | 列表 | 导入 DTO | 导出 DTO |
|---|---|---|---|---|---|---|---|
| `string` | `String` | `varchar(length)` | `t-input` | `t-input` | 文本 | String | String |
| `text` | `String` | `text` | `t-textarea` | 无 | 文本（省略） | String | String |
| `integer` | `Integer` | `int` | `t-input-number` | `t-input-number` | 数字 | String | Integer |
| `long`（业务） | `Long` | `bigint` | `t-input-number` | `t-input-number` | 数字 | String | Long |
| `long`（标识） | `String` | `varchar(32)` | `t-input` | `t-input` | 文本 | String | String |
| `decimal` | `BigDecimal` | `decimal(precision,scale)` | `t-input-number` | 两个输入框（between） | 数字 | String | BigDecimal |
| `boolean` | `Integer`（0/1） | `tinyint` | `t-switch :custom-value="[1,0]"` | `t-select` | `t-tag` | String | String（是/否） |
| `date` | `Date` + `@JsonFormat("yyyy-MM-dd")` | `date` | `t-date-picker` | 范围选择 | 日期 | String | Date + `@DateTimeFormat("yyyy-MM-dd")` |
| `datetime` | `Date` | `datetime` | `t-date-picker` + `enable-time-picker` | 范围选择 | 日期时间 | String | Date + `@DateTimeFormat("yyyy-MM-dd HH:mm:ss")` |
| `status` | `Integer` | `tinyint` | `t-select`（options） | `t-select` | `t-tag` + theme | String | String（label） |
| `dict` | `String` 或 `Integer`（`valueType`） | `varchar(32)` / `int` | `t-select`（options） | `t-select` | `{{field}}_dictText` | String → value | String（text） |
| `foreignKey` | `String` | `varchar(32)` | `t-select`（options/远程） | `t-select` | `{{field}}_dictText` | String → id | String（text） |

### 长整型 ID

雪花 ID、外键 ID 一律映射为 `String` / `varchar(32)`，避免 JavaScript 超过 `Number.MAX_SAFE_INTEGER` 后精度丢失。只有纯业务计数（如库存、次数）使用 `Long` / `number`。

### 日期

- `date`：实体加 `@JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")`。
- `datetime`：沿用项目默认 `yyyy-MM-dd HH:mm:ss`，可省略 `@JsonFormat`。
- Excel DTO：导入用 `String`，服务层用 `SimpleDateFormat` 解析；导出用 `Date` + `@DateTimeFormat`。
- 查询范围使用瞬态字段 `{{field}}Start` / `{{field}}End`，实体上加 `@TableField(exist = false)`，服务层用 `ge` / `le`。

## 4. 校验注解

| 类型 | 注解 |
|---|---|
| 必填 String | `@NotBlank(message = "{{label}}不能为空", groups = {AddGroup.class, UpdateGroup.class})` |
| 必填 Integer/Long/BigDecimal/Date | `@NotNull(...)` |
| 有长度上限的 String | `@Size(max = length, ...)` |
| decimal 下限 | `@DecimalMin(value = "0", ...)` |
| 邮箱/手机号等业务格式 | 仅当规格明确要求时加，不默认添加 |

所有校验注解都必须带 `groups = {AddGroup.class, UpdateGroup.class}`，否则 `ValidatorUtils.validateEntity(..., AddGroup.class)` 不会执行。

## 5. 字典与外键

固定状态/枚举：

```java
@Dict(dicCode = "product_status")
private Integer status;
```

外键（表字典）：

```java
@Dict(dicCode = "id", dicText = "category_name", dictTable = "product_category")
private String categoryId;
```

规则：

- `@Dict` 只影响 JSON 响应，追加 `<field>_dictText`；**不影响 Fesod 导入导出**，导出文本需在 service 手动解析。
- `dictTable` 必须在 `athena-boot/src/main/resources/application.yml` 的 `athena.dict.allowed-tables` 中，生成时用 `Edit` 追加，不要重复。
- 前端没有「按字典编码取项」的公共 API。表单/查询下拉选项必须来自：
  - 规格中的静态 `options`；或
  - 已有模块的 `/select` 接口；或
  - 本模块额外生成的 `/select` 接口（仅当规格明确要求）。
- `@Dict` 取不到文本时前端应回退显示原值。

## 6. 敏感字段

名字匹配 `password|passwd|secret|token|salt|private_key|id_card|bank_card` 的字段：

- 标记 `sensitive: true`
- `import: never`、`export: false`
- 导出 DTO 与导入 DTO 都不生成该列
- 如确需存储，实体中也不要在列表/详情返回明文；按具体业务另做脱敏

## 7. 从 DDL 推断

1. 解析列名、SQL 类型、`NOT NULL`、默认值、`COMMENT`、`UNIQUE KEY`、索引。
2. 忽略 `id/creator/ctime/editor/mtime/del_flag`。
3. SQL 类型映射：
   - `varchar/char` → `string`（取长度）
   - `text/longtext` → `text`
   - `int/smallint/tinyint` → `integer`；`tinyint(1)` 或 `is_*` → `boolean`
   - `bigint` → `long`；列名以 `_id` 结尾 → `string`（标识）
   - `decimal/numeric` → `decimal`
   - `date` → `date`；`datetime/timestamp` → `datetime`
4. `label`：优先用列 `COMMENT`；没有注释时用列名转可读文本，并在报告中标注「标签待确认」。
5. `required`：`NOT NULL` 且无默认值（审计字段除外）。
6. `unique`：列出现在 `UNIQUE KEY` 中。
7. `query`：
   - `name/title/code/*_name/*_code` → `like`
   - `status/state/type/*_id/boolean` → `eq`
   - `*_date/*_time/*_at` → `between`
   - `remark/description/content` → `none`
8. `sensitive`：按第 6 节匹配。
9. 外键：`*_id` 且不是主键 → `foreignKey`。目标表在 `docs/athena_boot.sql` 中可推断 `dictTable`/`dicText`；否则向用户确认。

推断完成后输出干跑表：

| 字段 | 名称 | 类型 | Java | SQL | 查询 | 表单 | 导入 | 导出 |
|---|---|---|---|---|---|---|---|---|
| productCode | 商品编码 | string | String | varchar(64) | like | input | 必填 | 是 |

## 8. 规格自检

- 每个 `status/dict/boolean/foreignKey` 都有可用的 `options` 或目标接口。
- 没有重复 `name`。
- `query: between` 的字段是 date/datetime/decimal/integer。
- `unique: true` 的字段类型适合建唯一索引。
- `import: required` 的字段不应同时是 `sensitive`。
- 至少有一个 `list: true` 字段，否则页面无列可展示。
