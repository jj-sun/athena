export interface LoginInfo {
  token: string;
  userInfo: any;
}

export interface Result<T> {
  code: number;
  msg: string;
  result: T;
}

export interface PageResult<T> {
  list: Array<T>;
  total: number;
}

export interface BaseSelect {
  label: string;
  value: string;
}
