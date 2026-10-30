import type { AxiosRequestHeaders, AxiosResponse, InternalAxiosRequestConfig } from 'axios';
import Axios from 'axios';
import Cookies from 'js-cookie';
import NProgress from 'nprogress';
import qs from 'qs';
import { MessagePlugin } from 'tdesign-vue-next';

import { USER_TOKEN_KEY } from '@/constants';
import router from '@/router';
import { useUserStore } from '@/store';

import { parseUrlParams, randomNonce, signParams } from './sign';

const baseURL: string = import.meta.env.VITE_API_URL || '/athena';
const CONTENT_TYPE = 'Content-Type';

const FORM_URLENCODED = 'application/x-www-form-urlencoded; charset=UTF-8';

const APPLICATION_JSON = 'application/json; charset=UTF-8';

// 标记是否正在跳转登录，防止重复跳转
let isRedirecting = false;

// 定义业务错误类型
class BusinessError extends Error {
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
    // FormData 由浏览器自动生成 multipart boundary，不能强行写成 application/json
    const isFormData = typeof FormData !== 'undefined' && config.data instanceof FormData;
    if (!config.headers.has(CONTENT_TYPE) && !isFormData) {
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
      // 后端验签包含 URL 上的查询参数（如 `?roleId=…`），这些参数不在 config.params 里，
      // 必须一并纳入签名，否则会返回「无效签名」。
      // 真实请求中 URL 自带参数排在 config.params 之前，服务端同名取第一个值，因此以 URL 为准。
      const urlParams = config.url ? parseUrlParams(config.url) : {};
      const sign = await signParams({ ...queryParams, ...urlParams, timestamp, nonce }, secret);
      config.params = { ...queryParams, timestamp, nonce, sign };
    }

    return config;
  },
  (error) => {
    return Promise.reject(error.response);
  },
);

service.interceptors.response.use(
  async (response) => {
    if (response.config.responseType === 'blob') {
      return handleBlobResponse(response);
    }
    return handleEnvelope(response.data);
  },
  async (error) => {
    const status = error?.response?.status;
    let backendMessage = error?.response?.data?.message;
    if (isBlob(error?.response?.data)) {
      // 下载接口出错时，后端仍返回 JSON，但 responseType=blob 会把它包成 Blob
      backendMessage = (await readBlobMessage(error.response.data)) ?? backendMessage;
    }

    // 401：登录态无效，需要重新登录
    if (status === 401) {
      redirectToLogin();
      return Promise.reject(new Error('Unauthorized'));
    }

    // 403：已登录但权限不足。这里不能跳登录页 —— 那样会把用户正常会话直接销毁，
    // 表现为"点了没有权限的按钮就被踢下线"。只提示并让调用方自行处理。
    if (status === 403) {
      const message = backendMessage || '无操作权限';
      void MessagePlugin.error(message);
      error.name = 'ApiError';
      error.code = status;
      error.message = message;
      return Promise.reject(error);
    }

    if (import.meta.env.MODE === 'development') {
      console.log(error);
    }
    error.name = 'ApiError';
    error.code = status || 500;
    error.message = backendMessage || '服务器异常，请稍后重试…';
    return Promise.reject(error);
  },
);

/** 统一处理后端 Result 信封（{ code, message, data }）。 */
function handleEnvelope(payload: any): any {
  const { code, message, data } = payload || {};
  // 鲁棒识别 Sa-Token NotLoginException（后端通用异常处理器返回 code=500 + 含 token/未登录 的 message）
  if (code === 500 && message && /token|未登录|登录已过期/i.test(message)) {
    redirectToLogin();
    throw new Error('Token已失效');
  }
  if (code === 200) {
    return data;
  }
  throw new BusinessError(code, message, data);
}

function isBlob(value: unknown): value is Blob {
  return typeof Blob !== 'undefined' && value instanceof Blob;
}

/**
 * responseType=blob 时，正常导出返回 Excel 二进制；业务错误/鉴权失败仍返回 JSON，
 * 只是被 axios 包成了 Blob。这里把 JSON Blob 还原成统一的业务异常，避免前端
 * 把错误信息当成 .xlsx 文件下载。
 */
async function handleBlobResponse(response: AxiosResponse): Promise<any> {
  const blob = response.data as Blob;
  // 正常下载是 xlsx 二进制，没必要把它整体读成文本
  if (blob.type && !/json|text/i.test(blob.type)) {
    return blob;
  }
  const payload = await parseJsonBlob(blob);
  if (payload) {
    return handleEnvelope(payload);
  }
  return blob;
}

async function parseJsonBlob(blob: Blob): Promise<any | null> {
  try {
    const text = await blob.text();
    if (!text.trim().startsWith('{')) return null;
    return JSON.parse(text);
  } catch {
    return null;
  }
}

async function readBlobMessage(blob: Blob): Promise<string | undefined> {
  const payload = await parseJsonBlob(blob);
  return payload?.message;
}

// 处理登录跳转
const redirectToLogin = () => {
  if (isRedirecting) return;
  // 已在登录页则不再重复跳转，避免 redirect 参数嵌套
  if (router.currentRoute.value.path === '/login') return;
  isRedirecting = true;
  NProgress.start();

  void MessagePlugin.error('登录已过期，请重新登录');

  const userStore = useUserStore();
  void userStore.logout();
  window.location.href = `/login?redirect=${encodeURIComponent(router.currentRoute.value.fullPath)}`;
};

export default service;
