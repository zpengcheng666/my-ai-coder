<template>
  <div class="settings-panel">
    <!-- 头部 -->
    <div class="settings-header">
      <div class="header-title-wrapper">
        <el-icon class="header-icon"><Setting /></el-icon>
        <h3>系统设置</h3>
      </div>
      <el-button 
        @click="$emit('close')" 
        :icon="Close" 
        circle 
        class="close-btn"
        text
      />
    </div>

    <div class="settings-content">
      <!-- 用户设置 -->
      <el-card class="settings-section" shadow="hover">
        <template #header>
          <div class="card-header">
            <el-icon class="section-icon"><User /></el-icon>
            <span class="section-title">用户设置</span>
          </div>
        </template>
        <div class="setting-item">
          <div class="setting-label-wrapper">
            <el-icon class="label-icon"><UserFilled /></el-icon>
            <label class="setting-label">用户ID</label>
          </div>
          <el-input
            v-model="localSettings.userId"
            placeholder="输入用户ID"
            @input="saveSettings"
            clearable
            class="setting-input"
          >
            <template #prefix>
              <el-icon><Key /></el-icon>
            </template>
          </el-input>
          <div class="setting-description">
            <el-icon class="desc-icon"><InfoFilled /></el-icon>
            用于标识您的身份，会话记录将与此ID关联
          </div>
        </div>
        <el-divider />
        <div class="setting-item">
          <div class="setting-label-wrapper">
            <el-icon class="label-icon"><Avatar /></el-icon>
            <label class="setting-label">用户名称</label>
          </div>
          <el-input
            v-model="localSettings.userName"
            placeholder="输入用户名称"
            @input="saveSettings"
            clearable
            class="setting-input"
          >
            <template #prefix>
              <el-icon><Edit /></el-icon>
            </template>
          </el-input>
          <div class="setting-description">
            <el-icon class="desc-icon"><InfoFilled /></el-icon>
            显示在界面上的用户名称
          </div>
        </div>
      </el-card>

      <!-- 聊天设置 -->
      <el-card class="settings-section" shadow="hover">
        <template #header>
          <div class="card-header">
            <el-icon class="section-icon"><ChatDotRound /></el-icon>
            <span class="section-title">聊天设置</span>
          </div>
        </template>
        <div class="setting-item">
          <div class="setting-label-wrapper">
            <el-icon class="label-icon"><VideoPlay /></el-icon>
            <label class="setting-label">启用流式输出</label>
          </div>
          <div class="switch-wrapper">
            <el-switch
              v-model="localSettings.streamMode"
              @change="saveSettings"
              active-text="开启"
              inactive-text="关闭"
            />
            <span class="switch-status">{{ localSettings.streamMode ? '已开启' : '已关闭' }}</span>
          </div>
          <div class="setting-description">
            <el-icon class="desc-icon"><InfoFilled /></el-icon>
            AI回复时实时显示内容（推荐开启）
          </div>
        </div>
        <el-divider />
        <div class="setting-item">
          <div class="setting-label-wrapper">
            <el-icon class="label-icon"><ArrowDownBold /></el-icon>
            <label class="setting-label">自动滚动到底部</label>
          </div>
          <div class="switch-wrapper">
            <el-switch
              v-model="localSettings.autoScroll"
              @change="saveSettings"
              active-text="开启"
              inactive-text="关闭"
            />
            <span class="switch-status">{{ localSettings.autoScroll ? '已开启' : '已关闭' }}</span>
          </div>
          <div class="setting-description">
            <el-icon class="desc-icon"><InfoFilled /></el-icon>
            新消息到达时自动滚动到聊天底部
          </div>
        </div>
        <el-divider />
        <div class="setting-item">
          <div class="setting-label-wrapper">
            <el-icon class="label-icon"><Clock /></el-icon>
            <label class="setting-label">显示消息时间</label>
          </div>
          <div class="switch-wrapper">
            <el-switch
              v-model="localSettings.showTimestamp"
              @change="saveSettings"
              active-text="开启"
              inactive-text="关闭"
            />
            <span class="switch-status">{{ localSettings.showTimestamp ? '已开启' : '已关闭' }}</span>
          </div>
          <div class="setting-description">
            <el-icon class="desc-icon"><InfoFilled /></el-icon>
            在消息中显示发送时间
          </div>
        </div>
      </el-card>

      <!-- 连接设置 -->
      <el-card class="settings-section" shadow="hover">
        <template #header>
          <div class="card-header">
            <el-icon class="section-icon"><Connection /></el-icon>
            <span class="section-title">连接设置</span>
          </div>
        </template>
        <div class="setting-item">
          <div class="setting-label-wrapper">
            <el-icon class="label-icon"><Link /></el-icon>
            <label class="setting-label">后端服务地址</label>
          </div>
          <el-input
            v-model="localSettings.apiBaseUrl"
            placeholder="http://localhost:8081/api"
            @input="saveSettings"
            clearable
            class="setting-input"
          >
            <template #prefix>
              <el-icon><Position /></el-icon>
            </template>
          </el-input>
          <div class="setting-description">
            <el-icon class="desc-icon"><InfoFilled /></el-icon>
            AI助手后端服务的地址
          </div>
        </div>
        <el-divider />
        <div class="setting-item">
          <div class="setting-label-wrapper">
            <el-icon class="label-icon"><Timer /></el-icon>
            <label class="setting-label">连接超时时间</label>
          </div>
          <div class="timeout-wrapper">
            <el-input-number
              v-model="localSettings.timeout"
              :min="5"
              :max="60"
              :step="5"
              @change="saveSettings"
              class="timeout-input"
            />
            <span class="timeout-unit">秒</span>
          </div>
          <div class="setting-description">
            <el-icon class="desc-icon"><InfoFilled /></el-icon>
            网络请求的超时时间（范围：5-60秒）
          </div>
        </div>
      </el-card>

      <!-- 系统信息 -->
      <el-card class="settings-section" shadow="hover">
        <template #header>
          <div class="card-header">
            <el-icon class="section-icon"><Monitor /></el-icon>
            <span class="section-title">系统信息</span>
          </div>
        </template>
        <div class="system-info">
          <el-descriptions :column="1" size="default" border class="info-descriptions">
            <el-descriptions-item label="连接状态">
              <div class="status-wrapper">
                <el-tag 
                  :type="connectionStatus.type || 'info'" 
                  effect="dark"
                  size="large"
                  class="status-tag"
                >
                  <el-icon class="status-icon">
                    <component :is="connectionStatus.type === 'success' ? 'CircleCheck' : connectionStatus.type === 'danger' ? 'CircleClose' : 'QuestionFilled'" />
                  </el-icon>
                  {{ connectionStatus.text || '未知' }}
                </el-tag>
              </div>
            </el-descriptions-item>
            <el-descriptions-item label="后端地址">
              <div class="info-value">
                <el-icon class="value-icon"><Position /></el-icon>
                <span class="value-text">{{ localSettings.apiBaseUrl || '未设置' }}</span>
              </div>
            </el-descriptions-item>
            <el-descriptions-item label="超时时间">
              <div class="info-value">
                <el-icon class="value-icon"><Timer /></el-icon>
                <span class="value-text">{{ localSettings.timeout || 60 }} 秒</span>
              </div>
            </el-descriptions-item>
            <el-descriptions-item label="版本信息">
              <div class="version-info">
                <el-tag type="info" effect="plain" size="default">v1.0.0</el-tag>
              </div>
            </el-descriptions-item>
            <el-descriptions-item label="最后检查">
              <div class="check-time">
                <el-icon class="time-icon"><Clock /></el-icon>
                <span>{{ lastHealthCheck || '未检查' }}</span>
              </div>
            </el-descriptions-item>
          </el-descriptions>
          <div class="health-check-action">
            <el-button 
              @click="checkHealth" 
              :loading="checking" 
              type="primary" 
              size="large"
              class="health-check-btn"
            >
              <el-icon v-if="!checking"><Search /></el-icon>
              {{ checking ? '检查中...' : '检查连接状态' }}
            </el-button>
          </div>
        </div>
      </el-card>

      <!-- 当前设置汇总 -->
      <el-card class="settings-section" shadow="hover">
        <template #header>
          <div class="card-header">
            <el-icon class="section-icon"><List /></el-icon>
            <span class="section-title">当前设置汇总</span>
          </div>
        </template>
        <div class="settings-summary">
          <el-descriptions :column="2" size="default" border class="summary-descriptions">
            <el-descriptions-item label="用户ID">
              <span class="summary-value">{{ localSettings.userId || '未设置' }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="用户名称">
              <span class="summary-value">{{ localSettings.userName || '未设置' }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="流式输出">
              <el-tag :type="localSettings.streamMode ? 'success' : 'info'" size="small">
                {{ localSettings.streamMode ? '已开启' : '已关闭' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="自动滚动">
              <el-tag :type="localSettings.autoScroll ? 'success' : 'info'" size="small">
                {{ localSettings.autoScroll ? '已开启' : '已关闭' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="显示时间">
              <el-tag :type="localSettings.showTimestamp ? 'success' : 'info'" size="small">
                {{ localSettings.showTimestamp ? '已开启' : '已关闭' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="超时时间">
              <span class="summary-value">{{ localSettings.timeout || 60 }} 秒</span>
            </el-descriptions-item>
          </el-descriptions>
        </div>
      </el-card>

      <!-- 数据管理 -->
      <el-card class="settings-section" shadow="hover">
        <template #header>
          <div class="card-header">
            <el-icon class="section-icon"><FolderOpened /></el-icon>
            <span class="section-title">数据管理</span>
          </div>
        </template>
        <div class="data-actions">
          <el-button 
            @click="exportData" 
            type="success" 
            size="large"
            class="action-btn export-btn"
          >
            <el-icon><Upload /></el-icon>
            <span>导出设置</span>
            <div class="btn-desc">备份当前配置</div>
          </el-button>
          <el-button 
            @click="importData" 
            type="primary" 
            size="large"
            class="action-btn import-btn"
          >
            <el-icon><Download /></el-icon>
            <span>导入设置</span>
            <div class="btn-desc">恢复配置</div>
          </el-button>
          <el-button 
            @click="resetSettings" 
            type="warning" 
            size="large"
            class="action-btn reset-btn"
          >
            <el-icon><Refresh /></el-icon>
            <span>重置设置</span>
            <div class="btn-desc">恢复默认值</div>
          </el-button>
        </div>
        <input
          ref="fileInput"
          type="file"
          accept=".json"
          style="display: none"
          @change="handleFileImport"
        />
        <div class="data-info">
          <el-alert
            title="数据管理提示"
            type="info"
            :closable="false"
            show-icon
          >
            <template #default>
              <div class="alert-content">
                <p>• 导出设置：将当前所有配置保存为JSON文件</p>
                <p>• 导入设置：从JSON文件恢复之前的配置</p>
                <p>• 重置设置：将所有设置恢复为默认值</p>
              </div>
            </template>
          </el-alert>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch } from 'vue'
import { 
  Close, 
  Search, 
  Upload, 
  Download, 
  Refresh,
  Setting,
  User,
  UserFilled,
  Key,
  Avatar,
  Edit,
  InfoFilled,
  ChatDotRound,
  VideoPlay,
  ArrowDownBold,
  Clock,
  Connection,
  Link,
  Position,
  Timer,
  Monitor,
  CircleCheck,
  CircleClose,
  QuestionFilled,
  FolderOpened,
  List
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { checkServiceHealth } from '../api'
import { formatTime } from '../utils'
import { getUserSettings, saveUserSettings } from '../api/modules/user-setting'

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
    userId: '',
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
async function saveSettings(): Promise<void> {
  try {
    // 保存到localStorage
    localStorage.setItem('ai-helper-settings', JSON.stringify(localSettings))
    
    // 如果有用户ID，则同时保存到后端
    if (localSettings.userId) {
      await saveUserSettings(localSettings.userId, localSettings)
    }
    
    // 通知父组件
    emit('settings-changed', localSettings)
    
    ElMessage.success('设置已保存')
  } catch (error) {
    ElMessage.error('设置保存失败')
    console.error('保存设置失败:', error)
  }
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
  try {
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
    ElMessage.success('设置导出成功')
  } catch (error) {
    ElMessage.error('设置导出失败')
    console.error('导出设置失败:', error)
  }
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
  reader.onload = async (e) => {
    try {
      const result = e.target?.result
      if (typeof result === 'string') {
        const data = JSON.parse(result)
        if (data.settings) {
          Object.assign(localSettings, data.settings)
          await saveSettings()
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
  .then(async () => {
    Object.assign(localSettings, {
      userId: '',
      userName: '用户',
      streamMode: true,
      autoScroll: true,
      showTimestamp: true,
      apiBaseUrl: 'http://localhost:8081/api',
      timeout: 60
    } as Settings)
    await saveSettings()
    ElMessage.success('设置已重置')
  })
  .catch(() => {
    // 用户取消操作
  })
}

async function loadSettings(): Promise<void> {
  try {
    // 尝试从localStorage加载
    const saved = localStorage.getItem('ai-helper-settings')
    if (saved) {
      Object.assign(localSettings, JSON.parse(saved))
    }
    
    // 如果有用户ID，尝试从后端加载设置
    if (localSettings.userId) {
      try {
        const response = await getUserSettings(localSettings.userId)
        if (response.data) {
          Object.assign(localSettings, response.data)
        }
      } catch (error) {
        console.warn('从后端加载用户设置失败:', error)
        // 如果后端加载失败，继续使用localStorage或默认设置
      }
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
  background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
  border-radius: 12px;
  max-width: 800px;
  width: 100%;
  max-height: 85vh;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12);
}

/* 头部样式 */
.settings-header {
  padding: 24px 28px;
  border-bottom: 2px solid #e8ecf0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.header-title-wrapper {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-icon {
  font-size: 24px;
  color: white;
}

.settings-header h3 {
  margin: 0;
  font-size: 22px;
  font-weight: 600;
  color: white;
  letter-spacing: 0.5px;
}

.close-btn {
  color: white;
  font-size: 20px;
  transition: all 0.3s ease;
}

.close-btn:hover {
  background-color: rgba(255, 255, 255, 0.2);
  transform: rotate(90deg);
}

/* 内容区域 */
.settings-content {
  flex: 1;
  overflow-y: auto;
  padding: 24px 28px;
  display: flex;
  flex-direction: column;
  gap: 24px;
  background-color: #f5f7fa;
}

/* 卡片样式 */
.settings-section {
  border-radius: 12px;
  border: 1px solid #e4e7ed;
  transition: all 0.3s ease;
  background: white;
}

.settings-section:hover {
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  transform: translateY(-2px);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 10px;
  font-weight: 600;
  color: #303133;
  font-size: 17px;
}

.section-icon {
  font-size: 20px;
  color: #667eea;
}

.section-title {
  font-weight: 600;
  color: #303133;
}

/* 设置项样式 */
.setting-item {
  margin-bottom: 24px;
}

.setting-item:last-child {
  margin-bottom: 0;
}

.setting-label-wrapper {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}

.label-icon {
  font-size: 18px;
  color: #667eea;
}

.setting-label {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}

.setting-input {
  margin-bottom: 8px;
}

.setting-input :deep(.el-input__wrapper) {
  border-radius: 8px;
  box-shadow: 0 0 0 1px #dcdfe6 inset;
  transition: all 0.3s ease;
}

.setting-input :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #c0c4cc inset;
}

.setting-input :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #667eea inset;
}

.setting-description {
  display: flex;
  align-items: flex-start;
  gap: 6px;
  margin-top: 8px;
  font-size: 13px;
  color: #909399;
  line-height: 1.6;
  padding: 8px 12px;
  background-color: #f5f7fa;
  border-radius: 6px;
  border-left: 3px solid #e4e7ed;
}

.desc-icon {
  font-size: 14px;
  color: #909399;
  margin-top: 2px;
  flex-shrink: 0;
}

/* 开关样式 */
.switch-wrapper {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.switch-status {
  font-size: 14px;
  font-weight: 500;
  color: #606266;
  padding: 4px 12px;
  background-color: #f0f2f5;
  border-radius: 6px;
}

/* 超时设置样式 */
.timeout-wrapper {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.timeout-input {
  width: 150px;
}

.timeout-input :deep(.el-input__wrapper) {
  border-radius: 8px;
}

.timeout-unit {
  font-size: 14px;
  color: #606266;
  font-weight: 500;
}

/* 系统信息样式 */
.system-info {
  padding: 8px 0;
}

.info-descriptions {
  margin-bottom: 20px;
}

.info-descriptions :deep(.el-descriptions__label) {
  font-weight: 600;
  color: #606266;
  width: 120px;
}

.info-descriptions :deep(.el-descriptions__content) {
  color: #303133;
}

.status-wrapper {
  display: flex;
  align-items: center;
}

.status-tag {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 14px;
  font-weight: 500;
}

.status-icon {
  font-size: 16px;
}

.info-value {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #303133;
}

.value-icon {
  font-size: 16px;
  color: #909399;
}

.value-text {
  font-size: 14px;
  word-break: break-all;
}

.version-info {
  display: flex;
  align-items: center;
}

.check-time {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #606266;
}

.time-icon {
  font-size: 16px;
}

/* 设置汇总样式 */
.settings-summary {
  padding: 8px 0;
}

.summary-descriptions {
  margin-bottom: 0;
}

.summary-descriptions :deep(.el-descriptions__label) {
  font-weight: 600;
  color: #606266;
  width: 100px;
}

.summary-descriptions :deep(.el-descriptions__content) {
  color: #303133;
}

.summary-value {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
}

.health-check-action {
  margin-top: 20px;
}

.health-check-btn {
  width: 100%;
  height: 44px;
  font-size: 15px;
  font-weight: 500;
  border-radius: 8px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  transition: all 0.3s ease;
}

.health-check-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

/* 数据管理按钮样式 */
.data-actions {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}

.data-info {
  margin-top: 20px;
}

.alert-content {
  font-size: 13px;
  line-height: 1.8;
}

.alert-content p {
  margin: 4px 0;
  color: #606266;
}

.action-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  height: 100px;
  padding: 16px;
  border-radius: 10px;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
  border: none !important;
  cursor: pointer;
}

.action-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
  transition: left 0.5s ease;
}

.action-btn:hover::before {
  left: 100%;
}

.action-btn:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
}

.action-btn :deep(.el-icon) {
  font-size: 28px;
  margin-bottom: 4px;
  color: white !important;
}

.action-btn span {
  font-size: 15px;
  font-weight: 600;
  color: white !important;
}

.btn-desc {
  font-size: 12px;
  opacity: 0.9;
  margin-top: 4px;
  color: white !important;
}

.export-btn {
  background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
  border: none;
  color: white;
}

.export-btn:hover {
  box-shadow: 0 8px 24px rgba(17, 153, 142, 0.4);
}

.import-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  color: white;
}

.import-btn:hover {
  box-shadow: 0 8px 24px rgba(102, 126, 234, 0.4);
}

.reset-btn {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  border: none;
  color: white;
}

.reset-btn:hover {
  box-shadow: 0 8px 24px rgba(245, 87, 108, 0.4);
}

/* 分割线样式 */
:deep(.el-divider) {
  margin: 20px 0;
  border-color: #e4e7ed;
}

/* 滚动条样式 */
.settings-content::-webkit-scrollbar {
  width: 8px;
}

.settings-content::-webkit-scrollbar-track {
  background: #f1f3f5;
  border-radius: 4px;
}

.settings-content::-webkit-scrollbar-thumb {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 4px;
}

.settings-content::-webkit-scrollbar-thumb:hover {
  background: linear-gradient(135deg, #5568d3 0%, #6a3f8f 100%);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .settings-panel {
    max-width: 95vw;
    max-height: 90vh;
    border-radius: 8px;
  }

  .settings-header {
    padding: 20px 20px;
  }

  .settings-header h3 {
    font-size: 18px;
  }

  .settings-content {
    padding: 20px;
    gap: 20px;
  }

  .data-actions {
    grid-template-columns: 1fr;
    gap: 12px;
  }

  .action-btn {
    height: 80px;
  }

  .timeout-wrapper {
    flex-direction: column;
    align-items: flex-start;
  }

  .timeout-input {
    width: 100%;
  }
}

@media (max-width: 480px) {
  .settings-header {
    padding: 16px;
  }

  .settings-content {
    padding: 16px;
  }

  .card-header {
    font-size: 15px;
  }

  .setting-label {
    font-size: 14px;
  }

  .action-btn {
    height: 70px;
  }

  .action-btn :deep(.el-icon) {
    font-size: 24px;
  }
}
</style>