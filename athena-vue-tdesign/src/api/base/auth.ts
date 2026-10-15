import { POST } from '@/utils/http';
import { rsaEncrypt } from '@/utils/crypto';

export function login(username: string, password: string) {
  const data = {
    username: rsaEncrypt(username),
    password: rsaEncrypt(password),
  };
  return POST('/login', data);
}

export function logout() {
  return POST('/logout');
}
