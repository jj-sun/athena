// 仅固定枚举/状态/布尔值使用；动态字典请使用 /select 接口。
// <GENERATE: 没有固定选项字段时不要生成此文件>

export const statusOptions = [
  { label: '启用', value: 1, theme: 'success' as const },
  { label: '停用', value: 0, theme: 'default' as const },
];

export function statusText(value?: number) {
  return statusOptions.find((item) => item.value === value)?.label ?? '';
}

export function statusTheme(value?: number): 'success' | 'default' {
  return value === 1 ? 'success' : 'default';
}
