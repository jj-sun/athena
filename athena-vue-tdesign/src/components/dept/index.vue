<template>
  <t-tree-select
    v-model:value="innerValue"
    :placeholder="placeholder"
    :clearable="clearable"
    :disabled="disabled"
    :filterable="filterable"
    :data="deptTree"
    :keys="{ value: 'key', label: 'title' }"
    :loading="loading"
    v-bind="restAttrs"
    @change="onChange"
  />
</template>
<script setup lang="ts">
import type { TreeSelectProps, TreeSelectValue } from 'tdesign-vue-next';
import { computed, onMounted, ref, useAttrs } from 'vue';

import { getDeptSelect } from '@/api/system/dept';
import type { SysDeptTree } from '@/types/system';

interface Props {
  modelValue?: string | undefined;
  placeholder?: string;
  clearable?: boolean;
  disabled?: boolean;
  filterable?: boolean;
  /** 是否在树根部提供“顶级部门”选项（值 0），用于新建一级部门 */
  includeRoot?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: undefined,
  placeholder: '请选择部门',
  clearable: true,
  disabled: false,
  filterable: true,
  includeRoot: false,
});

const emit = defineEmits<{
  'update:modelValue': [value: string | undefined];
  change: [value: string | undefined, context?: any];
}>();

const attrs = useAttrs();

const deptTree = ref<Array<SysDeptTree>>([]);
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
    const res = (await getDeptSelect()) ?? [];
    deptTree.value = props.includeRoot
      ? [
          {
            info: {
              id: '0',
              parentId: '0',
              deptName: '顶级部门',
              deptCode: '',
              description: '',
              sortOrder: 0,
            },
            key: '0',
            parentKey: '0',
            title: '顶级部门',
            disabled: false,
            children: res,
          },
        ]
      : res;
  } catch (error) {
    console.error('加载部门失败', error);
    deptTree.value = [];
  } finally {
    loading.value = false;
  }
}

function onChange(
  value: TreeSelectValue | undefined,
  context: TreeSelectProps['onChange'] extends (v: any, ctx: infer C) => void ? C : never,
) {
  // 部门树的 value 是字符串 id，多选/对象值不在本组件的使用范围内
  const nextValue = typeof value === 'string' ? value : undefined;
  emit('update:modelValue', nextValue);
  emit('change', nextValue, context);
}

onMounted(loadDeptTree);
</script>
<style scoped lang="less"></style>
