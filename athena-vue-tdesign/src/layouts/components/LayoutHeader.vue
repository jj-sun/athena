<template>
  <l-header
    v-if="settingStore.showHeader"
    :show-logo="settingStore.showHeaderLogo"
    :theme="settingStore.displayMode"
    :layout="settingStore.layout"
    :is-fixed="settingStore.isHeaderFixed"
    :menu="headerMenu"
    :is-compact="settingStore.isSidebarCompact"
  />
</template>
<script setup lang="ts">
import { storeToRefs } from 'pinia';
import { computed } from 'vue';

import { usePermissionStore, useSettingStore } from '@/store';
import type { MenuRoute } from '@/types/interface';

import LHeader from './Header.vue';

const permissionStore = usePermissionStore();
const settingStore = useSettingStore();
const { routers: menuRouters } = storeToRefs(permissionStore);
const headerMenu = computed(() => {
  let routes = menuRouters.value;
  if (settingStore.layout === 'mix') {
    routes = settingStore.splitMenu
      ? menuRouters.value.map((menu) => ({
          ...menu,
          children: [] as never[],
        }))
      : [];
  }
  // Header/MenuContent 只消费 path/title/icon，RouteRecordName 的 symbol 分支不会用到
  return routes as unknown as MenuRoute[];
});
</script>
