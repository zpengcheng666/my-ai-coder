<template>
  <el-container class="app-container">
    <!-- 头部工具栏 -->
    <el-header class="app-header">
      <div class="header-left">
        <el-button @click="toggleSidebar" class="sidebar-toggle" link>
          <el-icon size="20"><Menu /></el-icon>
        </el-button>
        <h1 class="app-title">AI 编程知识库</h1>
      </div>
      <div class="header-center">
        <div class="app-subtitle">{{ currentConversationTitle || '帮助您解答编程学习和求职面试相关问题' }}</div>
      </div>
      <div class="header-right">
        <el-button @click="openDocumentManager" class="toolbar-btn" link title="文档管理">
          <el-icon size="20"><Collection /></el-icon>
        </el-button>
        <el-button @click="openSettings" class="toolbar-btn" link title="设置">
          <el-icon size="20"><Setting /></el-icon>
        </el-button>
        <div class="connection-indicator">
          <span :class="['indicator', connectionStatus]" :title="connectionStatusText"></span>
        </div>
      </div>
    </el-header>

    <el-container class="app-body">
      <!-- 侧边栏 -->
      <el-aside :class="['sidebar', { collapsed: sidebarCollapsed }]">
        <ConversationList
          :userId="settings.userId"
          :currentConversationId="currentConversationId || undefined"
          @conversation-selected="selectConversation"
          @conversation-created="selectConversation"
        />
      </el-aside>

      <!-- 主聊天区域 -->
      <el-main class="main-content">
        <!-- 消息列表 -->
        <div class="messages-container" ref="messagesContainer">
          <div v-if="isLoadingMessages" class="messages-loading">
            历史消息加载中...
          </div>

          <div v-if="!isLoadingMessages && messages.length === 0" class="welcome-message">
            <div class="welcome-content">
              <div class="welcome-icon">🤖</div>
              <h2>欢迎使用 AI 编程知识库</h2>
              <p>我可以帮助您：</p>
              <ul>
                <li>解答编程技术问题</li>
                <li>提供代码示例和解释</li>
                <li>协助求职面试准备</li>
                <li>分享编程学习建议</li>
              </ul>
              <p>请随时向我提问吧！</p>
            </div>
          </div>

          <!-- 历史消息 -->
          <ChatMessage
            v-for="message in messages"
            :key="message.id"
            :message="message.content"
            :is-user="message.isUser"
            :timestamp="settings.showTimestamp ? message.timestamp : undefined"
          />

          <!-- AI 正在回复的消息 -->
          <div v-if="isAiTyping" class="chat-message ai-message">
            <div class="message-avatar">
              <div class="avatar ai-avatar">AI</div>
            </div>
            <div class="message-content">
              <div class="message-bubble">
                <div class="ai-typing-content">
                  <div class="ai-response-text message-markdown" v-html="currentAiResponseRendered"></div>
                  <LoadingDots v-if="isStreaming" />
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 输入框 -->
        <ChatInput
          :disabled="isAiTyping"
          @send-message="sendMessage"
          placeholder="请输入您的编程问题..."
        />
      </el-main>
    </el-container>

    <!-- 连接状态提示 -->
    <el-alert
      v-if="connectionError"
      title="连接服务器失败，请检查后端服务是否启动"
      type="error"
      center
      show-icon
      class="connection-error"
    />

    <!-- 文档管理对话框 -->
    <el-dialog
      v-if="showDocumentManager"
      v-model="showDocumentManager"
      title="知识库管理"
      width="600px"
      @close="closeDocumentManager"
    >
      <DocumentManager
        @document-added="onDocumentAdded"
        @documents-reloaded="onDocumentsReloaded"
      />
    </el-dialog>

    <!-- 设置对话框 -->
    <el-dialog
      v-if="showSettings"
      v-model="showSettings"
      :title="null"
      width="850px"
      :show-close="false"
      :close-on-click-modal="false"
      @close="closeSettings"
      class="settings-dialog"
    >
      <SettingsPanel
        :settings="settings"
        @settings-changed="updateSettings"
        @close="closeSettings"
      />
    </el-dialog>
  </el-container>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, computed, watch } from 'vue'
import { Menu, Collection, Setting } from '@element-plus/icons-vue'
import ChatMessage from './components/ChatMessage.vue'
import ChatInput from './components/ChatInput.vue'
import LoadingDots from './components/LoadingDots.vue'
import ConversationList from './components/ConversationList.vue'
import DocumentManager from './components/DocumentManager.vue'
import SettingsPanel from './components/SettingsPanel.vue'
import { useChat } from './composables/useChat'
import { useSidebar } from './composables/useSidebar'
import { useDialog } from './composables/useDialog'
import { marked } from 'marked'
import type { Ref } from 'vue'

// 使用 composables
const {
  messages,
  currentConversationId,
  currentConversationTitle,
  isAiTyping,
  isStreaming,
  currentAiResponse,
  currentEventSource,
  connectionError,
  isLoadingMessages,
  connectionStatus,
  connectionStatusText,
  settings,
  currentAiResponseRendered: rawCurrentAiResponseRendered,
  sendMessage,
  addMessage,
  startAiResponse,
  finishAiResponse,
  selectConversation,
  loadConversationMessages,
  updateSettings,
  updateConnectionStatus
} = useChat()

const { sidebarCollapsed, toggleSidebar } = useSidebar()
const { 
  showDocumentManager, 
  showSettings, 
  openDocumentManager, 
  closeDocumentManager, 
  openSettings, 
  closeSettings 
} = useDialog()

// 模板引用
const messagesContainer: Ref<HTMLElement | null> = ref(null)

// 计算属性
const currentAiResponseRendered = computed(() => {
  if (!currentAiResponse.value) return ''
  // 使用 marked v16 的新 API
  return marked.parse(currentAiResponse.value, {
    breaks: true, // 支持换行
    gfm: true // 支持GitHub风格的Markdown
  })
})

// 方法
function scrollToBottom() {
  setTimeout(() => {
    const container = messagesContainer.value
    if (container) {
      container.scrollTop = container.scrollHeight
    }
  }, 0)
}

function onDocumentAdded(response: any) {
  console.log('文档添加成功:', response)
  // 可以在这里显示成功消息或刷新相关状态
}

function onDocumentsReloaded(response: any) {
  console.log('文档重新加载成功:', response)
  // 可以在这里显示成功消息或刷新相关状态
}

function initializeApp() {
  // 如果没有当前会话，创建一个默认会话
  if (!currentConversationId.value) {
    // 这里需要生成一个ID，但由于我们移除了generateMemoryId的导入，我们需要另一种方式
    currentConversationId.value = 'conversation_' + Date.now()
  }
  console.log('会话ID:', currentConversationId.value)
}

// 生命周期钩子
onMounted(() => {
  // 加载设置
  try {
    const saved = localStorage.getItem('ai-helper-settings')
    if (saved) {
      const parsedSettings = JSON.parse(saved)
      Object.assign(settings, parsedSettings)
    }
  } catch (error) {
    console.error('加载设置失败:', error)
  }
  
  initializeApp()
  updateConnectionStatus()
  
  // 定期检查连接状态
  const intervalId = setInterval(() => {
    updateConnectionStatus()
  }, 30000) // 每30秒检查一次
  
  // 清理定时器
  onBeforeUnmount(() => {
    clearInterval(intervalId)
  })
})

onBeforeUnmount(() => {
  // 组件销毁前关闭连接
  if (currentEventSource.value) {
    currentEventSource.value.close()
  }
})

// 监听器
watch(messages, () => {
  if (settings.autoScroll) {
    scrollToBottom()
  }
}, { deep: true })

watch(currentAiResponse, () => {
  if (settings.autoScroll) {
    scrollToBottom()
  }
})
</script>

<style scoped>
.app-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
}

.app-header {
  background-color: #fff;
  padding: 12px 20px;
  border-bottom: 1px solid #e1e5e9;
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 60px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 15px;
  flex: 0 0 auto;
}

.sidebar-toggle {
  padding: 8px;
  border-radius: 4px;
  color: #666;
  font-size: 18px;
  transition: background-color 0.2s, color 0.2s;
}

.sidebar-toggle:hover {
  background-color: #f5f5f5;
  color: #333;
}

.app-title {
  font-size: 20px;
  font-weight: bold;
  color: #333;
  margin: 0;
}

.header-center {
  flex: 1;
  text-align: center;
  padding: 0 20px;
}

.app-subtitle {
  font-size: 14px;
  color: #666;
  margin: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 10px;
  flex: 0 0 auto;
}

.toolbar-btn {
  padding: 8px 10px;
  border-radius: 4px;
  color: #666;
  font-size: 16px;
  transition: background-color 0.2s, color 0.2s;
}

.toolbar-btn:hover {
  background-color: #f5f5f5;
  color: #333;
}

.connection-indicator {
  display: flex;
  align-items: center;
}

.indicator {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background-color: #ccc;
}

.indicator.connected {
  background-color: #28a745;
  box-shadow: 0 0 0 2px rgba(40, 167, 69, 0.3);
}

.indicator.disconnected {
  background-color: #dc3545;
  box-shadow: 0 0 0 2px rgba(220, 53, 69, 0.3);
}

.indicator.unknown {
  background-color: #6c757d;
}

.app-body {
  flex: 1;
  overflow: hidden;
}

.sidebar {
  flex: 0 0 300px;
  background-color: #f8f9fa;
  border-right: 1px solid #e1e5e9;
  transition: margin-left 0.3s ease;
  height: calc(100vh - 60px);
}

.sidebar.collapsed {
  margin-left: -300px;
}

.main-content {
  padding: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.messages-container {
  flex: 1;
  overflow-y: auto;
  padding: 20px 0;
}

.messages-loading {
  padding: 10px 20px;
  color: #666;
  font-size: 14px;
}

.welcome-message {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  padding: 0 20px;
}

.welcome-content {
  text-align: center;
  max-width: 400px;
  color: #666;
}

.welcome-icon {
  font-size: 48px;
  margin-bottom: 20px;
}

.welcome-content h2 {
  font-size: 20px;
  margin-bottom: 15px;
  color: #333;
}

.welcome-content p {
  margin-bottom: 10px;
  line-height: 1.5;
}

.welcome-content ul {
  text-align: left;
  margin: 15px 0;
}

.welcome-content li {
  margin-bottom: 5px;
}

/* AI 正在回复时的消息样式 */
.chat-message {
  display: flex;
  margin-bottom: 20px;
  padding: 0 20px;
}

.ai-message {
  justify-content: flex-start;
  flex-direction: row;
}

.message-avatar {
  display: flex;
  align-items: flex-start;
  margin: 0 10px;
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: bold;
  color: white;
}

.ai-avatar {
  background-color: #6c757d;
}

.message-content {
  max-width: 70%;
  min-width: 100px;
}

.message-bubble {
  padding: 12px 16px;
  border-radius: 18px;
  position: relative;
  word-wrap: break-word;
  word-break: break-word;
  background-color: #f1f3f4;
  color: #333;
  border-bottom-left-radius: 4px;
}

.ai-typing-content {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.ai-response-text {
  font-size: 14px;
  line-height: 1.5;
}

/* AI实时回复的Markdown样式 */
.ai-response-text.message-markdown h1,
.ai-response-text.message-markdown h2,
.ai-response-text.message-markdown h3,
.ai-response-text.message-markdown h4,
.ai-response-text.message-markdown h5,
.ai-response-text.message-markdown h6 {
  margin: 0.5em 0;
  font-weight: bold;
}

.ai-response-text.message-markdown h1 { font-size: 1.5em; }
.ai-response-text.message-markdown h2 { font-size: 1.3em; }
.ai-response-text.message-markdown h3 { font-size: 1.2em; }
.ai-response-text.message-markdown h4 { font-size: 1.1em; }
.ai-response-text.message-markdown h5 { font-size: 1em; }
.ai-response-text.message-markdown h6 { font-size: 0.9em; }

.ai-response-text.message-markdown p {
  margin: 0.5em 0;
}

.ai-response-text.message-markdown ul,
.ai-response-text.message-markdown ol {
  margin: 0.5em 0;
  padding-left: 1.5em;
}

.ai-response-text.message-markdown li {
  margin: 0.2em 0;
}

.ai-response-text.message-markdown code {
  background-color: rgba(0, 0, 0, 0.1);
  padding: 0.2em 0.4em;
  border-radius: 3px;
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  font-size: 0.9em;
}

.ai-response-text.message-markdown pre {
  background-color: rgba(0, 0, 0, 0.1);
  padding: 1em;
  border-radius: 5px;
  overflow-x: auto;
  margin: 0.5em 0;
}

.ai-response-text.message-markdown pre code {
  background-color: transparent;
  padding: 0;
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  font-size: 0.9em;
}

.ai-response-text.message-markdown blockquote {
  border-left: 4px solid #ccc;
  padding-left: 1em;
  margin: 0.5em 0;
  font-style: italic;
  color: #666;
}

.ai-response-text.message-markdown a {
  color: #007bff;
  text-decoration: underline;
}

.ai-response-text.message-markdown table {
  border-collapse: collapse;
  width: 100%;
  margin: 0.5em 0;
}

.ai-response-text.message-markdown th,
.ai-response-text.message-markdown td {
  border: 1px solid #ddd;
  padding: 0.5em;
  text-align: left;
}

.ai-response-text.message-markdown th {
  background-color: #f2f2f2;
  font-weight: bold;
}

.ai-response-text.message-markdown hr {
  border: none;
  border-top: 1px solid #ddd;
  margin: 1em 0;
}

.connection-error {
  position: fixed;
  top: 80px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 1000;
  animation: slideDown 0.3s ease-out;
}

/* 滚动条样式 */
.messages-container::-webkit-scrollbar {
  width: 6px;
}

.messages-container::-webkit-scrollbar-track {
  background: #f1f1f1;
}

.messages-container::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.messages-container::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .app-header {
    padding: 10px 15px;
  }
  
  .header-left {
    gap: 10px;
  }
  
  .app-title {
    font-size: 18px;
  }
  
  .header-center {
    padding: 0 10px;
  }
  
  .app-subtitle {
    font-size: 12px;
  }
  
  .sidebar {
    flex: 0 0 280px;
  }
  
  .sidebar.collapsed {
    margin-left: -280px;
  }
  
  .messages-container {
    padding: 15px 0;
  }
  
  .welcome-content {
    padding: 0 10px;
  }
  
  .message-content {
    max-width: 85%;
  }
  
  .chat-message {
    padding: 0 10px;
  }
  
  .toolbar-btn {
    padding: 6px 8px;
    font-size: 14px;
  }
}

@media (max-width: 480px) {
  .sidebar {
    flex: 0 0 100vw;
    position: absolute;
    top: 60px;
    bottom: 0;
    z-index: 100;
  }
  
  .sidebar.collapsed {
    margin-left: -100vw;
  }
  
  .header-center {
    display: none;
  }
  
  .app-title {
    font-size: 16px;
  }
}

/* 设置对话框样式 */
:deep(.settings-dialog) {
  border-radius: 12px;
}

:deep(.settings-dialog .el-dialog) {
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 12px 48px rgba(0, 0, 0, 0.15);
}

:deep(.settings-dialog .el-dialog__header) {
  display: none;
}

:deep(.settings-dialog .el-dialog__body) {
  padding: 0;
}
</style>