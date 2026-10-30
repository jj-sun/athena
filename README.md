**项目说明** 
- athena-boot是一个轻量级的，前后端分离的Java开发平台。
<br>

<br>

**技术选型：** 
- 核心框架：Spring Boot 4.1.2（Java 21）
- 安全框架：Sa-Token 1.45.0
- 视图框架：Spring Framework 7.x（Spring MVC）
- 持久层框架：MyBatis-Plus 3.5.18
- 定时器：Quartz
- 数据库连接池：Druid 
- 日志管理：SLF4J、Logback
- API 文档：nextdoc4j + springdoc
- 页面交互：Vue 3
<br> 

- athena-vue-tdesign 是一个基于 TDesign，使用 `Vue3`、`Vite`、`Pinia`、`TypeScript` 开发，可进行个性化主题配置，旨在提供项目开箱即用的、配置式的中后台项目。


### 开发

``` bash
## 进入前端目录
cd athena-vue-tdesign

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

