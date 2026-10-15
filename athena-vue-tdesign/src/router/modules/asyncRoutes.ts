import IFrame from '@/layouts/frame/index.vue';
import Layout from '@/layouts/index.vue';

export default [
  /*  {
    path: '/dashboard',
    component: Layout,
    redirect: '/dashboard/base',
    name: 'dashboard',
    meta: {
      title: '仪表盘',
      icon: shallowRef(DashboardIcon),
      orderNo: 0,
    },
    children: [
      {
        path: 'base',
        name: 'DashboardBase',
        component: () => import('@/views/dashboard/base/index.vue'),
        meta: {
          title: '概览仪表盘',
        },
      },
      {
        path: 'detail',
        name: 'DashboardDetail',
        component: () => import('@/views/dashboard/detail/index.vue'),
        meta: {
          title: '统计报表',
        },
      },
    ],
  }, */
  {
    path: '/form',
    name: 'form',
    component: Layout,
    redirect: '/form/base',
    meta: {
      title: '表单页',
      icon: 'edit-1',
      orderNo: 100,
    },
    children: [
      {
        path: 'base',
        name: 'FormBase',
        component: () => import('@/views/form/base/index.vue'),
        meta: {
          title: '基础表单页',
        },
      },
      {
        path: 'step',
        name: 'FormStep',
        component: () => import('@/views/form/step/index.vue'),
        meta: {
          title: '分步表单页',
        },
      },
    ],
  },
  {
    path: '/frame',
    name: 'Frame',
    component: Layout,
    redirect: '/frame/doc',
    meta: {
      icon: 'internet',
      title: '外部页面',
      orderNo: 101,
    },
    children: [
      {
        path: 'doc',
        name: 'Doc',
        component: IFrame,
        meta: {
          frameSrc: 'https://tdesign.tencent.com/starter/docs/vue-next/get-started',
          title: '使用文档（内嵌）',
        },
      },
      {
        path: 'TDesign',
        name: 'TDesign',
        component: IFrame,
        meta: {
          frameSrc: 'https://tdesign.tencent.com/vue-next/getting-started',
          title: 'TDesign 文档（内嵌）',
        },
      },
      {
        path: 'TDesign2',
        name: 'TDesign2',
        component: IFrame,
        meta: {
          frameSrc: 'https://tdesign.tencent.com/vue-next/getting-started',
          frameBlank: true,
          title: 'TDesign 文档（外链',
        },
      },
    ],
  },
  {
    path: '/result',
    name: 'result',
    component: Layout,
    redirect: '/result/success',
    meta: {
      title: '结果页',
      icon: 'check-circle',
      orderNo: 102,
    },
    children: [
      {
        path: 'success',
        name: 'ResultSuccess',
        component: () => import('@/views/result/success/index.vue'),
        meta: {
          title: '成功页',
        },
      },
      {
        path: 'fail',
        name: 'ResultFail',
        component: () => import('@/views/result/fail/index.vue'),
        meta: {
          title: '失败页',
        },
      },
      {
        path: 'network-error',
        name: 'ResultNetworkError',
        component: () => import('@/views/result/network-error/index.vue'),
        meta: {
          title: '网络异常',
        },
      },
      {
        path: '403',
        name: 'Result403',
        component: () => import('@/views/result/403/index.vue'),
        meta: { title: '无权限' },
      },
      {
        path: '404',
        name: 'Result404',
        component: () => import('@/views/result/404/index.vue'),
        meta: { title: '访问页面不存在页' },
      },
      {
        path: '500',
        name: 'Result500',
        component: () => import('@/views/result/500/index.vue'),
        meta: { title: '服务器出错页' },
      },
      {
        path: 'browser-incompatible',
        name: 'ResultBrowserIncompatible',
        component: () => import('@/views/result/browser-incompatible/index.vue'),
        meta: { title: '浏览器不兼容页' },
      },
      {
        path: 'maintenance',
        name: 'ResultMaintenance',
        component: () => import('@/views/result/maintenance/index.vue'),
        meta: { title: '系统维护页' },
      },
    ],
  },
  {
    path: '/user',
    name: 'user',
    component: Layout,
    redirect: '/user/index',
    meta: { title: '个人中心', icon: 'user-circle', orderNo: 103 },
    children: [
      {
        path: 'index',
        name: 'UserIndex',
        component: () => import('@/views/user/index.vue'),
        meta: { title: '个人中心' },
      },
    ],
  },
];
