import type { RouteRecordRaw } from 'vue-router';
import { createRouter, createWebHistory } from 'vue-router';

const env = import.meta.env.MODE || 'development';

const baseRoutes = import.meta.glob('./modules/baseRoutes.ts', { eager: true });

// 导入modules非homepage相关固定路由
const asyncRoutes = import.meta.glob('./modules/asyncRoutes.ts', { eager: true });

export const baseRouterList: Array<RouteRecordRaw> = mapModuleRouterList(baseRoutes);
export const asyncRouterList: Array<RouteRecordRaw> = mapModuleRouterList(asyncRoutes);

export const constantRoutes = [...baseRouterList, ...asyncRouterList];

// 固定路由模块转换为路由
export function mapModuleRouterList(modules: Record<string, unknown>): Array<RouteRecordRaw> {
  const routerList: Array<RouteRecordRaw> = [];
  Object.keys(modules).forEach((key) => {
    // @ts-expect-error 外部赋值不太好直接写类型
    const mod = modules[key].default || {};
    const modList = Array.isArray(mod) ? [...mod] : [mod];
    routerList.push(...modList);
  });
  return routerList;
}

export const getActive = (maxLevel = 3): string => {
  // 非组件内调用必须通过Router实例获取当前路由
  const route = router.currentRoute.value;

  if (!route.path) {
    return '';
  }

  return route.path
    .split('/')
    .filter((_item: string, index: number) => index <= maxLevel && index > 0)
    .map((item: string) => `/${item}`)
    .join('');
};

const router = createRouter({
  history: createWebHistory(env === 'site' ? '/starter/vue-next/' : import.meta.env.VITE_BASE_URL),
  routes: constantRoutes,
  scrollBehavior() {
    return {
      el: '#app',
      top: 0,
      behavior: 'smooth',
    };
  },
});

export default router;
