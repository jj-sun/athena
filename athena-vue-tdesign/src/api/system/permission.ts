import { GET, POST, PUT, DELETE } from '@/utils/http';
import type { SysPermission, Result } from '@/types/system';

const menuApi = {
  list: '/sys/permission/list',
  info: '/sys/permission/info',
  save: '/sys/permission/save',
  update: '/sys/permission/update',
  delete: '/sys/permission/delete',
};

export async function getMenuList(): Promise<Result<Array<SysPermission>>> {
  return GET(menuApi.list);
}

export async function getMenuInfo(id: string): Promise<Result<SysPermission>> {
  return GET(`${menuApi.info}/${id}`);
}

export async function saveMenu(data: SysPermission): Promise<Result<void>> {
  return POST(menuApi.save, data);
}

export async function updateMenu(data: SysPermission): Promise<Result<void>> {
  return PUT(menuApi.update, data);
}

export async function deleteMenu(id: string): Promise<Result<void>> {
  return DELETE(`${menuApi.delete}?id=${id}`);
}