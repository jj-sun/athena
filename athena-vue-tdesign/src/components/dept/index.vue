<template>
  <t-tree-select
    v-model:value="innerValue"
    :placeholder="placeholder"
    :clearable="clearable"
    :disabled="disabled"
    :filterable="filterable"
    :data="deptTree"
    :keys="{ value: 'id', label: 'deptName' }"
    :loading="loading"
    v-bind="restAttrs"
    @change="onChange"
  />
</template>
<script setup lang="ts">
import type { TreeSelectProps } from 'tdesign-vue-next';
import { computed, onMounted, ref, useAttrs } from 'vue';

import { getDeptList } from '@/api/system/dept';
import type { SysDept } from '@/types/system';

interface Props {
  modelValue?: string | undefined;
  placeholder?: string;
  clearable?: boolean;
  disabled?: boolean;
  filterable?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: undefined,
  placeholder: '请选择部门',
  clearable: true,
  disabled: false,
  filterable: true,
});

const emit = defineEmits<{
  'update:modelValue': [value: string | undefined];
  change: [value: string | undefined, context?: any];
}>();

const attrs = useAttrs();

const deptTree = ref<Array<SysDept>>([]);
const loading = ref<boolean>(false);

const restAttrs = computed(() => {
  const {
    modelValue: _modelValue,
    placeholder: _placeholder,
    clearable: _clearable,
    disabled: _disabled,
    filterable: _filterable,
    ...rest
  } = attrs as any;
  return rest;
});

const innerValue = computed<string | undefined>({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val),
});

async function loadDeptTree() {
  loading.value = true;
  try {
    const res = await getDeptList();
    deptTree.value = res;
  } catch (error) {
    console.error('加载部门失败', error);
  } finally {
    loading.value = false;
  }
}

function onChange(
  value: string | undefined,
  context: TreeSelectProps['onChange'] extends (v: any, ctx: infer C) => void ? C : never,
) {
  emit('update:modelValue', value);
  emit('change', value, context);
}

onMounted(loadDeptTree);
</script>
<style scoped lang="less"></style>
