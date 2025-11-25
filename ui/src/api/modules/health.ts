import axios from 'axios'

// 配置axios基础URL
const API_BASE_URL = 'http://localhost:8081/api'

/**
 * 检查后端服务是否可用
 * @returns 返回服务是否可用
 */
export async function checkServiceHealth(): Promise<boolean> {
  try {
    const response = await axios.get(`${API_BASE_URL}/ai/health`, {
      timeout: 5000
    })
    return response.status === 200
  } catch (error) {
    console.error('服务健康检查失败:', error)
    return false
  }
}