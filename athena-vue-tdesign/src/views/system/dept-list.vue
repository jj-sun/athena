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
                <template #icon>
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
        :pagination="false"
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

    <modal-dialog ref="modalDialog" :title="dialogTitle" @confirm="onSubmit">
      <template #content>
        <t-form :model="departmentModel" :label-col="{ span: 6 }">
          <t-form-item label="上级部门" name="parentId">
            <t-select v-model:value="departmentModel.parentId" placeholder="请选择上级部门">
              <t-select-option value="0">无</t-select-option>
              <t-select-option v-for="item of deptOptions" :key="item.id" :value="item.id">
                {{ item.deptName }}
              </t-select-option>
            </t-select>
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
import type { EnhancedTableProps } from 'tdesign-vue-next';
import { DialogPlugin, MessagePlugin } from 'tdesign-vue-next';
import { onMounted, reactive, ref, useTemplateRef } from 'vue';

import { deleteDept, deleteDeptBatch, getDeptInfo, getDeptList, saveDept, updateDept } from '@/api/system/dept';
import ModalDialog from '@/components/ModalDialog.vue';
import TableHeader from '@/components/table/TableHeader.vue';
import { useRowKey, useRowSelection, useTable, useTableColumn } from '@/hooks/table';
import type { SysDept } from '@/types/system';

const { tableLoading, dataList, indexColumn, handleSuccess } = useTable();
const rowKey = useRowKey('id');
const { selectedRowKeys, onSelectChange } = useRowSelection();

const treeData = ref([] as Array<SysDept>);
const deptOptions = ref([] as Array<SysDept>);

const modalDialog = useTemplateRef('modalDialog');
const dialogTitle = ref('');
const isAdd = ref(true);

const departmentModel = reactive<SysDept>({
  id: '',
  parentId: undefined,
  deptName: '',
  deptCode: '',
  sortOrder: 1,
  description: '',
});

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
async function loadData() {
  tableLoading.value = true;
  try {
    const res = await getDeptList();
    treeData.value = res;
    deptOptions.value = flattenDept(res);
    handleSuccess(res);
  } catch (error) {
    console.error('加载部门列表失败', error);
  } finally {
    tableLoading.value = false;
  }
}

function flattenDept(depts: Array<SysDept>): Array<SysDept> {
  const result: Array<SysDept> = [];
  depts.forEach((dept) => {
    result.push(dept);
    if (dept.children) {
      result.push(...flattenDept(dept.children));
    }
  });
  return result;
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
  modalDialog.value?.toggle();
}

async function onEdit(item: SysDept) {
  isAdd.value = false;
  dialogTitle.value = '编辑部门';
  try {
    const dept = await getDeptInfo(item.id);
    Object.assign(departmentModel, {
      id: dept.id,
      parentId: dept.parentId,
      deptName: dept.deptName,
      deptCode: dept.deptCode,
      sortOrder: dept.sortOrder || 1,
      description: dept.description || '',
    });
    modalDialog.value?.toggle();
  } catch (error) {
    MessagePlugin.error('获取部门信息失败');
  }
}

async function onSubmit() {
  if (!departmentModel.deptName) {
    MessagePlugin.error('请输入部门名称');
    return;
  }
  if (!departmentModel.deptCode) {
    MessagePlugin.error('请输入部门编号');
    return;
  }

  try {
    if (isAdd.value) {
      await saveDept(departmentModel);
      MessagePlugin.success('新增部门成功');
    } else {
      await updateDept(departmentModel);
      MessagePlugin.success('更新部门成功');
    }
    modalDialog.value?.close();
    loadData();
  } catch (error: any) {
    MessagePlugin.error(error?.response?.data?.msg || '操作失败');
  }
}

async function onDelete(item: SysDept) {
  if (hasChildren(item)) {
    MessagePlugin.warning('当前部门下有子部门，不能删除');
    return;
  }
  const confirmDia = DialogPlugin.confirm({
    header: '提示',
    body: '确定要删除此部门吗？',
    cancelBtn: '取消',
    confirmBtn: '删除',
    onConfirm: async () => {
      try {
        await deleteDept(item.id);
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
    MessagePlugin.warning('请选择要删除的部门');
    return;
  }
  const confirmDia = DialogPlugin.confirm({
    header: '提示',
    body: `确定要删除选中的 ${selectedRowKeys.value.length} 个部门吗？`,
    cancelBtn: '取消',
    confirmBtn: '删除',
    onConfirm: async () => {
      try {
        await deleteDeptBatch(selectedRowKeys.value);
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

onMounted(loadData);
</script>
