import { defineStore } from 'pinia';
import type { RouteRecordRaw } from 'vue-router';

import { getNavMenu } from '@/api/base/menu';
import router, { asyncRouterList, baseRouterList } from '@/router';
import { store } from '@/store';
import type { BaseTree } from '@/types/interface';
import { generatorRoutes } from '@/utils/route';

export function getMenuList() {
  return getNavMenu();
}

export const usePermissionStore = defineStore('permission', {
  state: () => ({
    whiteListRouters: ['/login'],
    routers: [] as RouteRecordRaw[],
    removeRoutes: [] as RouteRecordRaw[],
    asyncRoutes: [] as RouteRecordRaw[],
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
        // 发起菜单权限请求 获取菜单列表
        const menuTree: Array<BaseTree> = (await getMenuList()).menuTree;
        console.log(menuTree);
        this.asyncRoutes = generatorRoutes(menuTree);
        // this.asyncRoutes = [];
        // console.log(this.asyncRoutes);
        await this.initRoutes();
        return this.asyncRoutes;
      } catch (error) {
        console.log(error);
        throw new Error("Can't build routes", error);
      }
    },
    async restoreRoutes() {
      // 不需要在此额外调用initRoutes更新侧边导肮内容，在登录后asyncRoutes为空会调用
      this.asyncRoutes.forEach((item: RouteRecordRaw) => {
        if (item.name) {
          router.removeRoute(item.name);
        }
      });
      this.asyncRoutes = [];
    },
  },
});

export function getPermissionStore() {
  return usePermissionStore(store);
}
