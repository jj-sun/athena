import Cookies from 'js-cookie';
import { defineStore } from 'pinia';

import Avatar from '@/assets/img_avatar.gif';
import { usePermissionStore } from '@/store';
import type { LoginInfo } from '@/types/base';
import type { UserState } from '@/types/interface';

const InitUserInfo: UserState = {
  userId: '0',
  avatar: '',
  roleId: '0',
  roles: [],
  username: '',
  realname: '',
  token: '',
  info: {},
  permissionList: [],
};
const defaultAvatar = Avatar;
const USER_INFO_KEY = 'a-user-info';
const USER_TOKEN_KEY = 'X-Access-Token';
const userState: UserState = JSON.parse(localStorage.getItem(USER_INFO_KEY) || '{}');

export const useUserStore = defineStore('user', {
  state: (): UserState => {
    return {
      token: Cookies.get(USER_TOKEN_KEY),
      userId: userState.userId || '0',
      roleId: userState.roleId || '0',
      username: userState.username || '',
      realname: userState.realname || '',
      avatar: userState.avatar || defaultAvatar,
      info: userState.info || {},
      permissionList: userState.permissionList || [],
      roles: userState.roles || [],
    };
  },
  getters: {
    roles: (state) => {
      return state?.roles;
    },
    userInfo(state: UserState): UserState {
      return { ...state };
    },
  },
  actions: {
    setInfo(partial: Partial<UserState>) {
      this.$patch(partial);
    },
    saveUser(result: LoginInfo) {
      const { token, userInfo } = result;
      this.userId = userInfo.userId;
      this.token = token;
      this.roleId = userInfo.roleId;
      this.username = userInfo.username;
      this.realname = userInfo.realname;
      this.avatar = userInfo.avatar || defaultAvatar;

      Cookies.set(USER_TOKEN_KEY, token, { expires: 1 });

      localStorage.setItem(USER_INFO_KEY, JSON.stringify(userInfo));
    },
    logout() {
      return new Promise<void>((resolve) => {
        this.setInfo(InitUserInfo);
        Cookies.remove(USER_TOKEN_KEY);
        localStorage.clear();
        resolve();
      });
    },
  },
  persist: {
    afterRestore: () => {
      const permissionStore = usePermissionStore();
      permissionStore.initRoutes();
    },
    key: 'user',
    paths: ['token'],
  },
});
