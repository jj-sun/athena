<template>
  <result title="系统维护中" tip="系统维护中，请稍后再试" type="maintenance">
    <t-button theme="primary" @click="() => $router.push('/')">回到首页</t-button>
  </result>
</template>
<script setup lang="ts">
import Result from '@/components/result/index.vue';

defineOptions({
  name: 'ResultMaintenance',
});
</script>
