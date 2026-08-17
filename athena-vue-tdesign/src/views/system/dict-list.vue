<template>
  <div class="main-container">
    <table-search @search="loadData" @reset-search="resetSearch">
      <template #search-content>
        <t-col :span="3">
          <t-form-item label="字典名称" style="width: 100%">
            <t-input v-model:value="searchForm.dictName" placeholder="请输入字典名称" />
          </t-form-item>
        </t-col>
        <t-col :span="3">
          <t-form-item label="字典编号" style="width: 100%">
            <t-input v-model:value="searchForm.dictCode" placeholder="请输入字典编号" />
          </t-form-item>
        </t-col>
      </template>
    </table-search>
    <t-card>
      <table-header
        :selected-row-size="selectedRowKeys.length"
        :table-columns="tableColumns"
        @do-refresh="loadData"
        @clear-selected="onSelectChange([])"
        @do-table-setting="onUpdateTable"
      >
        <template #action>
          <t-button theme="primary" @click="onAdd">
            <template #icon><plus-icon /></template>
            新增
          </t-button>
          <t-dropdown>
            <t-button>
              批量操作
              <template #suffix><more-icon /></template>
            </t-button>
            <t-dropdown-menu>
              <t-dropdown-item value="1" @click="onBatchDelete">
                批量删除
                <template #icon>
                  <delete-icon />
                </template>
              </t-dropdown-item>
            </t-dropdown-menu>
          </t-dropdown>
        </template>
      </table-header>
      <t-table
        :selected-row-keys="selectedRowKeys"
        :loading="tableLoading"
        :data="dataList"
        :columns="tableColumns"
        :pagination="pagination"
        :row-key="rowKey"
        @select-change="onSelectChange"
      >
        <template #type="{ row }">
          <t-tag v-if="row.type === 1" theme="success" size="small">系统</t-tag>
          <t-tag v-else theme="warning" size="small">自定义</t-tag>
        </template>
        <template #actions="{ row }">
          <t-space>
            <t-button theme="primary" ghost size="small" @click="onEdit(row)">编辑</t-button>
            <t-button theme="danger" ghost size="small" @click="onDelete(row)">删除</t-button>
            <t-button theme="primary" ghost size="small" @click="onShowDictItem(row)">字典项</t-button>
          </t-space>
        </template>
      </t-table>
    </t-card>

    <modal-dialog ref="modalDialogRef" :title="actionTitle" @confirm="onSubmit">
      <template #content>
        <t-form :model="formData" :label-col="{ span: 6 }">
          <t-form-item label="字典名称" name="dictName">
            <t-input v-model:value="formData.dictName" placeholder="请输入字典名称" />
          </t-form-item>
          <t-form-item label="字典编号" name="dictCode">
            <t-input v-model:value="formData.dictCode" placeholder="请输入字典编号" />
          </t-form-item>
          <t-form-item label="字典类型" name="type">
            <t-radio-group v-model:value="formData.type">
              <t-radio :value="1">系统</t-radio>
              <t-radio :value="2">自定义</t-radio>
            </t-radio-group>
          </t-form-item>
          <t-form-item label="描述" name="description">
            <t-textarea
              v-model:value="formData.description"
              placeholder="请输入字典描述"
              :auto-size="{ minRows: 3, maxRows: 5 }"
            />
          </t-form-item>
        </t-form>
      </template>
    </modal-dialog>

    <modal-dialog ref="dictItemModalRef" title="字典项管理" @confirm="onDictItemConfirm">
      <template #content>
        <div v-if="currentDictId">
          <t-row :gutter="[10]">
            <t-col :span="8">
              <t-input v-model:value="newDictItem.label" placeholder="标签" />
            </t-col>
            <t-col :span="8">
              <t-input v-model:value="newDictItem.value" placeholder="值" />
            </t-col>
            <t-col :span="8">
              <t-button theme="primary" @click="addDictItem">添加</t-button>
            </t-col>
          </t-row>
          <t-table :data="dictItems" :columns="dictItemColumns" :pagination="false" :row-key="(row) => row.id">
            <template #actions="{ row }">
              <t-space>
                <t-button theme="primary" ghost size="small" @click="editDictItem(row)">编辑</t-button>
                <t-button theme="danger" ghost size="small" @click="deleteDictItem(row)">删除</t-button>
              </t-space>
            </template>
          </t-table>
        </div>
      </template>
    </modal-dialog>
  </div>
</template>
<script lang="ts" setup>
import { DeleteIcon, MoreIcon, PlusIcon } from 'tdesign-icons-vue-next';
import { DialogPlugin, MessagePlugin } from 'tdesign-vue-next';
import { onMounted, reactive, ref, useTemplateRef } from 'vue';

import {
  deleteDict,
  deleteDictItem,
  getDictInfo,
  getDictItemList,
  getDictList,
  saveDict,
  saveDictItem,
  updateDict,
} from '@/api/system/dict';
import ModalDialog from '@/components/ModalDialog.vue';
import TableHeader from '@/components/table/TableHeader.vue';
import TableSearch from '@/components/table/TableSearch.vue';
import { usePagination, useRowKey, useRowSelection, useTable, useTableColumn } from '@/hooks/table';
import type { TablePropsType } from '@/types/components';
import type { SysDict, SysDictItem } from '@/types/system';
import { sortColumns } from '@/utils';

const { tableLoading, dataList, indexColumn, handleSuccess } = useTable();
const rowKey = useRowKey('id');
const pagination = usePagination(loadData);
const { selectedRowKeys, onSelectChange } = useRowSelection();

const searchForm = reactive({
  dictName: '',
  dictCode: '',
});

const modalDialogRef = useTemplateRef('modalDialogRef');
const dictItemModalRef = useTemplateRef('dictItemModalRef');
const actionTitle = ref('添加字典');
const isAdd = ref(true);

const formData = reactive<SysDict>({
  id: '',
  dictName: '',
  dictCode: '',
  type: 2,
  description: '',
});

const currentDictId = ref('');
const dictItems = ref([] as Array<SysDictItem>);
const newDictItem = reactive({
  label: '',
  value: '',
});

const tableColumns = useTableColumn(
  [
    ...indexColumn,
    {
      title: '字典名称',
      colKey: 'dictName',
    },
    {
      title: '字典编号',
      colKey: 'dictCode',
    },
    {
      title: '字典类型',
      colKey: 'type',
    },
    {
      title: '描述',
      colKey: 'description',
    },
    {
      title: '创建时间',
      colKey: 'createTime',
    },
    {
      title: '操作',
      colKey: 'actions',
    },
  ],
  { align: 'center' },
);

const dictItemColumns = useTableColumn(
  [
    {
      title: '标签',
      colKey: 'itemText',
    },
    {
      title: '值',
      colKey: 'itemValue',
    },
    {
      title: '排序',
      colKey: 'sortOrder',
    },
    {
      title: '操作',
      colKey: 'actions',
    },
  ],
  { align: 'center' },
);

function resetSearch() {
  searchForm.dictName = '';
  searchForm.dictCode = '';
  loadData();
}

async function loadData() {
  tableLoading.value = true;
  try {
    const params = {
      page: pagination.current,
      pageSize: pagination.pageSize,
      ...searchForm,
    };
    const res = await getDictList(params);
    handleSuccess(res.list);
    pagination.setTotalSize(res.total);
  } catch (error) {
    console.error('加载字典列表失败', error);
  } finally {
    tableLoading.value = false;
  }
}

function onAdd() {
  isAdd.value = true;
  actionTitle.value = '添加字典';
  Object.assign(formData, {
    id: '',
    dictName: '',
    dictCode: '',
    type: 2,
    description: '',
  });
  modalDialogRef.value?.toggle();
}

async function onEdit(record: SysDict) {
  isAdd.value = false;
  actionTitle.value = '编辑字典';
  try {
    const dict = await getDictInfo(record.id);
    Object.assign(formData, {
      id: dict.id,
      dictName: dict.dictName,
      dictCode: dict.dictCode,
      type: dict.type || 2,
      description: dict.description || '',
    });
    modalDialogRef.value?.toggle();
  } catch (error) {
    MessagePlugin.error('获取字典信息失败');
  }
}

async function onSubmit() {
  if (!formData.dictName) {
    MessagePlugin.error('请输入字典名称');
    return;
  }
  if (!formData.dictCode) {
    MessagePlugin.error('请输入字典编号');
    return;
  }

  try {
    if (isAdd.value) {
      await saveDict(formData);
      MessagePlugin.success('新增字典成功');
    } else {
      await updateDict(formData);
      MessagePlugin.success('更新字典成功');
    }
    modalDialogRef.value?.close();
    loadData();
  } catch (error: any) {
    MessagePlugin.error(error?.response?.data?.msg || '操作失败');
  }
}

async function onDelete(record: SysDict) {
  const confirmDia = DialogPlugin.confirm({
    header: '提示',
    body: '确定要删除此字典吗？',
    cancelBtn: '取消',
    confirmBtn: '删除',
    onConfirm: async () => {
      try {
        await deleteDict(record.id);
        MessagePlugin.success('删除成功');
        loadData();
      } catch (error) {
        MessagePlugin.error('删除失败');
      } finally {
        confirmDia.destroy();
      }
    },
  });
}

async function onBatchDelete() {
  if (selectedRowKeys.value.length === 0) {
    MessagePlugin.warning('请选择要删除的字典');
    return;
  }
  const confirmDia = DialogPlugin.confirm({
    header: '提示',
    body: `确定要删除选中的 ${selectedRowKeys.value.length} 个字典吗？`,
    cancelBtn: '取消',
    confirmBtn: '删除',
    onConfirm: async () => {
      try {
        await deleteDict(selectedRowKeys.value[0]);
        MessagePlugin.success('批量删除成功');
        loadData();
        onSelectChange([]);
      } catch (error) {
        MessagePlugin.error('批量删除失败');
      } finally {
        confirmDia.destroy();
      }
    },
  });
}

async function onShowDictItem(record: SysDict) {
  currentDictId.value = record.id;
  try {
    const res = await getDictItemList({ dictId: record.id });
    dictItems.value = res.list;
  } catch (error) {
    dictItems.value = [];
    console.error('加载字典项失败', error);
  }
  newDictItem.label = '';
  newDictItem.value = '';
  dictItemModalRef.value?.toggle();
}

async function addDictItem() {
  if (!newDictItem.label) {
    MessagePlugin.error('请输入标签');
    return;
  }
  if (!newDictItem.value) {
    MessagePlugin.error('请输入值');
    return;
  }
  try {
    await saveDictItem({
      dictId: currentDictId.value,
      label: newDictItem.label,
      value: newDictItem.value,
      sortOrder: dictItems.value.length + 1,
    });
    MessagePlugin.success('添加字典项成功');
    newDictItem.label = '';
    newDictItem.value = '';
    onShowDictItem({ id: currentDictId.value } as SysDict);
  } catch (error) {
    MessagePlugin.error('添加失败');
  }
}

function editDictItem(record: SysDictItem) {
  MessagePlugin.info('请在弹窗中修改字典项');
}

async function deleteDictItem(record: SysDictItem) {
  const confirmDia = DialogPlugin.confirm({
    header: '提示',
    body: '确定要删除此字典项吗？',
    cancelBtn: '取消',
    confirmBtn: '删除',
    onConfirm: async () => {
      try {
        await deleteDictItem(record.id);
        MessagePlugin.success('删除成功');
        onShowDictItem({ id: currentDictId.value } as SysDict);
      } catch (error) {
        MessagePlugin.error('删除失败');
      } finally {
        confirmDia.destroy();
      }
    },
  });
}

function onDictItemConfirm() {
  dictItemModalRef.value?.close();
}

function onUpdateTable(newColumns: Array<TablePropsType>) {
  sortColumns(tableColumns, newColumns);
}

onMounted(loadData);
</script>
