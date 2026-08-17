import type { LoginInfo, Result } from '@/types/base/auth.ts';
import { POST } from '@/utils/http';

export async function login(username: string, password: string): Promise<Result<LoginInfo>> {
  const data = {
    username,
    password,
  };
  return POST('/login', data);
}

export async function logout(): Promise<Result<void>> {
  return POST('/logout');
}
