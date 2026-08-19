import type { SysPermission } from '@/types/system';
import { DELETE, GET, POST, PUT } from '@/utils/http';

const menuApi = {
  list: '/sys/permission/list',
  info: '/sys/permission/info',
  save: '/sys/permission/save',
  update: '/sys/permission/update',
  delete: '/sys/permission/delete',
};

export function getMenuList() {
  return GET(menuApi.list);
}

export function getMenuInfo(id: string) {
  return GET(`${menuApi.info}/${id}`);
}

export function saveMenu(data: SysPermission) {
  return POST(menuApi.save, data);
}

export function updateMenu(data: SysPermission) {
  return PUT(menuApi.update, data);
}

export function deleteMenu(id: string) {
  return DELETE(`${menuApi.delete}?id=${id}`);
}
