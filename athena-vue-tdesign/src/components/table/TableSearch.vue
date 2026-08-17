<template>
  <t-card class="mb-1">
    <t-form layout="inline">
      <t-row :gutter="[24, 24]" class="w-full">
        <slot name="search-content"></slot>

        <t-col :span="3">
          <t-space>
            <t-button theme="primary" @click="doSearch">搜索</t-button>
            <t-button theme="danger" @click="doResetSearch">重置</t-button>
            <slot name="extra"></slot>
          </t-space>
        </t-col>
      </t-row>
    </t-form>
  </t-card>
</template>
<script lang="ts">
import { defineComponent, nextTick, onMounted, ref } from 'vue';

export default defineComponent({
  name: 'TableSearch',
  emits: ['search', 'reset-search'],
  setup(props, { emit }) {
    function doSearch() {
      emit('search');
    }
    function doResetSearch() {
      emit('reset-search');
    }
    return {
      doSearch,
      doResetSearch,
    };
  },
});
</script>
<style lang="less" scoped>
.mb-1 {
  margin-bottom: 0.25rem;
}
.w-full {
  width: 100%;
}
</style>
