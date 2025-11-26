import axios from 'axios'
import { Conversation, CreateConversationResponse, ApiResponse } from '../types'

// 配置axios基础URL
const API_BASE_URL = 'http://localhost:8081/api'

/**
 * 创建新会话
 * @param userId 用户ID
 * @param title 会话标题
 * @returns 创建的会话信息
 */
export async function createConversation(
  userId: string = 'default_user',
  title: string = '新会话'
): Promise<ApiResponse<CreateConversationResponse>> {
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
 * @returns 会话列表
 */
export async function getConversations(userId: string = 'default_user'): Promise<ApiResponse<Conversation[]>> {
  try {
    const response = await axios.get(`${API_BASE_URL}/ai/conversations`, {
      params: { userId }
    })
    return response.data
  } catch (error) {
    console.error('获取会话列表失败:', error)
    throw error
  }
}

/**
 * 删除会话
 * @param conversationId 会话ID
 * @param userId 用户ID
 * @returns 删除结果
 */
export async function deleteConversation(
  conversationId: string,
  userId: string = 'default_user'
): Promise<ApiResponse<boolean>> {
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

/**
 * 更新会话标题
 * @param conversationId 会话ID
 * @param title 新标题
 * @param userId 用户ID
 * @returns 更新结果
 */
export async function updateConversationTitle(
  conversationId: string,
  title: string,
  userId: string = 'default_user'
): Promise<ApiResponse<boolean>> {
  try {
    const response = await axios.put(`${API_BASE_URL}/ai/conversation/${conversationId}`, {
      title
    }, {
      params: { userId }
    })
    return response.data
  } catch (error) {
    console.error('更新会话标题失败:', error)
    throw error
  }
}