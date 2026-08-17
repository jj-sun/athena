import type { BaseSelect, PageResult, Result, SysRole, SysUser } from '@/types/system';
import { DELETE, GET, POST, PUT } from '@/utils/http';

const roleApi = {
  list: '/sys/role/list',
  select: '/sys/role/select',
  info: '/sys/role/info',
  save: '/sys/role/save',
  update: '/sys/role/update',
  delete: '/sys/role/delete',
  deleteBatch: '/sys/role/deleteBatch',

  getPermissionIdList: '/sys/rolePermission/getPermissionIdList',

  listUsers: '/sys/role/listUsers',
  addUser: '/sys/role/addUser',
  removeUser: '/sys/role/removeUser',
};

export async function getRoleList(params: {
  page: number;
  pageSize: number;
  roleName?: string;
  roleCode?: string;
}): Promise<Result<PageResult<SysRole>>> {
  return GET(roleApi.list, params);
}

export async function getRoleSelect(): Promise<Result<Array<BaseSelect>>> {
  return GET(roleApi.select);
}

export async function getRoleInfo(id: string): Promise<Result<SysRole>> {
  return GET(`${roleApi.info}/${id}`);
}

export async function saveRole(data: SysRole): Promise<Result<void>> {
  return POST(roleApi.save, data);
}

export async function updateRole(data: SysRole): Promise<Result<void>> {
  return PUT(roleApi.update, data);
}

export async function deleteRole(id: string): Promise<Result<void>> {
  return DELETE(`${roleApi.delete}?id=${id}`);
}

export async function deleteRoleBatch(ids: Array<string>): Promise<Result<void>> {
  return DELETE(`${roleApi.deleteBatch}?ids=${ids.join(',')}`);
}
export async function getPermissionIdList(roleId: string): Promise<Array<string>> {
  return GET(`${roleApi.getPermissionIdList}?roleId=${roleId}`);
}

export async function getRoleUserList(params: {
  roleId: string;
  page: number;
  pageSize: number;
}): Promise<Result<PageResult<SysUser>>> {
  return GET(roleApi.listUsers, params);
}

export async function addUserToRole(data: { roleId: string; userIdList: string[] }): Promise<Result<void>> {
  return POST(roleApi.addUser, data);
}

export async function removeUserFromRole(roleId: string, userId: string): Promise<Result<void>> {
  return DELETE(`${roleApi.removeUser}?roleId=${roleId}&userId=${userId}`);
}
