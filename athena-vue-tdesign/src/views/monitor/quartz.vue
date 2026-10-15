<template>
  <div class="main-container">
    <table-search @search="loadData" @reset-search="resetSearch">
      <template #search-content>
        <t-col :span="3">
          <t-form-item label="任务名称" style="width: 100%">
            <t-input v-model:value="searchForm.beanName" placeholder="请输入任务名称" />
          </t-form-item>
        </t-col>
        <t-col :span="3">
          <t-form-item label="状态" style="width: 100%">
            <t-select v-model:value="searchForm.status" placeholder="请选择状态" clearable>
              <t-option label="正常" :value="0">正常</t-option>
              <t-option label="暂停" :value="1">暂停</t-option>
            </t-select>
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
        table-layout="fixed"
        :row-key="rowKey"
        @select-change="onSelectChange"
      >
        <template #status="{ row }">
          <t-tag v-if="row.status === 0" theme="success" size="small">正常</t-tag>
          <t-tag v-else theme="warning" size="small">暂停</t-tag>
        </template>
        <template #actions="{ row }">
          <t-space>
            <t-button theme="primary" ghost size="small" @click="onEdit(row)">编辑</t-button>
            <t-button theme="danger" ghost size="small" @click="onDelete(row)">删除</t-button>
            <t-button theme="primary" ghost size="small" @click="onRun(row)">立即执行</t-button>
            <t-button v-if="row.status === 0" theme="warning" ghost size="small" @click="onPause(row)"> 暂停 </t-button>
            <t-button v-else theme="success" ghost size="small" @click="onResume(row)"> 恢复 </t-button>
          </t-space>
        </template>
      </t-table>
    </t-card>

    <modal-dialog ref="modalDialogRef" :title="actionTitle" @confirm="onSubmit">
      <template #content>
        <t-form :data="formData" :label-col="{ span: 6 }">
          <t-form-item label="任务名称" name="beanName">
            <t-select
              v-model:value="formData.beanName"
              :loading="taskBeanLoading"
              placeholder="请选择任务Bean"
              filterable
              clearable
            >
              <t-option
                v-for="item in taskBeanOptions"
                :key="item.beanName"
                :value="item.beanName"
                :label="`${item.className} (${item.beanName})`"
                :disabled="item.disabled"
              />
            </t-select>
          </t-form-item>
          <t-form-item label="参数" name="parameter">
            <t-input v-model:value="formData.parameter" placeholder="请输入参数" />
          </t-form-item>
          <t-form-item label="Cron表达式" name="cronExpression">
            <t-input v-model:value="formData.cronExpression" placeholder="请输入Cron表达式" />
          </t-form-item>
          <t-form-item label="状态" name="status">
            <t-radio-group v-model:value="formData.status">
              <t-radio :value="0">正常</t-radio>
              <t-radio :value="1">暂停</t-radio>
            </t-radio-group>
          </t-form-item>
          <t-form-item label="备注" name="remark">
            <t-textarea
              v-model:value="formData.remark"
              placeholder="请输入备注"
              :auto-size="{ minRows: 2, maxRows: 4 }"
            />
          </t-form-item>
        </t-form>
      </template>
    </modal-dialog>
  </div>
</template>
<script lang="ts" setup>
import { DeleteIcon, MoreIcon, PlusIcon } from 'tdesign-icons-vue-next';
import { DialogPlugin, MessagePlugin } from 'tdesign-vue-next';
import { onMounted, reactive, ref, useTemplateRef } from 'vue';

import {
  deleteScheduleJob,
  deleteScheduleJobBatch,
  getScheduleJobInfo,
  getScheduleJobList,
  getScheduleTaskBeanList,
  pauseScheduleJob,
  resumeScheduleJob,
  runScheduleJob,
  saveScheduleJob,
  updateScheduleJob,
} from '@/api/monitor/schedule';
import ModalDialog from '@/components/ModalDialog.vue';
import TableHeader from '@/components/table/TableHeader.vue';
import TableSearch from '@/components/table/TableSearch.vue';
import { usePagination, useRowKey, useRowSelection, useTable, useTableColumn } from '@/hooks/table';
import type { SysScheduleJob, TaskBeanOption } from '@/types/monitor';

const { tableLoading, dataList, indexColumn, handleSuccess } = useTable();
const rowKey = useRowKey('id');
const pagination = usePagination(loadData);
const { selectedRowKeys, onSelectChange, onClearSelected } = useRowSelection();

const searchForm = reactive({
  beanName: '',
  status: undefined as number | undefined,
});

const modalDialogRef = useTemplateRef('modalDialogRef');
const actionTitle = ref('新增定时任务');
const isAdd = ref(true);

const taskBeanOptions = ref<TaskBeanOption[]>([]);
const taskBeanLoading = ref(false);

const formData = reactive<SysScheduleJob>({
  id: '',
  beanName: '',
  parameter: '',
  cronExpression: '',
  status: 1,
  remark: '',
});

const tableColumns = reactive(
  useTableColumn(
    [
      ...indexColumn,
      {
        title: '任务名称',
        colKey: 'beanName',
      },
      {
        title: '参数',
        colKey: 'parameter',
      },
      {
        title: 'Cron表达式',
        colKey: 'cronExpression',
      },
      {
        title: '状态',
        colKey: 'status',
        width: 80,
      },
      {
        title: '备注',
        colKey: 'remark',
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
        width: 280,
      },
    ],
    {
      align: 'center',
    },
  ),
);

function resetSearch() {
  searchForm.beanName = '';
  searchForm.status = undefined;
  loadData();
}

function loadTaskBeanOptions(excludeId?: string) {
  taskBeanLoading.value = true;
  getScheduleTaskBeanList(excludeId)
    .then((res) => {
      taskBeanOptions.value = res ?? [];
    })
    .catch((err) => {
      console.error('加载任务Bean列表失败', err);
      MessagePlugin.error('加载任务Bean列表失败');
      taskBeanOptions.value = [];
    })
    .finally(() => {
      taskBeanLoading.value = false;
    });
}

function loadData() {
  tableLoading.value = true;
  const params = {
    page: pagination.current,
    pageSize: pagination.pageSize,
    ...searchForm,
    status: searchForm.status ?? undefined,
  };
  getScheduleJobList(params)
    .then((res) => {
      handleSuccess(res.list);
      pagination.setTotalSize(res.total);
    })
    .catch((err) => {
      console.error('加载定时任务列表失败', err);
      MessagePlugin.error('加载定时任务列表失败');
    })
    .finally(() => {
      tableLoading.value = false;
    });
}

function onAdd() {
  isAdd.value = true;
  actionTitle.value = '新增定时任务';
  Object.assign(formData, {
    id: '',
    beanName: '',
    parameter: '',
    cronExpression: '',
    status: 1,
    remark: '',
  });
  loadTaskBeanOptions();
  modalDialogRef.value?.toggle();
}

function onEdit(row: SysScheduleJob) {
  isAdd.value = false;
  actionTitle.value = '编辑定时任务';
  getScheduleJobInfo(row.id)
    .then((job) => {
      Object.assign(formData, {
        id: job.id,
        beanName: job.beanName,
        parameter: job.parameter || '',
        cronExpression: job.cronExpression,
        status: job.status ?? 1,
        remark: job.remark || '',
      });
      loadTaskBeanOptions(row.id);
      modalDialogRef.value?.toggle();
    })
    .catch((err) => {
      console.log('获取定时任务信息失败', err);
      MessagePlugin.error('获取定时任务信息失败');
    });
}

async function onSubmit() {
  if (!formData.beanName) {
    void MessagePlugin.error('请选择任务Bean');
    return;
  }
  if (!formData.cronExpression) {
    void MessagePlugin.error('请输入Cron表达式');
    return;
  }

  try {
    if (isAdd.value) {
      await saveScheduleJob(formData);
      void MessagePlugin.success('新增定时任务成功');
    } else {
      await updateScheduleJob(formData);
      void MessagePlugin.success('更新定时任务成功');
    }
    modalDialogRef.value?.close();
    loadData();
  } catch (err) {
    console.log('定时任务操作失败', err);
    void MessagePlugin.error('定时任务操作失败');
  }
}

function onDelete(row: SysScheduleJob) {
  const confirmDia = DialogPlugin.confirm({
    header: '提示',
    body: '确定要删除此定时任务吗？',
    cancelBtn: '取消',
    confirmBtn: '删除',
    onConfirm: () => {
      deleteScheduleJob(row.id)
        .then(() => {
          MessagePlugin.success('定时任务删除成功');
          loadData();
        })
        .catch((err) => {
          console.log('定时任务删除失败', err);
          MessagePlugin.error('定时任务删除失败');
        })
        .finally(() => {
          confirmDia.destroy();
        });
    },
  });
}

function onBatchDelete() {
  if (selectedRowKeys.value.length === 0) {
    MessagePlugin.warning('请选择要删除的定时任务');
    return;
  }
  const confirmDia = DialogPlugin.confirm({
    header: '提示',
    body: `确定要删除选中的 ${selectedRowKeys.value.length} 个定时任务吗？`,
    cancelBtn: '取消',
    confirmBtn: '删除',
    onConfirm: () => {
      deleteScheduleJobBatch(selectedRowKeys.value as string[])
        .then(() => {
          MessagePlugin.success('定时任务批量删除成功');
          loadData();
          onClearSelected();
        })
        .catch((err) => {
          console.log('定时任务批量删除失败', err);
          MessagePlugin.error('定时任务批量删除失败');
        })
        .finally(() => {
          confirmDia.destroy();
        });
    },
  });
}
function onRun(row: SysScheduleJob) {
  runScheduleJob(row.id)
    .then(() => {
      MessagePlugin.success('执行成功');
      loadData();
    })
    .catch((err) => {
      console.log(err);
      MessagePlugin.error('执行失败');
    });
}

function onPause(row: SysScheduleJob) {
  pauseScheduleJob(row.id)
    .then(() => {
      MessagePlugin.success('已暂停');
      loadData();
    })
    .catch((err) => {
      console.log('暂停失败', err);
      MessagePlugin.error('暂停失败');
    });
}

function onResume(row: SysScheduleJob) {
  resumeScheduleJob(row.id)
    .then(() => {
      MessagePlugin.success('已恢复');
      loadData();
    })
    .catch((err) => {
      console.log('恢复失败', err);
      MessagePlugin.error('恢复失败');
    });
}

onMounted(loadData);
</script>
