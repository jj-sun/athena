import { defineStore } from 'pinia';
import type { RouteRecordRaw } from 'vue-router';

import { getNavMenu } from '@/api/base/menu';
import router, { asyncRouterList, baseRouterList } from '@/router';
import { store } from '@/store';
import { generatorRoutes } from '@/utils/route';

/** 所有角色登录后的默认首页：仪表盘-概览 */
export const DEFAULT_HOME_PATH = '/dashboard/analysis';

export function getMenuList() {
  return getNavMenu();
}

interface RouteLike {
  path?: string;
  meta?: { hidden?: boolean; type?: number };
  children?: RouteLike[];
}

function hasRoutePath(routes: RouteLike[], path: string): boolean {
  return routes.some(
    (route) => route.path === path || (route.children?.length ? hasRoutePath(route.children, path) : false),
  );
}

/**
 * 从动态路由中找出第一个可访问的叶子页面（仪表盘不可用时的兜底）。
 */
function findFirstAccessiblePath(routes: RouteLike[]): string | undefined {
  for (const route of routes) {
    if (route.children && route.children.length > 0) {
      const childPath = findFirstAccessiblePath(route.children);
      if (childPath) {
        return childPath;
      }
      continue;
    }
    // 目录节点不能作为落地页（渲染出来是空布局）
    if (route.meta?.hidden === true || route.meta?.type === 0) {
      continue;
    }
    if (route.path) {
      return route.path;
    }
  }
  return undefined;
}

export const usePermissionStore = defineStore('permission', {
  state: () => ({
    whiteListRouters: ['/login'],
    routers: [] as RouteRecordRaw[],
    removeRoutes: [] as RouteRecordRaw[],
    asyncRoutes: [] as RouteRecordRaw[],
    /** 当前用户的按钮级权限码，由 /sys/permission/nav 返回 */
    permissions: [] as string[],
    /** 是否已经完成过一次菜单加载（角色没有任何菜单时 asyncRoutes 仍为空，需要用此标记避免重复加载） */
    routesInitialized: false,
  }),
  actions: {
    async initRoutes() {
      const accessedRouters = this.asyncRoutes;

      // 在菜单展示全部路由
      this.routers = [...baseRouterList, ...accessedRouters, ...asyncRouterList];
      // console.log(this.routers);
      // 在菜单只展示动态路由和首页
      // this.routers = [...homepageRouterList, ...accessedRouters];
      // 在菜单只展示动态路由
      // this.routers = [...accessedRouters];
    },
    async buildAsyncRoutes() {
      try {
        // 发起菜单权限请求 获取菜单列表与按钮权限
        const { menuTree, permissions } = await getMenuList();
        this.permissions = permissions ?? [];
        this.asyncRoutes = generatorRoutes(menuTree ?? []);
        // this.asyncRoutes = [];
        // console.log(this.asyncRoutes);
        this.routesInitialized = true;
        await this.initRoutes();
        return this.asyncRoutes;
      } catch (error) {
        console.log(error);
        throw new Error("Can't build routes", error);
      }
    },
    async restoreRoutes() {
      // 不需要在此额外调用initRoutes更新侧边导肮内容，在登录后asyncRoutes为空会调用
      this.asyncRoutes.forEach((item) => {
        if (item.name) {
          router.removeRoute(item.name);
        }
      });
      this.asyncRoutes = [];
      this.permissions = [];
      this.routesInitialized = false;
    },

    /** 当前用户登录后的默认落地页：仪表盘首页优先，其次第一个有权限的菜单，最后 403 */
    getDefaultPath(): string {
      if (hasRoutePath(this.asyncRoutes, DEFAULT_HOME_PATH)) {
        return DEFAULT_HOME_PATH;
      }
      return findFirstAccessiblePath(this.asyncRoutes) ?? '/403';
    },
  },
});

export function getPermissionStore() {
  return usePermissionStore(store);
}
