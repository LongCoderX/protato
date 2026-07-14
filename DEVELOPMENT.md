# Development

## 构建

```bash
# Debug
./gradlew :app:assembleDebug

# 单元测试
./gradlew :app:testDebugUnitTest

# Release（需签名环境变量）
PROTATO_KEYSTORE_PATH=/path/to/protato-release.jks \
PROTATO_KEYSTORE_PASSWORD=*** \
PROTATO_KEY_ALIAS=*** \
PROTATO_KEY_PASSWORD=*** \
./gradlew :app:assembleRelease
```

若本机 `java` 不是 17，可临时指定：

```bash
JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home ./gradlew :app:assembleDebug
```

## 发布签名（GitHub Actions）

推送 `v*.*.*` 标签时构建 signed release。仓库 Secrets：

| Secret | 说明 |
| --- | --- |
| `PROTATO_KEYSTORE_BASE64` | release keystore 的 Base64 |
| `PROTATO_KEYSTORE_PASSWORD` | keystore 密码 |
| `PROTATO_KEY_ALIAS` | key alias |
| `PROTATO_KEY_PASSWORD` | key 密码 |

生成 Base64：

```bash
base64 -i protato-release.jks
```

## 目录结构

源码根：`app/src/main/java/com/protato/app/`

```text
MainActivity.kt / ProtatoApp.kt   # 入口与导航 glue
ProtatoModels.kt                  # AppState 与领域模型
ProtatoStore.kt / *Codec* / SecretStore.kt

state/       # 事件、reducer、StateHolder、Effects
ai/          # LLM 与 Agent 管线
domain/      # 纯业务（timer、todo、record…）
ui/          # 主题、导航、components
ui/screens/  # agent / focus / todo / records / settings / templates / more
platform/    # 计时 Service、闹钟、通知、EffectRunner
sync/        # WebDAV 单向备份
util/        # 日期、ID 等
```

架构边界与后续 backlog 见 [REFACTOR_PLAN.md](REFACTOR_PLAN.md)。

## 状态流（简要）

```text
UI 发 ProtatoEvent
  → ProtatoStateHolder / reducer 更新 AppState
  → 返回 ProtatoEffect（提醒、计时 service、闹钟等）
  → platform/ProtatoEffectRunner 执行
  → ProtatoStore 持久化
```

- UI 只展示状态并派发事件，不直接写整份 `AppState` 的复杂 mutation。
- Agent 只产生结构化动作草案，用户确认后才经 state 落地。
- 密钥不进主状态 JSON；备份规则排除状态、加密密钥和旧明文密钥文件。

## 本地数据

| 文件 | 内容 |
| --- | --- |
| `protato-state.json` | 应用状态（脱敏后） |
| `protato-secrets.enc` | Android Keystore 加密后的 LLM / WebDAV 密钥 |
| `protato-secrets.json` | 仅用于旧版本明文迁移，迁移成功后删除 |

## 可选后续（产品）

- Room 替换 JSON，便于统计查询
- 专注统计页
- 记录导出（Markdown / CSV / JSON）
