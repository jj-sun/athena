<template>
  <div class="main-container">
    <t-card>
      <table-header :table-columns="tableColumns" :show-message="false" @do-refresh="loadData">
        <template #action>
          <t-button theme="primary" @click="onAdd">
            <template #icon><plus-icon /></template>
            新增
          </t-button>
        </template>
      </table-header>
      <t-enhanced-table
        :loading="tableLoading"
        :data="treeData"
        :columns="tableColumns"
        :pagination="false"
        :row-key="rowKey"
        :column-controller="columnControllerConfig"
        :tree="treeConfig"
      >
        <template #type="{ row }">
          <t-tag v-if="row.type === 0" theme="success" size="small">目录</t-tag>
          <t-tag v-else-if="row.type === 1" theme="warning" size="small">菜单</t-tag>
          <t-tag v-else theme="danger" size="small">按钮</t-tag>
        </template>
        <template #icon="{ row }">
          <component :is="row.icon || 'MenuIcon'" style="font-size: 18px" />
        </template>
        <template #actions="{ row }">
          <t-space>
            <t-button theme="primary" ghost size="small" @click="onEdit(row)">编辑</t-button>
            <t-button theme="danger" ghost size="small" @click="onDelete(row)">删除</t-button>
          </t-space>
        </template>
      </t-enhanced-table>
    </t-card>

    <modal-dialog
      ref="modalDialog"
      :title="actionModel === 'add' ? '添加菜单' : '编辑菜单'"
      content-height="50vh"
      @confirm="onConfirm"
    >
      <template #content>
        <t-form :model="formData" :label-col="{ span: 6 }">
          <t-form-item label="上级菜单" name="parentId">
            <t-select v-model:value="formData.parentId" placeholder="请选择上级菜单">
              <t-select-option value="0">无</t-select-option>
              <t-select-option v-for="item of menuOptions" :key="item.id" :value="item.id">
                {{ item.name }}
              </t-select-option>
            </t-select>
          </t-form-item>
          <t-form-item label="菜单名称" name="name">
            <t-input v-model:value="formData.name" placeholder="请输入菜单名称" />
          </t-form-item>
          <t-form-item label="菜单类型" name="type">
            <t-radio-group v-model:value="formData.type" @change="onTypeChange">
              <t-radio :value="0">目录</t-radio>
              <t-radio :value="1">菜单</t-radio>
              <t-radio :value="2">按钮</t-radio>
            </t-radio-group>
          </t-form-item>
          <t-form-item v-if="formData.type === 1 || formData.type === 0" label="菜单URL" name="url">
            <t-input v-model:value="formData.url" placeholder="请输入菜单URL" />
          </t-form-item>
          <t-form-item v-if="formData.type === 1" label="组件路径" name="component">
            <t-input v-model:value="formData.component" placeholder="请输入组件路径" />
          </t-form-item>
          <t-form-item v-if="formData.type === 2" label="权限标识" name="perms">
            <t-input v-model:value="formData.perms" placeholder="请输入权限标识" />
          </t-form-item>
          <t-form-item label="菜单图标" name="icon">
            <t-input v-model:value="formData.icon" placeholder="请输入图标名称" />
          </t-form-item>
          <t-form-item label="排序" name="orderNum">
            <t-input-number v-model:value="formData.orderNum" />
          </t-form-item>
        </t-form>
      </template>
    </modal-dialog>
  </div>
</template>
<script lang="ts" setup>
import { PlusIcon } from 'tdesign-icons-vue-next';
import type { EnhancedTableProps } from 'tdesign-vue-next';
import { DialogPlugin, MessagePlugin } from 'tdesign-vue-next';
import { onMounted, reactive, ref, useTemplateRef } from 'vue';

import { deleteMenu, getMenuInfo, getMenuList, saveMenu, updateMenu } from '@/api/system/permission';
import ModalDialog from '@/components/ModalDialog.vue';
import TableHeader from '@/components/table/TableHeader.vue';
import { useColumnControllerConfig, useRowKey, useTable, useTableColumn } from '@/hooks/table';
import type { SysPermission } from '@/types/system';

const { tableLoading, dataList, indexColumn, handleSuccess } = useTable();
const rowKey = useRowKey('id');

const actionModel = ref<'add' | 'edit'>('add');
const modalDialog = useTemplateRef('modalDialog');
const treeData = ref([] as Array<SysPermission>);
const menuOptions = ref([] as Array<SysPermission>);
const { columnControllerConfig } = useColumnControllerConfig({});

const formData = reactive<SysPermission>({
  id: '',
  parentId: '0',
  name: '',
  url: '',
  component: '',
  perms: '',
  type: 1,
  icon: '',
  orderNum: 0,
});

const tableColumns = useTableColumn(
  [
    ...indexColumn,
    {
      title: '菜单名称',
      colKey: 'name',
    },
    {
      title: '菜单类型',
      colKey: 'type',
    },
    {
      title: '菜单URL',
      colKey: 'url',
    },
    {
      title: '组件路径',
      colKey: 'component',
    },
    {
      title: '权限标识',
      colKey: 'perms',
    },
    {
      title: '菜单图标',
      colKey: 'icon',
    },
    {
      title: '排序',
      colKey: 'orderNum',
    },
    {
      title: '操作',
      colKey: 'actions',
    },
  ],
  {
    align: 'center',
  },
);
const treeConfig: EnhancedTableProps['tree'] = reactive({
  childrenKey: 'children',
  treeNodeColumnIndex: 2,
  indent: 25,
  expandTreeNodeOnClick: true,
});
async function loadData() {
  tableLoading.value = true;
  try {
    const res = await getMenuList();
    treeData.value = res;
    menuOptions.value = flattenMenu(res);
    handleSuccess(res);
  } catch (error) {
    console.error('加载菜单列表失败', error);
  } finally {
    tableLoading.value = false;
  }
}

function flattenMenu(menus: Array<SysPermission>): Array<SysPermission> {
  const result: Array<SysPermission> = [];
  menus.forEach((menu) => {
    result.push(menu);
    if (menu.children) {
      result.push(...flattenMenu(menu.children));
    }
  });
  return result;
}

function hasChildren(item: SysPermission): boolean {
  return item.children && item.children.length > 0;
}

function onAdd() {
  actionModel.value = 'add';
  Object.assign(formData, {
    id: '',
    parentId: '0',
    name: '',
    url: '',
    component: '',
    perms: '',
    type: 1,
    icon: '',
    orderNum: 0,
  });
  modalDialog.value?.show();
}

async function onEdit(item: SysPermission) {
  actionModel.value = 'edit';
  try {
    const menu = await getMenuInfo(item.id);
    Object.assign(formData, {
      id: menu.id,
      parentId: menu.parentId || '0',
      name: menu.name,
      url: menu.url || '',
      component: menu.component || '',
      perms: menu.perms || '',
      type: menu.type || 1,
      icon: menu.icon || '',
      orderNum: menu.orderNum || 0,
    });
    modalDialog.value?.show();
  } catch (error) {
    MessagePlugin.error('获取菜单信息失败');
  }
}

function onTypeChange() {
  if (formData.type !== 1) {
    formData.component = '';
  }
  if (formData.type !== 2) {
    formData.perms = '';
  }
}

async function onConfirm() {
  if (!formData.name) {
    MessagePlugin.error('请输入菜单名称');
    return;
  }
  if (formData.type === 0 || formData.type === 1) {
    if (!formData.url) {
      MessagePlugin.error('请输入菜单URL');
      return;
    }
  }
  if (formData.type === 2 && !formData.perms) {
    MessagePlugin.error('请输入权限标识');
    return;
  }

  try {
    if (actionModel.value === 'add') {
      await saveMenu(formData);
      MessagePlugin.success('新增菜单成功');
    } else {
      await updateMenu(formData);
      MessagePlugin.success('更新菜单成功');
    }
    modalDialog.value?.close();
    loadData();
  } catch (error: any) {
    MessagePlugin.error(error?.response?.data?.msg || '操作失败');
  }
}

async function onDelete(item: SysPermission) {
  if (hasChildren(item)) {
    MessagePlugin.warning('请先删除子菜单或按钮');
    return;
  }
  const confirmDia = DialogPlugin.confirm({
    header: '提示',
    body: '是否要删除此数据？',
    confirmBtn: '删除',
    cancelBtn: '取消',
    onConfirm: async () => {
      try {
        await deleteMenu(item.id);
        MessagePlugin.success('删除成功');
        loadData();
      } catch (error: any) {
        MessagePlugin.error(error?.response?.data?.msg || '删除失败');
      } finally {
        confirmDia.destroy();
      }
    },
  });
}

onMounted(loadData);
</script>
