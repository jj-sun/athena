import type { TabValue } from 'tdesign-vue-next';
import type { LocationQueryRaw, RouteRecordName } from 'vue-router';

export interface RouteMeta {
  key?: string;
  title?: string | Record<string, string>;
  icon?: string;
  expanded?: boolean;
  orderNo?: number;
  hidden?: boolean;
  hiddenBreadcrumb?: boolean;
  single?: boolean;
  keepAlive?: boolean;
  frameSrc?: string;
  frameBlank?: boolean;
}

export interface RouteItem {
  path: string;
  name: string;
  component?: Component | string;
  components?: Component;
  redirect?: string;
  meta: RouteMeta;
  children?: Array<RouteItem>;
}

export interface MenuRoute {
  // TODO: menuitem 组件实际支持 string 类型但是类型错误，暂时使用 any 类型避免打包错误待组件类型修复
  path: any;
  title?: string | Record<string, string>;
  name?: string;
  icon?:
    | string
    | {
        render: () => void;
      };
  redirect?: string;
  children: MenuRoute[];
  meta: RouteMeta;
}

export type ModeType = 'dark' | 'light';

interface OriginRoute {
  menuUrl: string;
  menuName?: string;
  hidden?: boolean;
  outLink?: string;
  affix?: boolean;
  cacheable?: boolean;
  iconPrefix?: string;
  icon?: string;
  badge?: string | number;
  isSingle?: boolean;
  children: Array<OriginRoute>;
}

interface BaseTree {
  info: any;
  key: string;
  parentKey: string;
  title: string;
  disabled: boolean;
  children: Array<BaseTree>;
}

export interface UserInfo {
  name: string;
  roles: string[];
}
export interface UserState {
  /* token: string
  userInfo: {
    userId: string
    username: string
    realname: string
    email: string
    mobile: string
    avatar: string

    roleId: string
    roles: string[] | null
  } */
  token: string | undefined;
  username: string;
  realname: string;
  avatar: string;
  info: object;
  permissionList: Array<IBaseTree>;
  roles: string[];

  userId: string;
  roleId: string;
}
export interface IBaseTree {
  info: any;
  key: string;
  parentKey: string;
  label: string;
  disabled: boolean;
  children: Array<IBaseTree>;
}

export interface NotificationItem {
  id: string;
  content: string;
  type: string;
  status: boolean;
  collected: boolean;
  date: string;
  quality: string;
}

export interface TRouterInfo {
  path: string;
  query?: LocationQueryRaw;
  routeIdx?: number;
  title?: string;
  name?: RouteRecordName;
  isAlive?: boolean;
  isHome?: boolean;
  meta?: any;
}

export interface TTabRouterType {
  isRefreshing: boolean;
  tabRouterList: Array<TRouterInfo>;
}

export interface TTabRemoveOptions {
  value: TabValue;
  index: number;
  e: MouseEvent;
}
