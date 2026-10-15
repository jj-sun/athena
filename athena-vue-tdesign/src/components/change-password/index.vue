<template>
  <t-dialog
    :visible="visible"
    header="修改密码"
    :confirm-loading="loading"
    :close-on-overlay-click="false"
    @confirm="onSubmit"
    @cancel="onCancel"
    @close="onClose"
  >
    <t-form ref="formRef" :data="formData" :rules="rules" :label-col="{ span: 5 }">
      <t-form-item label="原密码" name="oldPassword">
        <t-input v-model:value="formData.oldPassword" type="password" placeholder="请输入原密码" clearable />
      </t-form-item>
      <t-form-item label="新密码" name="newPassword">
        <t-input v-model:value="formData.newPassword" type="password" placeholder="请输入新密码" clearable />
      </t-form-item>
      <t-form-item label="确认新密码" name="confirmPassword">
        <t-input v-model:value="formData.confirmPassword" type="password" placeholder="请再次输入新密码" clearable />
      </t-form-item>
    </t-form>
  </t-dialog>
</template>
<script setup lang="ts">
import type { FormInstanceFunctions, FormProps } from 'tdesign-vue-next';
import { MessagePlugin } from 'tdesign-vue-next';
import { reactive, ref } from 'vue';

import { changePassword } from '@/api/system/user';
import { passwordValidator } from '@/utils/validate';

defineProps<{
  visible: boolean;
}>();

const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void;
}>();

const formRef = ref<FormInstanceFunctions>();
const loading = ref(false);
const formData = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
});

const rules: FormProps['rules'] = {
  oldPassword: [{ required: true, message: '请输入原密码', type: 'error' }],
  newPassword: [{ validator: passwordValidator, trigger: 'blur' }],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', type: 'error' },
    {
      validator: (val: string) => {
        if (val !== formData.newPassword) {
          return { result: false, message: '两次输入的新密码不一致', type: 'error' };
        }
        return true;
      },
      trigger: 'blur',
    },
  ],
};

function reset() {
  formData.oldPassword = '';
  formData.newPassword = '';
  formData.confirmPassword = '';
  formRef.value?.clearValidate();
}

async function onSubmit() {
  const validateResult = await formRef.value?.validate();
  if (validateResult !== true) return;

  loading.value = true;
  changePassword(formData.oldPassword, formData.newPassword)
    .then(() => {
      MessagePlugin.success('密码修改成功');
      emit('update:visible', false);
      reset();
    })
    .catch((err) => {
      console.log('修改密码失败', err);
      MessagePlugin.error(err?.message || '密码修改失败');
    })
    .finally(() => {
      loading.value = false;
    });
}

function onCancel() {
  emit('update:visible', false);
  reset();
}

function onClose() {
  reset();
}
</script>
