# RainClassByeBye-Android

> **Tips**:目前这个apk没有经过测试（因为我没有考试可以测试了），所以请谨慎使用，如有问题欢迎提交issue。

RainClassByeBye-Android 是 RainClassByeBye 的 Android 客户端实现，基于 Kotlin 与 Jetpack Compose 构建。项目将原本偏命令行的雨课堂辅助流程迁移到移动端，提供登录、课程查看、任务查看、自动执行、状态恢复与 AI 配置等能力。



本项目构建在 [Auto-CQUPT-Plan/RainClassSDK](https://github.com/Auto-CQUPT-Plan/RainClassSDK) 之上：`RainClassSDK` 负责雨课堂登录和接口调用，`RainClassByeBye` 在其上补齐交互、LLM 求解、状态持久化、断点恢复和交卷流程。

## 功能概览

- 微信扫码登录与本地登录态保存。
- 首页展示当前用户信息与核心功能入口。
- 课程列表查看。
- 课程任务与作业信息查看。
- 自动执行任务，并展示实时进度与日志。
- 执行状态持久化，支持中断后继续处理。
- 设置页支持 AI 服务配置、模型获取、并发数量与自动交卷选项。
- 支持 GitHub Actions 云端构建 Release APK。

## 技术栈

| 类型 | 技术 |
| --- | --- |
| 语言 | Kotlin |
| UI | Jetpack Compose、Material 3 |
| 导航 | Jetpack Navigation 3 |
| 架构 | 多模块、MVVM、Repository |
| 依赖注入 | Koin |
| 网络 | Retrofit、OkHttp |
| 序列化 | Kotlinx Serialization |
| 本地配置 | DataStore Preferences |
| 本地数据库 | Room |
| 异步 | Kotlin Coroutines、StateFlow |
| 构建 | Gradle、Android Gradle Plugin |

## 项目结构

```text
.
├── app
│   └── 应用入口、依赖注入、全局导航、发布配置
├── core
│   ├── config
│   ├── database
│   ├── navigation3
│   └── network
├── feature
│   ├── login
│   ├── home
│   ├── courses
│   ├── homework
│   ├── exam
│   └── settings
├── build-logic
│   └── Gradle 约定插件
├── gradle
│   └── 版本目录与 wrapper
└── .github
    └── GitHub Actions 工作流
```

## 架构说明

项目整体分为三层模块：

```text
app
  ↓
feature/*
  ↓
core/*
```

### app

`app` 模块负责应用级组装：

- 初始化 Koin。
- 创建主 Activity。
- 判断启动页。
- 注册 Navigation 3 页面。
- 聚合所有 core 与 feature 模块。
- 配置 Release 版本号与签名。

业务逻辑不放在 `app` 中，`app` 只做入口和装配。

### core

`core` 模块放置跨业务复用的基础能力。

| 模块 | 说明 |
| --- | --- |
| `core:config` | 全局设置、DataStore、通用 UI、主题与基础模型 |
| `core:database` | Room 数据库与任务状态持久化 |
| `core:navigation3` | Navigation 3 的项目级封装 |
| `core:network` | 网络基础设施、Cookie、拦截器与通用 AI 客户端 |

`core` 不依赖任何 feature，保证基础设施层稳定、可复用。

### feature

`feature` 按业务功能拆分，每个功能模块独立维护自己的 UI、ViewModel、Repository、数据模型和网络声明。

| 模块 | 说明 |
| --- | --- |
| `feature:login` | 登录流程 |
| `feature:home` | 首页与用户信息 |
| `feature:courses` | 课程列表 |
| `feature:homework` | 任务列表与任务详情 |
| `feature:exam` | 自动执行、进度日志、状态恢复 |
| `feature:settings` | 应用设置与 AI 配置 |

推荐的 feature 分包方式：

```text
feature/<name>
└── src/main/kotlin/com/rainclass/feature/<name>
    ├── model
    │   ├── api
    │   ├── bean
    │   └── repository
    ├── ui
    └── viewmodel
```

## 核心设计

### MVVM 与单向状态

每个页面由 ViewModel 暴露 `StateFlow` 状态，Compose 页面只负责渲染状态和上报用户事件。这样可以让加载、错误、空状态和成功状态都集中在 ViewModel 中管理。

```text
UI
  -> ViewModel
  -> Repository
  -> API / Database / DataStore
```

### Repository 层

Repository 是业务数据入口，负责屏蔽底层网络、数据库或配置读取细节。UI 和 ViewModel 不直接关心请求如何发起，也不直接处理底层响应结构。

### Navigation 3 封装

项目在 `core:navigation3` 中封装了统一的导航能力：

- `RainRoute`：页面路由基类。
- `RainNavHost`：项目级 Navigation 3 容器。
- `rainEntry`：页面注册辅助方法。
- `RainNavigator`：统一处理入栈、弹栈、替换栈和返回结果。

新增页面时，只需要定义可序列化 route，并在 `AppNavHost` 中注册对应 entry。

### 配置管理

用户设置通过 DataStore 保存，主要包括：

- AI 服务格式。
- API Key。
- Base URL。
- 模型名称。
- 温度与输出长度。
- 请求超时。
- 并发数量。
- 是否自动交卷。

这些配置统一收敛在 `core:config`，业务模块只读取结构化后的设置对象。

### 状态持久化

执行状态通过 Room 保存，支持：

- 已完成记录。
- 失败记录。
- 当前进度。
- 最后错误。
- 中断后恢复。

这样即使应用退出或执行中断，也能在状态页继续处理。

### AI 适配

AI 调用被封装为统一客户端，对上层暴露一致的消息结构。设置页负责维护用户选择的服务格式和模型配置，业务层不直接绑定某一个 AI 厂商。

当前支持的配置类型包括：

- OpenAI Chat Completions。
- OpenAI Responses。
- Anthropic Messages。
- Gemini。

## 开发环境

建议环境：

- Android Studio 最新稳定版。
- JDK 21。
- Android SDK。
- Gradle Wrapper 使用仓库内置版本。

项目使用版本目录：

```text
gradle/libs.versions.toml
```

依赖版本请优先在该文件中统一维护。

## 本地运行

克隆项目后进入 Android 项目目录：

```bash
cd RainClassByeBye-Android
```

构建 Debug APK：

```bash
./gradlew :app:assembleDebug
```

安装到已连接设备：

```bash
./gradlew :app:installDebug
```

构建 Release APK：

```bash
./gradlew :app:assembleRelease
```

如果没有提供 Release 签名环境变量，Release 构建不会使用正式签名配置。

## Release 签名配置

Release 签名通过环境变量注入：

| 环境变量 | 说明 |
| --- | --- |
| `VERSION_CODE` | 应用 versionCode |
| `VERSION_NAME` | 应用 versionName |
| `ANDROID_KEYSTORE_PATH` | keystore 文件路径 |
| `ANDROID_KEYSTORE_PASSWORD` | keystore 密码 |
| `ANDROID_KEY_ALIAS` | key alias |
| `ANDROID_KEY_PASSWORD` | key 密码 |

本地构建示例：

```bash
export VERSION_CODE=1
export VERSION_NAME=1.0.0
export ANDROID_KEYSTORE_PATH=/absolute/path/release.jks
export ANDROID_KEYSTORE_PASSWORD=your_store_password
export ANDROID_KEY_ALIAS=key0
export ANDROID_KEY_PASSWORD=your_key_password

./gradlew :app:assembleRelease
```

## GitHub Actions 发版

仓库内置 Release 工作流：

```text
.github/workflows/android-release.yml
```

触发方式：

- 推送 `v*` tag。
- 在 GitHub Actions 页面手动运行 workflow。

发版前需要在 GitHub Secrets 中配置：

| Secret | 说明 |
| --- | --- |
| `ANDROID_KEYSTORE_BASE64` | keystore 文件的 Base64 内容 |
| `ANDROID_KEYSTORE_PASSWORD` | keystore 密码 |
| `ANDROID_KEY_ALIAS` | key alias |
| `ANDROID_KEY_PASSWORD` | key 密码 |

推荐发版流程：

```bash
git tag v1.0.0
git push origin v1.0.0
```

工作流会自动构建 Release APK，并上传到 GitHub Release。

## 免责声明

本项目仅供学习、研究和接口分析使用。

使用者需要自行承担以下责任：

- 确认自己的使用行为符合学校规定、课程平台规则及当地法律法规。
- 自行评估账号风险、成绩风险、封禁风险和其他衍生后果。
- 不将本项目用于任何未经授权的考试、测验或其他违规场景。

作者和贡献者不对因使用、滥用或误用本项目造成的任何直接或间接损失负责。

另需注意：

- 项目默认支持自动提交单题答案。
- 但是自动交卷默认是关闭的，可以在设置里打开。
- 雨课堂平台可能存在时间限制、风控策略或接口变更，项目并不保证始终可用。

## License

本项目遵循仓库中的 [LICENSE.txt](LICENSE.txt)。

