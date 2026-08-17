import Layout from '@/layouts/index.vue';

export default [
  /*  {
    path: '/dashboard',
    component: Layout,
    redirect: '/dashboard/base',
    name: 'dashboard',
    meta: {
      title: {
        zh_CN: '仪表盘',
        en_US: 'Dashboard',
      },
      icon: shallowRef(DashboardIcon),
      orderNo: 0,
    },
    children: [
      {
        path: 'base',
        name: 'DashboardBase',
        component: () => import('@/views/dashboard/base/index.vue'),
        meta: {
          title: {
            zh_CN: '概览仪表盘',
            en_US: 'Overview',
          },
        },
      },
      {
        path: 'detail',
        name: 'DashboardDetail',
        component: () => import('@/views/dashboard/detail/index.vue'),
        meta: {
          title: {
            zh_CN: '统计报表',
            en_US: 'Dashboard Detail',
          },
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
      title: {
        zh_CN: '表单页',
        en_US: 'Form',
      },
      icon: 'edit-1',
    },
    children: [
      {
        path: 'base',
        name: 'FormBase',
        component: () => import('@/views/form/base/index.vue'),
        meta: {
          title: {
            zh_CN: '基础表单页',
            en_US: 'Base Form',
          },
        },
      },
      {
        path: 'step',
        name: 'FormStep',
        component: () => import('@/views/form/step/index.vue'),
        meta: {
          title: {
            zh_CN: '分步表单页',
            en_US: 'Step Form',
          },
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
      title: {
        zh_CN: '外部页面',
        en_US: 'External',
      },
    },
    children: [
      {
        path: 'doc',
        name: 'Doc',
        component: 'IFrame',
        meta: {
          frameSrc: 'https://tdesign.tencent.com/starter/docs/vue-next/get-started',
          title: {
            zh_CN: '使用文档（内嵌）',
            en_US: 'Documentation(IFrame)',
          },
        },
      },
      {
        path: 'TDesign',
        name: 'TDesign',
        component: 'IFrame',
        meta: {
          frameSrc: 'https://tdesign.tencent.com/vue-next/getting-started',
          title: {
            zh_CN: 'TDesign 文档（内嵌）',
            en_US: 'TDesign (IFrame)',
          },
        },
      },
      {
        path: 'TDesign2',
        name: 'TDesign2',
        component: 'IFrame',
        meta: {
          frameSrc: 'https://tdesign.tencent.com/vue-next/getting-started',
          frameBlank: true,
          title: {
            zh_CN: 'TDesign 文档（外链',
            en_US: 'TDesign Doc(Link)',
          },
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
      title: {
        zh_CN: '结果页',
        en_US: 'Result',
      },
      icon: 'check-circle',
    },
    children: [
      {
        path: 'success',
        name: 'ResultSuccess',
        component: () => import('@/views/result/success/index.vue'),
        meta: {
          title: {
            zh_CN: '成功页',
            en_US: 'Success',
          },
        },
      },
      {
        path: 'fail',
        name: 'ResultFail',
        component: () => import('@/views/result/fail/index.vue'),
        meta: {
          title: {
            zh_CN: '失败页',
            en_US: 'Fail',
          },
        },
      },
      {
        path: 'network-error',
        name: 'ResultNetworkError',
        component: () => import('@/views/result/network-error/index.vue'),
        meta: {
          title: {
            zh_CN: '网络异常',
            en_US: 'Network Error',
          },
        },
      },
      {
        path: '403',
        name: 'Result403',
        component: () => import('@/views/result/403/index.vue'),
        meta: { title: { zh_CN: '无权限', en_US: 'Forbidden' } },
      },
      {
        path: '404',
        name: 'Result404',
        component: () => import('@/views/result/404/index.vue'),
        meta: { title: { zh_CN: '访问页面不存在页', en_US: 'Not Found' } },
      },
      {
        path: '500',
        name: 'Result500',
        component: () => import('@/views/result/500/index.vue'),
        meta: { title: { zh_CN: '服务器出错页', en_US: 'Server Error' } },
      },
      {
        path: 'browser-incompatible',
        name: 'ResultBrowserIncompatible',
        component: () => import('@/views/result/browser-incompatible/index.vue'),
        meta: { title: { zh_CN: '浏览器不兼容页', en_US: 'BrowserIncompatible' } },
      },
      {
        path: 'maintenance',
        name: 'ResultMaintenance',
        component: () => import('@/views/result/maintenance/index.vue'),
        meta: { title: { zh_CN: '系统维护页', en_US: 'Maintenance' } },
      },
    ],
  },
  {
    path: '/user',
    name: 'user',
    component: Layout,
    redirect: '/user/index',
    meta: { title: { zh_CN: '个人中心', en_US: 'User Center' }, icon: 'user-circle' },
    children: [
      {
        path: 'index',
        name: 'UserIndex',
        component: () => import('@/views/user/index.vue'),
        meta: { title: { zh_CN: '个人中心', en_US: 'User Center' } },
      },
    ],
  },
];
