export function sortColumns(columns: Array<any>, newColumns: Array<any>) {
  if (!newColumns || !columns) return;
  const keyMap: Record<string, number> = {};
  newColumns.forEach((col, index) => {
    keyMap[col.key] = index;
  });
  columns.sort((a, b) => {
    const indexA = keyMap[a.key] !== undefined ? keyMap[a.key] : 999;
    const indexB = keyMap[b.key] !== undefined ? keyMap[b.key] : 999;
    return indexA - indexB;
  });
}

/**
 * 触发浏览器下载 Blob。
 * revoke 放在 setTimeout 中：同步 revoke 可能导致部分浏览器取消下载。
 */
export function downloadBlob(blob: Blob, filename: string) {
  const url = URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = filename;
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  setTimeout(() => URL.revokeObjectURL(url), 0);
}
