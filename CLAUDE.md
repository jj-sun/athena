# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概览

Monorepo，包含两个独立构建、通过 HTTP（已开启 CORS）互相通信的应用：

- **`athena-boot/`** — Java 后端。Spring Boot **4.1.2**（Java **21**）、MyBatis-Plus **3.5.18**、**Sa-Token**（JWT "Simple" 模式，非 Spring Security）、Quartz（JDBC 任务存储）、Druid、Redis（Lettuce）、Jetty（非 Tomcat）、Micrometer/Actuator、nextdoc4j + springdoc 提供 API 文档。
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
- API 文档在 `/athena/doc.html`（nextdoc4j，认证口令见 `application-*.yml` 的 `nextdoc4j.auth`）；Druid 控制台在 `/athena/druid/`（仅 dev 开启）。
- 注意：`pom.xml` 目标是 Java 21，构建与运行都需要 JDK 21。

### 前端（`athena-vue-tdesign/`）

```bash
npm install
npm run dev          # 开发服务器，端口 3002
npm run dev:mock     # 与 dev 等价：mock 插件在 dev 下始终启用
npm run build        # vue-tsc 类型检查 + vite 构建（release 模式）
npm run build:test   # 测试环境构建
npm run lint         # eslint（--max-warnings 0）
npm run lint:fix
npm run stylelint    # stylelint
```

- `VITE_API_URL` 在 `.env` / `.env.development` / `.env.release` 中设为 `http://localhost:8080/athena`；axios 直接使用它（vite 的 `/api` 代理到 `127.0.0.1:3000` 是遗留配置，应用并不走这条代理）。

## 架构

### 后端结构（`com.athena`）

- `AthenaApplication` — 启动入口。
- `modules/` — 业务功能，每个模块自包含 `controller` / `service` + `service/impl` / `mapper` + `mapper/xml` / `entity`：
  - `modules/sys` — 用户、角色、权限、部门、字典、日志、登录。
  - `modules/job` — Quartz 定时任务（`ScheduleJob`、`ScheduleUtils`）。
  - `modules/monitor` — Redis 监控 + 系统指标（通过 `SystemMetricsService`、`ActuatorMetricsController` 使用 Micrometer）。
- `common/` — 共享基础设施：`Result<T>`（统一响应）、`RRException` + `RRExceptionHandler`（全局 `@RestControllerAdvice`）、`base/po/BasePo`（MyBatis-Plus 实体基类，含 `ASSIGN_ID` 主键、审计字段、`delFlag`）、`annotation/Dict` + `aspect/DictSerializer`（字典码 → 标签序列化）、`annotation/Log` + `aspect/SysLogAspect`、`validator/`（JSR-303 分组）、`utils/`（RedisUtils、RsaUtils、SecurityUtil、TreeUtils、PageUtils/Query 等）。
- `config/` — MyBatis-Plus 配置 + 元对象处理器、Redis、CORS、Druid、`saToken/SaTokenConfig`（认证）。

### 约定

- **响应封装**：所有 controller 返回 `Result<T>` = `{ code, message, data, success }`；`code == 200` 表示成功。前端 axios 在 `200` 时解包 `data`，否则 reject（见 `axios.config.ts`）。
- **认证**：Sa-Token + JWT（Simple 模式）。Token 请求头为 `X-Access-Token`。登录接口为 `POST /login`，验证码走 Redis。`SaTokenConfig` 通过 `SaRouter.match("/**").notMatch(…)` 白名单放行公共路径，其余全部由 `StpUtil.checkLogin()` 校验。登录/改密中的 RSA 解密使用 `RsaUtils`（与前端 `src/utils/crypto.ts` 的 hex 密文格式配套）。Spring Security 相关代码已全部移除。
- **持久层**：MyBatis-Plus；删除为**物理删除**（`removeById`），项目未启用逻辑删除。部分表仍保留 `del_flag` 列作为状态标记（如用户禁用）。mapper 使用 `modules/**/mapper/xml` 下的 XML。主键为 `ASSIGN_ID`（雪花字符串）。
- **时间格式**：Jackson 配置为 `GMT+8` 与 `yyyy-MM-dd HH:mm:ss`。

### 前端结构（`src/`）

- `router/modules/` — `baseRoutes.ts`（静态）+ `asyncRoutes.ts`（动态，由权限驱动）。`permission.ts` 守卫通过 `permissionStore.buildAsyncRoutes()` 构建动态路由；`store/modules/permission.ts` 管理路由/菜单状态。
- `store/modules/` — `user`（token/登录状态，持久化）、`permission`、`setting`、`tabs-router`、`notification`。
- `api/` — 按领域分组的 axios 请求函数（`system`、`monitor`、`base`）；`utils/axios.config.ts` 是唯一的 axios 实例，负责注入 token 与 401/500 跳转登录处理。
- `views/` — 页面；`layouts/` — 应用外壳；`components/` — 基于 TDesign 的共享组件；`locales/` — i18n（`zh_CN` / `en_US`）。

## 系统监控

`modules/monitor` 提供系统指标（Micrometer，`SystemMetricsService`、`ActuatorMetricsController`）与 Redis 监控（`/sys/actuator/redis/snapshot`）。前端对应 `src/api/monitor/metrics.ts`、`src/api/monitor/redis.ts`、`src/views/monitor/SystemInfo.vue`；后端测试位于 `athena-boot/src/test/java/com/athena/modules/monitor/`。
