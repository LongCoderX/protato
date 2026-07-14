# Architecture & Refactor Backlog

更新：2026-07-14

重构目标：把「大文件 + 弱边界」收成可持续扩展的分层，而不是继续堆功能。
历史完成项已落地到代码；本文件只保留**目标边界**和**未完成清单**。

## 目标分层

```text
ui  →  state  →  domain
              →  data（store / codec / secrets / migration）
              →  ai
              →  platform

各层均可依赖 model；禁止反向依赖（domain↛ui、ai↛ui、data↛ui 等）。
```

| 层 | 职责 | 禁止 |
| --- | --- | --- |
| **ui** | 展示、临时 UI 状态、派发事件 | 直接 LLM/WebDAV、写复杂 AppState、排闹钟/通知 |
| **state** | 持有 AppState、事件/reducer、协调保存与副作用 | 大段 prompt、JSON 细节 |
| **domain** | 纯业务规则（可单测） | Android Context、Compose、IO、网络 |
| **ai** | Provider、prompt、intent、context、action 解析 | 直接改 AppState、无视权限裁剪 |
| **data** | 持久化、迁移、密钥 | UI、业务流程、平台副作用 |
| **platform** | Service / Alarm / Notification | Compose、LLM、业务推理 |

### Agent 管线（已有骨架）

```text
用户消息
  → IntentResolver
  → ContextBuilder（按权限裁剪）
  → PromptBuilder → LlmClient
  → ActionParser → 结构化 AgentAction
  → UI 确认 → state/domain 落地
```

原则：模型只提案，不直改状态；mutating action 必须用户确认。

### 主导航

底部 5 入口：任务 | 专注 | **对话（中心）** | 记录 | 更多
（模板、设置在「更多」）

## 已完成（摘要）

- `MainActivity` 瘦身；主题/导航/多 screen 组件拆分
- `state/`：StateHolder、事件族、各 reducer、Effects + EffectRunner
- `ai/`：LlmClient、TurnService、SessionState、Intent/Context/Action 等；关键路径有单测
- 存储：Store 外壳 + JsonCodec + Migration + loadResult；SecretStore 文件分离；备份排除敏感文件
- `sync/WebDavBackup`、`domain/timer`、`domain/todo`（排序、草案）、`domain/record`（待办匹配）已落地
- **2026-07-14 结构收口**：各 Screen 迁入 `ui/screens/*`；设置拆为 Appearance / Backup / Provider / Agent；Todo 编辑、提醒、完成复盘分离；Focus 页面、控制、背景和任务选择分离；计时/提醒 Service 进 `platform/`；CI 跑测试与 lint

## 仍偏大的文件（优先拆）

行数随代码变化，以仓库为准：

| 文件 | 方向 |
| --- | --- |
| `ui/screens/todo/TodoEditorDialogs.kt` / `TodoScreen.kt` | 继续拆主编辑与批量操作 |
| `RecordDialogs` / `AgentScreen` | 按区域继续拆展示组件 |
| `ProtatoApp.kt` | 保持为导航、状态映射和页面连接 |
| `AppStateJsonCodec.kt` | legacy 兼容逻辑抽到独立 adapter |

## 待办 backlog

### 1. State / App shell

- [x] WebDAV 备份请求与生命周期副作用从 `ProtatoApp` 迁出
- [ ] 页面一律只派发 `ProtatoEvent`（扫尾残留直写）

### 2. Domain

- [ ] `domain/todo/`：创建/编辑/完成/筛选/去重
- [ ] `domain/record/`、`domain/template/`
- [ ] 扩展 `domain/timer/`；补 `TodoParserTest`、`TimerSessionReducerTest` 等

### 3. Data / Secrets

- [ ] SecretStore 加密
- [ ] Codec 与 legacy migration 继续拆分
- [ ] 保存失败的用户可见处理

### 4. UI

- [ ] 大文件按上表拆分；设置改为二级中心
- [ ] 记录页进入模板管理的自然入口
- [ ] 根包 Screen 统一迁到 `ui/screens/*`

### 5. Agent / Memory（未开始）

- [ ] `AgentMemoryItem` + 本地确定性检索（日期/标签/标题/关键词）
- [ ] 记忆查看与删除入口
- [ ] ActionParser 收紧：拒绝越权与上下文外 target id
- embedding / 自动落地修改：非当前阶段目标

## 加功能门槛

| 想做 | 建议 |
| --- | --- |
| 纯 UI / 文案 / 空态 | 可做，小步 |
| Todo / 记录 / 模板 / 设置 / 计时小改 | 走对应 `ProtatoEvent` + reducer + holder 测试 |
| Agent 新提案类型 | 改 TurnService / Action / SessionState，再薄改 UI；必须确认后落地 |
| 自动改状态、长期记忆、复杂同步/密钥 | 先完成对应 backlog 再上 |

## 非目标（现阶段）

- JSON → Room 全量替换
- embedding
- 全 UI 视觉重做
- 账号体系 / 远端业务后端

## 验收（整体重构完成时）

- [x] `MainActivity` 仅 Activity setup
- [x] 主导航收敛为 5 入口
- [ ] `ProtatoApp` 不再承担主要业务协调
- [ ] domain 覆盖核心可测规则
- [ ] secrets 加密存储
- [ ] 新功能落到对应层，不再堆回大文件
