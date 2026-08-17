import type { Result, SysPermission } from '@/types/system/index.ts';
import { GET } from '@/utils/http';

export async function getNavMenu(): Promise<Result<Array<SysPermission>>> {
  return GET('/sys/permission/nav');
}
