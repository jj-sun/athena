# 前端生成规范

前端根：`athena-vue-tdesign/`，`@` 别名指向 `src`。

## 1. 文件

| 文件 | 内容 |
|---|---|
| `src/types/{{module}}/index.ts` | `{{Entity}}` 接口与查询参数类型 |
| `src/api/{{module}}/{{entityKebab}}.ts` | API 函数 |
| `src/views/{{module}}/constants.ts` | 固定状态/枚举选项（可选） |
| `src/views/{{module}}/{{entityKebab}}-list.vue` | 单列列表页 |

不修改 `src/router/modules/asyncRoutes.ts`。路由由后端 `sys_permission.component` 驱动，`component` 必须等于 `{{module}}/{{entityKebab}}-list`。

## 2. Types

```ts
export interface {{Entity}} {
  id: string;
  name: string;
  status?: number;
  status_dictText?: string;
  categoryId?: string;
  categoryId_dictText?: string;
  publishDate?: string;
  delFlag?: number;
  ctime?: string;
  mtime?: string;
}
```

规则：

- ID、外键、字典值用 `string`（除非规格明确是数字字典）。
- 所有 `@Dict` 字段补充可选的 `{{field}}_dictText`。
- 审计字段 `delFlag/ctime/mtime` 可选。
- 已存在同文件时只追加缺失接口，不覆盖。

## 3. API

```ts
import type { PageResult } from '@/types/base';
import type { {{Entity}} } from '@/types/{{module}}';
import { DELETE, GET, POST, PUT } from '@/utils/http';

const {{entity}}Api = {
  list: '/{{module}}/{{entity}}/list',
  info: '/{{module}}/{{entity}}/info',
  save: '/{{module}}/{{entity}}/save',
  update: '/{{module}}/{{entity}}/update',
  delete: '/{{module}}/{{entity}}/delete',
  deleteBatch: '/{{module}}/{{entity}}/deleteBatch',
  exportList: '/{{module}}/{{entity}}/export',
  importList: '/{{module}}/{{entity}}/import',
  importTemplate: '/{{module}}/{{entity}}/importTemplate',
};

export function get{{Entity}}List(params: Record<string, unknown>) {
  return GET<PageResult<{{Entity}}>>({{entity}}Api.list, params);
}

export function get{{Entity}}Info(id: string) {
  return GET<{{Entity}}>(`${apiBase}/info/${id}`);
}

export function save{{Entity}}(data: {{Entity}}) {
  return POST({{entity}}Api.save, data);
}

export function update{{Entity}}(data: {{Entity}}) {
  return PUT({{entity}}Api.update, data);
}

export function delete{{Entity}}(id: string) {
  return DELETE(`${apiBase}/delete?id=${id}`);
}

export function delete{{Entity}}Batch(ids: Array<string>) {
  return DELETE(`${apiBase}/deleteBatch?ids=${ids.join(',')}`);
}

export function export{{Entity}}List(params: Record<string, unknown>) {
  return GET<Blob>({{entity}}Api.exportList, params, { responseType: 'blob' });
}

export function import{{Entity}}(file: File) {
  const formData = new FormData();
  formData.append('file', file);
  return POST<number>({{entity}}Api.importList, formData);
}

export function download{{Entity}}ImportTemplate() {
  return GET<Blob>({{entity}}Api.importTemplate, undefined, { responseType: 'blob' });
}
```

注意：

- 所有路径都基于 `apiBase` 拼接；`apiBase` 的模板字面量只引用本地变量 `apiBase`，不包含 `{{...}}` 占位符，避免生成器替换时与 JS 模板字面量混淆。
- 导出/模板必须传 `{ responseType: 'blob' }`；`axios.config.ts` 会把 JSON 错误信封从 Blob 中还原为 `BusinessError`。
- 导入字段名固定 `file`。
- 导入返回 `number`（成功条数）。
- 遵循 `simple-import-sort`：外部依赖在前，空行，`@/` 导入按字母序，最后 `import type`。

## 4. Constants

仅固定状态/枚举/布尔值使用：

```ts
export const statusOptions = [
  { label: '启用', value: 1, theme: 'success' },
  { label: '停用', value: 0, theme: 'default' },
];
```

动态字典/外键不要硬编码；使用已有 `/select` 接口，或按规格生成模块自己的 `/select` 并在 `onMounted` 加载。没有权限时不要请求，避免进页面弹 403：

```ts
const { permissions } = getPermissionStore();
if (permissions.includes('{{permissionPrefix}}:list')) {
  loadOptions();
}
```

## 5. 页面结构

外层单列，使用项目通用类：

```vue
<template>
  <div class="main-container">
    <table-search @search="onSearch" @reset-search="resetSearch">
      <template #search-content>
        <t-col :span="3">
          <t-form-item label="名称" style="width: 100%">
            <t-input v-model:value="searchForm.name" placeholder="请输入名称" />
          </t-form-item>
        </t-col>
      </template>
    </table-search>

    <t-card>
      <table-header
        :selected-row-size="selectedRowKeys.length"
        :table-columns="tableColumns"
        @do-refresh="loadData"
        @clear-selected="onClearSelected"
        @do-table-setting="onUpdateTable"
      >
        <template #action>
          <t-button v-permission="'{{permissionPrefix}}:save'" theme="primary" @click="onAdd">
            <template #icon><plus-icon /></template>
            新增
          </t-button>
          <t-dropdown>
            <t-button v-permission="'{{permissionPrefix}}:deleteBatch'">
              批量操作
              <template #suffix><more-icon /></template>
            </t-button>
            <t-dropdown-menu>
              <t-dropdown-item value="1" @click="onBatchDelete">
                批量删除
                <template #prefixIcon><delete-icon /></template>
              </t-dropdown-item>
            </t-dropdown-menu>
          </t-dropdown>
          <t-button v-permission="'{{permissionPrefix}}:import'" variant="outline" @click="onOpenImport">
            <template #icon><upload-icon /></template>
            导入
          </t-button>
          <t-button v-permission="'{{permissionPrefix}}:export'" variant="outline" @click="onExport">
            <template #icon><download-icon /></template>
            导出
          </t-button>
        </template>
      </table-header>

      <t-table
        :selected-row-keys="selectedRowKeys"
        :loading="tableLoading"
        :data="dataList"
        :columns="tableColumns"
        :pagination="pagination"
        :column-controller="columnControllerConfig"
        table-layout="fixed"
        :row-key="rowKey"
        @select-change="onSelectChange"
      >
        <!-- 状态/布尔/字典插槽按字段生成 -->
        <template #actions="{ row }">
          <t-space>
            <t-button
              v-permission="['{{permissionPrefix}}:info', '{{permissionPrefix}}:update']"
              theme="primary"
              ghost
              size="small"
              @click="onEdit(row)"
            >
              编辑
            </t-button>
            <t-button
              v-permission="'{{permissionPrefix}}:delete'"
              theme="danger"
              ghost
              size="small"
              @click="onDelete(row)"
            >
              删除
            </t-button>
          </t-space>
        </template>
      </t-table>
    </t-card>

    <modal-dialog ref="modalDialogRef" :title="actionTitle" @cancel="onCancel" @confirm="onSubmit">
      <template #content>
        <t-form ref="formRef" :data="formData" :rules="rules" :label-col="{ span: 6 }">
          <!-- 每个 form: true 字段一个 t-form-item -->
        </t-form>
      </template>
    </modal-dialog>

    <t-dialog
      v-model:visible="importVisible"
      header="批量导入{{entityLabel}}"
      width="560px"
      :confirm-loading="importLoading"
      @confirm="onImportConfirm"
      @close="onImportClose"
    >
      <t-space direction="vertical" size="small" style="width: 100%">
        <t-upload
          v-model="importFiles"
          theme="custom"
          accept=".xlsx,.xls"
          :auto-upload="false"
          :size-limit="{ size: 10, unit: 'MB' }"
          placeholder="请选择 Excel 文件"
        >
          <t-button variant="outline">选择文件</t-button>
        </t-upload>
        <div v-if="importFiles.length > 0" style="font-size: 12px">已选择：{{ importFiles[0].name }}</div>
        <t-link theme="primary" @click="onDownloadTemplate">下载导入模板</t-link>
        <div style="color: var(--td-text-color-secondary); font-size: 12px; line-height: 20px">
          单次最多导入 5000 条。表头必须与模板一致；必填项不能为空。
        </div>
      </t-space>
    </t-dialog>
  </div>
</template>
```

**不要**出现：

- `t-row` / `t-col` 双栏布局
- 左侧部门树、`t-tree`、`dept-select`
- 角色选择、改密/重置密码、头像
- `t-switch` 切换状态并调用后端（项目没有现成的状态切换接口）

## 6. Script

```vue
<script lang="ts" setup>
import dayjs from 'dayjs';
import { DeleteIcon, DownloadIcon, MoreIcon, PlusIcon, UploadIcon } from 'tdesign-icons-vue-next';
import type { FormInstanceFunctions, FormProps, UploadFile } from 'tdesign-vue-next';
import { DialogPlugin, MessagePlugin } from 'tdesign-vue-next';
import { onMounted, reactive, ref, useTemplateRef } from 'vue';

import {
  delete{{Entity}},
  delete{{Entity}}Batch,
  download{{Entity}}ImportTemplate,
  export{{Entity}}List,
  get{{Entity}}Info,
  get{{Entity}}List,
  import{{Entity}},
  save{{Entity}},
  update{{Entity}},
} from '@/api/{{module}}/{{entityKebab}}';
import ModalDialog from '@/components/ModalDialog.vue';
import TableHeader from '@/components/table/TableHeader.vue';
import TableSearch from '@/components/table/TableSearch.vue';
import {
  useColumnControllerConfig,
  usePagination,
  useRowKey,
  useRowSelection,
  useTable,
  useTableColumn,
} from '@/hooks/table';
import type { TablePropsType } from '@/types/components';
import type { {{Entity}} } from '@/types/{{module}}';
import { downloadBlob, sortColumns } from '@/utils';
import { statusOptions } from './constants';

const { tableLoading, dataList, indexColumn, handleSuccess } = useTable();
const rowKey = useRowKey('id');
const pagination = usePagination(loadData);
const { selectedRowKeys, onSelectChange, onClearSelected } = useRowSelection();
const { columnControllerConfig } = useColumnControllerConfig({
  fields: ['status', 'ctime'],
});

const searchForm = reactive({
  name: '',
  status: undefined as number | undefined,
});
</script>
```

### 表单与规则

- `formData` 包含所有 `form: true` 字段；默认值：数字 `0` 或 `undefined`、字符串 `''`、状态取第一个选项、布尔 `1`。
- `rules` 只为 `required: true` 字段生成必填规则；有格式要求（邮箱/手机号/密码）时按规格添加。
- 编辑时先 `get{{Entity}}Info(row.id)`，再 `Object.assign(formData, ...)` 并打开弹窗。
- 提交：

```ts
async function onSubmit() {
  const valid = await formRef.value.validate();
  if (valid !== true) {
    return;
  }
  try {
    if (isAdd.value) {
      await save{{Entity}}(formData);
      void MessagePlugin.success('新增成功');
    } else {
      await update{{Entity}}(formData);
      void MessagePlugin.success('更新成功');
    }
    modalDialogRef.value?.close();
    loadData();
  } catch (error: any) {
    void MessagePlugin.error(error?.message || '操作失败');
  }
}
```

### 查询

```ts
function onSearch() {
  pagination.current = 1;
  loadData();
}

function resetSearch() {
  searchForm.name = '';
  searchForm.status = undefined;
  pagination.current = 1;
  loadData();
}

function loadData() {
  tableLoading.value = true;
  get{{Entity}}List({
    page: pagination.current,
    pageSize: pagination.pageSize,
    ...searchForm,
  })
    .then((res) => {
      handleSuccess(res.list);
      pagination.setTotalSize(res.total);
    })
    .catch((error: any) => {
      void MessagePlugin.error(error?.message || '加载列表失败');
    })
    .finally(() => {
      tableLoading.value = false;
    });
}
```

### 删除与批量删除

```ts
function onDelete(record: {{Entity}}) {
  const confirmDia = DialogPlugin.confirm({
    header: '提示',
    body: '确定要删除这条数据吗？',
    cancelBtn: '取消',
    confirmBtn: '删除',
    onConfirm: async () => {
      try {
        await delete{{Entity}}(record.id);
        void MessagePlugin.success('删除成功');
        if (dataList.length === 1 && pagination.current > 1) {
          pagination.current -= 1;
        }
        loadData();
      } catch (error: any) {
        void MessagePlugin.error(error?.message || '删除失败');
      } finally {
        confirmDia.destroy();
      }
    },
  });
}
```

批量删除：先检查 `selectedRowKeys.value.length`；成功后若 `ids.length >= dataList.length` 且当前页 > 1 则回退页码；重载并 `onClearSelected()`；`finally` 中 `destroy()`。

### 导出

```ts
async function onExport() {
  try {
    const blob = await export{{Entity}}List({ ...searchForm });
    downloadBlob(blob, `{{entityLabel}}_${dayjs().format('YYYYMMDD_HHmmss')}.xlsx`);
  } catch (error: any) {
    void MessagePlugin.error(error?.message || '导出失败');
  }
}
```

### 导入与模板

```ts
async function onImportConfirm() {
  const file = importFiles.value[0]?.raw;
  if (!(file instanceof File)) {
    void MessagePlugin.warning('请选择 Excel 文件');
    return;
  }
  importLoading.value = true;
  try {
    const count = await import{{Entity}}(file);
    void MessagePlugin.success(`成功导入 ${count} 条{{entityLabel}}`);
    importVisible.value = false;
    importFiles.value = [];
    loadData();
  } catch (error: any) {
    void MessagePlugin.error(error?.message || '导入失败');
  } finally {
    importLoading.value = false;
  }
}

async function onDownloadTemplate() {
  try {
    const blob = await download{{Entity}}ImportTemplate();
    downloadBlob(blob, '{{entityLabel}}导入模板.xlsx');
  } catch (error: any) {
    void MessagePlugin.error(error?.message || '下载模板失败');
  }
}
```

## 7. 列表列与插槽

- 第一列：`...indexColumn`（含勾选与序号）。
- 业务列：仅 `list: true` 的字段。
- 字典/外键列：`colKey: '{{field}}_dictText'`，并保留原字段作为回退：
  ```vue
  <template #categoryId_dictText="{ row }">
    {{ row.categoryId_dictText || row.categoryId || '-' }}
  </template>
  ```
- 状态/布尔列：用插槽渲染 `t-tag`，例如：

```vue
<template #status="{ row }">
  <t-tag v-if="row.status === 1" theme="success" size="small">启用</t-tag>
  <t-tag v-else theme="default" size="small">停用</t-tag>
</template>
```

- 操作列：`colKey: 'actions'`，`fixed: 'right'`，宽度 160–200。
- `useColumnControllerConfig({ fields: [...] })` 只列出可隐藏的列（通常是状态、时间等辅助列），不要把操作列放进去。
- `onUpdateTable(newColumns) { sortColumns(tableColumns, newColumns); }`。

## 8. 其他约定

- 标签、提示、确认文案硬编码简体中文；项目没有 vue-i18n。
- 不新增 `xlsx`/`exceljs`/`file-saver` 依赖。
- 不在页面里 `rsaEncrypt`（只有密码相关功能才需要）。
- `onMounted` 只做 `loadData()` 和必要的选项加载。
- 所有按钮/操作都要有 `v-permission`，权限码与后端、SQL 完全一致。
- 数组形式的 `v-permission` 表示「全部拥有」才显示；编辑按钮使用 `[':info', ':update']`。
