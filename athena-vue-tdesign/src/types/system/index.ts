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
  createTime?: string;
  roleIdList?: Array<string>;
}

export interface SysRole {
  id: string;
  roleName: string;
  roleCode: string;
  remark: string;
  delFlag?: number;
  createTime?: string;
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

export interface SysDict {
  id: string;
  dictName: string;
  dictCode: string;
  type: number;
  description: string;
  createTime?: string;
}

export interface SysDictItem {
  id?: string;
  dictId: string;
  label: string;
  value: string;
  sortOrder: number;
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
