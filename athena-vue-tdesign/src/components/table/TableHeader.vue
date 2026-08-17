<template>
  <div class="mb-2">
    <!--  表格头-左边的按钮操作  -->
    <div class="float-left">
      <t-space>
        <slot name="action" />
      </t-space>
    </div>
    <!--  表格头-右边的按钮操作  -->
    <div v-show="showSetting" class="float-right">
      <t-space size="middle">
        <span>
          <refresh-icon style="font-size: 20px; cursor: pointer" @click="doRefresh" />
        </span>
      </t-space>
    </div>
    <div style="clear: both"></div>
    <div v-show="showMessage" class="mt-2_5 w-full">
      <t-alert theme="info">
        <template #message>
          已选择 {{ selectedRowSize }} 项
        </template>
        <template #operation>
          <span @click="clearSelected">清空</span>
        </template>
      </t-alert>
    </div>
  </div>
</template>
<script lang="ts">
import { RefreshIcon } from 'tdesign-icons-vue-next';
import { defineComponent, h } from 'vue';

import type { TablePropsType } from '@/types/components';

export default defineComponent({
  name: 'TableHeader',
  components: { RefreshIcon },
  props: {
    showMessage: {
      type: Boolean,
      default: true,
    },
    showRefresh: {
      type: Boolean,
      default: true,
    },
    showSetting: {
      type: Boolean,
      default: true,
    },
    tableColumns: {
      type: Array,
      default: [],
    },
    selectedRowSize: {
      type: Number,
      default: 0,
    },
  },
  emits: ['doRefresh', 'doTableSetting', 'clearSelected'],
  setup(props, { emit }) {
    const doRefresh = function () {
      emit('doRefresh');
    };

    const onUpdateTable = function (tableColumns: Array<TablePropsType>) {
      emit('doTableSetting', tableColumns);
    };

    const clearSelected = function () {
      emit('clearSelected');
    };

    return {
      h,
      doRefresh,
      onUpdateTable,
      clearSelected,
    };
  },
});
</script>
<style scoped lang="less">
.mb-2 {
  margin-bottom: 0.5rem;
}
.float-left {
  float: left;
}
.float-right {
  float: right;
}
.mt-2_5 {
  margin-top: 0.625rem;
}
.w-full {
  width: 100%;
}
</style>
