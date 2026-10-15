<template>
  <result title="网络异常" tip="网络异常, 请稍后重试" type="wifi">
    <div>
      <t-button theme="default" @click="() => $router.push('/')">回到首页</t-button>
      <t-button @click="() => $router.push('/')">重新加载</t-button>
    </div>
  </result>
</template>
<script setup lang="ts">
import Result from '@/components/result/index.vue';

defineOptions({
  name: 'ResultNetworkError',
});
</script>
