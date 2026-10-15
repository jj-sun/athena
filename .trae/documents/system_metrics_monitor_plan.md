# Actuator 度量指标监控页实施计划

## 一、Summary

在现有 `monitor` 模块中新增 `SystemInfo.vue`，实现面向系统管理员/运维人员的综合监控页面：

- 展示系统 CPU、进程 CPU、JVM 堆内存、磁盘、线程、运行时长等核心指标。
- 用 ECharts 展示最近 60 个前端采样点的 CPU 与 JVM 堆内存趋势。
- 提供全部 Actuator 指标的搜索、详情查看、标签筛选和统计值展示。
- 默认每 5 秒自动刷新，可切换 5/15/30 秒、暂停或手动刷新。
- 通过受 Sa-Token 保护的后端适配接口访问指标，并关闭当前匿名可访问的原始 `/actuator/metrics` Web 端点。
- 不修改菜单或静态路由；后端动态菜单配置页面组件时使用 `monitor/SystemInfo`。

## 二、Current State Analysis

### 前端

- 前端项目位于 `athena-vue-tdesign`，使用 Vue 3、TDesign Vue Next `1.13.2` 和 ECharts `5.4.3`。
- 仓库已有 `src/views/monitor/`，不存在 `views/minitor/`；因此按现有模块命名使用 `monitor`。
- `src/utils/axios.config.ts` 的响应拦截器只接受 `{ code: 200, data }` 格式，不能直接消费 Actuator 原生 JSON。
- 页面通过后端菜单动态加载，`src/utils/route/index.ts` 会将组件值 `monitor/SystemInfo` 解析为 `src/views/monitor/SystemInfo.vue`。
- TDesign MCP 已核对本页需要的 `Card`、`Statistic`、`Progress`、`Table`、`Descriptions`、`Select`、`Loading`、`Empty` 等组件属性；图标库已确认存在 `cpu`、`server`、`hard-disk-storage`、`time`、`activity`、`refresh`、`pause`、`play`。

### 后端

- 后端已依赖 Spring Boot Actuator，开发环境当前配置 `management.endpoints.web.exposure.include: metrics,httptrace`。
- 已运行实例的实际原始地址为 `/athena/actuator/metrics`，响应为标准 Actuator `names` 列表；详情地址为 `/athena/actuator/metrics/{metricName}`。
- 原始 Actuator 端点不经过现有 Sa-Token MVC 拦截器，目前可以匿名访问。
- Spring Boot 4.1.0 的 `MetricsEndpoint` 由 `MeterRegistry` 提供指标列表和带标签的详情；当 metrics 不再 Web 暴露时自动配置不会创建该 Bean，因此适配服务需要基于 `MeterRegistry` 自行构造 `MetricsEndpoint`。
- 当前工作区已有与本任务无关的未提交修改，实施时不改动或回退这些文件。

## 三、接口与数据契约

新增控制器基路径：`/sys/actuator/metrics`。该路径自动受现有 Sa-Token 登录校验保护，所有响应继续使用项目统一的 `Result<T>`。

### 1. 核心概览

`GET /sys/actuator/metrics/overview`

`data` 字段：

```text
timestamp: long
systemCpuUsage: Double | null
processCpuUsage: Double | null
heapMemoryUsed: Double | null
heapMemoryMax: Double | null
diskFree: Double | null
diskTotal: Double | null
liveThreads: Double | null
peakThreads: Double | null
uptime: Double | null
requestCount: Double | null
```

- CPU 值保留 Actuator 的 `0..1` 比例；内存/磁盘为 bytes；运行时长为 seconds。
- 服务端读取缺失指标时返回 `null`，不因单个运行环境不提供某项指标而使整个概览失败。
- JVM 内存使用 `area:heap` 标签，HTTP 请求数读取 `COUNT`，其余概览值读取 `VALUE`。

### 2. 指标名称

`GET /sys/actuator/metrics`

返回 `{ names: string[] }`，名称按 Actuator 提供的稳定顺序/字典序用于前端检索。

### 3. 指标详情

`GET /sys/actuator/metrics/{metricName}?tag=key:value&tag=key:value`

`data` 保持 Actuator 详情结构：

```text
name: string
description: string | null
baseUnit: string | null
measurements: Array<{ statistic: string; value: number }>
availableTags: Array<{ tag: string; values: string[] }>
```

- `metricName` 使用路径变量；`tag` 支持重复查询参数。
- 指标不存在时返回业务 `404`；标签格式不是 `key:value` 时返回业务 `400`。

## 四、Proposed Changes

### 1. `athena-boot/src/main/resources/application-dev.yml`

- 从 `management.endpoints.web.exposure.include` 中移除 `metrics`，保留现有其他端点配置。
- 目的：关闭匿名访问的原始 `/actuator/metrics`，只允许通过 `/sys/actuator/metrics/**` 访问。

### 2. `athena-boot/src/main/java/com/athena/modules/monitor/domain/SystemMetricsOverview.java`

- 新增使用 Lombok `@Data` 的概览响应模型。
- 字段严格对应“核心概览”契约，使用可空 `Double` 支持不同 JVM/操作系统缺少指标的情况。

### 3. `athena-boot/src/main/java/com/athena/modules/monitor/service/SystemMetricsService.java`

- 定义 `getOverview()`、`listNames()`、`getMetric(name, tags)` 三个只读方法。
- 列表和详情返回 Spring Boot `MetricsEndpoint` 的描述对象，避免手工拼装原生指标结构。

### 4. `athena-boot/src/main/java/com/athena/modules/monitor/service/impl/SystemMetricsServiceImpl.java`

- 注入 `MeterRegistry` 并在服务内部构造 `MetricsEndpoint`，使适配接口不依赖 metrics 是否对 Web 暴露。
- 用统一私有方法按指标名、Statistic 和标签安全取值。
- 聚合 CPU、堆内存、磁盘、线程、运行时长和请求数；单项不存在时返回 `null`。
- 校验标签格式，并原样支持标签值中出现冒号的情况。

### 5. `athena-boot/src/main/java/com/athena/modules/monitor/controller/ActuatorMetricsController.java`

- 新增 `/sys/actuator/metrics` REST 控制器并注入 `SystemMetricsService`。
- 按上述三个接口返回统一 `Result`。
- 将不存在的指标和非法标签分别映射为明确的 404/400 业务响应。

### 6. `athena-boot/src/test/java/com/athena/modules/monitor/service/impl/SystemMetricsServiceImplTest.java`

- 使用 `SimpleMeterRegistry` 注册可控的 Gauge/Counter，不启动数据库、Redis 或完整 Spring 上下文。
- 覆盖指标列表、详情与标签过滤、概览聚合、缺失指标返回 `null` 四类行为。

### 7. `athena-vue-tdesign/src/types/monitor/index.ts`

- 新增 `SystemMetricsOverview`、`MetricNamesResponse`、`MetricDetail`、`MetricMeasurement`、`MetricAvailableTag` 类型。
- 不改动现有定时任务类型。

### 8. `athena-vue-tdesign/src/api/monitor/metrics.ts`

- 使用现有 `GET` 请求封装新增：
  - `getSystemMetricsOverview()`
  - `getMetricNames()`
  - `getMetricDetail(name, tags?)`
- 标签查询使用 `URLSearchParams.append` 生成重复的 `tag` 参数，并编码指标名与标签值。

### 9. `athena-vue-tdesign/src/views/monitor/SystemInfo.vue`

页面采用紧凑的运维控制台布局，不嵌套卡片：

- 顶部工具栏：连接状态、最后更新时间、刷新周期选择、暂停/继续图标按钮、手动刷新图标按钮；图标按钮提供 tooltip。
- 核心指标区：六个响应式 TDesign 卡片，分别展示系统 CPU、进程 CPU、JVM 堆内存、磁盘使用、线程、运行时长；比例指标使用 `t-progress`，数值使用 `t-statistic` 或等宽占位。
- 趋势区：两个固定高度的 ECharts 折线图，分别展示系统/进程 CPU 与 JVM 堆内存使用率。
- 指标浏览区：可搜索 `t-select` 选择全部指标；`t-descriptions` 展示名称、说明、基础单位；按 `availableTags` 动态生成标签选择器；`t-table` 展示 statistic、格式化值和单位。
- 默认选中 `jvm.memory.used`；切换指标时先获取无标签详情并保存完整标签选项，后续筛选不丢失已选标签定义。
- bytes 转为 IEC 单位，CPU usage 转为百分比，uptime 转为可读时长；未知单位保留数值和原始单位。
- 首次加载并行获取概览、指标名称和默认指标详情；之后定时刷新概览和当前指标详情，避免每轮重复请求名称列表。
- 前端仅保留最近 60 个成功采样点；刷新周期变化不清空历史，页面刷新后历史清空。
- 使用刷新互斥标记防止慢请求重叠。轮询失败时保留最后一次成功数据、将状态改为异常，并避免每个周期重复弹出错误消息。
- 页面失活或卸载时停止定时器并销毁图表；重新激活时立即刷新并恢复轮询。
- 监听窗口尺寸与主题变化，调整图表尺寸和颜色；图表容器保持稳定高度，避免数据加载导致布局跳动。
- 空指标、无详情、加载中分别使用 TDesign Skeleton/Loading/Empty 状态，不显示 `NaN`、`Infinity` 或伪造的 0。

## 五、Assumptions & Decisions

1. 用户所说的 `views/minitor` 按仓库实际目录纠正为 `src/views/monitor`，主文件名严格使用 `SystemInfo.vue`。
2. 页面受众是已登录的系统管理员或运维人员。
3. 只提供页面组件和配套接口，不写入数据库菜单，不修改 `asyncRoutes.ts`；菜单管理中组件值配置为 `monitor/SystemInfo`。
4. 趋势历史只存在浏览器内存，不增加服务端调度、缓存或数据库表。
5. 自动刷新默认 5 秒，选项为 5/15/30 秒；暂停只停止轮询，手动刷新始终可用。
6. 原始 Actuator metrics Web 端点关闭；适配服务直接使用 `MeterRegistry`，因此各环境不需要公开原始端点。
7. 不新增依赖，继续使用项目已有 TDesign、ECharts、VueUse、Axios 和 Spring Boot Actuator。
8. 不修改当前工作区中的 `SecurityUtil.java`、`permission.ts`、`user.ts`、`axios.config.ts`、`quartz.vue`、`permission-list.vue` 等既有未提交改动。

## 六、Verification

1. 后端单元测试：
   - 运行 `mvn -DskipTests=false -Dtest=SystemMetricsServiceImplTest test`。
   - 验证概览聚合、指标详情、标签过滤与缺失指标降级。
2. 后端编译：
   - 运行 `mvn -DskipTests package` 或至少 `mvn -DskipTests compile`。
3. 前端静态检查：
   - 运行 `npm run build:type`。
   - 对新增/修改前端文件运行 ESLint；运行 Stylelint 检查 `SystemInfo.vue`。
4. 接口验收：
   - 登录后确认 `/athena/sys/actuator/metrics`、`/overview` 和具体指标详情返回 `code: 200`。
   - 无 Token 请求适配接口应被拒绝。
   - `/athena/actuator/metrics` 不再暴露。
5. 页面验收：
   - 通过动态菜单组件值 `monitor/SystemInfo` 打开页面。
   - 验证六项概览、两张趋势图、指标搜索、标签筛选、值/单位格式化。
   - 验证 5/15/30 秒切换、暂停/继续、手动刷新以及请求不重叠。
   - 验证接口失败和部分指标缺失时保留旧数据并正确展示异常/空状态。
   - 在桌面与窄视口检查响应式换行、文本不溢出、图表非空且无控件重叠。
   - 切换明暗模式和主题色，确认图表与 TDesign 组件同步更新。
