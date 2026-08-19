import type { Component } from 'vue';

/**
 * import.meta.glob 返回类型
 */
export interface VueModule {
  default: Component;
}

/**
 * 路由组件类型
 */
export type RouteComponent = Component | (() => Promise<VueModule>);
