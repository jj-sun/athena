<template>
  <div>
    <table-search
      v-show="getSlotKeys.includes('table-search')"
      @search="emit('search')"
      @reset-search="emit('reset-search')"
    >
      <template #search-content>
        <slot name="table-search" />
      </template>
      <template #extra>
        <slot name="table-search-extra" />
      </template>
    </table-search>

    <t-card>
      <table-header
        :selected-row-size="selectedRowKeys.length"
        :table-columns="tableColumns"
        :show-setting="showSetting"
        :show-message="showMessage"
        @do-refresh="emit('doRefresh')"
        @clear-selected="emit('clearSelected')"
        @do-table-setting="onUpdateTableColumns"
      >
        <template #action>
          <slot name="table-header" />
        </template>
      </table-header>

      <slot name="table-body" />
    </t-card>
  </div>
</template>
<script setup lang="ts">
import { computed, unref } from 'vue';

import TableHeader from '@/components/table/TableHeader.vue';
import TableSearch from '@/components/table/TableSearch.vue';
import type { TablePropsType } from '@/types/components';

const props = defineProps({
  showMessage: {
    type: Boolean,
    default: true,
  },
  showSetting: {
    type: Boolean,
    default: true,
  },
  tableColumns: {
    type: Array,
    default: () => [],
  },
  selectedRowKeys: {
    type: Array,
    default: () => [],
  },
});

const emit = defineEmits(['search', 'reset-search', 'doRefresh', 'doTableSetting', 'clearSelected']);

const slots = defineSlots();

const getSlotKeys = computed(() => {
  return Object.keys(slots);
});

const onUpdateTableColumns = function (tableColumns: Array<TablePropsType>) {
  emit('doTableSetting', tableColumns);
};
</script>
<style scoped lang="less"></style>
