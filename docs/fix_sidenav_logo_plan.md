# 修复 SideNav Logo 区域样式问题

## 问题

SideNav.vue 的 logo 区域原本使用 SVG 组件（通过 `getLogo()` 根据折叠状态切换），被改成了 `<img>` + 文字的形式，导致样式异常：
- 同时显示图片和文字，布局不协调
- 折叠状态下没有适配（原本会切换为紧凑版 logo）
- 缺少 `showLogo` 条件控制

## 修改文件

### `src/layouts/components/SideNav.vue`

#### Logo 模板（第 12-20 行）
将当前的：
```html
<template #logo>
  <img height="28" src="@/assets/logo.png" alt="logo" />
  <div v-if="showLogo" :class="..." @click="goHome">
    <span :class="logoCls">athena-vue</span>
  </div>
</template>
```

恢复为使用 `getLogo()` 组件的方式：
```html
<template #logo>
  <span v-if="showLogo" :class="`${prefix}-side-nav-logo-wrapper`" @click="goHome">
    <component :is="getLogo()" :class="logoCls" />
  </span>
</template>
```

#### 说明
- `getLogo()` 函数已存在（第 165-168 行），会根据 `collapsed` 状态返回 `AssetLogo`（紧凑）或 `AssetLogoFull`（完整）
- 两个 SVG 组件已在第 36-37 行正确导入
- 该方式原生支持折叠状态的 logo 切换
- 移除多余的 `<img>` 标签和硬编码文字
