export interface {{Entity}} {
  id: string;

  // <GENERATE: 每个字段一行；ID/外键/字典值用 string，业务数字用 number>
  name: string;
  remark?: string;
  status?: number;
  status_dictText?: string;
  type?: number;
  type_dictText?: string;
  categoryId?: string;
  categoryId_dictText?: string;
  businessDate?: string;
  happenedAt?: string;
  amount?: number;

  delFlag?: number;
  ctime?: string;
  mtime?: string;
}

export interface {{Entity}}Query {
  page?: number;
  pageSize?: number;

  // <GENERATE: 每个 query != none 的字段>
  name?: string;
  status?: number;
  categoryId?: string;
  businessDateStart?: string;
  businessDateEnd?: string;
}
