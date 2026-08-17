# TDesign 组件属性检查与修复计划

## 概述

使用 TDesign MCP 文档对 `src/views/monitor/` 和 `src/views/system/` 目录下的所有 Vue 文件进行组件属性核对，发现并修复错误的组件属性和不一致的代码格式。

## 当前状态分析

已检查的文件：
- `src/views/monitor/quartz.vue`
- `src/views/system/dept-list.vue`
- `src/views/system/dict-list.vue`
- `src/views/system/log-list.vue`
- `src/views/system/permission-list.vue`
- `src/views/system/role-list.vue`
- `src/views/system/user-list.vue`

通过 TDesign MCP 文档核对，发现以下问题：

### 问题 1：`t-tag` 使用了无效的 `theme="error"`（组件属性错误）

**文件**：`src/views/system/role-list.vue` 第 57 行

```html
<t-tag v-else theme="error" size="small">禁用</t-tag>
```

**问题**：TDesign Tag 组件的 `theme` 属性可选值为 `default/primary/warning/danger/success`，不存在 `error` 选项。

**修复**：改为 `theme="danger"`。同一文件第 131 行已正确使用 `theme="danger"`，需要保持一致。

---

### 问题 2：`t-table` 使用了不存在的 `:row-selection` 属性（组件属性错误）

**文件**：
- `src/views/system/dept-list.vue` 第 27 行
- `src/views/system/dict-list.vue` 第 47 行
- `src/views/system/role-list.vue` 第 47 行

```html
<t-table :row-selection="{ selectedRowKeys, onChange: onSelectChange }" ...>
```

**问题**：TDesign Table 组件**不存在** `rowSelection` 属性。TDesign Table 使用 `rowSelectionAllowUncheck` 和 `rowSelectionType` 等独立属性，而非一个 `rowSelection` 对象。正确的行选中方式是 `:selected-row-keys` + `@select-change` 事件。

**影响**：`row-selection` 作为未知属性被忽略，导致 `selectedRowKeys` 无法更新，批量操作中 `selectedRowKeys.value.length` 始终为 0。

**修复**：改为 `quartz.vue` 和 `user-list.vue` 中已正确使用的模式：

```html
<t-table :selected-row-keys="selectedRowKeys" @select-change="onSelectChange" ...>
```

---

### 问题 3：`t-tree` 使用了无效的 `v-model:expand-all`（组件属性错误）

**文件**：`src/views/system/user-list.vue` 第 12 行

```html
<t-tree v-model:expand-all="expandAllFlag" ... />
```

**问题**：TDesign Tree 组件的 `expandAll` 属性是 Boolean 类型，**不支持 v-model 语法糖**。只有 `expanded`（Array）和 `actived`（Array）支持 v-model。

**修复**：改为单向绑定 `:expand-all="expandAllFlag"`。

---

### 问题 4：`t-button` 缺少 `ghost` 属性（格式不一致）

**文件**：`src/views/system/dict-list.vue` 第 61 行

```html
<t-button theme="danger" size="small" @click="onDelete(row)">删除</t-button>
```

**问题**：缺少 `ghost` 属性，与同一文件中相邻按钮（第 60、62 行）及其他文件中同类按钮不一致。

**修复**：添加 `ghost` 属性。

---

### 问题 5：quartz.vue 中状态值逻辑不一致（格式不正确）

**文件**：`src/views/monitor/quartz.vue`

- **搜索筛选**（第 13-14 行）：`正常` = value 1，`暂停` = value 0
- **表格显示**（第 60-61 行）：`status === 0` → 正常，`else` → 暂停（即 0=正常，非0=暂停）
- **表单单选**（第 103-104 行）：`正常` = value 0，`暂停` = value 1

**问题**：搜索筛选的状态值与表格显示和表单的逻辑相反。搜索中 正常=1，但显示和表单中 正常=0。

**修复**：将搜索筛选的值对齐为 正常=0、暂停=1（与表格显示和表单一致）：

```html
<t-option label="正常" :value="0">正常</t-option>
<t-option label="暂停" :value="1">暂停</t-option>
```

---

### 问题 6：user-list.vue 中 `item.key` 应为 `item.id`（代码错误）

**文件**：`src/views/system/user-list.vue` 第 537 行

```js
keys.push(item.key);  // 应为 item.id
```

**问题**：树数据使用 `id` 作为 value 字段（配置为 `:keys="{ value: 'id', label: 'deptName' }"`），但代码收集 `item.key`（undefined），导致展开全部功能失效。

**修复**：改为 `item.id`。同时 `getExpandedKeys` 变量被赋值但从未绑定到树组件，此 watch 逻辑实际为死代码，应删除整个 watch 块（`expandAll` prop 已能控制展开）。

## 修改方案

### 1. 修改 `src/views/system/role-list.vue`

- 第 57 行：`theme="error"` → `theme="danger"`

### 2. 修改 `src/views/system/dept-list.vue`

- 第 27 行：`:row-selection="{ selectedRowKeys, onChange: onSelectChange }"` → `:selected-row-keys="selectedRowKeys" @select-change="onSelectChange"`

### 3. 修改 `src/views/system/dict-list.vue`

- 第 47 行：`:row-selection="{ selectedRowKeys, onChange: onSelectChange }"` → `:selected-row-keys="selectedRowKeys" @select-change="onSelectChange"`
- 第 61 行：添加 `ghost` 属性

### 4. 修改 `src/views/system/role-list.vue`

- 第 47 行：`:row-selection="{ selectedRowKeys, onChange: onSelectChange }"` → `:selected-row-keys="selectedRowKeys" @select-change="onSelectChange"`

### 5. 修改 `src/views/system/user-list.vue`

- 第 12 行：`v-model:expand-all="expandAllFlag"` → `:expand-all="expandAllFlag"`
- 第 530-547 行：删除无效的 watch 块（`getExpandedKeys` 死代码），同时删除 `getExpandedKeys` 变量声明（第 233 行）

### 6. 修改 `src/views/monitor/quartz.vue`

- 第 13-14 行：搜索筛选值对齐，`正常` = :value="0"，`暂停` = :value="1"

## 假设与决策

1. **`ghost` 属性有效**：TDesign Button 官方文档确认 `ghost: Boolean` 是有效属性（默认 false），用于幽灵按钮。项目记忆中提到 "使用 variant='ghost' 代替 ghost 属性" 是不准确的，因为 `variant` 的可选值为 `base/outline/dashed/text`，不包含 `ghost`。
2. **行选中模式统一**：以 `quartz.vue` 和 `user-list.vue` 中使用的 `:selected-row-keys` + `@select-change` 为标准模式。
3. **quartz.vue 状态值对齐**：以 0=正常、1=暂停 为标准（符合表格显示和表单中的逻辑）。

## 验证步骤

1. 运行 `npx vue-tsc --noEmit` 确认 TypeScript 类型检查通过
2. 运行 `npm run lint` 确认 ESLint 检查通过
3. 启动开发服务器 `npm run dev`，手动验证：
   - 角色管理页面：禁用状态标签显示为红色（danger）而非报错
   - 部门/字典/角色管理页面：表格行选中后批量删除按钮能正确获取选中数量
   - 用户管理页面：切换"展开全部"开关后部门树能正确展开/折叠
   - 定时任务页面：搜索筛选状态值与表格显示一致
