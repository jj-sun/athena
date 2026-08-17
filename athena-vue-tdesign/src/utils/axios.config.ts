import type { AxiosInstance, AxiosRequestHeaders, AxiosResponse, InternalAxiosRequestConfig } from 'axios';
import Axios from 'axios';
import Cookies from 'js-cookie';
import NProgress from 'nprogress';
import qs from 'qs';
import { MessagePlugin } from 'tdesign-vue-next';

import { USER_TOKEN_KEY } from '@/constants';
import router from '@/router';
import { useUserStore } from '@/store';
import type { Result } from '@/types/axios';

const baseURL: string = import.meta.env.VITE_API_URL || '/athena';
export const CONTENT_TYPE = 'Content-Type';

export const FORM_URLENCODED = 'application/x-www-form-urlencoded; charset=UTF-8';

export const APPLICATION_JSON = 'application/json; charset=UTF-8';

export const TEXT_PLAIN = 'text/plain; charset=UTF-8';
// 标记是否正在跳转登录，防止重复跳转
let isRedirecting = false;

// 定义业务错误类型
export class BusinessError extends Error {
  code: number;
  data: any;

  constructor(code: number, message: string, data?: any) {
    super(message);
    this.name = '业务逻辑错误';
    this.code = code;
    this.data = data;
  }
}

const service: AxiosInstance = Axios.create({
  baseURL,
  timeout: 10 * 60 * 1000,
});

service.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    config.headers ??= {} as AxiosRequestHeaders;
    const token = Cookies.get(USER_TOKEN_KEY);
    if (!config.headers[CONTENT_TYPE]) {
      config.headers[CONTENT_TYPE] = APPLICATION_JSON;
    }
    if (config.headers[CONTENT_TYPE] === FORM_URLENCODED) {
      config.data = qs.stringify(config.data);
    }
    if (token) {
      config.headers[USER_TOKEN_KEY] = token;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error.response);
  },
);

service.interceptors.response.use(
  (response: AxiosResponse<Result<any>>) => {
    const { code, message, data } = response.data || {};
    // 鲁棒识别 Sa-Token NotLoginException（后端通用异常处理器返回 code=500 + 含 token/未登录 的 message）
    if (code === 500 && message && /token|未登录|登录已过期/i.test(message)) {
      redirectToLogin();
      return Promise.reject(new Error('Token expired'));
    }
    if (code === 200) {
      return data;
    }
    return Promise.reject(new BusinessError(code, message, data));
  },
  (error) => {
    const status = error?.response?.status;
    if (status === 401 || status === 403) {
      redirectToLogin();
      return Promise.reject(new Error('Unauthorized'));
    }
    if (import.meta.env.MODE === 'development') {
      console.log(error);
    }
    error.name = 'ApiError';
    error.code = status || 500;
    error.message = error?.response?.data?.error || '服务器异常，请稍后重试…';
    return Promise.reject(error);
  },
);

// 处理登录跳转
const redirectToLogin = () => {
  if (isRedirecting) return;
  isRedirecting = true;
  NProgress.start();

  MessagePlugin.error('登录已过期，请重新登录');

  const userStore = useUserStore();
  userStore.logout();
  // 重定向到登录页，保留当前路径以便登录后跳回
  router.replace({
    path: '/login',
    query: { redirect: router.currentRoute.value.fullPath },
  });
  isRedirecting = false;
  NProgress.done();
};

export default service;
