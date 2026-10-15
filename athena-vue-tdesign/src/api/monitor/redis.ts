import type { RedisMonitorSnapshot } from '@/types/monitor';
import { GET } from '@/utils/http';

const redisApi = {
  snapshot: '/sys/actuator/redis/snapshot',
};

export function getRedisMonitorSnapshot() {
  return GET<RedisMonitorSnapshot>(redisApi.snapshot);
}
