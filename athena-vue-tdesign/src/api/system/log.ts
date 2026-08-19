import { GET } from '@/utils/http';

const logApi = {
  list: '/sys/log/list',
};

export function getLogList(params: { page: number; pageSize: number; username?: string; logType?: number }) {
  return GET(logApi.list, params);
}
