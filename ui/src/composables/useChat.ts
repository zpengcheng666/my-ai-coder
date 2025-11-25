import { ref, Ref, reactive, computed, ComputedRef } from 'vue'
import { 
  Message, 
  Settings, 
  Conversation 
} from '../types'
import { 
  chatWithSSE, 
  getConversationMessages, 
  checkServiceHealth 
} from '../api'
import { generateMemoryId } from '../utils'

// 定义返回类型
export interface UseChatReturn {
  messages: Ref<Message[]>
  currentConversationId: Ref<string | null>
  currentConversationTitle: Ref<string>
  isAiTyping: Ref<boolean>
  isStreaming: Ref<boolean>
  currentAiResponse: Ref<string>
  currentEventSource: Ref<EventSource | null>
  connectionError: Ref<boolean>
  isLoadingMessages: Ref<boolean>
  connectionStatus: Ref<string>
  connectionStatusText: Ref<string>
  settings: Settings
  currentAiResponseRendered: ComputedRef<string>
  sendMessage: (message: string) => void
  addMessage: (content: string, isUser?: boolean) => void
  startAiResponse: (userMessage: string) => void
  finishAiResponse: () => void
  selectConversation: (conversation: Conversation | null) => Promise<void>
  loadConversationMessages: (conversationId: string) => Promise<void>
  updateSettings: (newSettings: Partial<Settings>) => void
  updateConnectionStatus: (status?: string | null) => Promise<void>
}

/**
 * 聊天状态管理Composable
 * @returns 聊天相关的状态和方法
 */
export function useChat(): UseChatReturn {
  // 响应式状态
  const messages = ref<Message[]>([])
  const currentConversationId = ref<string | null>(null)
  const currentConversationTitle = ref<string>('')
  const isAiTyping = ref<boolean>(false)
  const isStreaming = ref<boolean>(false)
  const currentAiResponse = ref<string>('')
  const currentEventSource = ref<EventSource | null>(null)
  const connectionError = ref<boolean>(false)
  const isLoadingMessages = ref<boolean>(false)
  const connectionStatus = ref<string>('unknown') // 'connected', 'disconnected', 'unknown'
  const connectionStatusText = ref<string>('连接状态未知')
  
  // 设置
  const settings = reactive<Settings>({
    userId: 'default_user',
    userName: '用户',
    streamMode: true,
    autoScroll: true,
    showTimestamp: true,
    apiBaseUrl: 'http://localhost:8081/api',
    timeout: 60
  })
  
  // 计算属性
  const currentAiResponseRendered = computed<string>(() => {
    if (!currentAiResponse.value) return ''
    // 使用 marked v16 的新 API (注意：这里需要在实际使用时导入marked)
    // 为了简化示例，我们直接返回处理后的文本
    return currentAiResponse.value
  })
  
  // 方法实现
  function addMessage(content: string, isUser: boolean = false): void {
    const message: Message = {
      id: Date.now() + Math.random(),
      content,
      isUser,
      timestamp: new Date()
    }
    messages.value.push(message)
  }
  
  function startAiResponse(userMessage: string): void {
    isAiTyping.value = true
    isStreaming.value = settings.streamMode
    currentAiResponse.value = ''
    connectionError.value = false
    
    // 关闭之前的连接
    if (currentEventSource.value) {
      currentEventSource.value.close()
    }
    
    // 确保有当前会话ID
    if (!currentConversationId.value) {
      currentConversationId.value = 'conversation_' + generateMemoryId()
    }
    
    // 开始SSE连接
    if (settings.streamMode) {
      currentEventSource.value = chatWithSSE(
        currentConversationId.value,
        userMessage,
        settings.userId,
        handleAiMessage,
        handleAiError,
        handleAiClose
      )
    }
  }
  
  function handleAiMessage(data: string): void {
    currentAiResponse.value += data
  }
  
  function handleAiError(error: any): void {
    console.error('AI 回复出错:', error)
    connectionError.value = true
    connectionStatus.value = 'disconnected'
    finishAiResponse()
    
    // 5秒后自动隐藏错误提示
    setTimeout(() => {
      connectionError.value = false
    }, 5000)
  }
  
  function handleAiClose(): void {
    finishAiResponse()
  }
  
  function finishAiResponse(): void {
    isStreaming.value = false
    
    // 如果有内容，添加到消息列表
    if (currentAiResponse.value.trim()) {
      addMessage(currentAiResponse.value.trim(), false)
    }
    
    // 重置状态
    isAiTyping.value = false
    currentAiResponse.value = ''
    
    // 重置连接错误状态（确保正常结束时清除错误提示）
    connectionError.value = false
    
    // 关闭连接
    if (currentEventSource.value) {
      currentEventSource.value.close()
      currentEventSource.value = null
    }
  }
  
  async function selectConversation(conversation: Conversation | null): Promise<void> {
    if (!conversation) {
      currentConversationId.value = null
      currentConversationTitle.value = ''
      messages.value = []
      return
    }

    const conversationId = conversation.conversationId
    if (!conversationId) return

    currentConversationId.value = conversationId
    currentConversationTitle.value = conversation.title || '未命名会话'
    messages.value = []

    await loadConversationMessages(conversationId)
    console.log('选择会话:', conversation)
  }
  
  async function loadConversationMessages(conversationId: string): Promise<void> {
    if (!conversationId) return

    isLoadingMessages.value = true
    try {
      const response = await getConversationMessages(conversationId, settings.userId)
      const history = Array.isArray(response.messages) ? response.messages : []
      const mappedMessages: Message[] = history.map(msg => ({
        id: msg.id || `${conversationId}-${Math.random()}`,
        content: msg.content || '',
        isUser: !!msg.isUser,
        timestamp: parseTimestamp(msg.timestamp)
      }))

      if (currentConversationId.value === conversationId) {
        messages.value = mappedMessages
      }
    } catch (error) {
      console.error('加载历史消息失败:', error)
    } finally {
      isLoadingMessages.value = false
    }
  }
  
  function parseTimestamp(value: string | [number, number, number, number, number, number, number] | undefined): Date {
    if (!value) return new Date()
    if (Array.isArray(value)) {
      const [year, month = 1, day = 1, hour = 0, minute = 0, second = 0, nano = 0] = value
      return new Date(year, month - 1, day, hour, minute, second, Math.floor(nano / 1e6))
    }
    const parsed = new Date(value)
    return isNaN(parsed.getTime()) ? new Date() : parsed
  }
  
  function updateSettings(newSettings: Partial<Settings>): void {
    Object.assign(settings, newSettings)
    // 保存到localStorage
    localStorage.setItem('ai-helper-settings', JSON.stringify(settings))
  }
  
  async function updateConnectionStatus(status: string | null = null): Promise<void> {
    if (status) {
      connectionStatus.value = status
    } else {
      try {
        const isHealthy = await checkServiceHealth()
        connectionStatus.value = isHealthy ? 'connected' : 'disconnected'
      } catch (error) {
        connectionStatus.value = 'disconnected'
      }
    }
    
    // 更新状态文本
    switch (connectionStatus.value) {
      case 'connected':
        connectionStatusText.value = '已连接'
        break
      case 'disconnected':
        connectionStatusText.value = '连接断开'
        break
      default:
        connectionStatusText.value = '连接状态未知'
    }
  }
  
  function sendMessage(message: string): void {
    // 添加用户消息
    addMessage(message, true)
    
    // 开始AI回复
    startAiResponse(message)
  }
  
  return {
    // 状态
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
    
    // 计算属性
    currentAiResponseRendered,
    
    // 方法
    sendMessage,
    addMessage,
    startAiResponse,
    finishAiResponse,
    selectConversation,
    loadConversationMessages,
    updateSettings,
    updateConnectionStatus
  }
}