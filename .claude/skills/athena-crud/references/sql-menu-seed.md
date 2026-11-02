# SQL 与菜单权限规范

## 1. 文件位置

生成到：

```
docs/migration/<yyyyMMdd>_<module>_<entity>.sql
```

例如 `docs/migration/20261102_product_product.sql`。与现有 `docs/migration/20261030_sys_user_dept_id.sql` 保持一致。

**不要修改 `docs/athena_boot.sql`**（它是结构参考/初始化 dump）。迁移脚本不自动执行，交付时提示用户自行在 MySQL `athena_boot` 中执行。

## 2. DDL

```sql
-- 一次性迁移脚本；执行前请确认表与菜单不存在，重复执行会产生重复菜单。
-- 预检查：
SELECT id, name, url FROM sys_permission
WHERE url IN ('/{{module}}', '/{{module}}/{{entityKebab}}');

CREATE TABLE `{{tableName}}` (
  `id` varchar(32) NOT NULL COMMENT '主键',
  -- <GENERATE: 每个业务字段一行，列名 snake_case，类型按 field-spec 映射>
  `name` varchar(128) NOT NULL COMMENT '名称',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态',
  `category_id` varchar(32) DEFAULT NULL COMMENT '分类ID',
  `publish_date` date DEFAULT NULL COMMENT '发布日期',
  `price` decimal(12,2) DEFAULT NULL COMMENT '价格',
  `description` text COMMENT '描述',
  `creator` varchar(32) DEFAULT NULL COMMENT '创建人',
  `ctime` datetime DEFAULT NULL COMMENT '创建时间',
  `editor` varchar(32) DEFAULT NULL COMMENT '修改人',
  `mtime` datetime DEFAULT NULL COMMENT '修改时间',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除标记',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_{{tableName}}_{{uniqueColumn}}` (`{{uniqueColumn}}`),
  KEY `idx_{{tableName}}_{{queryColumn}}` (`{{queryColumn}}`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
  COMMENT='{{entityLabel}}';
```

规则：

- `id` 固定 `varchar(32) NOT NULL`。
- 外键列固定 `varchar(32)`。
- `creator/ctime/editor/mtime/del_flag` 必须存在，与 `BasePo` 对应。
- `del_flag` 默认 `0`；它只是审计/状态字段，业务删除仍是物理删除。
- 仅为 `unique: true` 的字段生成 `UNIQUE KEY`。
- 常用 `query: eq` 的列可加普通索引；不要为每个字段都加。
- `text` 不要设默认值。
- 敏感字段如确需存储，列名明确，且不进入导入/导出 DTO。

## 3. 菜单与权限

`sys_permission` 没有 `sys_menu` 表。字段含义：

| 字段 | 含义 |
|---|---|
| `id` | varchar(32) 主键 |
| `parent_id` | 父节点；一级节点为 `'0'` |
| `name` | 菜单/按钮名称 |
| `url` | 前端路由 |
| `component` | `src/views` 下无 `.vue` 的路径；目录用 `layout` |
| `frame_src/frame_blank` | 外链相关，普通菜单传 NULL |
| `perms` | 权限码，多个用逗号分隔 |
| `type` | 0 目录，1 菜单，2 按钮 |
| `icon` | TDesign 图标名 |
| `hidden` | 0 隐藏，1 显示 |
| `order_num` | 排序 |
| `del_flag` | 0 正常 |

`sys_role_permission`：`id`、`role_id`、`permission_id`。

### 完整模板

```sql
SET @now = NOW();
SET @dirId  = REPLACE(UUID(), '-', '');
SET @menuId = REPLACE(UUID(), '-', '');
SET @btnInfoId        = REPLACE(UUID(), '-', '');
SET @btnSaveId        = REPLACE(UUID(), '-', '');
SET @btnUpdateId      = REPLACE(UUID(), '-', '');
SET @btnDeleteId      = REPLACE(UUID(), '-', '');
SET @btnDeleteBatchId = REPLACE(UUID(), '-', '');
SET @btnImportId      = REPLACE(UUID(), '-', '');
SET @btnExportId      = REPLACE(UUID(), '-', '');

-- 已核对：admin 角色 id（role_code='admin'）
SET @adminRoleId = '1485529471914319874';

-- 目录（新建一级菜单）
INSERT INTO `sys_permission`
(`id`,`parent_id`,`name`,`url`,`component`,`frame_src`,`frame_blank`,`perms`,`redirect`,
 `type`,`icon`,`hidden`,`order_num`,`creator`,`ctime`,`editor`,`mtime`,`del_flag`)
VALUES
(@dirId, '0', '{{moduleLabel}}', '/{{module}}', 'layout', NULL, NULL, NULL, NULL,
 0, '{{menuIcon}}', 1, {{menuOrder}}, 'admin', @now, 'admin', @now, 0);

-- 菜单
INSERT INTO `sys_permission`
(`id`,`parent_id`,`name`,`url`,`component`,`frame_src`,`frame_blank`,`perms`,`redirect`,
 `type`,`icon`,`hidden`,`order_num`,`creator`,`ctime`,`editor`,`mtime`,`del_flag`)
VALUES
(@menuId, @dirId, '{{entityLabel}}', '/{{module}}/{{entityKebab}}',
 '{{module}}/{{entityKebab}}-list', NULL, NULL,
 '{{permissionPrefix}}:list', NULL, 1, '{{menuIcon}}', 1, 0,
 'admin', @now, 'admin', @now, 0);

-- 按钮权限（7 条；加上菜单行的 list，共 8 个权限码）
INSERT INTO `sys_permission`
(`id`,`parent_id`,`name`,`url`,`component`,`frame_src`,`frame_blank`,`perms`,`redirect`,
 `type`,`icon`,`hidden`,`order_num`,`creator`,`ctime`,`editor`,`mtime`,`del_flag`)
VALUES
(@btnInfoId,        @menuId, '{{entityLabel}}详情',     NULL, NULL, NULL, NULL, '{{permissionPrefix}}:info',        NULL, 2, NULL, 1, 0, 'admin', @now, 'admin', @now, 0),
(@btnSaveId,        @menuId, '{{entityLabel}}新增',     NULL, NULL, NULL, NULL, '{{permissionPrefix}}:save',        NULL, 2, NULL, 1, 1, 'admin', @now, 'admin', @now, 0),
(@btnUpdateId,      @menuId, '{{entityLabel}}修改',     NULL, NULL, NULL, NULL, '{{permissionPrefix}}:update',      NULL, 2, NULL, 1, 2, 'admin', @now, 'admin', @now, 0),
(@btnDeleteId,      @menuId, '{{entityLabel}}删除',     NULL, NULL, NULL, NULL, '{{permissionPrefix}}:delete',      NULL, 2, NULL, 1, 3, 'admin', @now, 'admin', @now, 0),
(@btnDeleteBatchId, @menuId, '{{entityLabel}}批量删除', NULL, NULL, NULL, NULL, '{{permissionPrefix}}:deleteBatch', NULL, 2, NULL, 1, 4, 'admin', @now, 'admin', @now, 0),
(@btnImportId,      @menuId, '{{entityLabel}}导入',     NULL, NULL, NULL, NULL, '{{permissionPrefix}}:import',      NULL, 2, NULL, 1, 5, 'admin', @now, 'admin', @now, 0),
(@btnExportId,      @menuId, '{{entityLabel}}导出',     NULL, NULL, NULL, NULL, '{{permissionPrefix}}:export',      NULL, 2, NULL, 1, 6, 'admin', @now, 'admin', @now, 0);

-- 管理员授权
INSERT INTO `sys_role_permission` (`id`,`role_id`,`permission_id`)
SELECT REPLACE(UUID(), '-', ''), @adminRoleId, id
FROM `sys_permission`
WHERE `id` IN (@dirId, @menuId, @btnInfoId, @btnSaveId, @btnUpdateId,
               @btnDeleteId, @btnDeleteBatchId, @btnImportId, @btnExportId);

-- 可选：操作员角色授权（存在该角色时执行）
-- SET @operatorRoleId = (SELECT id FROM `sys_role` WHERE `role_code` = 'operate' AND `del_flag` = 0 LIMIT 1);
-- INSERT INTO `sys_role_permission` (`id`,`role_id`,`permission_id`)
-- SELECT REPLACE(UUID(), '-', ''), @operatorRoleId, id
-- FROM `sys_permission`
-- WHERE `id` IN (@dirId, @menuId, @btnInfoId, @btnSaveId, @btnUpdateId,
--                @btnDeleteId, @btnDeleteBatchId, @btnImportId, @btnExportId);
```

## 4. 规则

- 一级目录 `parent_id='0'`；如需挂到已有目录，先按 URL/名称查出真实 id，**不要硬编码** `1475702165283561473`。
- `component` 必须精确等于 `src/views` 下无 `.vue` 的路径，例如 `product/product-list`。路径写错会落到 500 页面。
- `hidden=1` 表示显示；`hidden=0` 表示隐藏。
- 菜单行 `perms` 承载 `:list`；按钮行承载其余 7 个权限码。
- 默认只给 admin 授权。非管理员角色需要在菜单管理页面手工勾选，或取消注释 `operate` 授权块。
- 管理员在 Sa-Token 中拥有 `*`，但 `sys_role_permission` 仍要写入，以保证权限管理页面和非管理员角色一致。
- 用 `UUID()` 去掉连字符生成 32 位 id，避免与雪花 id 冲突。
- 脚本是一次性的；重复执行会产生重复菜单。头部保留预检查与警告注释。
- 执行后需要重新登录或刷新 `/sys/permission/nav`，新菜单才会出现。

## 5. 验证查询

```sql
-- 菜单树
SELECT id, parent_id, name, url, component, perms, type, hidden, order_num
FROM sys_permission
WHERE url IN ('/{{module}}', '/{{module}}/{{entityKebab}}')
   OR perms LIKE '{{permissionPrefix}}:%'
ORDER BY type, order_num;

-- 管理员授权
SELECT p.name, p.perms
FROM sys_role_permission rp
JOIN sys_permission p ON p.id = rp.permission_id
WHERE rp.role_id = '1485529471914319874'
  AND (p.url IN ('/{{module}}', '/{{module}}/{{entityKebab}}')
       OR p.perms LIKE '{{permissionPrefix}}:%');
```
