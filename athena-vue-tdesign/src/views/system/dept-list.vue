<template>
  <div style="height: 100%">
    <t-row style="height: 100%">
      <t-col :flex="3" style="height: inherit">
        <t-card
          :loading="treeLoading"
          style="height: 100%"
          :body-style="{ padding: '8px' }"
          :header-style="{ padding: '8px 12px' }"
        >
          <template #header>
            <div class="dept-tree__header">
              <span class="dept-tree__title">组织架构</span>
              <t-button v-permission="'sys:dept:save'" theme="primary" variant="text" @click="onAddRoot">
                <template #icon><add-icon /></template>
                新增
              </t-button>
            </div>
          </template>
          <t-tree
            v-model:actived="activedKeys"
            v-model:expanded="expandedKeys"
            class="dept-tree"
            :data="treeData"
            :keys="{ value: 'id', label: 'deptName' }"
            activable
            hover
            line
            transition
            empty="暂无部门数据"
            @active="onTreeActive"
          >
            <template #operations="{ node }">
              <t-button
                v-permission="'sys:dept:save'"
                shape="square"
                size="small"
                theme="primary"
                variant="text"
                title="新增子部门"
                @click.stop="onAddChild(node)"
              >
                <template #icon><add-icon /></template>
              </t-button>
            </template>
          </t-tree>
        </t-card>
      </t-col>

      <t-col :flex="7" class="dept-detail">
        <t-card style="height: 100%">
          <template #header>
            <div class="dept-detail__header">
              <t-space :size="8" align="center">
                <building-icon />
                <span class="dept-detail__title">{{ currentDept ? currentDept.deptName : '部门详情' }}</span>
              </t-space>
              <t-space v-if="currentDept" :size="8">
                <t-button
                  v-permission="['sys:dept:info', 'sys:dept:update']"
                  theme="primary"
                  variant="outline"
                  @click="onEdit"
                >
                  编辑
                </t-button>
                <t-button v-permission="'sys:dept:delete'" theme="danger" variant="outline" @click="onDelete">
                  删除
                </t-button>
              </t-space>
            </div>
          </template>

          <t-descriptions v-if="currentDept" :column="2" bordered size="small">
            <t-descriptions-item label="部门名称">{{ currentDept.deptName }}</t-descriptions-item>
            <t-descriptions-item label="部门编号">{{ currentDept.deptCode || '—' }}</t-descriptions-item>
            <t-descriptions-item label="上级部门">{{ parentName }}</t-descriptions-item>
            <t-descriptions-item label="排序">{{ currentDept.sortOrder ?? 0 }}</t-descriptions-item>
            <t-descriptions-item label="状态">
              <t-tag v-if="currentDept.delFlag === 0" theme="success" size="small">正常</t-tag>
              <t-tag v-else theme="danger" size="small">禁用</t-tag>
            </t-descriptions-item>
            <t-descriptions-item label="描述" :span="2">{{ currentDept.description || '—' }}</t-descriptions-item>
          </t-descriptions>
          <t-empty v-else description="请从左侧选择一个部门" />
        </t-card>
      </t-col>
    </t-row>

    <modal-dialog ref="modalDialogRef" :title="dialogTitle" @cancel="onCancel" @confirm="onSubmit">
      <template #content>
        <t-form ref="formRef" :data="departmentModel" :rules="rules" :label-col="{ span: 6 }">
          <t-form-item label="上级部门" name="parentId">
            <dept-select
              v-model:value="departmentModel.parentId"
              include-root
              :disabled="isAddChild || (!isAdd && departmentModel.parentId === '0')"
            />
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
import { AddIcon, BuildingIcon } from 'tdesign-icons-vue-next';
import type { FormInstanceFunctions, FormProps, TreeNodeModel } from 'tdesign-vue-next';
import { DialogPlugin, MessagePlugin } from 'tdesign-vue-next';
import { computed, onMounted, reactive, ref, useTemplateRef } from 'vue';

import { deleteDept, getDeptInfo, getDeptList, saveDept, updateDept } from '@/api/system/dept';
import DeptSelect from '@/components/dept/index.vue';
import ModalDialog from '@/components/ModalDialog.vue';
import type { SysDept } from '@/types/system';

const treeLoading = ref(false);
const treeData = ref([] as Array<SysDept>);
const selectedId = ref('');
const activedKeys = ref([] as Array<string | number>);
const expandedKeys = ref([] as Array<string | number>);

const modalDialogRef = useTemplateRef('modalDialogRef');
const dialogTitle = ref('');
const isAdd = ref(true);
const isAddChild = ref(false);
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

/** id -> 部门节点，用于查上级名称、刷新后恢复选中 */
const flatMap = computed(() => {
  const map = new Map<string, SysDept>();
  const walk = (list: Array<SysDept> | undefined) => {
    list?.forEach((item) => {
      map.set(item.id, item);
      walk(item.children);
    });
  };
  walk(treeData.value);
  return map;
});

const currentDept = computed(() => flatMap.value.get(selectedId.value));
const parentName = computed(() => {
  const parentId = currentDept.value?.parentId;
  if (!parentId || parentId === '0') {
    return '顶级部门';
  }
  return flatMap.value.get(parentId)?.deptName ?? '—';
});

/** 接口未按 sort_order 排序，这里按排序值递归整理 */
function sortTree(list: Array<SysDept>): Array<SysDept> {
  return [...list]
    .sort((a, b) => (a.sortOrder ?? 0) - (b.sortOrder ?? 0))
    .map((item) => ({
      ...item,
      children: item.children?.length ? sortTree(item.children) : undefined,
    }));
}

/**
 * 重新加载部门树。
 * @param preferId 刷新后优先选中的部门 id；不传则沿用当前选中，失效时回退到同级父部门或第一个根部门
 */
function loadData(preferId?: string) {
  const keepId = preferId ?? selectedId.value;
  treeLoading.value = true;
  getDeptList()
    .then((res: Array<SysDept>) => {
      treeData.value = sortTree(res ?? []);
      if (expandedKeys.value.length === 0) {
        expandedKeys.value = treeData.value.map((item) => item.id);
      }
      const fallbackId = treeData.value[0]?.id ?? '';
      const nextId = keepId && flatMap.value.has(keepId) ? keepId : fallbackId;
      selectedId.value = nextId;
      activedKeys.value = nextId ? [nextId] : [];
    })
    .catch((err) => {
      console.error('加载部门列表失败', err);
      MessagePlugin.error('加载部门列表失败');
    })
    .finally(() => {
      treeLoading.value = false;
    });
}

/** 再次点击已激活节点会取消高亮，这里保持右侧详情不变并恢复高亮 */
function onTreeActive(value: Array<string | number>) {
  if (value.length > 0) {
    selectedId.value = String(value[0]);
    return;
  }
  if (selectedId.value) {
    activedKeys.value = [selectedId.value];
  }
}

function resetModel(dept: Partial<SysDept>) {
  Object.assign(departmentModel, {
    id: '',
    parentId: undefined,
    deptName: '',
    deptCode: '',
    sortOrder: 1,
    description: '',
    ...dept,
  });
}

function onAddRoot() {
  isAdd.value = true;
  isAddChild.value = false;
  dialogTitle.value = '添加部门';
  resetModel({ parentId: '0' });
  modalDialogRef.value?.show();
}

function onAddChild(node: TreeNodeModel<SysDept>) {
  isAdd.value = true;
  isAddChild.value = true;
  dialogTitle.value = '添加子部门';
  resetModel({ parentId: node.data.id });
  modalDialogRef.value?.show();
}

function onEdit() {
  const dept = currentDept.value;
  if (!dept) {
    return;
  }
  isAdd.value = false;
  isAddChild.value = false;
  dialogTitle.value = '编辑部门';
  getDeptInfo(dept.id)
    .then((detail: SysDept) => {
      resetModel({
        id: detail.id,
        parentId: detail.parentId,
        deptName: detail.deptName,
        deptCode: detail.deptCode,
        sortOrder: detail.sortOrder ?? 1,
        description: detail.description || '',
      });
      modalDialogRef.value?.show();
    })
    .catch((err) => {
      console.log('获取部门信息失败', err);
      MessagePlugin.error('获取部门信息失败');
    });
}

async function onSubmit() {
  const validateResult = await formRef.value.validate();
  if (validateResult !== true) {
    return;
  }
  try {
    if (isAdd.value) {
      const parentId = departmentModel.parentId;
      await saveDept(departmentModel);
      await MessagePlugin.success('新增部门成功');
      if (parentId && parentId !== '0' && !expandedKeys.value.includes(parentId)) {
        expandedKeys.value = [...expandedKeys.value, parentId];
      }
    } else {
      await updateDept(departmentModel);
      await MessagePlugin.success('更新部门成功');
    }
    modalDialogRef.value?.close();
    loadData();
  } catch (err: any) {
    console.log('部门操作失败', err);
    await MessagePlugin.error(err?.message || '部门操作失败');
  }
}

function onCancel() {
  formRef.value.clearValidate();
}

function onDelete() {
  const dept = currentDept.value;
  if (!dept) {
    return;
  }
  if (dept.children && dept.children.length > 0) {
    MessagePlugin.warning('当前部门下有子部门，不能删除');
    return;
  }
  const parentId = dept.parentId;
  const confirmDia = DialogPlugin.confirm({
    header: '提示',
    body: `确定要删除部门【${dept.deptName}】吗？`,
    cancelBtn: '取消',
    confirmBtn: '删除',
    onConfirm: () => {
      deleteDept(dept.id)
        .then(() => {
          MessagePlugin.success('部门删除成功');
          loadData(parentId === '0' ? '' : parentId);
        })
        .catch((err: any) => {
          console.log('部门删除失败', err);
          MessagePlugin.error(err?.message || '部门删除失败');
        })
        .finally(() => {
          confirmDia.destroy();
        });
    },
  });
}

onMounted(() => loadData());
</script>
<style scoped lang="less">
.dept-detail {
  padding-left: 8px;
}

.dept-tree__header,
.dept-detail__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  gap: 16px;
}

/* 节点上的「新增子部门」只在悬浮时出现 */
.dept-tree :deep(.t-tree__operations) {
  visibility: hidden;
}

.dept-tree :deep(.t-tree__item:hover) .t-tree__operations {
  visibility: visible;
}

.dept-tree__title,
.dept-detail__title {
  font-weight: 600;
}
</style>
