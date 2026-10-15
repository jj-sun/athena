# DeptSelect 部门选择组件开发计划

## 1. 需求分析

### 背景
当前项目中部门选择逻辑散落在各页面（如 user-list.vue 直接使用 `t-tree-select`），需要封装成独立的可复用组件，实现：
- 统一的部门树形选择 UI
- 自动加载部门树数据（内置 API 调用）
- 支持 `v-model:value` 双向绑定
- 支持字段回显（编辑时正确显示已选部门名称）
- 支持透传 TDesign TreeSelect 原生属性

### 参考实现（user-list.vue L145-151）
```vue
<t-tree-select
  v-model:value="formData.deptCode"
  placeholder="请选择部门"
  :data="departmentTree"
  :keys="{ value: 'id', label: 'deptName' }"
  clearable
/>
```

### 数据结构
- 部门类型：`SysDept`（[types/system/index.ts](file:///e:/github/athena/athena-vue-tdesign/src/types/system/index.ts#L28-L37)）
  - `id`: string — 部门ID（用作 value）
  - `deptName`: string — 部门名称（用作 label）
  - `children`: Array<SysDept> — 子部门
- API：`getDeptList()` — 返回树形部门数组（[api/system/dept.ts](file:///e:/github/athena/athena-vue-tdesign/src/api/system/dept.ts#L14-L16)）

---

## 2. 组件设计

### 2.1 文件位置
- 组件路径：[src/components/dept/index.vue](file:///e:/github/athena/athena-vue-tdesign/src/components/dept/index.vue)

### 2.2 Props 设计
| Prop | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `modelValue` (v-model:value) | `string \| undefined` | `undefined` | 绑定的部门ID |
| `placeholder` | `string` | `'请选择部门'` | 占位符 |
| `clearable` | `boolean` | `true` | 是否可清空 |
| `disabled` | `boolean` | `false` | 是否禁用 |
| `filterable` | `boolean` | `true` | 是否可搜索 |

> 额外支持通过 `v-bind="$attrs"` 透传 `t-tree-select` 所有原生属性。

### 2.3 Emits 设计
| 事件 | 类型 | 说明 |
|------|------|------|
| `update:modelValue` | `(value: string \| undefined) => void` | v-model 同步值 |
| `change` | `(value: string \| undefined, context: any) => void` | 选中变更事件 |

### 2.4 内部状态
| 变量 | 类型 | 说明 |
|------|------|------|
| `deptTree` | `Ref<Array<SysDept>>` | 部门树数据 |
| `loading` | `Ref<boolean>` | 加载状态 |

### 2.5 生命周期
- `onMounted`：调用 `loadDeptTree()` 拉取部门树数据

---

## 3. 修改文件清单

### 文件 1：`athena-vue-tdesign/src/components/dept/index.vue`（新建内容）
**操作**：覆盖现有空文件，实现 DeptSelect 组件。

**核心逻辑**：
1. 使用 `defineProps` 声明 props，使用 `defineEmits` 声明事件
2. 通过 `computed` 实现内部值与 v-model 的双向同步
3. `onMounted` 调用 `getDeptList()` 获取部门树
4. 模板中使用 `t-tree-select`，配置正确的 `keys: { value: 'id', label: 'deptName' }`
5. 使用 `v-bind="$attrs"` 透传原生属性

### 文件 2：`athena-vue-tdesign/src/views/system/user-list.vue`（修改）
**操作**：替换 L145-151 的 `t-tree-select` 为 DeptSelect 组件。

**变更点**：
1. L188：移除 `import { getDeptList }`（组件内部已处理）
2. 新增 `import DeptSelect from '@/components/dept/index.vue'`
3. L144-152：替换原 `t-tree-select` 为 `<DeptSelect v-model:value="formData.deptCode" />`
4. L229-L231：移除 `departmentTree`、`deptOptions` 变量（若不再被页面其他地方使用则删除）
5. L342-L349：移除 `loadDeptTree()` 函数（同上）
6. L532：移除 `loadDeptTree()` 调用（同上）

> ⚠️ 注意：`departmentTree` 在 user-list.vue 中还被 L11-22 的左侧 `t-tree`（部门筛选树）使用。因此**不能**删除 `departmentTree` 变量、`loadDeptTree()` 函数和 `onMounted` 中的调用。仅替换弹窗表单里的 `t-tree-select` 即可。

---

## 4. 实施步骤

### Step 1：实现 DeptSelect 组件
在 `src/components/dept/index.vue` 中写入完整组件代码，包括：
- `<script setup lang="ts">`：props/emits/状态/方法
- `<template>`：`t-tree-select` 模板，带透传
- `<style scoped lang="less">`：（按需留空）

### Step 2：在 user-list.vue 中集成
- 引入 DeptSelect 组件
- 替换 L145-151 的 `t-tree-select` 为 DeptSelect
- 保留左侧部门筛选树相关代码不动

### Step 3：验证
- 运行 `npm run lint` 检查 TypeScript / ESLint 正确性
- 手动验证（可选）：
  - 新增用户 → 部门选择器正常弹出部门树 → 可选
  - 编辑用户 → 原 deptCode 正确回显部门名 → 可改可清空

---

## 5. 风险与注意事项

| 风险 | 应对 |
|------|------|
| `v-model` 回显时显示 ID 而不是名称 | 确保 `t-tree-select` 的 `keys` 正确映射（value=id, label=deptName），data 必须在渲染前加载完成（loading 兜底） |
| 透传属性覆盖内置属性 | 先写内置 props（keys、data、loading），再用 `v-bind="restAttrs"` 展开用户 attrs，避免冲突 |
| `getDeptList()` 返回结构非树形（扁平） | 根据现有代码（user-list.vue L344-345 直接赋值给 departmentTree 并被 t-tree 使用）确认是树形结构，无需额外转换 |
| 多次使用组件导致重复请求 API | 各实例独立请求，开销可接受；若后续需优化可引入全局 store 缓存（不在本期范围） |

---

## 6. 完成标准
- [x] DeptSelect 组件文件存在且包含完整逻辑
- [x] user-list.vue 弹窗中部门选择已使用 DeptSelect 替换
- [x] `npm run lint` 无错误
- [x] 组件可被其他页面直接 import 使用，无需额外获取部门数据
