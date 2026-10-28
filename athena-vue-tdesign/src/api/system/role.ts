import type { SysRole } from '@/types/system';
import { DELETE, GET, POST, PUT } from '@/utils/http';

const roleApi = {
  list: '/sys/role/list',
  select: '/sys/role/select',
  info: '/sys/role/info',
  save: '/sys/role/save',
  update: '/sys/role/update',
  delete: '/sys/role/delete',
  deleteBatch: '/sys/role/deleteBatch',
  // 角色和菜单权限URL
  getPermissionIdList: '/sys/rolePermission/getPermissionIdList',
  saveRolePerms: '/sys/rolePermission/saveBatch',

  // 角色和用户URL
  listUsers: '/sys/role/listUsers',
  addUser: '/sys/role/addUser',
  removeUser: '/sys/role/removeUser',
};

export function getRoleList(params: { page: number; pageSize: number; roleName?: string; roleCode?: string }) {
  return GET(roleApi.list, params);
}

export function getRoleSelect() {
  return GET(roleApi.select);
}

export function getRoleInfo(id: string) {
  return GET(`${roleApi.info}/${id}`);
}

export function saveRole(data: SysRole) {
  return POST(roleApi.save, data);
}

export function updateRole(data: SysRole) {
  return PUT(roleApi.update, data);
}

export function deleteRole(id: string) {
  return DELETE(`${roleApi.delete}?id=${id}`);
}

export function deleteRoleBatch(ids: Array<string>) {
  return DELETE(`${roleApi.deleteBatch}?ids=${ids.join(',')}`);
}
export function getPermissionIdList(roleId: string) {
  return GET<string[]>(`${roleApi.getPermissionIdList}?roleId=${roleId}`);
}

export function getRoleUserList(params: { roleId: string; page: number; pageSize: number }) {
  return GET(roleApi.listUsers, params);
}

export function addUserToRole(data: { roleId: string; userIdList: string[] }) {
  return POST(roleApi.addUser, data);
}

export function removeUserFromRole(roleId: string, userId: string) {
  return DELETE(`${roleApi.removeUser}?roleId=${roleId}&userId=${userId}`);
}

export function saveRolePerms(data: { roleId: string; permissionIdList: string[] }) {
  return POST(roleApi.saveRolePerms, data);
}
