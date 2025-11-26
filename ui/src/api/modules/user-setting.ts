import axios from 'axios'
import { ApiResponse } from '../types'

// 配置axios基础URL
const API_BASE_URL = 'http://localhost:8081/api'

export interface UserSetting {
  userId: string
  userName: string
  streamMode: boolean
  autoScroll: boolean
  showTimestamp: boolean
  apiBaseUrl: string
  timeout: number
}

/**
 * 获取用户设置
 * @param userId 用户ID
 * @returns 用户设置
 */
export async function getUserSettings(userId: string): Promise<ApiResponse<UserSetting>> {
  try {
    const response = await axios.get(`${API_BASE_URL}/settings/${userId}`)
    return response.data
  } catch (error) {
    console.error('获取用户设置失败:', error)
    throw error
  }
}

/**
 * 保存用户设置
 * @param userId 用户ID
 * @param settings 用户设置
 * @returns 保存结果
 */
export async function saveUserSettings(
  userId: string,
  settings: UserSetting
): Promise<ApiResponse<boolean>> {
  try {
    const response = await axios.post(`${API_BASE_URL}/settings/${userId}`, settings)
    return response.data
  } catch (error) {
    console.error('保存用户设置失败:', error)
    throw error
  }
}