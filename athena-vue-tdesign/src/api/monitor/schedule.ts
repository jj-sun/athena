import type { SysScheduleJob } from '@/types/monitor';
import { DELETE, GET, POST, PUT } from '@/utils/http';

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

export function getScheduleJobList(params: { page: number; pageSize: number; beanName?: string; status?: number }) {
  return GET(scheduleApi.list, params);
}

export function getScheduleJobInfo(id: string) {
  return GET(`${scheduleApi.info}/${id}`);
}

export function getScheduleTaskBeanList(excludeId?: string) {
  const params = excludeId ? { excludeId } : {};
  return GET(scheduleApi.taskBeanList, params);
}

export function saveScheduleJob(data: SysScheduleJob) {
  return POST(scheduleApi.save, data);
}

export function updateScheduleJob(data: SysScheduleJob) {
  return PUT(scheduleApi.update, data);
}

export function deleteScheduleJob(id: string) {
  return DELETE(`${scheduleApi.delete}?id=${id}`);
}

export function deleteScheduleJobBatch(ids: Array<string>) {
  return DELETE(`${scheduleApi.deleteBatch}?ids=${ids.join(',')}`);
}

export function runScheduleJob(id: string) {
  return GET(`${scheduleApi.run}?id=${id}`);
}

export function pauseScheduleJob(id: string) {
  return GET(`${scheduleApi.pause}?id=${id}`);
}

export function resumeScheduleJob(id: string) {
  return GET(`${scheduleApi.resume}?id=${id}`);
}
