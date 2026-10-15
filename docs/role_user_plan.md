# 角色关联用户功能实现计划

## 需求概述
在角色管理页面 (`role-list.vue`) 的 `modal-drawer` (ref="userModalDrawerRef") 中实现角色关联用户列表功能，包括：
1. 展示当前角色关联的用户列表
2. 增加用户关联到当前角色
3. 从当前角色移除用户

## 代码库调研结论

### 现有架构
- **前端**: Vue 3 + TDesign Vue Next，使用组合式 API
- **后端**: Spring Boot + MyBatis-Plus，有 `SysUserRole` 关联表（userId + roleId）
- **API 规范**: GET/POST/PUT/DELETE 封装在 `@/utils/http`，统一返回 `Result<T>`

### 已有的关联能力
- `SysUserRoleMapper.xml`: 有 `queryRoleIdList` (根据userId查roleId列表)、`deleteBatch` (根据roleIds批量删除)
- `SysUserRoleServiceImpl.saveOrUpdate(userId, roleIdList)`: 保存或更新用户角色关系
- `SysUserController`: 用户增删改查接口已存在
- `role-list.vue`: 已有 `userModalDrawerRef` 的 ModalDrawer 框架，内容为空

### 缺失的能力
- 根据角色ID查询用户列表（需关联 sys_user_role 和 sys_user 表）
- 前端 API 层缺少角色关联用户的接口

---

## 实现步骤

### 步骤 1: 后端 - 添加角色关联用户查询和操作接口

#### 1.1 修改 `SysUserRoleMapper.java`
**文件**: `e:\github\athena-boot\src\main\java\com\athena\modules\sys\mapper\SysUserRoleMapper.java`
- 添加方法: `List<String> queryUserIdList(String roleId)` — 根据角色ID查用户ID列表
- 添加方法: `int deleteByRoleAndUserId(@Param("roleId") String roleId, @Param("userId") String userId)` — 删除单个用户角色关系
- 添加方法: `int batchDeleteByRoleAndUserIds(@Param("roleId") String roleId, @Param("userIds") List<String> userIds)` — 批量删除

#### 1.2 修改 `SysUserRoleMapper.xml`
**文件**: `e:\github\athena-boot\src\main\java\com\athena\modules\sys\mapper\xml\SysUserRoleMapper.xml`
- 添加 `queryUserIdList` SQL: `SELECT user_id FROM sys_user_role WHERE role_id = #{value}`
- 添加 `deleteByRoleAndUserId` SQL
- 添加 `batchDeleteByRoleAndUserIds` SQL

#### 1.3 修改 `SysUserRoleService.java` / `SysUserRoleServiceImpl.java`
- 添加: `List<String> queryUserIdList(String roleId)`
- 添加: `void saveUserRole(String userId, String roleId)` — 添加单个用户角色关系
- 添加: `void deleteUserRole(String userId, String roleId)` — 删除单个用户角色关系
- 添加: `void batchDeleteUserRole(String roleId, List<String> userIds)` — 批量删除

#### 1.4 修改 `SysRoleController.java`
**文件**: `e:\github\athena-boot\src\main\java\com\athena\modules\sys\controller\SysRoleController.java`
- 添加 `GET /sys/role/listUsers` — 根据角色ID获取用户列表
  - 参数: roleId, page, pageSize
  - 返回: `Result<PageUtils>` 包含用户分页数据
- 添加 `POST /sys/role/addUser` — 关联用户到角色
  - Body: `{ roleId, userIdList: [...] }`
- 添加 `DELETE /sys/role/removeUser` — 从角色移除用户
  - 参数: roleId, userId

### 步骤 2: 前端 API 层 - 添加角色关联用户接口

#### 2.1 修改 `src/api/system/role.ts`
**文件**: `e:\github\athena-vue-tdesign\src\api\system\role.ts`
- `listUsers: '/sys/role/listUsers'`
- `addUser: '/sys/role/addUser'`
- `removeUser: '/sys/role/removeUser'`
- 导出函数: `getRoleUserList`, `addUserToRole`, `removeUserFromRole`

### 步骤 3: 前端页面 - 实现角色关联用户抽屉

#### 3.1 修改 `src/views/system/role-list.vue`
**文件**: `e:\github\athena-vue-tdesign\src\views\system\role-list.vue`

**模板变更:**
- 在操作列 actions 中添加"用户"按钮 (使用 `user-icon` 或类似图标)
- 实现 `modal-drawer#userModalDrawerRef` 的内容：
  - 顶部: 角色名称显示 + 添加用户按钮
  - 主体: 用户列表表格 (t-table)，显示用户名、姓名、邮箱、创建时间、操作
  - 操作列: 移除按钮
- 添加"添加用户"对话框 (modal-dialog)：
  - 包含用户选择器（使用 t-select multiple 或 t-transfer 从已有用户列表中选择）

**脚本变更:**
- 导入新的 API 函数: `getRoleUserList`, `addUserToRole`, `removeUserFromRole`
- 导入 `UserIcon` 等图标
- 导入 `useTable`, `useTableColumn`, `usePagination`, `useRowKey` 等 hooks
- 添加状态变量:
  - `currentRole` - 当前操作的角色
  - `userTableLoading` - 用户表格加载状态
  - `userList` - 用户列表数据
  - `userPagination` - 用户分页
  - `availableUserOptions` - 可添加的用户选项
  - `selectedUserIds` - 待添加的用户ID列表
  - `userSearchForm` - 搜索表单
- 添加方法:
  - `onShowUsers(row)` - 打开用户抽屉，加载用户列表
  - `loadRoleUsers()` - 加载角色关联用户
  - `onRemoveUser(row)` - 从角色移除用户
  - `onBatchRemoveUsers()` - 批量移除用户
  - `onOpenAddUserDialog()` - 打开添加用户对话框
  - `onAddUserConfirm()` - 确认添加用户到角色
  - `loadAvailableUsers()` - 加载可添加的用户列表

---

## 文件修改清单

| 文件路径 | 操作 | 说明 |
|---------|------|------|
| `athena-boot/.../mapper/SysUserRoleMapper.java` | 修改 | 添加查询和删除方法 |
| `athena-boot/.../mapper/xml/SysUserRoleMapper.xml` | 修改 | 添加SQL映射 |
| `athena-boot/.../service/SysUserRoleService.java` | 修改 | 添加服务接口方法 |
| `athena-boot/.../service/impl/SysUserRoleServiceImpl.java` | 修改 | 实现服务方法 |
| `athena-boot/.../controller/SysRoleController.java` | 修改 | 添加3个REST端点 |
| `athena-vue-tdesign/src/api/system/role.ts` | 修改 | 添加3个API函数 |
| `athena-vue-tdesign/src/views/system/role-list.vue` | 修改 | 实现用户抽屉功能 |

---

## 风险与注意事项

1. **后端分页**: 查询角色关联用户列表需要分页，需在 Mapper XML 中实现带分页的查询（MyBatis-Plus 的 Page 插件可自动处理）
2. **用户选择器**: 添加用户到角色时，需要展示尚未关联到该角色的用户列表，后端需排除已关联用户
3. **事务性**: 删除角色时已级联删除用户关联，新增操作无需特殊事务
4. **权限控制**: 新端点需考虑权限注解（当前代码大部分注释掉了权限注解，保持一致即可）
5. **前端 hooks 复用**: 用户抽屉内的表格可复用现有 `useTable`, `usePagination` 等 hooks，注意变量命名冲突