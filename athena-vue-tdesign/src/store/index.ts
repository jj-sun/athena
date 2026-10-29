import type { Pinia } from 'pinia';
import { createPinia, getActivePinia } from 'pinia';
import { createPersistedState } from 'pinia-plugin-persistedstate';

// 开发期 Vite 可能以带 ?t= 的新 URL 重复执行本模块（HMR 模块副本）。
// 此时必须复用应用已安装的 Pinia，否则会创建出第二个空 store，
// 表现为热更新后页面读不到登录时写入的权限。
const activePinia = getActivePinia();
const store: Pinia = activePinia ?? createPinia();
if (!activePinia) {
  store.use(createPersistedState());
}

export { store };

export * from './modules/notification';
export * from './modules/permission';
export * from './modules/setting';
export * from './modules/tabs-router';
export * from './modules/user';

export default store;
