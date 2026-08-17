
<p style="display:flex; justify-content: center">
</p>

<p align="center">
  <a href="https://nodejs.org/en/about/releases/"><img src="https://img.shields.io/node/v/vite.svg" alt="node compatibility"></a>
  <a href="https://github.com/Tencent/tdesign-vue-next/blob/develop/LICENSE">
    <img src="https://img.shields.io/npm/l/tdesign-vue-next.svg?sanitize=true" alt="License">
  </a>
</p>


### 项目简介

athena-vue-tdeisgn 是一个基于 TDesign，使用 `Vue3`、`Vite`、`Pinia`、`TypeScript` 开发，可进行个性化主题配置，旨在提供项目开箱即用的、配置式的中后台项目。

### 特性

- 内置多种常用的中后台页面
- 完善的目录结构
- 完善的代码规范配置
- 支持深色模式
- 自定义主题颜色
- 多种空间布局
- 内置 Mock 数据方案

### 使用

> 通过 `tdesign-starter-cli` 初始化项目仓库

```bash
## 1、安装 tdesign-starter-cli
npm i tdesign-starter-cli@latest -g

## 2、创建项目
td-starter init
```

### 开发

``` bash
## 安装依赖
npm install

## 启动项目
npm run dev
```

### 构建

```bash
## 构建正式环境
npm run build

## 构建测试环境
npm run build:test
```

### 其他

```bash
## 预览构建产物
npm run preview

## 代码格式检查
npm run lint

## 代码格式检查与自动修复
npm run lint:fix

## style格式检查
npm run stylelint

## style格式检查与自动修复
npm run stylelint:fix
```

