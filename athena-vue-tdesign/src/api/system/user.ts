import type { SysUser } from '@/types/system';
import { rsaEncrypt } from '@/utils/crypto';
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

export function getUserList(params: {
  page: number;
  pageSize: number;
  username?: string;
  realname?: string;
  email?: string;
  deptCode?: string;
}) {
  return GET(userApi.list, params);
}

export function getUserInfo(id: string) {
  return GET(`${userApi.info}/${id}`);
}

export function saveUser(data: SysUser) {
  return POST(userApi.save, data);
}

export function updateUser(data: SysUser) {
  return PUT(userApi.update, data);
}

export function deleteUser(id: string) {
  return DELETE(`${userApi.delete}?id=${id}`);
}

export function deleteUserBatch(ids: Array<string>) {
  return DELETE(`${userApi.deleteBatch}?ids=${ids.join(',')}`);
}

export function resetUserPassword(id: string, password: string) {
  return POST(userApi.resetPassword, { id, password: rsaEncrypt(password) });
}

export function changePassword(oldPassword: string, newPassword: string) {
  return POST('/sys/user/password', {
    password: rsaEncrypt(oldPassword),
    newPassword: rsaEncrypt(newPassword),
  });
}
