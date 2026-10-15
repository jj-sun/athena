import type { AxiosRequestHeaders, InternalAxiosRequestConfig } from 'axios';
import Axios from 'axios';
import Cookies from 'js-cookie';
import NProgress from 'nprogress';
import qs from 'qs';
import { MessagePlugin } from 'tdesign-vue-next';

import { USER_TOKEN_KEY } from '@/constants';
import router from '@/router';
import { useUserStore } from '@/store';
import { randomNonce, signParams } from './sign';

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

const service = Axios.create({
  baseURL,
  timeout: 10 * 60 * 1000,
});

service.interceptors.request.use(
  async (config: InternalAxiosRequestConfig) => {
    config.headers ??= {} as AxiosRequestHeaders;
    const token = Cookies.get(USER_TOKEN_KEY);
    if (!config.headers.has(CONTENT_TYPE)) {
      config.headers.set(CONTENT_TYPE, APPLICATION_JSON);
    }
    if (config.headers[CONTENT_TYPE] === FORM_URLENCODED) {
      config.data = qs.stringify(config.data);
    }
    if (token) {
      config.headers[USER_TOKEN_KEY] = token;
    }

    // 防重放签名：附加 timestamp / nonce / sign query 参数，与后端 sa-token-sign 校验保持一致
    const secret = import.meta.env.VITE_REPLAY_SECRET;
    if (secret && typeof crypto !== 'undefined' && crypto.subtle) {
      const timestamp = String(Date.now());
      const nonce = randomNonce();
      const queryParams = (config.params ?? {}) as Record<string, unknown>;
      const sign = await signParams({ ...queryParams, timestamp, nonce }, secret);
      config.params = { ...queryParams, timestamp, nonce, sign };
    }

    return config;
  },
  (error) => {
    return Promise.reject(error.response);
  },
);

service.interceptors.response.use(
  (response) => {
    const { code, message, data } = response.data || {};
    // 鲁棒识别 Sa-Token NotLoginException（后端通用异常处理器返回 code=500 + 含 token/未登录 的 message）
    if (code === 500 && message && /token|未登录|登录已过期/i.test(message)) {
      redirectToLogin();
      return Promise.reject(new Error('Token已失效'));
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
  console.log('进入登录跳转页面', isRedirecting);
  console.log(router.currentRoute.value.path);
  if (isRedirecting) return;
  // 已在登录页则不再重复跳转，避免 redirect 参数嵌套
  if (router.currentRoute.value.path === '/login') return;
  isRedirecting = true;
  NProgress.start();

  void MessagePlugin.error('登录已过期，请重新登录');

  const userStore = useUserStore();
  userStore.logout().then(() => {
    console.log('跳转成功');
  });
  window.location.href = `/login?redirect=${encodeURIComponent(router.currentRoute.value.fullPath)}`;
};

export default service;
