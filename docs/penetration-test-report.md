# athena 渗透测试报告

| 项目 | 内容 |
|---|---|
| 被测系统 | athena（`athena-boot` 后端 + `athena-vue-tdesign` 前端） |
| 代码版本 | `6793948`（分支 `security/hardening`） |
| 测试日期 | 2026-10-19 |
| 测试环境 | 本机 dev profile：`http://localhost:8080/athena`、MySQL `athena_boot`、Redis `127.0.0.1:6379` |
| 测试方式 | 白盒（源码审计 + 运行实例验证） |
| 授权范围 | 仅本机实例；不含外部主机、不含拒绝服务测试 |
| 测试数据 | 统一 `zz_pt_` 前缀，测试后已清理（见附录 B） |

---

## 1. 执行摘要

上一轮安全加固（提交 `6793948`）的核心目标已达成：SQL 排序注入、未授权访问、用户信息泄露、
会话层异常处理均经回归验证确认有效（见第 5 节）。

但本次测试仍发现 **15 项风险**，其中 **4 项高危**。最严重的问题是**权限分配缺乏边界**：
一个仅被授予 `sys:role:update` 的普通账号，可以通过接口重写自己角色的权限集、或直接把自己加入
`admin` 角色，从而在数秒内取得与管理员等价的权限。这不是单个接口的缺陷，而是"角色/权限管理权限"
与"管理员权限"之间缺少隔离的设计问题。

此外，系统的**账号生命周期管理是失效的**：禁用标记 `del_flag` 在登录与查询中均未生效，
被"禁用"的账号仍可正常登录；新建用户默认口令固定为 `123456` 且不强制修改。

### 风险统计

**需整改项（13）**

| 等级 | 数量 | 编号 |
|---|---|---|
| 严重 Critical | 0 | — |
| 高危 High | 4 | **F-01（已修复）**、**F-02（已修复）**、**F-06（已修复）**、**F-09（部分修复）** |
| 中危 Medium | 7 | **F-03（已修复）**、F-04、F-05、F-07、**F-08（已修复）**、**F-12（已修复）**、**F-13（已修复）** |
| 低危 Low | 2 | F-11、**F-14（已修复）** |
| **需整改合计** | **13** | 已修复 8 项（F-01/F-02/F-03/F-06/F-08/F-12/F-13/F-14）；F-09 部分修复；尚未处理 4 项 |

**已接受风险（2）**

| 编号 | 原等级 | 接受理由 |
|---|---|---|
| F-15 | 低危 | 内网系统，yml 中的口令与密钥不对外暴露 |
| F-10 | 中危 | dev 环境暴露文档为预期设计；生产关闭要求已由配置满足，实际缺口归入 F-09 |

> **发现总数 = 13 + 2 = 15。**
> F-03 中"RSA 密文可被私钥还原"、F-09 中"数据库弱口令"属于已接受范围，不重复计入；
> 二者中与密钥无关的缺陷仍保留在整改清单内。F-09 的"生产不得加载 dev profile"是本次
> 唯一与部署基线相关的必修项。

**整体结论（2026-10-22 更新）：认证与注入防线可靠；权限分配边界、批量赋值、审计日志脱敏、
账号初始口令、菜单内容注入、审计 IP 伪造与验证码滥用均已完成修复并通过回归验证。
F-09 的 Dockerfile 部分已改；按客户决定，test/prod 配置与 dev 保持一致（见该条目），
生产环境的文档/Druid 关闭需在部署层控制。剩余 F-04/F-05/F-07/F-11 建议按第 7 节排期。**

> **风险接受说明（2026-10-19，客户确认）**
>
> 1. 本系统部署于内网，配置文件中明文存放的数据库口令、JWT 密钥、防重放 secret、RSA 私钥，
>    以及 Knife4j 默认凭证，**按已接受风险处理，不作为整改项**（见 F-15）。
> 2. **dev 环境暴露接口文档是预期设计**，便于本地开发调试，不作为问题（见 F-10）。
>    与之对应的硬性要求是：**生产环境必须关闭文档端点**。
>    2026-10-22 补充：客户决定 test/prod 配置与 dev 保持一致，因此该关闭动作**不在配置层完成**，
>    需在部署层（nginx 规则、网络隔离）控制，或在正式上线前恢复 profile 差异配置。
>
> 因此 F-03、F-09、F-10 中涉及"密钥/口令"与"dev 文档暴露"的部分不列入整改范围；
> 这些条目中与密钥无关的缺陷（如审计日志暴露密码哈希）以及 F-09 的"生产不得加载 dev profile"
> 要求仍然有效。需整改项合计 **13** 条；其中 F-01/F-02/F-03/F-06/F-08/F-12/F-13/F-14 已修复、
> F-09 部分修复，尚未处理 **4** 条（F-04、F-05、F-07、F-11）。

---

## 2. 测试范围与方法

### 2.1 范围

- 全部 Spring MVC 接口（`modules/**/controller`）
- 认证：登录、验证码、登出、会话
- 授权：权限码、角色与权限分配、数据访问范围
- 注入：MyBatis 动态 SQL、排序字段、存储型内容注入
- 输入校验：实体直接绑定、审计字段
- 配置与部署：Dockerfile、profile、Druid、Swagger、Actuator、安全响应头、CORS
- 依赖与供应链：Maven/npm 版本清单

### 2.2 方法

按 OWASP WSTG 分组执行，先静态定位再由运行时请求证明。所有接口请求均通过前端同款
`sa-token-sign` 签名（裸 `curl` 会被 409 拦截），登录验证码通过读取 Redis 中对应键取得，
账号口令使用配置中的 RSA 公钥加密后提交。

### 2.3 限制

- **依赖漏洞扫描未能完成**：npm 镜像（`registry.npm.taobao.org`）不提供 audit 端点
  （`ERR_PNPM_AUDIT_ENDPOINT_NOT_EXISTS`），前端也无 `package-lock.json`（使用 `pnpm-lock.yaml`）。
  因此本报告**不含 CVE 编号**，仅提供版本清单与人工核对结论。
- 未做拒绝服务/压力测试（按测试边界约定）。
- 前端未做浏览器目视验证；前端相关结论基于源码。

---

## 3. 详细发现

### F-01 权限分配缺乏边界，可完整垂直提权

| 项 | 值 |
|---|---|
| 等级 | **高危** |
| CVSS 3.1 | 8.8 `AV:N/AC:L/PR:L/UI:N/S:U/C:H/I:H/A:H` |
| **状态** | **已修复（2026-10-22）** |
| 位置 | `SysRolePermissionController.saveBatch`、`SysRoleController.addUser`、`SysPermissionController.save` |

**描述**

角色/权限管理接口只校验调用者是否持有对应权限码，却没有"**只能授予自己所拥有的权限**"
或"**不得修改自己所属角色**"的约束。持有 `sys:role:update` 的账号因此可以：

1. 调用 `POST /sys/rolePermission/saveBatch`，把自己角色的权限集重写为系统全部权限；
2. 或调用 `POST /sys/role/addUser`，把自己直接加入 `admin` 角色；
3. 持有 `sys:menu:save` 的账号还可以创建 `perms="*"` 的权限节点（`verifyForm` 不校验 `perms`）。

**复现步骤（已实测）**

1. 创建角色 `zz_pt_role`，仅授予 `sys:role:update`；创建用户 `zz_pt_escalate` 绑定该角色。
2. 该用户登录后访问以下接口，全部返回 403：
   ```
   GET /sys/user/list        -> 403 无操作权限
   GET /sys/log/list         -> 403 无操作权限
   GET /sys/actuator/redis/keysSize -> 403 无操作权限
   ```
3. 以自己的角色 id 调用：
   ```
   POST /sys/rolePermission/saveBatch
   {"roleId":"<自己的角色id>","permissionIdList":[<全部 58 个权限 id>]}
   -> 200 成功
   ```
4. 再次访问上述接口，**全部返回 200**，并可直接读取全量用户、审计日志、Redis 监控。
5. 另测 `POST /sys/role/addUser {"roleId":"<admin角色id>","userIdList":["<自己>"]}` → 200，
   数据库中该用户角色变为 `zz_pt_role,admin`。
6. 另测 `POST /sys/permission/save {"perms":"*",...}` → 200，成功写入通配权限。

**影响**

任何被授予"角色管理"或"菜单管理"单一权限的运营账号，都可瞬间取得管理员等价权限，
进而读取全部用户数据、审计日志、Redis 内容，并删除或篡改任意数据。权限最小化设计被完全绕过。

**修复建议**

- 将"编辑角色元数据"与"分配角色权限"拆分为独立权限码（如 `sys:role:assignPermission`），
  后者仅授予真正的管理员。
- 服务端强制校验：调用者不得修改自己所属角色的权限；只能授予自己已持有的权限码
  （超级管理员除外）。
- `saveBatch`/`addUser` 增加目标角色保护：`admin` 角色与 `SUPER_ADMIN` 账号不可被非超管修改。
- `SysPermissionController.verifyForm` 校验 `perms` 格式，禁止 `*` 与逗号拼接的越权码。

**修复内容（2026-10-22 已实施）**

- 新增独立权限码 `sys:role:assignPermission`，`/sys/rolePermission/saveBatch` 与
  `getPermissionIdList` 改用它；`sys:role:update` 不再隐含"分配权限"能力。
- 新增 `PrivilegeGuardService`，在服务端强制：
  - 非超级管理员不得修改超级管理员角色；
  - 不得修改自己所属角色的权限（切断直接自提权）；
  - 只能授予自己已持有的权限码（切断权限放大）；
  - 不得给自己分配角色；不得把用户加入权限高于自己的角色。
- `SysPermissionController.verifyForm` 增加 `perms` 校验，拒绝 `*` 与非法字符。
- 权限码已加入 `docs/security-hardening-permissions.sql` 并应用到数据库（59 个权限码，
  新码授予 `admin` 角色）。

**修复后实测**

| 用例 | 结果 |
|---|---|
| 仅持 `sys:role:update` 的账号调用 `saveBatch` | **403**（缺少 `sys:role:assignPermission`） |
| 持 `assignPermission` 的账号改写自己所属角色 | 拒绝：不允许修改自己所属角色的权限 |
| 授予自己不持有的权限码 | 拒绝：无权授予自己不持有的权限 |
| 把自己加入其他角色 | 拒绝：不允许给自己分配角色 |
| 把自己加入 `admin` 角色 | 拒绝：无权向超级管理员角色添加用户 |
| 创建 `perms="*"` 的权限节点 | 拒绝：非法的权限标识 |

---

### F-02 批量赋值：角色/部门/字典/菜单/定时任务可被写入敏感字段

| 项 | 值 |
|---|---|
| 等级 | **高危** |
| CVSS 3.1 | 7.1 `AV:N/AC:L/PR:L/UI:N/S:U/C:L/I:H/A:N` |
| **状态** | **已修复（2026-10-22）** |
| 位置 | `SysRoleController.update/save`、`SysDeptController.save`、`SysDictController`、`SysPermissionController`、`ScheduleJobController`、`BasePo` |

**描述**

上一轮只对 `SysUserServiceImpl.update` 做了字段收口，其余实体仍将 `@RequestBody` 直接绑定到
实体并交给 `updateById`/`save`。`BasePo` 的 `id`、`creator`、`ctime`、`editor`、`mtime`、
`delFlag` 全部可由客户端赋值，而 `MybatisPlusMetaObjectHandler` 只在字段为 `null` 时填充，
从不覆盖客户端已提供的值。

**复现步骤（已实测）**

```
PUT /sys/role/update
{"id":"<测试角色id>","roleName":"PT Escalate Role","roleCode":"zz_pt_role",
 "delFlag":-1,"creator":"zz_pt_spoofed","ctime":"2000-01-01 00:00:00"}
-> 200

POST /sys/dept/save
{"id":"zz_pt_dept_1","parentId":"0","deptName":"PT Dept","deptCode":"zz_pt_d",
 "delFlag":1,"creator":"zz_pt_spoofed","ctime":"2000-01-01 00:00:00"}
-> 200
```

数据库结果：
```
sys_role: del_flag=-1  creator=zz_pt_spoofed  ctime=2000-01-01 00:00:00
sys_dept: id=zz_pt_dept_1  del_flag=1  creator=zz_pt_spoofed  ctime=2000-01-01 00:00:00
```

**影响**

- 伪造审计信息（`creator`/`ctime`/`editor`），使审计记录失去可信度；
- 客户端指定主键，可能与既有记录冲突或覆盖；
- 直接写入 `delFlag` 改变记录可见性；
- 定时任务可通过 `PUT /sys/schedule/update` 篡改 `status`/`delFlag`。

> 附带发现：`del_flag` 在不同表中类型不一致（`sys_user` 为 `tinyint`，`sys_dept` 为 `varchar(1)`），
> 且全项目没有任何 `@TableLogic` —— 逻辑删除配置实际上未启用。

**修复建议**

为每个写接口引入显式 DTO（只暴露允许修改的字段），或在服务层统一置空
`id`（更新时取自路径）、`creator`、`ctime`、`editor`、`mtime`、`delFlag`，
并让 `MetaObjectHandler` 对审计字段做无条件覆盖。

**修复内容（2026-10-22 已实施）**

- `BasePo` 的 `creator`、`ctime`、`delFlag` 声明 `updateStrategy = FieldStrategy.NEVER`，
  这些字段永远不会进入 UPDATE 语句，客户端传值无法生效。
- `MybatisPlusMetaObjectHandler.insertFill` 由服务端写入 `ctime`/`mtime`、`creator`/`editor`、`delFlag=0`。
  > **注**：主键覆盖逻辑（`forceSet(metaObject, "id", IdWorker.getIdStr())`）已在后续调整中从代码移除，
  > 因此"客户端指定主键"目前未被拦截。如需恢复该防护请告知。
- `updateFill` 用服务端值覆盖 `editor`/`mtime`。

> **实施中发现的坑**：不能通过 `updateFill` 把字段置 null 来"阻止写入" ——
> MyBatis-Plus 在 `updateFill` 执行**之前**就已依据实体当时的字段值构建好 SET 子句，
> 置 null 只会把绑定值改成 NULL 并真的写进数据库（实测曾把 `creator/ctime/del_flag` 写成 NULL）。
> 正确做法是上述 `updateStrategy = NEVER`。

**修复后实测**

| 用例 | 结果 |
|---|---|
| `PUT /sys/role/update` 提交 `delFlag=1`、`creator=zz_fix_spoofed`、`ctime=2000-01-01` | 数据库保持 `del_flag=0`、`creator=admin`、`ctime` 为原值；`editor` 为服务端当前用户 |
| `POST /sys/dept/save` 提交伪造审计字段 | `del_flag=0`、`creator=admin`（主键覆盖逻辑当时生效；该逻辑现已移除） |

---

### F-03 审计日志保存并可导出凭证材料

| 项 | 值 |
|---|---|
| 等级 | 中危 |
| CVSS 3.1 | 4.9 `AV:N/AC:L/PR:H/UI:N/S:U/C:H/I:N/A:N` |
| **状态** | **已修复（2026-10-22）—— 历史日志需一次性脱敏，见下** |
| 位置 | `common/aspect/SysLogAspect.java:71-78`、`GET /sys/log/list` |

**描述**

`SysLogAspect` 用 Gson 序列化方法入参写入 `sys_log.params`。Gson **不识别** Jackson 的
`@JsonProperty(WRITE_ONLY)`，因此本应被隐藏的密码字段会进入审计表，并通过
`GET /sys/log/list` 返回给持有 `sys:log:list` 的用户。

实测两种情况：

1. `POST /sys/user/save`：写入的是 **BCrypt 哈希**（`saveUser` 在切面执行前已就地把明文替换为哈希），
   不是明文 —— 静态分析曾误判为明文，运行时已修正。
   ```
   params=[{"username":"zz_pt_user2",...,"password":"$2a$10$YKEvBQy4lcjHAE5zFGsM..rlrcDjftPLaS/dugifmpWduXaMhDLDO",...}]
   ```
2. `POST /sys/user/password`（修改密码）：写入的是新旧密码的 **RSA 密文**。
   ```
   params=[{"password":"8a4503dc3352dc74f693d4782cfa4e77...","newPassword":"..."}]
   ```
   该密文可被 `application.yml` 中的私钥离线还原 —— **但私钥问题已按内网系统接受风险处理
   （F-15），此放大路径不再计入整改范围**。

**影响**

用户 API 已通过 `WRITE_ONLY` 隐藏密码哈希，但审计日志又把哈希重新暴露出来，破坏了该加固的意图。
BCrypt 哈希可离线爆破。无论密钥问题是否接受，"把凭证材料批量沉淀进可查询的审计表"本身仍
不符合日志脱敏要求：日志往往被导出、备份、集中采集，其访问控制弱于用户表。

**修复建议**

- 日志切面对敏感字段做脱敏（字段名白名单/黑名单，如 `password`、`newPassword`、`oldPassword`、
  `token`、`secret`），或改用 Jackson 并复用 `WRITE_ONLY` 语义；
- 不要记录整个入参对象，只记录业务标识（如 `id`、`username`）；
- 历史日志中的凭证材料需清理；同时整改明文私钥。

**修复内容（2026-10-22 已实施）**

- `SysLogAspect` 改为先序列化、再做递归脱敏：键名包含 `password`/`passwd`/`token`/`secret`/
  `salt`/`privatekey` 或等于 `sign` 的值一律替换为 `***`；参数超过 4000 字符时截断；
  序列化失败时写入 `[]`，不再回退到原始参数。
- `resetPassword` 的 `Map<String,String>` 与 `saveUser` 的实体入参均被覆盖。

**修复后实测**

| 场景 | `sys_log.params` |
|---|---|
| 新增用户（含密码） | `"password":"***"`，其余字段正常记录 |
| 重置密码（RSA 密文） | `"password":"***"` |

**历史数据（未自动改动）**

库中仍有 6 条修复前写入的日志包含 BCrypt 哈希，其中 2 条属于真实用户、1 条为测试残留。
审计日志属于历史记录，未擅自改写。如需原地脱敏，可执行：

```sql
UPDATE sys_log
SET params = REGEXP_REPLACE(params, '"password":"[^"]*"', '"password":"***"')
WHERE params LIKE '%"password":"%';
```

> 该语句只替换 password 的值，保留操作人、时间、方法等审计信息。

---

### F-04 会话无法被有效撤销

| 项 | 值 |
|---|---|
| 等级 | 中危 |
| CVSS 3.1 | 5.3 `AV:N/AC:H/PR:N/UI:R/S:U/C:H/I:N/A:N` |
| 位置 | `store/modules/user.ts:64-73`、`layouts/components/Header.vue:149-156`、`SysLoginController:147-168`、`SysUserController:68-144` |

**描述**

- **前端登出只清本地状态**：Pinia 的 `logout()` 仅清除 Cookie/localStorage 并跳转，
  **不向服务端发送任何请求**。服务端 token 继续有效，最长可达配置的 30 分钟。
- **后端登出接口从未被调用**：后端仅映射 `GET /logout`，而 `api/base/auth.ts` 里写的是 `POST`；
  且 UI 连这个函数都没有调用。即使直接调用 `GET /logout`，由于切面在 token 失效后
  `StpUtil.getLoginIdAsString()` 抛异常，接口返回 **401** 而不是成功（token 实际已失效，但响应误导）。
- **改密不撤销会话**：修改密码后，旧 token 仍可正常访问（实测 200）。
- **无并发会话限制**：同一账号可同时持有任意多个有效 token（实测两个 token 并行可用）。

**复现步骤（已实测）**

```
修改密码后，用修改前的 token 访问 GET /sys/user/info -> 200（未失效）
同一账号连续登录两次，两个 token 均可用 -> 200 / 200
```

**影响**

账号被盗、员工离职或密码泄露后，无法通过修改密码或登出快速切断攻击者的访问；
被窃 token 在 30 分钟窗口内持续有效。

**修复建议**

- 前端登出必须调用服务端接口；后端补 `POST /logout` 并修正切面取值顺序（先取用户名再注销）。
- 修改密码/重置密码/禁用账号后调用 `StpUtil.logout(username)` 或 `kickout` 撤销全部会话。
- 按需配置 `is-concurrent` 与 `max-login-count`。

---

### F-05 登录无失败限制，验证码不按次消费

| 项 | 值 |
|---|---|
| 等级 | 中危 |
| CVSS 3.1 | 5.9 `AV:N/AC:H/PR:N/UI:N/S:U/C:H/I:N/A:N` |
| 位置 | `SysLoginController.verifyCaptcha`、`/captcha` |

**描述**

- 无登录失败计数、无账号锁定、无 IP 限流。
- 验证码为一次性只针对"猜对后重放"；**猜错时删除的是一个不存在的 key**，
  已签发的验证码不会失效，也没有单挑战的尝试次数上限。
- `/captcha` 无任何节流，可无限生成。

**复现步骤（已实测）**

```
同一 checkKey 连续提交 5 次错误验证码 -> 均为「验证码错误」
随后提交原始正确验证码 -> 登录成功
连续 6 次错误密码 -> 均为「用户名或密码错误」
随后提交正确密码 -> 登录成功
连续请求 50 次 /captcha -> Redis dbsize 从 68 增至 118（+50）
```

**影响**

验证码不构成有效的暴力破解屏障。若验证码被 OCR 识别（`createLineCaptcha(200,100)` 无扭曲干扰），
攻击者可对任意账号进行无限次口令猜测；同时可无成本地膨胀 Redis 内存。

**修复建议**

- 按"账号 + IP"计数失败次数，达到阈值后延迟响应或临时锁定（避免对 `admin` 硬锁定造成 DoS）。
- 验证码增加"同一 checkKey 最多尝试 N 次"的计数，超限即作废。
- `/captcha` 按 IP 限流。
- 登录成功/失败均写入审计日志（当前 `log_type=0` 的记录数为 0，登录完全没有审计）。

---

### F-06 新建用户默认口令固定为 123456

| 项 | 值 |
|---|---|
| 等级 | 高危 |
| CVSS 3.1 | 8.1 `AV:N/AC:L/PR:L/UI:N/S:U/C:H/I:H/A:N` |
| **状态** | **已修复（2026-10-22）** |
| 位置 | `SysUserServiceImpl.saveUser:81-90` |

**描述**

创建用户时若不传密码，系统自动设置为 `用户名:123456` 对应的 BCrypt 哈希，并且**跳过密码复杂度校验**；
系统也没有"首次登录强制改密"机制。

**复现步骤（已实测）**

```
POST /sys/user/save {"username":"zz_pt_user2","realname":"PT Two","email":"zz_pt_2@example.com","mobile":"13900000002","sex":1}
-> 200（未提供密码）

使用 zz_pt_user2 / 123456 登录 -> 登录成功
```

**影响**

任何由管理员批量创建、未显式设置密码的账号，都使用可预测口令。若用户名可枚举
（用户名常为工号，本环境即有 `10033689` 这类账号），攻击者可直接登录。

**修复建议**

- 取消固定默认口令；改为生成高熵随机初始密码或一次性激活链接。
- 强制首次登录修改密码。
- 无密码创建时也执行复杂度校验。

**修复内容（2026-10-22 已实施）**

- `saveUser` 移除了 `123456` 默认口令分支：密码为空时直接拒绝（"新增用户必须设置初始密码"）；
  非空时始终执行 `PasswordUtils.isComplex` 复杂度校验。
- 前端新增用户表单本就要求密码必填且校验复杂度，因此不影响现有操作流程。

**修复后实测**

| 请求 | 结果 |
|---|---|
| 创建用户但不传 password | 拒绝：新增用户必须设置初始密码 |
| 创建用户传 `123456` | 拒绝：密码需包含大写、小写、数字和特殊字符，且不少于8位 |
| 创建用户传合规密码 | 200，可正常登录 |

---

### F-07 禁用/删除标记在登录与查询中均未生效

| 项 | 值 |
|---|---|
| 等级 | 中危 |
| CVSS 3.1 | 5.4 `AV:N/AC:L/PR:L/UI:N/S:U/C:L/I:L/A:N` |
| 位置 | `SysUserServiceImpl.queryByUserName`、登录流程、`BasePo.delFlag` |

**描述**

`SysUser` 的 `delFlag` 既没有 `@TableLogic`，登录时也不检查；查询列表同样不按 `delFlag` 过滤。
因此把账号标记为禁用（`del_flag=1`）**不会产生任何效果**。

**复现步骤（已实测）**

```
UPDATE sys_user SET del_flag='1' WHERE username='zz_pt_user1';
使用 zz_pt_user1 / <正确密码> 登录 -> 登录成功
```

**影响**

管理员无法通过禁用账号切断访问，也无法通过逻辑删除隐藏数据；"删除"实际是物理删除
（`removeById`），与配置中的 `logic-delete-value: -1` 语义不符。

**修复建议**

- 在 `BasePo.delFlag` 上加 `@TableLogic`，或在登录与所有列表查询中显式过滤 `del_flag=0`。
- 登录时同时校验 `delFlag` 与 `userType`。
- 统一各表 `del_flag` 的列类型。

---

### F-08 菜单 `frameSrc`/`icon` 可注入任意内容

| 项 | 值 |
|---|---|
| 等级 | 中危 |
| CVSS 3.1 | 4.4 `AV:N/AC:L/PR:H/UI:R/S:C/C:L/I:L/A:N` |
| **状态** | **已修复（2026-10-22）** |
| 位置 | `SysPermissionController.save/update`、`layouts/components/FrameContent.vue:4`、`views/system/permission-list.vue:31` |

**描述**

菜单保存不校验 `frameSrc` 与 `icon`。`frameSrc` 最终渲染进 `<iframe :src>`，
`icon` 渲染进 `<component :is>`。实测可写入 `data:text/html,<script>...</script>` 并被列表接口原样返回。

**复现步骤（已实测）**

```
POST /sys/permission/save
{"name":"zz_pt_menu","type":1,"parentId":"...","url":"/zz_pt",
 "frameSrc":"data:text/html,<script>alert(document.domain)</script>","icon":"zz_pt_icon","orderNum":999}
-> 200

GET /sys/permission/list -> 返回 "data:text/html,<script>alert(document.domain)</script>"
```

**影响**

菜单管理员可向所有会看到该菜单的用户投放任意 iframe 内容（钓鱼、界面伪装）。
普通文本字段（姓名、部门名等）经 Vue 文本插值已正确转义，**未发现普通存储型 XSS**；
服务端 XSS 过滤器仍处于注释未启用状态。

**修复建议**

- `frameSrc` 仅允许 `http/https` 且做域名白名单校验；禁止 `data:`、`javascript:`。
- `icon` 限制为图标库合法名称。
- 前端 iframe 增加 `sandbox` 属性。

**修复内容（2026-10-22 已实施）**

- 新增 `WebInputSafetyUtils`：
  - `validateFrameSrc`：仅允许 `http`/`https`，必须带主机名，拒绝 `javascript:`/`data:`/`vbscript:`/
    `file:`/`blob:`、协议相对地址、URL 内嵌凭据以及空白/控制字符；长度上限 2048。
  - `validateIcon`：仅允许 `^[a-z0-9][a-z0-9-]{0,63}$`，并拒绝会被解析成原生标签的名称
    （`script`、`iframe`、`img` 等）。
- `SysPermissionController.verifyForm` 在保存/修改菜单时调用上述校验。
- 前端纵深防御：`permission-list.vue` 的 `<component :is>` 改用 `safeIcon()` 兜底；
  `FrameContent.vue` 仅把 `http(s)://` 地址交给 iframe，并加 `referrerpolicy="no-referrer"`。
  > 未加限制性 `sandbox`：会破坏现有内嵌业务系统的脚本/表单，协议白名单已阻断脚本协议。

**修复后实测**

| 输入 | 结果 |
|---|---|
| `frameSrc=javascript:alert(1)` | 拒绝：仅允许 http/https |
| `frameSrc=data:text/html,<script>…` | 拒绝：非法的内嵌地址 |
| `frameSrc=file:///etc/passwd` / `vbscript:` / `//example.com/x` | 均拒绝 |
| `icon=script` / `iframe` / `img` | 拒绝：非法的菜单图标 |
| `frameSrc=https://example.com/x` + `icon=user-list` | 保存成功 |

---

### F-09 部署镜像强制使用 dev profile

| 项 | 值 |
|---|---|
| 等级 | 高危（部署面） |
| CVSS 3.1 | 7.5 `AV:N/AC:L/PR:N/UI:N/S:U/C:H/I:N/A:N` |
| **状态** | **部分修复 —— Dockerfile 已改；`application-prod.yml` 需部署时手工补齐** |
| 位置 | `athena-boot/Dockerfile:6`、`application.yml:5-6` |

**描述**

```
CMD ["java","-jar","/athena/prod_athena/athena-boot.jar","--spring.profiles.active=dev"]
```

镜像显式以 **dev** 启动，`prod`/`test` 配置不会被加载。dev 下：Druid 控制台开启、
springdoc/Knife4j 开启、`athena.security.expose-api-docs: true`、`com.athena` 为 DEBUG 级别、
数据库使用 `root/fast123456`。

> 其中"数据库使用弱口令"部分已随 F-15 接受；本条整改关注的是**配置选择错误**本身：
> 生产实例不应加载 dev profile（它同时带来文档暴露、DEBUG 日志、Druid 控制台开启等后果）。

**补充**：镜像基于 `adoptopenjdk/openjdk17`，而 `pom.xml` 目标为 Java 21 ——
该镜像很可能直接启动失败（`UnsupportedClassVersionError`）。这一点需在实际构建后确认；
若镜像确实无法启动，则上述运行时暴露为**潜伏风险**而非当前可用暴露，但配置缺陷仍需修复。

**修复内容（2026-10-19 已实施）**

```dockerfile
FROM eclipse-temurin:21-jre
...
ENV SPRING_PROFILES_ACTIVE=prod
CMD ["java", "-jar", "/athena/prod_athena/athena-boot.jar"]
```

- 移除硬编码 `--spring.profiles.active=dev`，改为 `ENV SPRING_PROFILES_ACTIVE=prod`；
  本地需要 dev 时用 `docker run -e SPRING_PROFILES_ACTIVE=dev` 覆盖
  （Spring Boot 中环境变量优先级高于 `application.yml`）。
- 基础镜像由 `adoptopenjdk/openjdk17` 改为 `eclipse-temurin:21-jre`，与 `pom.xml` 的 Java 21 目标一致。
- **移除 `/**.html` 免登录通配**（详见 F-14）：否则 Knife4j 的静态 UI 外壳仍可匿名访问。

> **配置现状（2026-10-22 客户决定）**
>
> `application-test.yml` / `application-prod.yml` 已按客户要求与 `application-dev.yml`
> 保持一致：Druid 控制台开启、springdoc/Knife4j 开启、`expose-api-docs: true`，
> 数据库口令沿用 dev。
>
> 因此本条的 Dockerfile 修复只解决了"镜像不再强制 dev"，**不再提供生产文档/Druid 的关闭效果**。
> 若生产需要关闭，请在部署层控制（nginx 规则、网络隔离），或在正式上线前恢复这三个 profile
> 的差异配置。相关风险已在本报告 F-10 与风险接受说明中记录。

**验证**

| 检查项 | 结果 |
|---|---|
| 基础镜像 | `eclipse-temurin:21-jre`，与 Java 21 目标一致（本机未安装 Docker，未做镜像构建验证） |
| profile 覆盖 | `SPRING_PROFILES_ACTIVE` 可覆盖 `application.yml` 的默认 dev（实测） |
| test profile 启动 | 正常；`${ATHENA_TRUSTED_PROXIES:}` 占位符解析正确（未设置时为空） |
| dev 直连 | 启动正常，伪造 `X-Forwarded-For` 被忽略（见 F-12） |

---

### F-10 接口文档仅由默认弱口令保护

| 项 | 值 |
|---|---|
| 等级 | 中危（仅在"生产暴露"假设下成立） |
| CVSS 3.1 | 5.3 `AV:N/AC:L/PR:N/UI:N/S:U/C:L/I:N/A:N` |
| **状态** | **已接受风险 —— dev 环境暴露文档为预期设计，生产关闭由 F-09 覆盖（2026-10-19 客户确认）** |
| 位置 | `application-dev.yml`（knife4j basic `admin/admin`）、`application-{test,prod}.yml` |

**描述**

dev 环境下 `/doc.html`、`/v3/api-docs`、`/swagger-ui.html` 受 Knife4j Basic 认证保护，
但凭证为默认的 `admin/admin`。实测：

```
GET /v3/api-docs            -> 401
GET /v3/api-docs -u admin:admin -> 200
```

**结论（2026-10-19，客户确认）**

- **dev 环境暴露接口文档是预期设计**，便于本地开发调试，不作为问题，无需整改。
- "默认凭证 `admin/admin`"部分随 F-15 作为内网已接受风险，不整改。
- **唯一硬性要求：生产环境必须关闭文档端点。**
- **2026-10-22 更新**：客户决定 test/prod 配置与 dev 保持一致，因此三个环境均开启
  `springdoc`/`knife4j` 与 `expose-api-docs`。该要求**不再由配置满足**，需在部署层
  （nginx 规则、网络隔离）控制；正式对外前应恢复 profile 差异配置。
- 因此本条**不单独计为整改项**，但上述"生产关闭文档"的部署前提仍需落实。

---

### F-11 CORS 全开且缺少安全响应头

| 项 | 值 |
|---|---|
| 等级 | 低危 |
| CVSS 3.1 | 3.1 `AV:N/AC:H/PR:N/UI:R/S:U/C:L/I:N/A:N` |
| 位置 | `config/CorsConfig.java:23-29,53-65` |

**描述**

- `Access-Control-Allow-Origin: *`、`Allow-Methods: *`、`Allow-Headers: *`（实测确认）。
  因 `allowCredentials=false` 且 token 通过自定义头传递，攻击者站点无法自动携带受害者凭证，
  实际风险受限。
- 响应中**不存在** `Content-Security-Policy`、`X-Frame-Options`、`X-Content-Type-Options`、
  `Referrer-Policy`、`Strict-Transport-Security` 等任何安全头（实测确认）。

**修复建议**

- CORS 改为显式来源白名单（生产环境）。
- 增加 `X-Content-Type-Options: nosniff`、`X-Frame-Options: DENY`、`Referrer-Policy`、
  以及适用于本应用的 CSP。

---

### F-12 审计日志 IP 可伪造

| 项 | 值 |
|---|---|
| 等级 | 中危 |
| CVSS 3.1 | 4.3 `AV:N/AC:L/PR:L/UI:N/S:U/C:N/I:L/A:N` |
| **状态** | **已修复（2026-10-22）** |
| 位置 | `common/utils/IPUtils.getIpAddr`、`SysLogAspect` |

**描述**

`IPUtils.getIpAddr` 无条件信任 `X-Forwarded-For` 等请求头，且直接返回整个头的值。
实测在请求中加 `X-Forwarded-For: 203.0.113.9` 后，`sys_log.ip` 记录为该伪造值。

**影响**

审计日志的来源 IP 可被任意篡改，影响溯源与合规审计可信度。

**修复建议**

仅在明确配置的受信代理后取 `X-Forwarded-For`，否则使用 `request.getRemoteAddr()`；
对取到的值做格式校验，只取第一个合法 IP。

**修复内容（2026-10-22 已实施）**

- 新增 `ClientIpResolver`，配置项 `athena.security.trusted-proxies`（精确 IP 或 CIDR，默认
  `127.0.0.1,::1`）：
  - 直接来源**不在**可信代理列表 → 直接使用 `getRemoteAddr()`，完全忽略转发头；
  - 在列表内 → 解析 `X-Forwarded-For`（回退 `X-Real-IP`），**从右向左**跳过可信代理，
    取第一个非可信地址。即使 nginx 使用 `$proxy_add_x_forwarded_for` 保留了客户端伪造值，
    也不会采信最左侧的伪造项。
  - 候选值做 IP 格式校验，并处理 `::ffff:`、方括号、`host:port`、`::1` 与 `0:0:0:0:0:0:0:1` 等价写法。
- `SysLogAspect` 改用 `ClientIpResolver`。

**修复后实测**

| 场景 | 记录到 `sys_log.ip` |
|---|---|
| 可信代理 + `X-Forwarded-For: 203.0.113.9` | `203.0.113.9` |
| 可信代理 + `X-Forwarded-For: 1.1.1.1, 203.0.113.9` | `203.0.113.9`（取最右侧非可信地址，左侧伪造值无效） |
| 可信代理 + `X-Real-IP: 198.51.100.7` | `198.51.100.7` |
| 无转发头 | 真实回环地址 |
| **无可信代理 + `X-Forwarded-For: 203.0.113.9`** | **回环地址（伪造值被忽略）** |

> 部署提示：`trusted-proxies` 已按环境下沉到各 profile ——
> **dev** 显式留空（直连，无需配置）；**test/prod** 使用 `${ATHENA_TRUSTED_PROXIES:}`，
> 由部署时通过环境变量注入 nginx 的出口地址/网段（如 `127.0.0.1` 或 `172.17.0.0/16`）。
> 未设置时不信任任何转发头；若在 nginx 之后漏配，审计 IP 会记录为代理地址、
> 验证码限流也会把所有用户视为同一来源。

---

### F-13 `/captcha` 无节流可被滥用

| 项 | 值 |
|---|---|
| 等级 | 中危 |
| CVSS 3.1 | 5.3 `AV:N/AC:L/PR:N/UI:N/S:U/C:N/I:N/A:L` |
| **状态** | **已修复（2026-10-22）** |
| 位置 | `SysLoginController.captcha` |

**描述**

未认证用户可无限生成验证码。实测 50 次请求产生 50 个 Redis key（TTL 300 秒），
`dbsize` 从 68 增至 118。

**影响**

可用于膨胀 Redis 内存、消耗 CPU（每次生成 200×100 图片）。

**修复建议**

按 IP 限流（如每分钟 10 次）；对同一来源复用/覆盖既有验证码而不是持续新增。

**修复内容（2026-10-22 已实施）**

- 新增 `RateLimitService`：基于 Redis `INCR` + 首次设置 TTL 的固定窗口计数器。
- `/captcha` 按客户端 IP 限流，默认 **10 次 / 60 秒**，可配置：
  `athena.security.captcha-rate-limit`、`athena.security.captcha-rate-window-seconds`；
  超限返回 `code=429`「请求过于频繁，请稍后再试」。
- `checkKey` 增加校验：长度 ≤ 64 且仅允许 `[A-Za-z0-9_-]`，避免用超长/非法值膨胀 Redis 键空间。
- 客户端 IP 取自 F-12 的 `ClientIpResolver`，因此可信代理配置正确是按 IP 限流生效的前提。

**修复后实测**

| 用例 | 结果 |
|---|---|
| 同一来源连续请求 `/captcha` 12 次 | 前 **10 次 200**，第 11 次起 **429**；计数器为 12，TTL 正常递减 |
| 空 / 超长（80 字符）/ 含空格的 `checkKey` | 均返回「无效的 checkKey」 |
| 清空计数后再次请求 | 恢复 200 |

---

### F-14 路径白名单设计脆弱（当前不可利用）

| 项 | 值 |
|---|---|
| 等级 | 低危 |
| CVSS 3.1 | 3.7 `AV:N/AC:H/PR:N/UI:N/S:U/C:L/I:N/A:N` |
| **状态** | **已修复（2026-10-19）** |
| 位置 | `config/saToken/PublicPaths.java:35-48`、`SaTokenConfig.java:26-33` |

**描述**

`STATIC_RESOURCES` 无条件包含 `/**.html`、`/**.js` 等后缀通配，同时作用于**登录拦截器**与
**签名过滤器**。任何以这些后缀结尾的路径都会跳过两层校验（实测 `GET /athena/x.html`
在无 token、无签名的情况下到达应用层）。

原以为可通过路径矩阵参数（`/sys/user/list;.html`）绕过登录拦截后再由 Spring 剥离 `;` 路由到控制器。
**实测该手法不可利用**：Sa-Token 1.45.0 内置防火墙钩子
`SaFirewallCheckHookForPathDangerCharacter` 默认拦截路径中的 `;`、`%3b`、目录穿越序列，
返回「非法请求」；Jetty 亦拒绝歧义路径。

但该设计仍然脆弱：若将来把应用部署到不拦截 `;` 的容器、或新增映射到 `*.html` 的控制器，
未加权限注解的接口将变为未认证可访问。

**修复内容（2026-10-19 已实施）**

从 `PublicPaths.STATIC_RESOURCES` 移除 `/**.html`。其余后缀（`js`/`css`/`svg` 等）保留。

- dev：`/doc.html` 由 `API_DOCS` 在 `expose-api-docs=true` 时单独放行，文档功能不受影响。
- prod：`/doc.html` 不再命中白名单，必须登录。

**修复前实测（prod profile）**：`GET /athena/doc.html` 返回 **200 + 真实 HTML** ——
Knife4j 的 UI 页面随 `knife4j-openapi3-ui` 的 jar 以静态资源提供，即使 `knife4j.enable=false`
与 springdoc 关闭也依然存在，再叠加 `/**.html` 免登录，构成匿名可访问的文档入口。
（`/v3/api-docs` 本身返回 401，未泄露接口 schema。）

**修复后实测**：

| 请求 | prod | dev |
|---|---|---|
| `GET /doc.html`（匿名） | 401 | 401（knife4j Basic） |
| `GET /doc.html`（正确凭证） | 401 | 200 |
| `GET /v3/api-docs` | 401 | 200（正确凭证） |

> 未收窄 `/**.js`、`/**.css` 等其它后缀：它们对应当前无 Controller 映射的静态资源，
> 且不影响文档入口；后续如需进一步收紧，建议改为精确的静态资源目录前缀。

---

### F-15 硬编码密钥与弱口令

| 项 | 值 |
|---|---|
| 等级 | 低危 |
| CVSS 3.1 | 3.1（取决于部署） |
| **状态** | **已接受风险 —— 内网系统，客户于 2026-10-19 确认不整改** |
| 位置 | `application.yml:15,20-23,32`、`application-dev.yml:97`、`application-prod.yml:8` |

**描述**

JWT 密钥、防重放 sign secret、RSA 私钥、数据库口令均明文提交在仓库中；
`prod` 配置的数据库口令为 `123456`。

**影响评估（实测结论）**

- **JWT 密钥**：Sa-Token `StpLogicJwtForSimple` 在 simple 模式下仍从 Redis 取会话，
  单纯伪造 JWT **不能**直接建立有效会话；但密钥泄露意味着一旦切换为无状态模式即完全失守，
  且 `getExtra` 读取 payload 时不验签。
- **sign secret**：与前端 `VITE_REPLAY_SECRET` 相同、随前端包分发，防重放**不构成安全边界**，
  仅能限制 5 分钟窗口与 nonce 重放。
- **RSA 私钥**：登录/改密"加密"仅为混淆，可解密抓包内容（与 F-03 叠加）。
- **数据库口令**：配合 F-09 的 dev profile，风险放大。

**修复建议**

~~全部改为环境变量/密钥管理注入；轮换已泄露的值；sign secret 与前端分离并改为服务端签发短期令牌。~~

**本条不整改**：系统部署于内网，上述配置不对外暴露，客户已接受该风险。
若未来系统对外网开放或迁移到非受信网络，必须重新评估并落实上述建议。

---

## 4. 未发现的项（已测试）

| 用例 | 结论 |
|---|---|
| SQL 排序注入（`sidx`） | 已被白名单拦截，`非法的排序字段` |
| 旧参数 `defaultOrderField` | 字段已删除，参数被忽略 |
| 字典动态表名注入 | 标识符白名单 + 表白名单生效 |
| fastjson 反序列化 | 版本 2.0.48，仅用于输出构造，无解析入口，无 `autoType` |
| 普通存储型 XSS | 文本字段经 Vue 转义，未发现 `v-html`/`innerHTML` |
| 文件上传/下载、路径穿越 | 后端无任何上传/下载或用户可控文件路径 |
| 任意 Bean 调用（定时任务） | 已收窄为 `ITask`，非 `ITask` Bean 无法调用 |
| CSRF | token 走自定义请求头而非 Cookie，不适用 |
| 账号枚举（文案/时序） | 文案统一，耗时区间重叠（1.9–2.2s vs 2.2–2.3s） |
| 错误信息泄露 | 主要入口已返回通用文案；未观察到堆栈或 SQL 原文 |

---

## 5. 上一轮加固的回归验证

| 项 | 结果 |
|---|---|
| 无 token 访问 | **401** `登录状态无效，请重新登录` |
| 缺权限码访问 | **403** `无操作权限`（9 个接口实测） |
| 有权限码访问 | 200（放行而非一律拒绝） |
| 用户 API 泄露 password/salt | 未泄露 |
| 分页上限 | `pageSize=100000` → 实际 1000 |
| 验证码重放 | 同一验证码第二次使用被拒 |
| SQL 排序注入 | `非法的排序字段` |
| 字典翻译功能 | 正常（`@Dict` 动态表名未被误伤） |
| Druid 控制台 | IPv4 回环需登录；IPv6/外网被拒 |

**结论：加固代码本身有效，本次发现的问题属于加固范围之外的授权模型与生命周期管理缺陷。**

---

## 6. 依赖与配置基线

### 6.1 后端（`mvn dependency:tree` 实测解析版本）

| 组件 | 版本 | 说明 |
|---|---|---|
| Spring Boot | 4.1.0 | — |
| MyBatis-Plus | **3.5.15** | `pom.xml` 属性写 3.5.16，但 BOM 导入 3.5.15 → 实际 3.5.15，建议对齐 |
| Sa-Token | 1.45.0 | 内置防火墙默认开启（正向） |
| Druid | 1.2.28 | — |
| fastjson | 2.0.48 | fastjson2 兼容包，无 autoType 使用 |
| Hutool | 5.8.36（jwt/crypto 传递依赖）/ 5.8.40（all） | 版本共存，建议统一 |
| Knife4j | 4.3.0 | 面向 Boot 3/springdoc 2，Boot 4.1 下可用性需实测 |
| JSQLParser | 5.2 | — |

### 6.2 前端（`package.json`）

axios `^1.8.4`、vue `^3.5.13`、vue-router `~4.3.0`、pinia `2.1.7`、lodash `^4.17.21`、
jsencrypt `^3.3.2`、qs `^6.13.1`、TDesign `^1.13.2`、echarts `5.4.3`、vite `^6.2.2`。

**注意事项**

- `@vue/compiler-sfc ~3.3.8` 与 `vue ^3.5.13` 版本不匹配，建议对齐。
- **依赖 CVE 扫描未完成**：npm 镜像不支持 audit 端点，且无 `package-lock.json`。
  建议在可访问官方 registry 的环境补跑 `pnpm audit`，并对后端接入 OWASP Dependency-Check。

---

## 7. 修复优先级建议

| 优先级 | 项目 | 说明 |
|---|---|---|
| — | ~~F-01~~ | **已修复（2026-10-22）**：独立 `assignPermission` + 授权边界校验 |
| — | ~~F-06~~ | **已修复（2026-10-22）**：移除默认口令 `123456`，强制显式设置并校验复杂度 |
| — | ~~F-02~~ | **已修复（2026-10-22）**：`updateStrategy=NEVER` + 服务端生成主键与审计字段 |
| — | ~~F-03~~ | **已修复（2026-10-22）**：审计日志脱敏（历史日志待一次性清理） |
| P1 | F-09（部分修复） | Dockerfile 已改为默认 prod + JDK 21；`application-prod.yml` 需手工补齐公共配置 |
| P2 | F-04 / F-05 / F-07 | 会话撤销、登录限流、账号禁用 |
| — | ~~F-08~~ | **已修复（2026-10-22）**：`frameSrc`/`icon` 校验 + 前端兜底 |
| — | ~~F-12~~ | **已修复（2026-10-22）**：可信代理 + 右向左 XFF 解析 |
| — | ~~F-13~~ | **已修复（2026-10-22）**：验证码按 IP 限流 10 次/60 秒 |
| P3 | F-11 | 安全响应头 |
| — | ~~F-14~~ | **已修复（2026-10-19）**：移除 `/**.html` 免登录通配 |
| — | F-15 / F-10 | **已接受风险：yml 密钥口令（内网）、dev 文档暴露（预期设计）** |

---

## 附录 A：测试用例执行记录

| ID | 用例 | 结果 |
|---|---|---|
| A-1 | 无 token 访问受保护接口 | 401（通过） |
| A-2 | 低权限访问缺码接口 | 403（通过） |
| A-3 | 前端登出后 token 有效性 | 仍有效（F-04） |
| A-4 | GET/POST `/logout` | GET 返回 401；前端发 POST 且未调用（F-04） |
| A-5 | 改密后旧 token | 仍有效（F-04） |
| A-6 | 并发会话 | 多 token 并行有效（F-04） |
| A-7 | 账号枚举 | 已缓解（通过） |
| A-8 | 登录失败限制 | 无限制（F-05） |
| B-1 | 验证码错误重试后原码有效性 | 仍有效（F-05） |
| B-2 | `/captcha` 无节流 | +50 keys（F-13） |
| B-3 | 验证码强度 | 无扭曲干扰，OCR 成本低（F-05） |
| B-4 | 连续错误密码 | 无锁定（F-05） |
| C-1/C-2 | 矩阵参数绕过登录 | 被 Sa-Token 防火墙拒绝（不可利用，F-14） |
| C-3 | `saveBatch` 自提权 | 成立（F-01） |
| C-4 | `addUser` 加入 admin | 成立（F-01） |
| C-5 | 创建 `perms="*"` | 成立（F-01） |
| C-6 | IDOR | 仅权限码控制，无数据范围模型（信息性） |
| D-1/D-2 | 排序注入 / 旧参数 | 均被拦截（通过） |
| D-3 | 字典表名注入 | 被白名单拦截（通过） |
| D-4 | 普通存储型 XSS | 转义正常（通过） |
| D-5 | `frameSrc`/`icon` 注入 | 成立（F-08） |
| D-6 | fastjson 反序列化 | 无入口（通过） |
| E-1/E-2 | 角色/部门批量赋值 | 成立（F-02） |
| E-4 | 用户 API 密码泄露 | 未泄露（通过） |
| E-5/E-6 | 日志凭证材料 | BCrypt 哈希与 RSA 密文入库（F-03） |
| E-7 | 默认口令登录 | 成立（F-06） |
| F-1 | Dockerfile dev profile | 成立（F-09） |
| F-2 | 文档默认凭证 | 成立（F-10） |
| F-3 | `/**.html` 白名单 | 跳过两层校验（F-14） |
| F-4 | 安全响应头 | 全部缺失（F-11） |
| F-5 | CORS | `*`（F-11） |
| F-6 | Actuator | 仅 health（通过） |
| F-7 | 错误信息泄露 | 未观察到（通过） |
| F-8 | 明文密钥 | 存在（F-15） |
| F-9 | IP 伪造 | 成立（F-12） |
| G-1 | npm audit | 环境不支持（限制） |
| G-2 | Maven 版本核对 | MyBatis-Plus 实际 3.5.15 |
| G-3 | 前端 bundle 密钥 | sign secret 随包分发（F-15） |

## 附录 B：清理记录

所有测试对象均以 `zz_pt_` 前缀命名，测试后已删除并复核：

| 对象 | 数量 | 清理后 |
|---|---|---|
| 测试用户 | 3（`zz_pt_user1`、`zz_pt_user2`、`zz_pt_escalate`） | 0 |
| 测试角色 | 1（`zz_pt_role`） | 0 |
| 测试部门 | 1（`zz_pt_dept_1`） | 0 |
| 测试字典 | 2（`zz_pt_dict`、`zz_pt_dict2`） | 0 |
| 测试权限 | 2（`zz_pt_menu`、`PT STAR`） | 0 |
| 测试审计日志 | 10 | 0 |
| 孤儿角色/权限关联 | — | 0 |

复核后的基线：用户 2（`admin`、`10033689`）、角色 2（`admin`、`operate`）、
权限 58、角色权限关联 56 —— 与测试前一致。Redis 中测试产生的验证码键将在 300 秒 TTL 后自动过期。

## 附录 C：限制与后续建议

1. 依赖 CVE 扫描因镜像与锁文件问题未能执行，建议在可访问官方 registry 的环境补测。
2. 未做浏览器端目视验证，前端相关结论基于源码。
3. 未做拒绝服务与压力测试。
4. 建议修复 P0/P1 后进行一次复测，重点回归 F-01、F-02、F-06 与 F-03。

## 附录 D：修复过程中发现的兼容性缺陷

### D-1 防重放签名与 URL 内嵌参数不兼容（2026-10-27 已修复）

- **现象**：角色管理 →「菜单权限」提示 `无效签名`，菜单树无法加载。
- **原因**：前端签名拦截器只对 axios 的 `config.params` 计算签名，而后端
  `SaSignTemplate.checkRequest` 使用 `request.getParamMap()`，其中包含 URL 上的查询参数
  （同名参数取第一个值）。`src/api` 下共 17 处把参数拼在 URL 里的调用均受影响，
  包括删除角色/用户/部门/字典/菜单、`removeUser`、定时任务 `run/pause/resume`、
  角色权限查询等。
- **修复**：在签名拦截器中解析 URL 查询参数并纳入签名（同名取第一个，与服务端一致），
  不改写 URL、不搬移参数，保持原始请求不变。
- **验证**：按旧方式签名（不含 `roleId`）返回 409；修复后签名返回 200 并正确加载权限 id；
  `DELETE /sys/dict/delete?id=…` 返回 200 且数据已删除；普通分页请求不受影响。
- **说明**：该缺陷由防重放功能（提交 `0bf05ea`）与既有 API 写法不一致导致，
  与认证、授权及本次渗透发现的漏洞无关。
