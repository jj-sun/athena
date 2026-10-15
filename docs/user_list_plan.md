# 用户管理功能增强计划

## 需求概述
1. **部门多选过滤用户列表**：点击左侧部门树勾选多个部门，右侧用户列表按选中部门过滤显示
2. **修改用户密码**：在用户列表操作列增加"修改密码"按钮，管理员可重置指定用户的密码

---

## 一、部门多选过滤用户列表

### 后端改动

#### 1. `SysUserServiceImpl.java` — 增加按部门过滤逻辑
**文件**: `e:\github\athena-boot\src\main\java\com\athena\modules\sys\service\impl\SysUserServiceImpl.java`

- `queryPage()` 方法增加对 `deptCode` 的过滤条件
- 前端传来的 `deptCode` 可能是逗号分隔的多个部门ID，使用 `in` 条件过滤
- 需要收集选中部门及其所有子部门ID（前端传叶子节点ID，后端需扩展包含子部门）

#### 2. `SysUserController.java` — 调整 list 接口
**文件**: `e:\github\athena-boot\src\main\java\com\athena\modules\sys\controller\SysUserController.java`

- list 方法接收 `deptCode` 参数（逗号分隔的字符串），传入 `SysUser` 实体

#### 3. `SysDeptService` — 增加获取子部门ID的方法
**文件**: `e:\github\athena-boot\src\main\java\com\athena\modules\sys\service\SysDeptService.java`
**文件**: `e:\github\athena-boot\src\main\java\com\athena\modules\sys\service\impl\SysDeptServiceImpl.java`

- 新增方法：`List<String> getSubDeptIds(String deptCode)` — 获取指定部门及所有子部门的ID

### 前端改动

#### 1. `user.ts` — API 方法调整
**文件**: `e:\github\athena-vue-tdesign\src\api\system\user.ts`

- `getUserList` 参数增加 `deptCode` 字段（可选）

#### 2. `user-list.vue` — 部门联动
**文件**: `e:\github\athena-vue-tdesign\src\views\system\user-list.vue`

- `onDeptCheck` 中，当勾选部门时，将选中的 keys 赋值给搜索参数
- `loadData` 中将 `selectedDeptKeys` 作为 `deptCode` 参数传给后端
- 清空勾选时重置搜索

---

## 二、修改用户密码

### 后端改动

#### 1. `SysUserController.java` — 新增管理员重置密码接口
**文件**: `e:\github\athena-boot\src\main\java\com\athena\modules\sys\controller\SysUserController.java`

- 新增接口 `POST /sys/user/resetPassword`
- 接收 `{ id: String, password: String }` 请求体
- 对新密码进行加密处理（MD5 + BCrypt，与现有逻辑一致）
- 权限标识：`@SaCheckPermission("sys:user:update")`

#### 2. `SysUserService` / `SysUserServiceImpl` — 新增按ID修改密码方法
**文件**: `e:\github\athena-boot\src\main\java\com\athena\modules\sys\service\SysUserService.java`
**文件**: `e:\github\athena-boot\src\main\java\com\athena\modules\sys\service\impl\SysUserServiceImpl.java`

- 新增方法：`boolean resetPassword(String id, String newPassword)` — 根据用户ID直接重置密码

### 前端改动

#### 1. `user.ts` — 新增重置密码 API
**文件**: `e:\github\athena-vue-tdesign\src\api\system\user.ts`

- 新增 `resetUserPassword(id: string, password: string)` 方法

#### 2. `user-list.vue` — 增加修改密码功能
**文件**: `e:\github\athena-vue-tdesign\src\views\system\user-list.vue`

- 操作列增加"修改密码"按钮
- 新增密码修改弹窗（密码 + 确认密码）
- 密码修改逻辑：验证两次密码一致 → 调用 API → 提示成功

---

## 文件修改清单

| 文件 | 改动类型 | 说明 |
|------|---------|------|
| `athena-boot/.../SysUserController.java` | 修改 | 部门过滤 + 新增重置密码接口 |
| `athena-boot/.../SysUserServiceImpl.java` | 修改 | queryPage增加deptCode过滤 + 新增resetPassword |
| `athena-boot/.../SysUserService.java` | 修改 | 新增接口方法声明 |
| `athena-boot/.../SysDeptService.java` | 修改 | 新增getSubDeptIds方法声明 |
| `athena-boot/.../SysDeptServiceImpl.java` | 修改 | 实现getSubDeptIds |
| `athena-vue-tdesign/src/api/system/user.ts` | 修改 | getUserList加deptCode参数 + 新增resetUserPassword |
| `athena-vue-tdesign/src/views/system/user-list.vue` | 修改 | 部门联动过滤 + 修改密码按钮和弹窗 |

---

## 风险与注意事项

1. **部门树勾选事件**：TDesign `t-tree` 的 `@check` 事件返回的是已勾选的 keys 数组，需要注意父子节点联动（勾选父节点会自动勾选子节点）。前端直接传 keys，后端需要扩展为包含所有子部门。
2. **密码加密一致性**：新增密码加密逻辑需与现有 `saveUser` 中的加密方式保持一致（MD5(username:password) + BCrypt）。
3. **权限控制**：管理员重置密码使用 `sys:user:update` 权限，与编辑用户权限一致。
4. **现有密码接口**：`POST /sys/user/password` 是用户自助修改密码，本次不改动。
5. **清空部门选择**：当清空部门勾选时，需要重置用户列表的部门过滤条件。