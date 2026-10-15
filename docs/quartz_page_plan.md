# Quartz 定时任务页面开发计划

## 一、需求概述

参照 `views/system/user-list.vue` 的实现模式，开发定时任务管理页面 `views/monitor/quartz.vue`，实现以下功能：
- 列表查询（分页、搜索）
- 新增定时任务
- 编辑定时任务
- 删除（单个+批量）
- 启动/恢复任务
- 暂停任务
- 立即执行任务

## 二、后端接口分析

### 已有实体 `ScheduleJob`（`athena-boot/.../job/entity/ScheduleJob.java`）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | String | 主键（BasePo） |
| beanName | String | Spring Bean 名称 |
| parameter | String | 任务参数 |
| cronExpression | String | Cron 表达式 |
| status | Integer | 任务状态（0=暂停，1=正常） |
| remark | String | 备注 |
| delFlag | Integer | 删除标记（BasePo，@Dict(dicCode="del_flag")） |
| ctime | Date | 创建时间（BasePo） |

### 已有接口 `ScheduleJobController`（`athena-boot/.../job/controller/ScheduleJobController.java`）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/sys/schedule/list` | 分页列表 |
| GET | `/sys/schedule/info/{jobId}` | 详情 |
| POST | `/sys/schedule/save` | 新增 |
| PUT | `/sys/schedule/update` | 修改 |
| DELETE | `/sys/schedule/delete?id=` | 删除 |
| DELETE | `/sys/schedule/deleteBatch?ids=` | 批量删除 |
| GET | `/sys/schedule/run?id=` | 立即执行 |
| GET | `/sys/schedule/pause?id=` | 暂停 |
| GET | `/sys/schedule/resume?id=` | 恢复/启动 |

## 三、前端文件规划

### 1. 类型定义 — `src/types/system/index.ts`
新增 `SysScheduleJob` 接口：
```typescript
export interface SysScheduleJob {
  id: string;
  beanName: string;
  parameter?: string;
  cronExpression: string;
  status: number;       // 0=暂停, 1=正常
  remark?: string;
  delFlag?: number;
  ctime?: string;
}
```

### 2. API 接口 — `src/api/system/schedule.ts`（新建）
封装所有后端接口调用：
- `getScheduleJobList(params)` → GET `/sys/schedule/list`
- `getScheduleJobInfo(id)` → GET `/sys/schedule/info/{id}`
- `saveScheduleJob(data)` → POST `/sys/schedule/save`
- `updateScheduleJob(data)` → PUT `/sys/schedule/update`
- `deleteScheduleJob(id)` → DELETE `/sys/schedule/delete`
- `deleteScheduleJobBatch(ids)` → DELETE `/sys/schedule/deleteBatch`
- `runScheduleJob(id)` → GET `/sys/schedule/run`
- `pauseScheduleJob(id)` → GET `/sys/schedule/pause`
- `resumeScheduleJob(id)` → GET `/sys/schedule/resume`

### 3. 页面组件 — `src/views/monitor/quartz.vue`（新建）

#### 页面结构
- 顶部搜索区：Bean名称搜索、状态筛选
- 操作按钮区：新增、批量删除
- 数据表格：
  - 列：Bean名称、参数、Cron表达式、状态、备注、创建时间、操作
  - 命名插槽：`#status`（Tag 显示状态）、`#actions`（编辑/删除/执行/暂停/恢复）
- 弹窗表单：新增/编辑定时任务

#### 使用的组件和 hooks
- `TableSearch` — 搜索组件
- `TableHeader` — 表格头组件
- `ModalDialog` — 弹窗组件
- `useTable` / `useTableColumn` / `usePagination` / `useRowKey` / `useRowSelection` / `useColumnControllerConfig` — 表格 hooks
- `t-table` / `t-tag` / `t-button` / `t-dialog` / `t-form` / `t-input` / `t-select` / `t-radio-group` — TDesign 组件

#### 表格列定义
| 列名 | colKey | 说明 |
|------|--------|------|
| Bean名称 | beanName | — |
| 参数 | parameter | — |
| Cron表达式 | cronExpression | — |
| 状态 | status | Tag 显示：正常/暂停 |
| 备注 | remark | — |
| 创建时间 | ctime | — |
| 操作 | actions | 编辑/删除/执行/暂停/恢复 |

#### 操作按钮
- 行操作：编辑、删除、立即执行、暂停（状态为正常时）、恢复（状态为暂停时）
- 批量操作：批量删除

## 四、实施步骤

1. **修改 `src/types/system/index.ts`** — 添加 `SysScheduleJob` 类型
2. **创建 `src/api/system/schedule.ts`** — 封装所有 API 接口
3. **创建 `src/views/monitor/quartz.vue`** — 实现完整的定时任务管理页面

## 五、注意事项

- 页面参照 `user-list.vue` 模式，使用命名列插槽（`#colKey="{ row }"`）
- `status` 字段：0=暂停，1=正常，使用 `t-tag` 组件展示
- 操作按钮需根据任务状态动态显示：暂停状态显示"恢复"，正常状态显示"暂停"
- 暂停/恢复/执行操作使用 GET 请求，操作完成后刷新列表
- `deleteBatch` 接口参数格式为 `ids=1,2,3`（逗号分隔）
- `info` 接口路径为 `/sys/schedule/info/{jobId}`（路径参数）
