import type { SysDept } from '@/types/system';
import { DELETE, GET, POST, PUT } from '@/utils/http';

const deptApi = {
  list: '/sys/dept/list',
  select: '/sys/dept/select',
  info: '/sys/dept/info',
  save: '/sys/dept/save',
  update: '/sys/dept/update',
  delete: '/sys/dept/delete',
  deleteBatch: '/sys/dept/deleteBatch',
};

export function getDeptList() {
  return GET(deptApi.list);
}

export function deleteBatch() {
  return GET(deptApi.select);
}

export function getDeptInfo(id: string) {
  return GET(`${deptApi.info}/${id}`);
}

export function saveDept(data: SysDept) {
  return POST(deptApi.save, data);
}

export function updateDept(data: SysDept) {
  return PUT(deptApi.update, data);
}

export function deleteDept(id: string) {
  return DELETE(`${deptApi.delete}?id=${id}`);
}

export function deleteDeptBatch(ids: Array<string>) {
  return DELETE(`${deptApi.deleteBatch}?ids=${ids.join(',')}`);
}
