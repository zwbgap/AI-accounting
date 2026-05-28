# AI记账

一款基于 Jetpack Compose 和 AI 能力的智能记账 Android 应用，支持拍照识别票据、自然语言输入记账、智能消费分析等功能。

## 功能特性

### 记账方式
- **手动记账** — 支出/收入切换，15 个默认分类，自定义备注和日期
- **拍照识别** — 拍摄或从相册选取票据图片，AI 自动提取金额、分类和备注
- **AI 智能记账** — 自然语言输入（如"午饭花了35块"），AI 自动解析为结构化交易记录

### 数据管理
- **首页看板** — 当月收支总览、今日及近期交易列表
- **财务分析** — 按周/月/年维度查看分类支出占比和收支明细
- **预算管理** — 设置月度总预算和分类预算，超支预警（进度条变红）
- **分类管理** — 支出/收入分类的增删改查，支持自定义图标
- **交易搜索** — 按备注、分类、金额全文检索
- **CSV 导出** — 一键导出交易记录为 CSV 文件（兼容 Excel）

### AI 能力
- **多服务商支持** — 智谱 AI（默认）、OpenAI、通义千问、文心一言、DeepSeek，或自定义 OpenAI 兼容接口
- **连接测试** — 配置 API Key 后可一键测试连通性
- **细粒度开关** — 独立控制图片识别、自然语言解析、AI 分类、AI 消费建议等功能

## 技术栈

| 类别 | 技术 |
|------|------|
| 语言 | Kotlin |
| UI 框架 | Jetpack Compose + Material Design 3 |
| 架构模式 | MVVM |
| 依赖注入 | Hilt |
| 数据库 | Room |
| 导航 | Navigation Compose |
| 相机 | CameraX |
| 网络 | OkHttp |
| 图表 | Vico |
| 图片加载 | Coil |
| 序列化 | Kotlinx Serialization |

## 系统要求

- Android 8.0（API 26）及以上
- compileSdk / targetSdk: 35

## 项目结构

```
app/src/main/java/com/example/accounting/
├── ai/                          # AI 服务层
│   ├── AiConfig.kt              # 默认 AI 配置
│   ├── AiSettingsManager.kt     # AI 设置管理（SharedPreferences）
│   ├── analysis/AiAnalysisService.kt  # AI 消费分析
│   └── voice/TextAiParser.kt    # 自然语言解析
├── data/db/                     # 数据层
│   ├── AppDatabase.kt           # Room 数据库
│   ├── entity/                  # 实体：Transaction, Category, Budget
│   └── dao/                     # DAO：TransactionDao, CategoryDao, BudgetDao
├── di/                          # Hilt DI 模块
│   └── DatabaseModule.kt
├── navigation/
│   └── AppNavigation.kt         # 路由定义、底部导航栏
└── ui/screens/                  # 界面层
    ├── home/                    # 首页
    ├── add/                     # 手动记账
    ├── camera/                  # 拍照识别
    ├── voice/                   # AI 智能记账
    ├── analysis/                # 财务分析
    ├── budget/                  # 预算管理
    ├── category/                # 分类管理
    ├── search/                  # 交易搜索
    ├── settings/                # 设置
    └── ai/                      # AI 设置
```

## 权限说明

| 权限 | 用途 |
|------|------|
| `CAMERA` | 拍摄票据照片 |
| `INTERNET` | 调用 AI API |
| `WRITE_EXTERNAL_STORAGE` | CSV 导出 |
| `READ_EXTERNAL_STORAGE` | 从相册选取图片 |

## 快速开始

1. 克隆项目后用 Android Studio 打开
2. 同步 Gradle 依赖
3. 连接设备或启动模拟器，运行应用
4. 进入 **设置 > AI 设置**，配置 API Key 以启用 AI 功能（默认使用智谱 AI）
