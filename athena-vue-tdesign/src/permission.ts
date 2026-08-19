import 'nprogress/nprogress.css'; // progress bar style

import Cookies from 'js-cookie';
import NProgress from 'nprogress'; // progress bar
import { MessagePlugin } from 'tdesign-vue-next';
import type { RouteRecordRaw } from 'vue-router';

import { USER_TOKEN_KEY } from '@/constants';
import router from '@/router';
import { getPermissionStore } from '@/store';

NProgress.configure({ showSpinner: false });

function isTokenExpired(): boolean {
  const token = Cookies.get(USER_TOKEN_KEY);
  return !!token;
}
router.beforeEach(async (to, from, next) => {
  NProgress.start();

  const permissionStore = getPermissionStore();
  const { whiteListRouters, asyncRoutes } = permissionStore;

  if (isTokenExpired()) {
    if (to.path === '/login') {
      next();
      return;
    }
    try {
      if (asyncRoutes.length === 0) {
        const routeList = await permissionStore.buildAsyncRoutes();
        routeList.forEach((item) => {
          if (item.name && !router.hasRoute(item.name)) {
            router.addRoute(item as RouteRecordRaw);
          }
        });

        next({ path: to.fullPath, replace: true, query: to.query });
        return;
      }

      if (to.matched.length > 0) {
        next();
      } else {
        next('/');
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
