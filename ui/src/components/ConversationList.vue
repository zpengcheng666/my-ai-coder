<template>
  <div class="conversation-list">
    <div class="conversation-header">
      <h3>会话列表</h3>
      <el-button @click="createNewConversation" type="primary" plain>
        <el-icon><Plus /></el-icon>
        新建会话
      </el-button>
    </div>

    <div class="conversation-search">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索会话..."
        clearable
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
    </div>

    <div class="conversation-items">
      <el-card 
        v-for="conversation in filteredConversations"
        :key="conversation.conversationId"
        :class="['conversation-item', { active: currentConversationId === conversation.conversationId }]"
        @click="selectConversation(conversation)"
        shadow="hover"
      >
        <div class="conversation-info">
          <h4 class="conversation-title">{{ conversation.title || '未命名会话' }}</h4>
          <p class="conversation-time">{{ conversation.createTime ? formatTime(new Date(conversation.createTime)) : '' }}</p>
        </div>
        <div class="conversation-actions">
          <el-button @click.stop="deleteConversation(conversation)" type="danger" :icon="Delete" circle size="small" />
        </div>
      </el-card>
    </div>

    <div v-if="loading" class="loading">
      <el-skeleton :rows="3" animated />
    </div>

    <div v-if="errorMessage" class="error">
      <el-alert :title="errorMessage" type="error" show-icon />
    </div>

    <!-- 创建会话对话框 -->
    <el-dialog
      v-model="showCreateDialog"
      title="创建新会话"
      width="400px"
    >
      <el-input
        v-model="newConversationTitle"
        placeholder="请输入会话标题"
        @keydown.enter="confirmCreateConversation"
      />
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showCreateDialog = false">取消</el-button>
          <el-button type="primary" @click="confirmCreateConversation">创建</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { Plus, Search, Delete } from '@element-plus/icons-vue'
import { ElMessageBox } from 'element-plus'
import { 
  getUserConversations, 
  createConversation, 
  deleteConversation as deleteConversationApi 
} from '../api'
import { formatTime } from '../utils'
import type { Conversation } from '../types'

// 定义Props
interface Props {
  userId?: string
  currentConversationId?: string
}

const props = withDefaults(defineProps<Props>(), {
  userId: 'default_user',
  currentConversationId: undefined
})

// 定义Emits
const emit = defineEmits<{
  (e: 'conversation-selected', conversation: Conversation | null): void
  (e: 'conversation-created', conversation: { conversationId: string; title: string }): void
}>()

// 响应式状态
const conversations = ref<Conversation[]>([])
const loading = ref<boolean>(false)
const errorMessage = ref<string | null>(null)
const searchKeyword = ref<string>('')
const showCreateDialog = ref<boolean>(false)
const newConversationTitle = ref<string>('')

// 计算属性
const filteredConversations = computed<Conversation[]>(() => {
  if (!searchKeyword.value) {
    return conversations.value
  }
  return conversations.value.filter(conv =>
    conv.title && conv.title.toLowerCase().includes(searchKeyword.value.toLowerCase())
  )
})

// 方法
async function loadConversations(): Promise<void> {
  loading.value = true
  errorMessage.value = null
  try {
    const response = await getUserConversations(props.userId)
    conversations.value = response.conversations || []
  } catch (error) {
    errorMessage.value = '加载会话列表失败'
    console.error('加载会话列表失败:', error as Error)
  } finally {
    loading.value = false
  }
}

function selectConversation(conversation: Conversation): void {
  emit('conversation-selected', conversation)
}

function createNewConversation(): void {
  showCreateDialog.value = true
  newConversationTitle.value = ''
}

async function confirmCreateConversation(): Promise<void> {
  if (!newConversationTitle.value.trim()) {
    newConversationTitle.value = '新会话'
  }

  try {
    const response = await createConversation(props.userId, newConversationTitle.value)
    showCreateDialog.value = false
    newConversationTitle.value = ''
    
    // 创建成功后重新加载会话列表
    await loadConversations()
    
    // 通知父组件选择新创建的会话
    emit('conversation-created', {
      conversationId: response.conversationId,
      title: response.title
    })
  } catch (error) {
    errorMessage.value = '创建会话失败'
    console.error('创建会话失败:', error as Error)
  }
}

async function deleteConversation(conversation: Conversation): Promise<void> {
  if (!conversation || !conversation.conversationId) return
  
  try {
    await ElMessageBox.confirm(
      `确定要删除会话"${conversation.title}"吗？`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )
    
    await deleteConversationApi(conversation.conversationId, props.userId)
    // 成功后刷新列表
    await loadConversations()
    // 如果删除的是当前会话，通知父组件清空选择
    if (props.currentConversationId === conversation.conversationId) {
      emit('conversation-selected', null)
    }
  } catch (error) {
    if (error !== 'cancel') {
      errorMessage.value = '删除会话失败'
      console.error('删除会话失败:', error as Error)
    }
  }
}

// 生命周期钩子
onMounted(() => {
  loadConversations()
})

// 监听器
watch(() => props.userId, () => {
  loadConversations()
})
</script>

<style scoped>
.conversation-list {
  width: 300px;
  height: 100%;
  background-color: #f8f9fa;
  border-right: 1px solid #e1e5e9;
  display: flex;
  flex-direction: column;
}

.conversation-header {
  padding: 20px;
  border-bottom: 1px solid #e1e5e9;
  background-color: white;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.conversation-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: bold;
  color: #333;
}

.conversation-search {
  padding: 15px 20px;
  background-color: white;
  border-bottom: 1px solid #e1e5e9;
}

:deep(.el-input__wrapper) {
  border-radius: 20px;
}

.conversation-items {
  flex: 1;
  overflow-y: auto;
  padding: 10px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.conversation-item {
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid #ebeef5;
  border-radius: 8px;
}

.conversation-item:hover {
  border-color: #409eff;
}

.conversation-item.active {
  border-color: #409eff;
  background-color: #ecf5ff;
}

.conversation-info {
  flex: 1;
  padding: 10px;
}

.conversation-title {
  margin: 0 0 5px 0;
  font-size: 14px;
  font-weight: 500;
  color: #333;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.conversation-time {
  margin: 0;
  font-size: 12px;
  color: #666;
}

.conversation-actions {
  display: flex;
  align-items: center;
  padding: 0 10px 10px;
}

.loading {
  padding: 20px;
}

.error {
  padding: 20px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

/* 滚动条样式 */
.conversation-items::-webkit-scrollbar {
  width: 6px;
}

.conversation-items::-webkit-scrollbar-track {
  background: #f1f1f1;
}

.conversation-items::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.conversation-items::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

@media (max-width: 768px) {
  .conversation-list {
    width: 100%;
    height: auto;
    max-height: 40vh;
    border-right: none;
    border-bottom: 1px solid #e1e5e9;
  }
}
</style>