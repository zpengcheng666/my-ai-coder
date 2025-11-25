import axios from 'axios'
import { 
  Conversation, 
  ConversationApiResponse, 
  CreateConversationResponse 
} from '../../types'

// 配置axios基础URL
const API_BASE_URL = 'http://localhost:8081/api'

/**
 * 创建新会话
 * @param userId 用户ID
 * @param title 会话标题
 * @returns 返回创建的会话信息
 */
export async function createConversation(
  userId: string, 
  title: string
): Promise<CreateConversationResponse> {
  try {
    const response = await axios.post(`${API_BASE_URL}/ai/conversation`, {
      userId,
      title
    })
    return response.data
  } catch (error) {
    console.error('创建会话失败:', error)
    throw error
  }
}

/**
 * 获取用户会话列表
 * @param userId 用户ID
 * @param page 页码
 * @param size 每页大小
 * @returns 返回会话列表
 */
export async function getUserConversations(
  userId: string, 
  page: number = 0, 
  size: number = 20
): Promise<ConversationApiResponse> {
  try {
    const response = await axios.get(`${API_BASE_URL}/ai/conversations`, {
      params: {
        userId,
        page,
        size
      }
    })
    return response.data
  } catch (error) {
    console.error('获取会话列表失败:', error)
    throw error
  }
}

/**
 * 删除会话（软删除）
 * @param conversationId 会话ID
 * @param userId 用户ID
 * @returns 返回删除结果
 */
export async function deleteConversation(
  conversationId: string, 
  userId: string
): Promise<any> {
  try {
    const response = await axios.delete(`${API_BASE_URL}/ai/conversation/${conversationId}`, {
      params: { userId }
    })
    return response.data
  } catch (error) {
    console.error('删除会话失败:', error)
    throw error
  }
}