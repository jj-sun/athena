import { GET, POST, PUT, DELETE } from '@/utils/http';
import type { SysDept, Result } from '@/types/system';

const deptApi = {
  list: '/sys/dept/list',
  info: '/sys/dept/info',
  save: '/sys/dept/save',
  update: '/sys/dept/update',
  delete: '/sys/dept/delete',
  deleteBatch: '/sys/dept/deleteBatch',
};

export async function getDeptList(): Promise<Result<Array<SysDept>>> {
  return GET(deptApi.list);
}

export async function getDeptInfo(id: string): Promise<Result<SysDept>> {
  return GET(`${deptApi.info}/${id}`);
}

export async function saveDept(data: SysDept): Promise<Result<void>> {
  return POST(deptApi.save, data);
}

export async function updateDept(data: SysDept): Promise<Result<void>> {
  return PUT(deptApi.update, data);
}

export async function deleteDept(id: string): Promise<Result<void>> {
  return DELETE(`${deptApi.delete}?id=${id}`);
}

export async function deleteDeptBatch(ids: Array<string>): Promise<Result<void>> {
  return DELETE(`${deptApi.deleteBatch}?ids=${ids.join(',')}`);
}