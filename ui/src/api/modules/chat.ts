import axios from 'axios'
import { 
  Message, 
  ConversationMessagesResponse, 
  CreateConversationResponse 
} from '../../types'

// 配置axios基础URL
const API_BASE_URL = 'http://localhost:8081/api'

// 配置axios默认设置
axios.defaults.timeout = 60000
axios.defaults.headers.post['Content-Type'] = 'application/json'

/**
 * 使用 SSE 方式调用聊天接口
 * @param conversationId 会话ID
 * @param message 用户消息
 * @param userId 用户ID
 * @param onMessage 接收消息的回调函数
 * @param onError 错误处理回调函数
 * @param onClose 连接关闭回调函数
 * @returns 返回 EventSource 对象，用于手动关闭连接
 */
export function chatWithSSE(
  conversationId: string, 
  message: string, 
  userId: string = 'default_user', 
  onMessage: (data: string) => void, 
  onError?: (error: any) => void, 
  onClose?: () => void
): EventSource {
  // 构建URL参数
  const params = new URLSearchParams({
    memoryId: conversationId, // 兼容旧版本参数名
    conversationId: conversationId,
    userId: userId,
    message: message
  })
  
  // 创建 EventSource 连接
  const eventSource = new EventSource(`${API_BASE_URL}/ai/chat?${params}`)
  
  // 处理接收到的消息
  eventSource.onmessage = function(event) {
    try {
      const data = event.data
      if (data && data.trim() !== '') {
        onMessage(data)
      }
    } catch (error) {
      console.error('解析消息失败:', error)
      onError && onError(error)
    }
  }
  
  // 处理错误
  eventSource.onerror = function(error) {
    console.log('SSE 连接状态:', eventSource.readyState)
    // 只有在连接状态不是正常关闭时才报错
    if (eventSource.readyState !== EventSource.CLOSED) {
      console.error('SSE 连接错误:', error)
      onError && onError(error)
    } else {
      console.log('SSE 连接正常结束')
    }
    
    // 确保连接关闭
    if (eventSource.readyState !== EventSource.CLOSED) {
      eventSource.close()
    }
  }
  
  // 处理连接关闭
  eventSource.addEventListener('close', function() {
    console.log('SSE 连接已关闭')
    onClose && onClose()
  })
  
  return eventSource
}

/**
 * 普通聊天接口（非流式）
 * @param conversationId 会话ID
 * @param message 用户消息
 * @param userId 用户ID
 * @returns 返回聊天响应
 */
export async function chatSync(
  conversationId: string, 
  message: string, 
  userId: string = 'default_user'
): Promise<any> {
  try {
    const response = await axios.post(`${API_BASE_URL}/ai/chat`, {
      conversationId,
      message,
      userId
    })
    return response.data
  } catch (error) {
    console.error('同步聊天请求失败:', error)
    throw error
  }
}

/**
 * 根据会话ID获取历史消息
 * @param conversationId 会话ID
 * @param userId 用户ID
 * @returns 返回会话历史消息
 */
export async function getConversationMessages(
  conversationId: string, 
  userId: string = 'default_user'
): Promise<ConversationMessagesResponse> {
  try {
    const response = await axios.get(`${API_BASE_URL}/ai/conversation/${conversationId}/messages`, {
      params: { userId }
    })
    return response.data
  } catch (error) {
    console.error('获取会话历史消息失败:', error)
    throw error
  }
}