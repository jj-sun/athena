import { POST } from '@/utils/http';

export function login(username: string, password: string) {
  const data = {
    username,
    password,
  };
  return POST('/login', data);
}

export function logout() {
  return POST('/logout');
}
