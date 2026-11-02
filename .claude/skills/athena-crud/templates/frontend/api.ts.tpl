import type { PageResult } from '@/types/base';
import type { {{Entity}} } from '@/types/{{module}}';
import { DELETE, GET, POST, PUT } from '@/utils/http';

const apiBase = '/{{module}}/{{entity}}';

const {{entity}}Api = {
  list: `${apiBase}/list`,
  info: `${apiBase}/info`,
  save: `${apiBase}/save`,
  update: `${apiBase}/update`,
  delete: `${apiBase}/delete`,
  deleteBatch: `${apiBase}/deleteBatch`,
  exportList: `${apiBase}/export`,
  importList: `${apiBase}/import`,
  importTemplate: `${apiBase}/importTemplate`,
};

export function get{{Entity}}List(params: Record<string, unknown>) {
  return GET<PageResult<{{Entity}}>>({{entity}}Api.list, params);
}

export function get{{Entity}}Info(id: string) {
  return GET<{{Entity}}>(`${apiBase}/info/${id}`);
}

export function save{{Entity}}(data: {{Entity}}) {
  return POST({{entity}}Api.save, data);
}

export function update{{Entity}}(data: {{Entity}}) {
  return PUT({{entity}}Api.update, data);
}

export function delete{{Entity}}(id: string) {
  return DELETE(`${apiBase}/delete?id=${id}`);
}

export function delete{{Entity}}Batch(ids: Array<string>) {
  return DELETE(`${apiBase}/deleteBatch?ids=${ids.join(',')}`);
}

/** 按当前查询条件导出全部匹配数据 */
export function export{{Entity}}List(params: Record<string, unknown>) {
  return GET<Blob>({{entity}}Api.exportList, params, { responseType: 'blob' });
}

/** 批量导入，返回成功导入数量 */
export function import{{Entity}}(file: File) {
  const formData = new FormData();
  formData.append('file', file);
  return POST<number>({{entity}}Api.importList, formData);
}

/** 下载导入模板（仅表头） */
export function download{{Entity}}ImportTemplate() {
  return GET<Blob>({{entity}}Api.importTemplate, undefined, { responseType: 'blob' });
}
