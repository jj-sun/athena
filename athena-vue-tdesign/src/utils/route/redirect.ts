/**
 * 解析登录页的 redirect 参数。
 *
 * 兼容历史链接：旧版 Header 先 encodeURIComponent，vue-router 序列化 query 时又编码一次，
 * 浏览器解码后 query 里仍残留 "%2Fsystem%2Fuser" 这样的值，这里再解一次。
 * 同时只允许站内绝对路径，避免开放重定向。
 */
export function resolveRedirectPath(raw: unknown): string {
  let target = typeof raw === 'string' ? raw : '';

  // 合法路径一定以 "/" 开头，只有仍处于编码状态的值才需要再解一次
  if (target && !target.startsWith('/')) {
    try {
      target = decodeURIComponent(target);
    } catch {
      // 非法百分号序列，走兜底
      target = '';
    }
  }

  // 必须以单个 "/" 开头：排除 //evil.com、/\evil.com、http(s)://…、javascript:… 等
  if (!target.startsWith('/') || target.startsWith('//') || target.startsWith('/\\')) {
    return '/';
  }

  // 登录页本身不能作为回跳目标
  if (target === '/login' || target.startsWith('/login?') || target.startsWith('/login#')) {
    return '/';
  }

  return target;
}
