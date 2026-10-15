# 定时任务类名下拉选择功能实现计划

## 一、需求分析

### 1.1 核心需求
- 将前端 `quartz.vue` 中「任务名称(beanName)」的文本输入框改为 TDesign 下拉选择组件
- 后端自动扫描 `com.athena.modules.job.task` 包下的所有实现类（排除接口）
- 已被定时任务使用的 beanName 在下拉中置为禁用（不可选中）
- 编辑模式下，当前任务自身的 beanName 保持可选

### 1.2 关键发现
- `beanName` 存储的是 **Spring Bean 名称**（如 `@Component("testTask")` 中的 `testTask`），而非 Java 类名
- `ITask.java` 是公共接口，需要排除；`TestTask.java` 是实现类，需要被扫描到
- TDesign Select 的 `t-option` 支持 `disabled` 属性控制选项禁用

---

## 二、需要修改/新增的文件

### 后端 (athena-boot)
| 文件 | 操作 | 说明 |
|------|------|------|
| `modules/job/service/ScheduleJobService.java` | **修改** | 新增获取可用 Task Bean 列表的接口方法 |
| `modules/job/service/impl/ScheduleJobServiceImpl.java` | **修改** | 实现包扫描逻辑，返回带禁用状态的 Bean 列表 |
| `modules/job/controller/ScheduleJobController.java` | **修改** | 新增对外 API 接口，支持可选参数 `excludeId`（编辑时排除自身） |

### 前端 (athena-vue-tdesign)
| 文件 | 操作 | 说明 |
|------|------|------|
| `src/types/monitor/index.ts` | **修改** | 新增 TaskBean 类型定义 |
| `src/api/monitor/schedule.ts` | **修改** | 新增 `getScheduleTaskBeanList` API 方法 |
| `src/views/monitor/quartz.vue` | **修改** | 将 beanName 输入框改为 t-select 下拉，加载时获取选项列表 |

---

## 三、详细实现步骤

### 步骤 1：后端 - ScheduleJobService 接口
**文件**：`ScheduleJobService.java`
- 新增方法定义：
  ```java
  /**
   * 获取可用的定时任务 Bean 列表
   * @param excludeId 排除的任务ID（编辑时传入，自身beanName保持可选）
   */
  List<Map<String, Object>> getTaskBeanList(String excludeId);
  ```

### 步骤 2：后端 - ScheduleJobServiceImpl 实现
**文件**：`ScheduleJobServiceImpl.java`
- 实现 `getTaskBeanList` 方法，核心逻辑：
  1. **包扫描**：使用 Spring 的 `ClassPathScanningCandidateComponentProvider` + `AssignableTypeFilter` 扫描 `com.athena.modules.job.task` 包下所有实现了 `ITask` 接口的类（自动排除接口）
  2. **获取 Bean 名称**：读取类上 `@Component` 或 `@Service` 等注解的 value 值，没有则用默认类名首字母小写
  3. **查询已使用**：查询 `schedule_job` 表中所有 `beanName`（排除 `excludeId` 对应行）
  4. **构造返回**：每条记录包含 `beanName`（String，Spring Bean名）、`className`（String，完整类名或简称）、`disabled`（Boolean，是否禁用）
  5. 若 `excludeId` 不为空，该 ID 对应的 beanName 不标记为 disabled

### 步骤 3：后端 - Controller API
**文件**：`ScheduleJobController.java`
- 新增 GET 接口：`/sys/schedule/taskBeanList`
- 参数：`excludeId`（可选，String）
- 返回：`Result<List<Map<String, Object>>>` 或封装 DTO

### 步骤 4：前端 - 类型定义
**文件**：`types/monitor/index.ts`
- 新增接口：
  ```ts
  export interface TaskBeanOption {
    beanName: string;      // Spring Bean 名称（value）
    className: string;     // 类名，用于展示 label
    disabled?: boolean;    // 是否已被使用
  }
  ```

### 步骤 5：前端 - API 封装
**文件**：`api/monitor/schedule.ts`
- 新增 `taskBeanList` 接口路径
- 新增方法：
  ```ts
  export async function getScheduleTaskBeanList(excludeId?: string): Promise<Result<TaskBeanOption[]>>
  ```

### 步骤 6：前端 - quartz.vue 改造
**文件**：`views/monitor/quartz.vue`（使用 TDesign MCP 组件规范）

1. **新增变量**：
   - `taskBeanOptions: TaskBeanOption[]` 存储下拉选项
   - `taskBeanLoading: boolean` 加载状态

2. **模板替换**（L78-L80）：
   - `<t-input>` → `<t-select>`，配置：
     - `v-model:value="formData.beanName"`
     - `:options="taskBeanOptions"` + `keys="{ label: 'className', value: 'beanName' }"`（或使用 `t-option` 循环渲染以支持 `disabled`）
     - **推荐方式**：`t-option` v-for 循环，绑定 `:disabled="item.disabled"`，这样更直观
     - `placeholder="请选择任务Bean"`
     - `filterable` 支持搜索
     - `:loading="taskBeanLoading"`

3. **新增方法** `loadTaskBeanOptions(excludeId?: string)`：
   - 调用 `getScheduleTaskBeanList(excludeId)`
   - 结果赋值给 `taskBeanOptions`

4. **修改 onAdd()**：
   - 重置表单前调用 `loadTaskBeanOptions()`（不传 excludeId）

5. **修改 onEdit(row)**：
   - 加载详情时，同步调用 `loadTaskBeanOptions(row.id)`（传当前ID排除自身）

6. **修改 onSubmit() 校验**：
   - 「请输入Bean名称」→「请选择任务Bean」

---

## 四、风险与注意事项

1. **Spring Boot 4.x / Spring 6 包扫描兼容**
   - `ClassPathScanningCandidateComponentProvider` 在新版本仍可用，需注意 Jakarta 命名空间
   - 备选方案：使用 `ApplicationContext.getBeansOfType(ITask.class)` 直接获取所有实现 Bean（更简洁、更可靠，因为 ITask 实现类都标注了 @Component）
   - **推荐采用备选方案**，避免资源路径扫描问题

2. **Bean 名称获取**
   - 使用 `ApplicationContext` 获取 Bean 名称更准确（getBeanNamesForType）
   - 获取 Bean 的 Class：`applicationContext.getType(beanName)`

3. **并发禁用判断**
   - 后端接口返回时已计算 disabled，前端直接用即可
   - 编辑时必须传入 excludeId，否则当前任务的 beanName 会被禁用

4. **搜索条件栏不变**
   - 仅弹窗表单中的 beanName 改为下拉，顶部搜索栏的输入框保持不变（搜索支持模糊匹配任意输入）

---

## 五、成功验证标准

- [ ] 打开「新增定时任务」弹窗，任务名称显示为下拉框
- [ ] 下拉列表中包含 `testTask`（TestTask 的 Bean 名），label 显示类名
- [ ] 数据库中已存在的 beanName 在新增时显示为禁用态（灰色不可选）
- [ ] 编辑已有任务时，该任务自己的 beanName 可以正常选中，其它已占用的仍禁用
- [ ] 选中后提交，保存的 beanName 值正确写入 schedule_job 表
- [ ] 下拉支持搜索过滤
- [ ] 无任何编译错误或类型错误
