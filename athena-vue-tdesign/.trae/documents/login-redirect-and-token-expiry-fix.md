# 登录跳转与 Token 失效跳转问题修复计划

## 总结

修复两个前端问题（按用户要求**仅改前端**，不动后端 athena-boot）：

1. **登录后不及时跳转首页 / 需多次登录**：登录后 push 到 `/dashboard`，但动态生成的 `/dashboard` 父路由无 `redirect`，落到无子视图的 Layout（白屏）；同时路由守卫的 catch 块与 `afterEach` 钩子耦合，在动态路由构建偶发失败时误清 token，导致用户被强制登出。
2. **Token 失效后不及时返回登录页**：响应拦截器用 `code===500 && message.includes('token')` 字符串匹配判断失效，`message` 为 null 时直接抛 TypeError 跳转永不触发；错误拦截器（非 2xx）不处理 401；`redirectToLogin` 还有 1.5s 延迟。

---

## 现状分析（根因）

### 问题 1 根因

**根因 A（主）：登录默认跳转目标错误** — [Login.vue:146](file:///e:/github/athena-vue-tdesign/src/views/login/components/Login.vue#L146)
```js
const redirectUrl = redirect ? decodeURIComponent(redirect) : '/dashboard';
```
登录后 push `/dashboard`。但动态路由由 [route/index.ts:165-193](file:///e:/github/athena-vue-tdesign/src/utils/route/index.ts#L165-L193) 的 `generatorRoutes` 生成，**父路由不设置 `redirect`**。`/dashboard` 作为父路由只渲染 Layout、无子视图 → 白屏，用户以为没跳转。而 [baseRoutes.ts:28-32](file:///e:/github/athena-vue-tdesign/src/router/modules/baseRoutes.ts#L28-L32) 中 `/`（name `root`）配置了 `redirect: '/dashboard/analysis'`，push `/` 才能正确落到具体页面。

**根因 B（次）：路由守卫 catch 与 afterEach 耦合误清 token** — [permission.ts:47-54](file:///e:/github/athena-vue-tdesign/src/permission.ts#L47-L54) 与 [permission.ts:68-75](file:///e:/github/athena-vue-tdesign/src/permission.ts#L68-L75)
```js
// catch 块：buildAsyncRoutes 失败时重定向到 /login
} catch (error) {
  next({ path: '/login', query: { redirect: encodeURIComponent(to.fullPath) } });
}
// afterEach：任何进入 /login 都登出（清 token）
router.afterEach((to) => {
  if (to.path === '/login') {
    userStore.logout();            // ← 误清 token
    permissionStore.restoreRoutes();
  }
});
```
`buildAsyncRoutes()`（调用 `/sys/permission/nav` 菜单接口）偶发失败时，catch 重定向到 `/login`，afterEach 随即 `userStore.logout()` 清掉刚登录的 token → 用户被登出，需重新登录。`getMenuList` 网络抖动即触发。

**根因 C（次）：`router.hasRoute(to.name)` 判断不稳** — [permission.ts:42-46](file:///e:/github/athena-vue-tdesign/src/permission.ts#L42-L46)
```js
if (router.hasRoute(to.name as string)) { next(); } else { next('/'); }
```
`to.name` 为 undefined 时 `router.hasRoute(undefined)` 返回 false，落到 `next('/')`，多一次跳转。

### 问题 2 根因

**根因 A（主）：token 失效检测极脆弱** — [axios.config.ts:64-78](file:///e:/github/athena-vue-tdesign/src/utils/axios.config.ts#L64-L78)
```js
if (response.data.code === 500 && response.data.message.includes('token')) {
```
- 后端 [RRExceptionHandler.java:42-46](file:///e:/github/athena-boot/src/main/java/com/athena/common/exception/RRExceptionHandler.java#L42-L46) 无 `NotLoginException` 专用处理器，Sa-Token 失效异常被通用 `Exception` 分支捕获，返回 `{ code:500, message: e.getMessage() }`（HTTP 200）。
- `message` 为 null/undefined 时 `.includes('token')` 抛 TypeError，跳转永不触发。
- 仅靠字符串 "token" 匹配，大小写敏感，覆盖面窄。

**根因 B（次）：错误拦截器不处理 401** — [axios.config.ts:79-87](file:///e:/github/athena-vue-tdesign/src/utils/axios.config.ts#L79-L87)
非 2xx 响应（如 401/403）只 reject，不跳转登录。

**根因 C（次）：跳转延迟** — [axios.config.ts:98-108](file:///e:/github/athena-vue-tdesign/src/utils/axios.config.ts#L98-L108)
`redirectToLogin` 用 `setTimeout(..., 1500)` 延迟 1.5s 才跳转，导致"不及时"。

---

## 拟定修改（仅前端）

### 修改 1：`src/views/login/components/Login.vue`（问题 1 主修复）

**位置**：[Login.vue:146](file:///e:/github/athena-vue-tdesign/src/views/login/components/Login.vue#L146)

**改法**：默认跳转目标 `/dashboard` → `/`，利用 `root` 路由的 `redirect: '/dashboard/analysis'` 落到具体页面。

```js
const redirectUrl = redirect ? decodeURIComponent(redirect) : '/';
```

**为什么**：`/dashboard` 父路由无 redirect 会白屏；`/` 会重定向到 `/dashboard/analysis`。有 `redirect` 查询参数时仍 honoring 原值。

### 修改 2：`src/permission.ts`（问题 1 次修复：解耦 afterEach + 稳健路由判断）

**位置**：[permission.ts:42-46](file:///e:/github/athena-vue-tdesign/src/permission.ts#L42-L46) 与 [permission.ts:68-75](file:///e:/github/athena-vue-tdesign/src/permission.ts#L68-L75)

**改法 A**：`router.hasRoute(to.name)` → `to.matched.length > 0`，避免 `to.name` 为 undefined 时误判。
```js
if (to.matched.length > 0) {
  next();
} else {
  next('/');
}
```

**改法 B**：`afterEach` 中**移除 `userStore.logout()`，保留 `permissionStore.restoreRoutes()`**。
```js
router.afterEach((to) => {
  if (to.path === '/login') {
    const permissionStore = getPermissionStore();
    permissionStore.restoreRoutes();
    // 不再调用 userStore.logout()：
    // - 显式登出走 Header.handleLogout（已调用 user.logout()）
    // - token 失效走 redirectToLogin（已调用 userStore.logout()）
    // - 路由构建失败走 catch 重定向，此时不应清 token，让用户可立即重试登录
  }
  NProgress.done();
});
```

**为什么**：
- `restoreRoutes()` 仍需保留，因为 [Header.vue:164-172](file:///e:/github/athena-vue-tdesign/src/layouts/components/Header.vue#L164-L172) 的 `handleLogout` 和 `redirectToLogin` 都没调用它，afterEach 是兜底清理动态路由的唯一入口。
- 移除 `logout()` 后：构建路由偶发失败 → catch 重定向 /login → afterEach 只清路由不清 token → 用户保留登录态可立即重试，不再被强制登出。
- 显式登出（Header）和 token 失效（axios）仍各自显式调用 `logout()`，行为不变。

### 修改 3：`src/utils/axios.config.ts`（问题 2 修复：鲁棒识别 + 立即跳转）

**位置**：[axios.config.ts:64-88](file:///e:/github/athena-vue-tdesign/src/utils/axios.config.ts#L64-L88) 与 [axios.config.ts:91-109](file:///e:/github/athena-vue-tdesign/src/utils/axios.config.ts#L91-L109)

**改法 A**：响应拦截器中鲁棒识别 token 失效。
```js
service.interceptors.response.use(
  (response: AxiosResponse<Result<any>>) => {
    const { code, message, data } = response.data || {};
    // 鲁棒识别 Sa-Token NotLoginException（后端通用异常处理器返回 code=500 + 含 token/未登录 的 message）
    if (code === 500 && message && /token|未登录|登录已过期/i.test(message)) {
      redirectToLogin();
      return Promise.reject(new Error('Token expired'));
    }
    if (code === 200) {
      return data;
    }
    return Promise.reject(new BusinessError(code, message, data));
  },
  ...
);
```
要点：
- `response.data || {}` 与 `message &&` 防御 null/undefined，避免 TypeError。
- 正则 `/token|未登录|登录已过期/i` 覆盖 Sa-Token 1.45.0 所有 NotLoginException 消息（"未能读取到有效token"、"token已过期"、"token无效"、"token已被顶下线/踢下线/冻结"），大小写不敏感。

**改法 B**：错误拦截器（非 2xx）增加 401/403 跳转。
```js
(error) => {
  const status = error?.response?.status;
  if (status === 401 || status === 403) {
    redirectToLogin();
    return Promise.reject(new Error('Unauthorized'));
  }
  if (import.meta.env.MODE === 'development') {
    console.log(error);
  }
  error.name = 'ApiError';
  error.code = status || 500;
  error.message = error?.response?.data?.error || '服务器异常，请稍后重试…';
  return Promise.reject(error);
},
```

**改法 C**：`redirectToLogin` 去掉 1.5s `setTimeout`，立即跳转。
```js
const redirectToLogin = () => {
  if (isRedirecting) return;
  isRedirecting = true;
  NProgress.start();
  MessagePlugin.error('登录已过期，请重新登录');
  const userStore = useUserStore();
  userStore.logout();
  router.replace({
    path: '/login',
    query: { redirect: router.currentRoute.value.fullPath },
  });
  isRedirecting = false;
  NProgress.done();
};
```
`isRedirecting` 仍防重入；TDesign `MessagePlugin` 全局 toast 跨路由存活，提示不会丢。

---

## 假设与决策

1. **仅改前端**（用户决定）：不新增后端 `NotLoginException` 处理器。前端用正则匹配 Sa-Token 消息识别失效，覆盖该框架 1.45.0 全部 NotLoginException 类型消息。
2. **登录后跳 `/`**（用户决定）：依赖 `root` 路由 `redirect: '/dashboard/analysis'`。假设后端菜单存在 `/dashboard/analysis` 这条记录（现状下 `/` 已配置该 redirect，说明该路径有效）。
3. **保留 `afterEach` 的 `restoreRoutes()`**：因 `Header.handleLogout` 与 `redirectToLogin` 均未调用它，afterEach 是动态路由清理的唯一兜底，不能整体删除，仅删 `logout()`。
4. **不动 `generatorRoutes`**：不补父路由 `redirect`，因为改跳 `/` 已规避白屏问题，最小改动。
5. **不动 user store 的 persist 配置**：token 双存储（cookie + localStorage persist）虽不优雅，但路由守卫与 axios 均以 cookie 为准，不影响本次修复。

---

## 验证步骤

1. **类型检查**：`npx vue-tsc --noEmit` 通过，无类型错误。
2. **问题 1 验证（登录跳转）**：
   - 清浏览器存储，访问 `/`，被重定向到 `/login`。
   - 输入 admin/123456 登录 → 应**立即**跳到 `/dashboard/analysis`，无白屏、无需多次点击。
   - 模拟菜单接口失败（断网/改后端返回 500）：登录后应落到 `/login` 且**不被清 token**（cookie 仍在），可立即重试登录恢复。
3. **问题 2 验证（token 失效跳转）**：
   - 登录后，在 Redis 删除对应 token（或等 30 分钟超时）。
   - 触发任意接口请求 → 应**立即**（无 1.5s 延迟）弹出"登录已过期"并跳回 `/login`，URL 带 `redirect` 参数。
   - 重新登录后能跳回原页面。
4. **显式登出回归**：点 Header 登出 → 跳 `/login`，动态路由被清理；重新登录后菜单正常重建。
5. **并发请求回归**：token 失效时多个接口并发失败，只弹一次提示、只跳转一次（`isRedirecting` 生效）。
