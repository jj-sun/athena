/**
 * sa-token-sign 签名工具，与后端 SaSignTemplate.createSign 严格一致。
 *
 * 算法：参数按 key 字典序排序 → 拼接成 "k=v&k=v"（跳过空值、跳过 sign）→ 末尾追加 "&key=<secret>" → SHA-256 小写 hex。
 */

function isEmpty(value: unknown): boolean {
  return value === null || value === undefined || value === '';
}

/**
 * 解析 URL 中的查询参数。
 *
 * 后端验签使用 request.getParamMap()，其中包含 URL 上的查询参数（同名参数取第一个值）。
 * 因此签名不能只看 axios 的 config.params，否则所有把参数拼在 URL 里的请求都会验签失败。
 */
export function parseUrlParams(url: string): Record<string, string> {
  const index = url.indexOf('?');
  if (index < 0) return {};
  const search = new URLSearchParams(url.slice(index + 1));
  const params: Record<string, string> = {};
  search.forEach((value, key) => {
    // 与服务端一致：同名参数只取第一个
    if (!(key in params)) params[key] = value;
  });
  return params;
}

/** 生成 32 位随机 nonce（16 字节 → 32 位小写 hex） */
export function randomNonce(): string {
  const bytes = new Uint8Array(16);
  crypto.getRandomValues(bytes);
  return Array.from(bytes, (b) => b.toString(16).padStart(2, '0')).join('');
}

async function sha256Hex(input: string): Promise<string> {
  const data = new TextEncoder().encode(input);
  const digest = await crypto.subtle.digest('SHA-256', data);
  return Array.from(new Uint8Array(digest), (b) => b.toString(16).padStart(2, '0')).join('');
}

/**
 * 计算签名。
 * @param params 参与签名的参数（不含 sign 字段，值需为标量 string/number/boolean）
 */
export async function signParams(params: Record<string, unknown>, secret: string): Promise<string> {
  const keys = Object.keys(params)
    .filter((key) => key !== 'sign' && !isEmpty(params[key]))
    .sort();
  const joined = keys.map((key) => `${key}=${params[key]}`).join('&');
  return sha256Hex(`${joined}&key=${secret}`);
}
