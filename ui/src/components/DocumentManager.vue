<template>
  <div class="document-manager">
    <div class="document-header">
      <h3>知识库管理</h3>
      <div class="document-actions">
        <el-button @click="showAddDialog = true" type="success">
          <el-icon><FolderAdd /></el-icon>
          添加文档
        </el-button>
        <el-button @click="reloadDocuments" :loading="loading" type="primary">
          <el-icon><Refresh /></el-icon>
          重新加载
        </el-button>
      </div>
    </div>

    <div class="document-info">
      <el-alert
        title="管理AI助手的知识库文档，支持添加新文档和重新加载现有文档。"
        type="info"
        show-icon
        :closable="false"
      />
    </div>

    <div class="document-stats">
      <el-descriptions :column="1" size="small" border>
        <el-descriptions-item label="文档路径">src/main/resources/docs</el-descriptions-item>
        <el-descriptions-item label="支持格式">.txt, .md, .doc, .pdf</el-descriptions-item>
      </el-descriptions>
    </div>

    <div class="operation-log">
      <h4>操作日志</h4>
      <div class="log-items">
        <el-card 
          v-for="(log, index) in logs"
          :key="index"
          :class="['log-item', log.type]"
          shadow="hover"
        >
          <div class="log-content">
            <span class="log-time">{{ formatTime(log.timestamp) }}</span>
            <span class="log-message">{{ log.message }}</span>
          </div>
        </el-card>
      </div>
    </div>

    <div v-if="loading" class="loading">
      <el-skeleton :rows="3" animated />
    </div>

    <!-- 添加文档对话框 -->
    <el-dialog
      v-model="showAddDialog"
      title="添加文档到知识库"
      width="500px"
    >
      <div class="dialog-content">
        <p class="dialog-description">
          请输入要添加到知识库的文档完整路径。文档将被自动解析并添加到AI助手的知识库中。
        </p>
        <el-input
          v-model="newDocumentPath"
          placeholder="例如: /path/to/your/document.txt"
          @keydown.enter="confirmAddDocument"
        />
        <div class="path-examples">
          <p class="examples-title">路径示例:</p>
          <el-alert
            title="src/main/resources/docs/新文档.md"
            type="info"
            :closable="false"
          />
          <el-alert
            title="D:/documents/项目说明.txt"
            type="info"
            :closable="false"
          />
          <el-alert
            title="/home/user/知识文档.pdf"
            type="info"
            :closable="false"
          />
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showAddDialog = false">取消</el-button>
          <el-button 
            type="success" 
            @click="confirmAddDocument"
            :disabled="!newDocumentPath.trim() || loading"
          >
            添加
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { FolderAdd, Refresh } from '@element-plus/icons-vue'
import { addDocumentToRAG, reloadRAGDocuments } from '../api'
import { formatTime } from '../utils'

// 定义Emits
const emit = defineEmits<{
  (e: 'document-added', response: any): void
  (e: 'documents-reloaded', response: any): void
}>()

// 定义接口
interface LogEntry {
  type: 'success' | 'error' | 'warning' | 'info'
  message: string
  timestamp: Date
}

// 响应式状态
const loading = ref<boolean>(false)
const showAddDialog = ref<boolean>(false)
const newDocumentPath = ref<string>('')
const logs = ref<LogEntry[]>([])

// 方法
async function addDocument(filePath: string): Promise<void> {
  loading.value = true
  try {
    const response = await addDocumentToRAG(filePath)
    addLog('success', `成功添加文档: ${filePath}`)
    showAddDialog.value = false
    newDocumentPath.value = ''
    emit('document-added', response)
  } catch (error: any) {
    const message = error.response?.data?.error || `添加文档失败: ${error.message}`
    addLog('error', message)
    console.error('添加文档失败:', error)
  } finally {
    loading.value = false
  }
}

async function reloadDocuments(): Promise<void> {
  loading.value = true
  try {
    const response = await reloadRAGDocuments()
    addLog('success', '知识库文档重新加载成功')
    emit('documents-reloaded', response)
  } catch (error: any) {
    const message = error.response?.data?.error || `重新加载失败: ${error.message}`
    addLog('error', message)
    console.error('重新加载文档失败:', error)
  } finally {
    loading.value = false
  }
}

function confirmAddDocument(): void {
  if (!newDocumentPath.value.trim()) {
    addLog('warning', '请输入有效的文档路径')
    return
  }
  addDocument(newDocumentPath.value.trim())
}

function addLog(type: 'success' | 'error' | 'warning' | 'info', message: string): void {
  logs.value.unshift({
    type,
    message,
    timestamp: new Date()
  })
  
  // 只保留最近20条日志
  if (logs.value.length > 20) {
    logs.value = logs.value.slice(0, 20)
  }
}

// 生命周期钩子
onMounted(() => {
  // 添加欢迎日志
  addLog('info', '知识库管理器已启动')
})
</script>

<style scoped>
.document-manager {
  background-color: white;
  border-radius: 8px;
  overflow: hidden;
}

.document-header {
  padding: 20px;
  border-bottom: 1px solid #e1e5e9;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: #f8f9fa;
}

.document-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: bold;
  color: #333;
}

.document-actions {
  display: flex;
  gap: 10px;
}

.document-info {
  padding: 15px 20px;
  border-bottom: 1px solid #e1e5e9;
}

.document-stats {
  padding: 20px;
  border-bottom: 1px solid #e1e5e9;
}

.operation-log {
  padding: 20px;
}

.operation-log h4 {
  margin: 0 0 15px 0;
  font-size: 16px;
  color: #333;
}

.log-items {
  max-height: 300px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.log-item {
  border-radius: 4px;
}

.log-item.success {
  --el-card-border-color: #28a745;
  --el-card-bg-color: #d4edda;
}

.log-item.error {
  --el-card-border-color: #dc3545;
  --el-card-bg-color: #f8d7da;
}

.log-item.warning {
  --el-card-border-color: #ffc107;
  --el-card-bg-color: #fff3cd;
}

.log-item.info {
  --el-card-border-color: #17a2b8;
  --el-card-bg-color: #d1ecf1;
}

.log-content {
  display: flex;
  gap: 10px;
  align-items: flex-start;
}

.log-time {
  white-space: nowrap;
  opacity: 0.8;
  font-size: 12px;
  min-width: 80px;
}

.log-message {
  flex: 1;
  word-break: break-word;
}

.loading {
  padding: 20px;
}

.dialog-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.dialog-description {
  margin: 0;
  color: #666;
  font-size: 14px;
  line-height: 1.5;
}

.path-examples {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.examples-title {
  margin: 0 0 8px 0;
  font-size: 13px;
  font-weight: 500;
  color: #495057;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

/* 滚动条样式 */
.log-items::-webkit-scrollbar {
  width: 6px;
}

.log-items::-webkit-scrollbar-track {
  background: #f1f1f1;
}

.log-items::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.log-items::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

@media (max-width: 768px) {
  .document-header {
    flex-direction: column;
    gap: 15px;
    align-items: stretch;
  }

  .document-actions {
    justify-content: stretch;
  }

  .document-actions :deep(.el-button) {
    flex: 1;
    justify-content: center;
  }

  .dialog {
    width: 95vw;
    height: 90vh;
  }
}
</style>