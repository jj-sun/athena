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

export interface SysScheduleJobLog {
  id: string;
  jobId: string;
  beanName: string;
  parameter?: string;
  /** 0：成功，1：失败 */
  status: number;
  error?: string;
  /** 耗时（毫秒） */
  times?: number;
  ctime?: string;
}

export interface TaskBeanOption {
  beanName: string;
  className: string;
  disabled?: boolean;
}

export interface SystemMetricsOverview {
  timestamp: number;
  systemCpuUsage: number | null;
  processCpuUsage: number | null;
  heapMemoryUsed: number | null;
  heapMemoryMax: number | null;
  diskFree: number | null;
  diskTotal: number | null;
  liveThreads: number | null;
  peakThreads: number | null;
  uptime: number | null;
  requestCount: number | null;
}

export interface MetricNamesResponse {
  names: string[];
}

export interface MetricMeasurement {
  statistic: string;
  value: number;
}

export interface MetricAvailableTag {
  tag: string;
  values: string[];
}

export interface MetricDetail {
  name: string;
  description: string | null;
  baseUnit: string | null;
  measurements: MetricMeasurement[];
  availableTags: MetricAvailableTag[];
}

export interface RedisInfoItem {
  key: string;
  value: string;
  description: string | null;
}

export interface RedisMonitorSnapshot {
  timestamp: number;
  redisVersion: string | null;
  redisMode: string | null;
  role: string | null;
  uptimeSeconds: number | null;
  keyCount: number | null;
  usedMemory: number | null;
  peakMemory: number | null;
  maxMemory: number | null;
  connectedClients: number | null;
  blockedClients: number | null;
  operationsPerSecond: number | null;
  totalCommandsProcessed: number | null;
  keyspaceHits: number | null;
  keyspaceMisses: number | null;
  keyspaceHitRate: number | null;
  rejectedConnections: number | null;
  details: RedisInfoItem[];
}

export type { PageResult, Result };
