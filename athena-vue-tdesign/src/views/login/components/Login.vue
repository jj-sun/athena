<template>
  <t-form
    ref="form"
    class="item-container"
    :class="[`login-${type}`]"
    :data="formData"
    :rules="FORM_RULES"
    label-width="0"
    @submit="onSubmit"
  >
    <template v-if="type === 'password'">
      <t-form-item name="account">
        <t-input v-model="formData.username" size="large" placeholder="请输入账号">
          <template #prefix-icon>
            <t-icon name="user" />
          </template>
        </t-input>
      </t-form-item>

      <t-form-item name="password">
        <t-input
          v-model="formData.password"
          size="large"
          :type="showPsw ? 'text' : 'password'"
          clearable
          placeholder="请输入登录密码"
        >
          <template #prefix-icon>
            <t-icon name="lock-on" />
          </template>
          <template #suffix-icon>
            <t-icon :name="showPsw ? 'browse' : 'browse-off'" @click="showPsw = !showPsw" />
          </template>
        </t-input>
      </t-form-item>

      <t-form-item name="captcha">
        <div class="captcha-container">
          <t-input v-model="formData.captcha" size="large" placeholder="请输入验证码" :maxlength="6">
            <template #prefix-icon>
              <t-icon name="secured" />
            </template>
          </t-input>
          <img
            v-if="captchaImage"
            class="captcha-image"
            :src="`data:image/png;base64,${captchaImage}`"
            alt="验证码"
            title="点击刷新验证码"
            @click="refreshCaptcha"
          />
          <t-button v-else size="large" variant="outline" :loading="captchaLoading" @click="refreshCaptcha">
            加载验证码
          </t-button>
        </div>
      </t-form-item>

      <div class="check-container remember-pwd">
        <t-checkbox>记住账号</t-checkbox>
        <span class="tip">忘记账号</span>
      </div>
    </template>

    <!-- 扫码登录 -->
    <template v-else-if="type === 'qrcode'">
      <div class="tip-container">
        <span class="tip">使用微信扫一扫登录</span>
        <span class="refresh">刷新 <t-icon name="refresh" /> </span>
      </div>
      <qrcode-vue value="" :size="160" level="H" />
    </template>

    <!-- 手机号登录 -->
    <template v-else>
      <t-form-item name="phone">
        <t-input v-model="formData.phone" size="large" placeholder="请输入手机号">
          <template #prefix-icon>
            <t-icon name="mobile" />
          </template>
        </t-input>
      </t-form-item>

      <t-form-item class="verification-code" name="verifyCode">
        <t-input v-model="formData.verifyCode" size="large" placeholder="请输入验证码" />
        <t-button size="large" variant="outline" :disabled="countDown > 0" @click="sendCode">
          {{ countDown === 0 ? '发送验证码' : `${countDown}秒后可重发` }}
        </t-button>
      </t-form-item>
    </template>

    <t-form-item v-if="type !== 'qrcode'" class="btn-container">
      <t-button block size="large" type="submit" :loading="loading"> 登录 </t-button>
    </t-form-item>

    <div class="switch-container">
      <span v-if="type !== 'password'" class="tip" @click="switchType('password')">使用账号登录</span>
      <span v-if="type !== 'qrcode'" class="tip" @click="switchType('qrcode')">使用微信扫一扫登录</span>
      <span v-if="type !== 'phone'" class="tip" @click="switchType('phone')">使用手机号登录</span>
    </div>
  </t-form>
</template>
<script setup lang="ts">
import QrcodeVue from 'qrcode.vue';
import type { FormInstanceFunctions, FormRule, SubmitContext } from 'tdesign-vue-next';
import { MessagePlugin } from 'tdesign-vue-next';
import { onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { getCaptcha, login } from '@/api/base/auth';
import { useCounter } from '@/hooks';
import { useUserStore } from '@/store';
import { randomNonce } from '@/utils/sign';

const userStore = useUserStore();

const INITIAL_DATA = {
  phone: '',
  username: '',
  password: '',
  captcha: '',
  verifyCode: '',
  checked: false,
};

const FORM_RULES: Record<string, FormRule[]> = {
  phone: [{ required: true, message: '手机号必填', type: 'error' }],
  username: [{ required: true, message: '账号必填', type: 'error' }],
  password: [{ required: true, message: '密码必填', type: 'error' }],
  captcha: [{ required: true, message: '验证码必填', type: 'error' }],
  verifyCode: [{ required: true, message: '验证码必填', type: 'error' }],
};

const type = ref('password');

const form = ref<FormInstanceFunctions>();
const formData = ref({ ...INITIAL_DATA });
const showPsw = ref(false);

/** 验证码图片（base64，不含 data URI 前缀） */
const captchaImage = ref('');
/** 本次验证码的随机标识，登录时必须原样回传 */
const checkKey = ref('');
const captchaLoading = ref(false);

const [countDown, handleCounter] = useCounter();

const switchType = (val: string) => {
  type.value = val;
};

const router = useRouter();
const route = useRoute();

/**
 * 拉取验证码。每次都必须换新的 checkKey：后端以 checkKey + 验证码 计算存储键，
 * 复用旧 checkKey 会导致校验失败。
 */
const refreshCaptcha = async () => {
  if (captchaLoading.value) return;
  captchaLoading.value = true;
  try {
    checkKey.value = randomNonce();
    captchaImage.value = await getCaptcha(checkKey.value);
    formData.value.captcha = '';
  } catch (err) {
    captchaImage.value = '';
    console.log('验证码加载失败', err);
  } finally {
    captchaLoading.value = false;
  }
};

onMounted(() => {
  void refreshCaptcha();
});

/**
 * 发送验证码
 */
const sendCode = () => {
  form.value.validate({ fields: ['phone'] }).then((e) => {
    if (e === true) {
      handleCounter();
    }
  });
};

const loading = ref(false);

const onSubmit = (ctx: SubmitContext) => {
  if (ctx.validateResult === true) {
    if (loading.value) return;
    loading.value = true;
    login(formData.value.username, formData.value.password, formData.value.captcha, checkKey.value)
      .then((loginRes) => {
        userStore.saveUser(loginRes);
        MessagePlugin.success('登录成功');
        const redirect = route.query.redirect as string;
        const redirectUrl = redirect || '/';
        router.replace(redirectUrl);
      })
      .catch((err) => {
        console.log('登录失败', err);
        MessagePlugin.error(err?.message || '登录失败');
        // 验证码为一次性凭证，后端无论校验成功与否都会消费掉，失败后必须重新获取
        void refreshCaptcha();
      })
      .finally(() => {
        loading.value = false;
      });
  }
};
</script>
<style lang="less" scoped>
@import '../index.less';

.captcha-container {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;

  .t-input {
    flex: 1;
  }

  .captcha-image {
    width: 120px;
    height: 40px;
    cursor: pointer;
    border-radius: 3px;
    border: 1px solid var(--td-component-border);
    object-fit: cover;
  }
}
</style>
