# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概览

Monorepo，包含两个独立构建、通过 HTTP（已开启 CORS）互相通信的应用：

- **`athena-boot/`** — Java 后端。Spring Boot **4.1.0**（Java **21**）、MyBatis-Plus 3.5.16、**Sa-Token**（JWT "Simple" 模式，非 Spring Security）、Quartz（JDBC 任务存储）、Druid、Redis（Lettuce）、Jetty（非 Tomcat）、Micrometer/Actuator、Knife4j/springdoc 提供 API 文档。
- **`athena-vue-tdesign/`** — 前端。Vue 3 + Vite 6 + Pinia + TypeScript + TDesign（tdesign-vue-next），并集成 vite-plugin-mock。

## 常用命令

### 后端（`athena-boot/`）

```bash
# 打包（单元测试默认跳过 —— surefire 配置了 <skipTests>true</skipTests>）
mvn clean package

# 启动应用（端口 8080，context-path /athena，profile 为 dev）
mvn spring-boot:run
# 或在 IDE 中运行 com.athena.AthenaApplication

# 运行单个测试类（需覆盖默认的 skip）：
mvn test -DskipTests=false -Dtest=SystemMetricsServiceImplTest
```

- 激活 profile 为 `dev`（在 `application.yml` 中设置）；`application-dev.yml` 包含数据源（MySQL `athena_boot`）、Redis（`127.0.0.1:6379`）、Quartz JDBC 配置。
- 自定义 Maven settings 文件位于 `.trae/maven-test-settings.xml`（指向本地 `file:///D:/apache-maven/repository` 仓库），可用于离线/工作区依赖解析：`mvn -s .trae/maven-test-settings.xml …`。
- Swagger 文档在 `/athena/doc.html`；Druid 控制台在 `/athena/druid/`。
- 注意：`pom.xml` 目标是 Java 21；当前 PATH 上的本地 JDK 是 17 —— 若编译报错请换用 JDK 21。

### 前端（`athena-vue-tdesign/`）

```bash
npm install
npm run dev          # 开发服务器，端口 3002
npm run dev:mock     # 使用 mock API 启动（无需后端）
npm run build        # vue-tsc 类型检查 + vite 构建（release 模式）
npm run build:test   # 测试环境构建
npm run lint         # eslint（--max-warnings 0）
npm run lint:fix
npm run stylelint    # stylelint
```

- `VITE_API_URL` 在 `.env` / `.env.development` 中设为 `http://localhost:8080/athena`；axios 直接使用它（vite 的 `/api` 代理到 `127.0.0.1:3000` 虽有配置，但应用并不走这条代理）。

## 架构

### 后端结构（`com.athena`）

- `AthenaApplication` — 启动入口。
- `modules/` — 业务功能，每个模块自包含 `controller` / `service` + `service/impl` / `mapper` + `mapper/xml` / `entity`：
  - `modules/sys` — 用户、角色、权限、部门、字典、日志、登录。
  - `modules/job` — Quartz 定时任务（`ScheduleJob`、`ScheduleUtils`）。
  - `modules/monitor` — Redis 监控 + 系统指标（通过 `SystemMetricsService`、`ActuatorMetricsController` 使用 Micrometer）。
- `common/` — 共享基础设施：`Result<T>`（统一响应）、`RRException` + `RRExceptionHandler`（全局 `@RestControllerAdvice`）、`base/po/BasePo`（MyBatis-Plus 实体基类，含 `ASSIGN_ID` 主键、审计字段、`delFlag`）、`annotation/Dict` + `aspect/DictSerializer`（字典码 → 标签序列化）、`annotation/Log` + `aspect/SysLogAspect`、`xss/` 过滤器、`validator/`（JSR-303 分组）、`utils/`（RedisUtils、SecurityUtil、TreeUtils、PageUtils/Query 等）。
- `config/` — MyBatis-Plus 配置 + 元对象处理器、Redis、CORS、Druid、`saToken/SaTokenConfig`（认证）。

### 约定

- **响应封装**：所有 controller 返回 `Result<T>` = `{ code, message, data, success }`；`code == 200` 表示成功。前端 axios 在 `200` 时解包 `data`，否则 reject（见 `axios.config.ts`）。
- **认证**：Sa-Token + JWT（Simple 模式）。Token 请求头为 `X-Access-Token`。登录接口为 `POST /login`（验证码已实现但被注释掉）。`SaTokenConfig` 通过 `SaRouter.match("/**").notMatch(…)` 白名单放行公共路径，其余全部由 `StpUtil.checkLogin()` 校验。遗留的 Spring Security `JwtAuthenticationTokenFilter` 已完全注释掉——不要恢复它。
- **持久层**：MyBatis-Plus；逻辑删除通过 `del_flag`（0 = 正常，`-1` = 已删除）；mapper 使用 `modules/**/mapper/xml` 下的 XML。主键为 `ASSIGN_ID`（雪花字符串）。
- **时间格式**：Jackson 配置为 `GMT+8` 与 `yyyy-MM-dd HH:mm:ss`。

### 前端结构（`src/`）

- `router/modules/` — `baseRoutes.ts`（静态）+ `asyncRoutes.ts`（动态，由权限驱动）。`permission.ts` 守卫通过 `permissionStore.buildAsyncRoutes()` 构建动态路由；`store/modules/permission.ts` 管理路由/菜单状态。
- `store/modules/` — `user`（token/登录状态，持久化）、`permission`、`setting`、`tabs-router`、`notification`。
- `api/` — 按领域分组的 axios 请求函数（`system`、`monitor`、`base`）；`utils/axios.config.ts` 是唯一的 axios 实例，负责注入 token 与 401/500 跳转登录处理。
- `views/` — 页面；`layouts/` — 应用外壳；`components/` — 基于 TDesign 的共享组件；`locales/` — i18n（`zh_CN` / `en_US`）。

## 进行中的工作（未提交）

工作区有一项进行中的「系统指标 + Redis 监控」功能：后端 `modules/monitor` 下新增文件（`SystemMetricsService`/`SystemMetricsServiceImpl`、`ActuatorMetricsController`、`RedisMonitorSnapshot`、`SystemMetricsOverview`）以及前端文件（`src/api/monitor/metrics.ts`、`src/api/monitor/redis.ts`、`src/views/monitor/SystemInfo.vue`），另有 `src/test/.../monitor/` 下的单元测试。设计说明见 `.trae/documents/system_metrics_monitor_plan.md` 和 `system_info_redis_monitor_plan.md`。
