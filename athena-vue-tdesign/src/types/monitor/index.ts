import type { PageResult, Result } from '@/types/base';

export interface SysScheduleJob {
  id: string;
  beanName: string;
  parameter?: string;
  cronExpression: string;
  status: number;
  remark?: string;
  delFlag?: number;
  ctime?: string;
}

export interface TaskBeanOption {
  beanName: string;
  className: string;
  disabled?: boolean;
}

export type { PageResult, Result };
