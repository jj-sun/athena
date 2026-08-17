<template>
  <div style="height: 100%">
    <t-row>
      <t-col :flex="1" style="height: inherit">
        <t-card style="height: 100%" class="h-full" :body-style="{ padding: '5px' }" :header-style="{ padding: '5px' }">
          <t-space>
            <t-input v-model:value="deptSearch" placeholder="搜索部门" size="small" @change="deptSearchChange" />
            <t-switch v-model:value="expandAllFlag" size="small" />
          </t-space>
          <div class="mt-4">
            <t-tree
              :expand-all="expandAllFlag"
              :data="departmentTree"
              checkable
              activable
              :expand-level="1"
              :filter="deptFilter"
              :keys="{ value: 'id', label: 'deptName' }"
              line
              hover
              @change="onDeptCheck"
            />
          </div>
        </t-card>
      </t-col>
      <t-col :flex="9">
        <table-search @search="loadData" @reset-search="resetSearch">
          <template #search-content>
            <t-col :span="3">
              <t-form-item label="性别" style="width: 100%">
                <t-input v-model:value="searchForm.username" placeholder="请输入用户名" />
              </t-form-item>
            </t-col>
            <t-col :span="3">
              <t-form-item label="姓名" style="width: 100%">
                <t-input v-model:value="searchForm.realname" placeholder="请输入姓名" />
              </t-form-item>
            </t-col>
            <t-col :span="3">
              <t-form-item label="邮箱" style="width: 100%">
                <t-input v-model:value="searchForm.email" placeholder="请输入邮箱" />
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
              <t-button theme="primary">
                <template #icon><upload-icon /></template>
                导入
              </t-button>
              <t-button theme="primary">
                <template #icon><download-icon /></template>
                导出
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
            :column-controller="columnControllerConfig"
            table-layout="fixed"
            :row-key="rowKey"
            :scroll="{ x: 'max-content' }"
            @select-change="onSelectChange"
          >
            <template #sex="{ row }">
              {{ row.sex === 1 ? '男' : '女' }}
            </template>
            <template #avatar="{ row }">
              <t-avatar>{{ row.realname?.substring(0, 1) || '?' }}</t-avatar>
            </template>
            <template #delFlag="{ row }">
              <t-tag v-if="row.delFlag === 0" theme="success" size="small">正常</t-tag>
              <t-tag v-else theme="danger" size="small">禁用</t-tag>
            </template>
            <template #actions="{ row }">
              <t-space>
                <t-button theme="primary" ghost size="small" @click="onEdit(row)">编辑</t-button>
                <t-button theme="warning" ghost size="small" @click="onResetPassword(row)">
                  <template #icon><key-icon /></template>
                  改密
                </t-button>
                <t-button theme="danger" ghost size="small" @click="onDelete(row)">删除</t-button>
              </t-space>
            </template>
          </t-table>
        </t-card>
      </t-col>
    </t-row>

    <modal-dialog ref="modalDialogRef" :title="actionTitle" @confirm="onSubmit">
      <template #content>
        <t-form :model="formData" :label-col="{ span: 6 }">
          <t-form-item label="用户名" name="username">
            <t-input v-model:value="formData.username" placeholder="请输入用户名" />
          </t-form-item>
          <t-form-item label="姓名" name="realname">
            <t-input v-model:value="formData.realname" placeholder="请输入姓名" />
          </t-form-item>
          <t-form-item v-if="isAdd" label="密码" name="password">
            <t-input v-model:value="formData.password" type="password" placeholder="请输入密码" />
          </t-form-item>
          <t-form-item label="邮箱" name="email">
            <t-input v-model:value="formData.email" placeholder="请输入邮箱" />
          </t-form-item>
          <t-form-item label="手机号" name="mobile">
            <t-input v-model:value="formData.mobile" placeholder="请输入手机号" />
          </t-form-item>
          <t-form-item label="性别" name="sex">
            <t-radio-group v-model:value="formData.sex">
              <t-radio :value="1">男</t-radio>
              <t-radio :value="2">女</t-radio>
            </t-radio-group>
          </t-form-item>
          <t-form-item label="所属部门" name="deptCode">
            <t-tree-select
              v-model:value="formData.deptCode"
              placeholder="请选择部门"
              :data="departmentTree"
              :keys="{ value: 'id', label: 'deptName' }"
              clearable
            />
          </t-form-item>
          <t-form-item label="角色" name="roleIdList">
            <t-select v-model:value="formData.roleIdList" placeholder="请选择角色" multiple :options="roleOptions" />
          </t-form-item>
        </t-form>
      </template>
    </modal-dialog>

    <t-dialog
      v-model:visible="passwordDialogVisible"
      header="修改密码"
      cancel-btn="取消"
      confirm-btn="确定"
      @confirm="onSubmitPassword"
      @cancel="passwordDialogVisible = false"
    >
      <t-form :model="passwordForm" :label-col="{ span: 5 }">
        <t-form-item label="用户名">
          <t-input v-model:value="passwordForm.username" disabled />
        </t-form-item>
        <t-form-item label="新密码">
          <t-input v-model:value="passwordForm.password" type="password" placeholder="请输入新密码" />
        </t-form-item>
        <t-form-item label="确认密码">
          <t-input v-model:value="passwordForm.confirmPassword" type="password" placeholder="请再次输入新密码" />
        </t-form-item>
      </t-form>
    </t-dialog>
  </div>
</template>
<script lang="ts" setup>
import { DeleteIcon, DownloadIcon, KeyIcon, MoreIcon, PlusIcon, UploadIcon } from 'tdesign-icons-vue-next';
import type { TreeProps } from 'tdesign-vue-next';
import { DialogPlugin, MessagePlugin } from 'tdesign-vue-next';
import { getCurrentInstance, onMounted, reactive, ref, useTemplateRef } from 'vue';

import { getDeptList } from '@/api/system/dept';
import { getRoleSelect } from '@/api/system/role';
import {
  deleteUser,
  deleteUserBatch,
  getUserInfo,
  getUserList,
  resetUserPassword,
  saveUser,
  updateUser,
} from '@/api/system/user';
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
  useTableHeight,
} from '@/hooks/table';
import type { TablePropsType } from '@/types/components';
import type { SysDept, SysRole, SysUser } from '@/types/system';
import { sortColumns } from '@/utils';

const { tableLoading, dataList, tableHeight, indexColumn, handleSuccess } = useTable();
const rowKey = useRowKey('id');
const pagination = usePagination(loadData);
const { selectedRowKeys, onSelectChange } = useRowSelection();
const { columnControllerConfig } = useColumnControllerConfig({});

const searchForm = reactive({
  username: '',
  realname: '',
  email: '',
});

const expandAllFlag = ref(false);
const deptSearch = ref('');
const selectedDeptKeys = ref([] as Array<string>);
const departmentTree = ref([] as Array<any>);
const deptOptions = ref([] as Array<SysDept>);
const roleOptions = ref([] as Array<SysRole>);
const deptFilter = ref<TreeProps['filter']>(null);

const modalDialogRef = useTemplateRef('modalDialogRef');
const actionTitle = ref('新增用户');
const isAdd = ref(true);

const formData = reactive<SysUser>({
  id: '',
  username: '',
  realname: '',
  password: '',
  email: '',
  mobile: '',
  sex: 1,
  deptCode: '',
  roleIdList: [],
});

const passwordDialogVisible = ref(false);
const passwordForm = reactive({
  id: '',
  username: '',
  password: '',
  confirmPassword: '',
});

const tableColumns = reactive(
  useTableColumn(
    [
      ...indexColumn,
      {
        title: '用户名',
        colKey: 'username',
      },
      {
        title: '姓名',
        colKey: 'realname',
      },
      {
        title: '邮箱',
        colKey: 'email',
      },
      {
        title: '手机号',
        colKey: 'mobile',
      },
      {
        title: '性别',
        colKey: 'sex',
        width: 80,
      },
      {
        title: '头像',
        colKey: 'avatar',
      },
      {
        title: '部门',
        colKey: 'deptCode_dictText',
      },
      {
        title: '状态',
        colKey: 'delFlag',
        width: 80,
      },
      {
        title: '创建时间',
        colKey: 'ctime',
      },
      {
        title: '操作',
        colKey: 'actions',
        fixed: 'right',
        width: 200,
      },
    ],
    {
      align: 'center',
    },
  ),
);

function resetSearch() {
  searchForm.username = '';
  searchForm.realname = '';
  searchForm.email = '';
  loadData();
}

async function loadData() {
  tableLoading.value = true;
  try {
    const params: any = {
      page: pagination.current,
      pageSize: pagination.pageSize,
      ...searchForm,
    };
    if (selectedDeptKeys.value.length > 0) {
      params.deptCode = selectedDeptKeys.value.join(',');
    }
    const res = await getUserList(params);
    handleSuccess(res.list);
    pagination.setTotalSize(res.total);
  } catch (error) {
    console.error('加载用户列表失败', error);
  } finally {
    tableLoading.value = false;
  }
}

async function loadDeptTree() {
  try {
    const res = await getDeptList();
    departmentTree.value = res;
  } catch (error) {
    console.error('加载部门失败', error);
  }
}

async function loadRoleOptions() {
  try {
    const res = await getRoleSelect();
    roleOptions.value = res;
  } catch (error) {
    console.error('加载角色失败', error);
  }
}

function deptSearchChange(state: any) {
  if (!deptSearch.value) {
    deptFilter.value = null;
    return;
  }
  deptFilter.value = (node: any) => {
    const rs = (node.label as string).includes(deptSearch.value);
    return rs;
  };
}

function onDeptCheck(checked) {
  selectedDeptKeys.value = checked;
  loadData();
}

function onAdd() {
  isAdd.value = true;
  actionTitle.value = '新增用户';
  Object.assign(formData, {
    id: '',
    username: '',
    realname: '',
    password: '',
    email: '',
    mobile: '',
    sex: 1,
    deptCode: '',
    roleIdList: [],
  });
  modalDialogRef.value.toggle();
}

async function onEdit(record: SysUser) {
  isAdd.value = false;
  actionTitle.value = '编辑用户';
  try {
    const user: SysUser = await getUserInfo(record.id);
    Object.assign(formData, {
      id: user.id,
      username: user.username,
      realname: user.realname,
      password: '',
      email: user.email,
      mobile: user.mobile,
      sex: user.sex || 1,
      deptCode: user.deptCode,
      roleIdList: user.roleIdList || [],
    });
    modalDialogRef.value?.toggle();
  } catch (error) {
    MessagePlugin.error('获取用户信息失败');
  }
}

async function onSubmit() {
  if (!formData.username) {
    MessagePlugin.error('请输入用户名');
    return;
  }
  if (!formData.realname) {
    MessagePlugin.error('请输入姓名');
    return;
  }
  if (!formData.email) {
    MessagePlugin.error('请输入邮箱');
    return;
  }
  if (isAdd.value && !formData.password) {
    MessagePlugin.error('请输入密码');
    return;
  }

  try {
    if (isAdd.value) {
      await saveUser(formData);
      MessagePlugin.success('新增用户成功');
    } else {
      await updateUser(formData);
      MessagePlugin.success('更新用户成功');
    }
    modalDialogRef.value?.close();
    loadData();
  } catch (error: any) {
    MessagePlugin.error(error?.response?.data?.msg || '操作失败');
  }
}

async function onDelete(record: SysUser) {
  const confirmDia = DialogPlugin.confirm({
    header: '提示',
    body: '确定要删除此用户吗？',
    cancelBtn: '取消',
    confirmBtn: '删除',
    onConfirm: async () => {
      try {
        await deleteUser(record.id);
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
    MessagePlugin.warning('请选择要删除的用户');
    return;
  }
  const confirmDia = DialogPlugin.confirm({
    header: '提示',
    body: `确定要删除选中的 ${selectedRowKeys.value.length} 个用户吗？`,
    cancelBtn: '取消',
    confirmBtn: '删除',
    onConfirm: async () => {
      try {
        await deleteUserBatch(selectedRowKeys.value);
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

function onResetPassword(row: SysUser) {
  passwordForm.id = row.id;
  passwordForm.username = row.username;
  passwordForm.password = '';
  passwordForm.confirmPassword = '';
  passwordDialogVisible.value = true;
}

async function onSubmitPassword() {
  if (!passwordForm.password) {
    MessagePlugin.error('请输入新密码');
    return false;
  }
  if (passwordForm.password.length < 6) {
    MessagePlugin.error('密码长度不能少于6位');
    return false;
  }
  if (passwordForm.password !== passwordForm.confirmPassword) {
    MessagePlugin.error('两次密码输入不一致');
    return false;
  }
  try {
    await resetUserPassword(passwordForm.id, passwordForm.password);
    MessagePlugin.success('密码修改成功');
    passwordDialogVisible.value = false;
    return true;
  } catch (error: any) {
    MessagePlugin.error(error?.response?.data?.msg || '密码修改失败');
    return false;
  }
}

function onUpdateTable(newColumns: Array<TablePropsType>) {
  sortColumns(tableColumns, newColumns);
}

onMounted(async () => {
  tableHeight.value = await useTableHeight(getCurrentInstance());
  loadData();
  loadDeptTree();
  loadRoleOptions();
});
</script>
