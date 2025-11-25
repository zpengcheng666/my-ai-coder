import axios from 'axios'

// 配置axios基础URL
const API_BASE_URL = 'http://localhost:8081/api'

/**
 * 添加文档到RAG知识库
 * @param filePath 文件路径
 * @returns 返回添加结果
 */
export async function addDocumentToRAG(filePath: string): Promise<any> {
  try {
    const response = await axios.post(`${API_BASE_URL}/ai/rag/document`, {
      filePath
    })
    return response.data
  } catch (error) {
    console.error('添加文档失败:', error)
    throw error
  }
}

/**
 * 重新加载RAG文档
 * @returns 返回重新加载结果
 */
export async function reloadRAGDocuments(): Promise<any> {
  try {
    const response = await axios.post(`${API_BASE_URL}/ai/rag/reload`)
    return response.data
  } catch (error) {
    console.error('重新加载文档失败:', error)
    throw error
  }
}