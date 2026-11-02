-- ============================================================================
-- {{moduleLabel}} / {{entityLabel}} 初始化脚本
-- 目标库：athena_boot
-- 注意：一次性脚本。重复执行会产生重复菜单；执行前请先跑下面的预检查。
-- ============================================================================

-- 预检查：应返回 0 行
SELECT id, name, url
FROM sys_permission
WHERE url IN ('/{{module}}', '/{{module}}/{{entityKebab}}');

-- ----------------------------------------------------------------------------
-- 1. 业务表
-- ----------------------------------------------------------------------------
CREATE TABLE `{{tableName}}` (
  `id` varchar(32) NOT NULL COMMENT '主键',

  -- <GENERATE: 每个业务字段一行；列名 snake_case，类型按 field-spec 映射>
  `name` varchar(128) NOT NULL COMMENT '名称',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态',
  `category_id` varchar(32) DEFAULT NULL COMMENT '分类ID',
  `business_date` date DEFAULT NULL COMMENT '业务日期',
  `amount` decimal(12,2) DEFAULT NULL COMMENT '金额',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',

  `creator` varchar(32) DEFAULT NULL COMMENT '创建人',
  `ctime` datetime DEFAULT NULL COMMENT '创建时间',
  `editor` varchar(32) DEFAULT NULL COMMENT '修改人',
  `mtime` datetime DEFAULT NULL COMMENT '修改时间',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除标记',
  PRIMARY KEY (`id`),

  -- <GENERATE: 仅为 unique: true 的字段生成>
  UNIQUE KEY `uk_{{tableName}}_{{uniqueColumn}}` (`{{uniqueColumn}}`),

  -- <GENERATE: 仅为常用 eq 查询列生成>
  KEY `idx_{{tableName}}_{{queryColumn}}` (`{{queryColumn}}`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
  COMMENT='{{entityLabel}}';

-- ----------------------------------------------------------------------------
-- 2. 菜单与按钮权限
-- ----------------------------------------------------------------------------
SET @now = NOW();
SET @dirId            = REPLACE(UUID(), '-', '');
SET @menuId           = REPLACE(UUID(), '-', '');
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

-- 菜单（list 权限挂在菜单行）
INSERT INTO `sys_permission`
(`id`,`parent_id`,`name`,`url`,`component`,`frame_src`,`frame_blank`,`perms`,`redirect`,
 `type`,`icon`,`hidden`,`order_num`,`creator`,`ctime`,`editor`,`mtime`,`del_flag`)
VALUES
(@menuId, @dirId, '{{entityLabel}}', '/{{module}}/{{entityKebab}}',
 '{{module}}/{{entityKebab}}-list', NULL, NULL,
 '{{permissionPrefix}}:list', NULL, 1, '{{menuIcon}}', 1, 0,
 'admin', @now, 'admin', @now, 0);

-- 按钮权限（7 条）
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

-- 管理员授权（admin 在 Sa-Token 中有 *，这里仍写入以保持权限管理页面一致）
INSERT INTO `sys_role_permission` (`id`,`role_id`,`permission_id`)
SELECT REPLACE(UUID(), '-', ''), @adminRoleId, id
FROM `sys_permission`
WHERE `id` IN (@dirId, @menuId, @btnInfoId, @btnSaveId, @btnUpdateId,
               @btnDeleteId, @btnDeleteBatchId, @btnImportId, @btnExportId);

-- 可选：操作员角色授权（存在 role_code='operate' 时取消注释）
-- SET @operatorRoleId = (SELECT id FROM `sys_role` WHERE `role_code` = 'operate' AND `del_flag` = 0 LIMIT 1);
-- INSERT INTO `sys_role_permission` (`id`,`role_id`,`permission_id`)
-- SELECT REPLACE(UUID(), '-', ''), @operatorRoleId, id
-- FROM `sys_permission`
-- WHERE `id` IN (@dirId, @menuId, @btnInfoId, @btnSaveId, @btnUpdateId,
--                @btnDeleteId, @btnDeleteBatchId, @btnImportId, @btnExportId);

-- ----------------------------------------------------------------------------
-- 3. 执行后检查
-- ----------------------------------------------------------------------------
SELECT id, parent_id, name, url, component, perms, type, hidden, order_num
FROM sys_permission
WHERE url IN ('/{{module}}', '/{{module}}/{{entityKebab}}')
   OR perms LIKE '{{permissionPrefix}}:%'
ORDER BY type, order_num;
