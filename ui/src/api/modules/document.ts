import axios from 'axios'
import { ApiResponse } from '../../types'

// 配置axios基础URL
const API_BASE_URL = 'http://localhost:8081/api'

/**
 * 添加文档
 * @param file 文件对象
 * @param userId 用户ID
 * @returns 添加结果
 */
export async function addDocument(file: File, userId: string = 'default_user'): Promise<ApiResponse<any>> {
  try {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('userId', userId)
    
    const response = await axios.post(`${API_BASE_URL}/ai/documents`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
    
    return response.data
  } catch (error) {
    console.error('添加文档失败:', error)
    throw error
  }
}

/**
 * 删除文档
 * @param docId 文档ID
 * @param userId 用户ID
 * @returns 删除结果
 */
export async function deleteDocument(docId: string, userId: string = 'default_user'): Promise<ApiResponse<boolean>> {
  try {
    const response = await axios.delete(`${API_BASE_URL}/ai/documents/${docId}`, {
      params: { userId }
    })
    return response.data
  } catch (error) {
    console.error('删除文档失败:', error)
    throw error
  }
}

/**
 * 获取文档列表
 * @param userId 用户ID
 * @returns 文档列表
 */
export async function getDocuments(userId: string = 'default_user'): Promise<ApiResponse<any[]>> {
  try {
    const response = await axios.get(`${API_BASE_URL}/ai/documents`, {
      params: { userId }
    })
    return response.data
  } catch (error) {
    console.error('获取文档列表失败:', error)
    throw error
  }
}

/**
 * 将本地文档添加到 RAG 知识库
 * @param filePath 文档本地路径
 * @returns 添加结果
 */
export async function addDocumentToRAG(filePath: string): Promise<ApiResponse<any>> {
  try {
    const response = await axios.post(`${API_BASE_URL}/ai/rag/documents`, {
      filePath
    })
    return response.data
  } catch (error) {
    console.error('添加文档到 RAG 失败:', error)
    throw error
  }
}

/**
 * 重新加载 RAG 知识库文档
 * @returns 重新加载结果
 */
export async function reloadRAGDocuments(): Promise<ApiResponse<any>> {
  try {
    const response = await axios.post(`${API_BASE_URL}/ai/rag/reload`)
    return response.data
  } catch (error) {
    console.error('重新加载 RAG 文档失败:', error)
    throw error
  }
}