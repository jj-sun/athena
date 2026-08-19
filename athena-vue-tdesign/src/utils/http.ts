import type { AxiosRequestConfig } from 'axios';

import service from './axios.config';

// 封装泛型请求方法
function request<T = any>(config: AxiosRequestConfig): Promise<T> {
  return service.request(config) as Promise<T>;
}

export function GET<T = any>(url: string, params?: any): Promise<T> {
  return request<T>({
    method: 'GET',
    url,
    params,
  });
}

export function POST<T = any>(url: string, data?: any): Promise<T> {
  return request<T>({
    method: 'POST',
    url,
    data,
  });
}

// PUT 请求
export function PUT<T = any>(url: string, data?: any): Promise<T> {
  return request<T>({
    method: 'PUT',
    url,
    data,
  });
}

// DELETE 请求
export function DELETE<T = any>(url: string): Promise<T> {
  return request<T>({
    method: 'DELETE',
    url,
  });
}
