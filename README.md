# Protato

Protato 是一个 Android 端番茄时钟原型，核心目标是把「Todo -> 专注计时 -> 结束复盘 -> 完整记录」串成一条连续流程。

## 已实现功能

- 番茄时钟：支持专注、短休息、长休息三种模式。
- 自定义时长：专注、短休息、长休息都可以在应用内调整。
- Todo：添加、完成、删除待办，并可以给本轮番茄绑定一个待办。
- 记录模板：可新建、编辑、删除模板，可设置默认模板。
- 模板字段：支持单选和简答，支持必填字段。
- 番茄记录：专注结束后弹出模板填写表单，保存后形成完整记录。
- 本地存储：数据保存到应用私有目录的 `protato-state.json`。

## 项目结构

- `app/src/main/java/com/protato/app/MainActivity.kt`：Compose UI 和应用主流程。
- `app/src/main/java/com/protato/app/ProtatoModels.kt`：Todo、模板、字段、记录等数据模型。
- `app/src/main/java/com/protato/app/ProtatoStore.kt`：本地 JSON 读写。

## 运行方式

1. 用 Android Studio 打开本目录。
2. 等待 Gradle Sync 完成。
3. 选择 Android 模拟器或真机运行 `app`。

本仓库已包含 Gradle wrapper。本机如果已经配置好 JDK 和 Android SDK，可以在项目根目录运行：

```bash
JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home ./gradlew :app:assembleDebug
```

Debug APK 会生成在：

```text
app/build/outputs/apk/debug/app-debug.apk
```

## 后续建议

- 增加前台服务和系统通知，让计时在后台更可靠。
- 用 Room 替换 JSON 文件，方便查询统计和迁移。
- 增加番茄统计页，比如每天专注分钟、任务完成率、模板回答汇总。
- 增加模板字段排序和字段编辑。
- 支持导出记录为 Markdown、CSV 或 JSON。
