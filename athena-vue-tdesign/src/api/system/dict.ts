import { GET, POST, PUT, DELETE } from '@/utils/http';
import type { SysDict, SysDictItem, PageResult, Result } from '@/types/system';

const dictApi = {
  list: '/sys/dict/list',
  info: '/sys/dict/info',
  save: '/sys/dict/save',
  update: '/sys/dict/update',
  delete: '/sys/dict/delete',
};

const dictItemApi = {
  list: '/sys/dictItem/list',
  info: '/sys/dictItem/info',
  save: '/sys/dictItem/save',
  update: '/sys/dictItem/update',
  delete: '/sys/dictItem/delete',
};

export async function getDictList(params: {
  page: number;
  pageSize: number;
  dictName?: string;
  dictCode?: string;
}): Promise<Result<PageResult<SysDict>>> {
  return GET(dictApi.list, params);
}

export async function getDictInfo(id: string): Promise<Result<SysDict>> {
  return GET(`${dictApi.info}/${id}`);
}

export async function saveDict(data: SysDict): Promise<Result<void>> {
  return POST(dictApi.save, data);
}

export async function updateDict(data: SysDict): Promise<Result<void>> {
  return PUT(dictApi.update, data);
}

export async function deleteDict(id: string): Promise<Result<void>> {
  return DELETE(`${dictApi.delete}?id=${id}`);
}

export async function getDictItemList(params: { dictId: string }): Promise<Result<PageResult<SysDictItem>>> {
  return GET(dictItemApi.list, params);
}

export async function saveDictItem(data: SysDictItem): Promise<Result<void>> {
  return POST(dictItemApi.save, data);
}

export async function deleteDictItem(id: string): Promise<Result<void>> {
  return DELETE(`${dictItemApi.delete}?id=${id}`);
}