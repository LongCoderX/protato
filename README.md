# Protato

Protato 是一个 Android 端番茄时钟应用，把「Todo -> 专注计时 -> 结束复盘 -> 完整记录」串成一条连续流程。

## 特性

- 番茄计时：支持专注、短休息、长休息三种模式。
- 自定义时长：可在应用内调整专注、短休息、长休息时长。
- Todo 绑定：可添加、完成、删除待办，并为当前番茄绑定一个任务。
- 后台提醒：专注倒计时可通过通知保持可见，番茄结束后提醒填写复盘。
- 复盘模板：可新建、编辑、删除模板，并设置默认模板。
- 模板字段：支持单选和简答字段，支持必填配置。
- 番茄记录：专注结束后填写复盘表单，沉淀为完整记录。
- 本地存储：数据保存到应用私有目录的 `protato-state.json`。

## 界面截图

截图将放在 `docs/screenshots/` 目录中。

| 专注计时 | 待办管理 | 复盘记录 |
| --- | --- | --- |
| 待补充 | 待补充 | 待补充 |

## 运行

1. 用 Android Studio 打开本目录。
2. 等待 Gradle Sync 完成。
3. 选择 Android 模拟器或真机运行 `app`。

本机如果已经配置好 JDK 和 Android SDK，也可以在项目根目录运行：

```bash
JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home ./gradlew :app:assembleDebug
```

## 发布

推送语义化版本标签会触发 GitHub Actions 构建 signed release APK 并创建 GitHub Release：

```bash
git tag v1.0.0
git push origin v1.0.0
```

发布签名和开发记录见 [DEVELOPMENT.md](DEVELOPMENT.md)。
