import type { PageResult, Result, SysUser } from '@/types/system';
import { DELETE, GET, POST, PUT } from '@/utils/http';

const userApi = {
  list: '/sys/user/list',
  info: '/sys/user/info',
  save: '/sys/user/save',
  update: '/sys/user/update',
  delete: '/sys/user/delete',
  deleteBatch: '/sys/user/deleteBatch',
  resetPassword: '/sys/user/resetPassword',
};

export async function getUserList(params: {
  page: number;
  pageSize: number;
  username?: string;
  realname?: string;
  email?: string;
  deptCode?: string;
}): Promise<Result<PageResult<SysUser>>> {
  return GET(userApi.list, params);
}

export async function getUserInfo(id: string): Promise<Result<SysUser>> {
  return GET(`${userApi.info}/${id}`);
}

export async function saveUser(data: SysUser): Promise<Result<void>> {
  return POST(userApi.save, data);
}

export async function updateUser(data: SysUser): Promise<Result<void>> {
  return PUT(userApi.update, data);
}

export async function deleteUser(id: string): Promise<Result<void>> {
  return DELETE(`${userApi.delete}?id=${id}`);
}

export async function deleteUserBatch(ids: Array<string>): Promise<Result<void>> {
  return DELETE(`${userApi.deleteBatch}?ids=${ids.join(',')}`);
}

export async function resetUserPassword(id: string, password: string): Promise<Result<void>> {
  return POST(userApi.resetPassword, { id, password });
}
