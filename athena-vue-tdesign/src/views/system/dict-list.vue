<template>
  <div class="main-container">
    <table-search @search="onSearch" @reset-search="resetSearch">
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
        @clear-selected="onClearSelected"
        @do-table-setting="onUpdateTable"
      >
        <template #action>
          <t-button v-permission="'sys:dict:save'" theme="primary" @click="onAdd">
            <template #icon><plus-icon /></template>
            新增
          </t-button>
          <t-dropdown>
            <t-button v-permission="'sys:dict:delete'">
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
            <t-button
              v-permission="['sys:dict:info', 'sys:dict:update']"
              theme="primary"
              ghost
              size="small"
              @click="onEdit(row)"
            >
              编辑
            </t-button>
            <t-button v-permission="'sys:dict:delete'" theme="danger" ghost size="small" @click="onDelete(row)">
              删除
            </t-button>
            <t-button
              v-permission="'sys:dictItem:list'"
              theme="primary"
              ghost
              size="small"
              @click="onShowDictItem(row)"
            >
              字典项
            </t-button>
          </t-space>
        </template>
      </t-table>
    </t-card>

    <modal-dialog ref="modalDialogRef" :title="actionTitle" @confirm="onSubmit">
      <template #content>
        <t-form ref="formRef" :data="formData" :rules="rules" :label-col="{ span: 6 }">
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

    <modal-drawer
      ref="dictItemModalRef"
      title="字典项管理"
      size="large"
      @cancel="onCancel"
      @close="onDictItemClose"
      @confirm="onDictItemConfirm"
    >
      <template #content>
        <div v-if="currentDictId">
          <div class="dict-item-toolbar">
            <t-input
              v-model:value="dictItemSearchForm.itemText"
              class="dict-item-search"
              placeholder="请输入标签"
              clearable
              @enter="onSearchDictItems"
            />
            <t-input
              v-model:value="dictItemSearchForm.itemValue"
              class="dict-item-search"
              placeholder="请输入值"
              clearable
              @enter="onSearchDictItems"
            />
            <t-button theme="primary" @click="onSearchDictItems">查询</t-button>
            <t-button @click="onResetDictItemSearch">重置</t-button>
            <t-button
              v-permission="'sys:dictItem:save'"
              class="dict-item-add"
              theme="primary"
              variant="outline"
              @click="openAddDictItem"
            >
              新增
            </t-button>
          </div>
          <t-table
            :data="dictItems"
            :columns="dictItemColumns"
            :pagination="dictItemPagination"
            :loading="dictItemLoading"
            :disable-data-page="true"
            row-key="id"
          >
            <template #actions="{ row }">
              <t-space>
                <t-button
                  v-permission="'sys:dictItem:update'"
                  theme="primary"
                  ghost
                  size="small"
                  @click="editDictItem(row)"
                >
                  编辑
                </t-button>
                <t-button
                  v-permission="'sys:dictItem:delete'"
                  theme="danger"
                  ghost
                  size="small"
                  @click="onDeleteDictItem(row)"
                >
                  删除
                </t-button>
              </t-space>
            </template>
          </t-table>
        </div>
      </template>
    </modal-drawer>

    <modal-dialog ref="dictItemEditDialogRef" :title="dictItemDialogTitle" @confirm="onDictItemEditSubmit">
      <template #content>
        <t-form ref="dictItemEditFormRef" :data="dictItemEditForm" :rules="dictItemEditRules" :label-col="{ span: 5 }">
          <t-form-item label="标签" name="itemText">
            <t-input v-model:value="dictItemEditForm.itemText" placeholder="请输入标签" />
          </t-form-item>
          <t-form-item label="值" name="itemValue">
            <t-input v-model:value="dictItemEditForm.itemValue" placeholder="请输入值" />
          </t-form-item>
          <t-form-item label="排序" name="sortOrder">
            <t-input-number v-model:value="dictItemEditForm.sortOrder" :min="0" :step="1" style="width: 100%" />
          </t-form-item>
        </t-form>
      </template>
    </modal-dialog>
  </div>
</template>
<script lang="ts" setup>
import { DeleteIcon, MoreIcon, PlusIcon } from 'tdesign-icons-vue-next';
import type { FormInstanceFunctions, FormProps } from 'tdesign-vue-next';
import { DialogPlugin, MessagePlugin } from 'tdesign-vue-next';
import { computed, onMounted, reactive, ref, useTemplateRef } from 'vue';

import {
  deleteDict,
  deleteDictItem,
  getDictInfo,
  getDictItemList,
  getDictList,
  saveDict,
  saveDictItem,
  updateDict,
  updateDictItem,
} from '@/api/system/dict';
import ModalDialog from '@/components/ModalDialog.vue';
import ModalDrawer from '@/components/ModalDrawer.vue';
import TableHeader from '@/components/table/TableHeader.vue';
import TableSearch from '@/components/table/TableSearch.vue';
import { usePagination, useRowKey, useRowSelection, useTable, useTableColumn } from '@/hooks/table';
import type { TablePropsType } from '@/types/components';
import type { SysDict, SysDictItem } from '@/types/system';
import { sortColumns } from '@/utils';

const { tableLoading, dataList, indexColumn, handleSuccess } = useTable();
const rowKey = useRowKey('id');
const pagination = usePagination(loadData);
const { selectedRowKeys, onSelectChange, onClearSelected } = useRowSelection();

const searchForm = reactive({
  dictName: '',
  dictCode: '',
});
const formRef = ref<FormInstanceFunctions>(null);
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
const rules: FormProps['rules'] = {
  dictName: [
    {
      required: true,
      message: '字典名必填',
      type: 'error',
    },
  ],
  dictCode: [
    {
      required: true,
      message: '字典编码必填',
      type: 'error',
    },
  ],
};

const currentDictId = ref('');
const dictItems = ref([] as Array<SysDictItem>);
const dictItemSearchForm = reactive({
  itemText: '',
  itemValue: '',
});
const dictItemLoading = ref(false);
const dictItemPagination = usePagination(() => loadDictItems());
// 未筛选时的字典项总数，新增时用于计算排序号
const dictItemTotalAll = ref(0);
// 丢弃过期响应，避免快速翻页/切换字典时旧数据覆盖新数据
let dictItemRequestSeq = 0;

const dictItemEditDialogRef = useTemplateRef('dictItemEditDialogRef');
const dictItemEditFormRef = ref<FormInstanceFunctions>(null);
const dictItemDialogMode = ref<'add' | 'edit'>('edit');
const dictItemDialogTitle = computed(() => (dictItemDialogMode.value === 'add' ? '新增字典项' : '编辑字典项'));
const dictItemEditForm = reactive({
  id: '',
  dictId: '',
  itemText: '',
  itemValue: '',
  sortOrder: 1,
});
const dictItemEditRules: FormProps['rules'] = {
  itemText: [
    {
      required: true,
      message: '标签必填',
      type: 'error',
    },
  ],
  itemValue: [
    {
      required: true,
      message: '值必填',
      type: 'error',
    },
  ],
};
const dictItemEditSubmitting = ref(false);

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
      colKey: 'ctime',
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
      title: '序号',
      colKey: 'serial-number',
      width: 60,
    },
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
      width: 60,
    },
    {
      title: '操作',
      colKey: 'actions',
    },
  ],
  { align: 'center' },
);

function onSearch() {
  pagination.current = 1;
  loadData();
}

function resetSearch() {
  searchForm.dictName = '';
  searchForm.dictCode = '';
  pagination.current = 1;
  loadData();
}

function loadData() {
  tableLoading.value = true;
  const params = {
    page: pagination.current,
    pageSize: pagination.pageSize,
    ...searchForm,
  };
  getDictList(params)
    .then((res) => {
      handleSuccess(res.list);
      pagination.setTotalSize(res.total);
    })
    .catch((err) => {
      console.error('加载字典列表失败', err);
      MessagePlugin.error('加载字典列表失败');
    })
    .finally(() => {
      tableLoading.value = false;
    });
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

function onEdit(record: SysDict) {
  isAdd.value = false;
  actionTitle.value = '编辑字典';
  getDictInfo(record.id)
    .then((dict) => {
      Object.assign(formData, {
        id: dict.id,
        dictName: dict.dictName,
        dictCode: dict.dictCode,
        type: dict.type || 2,
        description: dict.description || '',
      });
      modalDialogRef.value?.toggle();
    })
    .catch((err) => {
      console.error('获取字典信息失败', err);
      MessagePlugin.error('获取字典信息失败');
    });
}

async function onSubmit() {
  const validateResult = await formRef.value.validate();
  if (validateResult === true) {
    try {
      if (isAdd.value) {
        await saveDict(formData);
        void MessagePlugin.success('新增字典成功');
      } else {
        await updateDict(formData);
        void MessagePlugin.success('更新字典成功');
      }
      modalDialogRef.value?.close();
      loadData();
    } catch (err: any) {
      console.error('字典信息操作失败', err);
      void MessagePlugin.error('字典信息操作失败');
    }
  }
}
function onCancel() {
  formRef.value?.clearValidate();
  dictItemEditDialogRef.value?.close();
}

function onDelete(record: SysDict) {
  const confirmDia = DialogPlugin.confirm({
    header: '提示',
    body: '确定要删除此字典吗？',
    cancelBtn: '取消',
    confirmBtn: '删除',
    onConfirm: () => {
      deleteDict(record.id)
        .then(() => {
          MessagePlugin.success('字典删除成功');
          // 删除的是当前页唯一一条时，删完回退一页
          if (dataList.length === 1 && pagination.current > 1) {
            pagination.current -= 1;
          }
          loadData();
        })
        .catch((err) => {
          console.log('字典删除失败', err);
          MessagePlugin.error('字典删除失败');
        })
        .finally(() => {
          confirmDia.destroy();
        });
    },
  });
}

function onBatchDelete() {
  const ids = (selectedRowKeys.value || []).map(String);
  if (ids.length === 0) {
    MessagePlugin.warning('请选择要删除的字典');
    return;
  }
  const confirmDia = DialogPlugin.confirm({
    header: '提示',
    body: `确定要删除选中的 ${ids.length} 个字典吗？`,
    cancelBtn: '取消',
    confirmBtn: '删除',
    onConfirm: () => {
      Promise.all(ids.map((id) => deleteDict(id)))
        .then(() => {
          MessagePlugin.success('字典批量删除成功');
          // 删除后当前页可能为空，回退一页
          if (ids.length >= dataList.length && pagination.current > 1) {
            pagination.current -= 1;
          }
          loadData();
          onClearSelected();
        })
        .catch((err) => {
          console.log('字典批量删除失败', err);
          MessagePlugin.error('字典批量删除失败');
          loadData();
        })
        .finally(() => {
          confirmDia.destroy();
        });
    },
  });
}

/**
 * 只刷新字典项列表，不打开/关闭抽屉。
 * 新增、删除后调用，避免误把抽屉关掉。
 */
function loadDictItems() {
  if (!currentDictId.value) {
    return Promise.resolve();
  }
  const seq = ++dictItemRequestSeq;
  dictItemLoading.value = true;
  return getDictItemList({
    dictId: currentDictId.value,
    page: dictItemPagination.current,
    pageSize: dictItemPagination.pageSize,
    ...dictItemSearchForm,
  })
    .then((res) => {
      if (seq !== dictItemRequestSeq) {
        return;
      }
      dictItems.value = res.list;
      dictItemPagination.setTotalSize(res.total);
      // 无筛选条件时顺便记住全量总数，供新增时计算排序号
      if (!dictItemSearchForm.itemText && !dictItemSearchForm.itemValue) {
        dictItemTotalAll.value = res.total;
      }
    })
    .catch((err) => {
      if (seq !== dictItemRequestSeq) {
        return;
      }
      dictItems.value = [];
      console.error('加载字典项失败', err);
      MessagePlugin.error('加载字典项失败');
    })
    .finally(() => {
      if (seq === dictItemRequestSeq) {
        dictItemLoading.value = false;
      }
    });
}

function onShowDictItem(record: SysDict) {
  currentDictId.value = record.id;
  dictItemPagination.current = 1;
  dictItemTotalAll.value = 0;
  dictItemSearchForm.itemText = '';
  dictItemSearchForm.itemValue = '';
  dictItemEditDialogRef.value?.close();
  loadDictItems().finally(() => {
    dictItemModalRef.value?.show();
  });
}

function onSearchDictItems() {
  dictItemPagination.current = 1;
  loadDictItems();
}

function onResetDictItemSearch() {
  dictItemSearchForm.itemText = '';
  dictItemSearchForm.itemValue = '';
  dictItemPagination.current = 1;
  loadDictItems();
}

/** 新增与编辑复用同一个弹窗，这里只切换标题和初始值 */
function openAddDictItem() {
  dictItemDialogMode.value = 'add';
  Object.assign(dictItemEditForm, {
    id: '',
    dictId: currentDictId.value,
    itemText: '',
    itemValue: '',
    sortOrder: dictItemTotalAll.value + 1,
  });
  dictItemEditFormRef.value?.clearValidate();
  dictItemEditDialogRef.value?.show();
}

function editDictItem(record: SysDictItem) {
  if (!record.id) {
    MessagePlugin.error('字典项缺少 ID，无法编辑');
    return;
  }
  dictItemDialogMode.value = 'edit';
  Object.assign(dictItemEditForm, {
    id: record.id,
    dictId: record.dictId || currentDictId.value,
    itemText: record.itemText,
    itemValue: record.itemValue,
    sortOrder: record.sortOrder ?? 1,
  });
  dictItemEditFormRef.value?.clearValidate();
  dictItemEditDialogRef.value?.show();
}

async function onDictItemEditSubmit() {
  if (dictItemEditSubmitting.value) {
    return;
  }
  dictItemEditSubmitting.value = true;
  const adding = dictItemDialogMode.value === 'add';
  try {
    const validateResult = await dictItemEditFormRef.value?.validate();
    if (validateResult !== true) {
      return;
    }
    if (adding) {
      await saveDictItem({
        dictId: dictItemEditForm.dictId,
        itemText: dictItemEditForm.itemText.trim(),
        itemValue: dictItemEditForm.itemValue.trim(),
        // 清空排序号时回退到默认的末尾序号，避免新记录排到最前面
        sortOrder: dictItemEditForm.sortOrder ?? dictItemTotalAll.value + 1,
      });
      MessagePlugin.success('添加字典项成功');
      dictItemEditDialogRef.value?.close();
      // 清空查询条件并跳到新记录所在的最后一页（按 sort_order 升序）
      dictItemSearchForm.itemText = '';
      dictItemSearchForm.itemValue = '';
      dictItemPagination.current = Math.max(1, Math.ceil((dictItemTotalAll.value + 1) / dictItemPagination.pageSize));
      loadDictItems();
      return;
    }
    await updateDictItem({
      id: dictItemEditForm.id,
      dictId: dictItemEditForm.dictId,
      itemText: dictItemEditForm.itemText.trim(),
      itemValue: dictItemEditForm.itemValue.trim(),
      sortOrder: dictItemEditForm.sortOrder,
    });
    MessagePlugin.success('更新字典项成功');
    dictItemEditDialogRef.value?.close();
    // 保留当前页
    loadDictItems();
  } catch (err) {
    console.error(adding ? '添加字典项失败' : '更新字典项失败', err);
    MessagePlugin.error(adding ? '添加字典项失败' : '更新字典项失败');
  } finally {
    dictItemEditSubmitting.value = false;
  }
}

function onDeleteDictItem(record: SysDictItem) {
  const itemId = record.id;
  if (!itemId) {
    MessagePlugin.error('字典项缺少 ID，无法删除');
    return;
  }
  // 删除的是当前页唯一一条时，删完回退一页，避免停留在空页
  const isOnlyRowOnPage = dictItems.value.length === 1;
  const pageBeforeDelete = dictItemPagination.current;
  const confirmDia = DialogPlugin.confirm({
    header: '提示',
    body: '确定要删除此字典项吗？',
    cancelBtn: '取消',
    confirmBtn: '删除',
    onConfirm: () => {
      deleteDictItem(itemId)
        .then(() => {
          MessagePlugin.success('字典项删除成功');
          if (isOnlyRowOnPage && pageBeforeDelete > 1) {
            dictItemPagination.current = pageBeforeDelete - 1;
          }
          // 只刷新列表，保持抽屉打开
          loadDictItems();
        })
        .catch((err) => {
          console.log('字典项删除失败', err);
          MessagePlugin.error('字典项删除失败');
        })
        .finally(() => {
          confirmDia.destroy();
        });
    },
  });
}

function onDictItemClose() {
  // 点击抽屉右上角关闭时，同步关闭可能仍打开的编辑弹窗
  dictItemEditDialogRef.value?.close();
}

function onDictItemConfirm() {
  dictItemEditDialogRef.value?.close();
  dictItemModalRef.value?.close();
}

function onUpdateTable(newColumns: Array<TablePropsType>) {
  sortColumns(tableColumns, newColumns);
}

onMounted(loadData);
</script>
<style scoped>
.dict-item-toolbar {
  display: flex;
  gap: 10px;
  align-items: center;
  margin-bottom: 12px;
}

.dict-item-search {
  width: 180px;
}

.dict-item-add {
  margin-left: auto;
}
</style>
