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