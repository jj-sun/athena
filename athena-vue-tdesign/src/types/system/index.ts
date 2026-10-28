import type { BaseSelect, PageResult, Result } from '@/types/base';

export interface SysUser {
  id: string;
  username: string;
  realname: string;
  password?: string;
  email: string;
  mobile: string;
  sex: number;
  deptCode: string;
  deptCode_dictText?: string;
  userType?: number;
  delFlag?: number;
  ctime?: string;
  roleIdList?: Array<string>;
}

export interface SysRole {
  id: string;
  roleName: string;
  roleCode: string;
  remark: string;
  delFlag?: number;
  ctime?: string;
}

export interface SysDept {
  id: string;
  parentId: string | undefined;
  deptName: string;
  deptCode: string;
  description: string;
  sortOrder: number;
  delFlag?: number;
  children?: Array<SysDept>;
}

/** 部门选择接口（/sys/dept/select）返回的树节点结构 */
export interface SysDeptTree {
  info: SysDept;
  key: string;
  parentKey: string;
  title: string;
  disabled: boolean;
  children?: Array<SysDeptTree>;
}

export interface SysDict {
  id: string;
  dictName: string;
  dictCode: string;
  type: number;
  description: string;
  /** 创建时间，后端字段名为 ctime */
  ctime?: string;
}

export interface SysDictItem {
  id?: string;
  dictId: string;
  /** 字典项标签，对应后端 item_text */
  itemText: string;
  /** 字典项值，对应后端 item_value */
  itemValue: string;
  sortOrder?: number;
  description?: string;
}

export interface SysPermission {
  id: string;
  parentId: string;
  name: string;
  url: string;
  component: string;
  frameSrc: string;
  frameBlank: number;
  redirect: string;
  perms: string;
  type: number;
  icon: string;
  orderNum: number;
  delFlag?: number;
  children?: Array<SysPermission>;
}

export interface SysLog {
  id: string;
  logType: number;
  username: string;
  operation: string;
  method: string;
  params: string;
  time: number;
  ip: string;
  createDate: string;
}

export type { BaseSelect, PageResult, Result };
