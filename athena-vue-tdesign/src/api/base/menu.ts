import { GET } from '@/utils/http';

export function getNavMenu() {
  return GET('/sys/permission/nav');
}
