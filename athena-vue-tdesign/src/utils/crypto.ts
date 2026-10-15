import JSEncrypt from 'jsencrypt';

/**
 * RSA 公钥（与后端私钥配对，由 SaSecureUtil.rsaGenerateKeyPair() 生成）
 * 后端使用 SaSecureUtil.rsaDecryptByPrivate 解密，期望 hex 密文，故加密后将 Base64 转 hex。
 */
const PUBLIC_KEY = `-----BEGIN PUBLIC KEY-----
MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQC3mLVsj9xRIYXjkW3Z0aPswE++DHPb3wmcGh1sAWxyBcr7Apmdgtco2iDaiGslJMPJQxTnHC2c1e6rbyyMrUg94FmJwQM0wXJJAmH6AUKH7nLxvfHoutIV3T7BKEMJGDT9zsn91SWPaqI85PsQvWq3tHzn4AKBh4d9AUOIRwxH4wIDAQAB
-----END PUBLIC KEY-----`;

function base64ToHex(base64: string): string {
  const raw = atob(base64);
  let hex = '';
  for (let i = 0; i < raw.length; i += 1) {
    hex += raw.charCodeAt(i).toString(16).padStart(2, '0');
  }
  return hex;
}

/**
 * RSA 公钥加密，返回 hex 字符串（匹配后端 SaSecureUtil.rsaDecryptByPrivate）
 */
export function rsaEncrypt(plain: string): string {
  const encryptor = new JSEncrypt();
  encryptor.setPublicKey(PUBLIC_KEY);
  const cipher = encryptor.encrypt(plain);
  if (!cipher) {
    throw new Error('加密失败');
  }
  return base64ToHex(cipher);
}
