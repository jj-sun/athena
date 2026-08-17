import { GET } from '@/utils/http';
import type { SysLog, PageResult, Result } from '@/types/system';

const logApi = {
  list: '/sys/log/list',
};

export async function getLogList(params: {
  page: number;
  pageSize: number;
  username?: string;
  logType?: number;
}): Promise<Result<PageResult<SysLog>>> {
  return GET(logApi.list, params);
}