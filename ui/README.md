# AI 编程小助手 - 前端项目

这是一个基于 Vue3 + TypeScript 开发的 AI 编程小助手前端应用，旨在帮助用户解答编程学习和求职面试相关的问题。

## 功能特点

- 🤖 **智能对话**：基于 SSE (Server-Sent Events) 的实时聊天体验
- 💬 **聊天界面**：现代化的聊天室风格界面，用户消息居右，AI 回复居左
- 📱 **响应式设计**：适配桌面和移动设备
- ⚡ **实时流式响应**：AI 回复实时显示，提供流畅的用户体验
- 🔄 **自动滚动**：新消息自动滚动到底部
- 🎨 **美观界面**：现代化 UI 设计，良好的用户体验
- 📝 **Markdown 支持**：AI 回复支持完整的 Markdown 格式，包括代码高亮、表格、列表等
- 🧩 **模块化架构**：采用 Composition API 和 TypeScript，代码结构清晰，易于维护

## 技术栈

- **Vue 3** - 前端框架（Composition API）
- **TypeScript** - 类型安全的 JavaScript 超集
- **Vite** - 构建工具
- **Axios** - HTTP 请求库
- **SSE (Server-Sent Events)** - 实时通信
- **Marked** - Markdown 解析和渲染
- **CSS3** - 样式和动画

## 项目结构

```
ai-code-helper-frontend/
├── public/                        # 静态资源
├── src/
│   ├── api/                      # API 接口
│   │   ├── modules/              # 按功能模块划分的 API
│   │   │   ├── chat.ts           # 聊天相关接口
│   │   │   ├── conversation.ts   # 会话管理接口
│   │   │   ├── document.ts       # 文档管理接口
│   │   │   └── health.ts         # 健康检查接口
│   │   └── index.ts              # API 统一导出
│   ├── components/               # Vue 组件
│   │   ├── ChatMessage.vue       # 聊天消息组件
│   │   ├── ChatInput.vue         # 输入框组件
│   │   ├── LoadingDots.vue       # 加载动画组件
│   │   └── ...                   # 其他组件
│   ├── composables/              # 可复用的 Composition Functions
│   │   ├── useChat.ts            # 聊天状态管理
│   │   ├── useSidebar.ts         # 侧边栏状态管理
│   │   └── useDialog.ts          # 对话框状态管理
│   ├── types/                    # TypeScript 类型定义
│   │   └── index.ts              # 项目类型定义
│   ├── utils/                    # 工具函数
│   │   └── index.ts              # 通用工具
│   ├── App.vue                   # 主应用组件
│   └── main.ts                   # 应用入口
├── index.html                    # HTML 模板
├── vite.config.ts                # Vite 配置
├── tsconfig.json                 # TypeScript 配置
├── package.json                  # 依赖管理
└── README.md                     # 项目说明
```

## 开始使用

### 前置要求

- Node.js 16.0 或更高版本
- npm 或 yarn 包管理器

### 安装依赖

```bash
npm install
```

### 启动开发服务器

```bash
npm run dev
```

应用将在 `http://localhost:3000` 运行。

### 构建生产版本

```bash
npm run build
```

构建文件将输出到 `dist` 目录。

### 预览生产版本

```bash
npm run preview
```

### 类型检查

```bash
npm run type-check
```

运行 TypeScript 类型检查。

## 后端接口

应用需要连接到后端服务器获取 AI 回复。后端接口信息：

- **基础 URL**: `http://localhost:8081/api`
- **聊天接口**: `GET /ai/chat`
  - 参数：
    - `memoryId`: 聊天室 ID（数字）
    - `message`: 用户消息（字符串）
  - 返回：SSE 流式响应

### 后端服务器

确保后端 SpringBoot 服务正在运行在 `http://localhost:8081`。

## 主要功能

### 1. 聊天功能

- 用户发送消息后，系统自动生成唯一的聊天室 ID
- 通过 SSE 实时接收 AI 回复
- 支持多行输入，按 Enter 发送，Shift+Enter 换行

### 2. 界面特性

- 用户消息显示在右侧（蓝色气泡）
- AI 回复显示在左侧（灰色气泡）
- 实时显示 AI 正在输入的状态
- 自动滚动到最新消息

### 3. 错误处理

- 连接失败时显示错误提示
- 自动重连机制
- 优雅的错误恢复

### 4. Markdown 支持

- **完整的 Markdown 语法**：支持标题、列表、代码块、表格等
- **代码高亮**：编程代码自动应用语法高亮
- **实时渲染**：AI 回复时 Markdown 内容实时渲染
- **响应式设计**：Markdown 内容在各种设备上完美显示
- **主题适配**：在不同背景色下都有良好的视觉效果

支持的 Markdown 功能包括：
- 标题 (H1-H6)
- 粗体、斜体、删除线
- 行内代码和代码块
- 无序和有序列表
- 链接和引用
- 表格和分割线

## 开发指南

### 项目架构

本项目采用模块化架构设计：
1. **Components**：Vue 组件，负责 UI 展示
2. **Composables**：可复用的 Composition Functions，负责业务逻辑
3. **API**：按功能模块划分的 API 接口
4. **Types**：TypeScript 类型定义
5. **Utils**：通用工具函数

### 添加新功能

1. 在 `src/components/` 目录下创建新组件
2. 在 `src/composables/` 目录下添加可复用逻辑
3. 在 `src/api/modules/` 目录下添加相关 API 接口
4. 在 `src/types/` 目录下添加相关类型定义

### 样式自定义

项目使用 CSS3 和 Flexbox 布局，样式文件分布在各个组件中。主要的样式变量：

- 主色调：`#007bff`
- 背景色：`#f0f0f0`
- 消息气泡：`#f1f3f4`（AI）、`#007bff`（用户）

## 浏览器支持

- Chrome 60+
- Firefox 60+
- Safari 12+
- Edge 79+

## 贡献指南

1. Fork 本项目
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 创建 Pull Request

## 许可证

本项目采用 MIT 许可证。

## 联系方式

如有问题或建议，请联系开发团队。