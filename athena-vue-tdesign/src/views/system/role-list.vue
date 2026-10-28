<template>
  <div class="main-container">
    <table-search @search="onSearch" @reset-search="resetSearch">
      <template #search-content>
        <t-col :span="3">
          <t-form-item label="角色名称" style="width: 100%">
            <t-input v-model:value="searchForm.roleName" placeholder="请输入角色名称" />
          </t-form-item>
        </t-col>
        <t-col :span="3">
          <t-form-item label="角色编号" style="width: 100%">
            <t-input v-model:value="searchForm.roleCode" placeholder="请输入角色编号" />
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
          <t-button v-permission="'sys:role:save'" theme="primary" @click="onAdd">
            <template #icon><plus-icon /></template>
            新增
          </t-button>
          <t-dropdown>
            <t-button v-permission="'sys:role:delete'">
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
        :column-controller="columnControllerConfig"
        @select-change="onSelectChange"
      >
        <template #delFlag="{ row }">
          <t-tag v-if="row.delFlag === 0" theme="success" size="small">正常</t-tag>
          <t-tag v-else theme="danger" size="small">禁用</t-tag>
        </template>
        <template #actions="{ row }">
          <t-space>
            <t-button
              v-permission="['sys:role:info', 'sys:role:update']"
              theme="primary"
              ghost
              size="small"
              @click="onEdit(row)"
            >
              编辑
            </t-button>
            <t-button v-permission="'sys:role:delete'" theme="danger" ghost size="small" @click="onDelete(row)">
              删除
            </t-button>
            <t-button
              v-permission="['sys:role:assignPermission', 'sys:menu:list']"
              theme="primary"
              ghost
              size="small"
              @click="onShowMenu(row)"
            >
              菜单权限
            </t-button>
            <t-button v-permission="'sys:role:list'" theme="primary" ghost size="small" @click="onShowUsers(row)">
              用户
            </t-button>
          </t-space>
        </template>
      </t-table>
    </t-card>

    <modal-dialog ref="modalDialogRef" :title="actionTitle" @cancel="onCancel" @confirm="onSubmit">
      <template #content>
        <t-form ref="formRef" :data="formData" :rules="rules" :label-col="{ span: 6 }">
          <t-form-item label="角色名称" name="roleName">
            <t-input v-model:value="formData.roleName" placeholder="请输入角色名称" />
          </t-form-item>
          <t-form-item label="角色编号" name="roleCode">
            <t-input v-model:value="formData.roleCode" placeholder="请输入角色编号" />
          </t-form-item>
          <t-form-item label="角色描述" name="remark">
            <t-textarea
              v-model:value="formData.remark"
              placeholder="请输入角色描述"
              :auto-size="{ minRows: 3, maxRows: 5 }"
            />
          </t-form-item>
        </t-form>
      </template>
    </modal-dialog>

    <modal-drawer ref="menuModalDialogRef" title="编辑菜单权限" @confirm="onMenuConfirm">
      <template #content>
        <t-tree
          v-model:expanded="defaultExpandedKeys"
          v-model:value="defaultCheckedKeys"
          :data="menuTreeData"
          :keys="{ value: 'id', label: 'name' }"
          :check-strictly="true"
          :disable-check="disableCheck"
          checkable
        />
      </template>
    </modal-drawer>
    <modal-drawer ref="userModalDrawerRef" title="用户" size="large" @confirm="onUserDrawerConfirm">
      <template #content>
        <div class="user-drawer-content">
          <div class="user-drawer-header">
            <span
              >当前角色：<strong>{{ currentRole?.roleName }}</strong></span
            >
            <t-button v-permission="'sys:role:update'" theme="primary" size="small" @click="onOpenAddUserDialog">
              <template #icon><plus-icon /></template>
              添加用户
            </t-button>
          </div>
          <t-table
            :loading="userTableLoading"
            :data="userList"
            :columns="userColumns"
            :pagination="userPagination"
            :row-key="userRowKey"
            size="small"
            table-layout="fixed"
          >
            <template #sex="{ row }">
              {{ row.sex === 1 ? '男' : '女' }}
            </template>
            <template #delFlag="{ row }">
              <t-tag v-if="row.delFlag === 0" theme="success" size="small">正常</t-tag>
              <t-tag v-else theme="danger" size="small">禁用</t-tag>
            </template>
            <template #userActions="{ row }">
              <t-button v-permission="'sys:role:update'" theme="danger" ghost size="small" @click="onRemoveUser(row)">
                移除
              </t-button>
            </template>
          </t-table>
        </div>
      </template>
    </modal-drawer>

    <modal-dialog ref="addUserDialogRef" title="添加用户" @confirm="onAddUserConfirm">
      <template #content>
        <t-form :label-col="{ span: 4 }">
          <t-form-item label="选择用户">
            <t-select
              v-model:value="selectedUserIds"
              placeholder="输入用户名搜索后选择"
              multiple
              :options="availableUserOptions"
              :max="50"
              filterable
              :filter="() => true"
              :loading="userSearchLoading"
              loading-text="加载中…"
              @search="onUserSearch"
            />
          </t-form-item>
        </t-form>
      </template>
    </modal-dialog>
  </div>
</template>
<script lang="ts" setup>
import { DeleteIcon, MoreIcon, PlusIcon } from 'tdesign-icons-vue-next';
import type { FormInstanceFunctions, FormProps, TreeNodeModel } from 'tdesign-vue-next';
import { DialogPlugin, MessagePlugin } from 'tdesign-vue-next';
import { onMounted, reactive, ref, useTemplateRef } from 'vue';

import { getMenuList } from '@/api/system/permission';
import {
  addUserToRole,
  deleteRole,
  deleteRoleBatch,
  getPermissionIdList,
  getRoleInfo,
  getRoleList,
  getRoleUserList,
  removeUserFromRole,
  saveRole,
  saveRolePerms,
  updateRole,
} from '@/api/system/role';
import { getUserList } from '@/api/system/user';
import ModalDialog from '@/components/ModalDialog.vue';
import ModalDrawer from '@/components/ModalDrawer.vue';
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
import type { SysRole, SysUser } from '@/types/system';
import { sortColumns } from '@/utils';

const { tableLoading, dataList, indexColumn, handleSuccess } = useTable();
const rowKey = useRowKey('id');
const pagination = usePagination(loadData);
const { selectedRowKeys, onSelectChange, onClearSelected } = useRowSelection();
const { columnControllerConfig } = useColumnControllerConfig({
  fields: ['remark', 'delFlag', 'ctime'],
});
const formRef = ref<FormInstanceFunctions>(null);

const searchForm = reactive({
  roleName: '',
  roleCode: '',
});
const modalDialogRef = useTemplateRef('modalDialogRef');
const menuModalDialogRef = useTemplateRef('menuModalDialogRef');
const actionTitle = ref('添加角色');
const isAdd = ref(true);

const formData = reactive<SysRole>({
  id: '',
  roleName: '',
  roleCode: '',
  remark: '',
});
const rules: FormProps['rules'] = {
  roleName: [
    {
      required: true,
      message: '角色名必填',
      type: 'error',
    },
  ],
  roleCode: [
    {
      required: true,
      message: '角色编码必填',
      type: 'error',
    },
  ],
};

const menuTreeData = ref([] as Array<any>);
const defaultCheckedKeys = ref([] as Array<string>);
const defaultExpandedKeys = ref([] as Array<string>);
/** 强制菜单（仪表盘、概览），在权限树中保持勾选且禁止取消 */
const mandatoryMenuIds = ref([] as Array<string>);

/** 与后端 getMandatoryMenuIds() 保持一致 */
const MANDATORY_MENU_URLS = ['/dashboard', '/dashboard/analysis'];

/** 从菜单树中按 URL 找出强制菜单 ID（菜单 ID 是动态雪花值，不能写死） */
function collectMandatoryMenuIds(nodes: Array<any>): Array<string> {
  const ids: Array<string> = [];
  const walk = (list: Array<any>) => {
    list?.forEach((node) => {
      if (node?.url && MANDATORY_MENU_URLS.includes(node.url)) {
        ids.push(String(node.id));
      }
      if (node?.children?.length) {
        walk(node.children);
      }
    });
  };
  walk(nodes);
  return ids;
}

function disableCheck(node: TreeNodeModel): boolean {
  return mandatoryMenuIds.value.includes(String(node.value));
}

const userModalDrawerRef = useTemplateRef('userModalDrawerRef');
const addUserDialogRef = useTemplateRef('addUserDialogRef');
const currentRole = ref<SysRole | null>(null);

const { tableLoading: userTableLoading, dataList: userList, handleSuccess: handleUserSuccess } = useTable();
const userRowKey = useRowKey('id');
const userPagination = usePagination(loadRoleUsers);
const selectedUserIds = ref([] as Array<string>);
const availableUserOptions = ref([] as Array<{ label: string; value: string }>);
/** 远程搜索用户时的加载态 */
const userSearchLoading = ref(false);

const userColumns = useTableColumn(
  [
    { title: '用户名', colKey: 'username' },
    { title: '姓名', colKey: 'realname' },
    { title: '性别', colKey: 'sex', width: 80 },
    { title: '邮箱', colKey: 'email' },
    { title: '手机号', colKey: 'mobile' },
    { title: '状态', colKey: 'delFlag', width: 80 },
    { title: '操作', colKey: 'userActions', width: 80 },
  ],
  { align: 'center' },
);

const tableColumns = useTableColumn(
  [
    ...indexColumn,
    {
      title: '角色名称',
      colKey: 'roleName',
    },
    {
      title: '角色编号',
      colKey: 'roleCode',
    },
    {
      title: '角色描述',
      colKey: 'remark',
    },
    {
      title: '状态',
      colKey: 'delFlag',
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

function onSearch() {
  pagination.current = 1;
  loadData();
}

function resetSearch() {
  searchForm.roleName = '';
  searchForm.roleCode = '';
  pagination.current = 1;
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
    const res = await getRoleList(params);
    void handleSuccess(res.list);
    pagination.setTotalSize(res.total);
  } catch (error) {
    console.error('加载角色列表失败', error);
  } finally {
    tableLoading.value = false;
  }
}

function onAdd() {
  isAdd.value = true;
  actionTitle.value = '添加角色';
  Object.assign(formData, {
    id: '',
    roleName: '',
    roleCode: '',
    remark: '',
  });
  modalDialogRef.value.toggle();
}

async function onEdit(record: SysRole) {
  isAdd.value = false;
  actionTitle.value = '编辑角色';
  getRoleInfo(record.id)
    .then((role) => {
      Object.assign(formData, {
        id: role.id,
        roleName: role.roleName,
        roleCode: role.roleCode,
        remark: role.remark,
      });
      modalDialogRef.value?.toggle();
    })
    .catch((err) => {
      console.log(err.message);
      MessagePlugin.error('获取角色信息失败');
    });
}

async function onSubmit() {
  const validateResult = await formRef.value.validate();
  if (validateResult === true) {
    try {
      let msg = '新增角色成功';
      if (isAdd.value) {
        await saveRole(formData);
      } else {
        await updateRole(formData);
        msg = '更新角色成功';
      }
      void MessagePlugin.success(msg);
      modalDialogRef.value?.close();
      await loadData();
    } catch (error: any) {
      void MessagePlugin.error(error?.message || '操作失败');
    }
  }
}
function onCancel() {
  formRef.value.clearValidate();
}
function onDelete(record: SysRole) {
  const confirmDia = DialogPlugin.confirm({
    header: '提示',
    body: '是否要删除此角色？',
    cancelBtn: '取消',
    confirmBtn: '删除',
    onConfirm: () => {
      deleteRole(record.id)
        .then(() => {
          MessagePlugin.success('删除成功');
          // 删除的是当前页唯一一条时，删完回退一页
          if (dataList.length === 1 && pagination.current > 1) {
            pagination.current -= 1;
          }
          loadData();
        })
        .catch((err) => {
          console.log(err.message);
          MessagePlugin.error('删除失败');
        })
        .finally(() => {
          confirmDia.destroy();
        });
    },
  });
}

async function onBatchDelete() {
  if (selectedRowKeys.value.length === 0) {
    void MessagePlugin.warning('请选择要删除的角色');
    return;
  }
  const confirmDia = DialogPlugin.confirm({
    header: '提示',
    body: `确定要删除选中的 ${selectedRowKeys.value.length} 个角色吗？`,
    cancelBtn: '取消',
    confirmBtn: '删除',
    onConfirm: () => {
      const ids = (selectedRowKeys.value || []) as string[];
      deleteRoleBatch(ids)
        .then(() => {
          MessagePlugin.success('批量删除成功');
          // 删除后当前页可能为空，回退一页
          if (ids.length >= dataList.length && pagination.current > 1) {
            pagination.current -= 1;
          }
          loadData();
          onClearSelected();
        })
        .catch((err) => {
          console.log(err);
          MessagePlugin.error('批量删除失败');
        })
        .finally(() => {
          confirmDia.destroy();
        });
    },
  });
}

async function onShowMenu(row: SysRole) {
  currentRole.value = row;
  try {
    menuTreeData.value = await getMenuList();
    const assignedPermissionIds = await getPermissionIdList(row.id);
    mandatoryMenuIds.value = collectMandatoryMenuIds(menuTreeData.value);
    // 已分配权限 + 强制菜单（前端兜底补齐，避免后端版本差异导致仪表盘未勾选）
    defaultCheckedKeys.value = Array.from(new Set([...(assignedPermissionIds ?? []), ...mandatoryMenuIds.value]));
    defaultExpandedKeys.value = defaultCheckedKeys.value;
    menuModalDialogRef.value?.toggle();
  } catch (error) {
    console.error('加载菜单失败', error);
  }
}

function onMenuConfirm() {
  if (!currentRole.value) return;
  // 允许提交空数组：后端支持清空该角色的全部权限
  saveRolePerms({ roleId: currentRole.value.id, permissionIdList: defaultCheckedKeys.value as string[] })
    .then(() => {
      MessagePlugin.success(`权限设置成功`);
    })
    .catch((err) => {
      console.log(err);
      MessagePlugin.error('权限设置失败');
    })
    .finally(() => {
      menuModalDialogRef.value?.close();
    });
}

function onUpdateTable(newColumns: Array<TablePropsType>) {
  sortColumns(tableColumns, newColumns);
}

function onShowUsers(row: SysRole) {
  currentRole.value = row;
  userPagination.current = 1;
  loadRoleUsers();
  userModalDrawerRef.value?.toggle();
}

function loadRoleUsers() {
  if (!currentRole.value) return;
  userTableLoading.value = true;
  getRoleUserList({
    roleId: currentRole.value.id,
    page: userPagination.current,
    pageSize: userPagination.pageSize,
  })
    .then((res) => {
      handleUserSuccess(res.list);
      userPagination.setTotalSize(res.total);
    })
    .catch((err) => {
      console.error('加载角色用户列表失败', err);
      MessagePlugin.error('加载角色用户列表失败');
    })
    .finally(() => {
      userTableLoading.value = false;
    });
}

async function onRemoveUser(row: SysUser) {
  if (!currentRole.value) return;
  const confirmDia = DialogPlugin.confirm({
    header: '提示',
    body: `确定要从角色"${currentRole.value.roleName}"中移除用户"${row.realname || row.username}"吗？`,
    cancelBtn: '取消',
    confirmBtn: '移除',
    onConfirm: () => {
      removeUserFromRole(currentRole.value!.id, row.id)
        .then(() => {
          MessagePlugin.success('移除成功');
          // 移除的是当前页唯一一条时，删完回退一页
          if (userList.length === 1 && userPagination.current > 1) {
            userPagination.current -= 1;
          }
          loadRoleUsers();
        })
        .catch((err) => {
          console.log(err);
          MessagePlugin.error('移除失败');
        })
        .finally(() => {
          confirmDia.destroy();
        });
    },
  });
}

function onUserDrawerConfirm() {
  userModalDrawerRef.value?.close();
}

/**
 * 按关键字远程搜索用户。
 *
 * 这里刻意不再用 pageSize: 9999 一次性拉全表 —— 后端已限制单页最大 1000 条，
 * 且超大 pageSize 属于批量拖取数据的行为。
 */
async function searchUsers(keyword: string) {
  userSearchLoading.value = true;
  try {
    const res = await getUserList({
      page: 1,
      pageSize: 20,
      username: keyword || undefined,
    });
    // 已在当前角色中的用户不再出现在候选项里（按已加载的角色成员排除，属尽力而为）
    const currentUserIds = new Set(userList.map((u: SysUser) => u.id));
    availableUserOptions.value = res.list
      .filter((u: SysUser) => !currentUserIds.has(u.id))
      .map((u: SysUser) => ({ label: `${u.realname || u.username}（${u.username}）`, value: u.id }));
  } catch (err) {
    console.log(err);
    MessagePlugin.error('加载用户列表失败');
  } finally {
    userSearchLoading.value = false;
  }
}

function onUserSearch(filterWords: string) {
  void searchUsers(filterWords ?? '');
}

function onOpenAddUserDialog() {
  if (!currentRole.value) return;
  selectedUserIds.value = [];
  availableUserOptions.value = [];
  void searchUsers('');
  addUserDialogRef.value?.toggle();
}

async function onAddUserConfirm() {
  if (!currentRole.value) return;
  if (selectedUserIds.value.length === 0) {
    void MessagePlugin.warning('请选择要添加的用户');
    return;
  }
  addUserToRole({
    roleId: currentRole.value.id,
    userIdList: selectedUserIds.value,
  })
    .then(() => {
      MessagePlugin.success('添加用户成功');
      addUserDialogRef.value?.close();
      loadRoleUsers();
    })
    .catch((err) => {
      console.log(err);
      MessagePlugin.error('添加用户失败');
    });
}

onMounted(loadData);
</script>
<style scoped>
.user-drawer-content {
  padding: 16px;
}
.user-drawer-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
</style>
