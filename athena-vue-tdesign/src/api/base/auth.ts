import { GET, POST } from '@/utils/http';
import { rsaEncrypt } from '@/utils/crypto';

export function login(username: string, password: string, captcha: string, checkKey: string) {
  const data = {
    username: rsaEncrypt(username),
    password: rsaEncrypt(password),
    // 注意：验证码与 checkKey 必须明文提交。后端直接拿它与 Redis 中存的值比对，
    // 不会做 RSA 解密，若在这里加密会导致登录始终失败。
    captcha,
    checkKey,
  };
  return POST('/login', data);
}

/**
 * 获取图形验证码。
 *
 * @param checkKey 本次验证码的随机标识，登录时需原样回传
 * @returns base64 图片数据，不含 `data:image/png;base64,` 前缀，由调用方自行拼接
 */
export function getCaptcha(checkKey: string) {
  return GET<string>('/captcha', { checkKey });
}

export function logout() {
  return POST('/logout');
}
