import type { Directive, DirectiveBinding } from 'vue';

import { getPermissionStore } from '@/store';

type PermissionValue = string | string[];

/**
 * 按钮级权限指令。
 *
 * 用法：
 *   v-permission="'sys:user:save'"                      // 需要该权限
 *   v-permission="['sys:role:info', 'sys:role:update']" // 需要同时具备
 *
 * 无权限时直接移除元素；权限列表来自登录后 `/sys/permission/nav` 返回的 permissions。
 * 注意：TDesign 的 `t-space` 会给每个子元素套一层 `.t-space-item`，只删按钮会留下空容器，
 * 导致剩余按钮之间多出一段间距，因此命中该容器时需要连容器一起移除。
 */
function hasPermission(value: PermissionValue | undefined): boolean {
  if (!value) {
    return true;
  }
  const required = Array.isArray(value) ? value : [value];
  if (required.length === 0) {
    return true;
  }
  const owned = getPermissionStore().permissions;
  return required.every((code) => owned.includes(code));
}

function apply(el: HTMLElement, binding: DirectiveBinding<PermissionValue>) {
  if (hasPermission(binding.value)) {
    return;
  }
  // 按钮/下拉/提示框位于 t-space 中时，Space 会为每个子节点生成 .t-space-item 容器。
  // 只删除内部元素会留下空容器，导致剩余按钮间距不一致；这里直接移除整个 space item。
  const spaceItem = el.closest('.t-space-item');
  if (spaceItem?.parentElement) {
    spaceItem.parentElement.removeChild(spaceItem);
    return;
  }
  el.parentElement?.removeChild(el);
}

export const permission: Directive<HTMLElement, PermissionValue> = {
  mounted: apply,
};
