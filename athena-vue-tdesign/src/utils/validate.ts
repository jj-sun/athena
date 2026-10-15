/**
 * 密码复杂度校验：大写 + 小写 + 数字 + 特殊字符，且不少于 8 位
 */
export const PASSWORD_REGEX = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9]).{8,}$/;

/**
 * TDesign FormRule 自定义校验器（返回 { result, message, type }）
 */
export function passwordValidator(val: string) {
  if (!val) return { result: false, message: '请输入密码', type: 'error' as const };
  if (val.length < 8) return { result: false, message: '密码长度不能少于8位', type: 'error' as const };
  if (!/[a-z]/.test(val)) return { result: false, message: '密码需包含小写字母', type: 'error' as const };
  if (!/[A-Z]/.test(val)) return { result: false, message: '密码需包含大写字母', type: 'error' as const };
  if (!/\d/.test(val)) return { result: false, message: '密码需包含数字', type: 'error' as const };
  if (!/[^A-Za-z0-9]/.test(val)) return { result: false, message: '密码需包含特殊字符', type: 'error' as const };
  return true;
}
