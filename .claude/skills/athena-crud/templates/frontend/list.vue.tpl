<template>
  <div class="main-container">
    <table-search @search="onSearch" @reset-search="resetSearch">
      <template #search-content>
        <!-- <GENERATE: 每个 query != none 的字段一个 t-col> -->
        <t-col :span="3">
          <t-form-item label="名称" style="width: 100%">
            <t-input v-model:value="searchForm.name" placeholder="请输入名称" />
          </t-form-item>
        </t-col>
        <t-col :span="3">
          <t-form-item label="状态" style="width: 100%">
            <t-select v-model:value="searchForm.status" :options="statusOptions" placeholder="请选择状态" clearable />
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
                <template #prefixIcon>
                  <delete-icon />
                </template>
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
        <template #status="{ row }">
          <t-tag :theme="statusTheme(row.status)" size="small">{{ statusText(row.status) }}</t-tag>
        </template>
        <template #categoryId_dictText="{ row }">
          {{ row.categoryId_dictText || row.categoryId || '-' }}
        </template>
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
          <!-- <GENERATE: 每个 form: true 的字段一个 t-form-item> -->
          <t-form-item label="名称" name="name">
            <t-input v-model:value="formData.name" placeholder="请输入名称" />
          </t-form-item>
          <t-form-item label="状态" name="status">
            <t-select v-model:value="formData.status" :options="statusOptions" placeholder="请选择状态" />
          </t-form-item>
          <t-form-item label="分类" name="categoryId">
            <t-select
              v-model:value="formData.categoryId"
              :options="categoryOptions"
              placeholder="请选择分类"
              clearable
            />
          </t-form-item>
          <t-form-item label="业务日期" name="businessDate">
            <t-date-picker v-model:value="formData.businessDate" placeholder="请选择业务日期" clearable />
          </t-form-item>
          <t-form-item label="金额" name="amount">
            <t-input-number v-model:value="formData.amount" :min="0" :decimal-places="2" style="width: 100%" />
          </t-form-item>
          <t-form-item label="备注" name="remark">
            <t-textarea
              v-model:value="formData.remark"
              placeholder="请输入备注"
              :auto-size="{ minRows: 3, maxRows: 5 }"
            />
          </t-form-item>
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

import { statusOptions, statusText, statusTheme } from './constants';

const { tableLoading, dataList, indexColumn, handleSuccess } = useTable();
const rowKey = useRowKey('id');
const pagination = usePagination(loadData);
const { selectedRowKeys, onSelectChange, onClearSelected } = useRowSelection();
const { columnControllerConfig } = useColumnControllerConfig({
  fields: ['status', 'categoryId_dictText', 'businessDate', 'amount', 'ctime'],
});

const searchForm = reactive({
  name: '',
  status: undefined as number | undefined,
});

const formRef = ref<FormInstanceFunctions>(null);
const modalDialogRef = useTemplateRef('modalDialogRef');
const actionTitle = ref('新增{{entityLabel}}');
const isAdd = ref(true);

const importVisible = ref(false);
const importLoading = ref(false);
const importFiles = ref<UploadFile[]>([]);

// <GENERATE: 外键/字典选项；没有该字段时删除。可从目标模块 /select 接口加载>
const categoryOptions = ref<Array<{ label: string; value: string }>>([]);

const formData = reactive<{{Entity}}>({
  id: '',
  name: '',
  status: 1,
  categoryId: '',
  businessDate: '',
  amount: undefined,
  remark: '',
});

const rules: FormProps['rules'] = {
  name: [{ required: true, message: '名称必填', type: 'error' }],
  status: [{ required: true, message: '状态必选', type: 'error' }],
  // <GENERATE: 其他 required 字段>
};

const tableColumns = reactive(
  useTableColumn(
    [
      ...indexColumn,
      // <GENERATE: list: true 的字段>
      {
        title: '名称',
        colKey: 'name',
      },
      {
        title: '状态',
        colKey: 'status',
        width: 90,
      },
      {
        title: '分类',
        colKey: 'categoryId_dictText',
      },
      {
        title: '业务日期',
        colKey: 'businessDate',
        width: 120,
      },
      {
        title: '金额',
        colKey: 'amount',
        width: 120,
      },
      {
        title: '创建时间',
        colKey: 'ctime',
        width: 180,
      },
      {
        title: '操作',
        colKey: 'actions',
        fixed: 'right',
        width: 160,
      },
    ],
    {
      align: 'center',
    },
  ),
);

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
      console.error('加载{{entityLabel}}列表失败', error);
      void MessagePlugin.error(error?.message || '加载{{entityLabel}}列表失败');
    })
    .finally(() => {
      tableLoading.value = false;
    });
}

async function onExport() {
  try {
    const blob = await export{{Entity}}List({ ...searchForm });
    downloadBlob(blob, `{{entityLabel}}_${dayjs().format('YYYYMMDD_HHmmss')}.xlsx`);
  } catch (error: any) {
    void MessagePlugin.error(error?.message || '导出失败');
  }
}

function onOpenImport() {
  importFiles.value = [];
  importVisible.value = true;
}

function onImportClose() {
  importFiles.value = [];
}

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

function onAdd() {
  isAdd.value = true;
  actionTitle.value = '新增{{entityLabel}}';
  Object.assign(formData, {
    id: '',
    name: '',
    status: 1,
    categoryId: '',
    businessDate: '',
    amount: undefined,
    remark: '',
  });
  modalDialogRef.value?.toggle();
}

function onEdit(record: {{Entity}}) {
  isAdd.value = false;
  actionTitle.value = '编辑{{entityLabel}}';
  get{{Entity}}Info(record.id)
    .then((item) => {
      Object.assign(formData, item);
      modalDialogRef.value?.toggle();
    })
    .catch((error: any) => {
      console.error('获取{{entityLabel}}信息失败', error);
      void MessagePlugin.error(error?.message || '获取{{entityLabel}}信息失败');
    });
}

async function onSubmit() {
  const valid = await formRef.value?.validate();
  if (valid !== true) {
    return;
  }
  try {
    if (isAdd.value) {
      await save{{Entity}}(formData);
      void MessagePlugin.success('新增{{entityLabel}}成功');
    } else {
      await update{{Entity}}(formData);
      void MessagePlugin.success('更新{{entityLabel}}成功');
    }
    modalDialogRef.value?.close();
    loadData();
  } catch (error: any) {
    void MessagePlugin.error(error?.message || '操作失败');
  }
}

function onCancel() {
  formRef.value?.clearValidate();
}

function onDelete(record: {{Entity}}) {
  const confirmDia = DialogPlugin.confirm({
    header: '提示',
    body: '确定要删除这条{{entityLabel}}吗？',
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

function onBatchDelete() {
  if (selectedRowKeys.value.length === 0) {
    void MessagePlugin.warning('请选择要删除的{{entityLabel}}');
    return;
  }
  const confirmDia = DialogPlugin.confirm({
    header: '提示',
    body: `确定要删除选中的 ${selectedRowKeys.value.length} 条{{entityLabel}}吗？`,
    cancelBtn: '取消',
    confirmBtn: '删除',
    onConfirm: async () => {
      const ids = (selectedRowKeys.value || []) as string[];
      try {
        await delete{{Entity}}Batch(ids);
        void MessagePlugin.success('批量删除成功');
        if (ids.length >= dataList.length && pagination.current > 1) {
          pagination.current -= 1;
        }
        loadData();
        onClearSelected();
      } catch (error: any) {
        void MessagePlugin.error(error?.message || '批量删除失败');
      } finally {
        confirmDia.destroy();
      }
    },
  });
}

function onUpdateTable(newColumns: Array<TablePropsType>) {
  sortColumns(tableColumns, newColumns);
}

onMounted(() => {
  loadData();
  // <GENERATE: 有外键/字典下拉时在这里加载选项>
});
</script>
