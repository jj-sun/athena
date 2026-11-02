# 构建验证与冒烟测试

## 1. JDK 21

`pom.xml` 目标是 Java 21，但 PATH 默认可能是 JDK 17。先确认：

```bash
mvn -version
```

如果显示的 Java 不是 21，查找 JDK 21：

```bash
ls -d /d/sdk/jdk/*21* "/c/Program Files/Java"/*21* \
      "/c/Program Files/Eclipse Adoptium"/*21* 2>/dev/null
```

本机已确认 JDK 21 位于：

```
D:\sdk\jdk\jdk-21.0.5+11
```

在**同一条命令**中设置 `JAVA_HOME` 并执行 Maven（Bash 每次调用不保留环境变量）：

```bash
export JAVA_HOME=/d/sdk/jdk/jdk-21.0.5+11
export PATH="$JAVA_HOME/bin:$PATH"
mvn -version
mvn -f /e/github/athena/athena-boot/pom.xml clean package
```

如果 Maven 对 MSYS 路径不兼容，改用 Windows 形式：

```bash
export JAVA_HOME='D:\sdk\jdk\jdk-21.0.5+11'
```

成功标准：输出 `BUILD SUCCESS`，且 `athena-boot/target/` 下生成 jar。构建超时建议 600000 ms。

`surefire` 已配置 `<skipTests>true</skipTests>`，无需额外 `-DskipTests`。如需跑测试：

```bash
mvn -f /e/github/athena/athena-boot/pom.xml test -DskipTests=false -Dtest=SomeTest
```

## 2. 前端

```bash
cd /e/github/athena/athena-vue-tdesign
npm run build
```

`npm run build` 会先执行 `vue-tsc` 类型检查，再执行 Vite 构建。成功标准：无 TypeScript 错误，生成 `dist/`。

不要执行 `npm install` 新增依赖；项目已具备所需包。

## 3. 定向 ESLint

```bash
cd /e/github/athena/athena-vue-tdesign
npx eslint src/views/<module> src/api/<module> src/types/<module> --max-warnings 0
```

如果 `src/types/<module>/index.ts` 是追加到已有文件，也要把该文件纳入检查。

## 4. 占位符与一致性检查

```bash
grep -REn "\{\{(module|moduleLabel|Entity|entity|entityKebab|entityLabel|tableName|permissionPrefix|field|Field|column|getter|label|menuIcon|menuOrder|uniqueColumn|queryColumn|dictCode|targetTable|textColumn)\}\}" <生成的后端/前端/SQL 文件>
```

必须无输出。不要用裸 `grep "{{"`：Vue 模板里的 `{{ row.name }}` 是正常插值，会误报。同时确认所有 `<GENERATE: ...>` / `<OPTIONAL: ...>` 注释已删除。

逐项核对：

- controller 的 `@SaCheckPermission` 权限码 = SQL 中 `sys_permission.perms` = 前端 `v-permission`。
- 菜单 `component` = `{{module}}/{{entityKebab}}-list`，对应文件 `src/views/{{module}}/{{entityKebab}}-list.vue`。
- 导入 DTO 的 `@ExcelProperty` 表头与前端导入弹窗说明、模板下载一致。
- 导出 DTO 字段与列表列一致，且没有敏感字段。
- `src/api` 的所有 URL 都以 `/{{module}}/{{entity}}` 开头，导入使用 `FormData` 字段 `file`。
- `athena.dict.allowed-tables` 已包含所有 `@Dict(dictTable=...)` 的表。
- 没有 `@TableLogic`、密码/角色/部门相关代码。

## 5. 手动冒烟

1. 在 MySQL `athena_boot` 执行迁移 SQL。
2. 启动后端（JDK 21）：
   ```bash
   export JAVA_HOME=/d/sdk/jdk/jdk-21.0.5+11
   export PATH="$JAVA_HOME/bin:$PATH"
   mvn -f /e/github/athena/athena-boot/pom.xml spring-boot:run
   ```
3. 打开 `http://localhost:8080/athena/doc.html`，带 `X-Access-Token` 验证：
   - `GET /{{module}}/{{entity}}/list`
   - `GET /{{module}}/{{entity}}/info/{id}`
   - `POST /save`、`PUT /update`
   - `DELETE /delete?id=`、`DELETE /deleteBatch?ids=a,b`
   - `GET /export` 返回 `.xlsx`
   - `GET /importTemplate` 返回 `.xlsx`
   - `POST /import` 成功返回条数；错误行返回 `第 N 行：...`
   - `@Dict` 字段响应包含 `{{field}}_dictText`
4. 启动前端：
   ```bash
   cd /e/github/athena/athena-vue-tdesign
   npm run dev
   ```
5. 重新登录（或刷新导航）后确认新菜单出现。
6. 测试：新增 → 编辑 → 删除 → 批量删除 → 导出 → 下载模板 → 导入。
7. 边界：
   - 导入文件中间放一条非法数据，确认整批回滚、无部分写入。
   - 导出含 `=`、`+`、`-`、`@` 开头的文本，确认前面有 `'`。
   - 无权限角色确认菜单/按钮不显示，直接调接口返回 403。
   - 唯一字段重复时提示「已存在」，而不是 500。

## 6. 常见错误

| 现象 | 原因/处理 |
|---|---|
| `invalid target release: 21` | 未设置 `JAVA_HOME` 到 JDK 21 |
| `Cannot find symbol` / Lombok 未生成 getter | 检查 `@Data`、`@EqualsAndHashCode(callSuper = true)`、JDK 21 |
| mapper XML 未加载 | 路径必须是 `modules/**/xml/*Mapper.xml`；确认 namespace 与接口全限定名一致 |
| 页面 500 / 组件找不到 | `sys_permission.component` 与 `src/views` 路径不一致 |
| 菜单不显示 | SQL 未执行、未重新登录、`hidden` 值写反、角色未授权 |
| 接口 403 | 权限码没有写入 `sys_permission` 或未分配给角色；admin 拥有 `*` 可用于排查 |
| `_dictText` 缺失 | 目标表不在 `athena.dict.allowed-tables`，或 Redis/字典数据缺失 |
| 导出下载到 JSON | 没有传 `{ responseType: 'blob' }`，或后端导出抛错被包装为 JSON |
| 导入表头错误 | `@ExcelProperty` 文案与模板不一致 |
| 导入部分成功 | 服务方法缺少 `@Transactional(rollbackFor = Exception.class)`，或边校验边保存 |
| 唯一键并发冲突 | 预检查之外仍可能冲突；捕获 `DuplicateKeyException` 转友好提示 |
| 前端类型报错 | `types` 接口少字段、`_dictText` 未声明、`res` 未标注 `PageResult` |
| ESLint 导入顺序报错 | 外部依赖 → 空行 → `@/` 按字母序 → `import type` |
