import type { RouteRecordRaw } from 'vue-router';

import type { BaseTree } from '@/types/interface';
import type { RouteComponent, VueModule } from '@/types/route';
import { BLANK_LAYOUT, EXCEPTION_COMPONENT, IFRAME, LAYOUT } from '@/utils/route/constant';

const LayoutMap = new Map<string, RouteComponent>();

LayoutMap.set('LAYOUT', LAYOUT);
LayoutMap.set('BLANK', BLANK_LAYOUT);
LayoutMap.set('IFRAME', IFRAME);

/**
 * 动态页面
 */
export const dynamicViewsModules = import.meta.glob<VueModule>('../../views/**/*.vue');
/**
 * 获取组件
 */
export function resolveComponent(component: string): RouteComponent {
  /**
   * Layout
   */
  const layout = LayoutMap.get(component.toUpperCase());

  if (layout) {
    return layout;
  }
  /**
   * views组件
   */
  return dynamicImport(component);
}

/**
 * 动态加载views
 */
function dynamicImport(component: string): RouteComponent {
  const keys = Object.keys(dynamicViewsModules);
  const match = keys.filter((key) => {
    const path = key.replace('../../views', '').replace('.vue', '');
    return path === `/${component}` || path === component;
  });
  if (match.length === 1) {
    return dynamicViewsModules[match[0]];
  }
  if (match.length > 1) {
    throw new Error(`存在重复页面:${component}`);
  }
  console.warn(`找不到页面:${component}`);
  return EXCEPTION_COMPONENT;
}

export function generatorRoutes(routeList: BaseTree[]): RouteRecordRaw[] {
  return (
    routeList
      // 目录（type=0）没有任何子菜单时没有可展示的内容，直接丢弃，避免侧边栏出现点进去是空白的菜单
      .filter((it) => !(it.info.type === 0 && !it.children?.length))
      .map((it): RouteRecordRaw => {
        return {
          path: it.info.url,
          name: it.info.id,
          component: resolveComponent(it.info.component),
          meta: {
            title: it.title,
            key: it.key,
            icon: it.info.icon,
            single: !!it.info.isSingle,
            hidden: !it.info.hidden,
            orderNo: it.info.orderNum as number,
            frameSrc: it.info.frameSrc,
            frameBlank: it.info.frameBlank === 0,
            // 0=目录 1=菜单，用于计算登录后的默认落地页
            type: it.info.type,
          },
          children: it.children?.length ? generatorRoutes(it.children) : undefined,
        };
      })
  );
}
