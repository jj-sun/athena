<template>
  <div class="main-container">
    <table-search @search="onSearch" @reset-search="resetSearch">
      <template #search-content>
        <t-col :span="3">
          <t-form-item label="用户名" style="width: 100%">
            <t-input v-model:value="searchForm.username" placeholder="请输入用户名" />
          </t-form-item>
        </t-col>
        <t-col :span="3">
          <t-form-item label="操作类型" style="width: 100%">
            <t-radio-group v-model:value="searchForm.logType">
              <t-radio :value="undefined">全部</t-radio>
              <t-radio :value="0">登录日志</t-radio>
              <t-radio :value="1">操作日志</t-radio>
            </t-radio-group>
          </t-form-item>
        </t-col>
      </template>
    </table-search>
    <t-card>
      <table-header :table-columns="tableColumns" :show-message="false" @do-refresh="loadData" />
      <t-table
        :loading="tableLoading"
        :data="dataList"
        :columns="tableColumns"
        :pagination="pagination"
        :row-key="rowKey"
      >
        <template #logType="{ row }">
          <t-tag v-if="row.logType === 0" theme="success" size="small">登录日志</t-tag>
          <t-tag v-else theme="warning" size="small">操作日志</t-tag>
        </template>
        <template #time="{ row }"> {{ row.time }}ms </template>
      </t-table>
    </t-card>
  </div>
</template>
<script lang="ts" setup>
import { MessagePlugin } from 'tdesign-vue-next';
import { onMounted, reactive } from 'vue';

import { getLogList } from '@/api/system/log';
import TableHeader from '@/components/table/TableHeader.vue';
import TableSearch from '@/components/table/TableSearch.vue';
import { usePagination, useRowKey, useTable, useTableColumn } from '@/hooks/table';

const { tableLoading, dataList, indexColumn, handleSuccess } = useTable();
const rowKey = useRowKey('id');
const pagination = usePagination(loadData);

const searchForm = reactive({
  username: '',
  logType: undefined as number | undefined,
});

const tableColumns = useTableColumn(
  [
    ...indexColumn,
    {
      title: '日志类型',
      colKey: 'logType',
    },
    {
      title: '用户名',
      colKey: 'username',
    },
    {
      title: '操作',
      colKey: 'operation',
    },
    {
      title: '请求方式',
      colKey: 'method',
      width: '10%',
    },
    {
      title: '请求参数',
      colKey: 'params',
      ellipsis: true,
    },
    {
      title: '执行时长',
      colKey: 'time',
      width: '10%',
    },
    {
      title: 'IP',
      colKey: 'ip',
      width: '12%',
    },
    {
      title: '创建时间',
      colKey: 'createDate',
      width: '18%',
    },
  ],
  { align: 'center' },
);

function onSearch() {
  pagination.current = 1;
  loadData();
}

function resetSearch() {
  searchForm.username = '';
  searchForm.logType = undefined;
  pagination.current = 1;
  loadData();
}

function loadData() {
  tableLoading.value = true;
  const params = {
    page: pagination.current,
    pageSize: pagination.pageSize,
    username: '',
    logType: -1,
  };
  if (searchForm.username) {
    params.username = searchForm.username;
  }
  if (searchForm.logType !== undefined) {
    params.logType = searchForm.logType;
  }
  getLogList(params)
    .then((res) => {
      handleSuccess(res.list);
      pagination.setTotalSize(res.total);
    })
    .catch((err) => {
      console.error('加载日志列表失败', err);
      MessagePlugin.error('加载日志列表失败');
    })
    .finally(() => {
      tableLoading.value = false;
    });
}

onMounted(loadData);
</script>
