# AI 编程小助手 - 前端项目

这是一个基于 Vue3 + TypeScript 开发的 AI 编程小助手前端应用，旨在帮助用户解答编程学习和求职面试相关的问题。

## 功能特点

- 🤖 **智能对话**：基于 SSE (Server-Sent Events) 的实时聊天体验
- 💬 **聊天界面**：现代化的聊天室风格界面，用户消息居右，AI 回复居左
- 📱 **响应式设计**：适配桌面和移动设备
- ⚡ **实时流式响应**：AI 回复实时显示，提供流畅的用户体验
- 🔄 **自动滚动**：新消息自动滚动到底部
- 🎨 **美观界面**：使用 Element Plus 组件库构建的现代化 UI 设计
- 📝 **Markdown 支持**：AI 回复支持完整的 Markdown 格式，包括代码高亮、表格、列表等
- 🧩 **模块化架构**：采用 Composition API 和 TypeScript，代码结构清晰，易于维护

## 技术栈

- **Vue 3** - 前端框架（Composition API）
- **TypeScript** - 类型安全的 JavaScript 超集
- **Vite** - 构建工具
- **Axios** - HTTP 请求库
- **SSE (Server-Sent Events)** - 实时通信
- **Marked** - Markdown 解析和渲染
- **Element Plus** - Vue 3 组件库
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
│   │   ├── ConversationList.vue  # 会话列表组件
│   │   ├── DocumentManager.vue   # 文档管理组件
│   │   └── SettingsPanel.vue     # 设置面板组件
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

应用将在 `http://localhost:3000` 运行（如果端口被占用，会自动使用其他端口）。

### 构建生产版本

```bash
npm run build
```

### 预览生产版本

```bash
npm run preview
```

### 类型检查

```bash
npm run type-check
```

## 使用说明

### 1. 基本操作

1. **开始对话**：在输入框中输入您的问题，按回车或点击发送按钮
2. **切换会话**：在左侧会话列表中点击不同的会话来切换
3. **新建会话**：点击会话列表顶部的"新建会话"按钮
4. **删除会话**：在会话列表中点击会话右侧的删除按钮

### 2. 文档管理

1. **打开文档管理**：点击右上角的"📚"按钮
2. **添加文档**：点击"添加文档"按钮，输入文档路径
3. **重新加载**：点击"重新加载"按钮刷新知识库

### 3. 系统设置

1. **打开设置**：点击右上角的"⚙️"按钮
2. **用户设置**：设置用户ID和名称
3. **聊天设置**：配置流式输出、自动滚动等选项
4. **连接设置**：配置后端服务地址和超时时间
5. **数据管理**：导出、导入或重置设置

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

项目使用 Element Plus 组件库和 CSS3，样式文件分布在各个组件中。主要的样式变量：

- 主色调：`#007bff`
- 背景色：`#f0f0f0`
- 消息气泡：`#f1f3f4`（AI）、`#007bff`（用户）

## 浏览器支持

- Chrome 90+
- Firefox 88+
- Safari 14+
- Edge 90+