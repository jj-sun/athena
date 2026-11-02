---
name: athena-crud
description: >-
  为 Athena 仓库生成完整的全栈业务模块：Spring Boot + MyBatis-Plus + Sa-Token 后端、
  Vue 3 + TDesign 列表/表单页、Apache Fesod Excel 导入导出、建表与菜单权限 SQL。
  以用户管理为蓝本，但不包含左侧部门树。当用户要求“新增业务模块 / 生成增删改查 /
  新增管理页面 / 生成导入导出 / scaffold a CRUD module / add an admin page”时使用。
user-invocable: true
allowed-tools: Read, Grep, Glob, Bash, Write, Edit, AskUserQuestion
---

# Athena 全栈业务模块生成器

按「用户管理」的骨架生成新业务模块：分页查询、新增、编辑、单条/批量删除、Excel 导入导出，以及建表与菜单权限 SQL。**不要复制用户管理左侧的部门树、角色选择、改密、超管保护等用户专属逻辑。**

参考实现（只读，用于核对细节）：

- 后端：`athena-boot/src/main/java/com/athena/modules/sys/controller/SysUserController.java`、`service/impl/SysUserServiceImpl.java`、`entity/SysUser.java`、`dto/SysUserImportExcel.java`、`dto/SysUserExportExcel.java`
- Excel：`athena-boot/src/main/java/com/athena/common/excel/ExcelUtils.java`
- 前端：`athena-vue-tdesign/src/views/system/user-list.vue`、`src/api/system/user.ts`、`src/types/system/index.ts`
- 单列页面参考：`src/views/system/dict-list.vue`
- 菜单种子：`docs/athena_boot.sql` 的 `sys_permission` / `sys_role_permission`
- 迁移目录：`docs/migration/`

## 0. 开始前必须阅读

按顺序读取：

1. `references/field-spec.md` — 字段规格、DDL 推断、类型映射
2. `references/backend-patterns.md` — 后端分层与注解
3. `references/frontend-patterns.md` — 页面结构与交互
4. `references/sql-menu-seed.md` — DDL 与菜单权限
5. `references/verification.md` — 构建与冒烟测试
6. `assets/spec.example.yaml` — 完整输入示例模板
7. 需要时读取 `templates/` 下对应模板

## 1. 适用与不适用

适用：新增一张业务表，需要标准 CRUD + Excel 导入导出 + 菜单权限。

不适用：修改 `sys_*` 系统表、只给现有模块加一个接口、审批流/工作流、纯查询报表。遇到不适用场景先说明，不要强行生成。

## 2. 收集输入

一次问清缺失信息，不要逐项反复打断：

- `module`：小写、无连字符，如 `product`
- `moduleLabel`：模块中文名，如 `商品管理`
- `Entity` / `entity` / `entityKebab` / `entityLabel`：如 `Product` / `product` / `product` / `商品`
- `tableName`：如 `product`
- `route`：默认 `/<module>/<entityKebab>`
- `permissionPrefix`：默认 `<module>:<entity>`
- 菜单：`parentId`（默认 `"0"` 新建一级目录）、图标（默认 `view-list`）、排序（默认 `10`）
- 字段清单（格式见下）
- 唯一字段；字典/外键元数据；是否需要本模块的 `/select` 选项接口

字段规格优先用 YAML（完整示例见 `assets/spec.example.yaml`）：

```yaml
fields:
  - name: productCode
    label: 商品编码
    type: string          # string|text|integer|long|decimal|boolean|date|datetime|status|dict|foreignKey
    length: 64
    required: true
    unique: true
    query: like           # like|eq|between|none
    list: true            # 是否列表展示
    form: true            # 是否出现在新增/编辑表单
    import: required      # required|optional|never
    export: true
    options:              # status/dict/boolean 必填，如 0=停用,1=启用
      - { label: 启用, value: 1, theme: success }
      - { label: 停用, value: 0, theme: default }
```

如果用户只给出 `CREATE TABLE` DDL：

1. 按 `references/field-spec.md` 的算法推断字段规格。
2. 先输出一张干跑表（字段 → Java/SQL/表单/查询/导入导出）。
3. 用户明确说「直接生成」时按推断结果继续，并在最终报告中列出所有假设。

## 3. 生成流程

### 3.1 预检

- `Glob` 检查后端包 `athena-boot/src/main/java/com/athena/modules/<module>/**`、前端 `src/views/<module>/**`、`src/api/<module>/**`。
- `Grep` 检查路由 `/<module>/<entityKebab>` 与权限码 `<permissionPrefix>:` 是否已存在。
- 存在冲突时停下询问：扩展现有模块还是换名。**不得静默覆盖任何已有文件。**
- 确认 JDK 21 可用于构建（见 `references/verification.md`）。

### 3.2 生成顺序

1. SQL DDL → `docs/migration/<yyyyMMdd>_<module>_<entity>.sql`
2. 后端 8 个文件（XML 仅在需要自定义 SQL 时生成）
3. 前端 4 个文件
4. 把菜单/按钮/角色授权 SQL 追加到同一个迁移文件
5. 如使用 `@Dict(dictTable=...)`，把目标表追加到 `athena.dict.allowed-tables`
6. 构建验证
7. 交付报告

### 3.3 文件映射

后端根：`athena-boot/src/main/java/com/athena/modules/<module>/`

| 模板 | 目标 |
|---|---|
| `templates/backend/Entity.java.tpl` | `entity/<Entity>.java` |
| `templates/backend/Mapper.java.tpl` | `mapper/<Entity>Mapper.java` |
| `templates/backend/Mapper.xml.tpl` | `mapper/xml/<Entity>Mapper.xml`（可选） |
| `templates/backend/ImportExcel.java.tpl` | `dto/<Entity>ImportExcel.java` |
| `templates/backend/ExportExcel.java.tpl` | `dto/<Entity>ExportExcel.java` |
| `templates/backend/Service.java.tpl` | `service/<Entity>Service.java` |
| `templates/backend/ServiceImpl.java.tpl` | `service/impl/<Entity>ServiceImpl.java` |
| `templates/backend/Controller.java.tpl` | `controller/<Entity>Controller.java` |

前端根：`athena-vue-tdesign/src/`

| 模板 | 目标 |
|---|---|
| `templates/frontend/types.ts.tpl` | `types/<module>/index.ts`（已存在则只追加接口） |
| `templates/frontend/api.ts.tpl` | `api/<module>/<entityKebab>.ts` |
| `templates/frontend/constants.ts.tpl` | `views/<module>/constants.ts`（仅固定枚举/状态） |
| `templates/frontend/list.vue.tpl` | `views/<module>/<entityKebab>-list.vue` |

SQL：`templates/sql/module.sql.tpl` → `docs/migration/<yyyyMMdd>_<module>_<entity>.sql`

全部模板使用 `{{...}}` 占位符。**模板中的 `name/status/categoryId/businessDate/amount/remark/type/happenedAt` 等示例字段只用于展示写法；生成时必须按字段规格替换或删除，不能把规格之外的示例字段留在结果里。** 同时删除所有 `<GENERATE: ...>`、`<OPTIONAL: ...>` 注释。

替换完成后执行精确检查（不要用裸 `grep "{{"`，Vue 模板插值 `{{ row.name }}` 会误报）：

```bash
grep -REn "\{\{(module|moduleLabel|Entity|entity|entityKebab|entityLabel|tableName|permissionPrefix|field|Field|column|getter|label|menuIcon|menuOrder|uniqueColumn|queryColumn|dictCode|targetTable|textColumn)\}\}" <生成的业务文件>
```

必须无输出。

## 4. 命名规则

| 项 | 规则 | 示例 |
|---|---|---|
| 后端包 | `com.athena.modules.<module>` | `com.athena.modules.product` |
| Controller 路径 | `/<module>/<entity>` | `/product/product` |
| 权限码 | `<module>:<entity>:<action>` | `product:product:list` |
| 前端路由 | `/<module>/<entityKebab>` | `/product/product` |
| 菜单 component | `<module>/<entityKebab>-list` | `product/product-list` |
| 前端页面 | `src/views/<module>/<entityKebab>-list.vue` | `src/views/product/product-list.vue` |
| 前端 API | `src/api/<module>/<entityKebab>.ts` | `src/api/product/product.ts` |
| 表名 | 默认 `<entity snake_case>` | `product` |

权限动作固定 8 个：`list`、`info`、`save`、`update`、`delete`、`deleteBatch`、`import`、`export`。菜单行承载 `list`，另有 7 个按钮行。

## 5. 硬性约束

- 页面单列，不含 `t-row`/`t-col` 双栏、部门树、`dept-select`、角色选择、头像、改密/重置密码。
- 不复制密码/BCrypt/RSA、`roleIdList`、`deptId`、`@UniqueUser`、超管 `admin` 保护。
- 不新增前端 `xlsx`/`exceljs`/`file-saver`；不新增后端 EasyExcel/POI。Excel 只用已引入的 Fesod。
- `del_flag` 只是状态字段，不是逻辑删除；删除用 `removeById`/`removeByIds`，禁止 `@TableLogic`。
- 每个 controller 方法必须有 `@SaCheckPermission`；所有写操作必须有 `@Log`。
- 列表与导出共用 `buildQueryWrapper`，避免查询条件漂移。
- 导出上限 50000 行；所有导出文本做公式注入转义。
- 导入上限 5000 行；`@Transactional(rollbackFor = Exception.class)`；跳过空行；行号 `i + 2`；聚合 `第 N 行：...` 错误；全量校验通过后再保存。
- `@Dict(dictTable=...)` 的表必须登记到 `athena.dict.allowed-tables`，否则 `_dictText` 不会出现。
- 不修改 `docs/athena_boot.sql`、`src/router/modules/asyncRoutes.ts`、`pom.xml`、`package.json`。
- 不覆盖已存在文件；`src/types/<module>/index.ts` 已存在时只追加缺失接口。

## 6. 验证

按 `references/verification.md` 执行：

```bash
export JAVA_HOME=/d/sdk/jdk/jdk-21.0.5+11
export PATH="$JAVA_HOME/bin:$PATH"
mvn -f /e/github/athena/athena-boot/pom.xml clean package
```

```bash
cd /e/github/athena/athena-vue-tdesign
npm run build
npx eslint src/views/<module> src/api/<module> src/types/<module> --max-warnings 0
```

构建失败必须修复后重跑，不得把失败结果报告为成功。

## 7. 交付报告

报告必须包含：

- 生成的文件清单（绝对路径）
- 迁移 SQL 路径、执行顺序与「需重新登录刷新菜单」提示
- 对 `application.yml` 的改动（如有）
- 推断出的字段假设（如有）
- 构建与 lint 结果
- 手动冒烟步骤
- 已知限制（如需额外种子字典数据）

## 8. 相关文件

- `references/field-spec.md`
- `references/backend-patterns.md`
- `references/frontend-patterns.md`
- `references/sql-menu-seed.md`
- `references/verification.md`
- `assets/spec.example.yaml`
