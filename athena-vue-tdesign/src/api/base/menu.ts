import type { BaseTree } from '@/types/interface';
import { GET } from '@/utils/http';

export interface NavMenuResponse {
  menuTree: BaseTree[];
  permissions: string[];
}

export function getNavMenu() {
  return GET<NavMenuResponse>('/sys/permission/nav');
}
