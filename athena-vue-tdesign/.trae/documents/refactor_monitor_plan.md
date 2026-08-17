# 重构：定时任务代码按模块归类到 monitor 目录

## 需求

将之前创建的定时任务相关代码按项目目录规范进行调整：
- 类型定义从 `src/types/system/index.ts` 迁移到 `src/types/monitor/index.ts`
- API 接口从 `src/api/system/schedule.ts` 迁移到 `src/api/monitor/schedule.ts`
- 更新 `quartz.vue` 中的 import 路径
- 删除旧文件中的对应代码

## 修改文件清单

### 1. 新建 `src/types/monitor/index.ts`
- 迁移 `SysScheduleJob` 接口（从 `src/types/system/index.ts`）
- 添加必要的 import（`PageResult`、`Result`）

### 2. 新建 `src/api/monitor/schedule.ts`
- 完整的 9 个 API 接口
- import 路径更新为 `@/types/monitor`

### 3. 修改 `src/types/system/index.ts`
- 删除 `SysScheduleJob` 接口定义

### 4. 删除 `src/api/system/schedule.ts`
- 旧文件已迁移，删除

### 5. 修改 `src/views/monitor/quartz.vue`
- import 路径：`@/api/system/schedule` → `@/api/monitor/schedule`
- import 路径：`@/types/system` → `@/types/monitor`

## 注意事项
- `quartz.vue` 中还有对 `@/types/system` 的其他类型引用吗？不需要，只用 `SysScheduleJob`
- API 接口内容完全相同，仅路径变更
