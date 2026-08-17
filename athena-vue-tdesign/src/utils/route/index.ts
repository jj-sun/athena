import cloneDeep from 'lodash/cloneDeep';

import type { BaseTree, RouteItem, RouteMeta } from '@/types/interface';
import {
  BLANK_LAYOUT,
  EXCEPTION_COMPONENT,
  IFRAME,
  LAYOUT,
  PAGE_NOT_FOUND_ROUTE,
  PARENT_LAYOUT,
} from '@/utils/route/constant';

// 动态从包内引入单个Icon,如果没有网络环境可以使用这种方式 但是会导致产物存在多个chunk
// const iconsPath = import.meta.glob('../../../node_modules/tdesign-icons-vue-next/esm/components/*.js');

// async function getMenuIcon(iconName: string): Promise<string> {
//   const RenderIcon = iconsPath[`../../../node_modules/tdesign-icons-vue-next/esm/components/${iconName}.js`];

//   const Icon = await RenderIcon();
//   return shallowRef(Icon.default);
// }

const LayoutMap = new Map<string, () => Promise<typeof import('*.vue')>>();

LayoutMap.set('LAYOUT', LAYOUT);
LayoutMap.set('BLANK', BLANK_LAYOUT);
LayoutMap.set('IFRAME', IFRAME);

let dynamicViewsModules: Record<string, () => Promise<Recordable>>;

// 动态引入路由组件
function asyncImportRoute(routes: RouteItem[] | undefined) {
  dynamicViewsModules = dynamicViewsModules || import.meta.glob('../../views/**/*.vue');
  if (!routes) return;

  routes.forEach(async (item) => {
    const { component, name } = item;
    const { children } = item;

    if (component) {
      const layoutFound = LayoutMap.get(component.toUpperCase());
      if (layoutFound) {
        item.component = layoutFound;
      } else {
        item.component = dynamicImport(dynamicViewsModules, component);
      }
    } else if (name) {
      item.component = PARENT_LAYOUT();
    }

    // 动态从包内引入单个Icon,如果没有网络环境可以使用这种方式 但是会导致产物存在多个chunk
    // if (item.meta.icon) item.meta.icon = await getMenuIcon(item.meta.icon);

    children && asyncImportRoute(children);
  });
}

function dynamicImport(dynamicViewsModules: Record<string, () => Promise<Recordable>>, component: string) {
  const keys = Object.keys(dynamicViewsModules);
  const matchKeys = keys.filter((key) => {
    const k = key.replace('../../views', '');
    const startFlag = component.startsWith('/');
    const endFlag = component.endsWith('.vue') || component.endsWith('.tsx');
    const startIndex = startFlag ? 0 : 1;
    const lastIndex = endFlag ? k.length : k.lastIndexOf('.');
    return k.substring(startIndex, lastIndex) === component;
  });
  if (matchKeys?.length === 1) {
    const matchKey = matchKeys[0];
    return dynamicViewsModules[matchKey];
  }
  if (matchKeys?.length > 1) {
    throw new Error(
      'Please do not create `.vue` and `.TSX` files with the same file name in the same hierarchical directory under the views folder. This will cause dynamic introduction failure',
    );
  } else {
    console.warn(`Can't find ${component} in pages folder`);
  }
  return EXCEPTION_COMPONENT;
}

export function transformObjectToRoute<T = RouteItem>(routeList: BaseTree[]): T[] {
  routeList.forEach(async (item) => {
    const route = transformBaseTreeToRouteItem(item);
    const component = route.component as string;
    console.log(route.component);
    if (component) {
      if (component.toUpperCase() === 'LAYOUT') {
        route.component = LayoutMap.get(component.toUpperCase());
      } else {
        route.children = [cloneDeep(route)];
        route.component = LAYOUT;
        route.name = `${route.name}`;
        route.path = ``;
        route.meta = (route.meta || {}) as RouteMeta;
      }
    } else {
      throw new Error('component is undefined');
    }

    route.children && asyncImportRoute(route.children);

    // 动态从包内引入单个Icon,如果没有网络环境可以使用这种方式 但是会导致产物存在多个chunk
    // if (route.meta.icon)
    // route.meta.icon = await getMenuIcon(route.meta.icon);
  });

  return [PAGE_NOT_FOUND_ROUTE, ...routeList] as unknown as T[];
}

function transformBaseTreeToRouteItem(it: BaseTree): RouteItem {
  const route: RouteItem = {
    path: it.info.url,
    name: it.info.id,
    // component: isMenu(it.menuUrl) ? Layout : getComponent(it),
    // component: it.info.url === 'layout' ? Layout : asynComponents[`../views/${it.info.component}.vue`],
    component: it.info.component,
    meta: {
      title: it.title,
      key: it.key,
      hidden: !!it.info.hidden,
      affix: !!it.affix,
      cacheable: !!it.cacheable,
      icon: it.info.icon,
      isSingle: !!it.isSingle,
    },
  };

  return route;
}

// 将背景对象变成路由对象
// export function transformObjectToRoute<T = RouteItem>(routeList: RouteItem[]): T[] {
//   routeList.forEach(async (route) => {
//     const component = route.component as string;

//     if (component) {
//       if (component.toUpperCase() === 'LAYOUT') {
//         route.component = LayoutMap.get(component.toUpperCase());
//       } else {
//         route.children = [cloneDeep(route)];
//         route.component = LAYOUT;
//         route.name = `${route.name}Parent`;
//         route.path = '';
//         route.meta = (route.meta || {}) as RouteMeta;
//       }
//     } else {
//       throw new Error('component is undefined');
//     }

//     route.children && asyncImportRoute(route.children);

//     // 动态从包内引入单个Icon,如果没有网络环境可以使用这种方式 但是会导致产物存在多个chunk
//     // if (route.meta.icon)
//     // route.meta.icon = await getMenuIcon(route.meta.icon);
//   });

//   return [PAGE_NOT_FOUND_ROUTE, ...routeList] as unknown as T[];
// }
/* function loadComponents(): Record<string, () => Promise<Recordable>> {
  dynamicViewsModules = dynamicViewsModules || import.meta.glob('../../views/!**!/!*.vue');
  return dynamicViewsModules;
} */

export function generatorRoutes<T = RouteItem>(routeList: BaseTree[]): T[] {
  dynamicViewsModules = dynamicViewsModules || import.meta.glob('../../views/**/*.vue');
  const tempRoutes: Array<RouteItem> = [];
  routeList.forEach((it) => {
    const layoutFound = LayoutMap.get(it.info.component.toUpperCase());
    const route: RouteItem = {
      path: it.info.url,
      name: it.info.id,
      hidden: !!it.hidden,
      component: layoutFound
        ? LayoutMap.get(it.info.component.toUpperCase())
        : dynamicImport(dynamicViewsModules, it.info.component),
      meta: {
        title: it.title,
        key: it.key,
        affix: !!it.affix,
        cacheable: !!it.cacheable,
        icon: it.info.icon,
        isSingle: !!it.isSingle,
        orderNo: !!it.info.orderNum as number,
      },
    };
    if (it.children) {
      route.children = generatorRoutes(it.children);
    }
    tempRoutes.push(route);
  });
  return tempRoutes;
}
