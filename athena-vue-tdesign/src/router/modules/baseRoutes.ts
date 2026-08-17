import Layout from '@/layouts/index.vue';

export default [
  {
    path: '/login',
    name: 'login',
    component: () => import('@/views/login/index.vue'),
  },
  {
    path: '/404',
    name: '404',
    hidden: true,
    component: () => import('@/views/result/404/index.vue'),
  },
  {
    path: '/500',
    name: '500',
    hidden: true,
    component: () => import('@/views/result/500/index.vue'),
  },
  {
    path: '/403',
    name: '403',
    hidden: true,
    component: () => import('@/views/result/403/index.vue'),
  },
  {
    path: '/',
    component: Layout,
    name: 'root',
    redirect: '/dashboard/analysis',
  },
];
