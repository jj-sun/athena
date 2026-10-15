# SystemInfo 系统与 Redis 监控页实施计划

## 一、Summary

在现有 `athena-vue-tdesign/src/views/monitor/SystemInfo.vue` 基础上加入 Redis 监控，并使用 TDesign `Tabs` 将页面组织为“系统指标”和“Redis”两个页签。系统页保留已有 Actuator 概览、趋势图和指标浏览器；Redis 页提供运行概览、真实前端采样趋势和可搜索的完整 `INFO` 明细。

页面继续面向已登录的系统管理员/运维人员，沿用顶部刷新频率、暂停/继续和手动刷新控制。轮询只刷新当前页签，系统与 Redis 的加载、健康状态、更新时间和趋势历史相互独立；Redis 不可用时不影响系统指标页。

## 二、Current State Analysis

### 已有系统监控

- `SystemInfo.vue` 已实现系统 CPU、进程 CPU、JVM 堆内存、磁盘、线程、运行时长六项概览，两张 ECharts 趋势图，以及 Actuator 指标搜索、标签筛选和详情表格。
- `src/api/monitor/metrics.ts` 和 `src/types/monitor/index.ts` 已包含 Actuator 指标接口与类型。
- 页面已有 5/15/30 秒轮询、暂停、手动刷新、页面可见性处理、KeepAlive 生命周期、主题同步和请求互斥逻辑。
- 动态菜单组件值 `monitor/SystemInfo` 已能解析到该页面，无需修改菜单数据或静态路由。

### 原始 Redis 后端现状

- `ActuatorRedisController` 基路径为 `/sys/actuator/redis`，会被现有 Sa-Token 全局拦截器保护。
- `GET /info` 返回统一的 `Result<List<RedisInfo>>`，包含 Redis `INFO` 的 key、value 和中文说明。
- `/keysSize`、`/memoryInfo`、`/keysSizeForReport`、`/memoryForReport` 直接返回 `Map`，不符合前端 Axios 拦截器要求的 `{ code: 200, data }` 契约，不能直接接入新页面。
- 两个报表接口只是把同一个当前值复制为五个时间点，不是真实历史，不用于新页面。
- `RedisServiceImpl` 多次直接获取连接且没有显式关闭；`getMemoryInfo` 在字段缺失时还可能因空值日志产生异常。新高频轮询路径必须使用单次、可关闭的连接。
- 初始状态没有 Redis 监控服务单元测试，也没有 TDesign 前端 Redis API 封装。

### TDesign 约束

已通过 tdesign-mcp 核对 Vue Next 的 `Tabs`、`Card`、`Statistic`、`Progress`、`Table`、`Descriptions`、`Input`、`Tag`、`Alert` 和 `Empty` API；图标库可使用 `server`、`key`、`chart` 等现有图标。页签使用受控 `v-model:value`，表格使用稳定 `row-key`，搜索框允许清空。

### 续接核查基线（2026-08-31）

- Redis 快照领域模型、服务方法、`/snapshot` 控制器、服务单元测试、前端类型和 API 文件已经落盘，但尚未完成本轮后端编译和测试验证。
- `SystemInfo.vue` 的 Redis 页签模板、响应式状态、计算属性、搜索过滤、趋势采样及四张图表的基础逻辑已经落盘。
- 当前前端仍存在明确的未闭环项：模板引用的 `refreshActiveData` 尚未实现；`startPolling()`、页面可见性恢复和 KeepAlive 激活仍固定调用系统刷新；没有监听 `activeTab` 完成 Redis 首次懒加载、切换刷新与可见后图表初始化；主题变化只更新系统图表；ResizeObserver 也需要在 Redis 图表挂载后补充观察。
- Redis 页签专用样式尚未补齐，至少需要覆盖 TabPanel 内容间距、INFO 搜索框、表格长文本省略和移动端布局；模板格式化和 TypeScript/ESLint/Stylelint 结果需在逻辑闭环后重新确认。
- 后端项目要求 JDK 21，当前终端默认 JDK 17；实施验证时必须显式设置 `JAVA_HOME=D:\sdk\jdk\jdk-21.0.5+11`。Surefire 默认 `skipTests=true`，测试命令必须显式覆盖该配置。
- Sa-Token 对 `/**` 执行登录校验，新增 `/sys/actuator/redis/snapshot` 不在排除列表中，因此沿用现有登录态保护。

## 三、接口与数据契约

新增受保护接口：

`GET /sys/actuator/redis/snapshot`

响应为项目统一的 `Result<RedisMonitorSnapshot>`，其中 `data` 为：

```text
timestamp: long
redisVersion: String | null
redisMode: String | null
role: String | null
uptimeSeconds: Long | null
keyCount: Long | null
usedMemory: Long | null
peakMemory: Long | null
maxMemory: Long | null
connectedClients: Long | null
blockedClients: Long | null
operationsPerSecond: Long | null
totalCommandsProcessed: Long | null
keyspaceHits: Long | null
keyspaceMisses: Long | null
keyspaceHitRate: Double | null
rejectedConnections: Long | null
details: Array<{ key: string; value: string; description: string | null }>
```

契约约定：

1. 内存单位为 bytes，运行时长为 seconds，命中率使用 `0..1` 比例，前端负责格式化。
2. `keyCount` 使用当前 Spring Redis 配置所选数据库的 `DBSIZE`，页面文案为“当前库 Key 数”，不硬编码 DB0。
3. `maxMemory === 0` 表示 Redis 未设置上限；前端显示“未限制”，不计算无意义的百分比。
4. 单个 `INFO` 字段不存在或无法解析时返回 `null`，不使整个快照失败；连接本身失败时接口返回统一业务错误。
5. `keyspaceHitRate = hits / (hits + misses)`；分母为 0 时返回 `null`。
6. `details` 按 key 排序，保证表格顺序稳定。
7. 旧 Redis 接口路径和响应保持兼容，不删除、不重命名；新页面只使用 `/snapshot`。

## 四、Proposed Changes

### 1. 新增 `athena-boot/src/main/java/com/athena/modules/monitor/domain/RedisMonitorSnapshot.java`

- 使用 Lombok `@Data` 定义上述强类型快照模型。
- 数值使用可空包装类型，以兼容不同 Redis 版本、模式和权限下缺少字段的情况。
- 快照内包含完整 `RedisInfo` 列表，使一次请求即可同时驱动概览、趋势和详情表格。

### 2. 修改 `athena-boot/src/main/java/com/athena/modules/monitor/service/RedisService.java`

- 增加 `getMonitorSnapshot()` 只读方法。
- 保留现有方法签名，避免影响其他潜在调用方。

### 3. 修改 `athena-boot/src/main/java/com/athena/modules/monitor/service/impl/RedisServiceImpl.java`

- 实现 `getMonitorSnapshot()`：只获取一次 `RedisConnection`，在同一连接上调用 `info()` 与 `dbSize()`，并使用 try-with-resources 关闭连接。
- 增加私有的字符串、Long 和 Double 安全解析方法；任何单字段解析失败均降级为 `null`。
- 从 `Properties` 提取版本、模式、角色、运行时长、内存、客户端、QPS、命中/未命中、拒绝连接等概览值，并生成排序后的详情列表。
- 将现有 `getRedisInfo()`、`getKeysSize()`、`getMemoryInfo()` 的连接访问同步整理为可关闭连接和空值安全实现，但不改变其返回字段和旧接口契约。
- 不再依赖或扩展伪历史报表方法；新趋势全部来自浏览器持续采样。

### 4. 修改 `athena-boot/src/main/java/com/athena/modules/monitor/exception/RedisConnectException.java`

- 增加可保留原始 cause 的构造方法。
- 快照获取失败时对外返回稳定的“Redis 连接失败”类消息，由全局异常处理器保持统一 Result 响应，同时服务端日志保留原始异常供排查。

### 5. 修改 `athena-boot/src/main/java/com/athena/modules/monitor/controller/ActuatorRedisController.java`

- 新增 `GET /snapshot`，返回 `Result<RedisMonitorSnapshot>`。
- 删除每次输出完整 Redis INFO 的日志，避免 5 秒轮询造成日志洪泛，并避免将 `run_id`、`config_file` 等信息写入业务日志。
- 保留 `/info` 及所有旧端点，确保兼容现有调用方。

### 6. 新增 `athena-boot/src/test/java/com/athena/modules/monitor/service/impl/RedisServiceImplTest.java`

使用 Mockito 模拟 `RedisConnectionFactory` 与 `RedisConnection`，不连接真实 Redis，覆盖：

- 完整 INFO 与 DBSIZE 能正确映射到快照字段和排序后的详情。
- 命中率计算正确，零请求时返回 `null`。
- 缺失或非法数值字段降级为 `null`。
- `maxmemory=0` 原样保留，供前端识别“未限制”。
- 成功和异常路径都正确关闭连接，连接异常转为 `RedisConnectException`。

### 7. 修改 `athena-vue-tdesign/src/types/monitor/index.ts`

- 增加 `RedisInfoItem` 与 `RedisMonitorSnapshot` 类型，字段与后端契约一一对应。
- 不改动已有 Actuator 和定时任务类型。

### 8. 新增 `athena-vue-tdesign/src/api/monitor/redis.ts`

- 定义 Redis API 根路径并新增 `getRedisMonitorSnapshot()`。
- 使用现有 `GET<T>`，依赖统一响应拦截器解包 `Result.data`。

### 9. 修改 `athena-vue-tdesign/src/views/monitor/SystemInfo.vue`

#### 页面结构

- 顶部保留统一刷新工具栏，其下增加 TDesign `t-tabs`，页签为“系统指标”和“Redis”。
- 默认激活“系统指标”；系统页原有概览、趋势与指标浏览器整体迁入对应 TabPanel，行为保持不变。
- Redis 页不嵌套页面级卡片，只为重复指标和独立工具面板使用卡片。

#### Redis 概览与详情

- 展示六个响应式概览卡片：Redis 版本/模式与角色、当前库 Key 数、已用内存/峰值或上限、连接/阻塞客户端、实时 QPS/累计命令数、命中率/拒绝连接数。
- Redis 运行时长作为版本或角色卡片的辅助信息；缺失值统一显示 `--`。
- 当 `maxMemory > 0` 时显示内存进度条并应用与系统卡片一致的阈值颜色；为 0 时显示“未限制”且不渲染进度百分比。
- 增加“Redis 内存趋势”和“Redis 吞吐趋势”两张 ECharts 折线图，分别保存已用内存和 `operationsPerSecond` 最近 60 个成功采样点；tooltip 使用字节/QPS格式化。
- 增加 Redis INFO 表格，列为“参数、当前值、说明”；使用带搜索图标、可清空的 TDesign Input 对 key、value、description 做大小写不敏感的本地过滤。
- 长值使用省略展示并提供 tooltip，表格无数据时显示 `t-empty`。

#### 刷新、状态与生命周期

- 将当前单一状态拆分为 system/redis 两套：首次加载、刷新中、连接健康、最近更新时间、趋势点和请求互斥标记。
- 统一工具栏显示当前页签的状态、更新时间和刷新中状态；切换页签时立即切换对应状态。
- 自动轮询和手动刷新都只请求当前页签。Redis 首次切入时懒加载，之后切入立即刷新；切离后不继续请求 Redis。
- 系统与 Redis 请求可各自独立完成；Redis 失败只将 Redis 状态置为异常，并保留上一次成功快照和趋势，系统状态不受影响。
- 同一数据源禁止重叠请求；错误提示每个数据源只在从健康转为异常时弹出一次，恢复成功后允许下一次异常再次提示。
- 页面隐藏、失活或卸载时停止轮询；重新激活时只刷新当前页签。
- Redis 图表仅在 Redis 面板可见后初始化；页签切换后 `nextTick` 执行 init/resize，主题切换时同步更新四张图表，卸载时全部 dispose。
- 响应式布局沿用现有断点；窄屏下页签、工具栏、搜索框、卡片和图表不得重叠或横向溢出。

## 五、Assumptions & Decisions

1. 用户已确认采用“页内标签页”和“Redis 概览 + 完整详情”。
2. 用户已确认 Redis 趋势展示“内存 + 吞吐”，并只轮询当前页签。
3. 不新增前后端依赖，不引入服务端时序存储；趋势仅存在当前浏览器会话内，刷新页面后清空。
4. 新增单一快照接口以减少网络往返和 Redis INFO 调用；不直接使用返回格式不兼容的旧 Map 接口。
5. 页面仍使用 `monitor/SystemInfo` 动态菜单组件值，不新增页面文件、不修改路由或菜单数据。
6. Redis 接口继续只要求登录态，不额外新增权限码；访问控制沿用 `/sys/**` 的 Sa-Token 全局校验。
7. 默认页签保持“系统指标”，避免 Redis 不可用时阻塞页面首次展示。
8. 现有工作区包含其他未提交改动，实施时只修改本计划列出的文件，不回退或覆盖用户改动。

## 六、Verification

1. 后端单元测试：使用 JDK 21 显式启用并运行 `SystemMetricsServiceImplTest` 与新增 `RedisServiceImplTest`，确认快照映射、缺失值、命中率、异常与连接关闭行为。
2. 后端编译：运行 Maven compile/package，确认新增领域模型、服务签名和控制器泛型通过编译。
3. 前端静态检查：运行 `npm run build:type`，对修改文件运行 ESLint，并运行 Stylelint 检查 `SystemInfo.vue`。
4. 接口验收：登录后确认 `/athena/sys/actuator/redis/snapshot` 返回 `code: 200` 和完整契约；无 Token 请求被拒绝；旧 Redis 接口仍可访问且契约未变。
5. Redis 正常场景：核对版本、模式、角色、Key 数、内存、客户端、QPS、命中率与 Redis CLI/INFO 一致；完整详情可按参数、值和说明搜索。
6. Redis 边界场景：验证 `maxmemory=0`、零命中样本、缺失 INFO 字段、长值、空详情均无 NaN、Infinity、错误百分比或布局溢出。
7. 故障隔离：停止/断开 Redis 后，Redis 页显示连接异常并保留最后成功数据，系统页仍可正常刷新；恢复 Redis 后状态自动恢复。
8. 轮询验收：验证首次进入 Redis 才发起请求，5/15/30 秒切换、暂停/继续、手动刷新有效，非当前页签无持续请求，同源请求不重叠。
9. 浏览器验收：在桌面与移动视口检查页签、六张 Redis 卡片、两张趋势图和详情表；确认图表非空、页签切换后尺寸正确、长文本不遮挡。
10. 主题验收：切换明暗模式和品牌色，确认系统与 Redis 图表、卡片、进度条和状态组件同步更新。

## 七、Execution Order

1. 在 `SystemInfo.vue` 实现统一的当前页签刷新入口，并补齐 Redis 初始化/刷新方法、独立错误去重、请求互斥和最后成功数据保留。
2. 将自动轮询、手动刷新、刷新频率切换、页面可见性、KeepAlive 和 `activeTab` 监听统一为只处理当前数据源；Redis 首次进入懒加载，后续进入立即刷新。
3. 补齐 Redis 图表在页签可见后的初始化、ResizeObserver 注册、主题同步和销毁逻辑，并检查快速切换页签或重复激活时不会重复创建实例或并发请求。
4. 补齐 Redis 页签响应式样式并执行格式化，确保 INFO 长文本、搜索框、工具栏和图表在桌面/移动端不重叠或溢出。
5. 使用 JDK 21 显式开启并运行 Redis/System metrics 单元测试及后端编译；不永久修改 POM 的全局跳过测试设置。
6. 执行前端 `build:type`、定向 ESLint、Stylelint 和生产构建，然后启动前后端完成鉴权、正常/异常 Redis、轮询隔离、桌面/移动布局及明暗主题验收。
