-- sys_user.dept_code -> dept_id
--
-- 背景：sys_user.dept_code 中保存的一直是 sys_dept.id，为避免误导统一改名为 dept_id。
-- 该脚本用于尚未手动改名的环境；已改名的环境执行时会跳过。
-- 注意：sys_dept.dept_code 是部门自身的业务编码，不受影响。

SET @col_exists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'sys_user'
    AND COLUMN_NAME = 'dept_code'
);

SET @ddl := IF(
  @col_exists > 0,
  'ALTER TABLE `sys_user` CHANGE COLUMN `dept_code` `dept_id` varchar(32) DEFAULT NULL COMMENT ''部门ID''',
  'SELECT ''sys_user.dept_code 不存在，无需迁移'' AS msg'
);

PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
