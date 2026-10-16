-- ============================================================================
-- Athena 安全加固 —— 权限码补齐脚本
--
-- 背景
--   后端为 44 个接口补上了 @SaCheckPermission 鉴权。权限码必须真实存在于
--   sys_permission 表中，否则对应接口会对所有人（含非 admin 角色）返回 403。
--   本脚本用于：探测现状 → 幂等补齐缺失权限码 → 授权 → 验证。
--
-- 重要
--   * 用户名为 admin 的账号因 SaTokenStpInterfaceImpl 中的 "*" 通配始终拥有全部权限，
--     所以即使漏了某个码，管理入口也不会被锁死；受影响的是其他角色。
--   * 但sys_user / sys_role 等已存在的接口不受影响，只有本次新增的码需要补齐。
--   * 执行前请备份 sys_permission 与 sys_role_permission 两张表。
--   * 脚本按 A → B → C → D 顺序执行，每段都要核对输出后再继续。
--   * 全脚本幂等，可重复执行。
-- ============================================================================


-- ============================================================================
-- A. 探测现状（只读，先跑这一段并把输出留存）
-- ============================================================================

-- A1. 现有全部权限/菜单节点
SELECT id, parent_id, name, type, perms, order_num, del_flag
FROM sys_permission
ORDER BY parent_id, order_num;

-- A2. 现有角色
SELECT id, role_name, role_code FROM sys_role;

-- A3. 各角色当前被授予的权限行数
SELECT role_id, COUNT(*) AS perm_count FROM sys_role_permission GROUP BY role_id;

-- A4. 找出「系统管理」目录节点，后面作为新增按钮权限的挂载点。
--     若查询为空，请改为手工从 A1 的结果里挑一个合适的目录节点 id。
SELECT id, name, perms FROM sys_permission
WHERE name = '系统管理' AND (parent_id = '0' OR parent_id IS NULL);


-- ============================================================================
-- B. 补齐缺失的权限码（幂等）
-- ============================================================================

-- B1. 本次新增（或可能缺失）的权限码清单，并指明挂在哪个菜单下。
--     按钮权限必须挂在 type=1 的菜单节点上，才能出现在「角色-权限树」里。
--     下面使用的菜单名称取自本库现有数据（用户管理/角色管理/菜单管理/字典管理/
--     部门管理/日志管理/定时任务/服务器信息）。若你的库中菜单名不同，请按 A1 的输出调整。
DROP TEMPORARY TABLE IF EXISTS tmp_perm_codes;
CREATE TEMPORARY TABLE tmp_perm_codes (
  perms       VARCHAR(255) NOT NULL PRIMARY KEY,
  name        VARCHAR(100) NOT NULL,
  parent_name VARCHAR(100) NOT NULL,
  order_num   INT          NOT NULL
);

INSERT INTO tmp_perm_codes (perms, name, parent_name, order_num) VALUES
  -- 用户管理
  ('sys:user:info',            '用户详情',     '用户管理', 130),
  ('sys:user:resetPassword',   '重置用户密码', '用户管理', 131),
  -- 角色管理
  ('sys:role:list',            '角色列表',     '角色管理', 100),
  ('sys:role:select',          '角色选择',     '角色管理', 101),
  ('sys:role:info',            '角色详情',     '角色管理', 102),
  ('sys:role:save',            '角色新增',     '角色管理', 103),
  ('sys:role:update',          '角色修改',     '角色管理', 104),
  ('sys:role:delete',          '角色删除',     '角色管理', 105),
  -- 菜单管理
  ('sys:menu:list',            '菜单列表',     '菜单管理', 110),
  ('sys:menu:select',          '菜单选择',     '菜单管理', 111),
  ('sys:menu:info',            '菜单详情',     '菜单管理', 112),
  ('sys:menu:save',            '菜单新增',     '菜单管理', 113),
  ('sys:menu:update',          '菜单修改',     '菜单管理', 114),
  ('sys:menu:delete',          '菜单删除',     '菜单管理', 115),
  -- 部门管理（原代码里这几个接口复用了 sys:user:* / sys:menu:*，现改为模块自洽的命名）
  ('sys:dept:list',            '部门列表',     '部门管理', 120),
  ('sys:dept:select',          '部门选择',     '部门管理', 121),
  ('sys:dept:info',            '部门详情',     '部门管理', 122),
  ('sys:dept:save',            '部门新增',     '部门管理', 123),
  ('sys:dept:update',          '部门修改',     '部门管理', 124),
  ('sys:dept:delete',          '部门删除',     '部门管理', 125),
  -- 字典管理
  ('sys:dict:list',            '字典列表',     '字典管理', 130),
  ('sys:dict:info',            '字典详情',     '字典管理', 131),
  ('sys:dict:save',            '字典新增',     '字典管理', 132),
  ('sys:dict:update',          '字典修改',     '字典管理', 133),
  ('sys:dict:delete',          '字典删除',     '字典管理', 134),
  ('sys:dictItem:list',        '字典项列表',   '字典管理', 135),
  ('sys:dictItem:info',        '字典项详情',   '字典管理', 136),
  ('sys:dictItem:save',        '字典项新增',   '字典管理', 137),
  ('sys:dictItem:update',      '字典项修改',   '字典管理', 138),
  ('sys:dictItem:delete',      '字典项删除',   '字典管理', 139),
  -- 日志管理
  ('sys:log:list',             '日志列表',     '日志管理', 140),
  -- 定时任务
  ('sys:schedule:list',        '任务列表',     '定时任务', 150),
  ('sys:schedule:info',        '任务详情',     '定时任务', 151),
  ('sys:schedule:save',        '任务新增',     '定时任务', 152),
  ('sys:schedule:update',      '任务修改',     '定时任务', 153),
  ('sys:schedule:delete',      '任务删除',     '定时任务', 154),
  ('sys:schedule:run',         '任务执行',     '定时任务', 155),
  ('sys:schedule:log',         '任务日志',     '定时任务', 156),
  -- 服务器信息（监控）
  ('sys:monitor:metrics',      '系统指标',     '服务器信息', 160),
  ('sys:monitor:redis',        'Redis 监控',   '服务器信息', 161);

-- B2. 只插入确实不存在的权限码，父节点按菜单名匹配。
--     注意 1：sys_permission.perms 允许逗号分隔多个码，因此不能只做等值比较，
--             必须同时用 FIND_IN_SET 判断该码是否已包含在某行的逗号列表中。
--     注意 2：主键为 ASSIGN_ID（雪花字符串），数据库不会自增，必须自己给值。
--     注意 3：父菜单名若匹配不到，该行不会被插入（JOIN 失败），D1 会暴露出来。
INSERT INTO sys_permission (id, parent_id, name, type, perms, order_num, del_flag, creator, ctime)
SELECT REPLACE(UUID(), '-', ''), parent.id, t.name, 2, t.perms, t.order_num, 0, 'security-hardening', NOW()
FROM tmp_perm_codes t
JOIN sys_permission parent ON parent.name = t.parent_name AND parent.type = 1
WHERE NOT EXISTS (
  SELECT 1 FROM sys_permission p
  WHERE p.perms = t.perms
     OR FIND_IN_SET(t.perms, REPLACE(p.perms, ' ', '')) > 0
);

-- B3. 确认插入结果
SELECT id, parent_id, name, type, perms, order_num
FROM sys_permission
WHERE creator = 'security-hardening'
ORDER BY parent_id, order_num;


-- ============================================================================
-- C. 授权
-- ============================================================================

-- C1. 指定需要这些权限码的角色（按 role_code，逗号分隔），例如 'admin,operate'。
--     用户名为 admin 的账号无需授权（拥有 "*" 通配）。
--     其余角色在授权前会失去对应菜单的访问权 —— 这是本次加固的预期结果。
SET @grant_role_codes = 'admin';

-- C2. 把本次涉及的权限码授予指定角色（跳过已存在的授权关系）
INSERT INTO sys_role_permission (id, role_id, permission_id)
SELECT REPLACE(UUID(), '-', ''), r.id, p.id
FROM sys_role r
JOIN sys_permission p ON p.creator = 'security-hardening'
WHERE FIND_IN_SET(r.role_code, @grant_role_codes) > 0
  AND NOT EXISTS (
    SELECT 1 FROM sys_role_permission rp
    WHERE rp.role_id = r.id AND rp.permission_id = p.id
  );

-- ----------------------------------------------------------------------------
-- C-备选. 行为中性授权（保持上线前后“每个人的有效权限不变”）
--
-- 本次把部门/字典接口原本误用的旧权限码替换成了新码。如果希望这些接口的可见性与
-- 上线前完全一致，可以改为：凡是当前持有“旧码”的角色，自动获得对应的“新码”。
-- 旧码对应关系（来自原代码里被注释掉的行）：
--     sys:dept:*     ← sys:user:list / sys:user:info / sys:user:save / sys:user:update / sys:user:delete
--     sys:dict:*     ← sys:menu:list / sys:menu:info / sys:menu:save / sys:menu:update / sys:menu:delete
--     sys:dictItem:* ← sys:user:list / sys:user:info / sys:user:save / sys:user:update / sys:user:delete
--
-- 注意：这样做会把“旧码权限过宽”的问题一并继承下来，不推荐。默认请使用 C1+C2 显式授权。
-- ----------------------------------------------------------------------------


-- ============================================================================
-- D. 验证（上线带注解的代码之前必须全部通过）
-- ============================================================================

-- D1. 代码中要求的所有权限码，是否都已存在？
--     下面列出的是当前代码里出现的全部 @SaCheckPermission 权限码。
--     本查询应返回 0 行；若有输出，说明这些码仍然缺失，需回到 B 段处理。
SELECT required.perms AS missing_perms
FROM (
  SELECT 'sys:dept:delete' AS perms UNION ALL SELECT 'sys:dept:info'        UNION ALL
  SELECT 'sys:dept:list'            UNION ALL SELECT 'sys:dept:save'        UNION ALL
  SELECT 'sys:dept:select'          UNION ALL SELECT 'sys:dept:update'      UNION ALL
  SELECT 'sys:dict:delete'          UNION ALL SELECT 'sys:dict:info'        UNION ALL
  SELECT 'sys:dict:list'            UNION ALL SELECT 'sys:dict:save'        UNION ALL
  SELECT 'sys:dict:update'          UNION ALL SELECT 'sys:dictItem:delete'  UNION ALL
  SELECT 'sys:dictItem:info'        UNION ALL SELECT 'sys:dictItem:list'    UNION ALL
  SELECT 'sys:dictItem:save'        UNION ALL SELECT 'sys:dictItem:update'  UNION ALL
  SELECT 'sys:log:list'             UNION ALL SELECT 'sys:menu:delete'      UNION ALL
  SELECT 'sys:menu:info'            UNION ALL SELECT 'sys:menu:list'        UNION ALL
  SELECT 'sys:menu:save'            UNION ALL SELECT 'sys:menu:select'      UNION ALL
  SELECT 'sys:menu:update'          UNION ALL SELECT 'sys:monitor:metrics'  UNION ALL
  SELECT 'sys:monitor:redis'        UNION ALL SELECT 'sys:role:delete'      UNION ALL
  SELECT 'sys:role:info'            UNION ALL SELECT 'sys:role:list'        UNION ALL
  SELECT 'sys:role:save'            UNION ALL SELECT 'sys:role:select'      UNION ALL
  SELECT 'sys:role:update'          UNION ALL SELECT 'sys:schedule:delete'  UNION ALL
  SELECT 'sys:schedule:info'        UNION ALL SELECT 'sys:schedule:list'    UNION ALL
  SELECT 'sys:schedule:log'         UNION ALL SELECT 'sys:schedule:run'     UNION ALL
  SELECT 'sys:schedule:save'        UNION ALL SELECT 'sys:schedule:update'  UNION ALL
  SELECT 'sys:user:delete'          UNION ALL SELECT 'sys:user:deleteBatch' UNION ALL
  SELECT 'sys:user:info'            UNION ALL SELECT 'sys:user:list'        UNION ALL
  SELECT 'sys:user:resetPassword'   UNION ALL SELECT 'sys:user:save'        UNION ALL
  SELECT 'sys:user:update'
) required
WHERE NOT EXISTS (
  SELECT 1 FROM sys_permission p
  WHERE p.perms = required.perms
     OR FIND_IN_SET(required.perms, REPLACE(p.perms, ' ', '')) > 0
);

-- D2. 按 SysUserMapper.queryAllPerms 的口径，展开“用户 → 有效权限”。
--     请确认需要访问各菜单的角色确实拿到了期望的权限码。
SELECT DISTINCT u.username, m.perms
FROM sys_user_role ur
LEFT JOIN sys_role_permission rm ON ur.role_id = rm.role_id
LEFT JOIN sys_permission m ON rm.permission_id = m.id
LEFT JOIN sys_user u ON u.id = ur.user_id
WHERE LENGTH(m.perms) > 0
ORDER BY u.username, m.perms;


-- ============================================================================
-- 回滚（仅在需要撤销本次新增时使用）
-- ============================================================================
-- DELETE rp FROM sys_role_permission rp
--   JOIN sys_permission p ON rp.permission_id = p.id
--   WHERE p.creator = 'security-hardening';
--
-- DELETE FROM sys_permission WHERE creator = 'security-hardening';
