<template>
  <div class="main-container">
    <t-card>
      <table-header :table-columns="tableColumns" :show-message="false" @do-refresh="loadData">
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
                <template #prefixIcon>
                  <delete-icon />
                </template>
              </t-dropdown-item>
            </t-dropdown-menu>
          </t-dropdown>
        </template>
      </table-header>
      <t-enhanced-table
        :selected-row-keys="selectedRowKeys"
        :loading="tableLoading"
        :data="treeData"
        :columns="tableColumns"
        :disable-data-page="true"
        :row-key="rowKey"
        :tree="treeConfig"
        @select-change="onSelectChange"
      >
        <template #delFlag="{ row }">
          <t-tag v-if="row.delFlag === 0" theme="success" size="small">正常</t-tag>
          <t-tag v-else theme="danger" size="small">禁用</t-tag>
        </template>
        <template #actions="{ row }">
          <t-space>
            <t-button theme="primary" ghost size="small" @click="onEdit(row)">编辑</t-button>
            <t-button theme="danger" ghost size="small" @click="onDelete(row)">删除</t-button>
          </t-space>
        </template>
      </t-enhanced-table>
    </t-card>

    <modal-dialog ref="modalDialogRef" :title="dialogTitle" @cancel="onCancel" @confirm="onSubmit">
      <template #content>
        <t-form ref="formRef" :data="departmentModel" :rules="rules" :label-col="{ span: 6 }">
          <t-form-item label="上级部门" name="parentId">
            <dept-select v-model:value="departmentModel.parentId" :disabled="departmentModel.parentId === '0'" />
          </t-form-item>
          <t-form-item label="部门名称" name="deptName">
            <t-input v-model:value="departmentModel.deptName" placeholder="请输入部门名称" />
          </t-form-item>
          <t-form-item label="部门编号" name="deptCode">
            <t-input v-model:value="departmentModel.deptCode" placeholder="请输入部门编号" />
          </t-form-item>
          <t-form-item label="排序" name="sortOrder">
            <t-input-number v-model:value="departmentModel.sortOrder" />
          </t-form-item>
          <t-form-item label="描述" name="description">
            <t-textarea
              v-model:value="departmentModel.description"
              placeholder="请输入部门描述"
              :auto-size="{ minRows: 3, maxRows: 5 }"
            />
          </t-form-item>
        </t-form>
      </template>
    </modal-dialog>
  </div>
</template>
<script lang="ts" setup>
import { DeleteIcon, MoreIcon, PlusIcon } from 'tdesign-icons-vue-next';
import type { EnhancedTableProps, FormInstanceFunctions, FormProps } from 'tdesign-vue-next';
import { DialogPlugin, MessagePlugin } from 'tdesign-vue-next';
import { onMounted, reactive, ref, useTemplateRef } from 'vue';

import { deleteDept, deleteDeptBatch, getDeptInfo, getDeptList, saveDept, updateDept } from '@/api/system/dept';
import DeptSelect from '@/components/dept/index.vue';
import ModalDialog from '@/components/ModalDialog.vue';
import TableHeader from '@/components/table/TableHeader.vue';
import { useRowKey, useRowSelection, useTable, useTableColumn } from '@/hooks/table';
import type { SysDept } from '@/types/system';

const { tableLoading, indexColumn, handleSuccess } = useTable();
const rowKey = useRowKey('id');
const { selectedRowKeys, onSelectChange, onClearSelected } = useRowSelection();

const treeData = ref([] as Array<SysDept>);
const deptOptions = ref([] as Array<SysDept>);

const modalDialogRef = useTemplateRef('modalDialogRef');
const dialogTitle = ref('');
const isAdd = ref(true);
const formRef = ref<FormInstanceFunctions>(null);

const departmentModel = reactive<SysDept>({
  id: '',
  parentId: undefined,
  deptName: '',
  deptCode: '',
  sortOrder: 1,
  description: '',
});
const rules: FormProps['rules'] = {
  parentId: [
    {
      required: true,
      message: '上级必须',
      type: 'error',
    },
  ],
  deptName: [
    {
      required: true,
      message: '部门名称必填',
      type: 'error',
    },
  ],
  deptCode: [
    {
      required: true,
      message: '部门编码必填',
      type: 'error',
    },
  ],
};

const tableColumns = useTableColumn([
  ...indexColumn,
  {
    title: '部门名称',
    colKey: 'deptName',
  },
  {
    title: '部门编号',
    colKey: 'deptCode',
    width: '20%',
  },
  {
    title: '排序',
    colKey: 'sortOrder',
    width: '10%',
  },
  {
    title: '描述',
    colKey: 'description',
  },
  {
    title: '状态',
    colKey: 'delFlag',
    width: '10%',
  },
  {
    title: '操作',
    colKey: 'actions',
    width: '15%',
  },
]);
const treeConfig: EnhancedTableProps['tree'] = reactive({
  childrenKey: 'children',
  treeNodeColumnIndex: 2,
  indent: 25,
  expandTreeNodeOnClick: true,
});
function loadData() {
  tableLoading.value = true;
  getDeptList()
    .then((res) => {
      treeData.value = res;
      deptOptions.value = res;
      handleSuccess(res);
    })
    .catch((err) => {
      console.error('加载部门列表失败', err);
      MessagePlugin.error('加载部门列表失败');
    })
    .finally(() => {
      tableLoading.value = false;
    });
}

function hasChildren(item: SysDept): boolean {
  return item.children && item.children.length > 0;
}

function onAdd() {
  isAdd.value = true;
  dialogTitle.value = '添加部门';
  Object.assign(departmentModel, {
    id: '',
    parentId: undefined,
    deptName: '',
    deptCode: '',
    sortOrder: 1,
    description: '',
  });
  modalDialogRef.value?.toggle();
}

function onEdit(item: SysDept) {
  isAdd.value = false;
  dialogTitle.value = '编辑部门';
  getDeptInfo(item.id)
    .then((dept) => {
      Object.assign(departmentModel, {
        id: dept.id,
        parentId: dept.parentId,
        deptName: dept.deptName,
        deptCode: dept.deptCode,
        sortOrder: dept.sortOrder || 1,
        description: dept.description || '',
      });
      modalDialogRef.value?.toggle();
    })
    .catch((err) => {
      console.log('获取部门信息失败', err);
      MessagePlugin.error('获取部门信息失败');
    });
}

async function onSubmit() {
  const validateResult = await formRef.value.validate();
  if (validateResult === true) {
    try {
      if (isAdd.value) {
        await saveDept(departmentModel);
        await MessagePlugin.success('新增部门成功');
      } else {
        await updateDept(departmentModel);
        await MessagePlugin.success('更新部门成功');
      }
      modalDialogRef.value?.close();
      loadData();
    } catch (err) {
      console.log('部门操作失败', err);
      await MessagePlugin.error('部门操作失败');
    }
  }
}
function onCancel() {
  formRef.value.clearValidate();
}
function onDelete(item: SysDept) {
  if (hasChildren(item)) {
    MessagePlugin.warning('当前部门下有子部门，不能删除');
    return;
  }
  const confirmDia = DialogPlugin.confirm({
    header: '提示',
    body: '确定要删除此部门吗？',
    cancelBtn: '取消',
    confirmBtn: '删除',
    onConfirm: () => {
      deleteDept(item.id)
        .then(() => {
          MessagePlugin.success('部门删除成功');
          loadData();
        })
        .catch((err) => {
          console.log('部门删除失败', err);
          MessagePlugin.error('部门删除失败');
        })
        .finally(() => {
          confirmDia.destroy();
        });
    },
  });
}

function onBatchDelete() {
  if (selectedRowKeys.value.length === 0) {
    MessagePlugin.warning('请选择要删除的部门');
    return;
  }
  const confirmDia = DialogPlugin.confirm({
    header: '提示',
    body: `确定要删除选中的 ${selectedRowKeys.value.length} 个部门吗？`,
    cancelBtn: '取消',
    confirmBtn: '删除',
    onConfirm: () => {
      deleteDeptBatch(selectedRowKeys.value as string[])
        .then(() => {
          MessagePlugin.success('部门批量删除成功');
          loadData();
          onClearSelected();
        })
        .catch((err) => {
          console.log('部门批量删除失败', err);
          MessagePlugin.error('部门批量删除失败');
        })
        .finally(() => {
          confirmDia.destroy();
        });
    },
  });
}

onMounted(loadData);
</script>
