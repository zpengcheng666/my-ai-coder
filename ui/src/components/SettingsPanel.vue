<template>
  <div class="settings-panel">
    <div class="settings-header">
      <h3>系统设置</h3>
      <el-button @click="$emit('close')" type="danger" :icon="Close" circle />
    </div>

    <div class="settings-content">
      <!-- 用户设置 -->
      <el-card class="settings-section">
        <template #header>
          <div class="card-header">
            <span>用户设置</span>
          </div>
        </template>
        <div class="setting-item">
          <el-form-item label="用户ID">
            <el-input
              v-model="localSettings.userId"
              placeholder="输入用户ID"
              @input="saveSettings"
            />
            <div class="setting-description">用于标识您的身份，会话记录将与此ID关联</div>
          </el-form-item>
        </div>
        <div class="setting-item">
          <el-form-item label="用户名称">
            <el-input
              v-model="localSettings.userName"
              placeholder="输入用户名称"
              @input="saveSettings"
            />
            <div class="setting-description">显示在界面上的用户名称</div>
          </el-form-item>
        </div>
      </el-card>

      <!-- 聊天设置 -->
      <el-card class="settings-section">
        <template #header>
          <div class="card-header">
            <span>聊天设置</span>
          </div>
        </template>
        <div class="setting-item">
          <el-form-item label="启用流式输出">
            <el-switch
              v-model="localSettings.streamMode"
              @change="saveSettings"
            />
            <div class="setting-description">AI回复时实时显示内容（推荐）</div>
          </el-form-item>
        </div>
        <div class="setting-item">
          <el-form-item label="自动滚动到底部">
            <el-switch
              v-model="localSettings.autoScroll"
              @change="saveSettings"
            />
            <div class="setting-description">新消息到达时自动滚动到聊天底部</div>
          </el-form-item>
        </div>
        <div class="setting-item">
          <el-form-item label="显示消息时间">
            <el-switch
              v-model="localSettings.showTimestamp"
              @change="saveSettings"
            />
            <div class="setting-description">在消息中显示发送时间</div>
          </el-form-item>
        </div>
      </el-card>

      <!-- 连接设置 -->
      <el-card class="settings-section">
        <template #header>
          <div class="card-header">
            <span>连接设置</span>
          </div>
        </template>
        <div class="setting-item">
          <el-form-item label="后端服务地址">
            <el-input
              v-model="localSettings.apiBaseUrl"
              placeholder="http://localhost:8081/api"
              @input="saveSettings"
            />
            <div class="setting-description">AI助手后端服务的地址</div>
          </el-form-item>
        </div>
        <div class="setting-item">
          <el-form-item label="连接超时时间 (秒)">
            <el-input-number
              v-model="localSettings.timeout"
              :min="5"
              :max="60"
              @change="saveSettings"
            />
            <div class="setting-description">网络请求的超时时间</div>
          </el-form-item>
        </div>
      </el-card>

      <!-- 系统信息 -->
      <el-card class="settings-section">
        <template #header>
          <div class="card-header">
            <span>系统信息</span>
          </div>
        </template>
        <el-descriptions :column="1" size="small" border>
          <el-descriptions-item label="连接状态">
            <el-tag :type="connectionStatus.type">{{ connectionStatus.text }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="版本">v1.0.0</el-descriptions-item>
          <el-descriptions-item label="最后检查">{{ lastHealthCheck || '未检查' }}</el-descriptions-item>
        </el-descriptions>
        <div style="margin-top: 16px;">
          <el-button 
            @click="checkHealth" 
            :loading="checking" 
            type="primary" 
            style="width: 100%"
          >
            <el-icon><Search /></el-icon>
            {{ checking ? '检查中...' : '检查连接' }}
          </el-button>
        </div>
      </el-card>

      <!-- 数据管理 -->
      <el-card class="settings-section">
        <template #header>
          <div class="card-header">
            <span>数据管理</span>
          </div>
        </template>
        <div class="data-actions">
          <el-button @click="exportData" type="success">
            <el-icon><Upload /></el-icon>
            导出设置
          </el-button>
          <el-button @click="importData" type="primary">
            <el-icon><Download /></el-icon>
            导入设置
          </el-button>
          <el-button @click="resetSettings" type="warning">
            <el-icon><Refresh /></el-icon>
            重置设置
          </el-button>
        </div>
        <input
          ref="fileInput"
          type="file"
          accept=".json"
          style="display: none"
          @change="handleFileImport"
        />
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch } from 'vue'
import { Close, Search, Upload, Download, Refresh } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { checkServiceHealth } from '../api'
import { formatTime } from '../utils'

// 定义Props
interface Settings {
  userId: string
  userName: string
  streamMode: boolean
  autoScroll: boolean
  showTimestamp: boolean
  apiBaseUrl: string
  timeout: number
}

interface Props {
  settings?: Settings
}

const props = withDefaults(defineProps<Props>(), {
  settings: () => ({
    userId: 'default_user',
    userName: '用户',
    streamMode: true,
    autoScroll: true,
    showTimestamp: true,
    apiBaseUrl: 'http://localhost:8081/api',
    timeout: 60
  })
})

// 定义Emits
const emit = defineEmits<{
  (e: 'close'): void
  (e: 'settings-changed', settings: Settings): void
}>()

// 定义接口
interface ConnectionStatus {
  text: string
  type: '' | 'success' | 'warning' | 'danger'
}

interface FileImportEvent extends Event {
  target: HTMLInputElement & EventTarget
}

// 响应式状态
const localSettings = reactive<Settings>({
  ...props.settings
})

const checking = ref<boolean>(false)
const connectionStatus = ref<ConnectionStatus>({
  text: '未知',
  type: ''
})
const lastHealthCheck = ref<string | null>(null)
const fileInput = ref<HTMLInputElement | null>(null)

// 方法
function saveSettings(): void {
  // 保存到localStorage
  localStorage.setItem('ai-helper-settings', JSON.stringify(localSettings))
  // 通知父组件
  emit('settings-changed', localSettings)
}

async function checkHealth(): Promise<void> {
  checking.value = true
  try {
    const isHealthy = await checkServiceHealth()
    if (isHealthy) {
      connectionStatus.value = {
        text: '连接正常',
        type: 'success'
      }
    } else {
      connectionStatus.value = {
        text: '连接失败',
        type: 'danger'
      }
    }
    lastHealthCheck.value = formatTime(new Date())
  } catch (error) {
    connectionStatus.value = {
      text: '检查失败',
      type: 'danger'
    }
    console.error('健康检查失败:', error)
  } finally {
    checking.value = false
  }
}

function exportData(): void {
  const data = {
    settings: localSettings,
    exportTime: new Date().toISOString(),
    version: '1.0.0'
  }
  
  const blob = new Blob([JSON.stringify(data, null, 2)], {
    type: 'application/json'
  })
  
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `ai-helper-settings-${new Date().toISOString().split('T')[0]}.json`
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  URL.revokeObjectURL(url)
}

function importData(): void {
  if (fileInput.value) {
    fileInput.value.click()
  }
}

function handleFileImport(event: Event): void {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]
  if (!file) return

  const reader = new FileReader()
  reader.onload = (e) => {
    try {
      const result = e.target?.result
      if (typeof result === 'string') {
        const data = JSON.parse(result)
        if (data.settings) {
          Object.assign(localSettings, data.settings)
          saveSettings()
          ElMessage.success('设置导入成功')
        } else {
          ElMessage.error('无效的设置文件')
        }
      }
    } catch (error) {
      ElMessage.error('设置文件格式错误')
      console.error('导入设置失败:', error)
    }
  }
  reader.readAsText(file)
  
  // 重置文件输入
  target.value = ''
}

function resetSettings(): void {
  ElMessageBox.confirm(
    '确定要重置所有设置吗？这将清除您的个人配置。',
    '确认重置',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  )
  .then(() => {
    Object.assign(localSettings, {
      userId: 'default_user',
      userName: '用户',
      streamMode: true,
      autoScroll: true,
      showTimestamp: true,
      apiBaseUrl: 'http://localhost:8081/api',
      timeout: 60
    } as Settings)
    saveSettings()
    ElMessage.success('设置已重置')
  })
  .catch(() => {
    // 用户取消操作
  })
}

function loadSettings(): void {
  try {
    const saved = localStorage.getItem('ai-helper-settings')
    if (saved) {
      Object.assign(localSettings, JSON.parse(saved))
    }
  } catch (error) {
    console.error('加载设置失败:', error)
  }
}

// 生命周期钩子
onMounted(() => {
  loadSettings()
  checkHealth()
})

// 监听器
watch(() => props.settings, (newSettings) => {
  if (newSettings) {
    Object.assign(localSettings, newSettings)
  }
}, { deep: true })
</script>

<style scoped>
.settings-panel {
  background-color: white;
  border-radius: 8px;
  max-width: 600px;
  width: 100%;
  max-height: 80vh;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.settings-header {
  padding: 20px 24px;
  border-bottom: 1px solid #e1e5e9;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: #f8f9fa;
}

.settings-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: bold;
  color: #333;
}

.card-header {
  font-weight: 600;
  color: #333;
  font-size: 16px;
}

.settings-content {
  flex: 1;
  overflow-y: auto;
  padding: 20px 24px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.setting-item {
  margin-bottom: 20px;
}

.setting-item:last-child {
  margin-bottom: 0;
}

.setting-description {
  margin: 6px 0 0 0;
  font-size: 12px;
  color: #666;
  line-height: 1.4;
}

.data-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.data-actions :deep(.el-button) {
  flex: 1;
  min-width: 120px;
}

/* 滚动条样式 */
.settings-content::-webkit-scrollbar {
  width: 6px;
}

.settings-content::-webkit-scrollbar-track {
  background: #f1f1f1;
}

.settings-content::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.settings-content::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

@media (max-width: 768px) {
  .settings-panel {
    max-width: 95vw;
    max-height: 90vh;
  }

  .settings-content {
    padding: 16px 20px;
  }

  .data-actions {
    flex-direction: column;
  }

  .data-actions :deep(.el-button) {
    min-width: auto;
  }
}
</style>