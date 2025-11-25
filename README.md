# AI 编程小助手 🤖

一个智能的 AI 编程助手，帮助开发者完成编程任务、面试准备和技术学习。基于 Spring Boot 和 LangChain4j 构建，具备持久化对话记忆、RAG（检索增强生成）以及现代化的 Vue.js 前端界面。

## ✨ 核心功能

- **🧠 智能对话界面**：基于通义千问大模型的智能对话助手
- **💾 持久化对话记忆**：支持会话保存和恢复，数据持久化存储
- **📚 RAG 知识增强**：基于本地文档知识库的检索增强生成
- **🛡️ 输入安全防护**：安全的输入验证机制，确保交互安全
- **🎯 专业工具集成**：面试题生成、技术指导等专业工具
- **🔄 实时流式响应**：基于 SSE 的流式响应机制
- **🎨 现代化界面**：Vue.js 响应式前端，支持 Markdown 渲染
- **📖 内置知识库**：预装 Java 学习路线、面试题库、求职指南等

## 🏗️ 技术栈

### 后端技术
- **开发框架**：Spring Boot 3.5.3
- **Java 版本**：Java 21
- **AI 框架**：LangChain4j 1.1.0
- **大模型服务**：阿里云百炼（通义千问）
- **数据库**：MySQL 8.0.33
- **缓存**：Redis
- **ORM 框架**：Spring Data JPA
- **构建工具**：Maven

### 前端技术
- **开发框架**：Vue.js 3.3.4
- **构建工具**：Vite 4.4.9
- **HTTP 客户端**：Axios
- **Markdown 解析**：Marked 16.0.0

## 📋 环境要求

- Java 21 或更高版本
- Node.js 16+ 和 npm
- MySQL 8.0+
- Redis
- Maven（项目自带）

## 🚀 快速开始

### 1. 克隆项目

```bash
git clone <repository-url>
cd ai-coder
```

### 2. 创建数据库

在 MySQL 中创建数据库：

```sql
CREATE DATABASE ai_chat_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 3. 配置应用

编辑 `src/main/resources/application.yml` 文件：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/ai_chat_db?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: 你的MySQL用户名
    password: 你的MySQL密码

  data:
    redis:
      host: localhost
      port: 6379

langchain4j:
  community:
    dashscope:
      chat-model:
        api-key: 你的DashScope_API_Key
      streaming-chat-model:
        api-key: 你的DashScope_API_Key
      embedding-model:
        api-key: 你的DashScope_API_Key
```

**获取 DashScope API Key**：访问 [阿里云百炼平台](https://dashscope.console.aliyun.com/)

### 4. 启动 Redis

```bash
# Windows
redis-server

# Linux/Mac
redis-server /usr/local/etc/redis.conf
```

### 5. 启动后端服务

```bash
# 使用 Maven Wrapper
.\mvnw.cmd spring-boot:run

# 或使用 Maven
mvn spring-boot:run
```

后端服务将在 `http://localhost:8081/api` 启动

### 6. 启动前端服务

```bash
cd ui
npm install
npm run dev
```

前端服务将在 `http://localhost:5173` 启动

## 📁 项目结构

```
ai-coder/
├── src/main/java/org/example/
│   ├── ai/
│   │   ├── entity/              # 对话相关的实体类
│   │   ├── guardrail/           # 输入验证和安全防护
│   │   ├── listener/            # 监听器配置
│   │   ├── mapper/              # MyBatis-Plus Mapper接口
│   │   ├── memory/              # 持久化聊天记忆实现
│   │   ├── mcp/                 # MCP配置
│   │   ├── model/               # AI 模型配置
│   │   ├── rag/                 # RAG 配置和实现
│   │   ├── service/             # 业务逻辑服务层
│   │   ├── tool/                # LangChain4j 工具集
│   │   ├── AiCodeHelper.java    # AI 助手主接口
│   │   └── AiCodeHelperService.java  # AI 服务实现
│   ├── config/                  # Spring 配置类
│   ├── controller/              # REST API 控制器
│   └── util/                    # 工具类
├── src/main/resources/
│   ├── docs/                    # 知识库文档
│   ├── mapper/                  # MyBatis-Plus XML映射文件
│   ├── application.yml          # 应用配置文件
│   └── system-prompt.txt        # AI 系统提示词
├── ui/
│   ├── src/
│   │   ├── api/                 # API 客户端
│   │   ├── components/          # Vue 组件
│   │   ├── utils/               # 前端工具类
│   │   ├── App.vue              # 主 Vue 组件
│   │   └── main.js              # Vue 入口文件
│   ├── index.html
│   ├── package.json
│   └── vite.config.js
├── pom.xml                      # Maven 依赖配置
└── README.md
```

## 🔧 配置说明

### RAG 配置

在 `application.yml` 中自定义 RAG 行为：

```yaml
rag:
  documents:
    path: src/main/resources/docs  # 知识库文档路径
  vector:
    store:
      type: redis                   # 向量存储类型：redis 或 memory
  segment:
    max-size: 800                   # 最大分块大小
    max-overlap: 200                # 分块重叠大小
  retrieval:
    max-results: 10                 # 最大检索结果数
    min-score: 0.7                  # 最小相似度分数
```

### 对话记忆配置

```yaml
chat:
  memory:
    max-messages: 20                # 上下文中保留的最大消息数
```

## 🎯 API 接口

### 聊天相关接口

- `POST /api/chat` - 发送聊天消息（SSE 流式响应）
- `POST /api/chat/session/new` - 创建新的对话会话
- `GET /api/chat/sessions` - 获取所有对话会话
- `GET /api/chat/session/{sessionId}/messages` - 获取指定会话的消息记录
- `DELETE /api/chat/session/{sessionId}` - 删除指定会话

## 🧪 测试

运行测试用例：

```bash
.\mvnw.cmd test
```

## 📚 知识库

系统在 `src/main/resources/docs/` 目录下预装了以下文档：

- Java 编程学习路线
- 程序员常见面试题
- 鱼皮的求职指南
- 鱼皮的项目学习建议

你可以添加自己的文档来增强 AI 的知识库能力。

## 🔒 安全特性

- **输入安全防护**：使用 `SafeInputGuardrail` 验证和净化用户输入
- **跨域配置**：受控的跨域访问
- **SQL 注入防护**：JPA 参数化查询

## 🛠️ 开发指南

### 添加新工具

1. 在 `src/main/java/org/example/ai/tool/` 创建新的工具类
2. 使用 `@Tool` 注解标注方法
3. 在 `AiCodeHelperServiceFactory` 中注册

示例代码：

```java
public class MyCustomTool {
    @Tool("工具功能描述")
    public String myToolMethod(String input) {
        // 工具逻辑
        return result;
    }
}
```

### 自定义系统提示词

编辑 `src/main/resources/system-prompt.txt` 文件来改变 AI 的行为和个性。

## 🐛 常见问题

### 数据库连接问题
- 确保 MySQL 在 3306 端口运行
- 检查 `application.yml` 中的数据库凭据
- 确认数据库 `ai_chat_db` 已创建

### Redis 连接问题
- 验证 Redis 是否运行：`redis-cli ping`（应返回 PONG）
- 检查 Redis 端口配置

### API Key 问题
- 确保 DashScope API Key 有效且有足够的配额
- 检查到阿里云服务的网络连接

## 📝 许可证

本项目可用于教育和商业用途。

## 🤝 贡献

欢迎贡献代码！请随时提交 Pull Request。

## 📧 联系方式

如有问题或需要支持，请在仓库中提 Issue。

---

**使用 Spring Boot 和 LangChain4j 构建 ❤️**
