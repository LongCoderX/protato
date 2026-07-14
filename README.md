# Protato

Android 番茄时钟：把「待办 → 专注计时 → 复盘 → 记录」串成一条连续流程，并支持 AI 对话助手与 WebDAV 备份。

## 功能

- **专注计时**：专注 / 短休息 / 长休息，可自定义时长；后台通知与结束后复盘提醒
- **待办**：增删改、完成、标签与日期；可为番茄绑定任务；复杂任务支持完成复盘
- **复盘模板**：自定义字段（单选 / 简答）、必填与默认模板
- **记录**：番茄结束后沉淀复盘；支持每日总结
- **AI 对话**：配置 LLM Provider 后可规划/修改待办草案（需用户确认后才落地）
- **备份与存储**：WebDAV 单向备份；本地 JSON（`protato-state.json`），密钥独立存放

## 运行

**Android Studio**：打开本目录 → Sync → 运行 `app`。

**命令行**（需 JDK 17 + Android SDK）：

```bash
./gradlew :app:assembleDebug
# 若本机 JDK 不在默认路径：
# JAVA_HOME=/path/to/jdk-17 ./gradlew :app:assembleDebug
```

单元测试：

```bash
./gradlew :app:testDebugUnitTest
```

## 发布

推送语义化版本标签会触发 GitHub Actions，构建 signed release APK 并创建 Release：

```bash
git tag v1.0.0
git push origin v1.0.0
```

签名配置、目录结构与开发约定见 [DEVELOPMENT.md](DEVELOPMENT.md)。
架构边界与后续重构 backlog 见 [REFACTOR_PLAN.md](REFACTOR_PLAN.md)。
