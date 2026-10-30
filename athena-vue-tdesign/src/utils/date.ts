// 获取常用时间
import dayjs from 'dayjs';

export const LAST_7_DAYS = [
  dayjs().subtract(7, 'day').format('YYYY-MM-DD'),
  dayjs().subtract(1, 'day').format('YYYY-MM-DD'),
];
