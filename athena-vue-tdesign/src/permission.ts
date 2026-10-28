import 'nprogress/nprogress.css'; // progress bar style

import Cookies from 'js-cookie';
import NProgress from 'nprogress'; // progress bar
import { MessagePlugin } from 'tdesign-vue-next';
import type { RouteRecordRaw } from 'vue-router';

import { USER_TOKEN_KEY } from '@/constants';
import router from '@/router';
import { DEFAULT_HOME_PATH, getPermissionStore, getTabsRouterStore } from '@/store';

NProgress.configure({ showSpinner: false });

function hasToken(): boolean {
  const token = Cookies.get(USER_TOKEN_KEY);
  return !!token;
}
router.beforeEach(async (to, from, next) => {
  NProgress.start();

  const permissionStore = getPermissionStore();
  const { whiteListRouters, routesInitialized } = permissionStore;

  if (hasToken()) {
    if (to.path === '/login') {
      next();
      return;
    }
    try {
      if (!routesInitialized) {
        const routeList = await permissionStore.buildAsyncRoutes();
        routeList.forEach((item) => {
          if (item.name && !router.hasRoute(item.name)) {
            router.addRoute(item as RouteRecordRaw);
          }
        });

        // 清理持久化下来的多标签页：上一个账号（或上一版写死的首页）留下的、
        // 当前角色没有权限的路由会从标签栏移除。
        const tabsRouterStore = getTabsRouterStore();
        const tabs = tabsRouterStore.tabRouterList.filter((tab) => !!tab.name && router.hasRoute(tab.name));

        // 仪表盘是所有角色的强制首页，固定保留一个不可关闭的首页标签
        const homeRoute = router.resolve(DEFAULT_HOME_PATH);
        if (homeRoute.matched.length > 0 && homeRoute.name) {
          const homeTab = tabs.find((tab) => tab.path === DEFAULT_HOME_PATH);
          if (homeTab) {
            homeTab.isHome = true;
            homeTab.isAlive = true;
          } else {
            tabs.unshift({
              path: DEFAULT_HOME_PATH,
              name: homeRoute.name,
              title: (homeRoute.meta.title as string) ?? '概览',
              isAlive: true,
              isHome: true,
            });
          }
          tabs.forEach((tab) => {
            tab.isHome = tab.path === DEFAULT_HOME_PATH;
          });
        } else if (tabs.length > 0 && !tabs.some((tab) => tab.isHome)) {
          tabs[0].isHome = true;
        }
        tabsRouterStore.tabRouterList = tabs;

        next({ path: to.fullPath, replace: true, query: to.query });
        return;
      }

      // 根路径按角色落到第一个可访问页面，避免写死 /dashboard/analysis 对无权限角色不可用
      if (to.path === '/') {
        next({ path: permissionStore.getDefaultPath(), replace: true });
        NProgress.done();
        return;
      }

      if (to.matched.length > 0) {
        next();
      } else {
        // 访问了当前角色没有权限/不存在的地址，回退到默认首页而不是 /（/ 又会重定向，容易死循环）
        next({ path: permissionStore.getDefaultPath(), replace: true });
      }
      NProgress.done();
    } catch {
      void MessagePlugin.error('加载菜单失败，请重新登录');
      next({
        path: '/login',
        query: { redirect: to.fullPath },
      });
      NProgress.done();
    }
  } else {
    if (whiteListRouters.includes(to.path)) {
      next();
    } else {
      next({
        path: '/login',
        query: { redirect: to.fullPath },
      });
    }
    NProgress.done();
  }
});

router.afterEach((to) => {
  if (to.path === '/login') {
    const permissionStore = getPermissionStore();

    permissionStore.restoreRoutes().finally(() => {
      NProgress.done();
    });
    // 不再调用 userStore.logout()：
    // - 显式登出走 Header.handleLogout（已调用 user.logout()）
    // - token 失效走 redirectToLogin（已调用 userStore.logout()）
    // - 路由构建失败走 catch 重定向，此时不应清 token，让用户可立即重试登录
  }
});
