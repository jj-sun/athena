import type { AxiosRequestConfig } from 'axios';

import type { Result } from '@/types/axios';

import service from './axios.config';

// 封装泛型请求方法
function request<T = any>(config: AxiosRequestConfig): Promise<T> {
  return service.request(config).then((res: any) => res as T);
}

export async function GET<T = any>(url: string, params?: any): Promise<T> {
  const response = await request<Result<T>>({
    method: 'GET',
    url,
    params,
  });
  return response as T;
}

export async function POST<T = any>(url: string, data?: any): Promise<T> {
  const response = await request<Result<T>>({
    method: 'POST',
    url,
    data,
  });
  return response as T;
}

// PUT 请求
export async function PUT<T = any>(url: string, data?: any): Promise<T> {
  const response = await request<Result<T>>({
    method: 'PUT',
    url,
    data,
  });
  return response as T;
}

// DELETE 请求
export async function DELETE<T = any>(url: string): Promise<T> {
  const response = await request<Result<T>>({
    method: 'DELETE',
    url,
  });
  return response as T;
}
