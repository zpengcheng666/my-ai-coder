import axios from 'axios'

// 配置axios基础URL
const API_BASE_URL = 'http://localhost:8081/api'

/**
 * 检查服务健康状态
 * @returns 服务是否健康
 */
export async function checkServiceHealth(): Promise<boolean> {
  try {
    const response = await axios.get(`${API_BASE_URL}/ai/health`)
    return response.status === 200
  } catch (error) {
    console.error('健康检查失败:', error)
    return false
  }
}