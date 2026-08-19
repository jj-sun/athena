import type { SysDict, SysDictItem } from '@/types/system';
import { DELETE, GET, POST, PUT } from '@/utils/http';

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

export function getDictList(params: { page: number; pageSize: number; dictName?: string; dictCode?: string }) {
  return GET(dictApi.list, params);
}

export function getDictInfo(id: string) {
  return GET(`${dictApi.info}/${id}`);
}

export function saveDict(data: SysDict) {
  return POST(dictApi.save, data);
}

export function updateDict(data: SysDict) {
  return PUT(dictApi.update, data);
}

export function deleteDict(id: string) {
  return DELETE(`${dictApi.delete}?id=${id}`);
}

export function getDictItemList(params: { dictId: string }) {
  return GET(dictItemApi.list, params);
}

export function saveDictItem(data: SysDictItem) {
  return POST(dictItemApi.save, data);
}

export function deleteDictItem(id: string) {
  return DELETE(`${dictItemApi.delete}?id=${id}`);
}
