import { DELETE, GET, POST, PUT } from '@/utils/http';
import type { SysScheduleJob, TaskBeanOption, PageResult, Result } from '@/types/monitor';

const scheduleApi = {
  list: '/sys/schedule/list',
  info: '/sys/schedule/info',
  save: '/sys/schedule/save',
  update: '/sys/schedule/update',
  delete: '/sys/schedule/delete',
  deleteBatch: '/sys/schedule/deleteBatch',
  run: '/sys/schedule/run',
  pause: '/sys/schedule/pause',
  resume: '/sys/schedule/resume',
  taskBeanList: '/sys/schedule/taskBeanList',
};

export async function getScheduleJobList(params: {
  page: number;
  pageSize: number;
  beanName?: string;
  status?: number;
}): Promise<Result<PageResult<SysScheduleJob>>> {
  return GET(scheduleApi.list, params);
}

export async function getScheduleJobInfo(id: string): Promise<Result<SysScheduleJob>> {
  return GET(`${scheduleApi.info}/${id}`);
}

export async function getScheduleTaskBeanList(excludeId?: string): Promise<Result<TaskBeanOption[]>> {
  const params = excludeId ? { excludeId } : {};
  return GET(scheduleApi.taskBeanList, params);
}

export async function saveScheduleJob(data: SysScheduleJob): Promise<Result<void>> {
  return POST(scheduleApi.save, data);
}

export async function updateScheduleJob(data: SysScheduleJob): Promise<Result<void>> {
  return PUT(scheduleApi.update, data);
}

export async function deleteScheduleJob(id: string): Promise<Result<void>> {
  return DELETE(`${scheduleApi.delete}?id=${id}`);
}

export async function deleteScheduleJobBatch(ids: Array<string>): Promise<Result<void>> {
  return DELETE(`${scheduleApi.deleteBatch}?ids=${ids.join(',')}`);
}

export async function runScheduleJob(id: string): Promise<Result<void>> {
  return GET(`${scheduleApi.run}?id=${id}`);
}

export async function pauseScheduleJob(id: string): Promise<Result<void>> {
  return GET(`${scheduleApi.pause}?id=${id}`);
}

export async function resumeScheduleJob(id: string): Promise<Result<void>> {
  return GET(`${scheduleApi.resume}?id=${id}`);
}
