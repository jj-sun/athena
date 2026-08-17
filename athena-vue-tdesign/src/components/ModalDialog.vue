<template>
  <t-dialog
    v-model:visible="showModal"
    :header="title"
    class="modal-dialog-wrapper"
    :dialog-style="bodyStyle"
    cancel-btn="取消"
    confirm-btn="确定"
    @confirm="onConfirm"
    @cancel="onCancel"
  >
    <div :style="{ maxHeight: '50vh', overflow: 'auto' }">
      <slot name="content"></slot>
    </div>
  </t-dialog>
</template>
<script lang="ts">
import { computed, defineComponent, ref } from 'vue';

export default defineComponent({
  name: 'ModalDialog',
  props: {
    title: {
      type: String,
      default: '操作',
    },
    contentHeight: {
      type: String,
      default: '30vh',
    },
  },
  emits: ['confirm', 'cancel'],
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
    return {
      showModal,
      bodyStyle,
      toggle,
      show,
      close,
      onConfirm,
      onCancel,
    };
  },
});
</script>
