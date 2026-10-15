<template>
  <t-config-provider :global-config="globalConfig">
    <router-view :class="[mode]" />
  </t-config-provider>
</template>
<script setup lang="ts">
import type { GlobalConfigProvider } from 'tdesign-vue-next';
import zhConfig from 'tdesign-vue-next/es/locale/zh_CN';
import { computed } from 'vue';

import { useSettingStore } from '@/store';

const store = useSettingStore();

const mode = computed(() => {
  return store.displayMode;
});

const globalConfig = zhConfig as unknown as GlobalConfigProvider;
</script>
<style lang="less" scoped>
#nprogress .bar {
  background: var(--td-brand-color) !important;
}
</style>
