<template>
  <t-drawer
    v-model:visible="showModal"
    :header="title"
    :size="size"
    class="modal-dialog-wrapper"
    :dialog-style="bodyStyle"
    cancel-btn="取消"
    confirm-btn="确定"
    @confirm="onConfirm"
    @cancel="onCancel"
    @close="onClose"
  >
    <div :style="{ overflow: 'auto' }">
      <slot name="content"></slot>
    </div>
  </t-drawer>
</template>
<script lang="ts">
import { computed, defineComponent, ref } from 'vue';

export default defineComponent({
  name: 'ModalDrawer',
  props: {
    title: {
      type: String,
      default: '操作',
    },
    size: {
      type: String,
      default: 'small',
    },
  },
  emits: ['confirm', 'cancel', 'close'],
  setup(props, { emit }) {
    const showModal = ref(false);
    const bodyStyle = computed(() => ({
      width: '50%',
    }));
    function toggle() {
      showModal.value = !showModal.value;
      return Promise.resolve(showModal.value);
    }
    function show() {
      showModal.value = true;
      return Promise.resolve(true);
    }
    function close() {
      showModal.value = false;
      return Promise.resolve(false);
    }
    function onConfirm() {
      emit('confirm');
    }
    function onCancel() {
      showModal.value = false;
      emit('cancel');
    }
    function onClose() {
      showModal.value = false;
      emit('close');
    }
    return {
      showModal,
      bodyStyle,
      toggle,
      show,
      close,
      onConfirm,
      onCancel,
      onClose,
    };
  },
});
</script>
