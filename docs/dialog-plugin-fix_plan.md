# 修复 system 文件夹下 DialogPlugin 用法计划

## 一、问题分析

### 1.1 现状
`src/views/system/` 下 5 个 Vue 文件共 11 处 `DialogPlugin.confirm` 调用，全部使用了**不符合 TDesign Vue Next 官方文档**的属性名。

通过 TDesign MCP 获取的官方 `DialogOptions` / `DialogProps` 规范确认，`DialogPlugin.confirm` 接受的属性为：

| 当前错误属性 | TDesign 正确属性 | 说明 |
|---|---|---|
| `title` | `header` | 头部标题 |
| `content` | `body` | 对话框内容 |
| `okText` | `confirmBtn` | 确认按钮文本（String 类型时直接作为按钮文字） |
| `cancelText` | `cancelBtn` | 取消按钮文本 |
| `onOk` | `onConfirm` | 确认按钮回调函数 |

### 1.2 涉及文件与位置

| 文件 | 调用数 | 行号 |
|---|---|---|
| `user-list.vue` | 2 | L450, L472 |
| `role-list.vue` | 3 | L353, L375, L441 |
| `dict-list.vue` | 3 | L308, L330, L392 |
| `dept-list.vue` | 2 | L240, L262 |
| `permission-list.vue` | 1 | L273 |

### 1.3 统一错误模式示例
```js
// ❌ 当前（错误）
DialogPlugin.confirm({
  title: '提示',
  content: '确定要删除此用户吗？',
  cancelText: '取消',
  okText: '删除',
  onOk: async () => { ... },
});
```

## 二、修改方案

### 2.1 属性名替换规则（全部 11 处统一）
```
title    → header
content  → body
okText   → confirmBtn
cancelText → cancelBtn
onOk     → onConfirm
```

### 2.2 修改后正确模式
```js
// ✅ 修正后
DialogPlugin.confirm({
  header: '提示',
  body: '确定要删除此用户吗？',
  cancelBtn: '取消',
  confirmBtn: '删除',
  onConfirm: async () => { ... },
});
```

### 2.3 逐文件修改清单

#### 文件 1: `src/views/system/user-list.vue`
- **L450-464** `onDelete`: `title→header, content→body, cancelText→cancelBtn, okText→confirmBtn, onOk→onConfirm`
- **L472-487** `onBatchDelete`: 同上

#### 文件 2: `src/views/system/role-list.vue`
- **L353-367** `onDelete`: 同上
- **L375-390** `onBatchDelete`: 同上
- **L441-455** `onRemoveUser`: 同上（confirmBtn 文本为"移除"）

#### 文件 3: `src/views/system/dict-list.vue`
- **L308-322** `onDelete`: 同上
- **L330-345** `onBatchDelete`: 同上
- **L392-406** `deleteDictItem`: 同上

#### 文件 4: `src/views/system/dept-list.vue`
- **L240-254** `onDelete` (函数名可能不同): 同上
- **L262-277** `onBatchDelete`: 同上

#### 文件 5: `src/views/system/permission-list.vue`
- **L273-287** `onDelete`: 同上

## 三、注意事项

1. **仅改属性名**，不修改回调函数内部的业务逻辑
2. **不添加 `theme: 'danger'`** 等额外属性，保持最小改动范围（用户只要求修正用法）
3. `onConfirm` 的回调签名 `(context: { e: MouseEvent | KeyboardEvent }) => void` 与现有 `async () => {}` 兼容（参数可选）
4. 回调内的 try/catch 错误处理模式保持不变

## 四、验证步骤

1. 修改后运行 `vue-tsc --noEmit` 确认无类型错误
2. 使用 GetDiagnostics 检查每个修改文件无 VS Code 诊断报错
3. 确认 11 处调用全部从 `title/content/okText/cancelText/onOk` 改为 `header/body/confirmBtn/cancelBtn/onConfirm`
