# 安全加固说明与上线手册

本次加固覆盖四类高危问题：SQL 注入、未授权访问、越权（水平/垂直）、以及高危暴露面与登录弱点。
所有结论均基于源码逐条复核，不是静态扫描的推测。

## 一、改动总览

### 1. SQL 注入

| 问题 | 位置 | 处理 |
|---|---|---|
| **`defaultOrderField` 无校验直接拼进 ORDER BY**（本轮最严重，且前端从未使用过该参数） | `Query` / `PageDto` | 从 `PageDto` 中**删除** `defaultOrderField` 与 `isAsc`，默认排序改由调用方以服务端常量传入 |
| `sidx` 仅由弱黑名单过滤（只挡 9 个关键字，且 `toLowerCase()` 会破坏合法列名） | `common/xss/SQLFilter` | 改为白名单正则校验（仅接受标识符），新增 `requireOrderField` / `requireIdentifier` |
| `SysDictMapper.xml` 中 4 处 `${table}/${text}/${code}` 原始拼接 | `SysDictServiceImpl` | 入口处做标识符校验 + 字典表白名单（`athena.dict.allowed-tables`） |
| 分页无上限，可 `pageSize=1000000` 拖库 | `MybatisPlusConfig` / `Query` | 全局 `setMaxLimit(1000)`（`Constant.MAX_PAGE_SIZE`）+ `Query` 上下界钳制 |
| 缺少全表更新/删除防护 | `MybatisPlusConfig` | 注册 `BlockAttackInnerInterceptor` |

**未启用** Druid WallFilter：经核实它此前**从未生效**（starter 的 bean 由 `filter.wall.enabled` 控制，
而旧配置只写了子项 `filter.wall.config.multi-statement-allow`），且该子项为 `true` 时会放开多语句执行。
现显式写为 `enabled: false` 避免被误开。注入风险已在应用层从源头治理。

### 2. 未授权访问（鉴权）

加固前：仅 `SysUserController` 的 6 个方法有 `@SaCheckPermission`，其余约 40 个接口
（角色、菜单、部门、字典、日志、定时任务、监控）**任意登录用户均可调用**。两条现成提权链：

- `GET /sys/role/select` 取到 admin 角色 id → `POST /sys/role/addUser` 把自己加进去；
- `POST /sys/rolePermission/saveBatch` 直接重写任意角色的权限集。

现在为 44 个接口补齐权限码（完整清单见 `security-hardening-permissions.sql` 的 D1 段）。

**刻意不加鉴权的接口**：`/login`、`/logout`、`/captcha`、`/sys/user/password`（自助改密）、
`/sys/permission/nav`（菜单树引导，每个登录用户都要用）。

> 不使用 `@SaCheckRole`：`SaTokenStpInterfaceImpl.getRoleList` 返回 null，角色校验必然失败。

### 3. 越权与敏感信息泄露

| 问题 | 处理 |
|---|---|
| `GET /sys/user/info/{id}` 仅需登录，返回完整实体，含 BCrypt 密码哈希 | 加 `sys:user:info`；`SysUser.password` 改为 `@JsonProperty(WRITE_ONLY)` |
| `SysUser.salt` 死字段被一并导出（实际从未被赋值） | **删除该字段**（全仓库唯一引用即声明处） |
| `PUT /sys/user/update` 批量赋值：可写入 `password`（明文、未哈希）、`delFlag`、审计字段 | `SysUserServiceImpl.update` 中统一置 null，交回 `MetaObjectHandler` |
| 更新时未传 `roleIdList` 会**清空该用户全部角色** | 仅在 `roleIdList != null` 时才改动角色绑定 |
| `POST /sys/user/resetPassword` 可重置 admin 密码 = 账号接管 | 拆分为独立权限码 `sys:user:resetPassword`，并拒绝以超管为目标 |
| 超管账号可被删除、改名、清空角色 | `update` / `delete` / `deleteBatch` / `resetPassword` 均加超管保护 |
| 定时任务 `getBean(beanName)` + 反射调用任意 Bean 方法 | 收窄为按 `ITask` 类型获取，非 `ITask` 直接拒绝 |

> 顺带修掉一个既有 bug：前端编辑用户时提交 `password: ''`，而 `updateById` 会写入空字符串，
> 等于把该用户密码改坏。现在 `password` 在更新路径被显式置 null。

### 4. 登录

- **重新启用验证码**（此前校验块被整体注释）。
  注意原注释代码本身有 bug：`/captcha` 存**原始大小写**，校验时却比对**小写**，
  Hutool 默认验证码含大小写字母，因此含大写字母时必然失败 —— 表现为「偶发登录失败」。
  现统一按小写存储与比对，且校验后立即删除 key（一次性）。
- **消除账号枚举**：用户不存在与密码错误统一为同一文案。
  仅统一文案还不够 —— BCrypt 明显慢于直接返回，故在「用户不存在」分支同样跑一次
  对固定哈希的 `checkpw`，抹平响应时间差。
- `/captcha` 要求 `checkKey` 非空（原先为空会退化成 `md5("null:code")`，所有无 key 客户端共用键空间），TTL 由 30 分钟缩短为 5 分钟。

### 5. 暴露面

- 删除 `TestDictController` 及白名单中的 `/test/dict/**`（完全免登录的演示接口）。
- 清理白名单中**无任何 Controller 映射**的死路径：`/sys/login`、`/sys/user/passwordChange`
  （真实路径是 `/sys/user/password`）、`/sys/common/static/**`、`/sys/common/pdf/**`、`/generic/**`。
- 接口文档路径改为 profile 驱动（`athena.security.expose-api-docs`），仅 dev 放开。
- dev 环境移除 actuator 的 `httptrace` 暴露（它会记录请求头，含 `X-Access-Token`）。
- 新增 `PublicPaths` 常量类，让登录拦截器与签名过滤器共用一份路径定义，避免两处漂移。

> **关于 Druid 控制台的关键认知**：它是 starter 注册的**独立 servlet**，而 `SaInterceptor` 是
> Spring MVC 的 `HandlerInterceptor`，**根本看不到它**。因此「把 `/druid/**` 从免登录白名单里删掉」
> 并不能保护它 —— 必须靠 `stat-view-servlet.enabled` 与自身的账号/来源限制。
> 另外浏览器访问 `/druid/` 无法携带 `X-Access-Token`，无法用登录态兜底。
> 现策略：dev 开启并限制为 `127.0.0.1` + 账号密码 + 禁止重置统计；test/prod 整块关闭。

## 二、环境矩阵

| 配置 | dev | test | prod |
|---|---|---|---|
| Druid 控制台 | 开启（仅 127.0.0.1，需登录） | 关闭 | 关闭 |
| 接口文档 `/doc.html`、`/v3/api-docs` | 开启 | 关闭 | 关闭 |
| `athena.security.expose-api-docs` | `true` | 默认 `false` | 默认 `false` |
| actuator 暴露 | 仅 `health` | 默认 | 默认 |

## 三、上线顺序（不可打乱）

1. **先执行 SQL**：`docs/security-hardening-permissions.sql` 的 A → B → C → D 段。
   D1 必须返回 0 行，D2 确认各角色拿到期望权限。
2. **再发布后端**。顺序依据：`SaTokenStpInterfaceImpl` 对 `admin` 返回 `"*"` 通配，
   即使某个码缺失，管理入口也不会被锁死；受影响的是非 admin 角色。
   > 这一点很关键 —— `admin` **不是**天然超管。`SecurityServiceImpl.getUserPermissions`
   > 给 admin 的只是 `sys_permission` 表中**已存在**的 `perms` 取并集，没有通配语义。
   > 若不做这层通配兜底，新增权限码一旦数据库里没有，**admin 同样会被锁死**。
3. **后端与前端必须同批发布**：验证码为新增的登录必填项，只发一侧会导致无法登录。

## 四、验证清单

> **签名门槛**：除 `CorsConfig` 排除列表外的路径都要先过 `SaServletFilter` 的签名校验，
> 裸 `curl` 会得到 **409** 而不是 401/403 —— 那是在测错层。
> 最省事的办法：登录前端后在 DevTools 里对任一 `/athena/...` 请求「Copy as cURL」，改路径复用。
> 注意 `timestamp-disparity` 为 300 秒且 nonce 去重，每个抓包结果 5 分钟内只能用一次。

| 项 | 验证方式 | 期望 |
|---|---|---|
| 鉴权 | 逐接口以「无 token / 缺权限码的角色 / admin」三种身份调用 | 401 / 403+`无操作权限` / 200 |
| 提权链 | 低权限 token 调 `POST /sys/role/addUser`、`POST /sys/rolePermission/saveBatch`、`GET /sys/schedule/run`、`GET /sys/actuator/redis/keysSize` | 全部 403 |
| 403 不踢下线 | 低权限角色触发一次 403 | 会话存活，不跳 `/login` |
| 敏感字段 | `GET /sys/user/info/{id}`、`/sys/user/list` 响应体 `grep -i 'password\|salt'` | 无匹配；登录/改密/重置密码回归正常 |
| 排序注入 | `?defaultOrderField=(select sleep(3))` 计时；`?order=asc&sidx=sleep(3)` | 前者参数被忽略且立即返回；后者返回「非法的排序字段」；`sidx=ctime` 仍正常 |
| 批量赋值 | 持 `sys:user:update` 提交含 `password`/`delFlag`/`roleIdList` 的 body | 旧密码仍可登录、`delFlag` 不变；**省略 `roleIdList` 时原有角色不被清空** |
| 账号接管 | `POST /sys/user/resetPassword` 目标为 admin id | 拒绝 |
| IDOR | 7 个 `info/{id}` 用缺 `:info` 码的 token | 全部 403 |
| 分页 | `?pageSize=100000` | 最多返回 1000 条；角色-添加用户选择器远程搜索仍可查到用户 |
| 验证码 | 连跑约 20 次登录（覆盖含大写字母的验证码）；同一验证码重放；错验证码 | 20/20 通过（**最易修错的点**）；重放被拒；错误文案统一 |
| 时序枚举 | 对比「存在用户+错密码」与「不存在用户」的 `time_total` | 量级相当 |
| 暴露面（prod） | `curl -i` 访问 `/druid/index.html`、`/v3/api-docs`、`/swagger-ui.html`、`/doc.html` | 全部 404 |
| 暴露面（dev） | 本机访问 `/druid/index.html` | 200 且弹出登录框；换非 127.0.0.1 网卡访问被拒 |
| actuator | `/actuator/httptrace` | 404 |

### 待运行时确认（无法静态断定）

- `knife4j 4.3.0` 面向 Spring Boot 3 / springdoc 2.x，本项目是 Spring Boot 4.1，
  其端点是否真能解析需实测。已在 test/prod 显式关闭，dev 保持不变。
- 静态资源后缀通配（`/**.html`、`/**.js` 等）本次**沿用现状未收窄**，因为无法在本地实测
  Sa-Token 的路由匹配语义。若要收窄，先用一次免登录 `GET /athena/x.html` 探针确认匹配行为。
  当前没有 Controller 映射到这些后缀，属卫生问题而非漏洞。

## 五、本次未处理（按约定保留）

- 配置中仍以明文提交的密钥：JWT 密钥、防重放 sign secret（与前端 `VITE_REPLAY_SECRET` 相同）、
  RSA 私钥、数据库密码、Knife4j `admin/admin`。已在配置文件中加注释标注为待整改项。
  其中 sign secret 是**对称密钥且随前端包分发**，防重放能力仅限时间戳窗口 + nonce 去重，
  不能作为鉴权或身份边界使用。
- 前端 XSS 过滤器（`FilterConfig` 中整个 bean 被注释）仍未启用，本轮不在范围内。
- 项目无租户/数据范围模型（`BasePo` 无 `tenantId`），本轮**未发明**数据范围机制，
  仅落实「持有对应权限码方可访问」。
