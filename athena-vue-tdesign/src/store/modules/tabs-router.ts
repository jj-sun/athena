import { defineStore } from 'pinia';

import { store } from '@/store';
import type { TRouterInfo, TTabRouterType } from '@/types/interface';

const state = {
  // 不再写死“仪表盘 /dashboard/base”首页：不同角色可访问的首页不同，
  // 第一个打开的标签会自动标记为 isHome。
  tabRouterList: [] as Array<TRouterInfo>,
  isRefreshing: false,
};

// 不需要做多标签tabs页缓存的列表 值为每个页面对应的name 如 DashboardDetail
// const ignoreCacheRoutes = ['DashboardDetail'];
const ignoreCacheRoutes = ['login'];

export const useTabsRouterStore = defineStore('tabsRouter', {
  state: () => state,
  getters: {
    tabRouters: (state: TTabRouterType) => state.tabRouterList,
    refreshing: (state: TTabRouterType) => state.isRefreshing,
  },
  actions: {
    // 处理刷新
    toggleTabRouterAlive(routeIdx: number) {
      this.isRefreshing = !this.isRefreshing;
      this.tabRouters[routeIdx].isAlive = !this.tabRouters[routeIdx].isAlive;
    },
    // 处理新增
    appendTabRouterList(newRoute: TRouterInfo) {
      // 不要将判断条件newRoute.meta.keepAlive !== false修改为newRoute.meta.keepAlive，starter默认开启保活，所以meta.keepAlive未定义时也需要进行保活，只有显式说明false才禁用保活。
      const needAlive = !ignoreCacheRoutes.includes(newRoute.name as string) && newRoute.meta?.keepAlive !== false;
      if (!this.tabRouters.find((route: TRouterInfo) => route.path === newRoute.path)) {
        // 第一个标签作为“首页”标签，不可关闭、不可拖动
        const isHome = this.tabRouterList.length === 0;
        this.tabRouterList = this.tabRouterList.concat({ ...newRoute, isAlive: needAlive, isHome });
      }
    },
    // 处理关闭当前
    subtractCurrentTabRouter(newRoute: TRouterInfo) {
      const { routeIdx } = newRoute;
      this.tabRouterList = this.tabRouterList.slice(0, routeIdx).concat(this.tabRouterList.slice(routeIdx + 1));
    },
    // 处理关闭右侧
    subtractTabRouterBehind(newRoute: TRouterInfo) {
      const { routeIdx } = newRoute;
      const homeIdx = this.tabRouters.findIndex((route: TRouterInfo) => route.isHome);
      let tabRouterList: Array<TRouterInfo> = this.tabRouterList.slice(0, routeIdx + 1);
      if (homeIdx >= 0 && routeIdx < homeIdx) {
        tabRouterList = tabRouterList.concat(this.tabRouterList[homeIdx]);
      }
      this.tabRouterList = tabRouterList;
    },
    // 处理关闭左侧
    subtractTabRouterAhead(newRoute: TRouterInfo) {
      const { routeIdx } = newRoute;
      const homeIdx = this.tabRouters.findIndex((route: TRouterInfo) => route.isHome);
      let tabRouterList: Array<TRouterInfo> = this.tabRouterList.slice(routeIdx);
      if (homeIdx >= 0 && routeIdx > homeIdx) {
        tabRouterList = [this.tabRouterList[homeIdx], ...tabRouterList];
      }
      this.tabRouterList = tabRouterList;
    },
    // 处理关闭其他
    subtractTabRouterOther(newRoute: TRouterInfo) {
      const { routeIdx } = newRoute;
      const homeIdx = this.tabRouters.findIndex((route: TRouterInfo) => route.isHome);
      if (routeIdx === homeIdx) {
        this.tabRouterList = [this.tabRouterList[routeIdx]];
      } else {
        const current = this.tabRouterList[routeIdx];
        this.tabRouterList = homeIdx >= 0 ? [this.tabRouterList[homeIdx], current] : [current];
      }
    },
    removeTabRouterList() {
      this.tabRouterList = [];
    },
    initTabRouterList(newRoutes: TRouterInfo[]) {
      newRoutes?.forEach((route: TRouterInfo) => this.appendTabRouterList(route));
    },
  },
  persist: true,
});

export function getTabsRouterStore() {
  return useTabsRouterStore(store);
}
