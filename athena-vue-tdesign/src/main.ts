import 'tdesign-vue-next/es/style/index.css';
import '@/style/index.less';
import './permission';

import TDesign from 'tdesign-vue-next';
import { createApp } from 'vue';

import App from './App.vue';
import { permission } from './directives/permission';
import router from './router';
import { store } from './store';

const app = createApp(App);

app.directive('permission', permission);
app.use(TDesign);
app.use(store);
app.use(router);
router.isReady().then(() => {
  app.mount('#app');
});
