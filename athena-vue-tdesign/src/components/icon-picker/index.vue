<template>
  <t-popup
    v-model:visible="panelVisible"
    trigger="click"
    placement="bottom-left"
    :overlay-inner-style="{ padding: '8px' }"
    overlay-inner-class-name="icon-picker-popup"
  >
    <t-input
      :model-value="modelValue"
      :placeholder="placeholder"
      :disabled="disabled"
      readonly
      class="icon-picker-input"
    >
      <template #prefix-icon>
        <t-icon v-if="modelValue" :name="modelValue" />
      </template>
      <template #suffix>
        <t-icon
          v-if="modelValue && !disabled"
          class="icon-picker-clear"
          name="close-circle-filled"
          @click.stop="clearIcon"
        />
      </template>
    </t-input>

    <template #content>
      <div class="icon-picker-panel">
        <t-input v-model:value="keyword" placeholder="搜索图标名称" size="small" clearable />
        <div class="icon-picker-filters">
          <t-radio-group v-model:value="styleFilter" theme="button" variant="default-filled" size="small">
            <t-radio value="all">全部</t-radio>
            <t-radio value="outline">描边</t-radio>
            <t-radio value="filled">填充</t-radio>
          </t-radio-group>
          <t-select
            v-model:value="categoryFilter"
            :options="ICON_CATEGORY_OPTIONS"
            size="small"
            class="icon-picker-category"
          />
        </div>
        <div class="icon-picker-grid">
          <button
            v-for="item in pageIcons"
            :key="item.stem"
            type="button"
            class="icon-picker-item"
            :class="{ 'is-active': item.stem === modelValue }"
            :title="item.stem"
            @click="selectIcon(item.stem)"
          >
            <t-icon :name="item.stem" size="22px" />
            <span class="icon-picker-name">{{ item.stem }}</span>
          </button>
        </div>
        <div v-if="pageIcons.length === 0" class="icon-picker-empty">未找到匹配的图标</div>
        <div class="icon-picker-footer">
          <span>共 {{ filteredIcons.length }} 个</span>
          <t-space size="small" align="center">
            <t-button size="small" variant="text" :disabled="page <= 1" @click="page -= 1">上一页</t-button>
            <span class="icon-picker-page">{{ page }} / {{ pageCount }}</span>
            <t-button size="small" variant="text" :disabled="page >= pageCount" @click="page += 1">下一页</t-button>
          </t-space>
        </div>
      </div>
    </template>
  </t-popup>
</template>
<script lang="ts" setup>
import { manifest } from 'tdesign-icons-vue-next';
import { computed, ref, watch } from 'vue';

import { getIconCategory, ICON_CATEGORY_OPTIONS, isFilledIcon } from './categories';

interface Props {
  /** 当前选中的图标名称，对应 t-icon 的 name */
  modelValue?: string;
  placeholder?: string;
  disabled?: boolean;
}

withDefaults(defineProps<Props>(), {
  modelValue: '',
  placeholder: '请选择图标',
  disabled: false,
});

const emit = defineEmits<{
  'update:modelValue': [value: string];
  change: [value: string];
}>();

const PAGE_SIZE = 120;

/** 预计算风格与分类，避免每次筛选都对 2000+ 图标做规则匹配 */
const ICON_LIST = manifest.map(({ stem }) => ({
  stem,
  filled: isFilledIcon(stem),
  category: getIconCategory(stem),
}));

type IconStyle = 'all' | 'outline' | 'filled';

const panelVisible = ref(false);
const keyword = ref('');
const styleFilter = ref<IconStyle>('all');
const categoryFilter = ref('all');
const page = ref(1);

const filteredIcons = computed(() => {
  const kw = keyword.value.trim().toLowerCase();
  return ICON_LIST.filter((item) => {
    if (kw && !item.stem.includes(kw)) {
      return false;
    }
    if (styleFilter.value === 'filled' && !item.filled) {
      return false;
    }
    if (styleFilter.value === 'outline' && item.filled) {
      return false;
    }
    if (categoryFilter.value !== 'all' && item.category !== categoryFilter.value) {
      return false;
    }
    return true;
  });
});

const pageCount = computed(() => Math.max(1, Math.ceil(filteredIcons.value.length / PAGE_SIZE)));

const pageIcons = computed(() => {
  const start = (page.value - 1) * PAGE_SIZE;
  return filteredIcons.value.slice(start, start + PAGE_SIZE);
});

watch([keyword, styleFilter, categoryFilter], () => {
  page.value = 1;
});

// 每次打开面板都从完整列表、第一页开始
watch(panelVisible, (visible) => {
  if (visible) {
    keyword.value = '';
    styleFilter.value = 'all';
    categoryFilter.value = 'all';
    page.value = 1;
  }
});

function selectIcon(stem: string) {
  emit('update:modelValue', stem);
  emit('change', stem);
  panelVisible.value = false;
}

function clearIcon() {
  emit('update:modelValue', '');
  emit('change', '');
  panelVisible.value = false;
}
</script>
<style scoped>
.icon-picker-input {
  cursor: pointer;
}

.icon-picker-clear {
  color: var(--td-text-color-placeholder);
  cursor: pointer;
}

.icon-picker-clear:hover {
  color: var(--td-text-color-secondary);
}

.icon-picker-panel {
  width: 420px;
}

.icon-picker-filters {
  display: flex;
  gap: 8px;
  align-items: center;
  margin-top: 8px;
}

.icon-picker-category {
  flex: 1;
  min-width: 0;
}

.icon-picker-grid {
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 6px;
  max-height: 300px;
  padding: 8px 0;
  overflow-y: auto;
}

.icon-picker-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
  align-items: center;
  justify-content: center;
  height: 62px;
  padding: 4px 2px;
  cursor: pointer;
  background: transparent;
  border: 1px solid transparent;
  border-radius: 4px;
}

.icon-picker-item:hover {
  background: var(--td-bg-color-container-hover);
  border-color: var(--td-brand-color-light);
}

.icon-picker-item.is-active {
  background: var(--td-brand-color-light);
  border-color: var(--td-brand-color);
}

.icon-picker-name {
  width: 100%;
  overflow: hidden;
  font-size: 11px;
  color: var(--td-text-color-secondary);
  text-overflow: ellipsis;
  white-space: nowrap;
}

.icon-picker-empty {
  padding: 24px 0;
  font-size: 13px;
  color: var(--td-text-color-placeholder);
  text-align: center;
}

.icon-picker-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 6px;
  font-size: 12px;
  color: var(--td-text-color-secondary);
  border-top: 1px solid var(--td-component-stroke);
}

.icon-picker-page {
  min-width: 48px;
  text-align: center;
}
</style>
