# Development Notes

## Release Signing

GitHub Actions 会在推送 `v*.*.*` 标签时构建 signed release APK。发布前需要在 GitHub 仓库的 Actions secrets 中配置：

- `PROTATO_KEYSTORE_BASE64`：release keystore 文件的 Base64 内容。
- `PROTATO_KEYSTORE_PASSWORD`：keystore 密码。
- `PROTATO_KEY_ALIAS`：签名 key alias。
- `PROTATO_KEY_PASSWORD`：签名 key 密码。

本地生成 `PROTATO_KEYSTORE_BASE64` 可运行：

```bash
base64 -i protato-release.jks
```

## Build Commands

Debug 构建：

```bash
JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home ./gradlew :app:assembleDebug
```

Release 构建需要提供签名环境变量：

```bash
PROTATO_KEYSTORE_PATH=/path/to/protato-release.jks \
PROTATO_KEYSTORE_PASSWORD=*** \
PROTATO_KEY_ALIAS=*** \
PROTATO_KEY_PASSWORD=*** \
JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home \
./gradlew :app:assembleRelease
```

## Project Structure

- `app/src/main/java/com/protato/app/MainActivity.kt`：Compose UI 和应用主流程。
- `app/src/main/java/com/protato/app/ProtatoModels.kt`：Todo、模板、字段、记录等数据模型。
- `app/src/main/java/com/protato/app/ProtatoStore.kt`：本地 JSON 读写。

## Future Ideas

- 用 Room 替换 JSON 文件，方便查询统计和迁移。
- 增加番茄统计页，比如每天专注分钟、任务完成率、模板回答汇总。
- 增加模板字段排序和字段编辑。
- 支持导出记录为 Markdown、CSV 或 JSON。
