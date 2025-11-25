<template>
  <div class="chat-input">
    <div class="input-container">
      <el-input
        ref="inputRef"
        v-model="inputMessage"
        :placeholder="placeholder"
        :disabled="disabled"
        type="textarea"
        :autosize="{ minRows: 1, maxRows: 6 }"
        @keydown="handleKeyDown"
      />
      <el-button
        :disabled="disabled || !inputMessage.trim()"
        @click="sendMessage"
        type="primary"
        circle
      >
        <el-icon><Promotion /></el-icon>
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick, onMounted } from 'vue'
import { Promotion } from '@element-plus/icons-vue'

// 定义Props
interface Props {
  disabled?: boolean
  placeholder?: string
}

const props = withDefaults(defineProps<Props>(), {
  disabled: false,
  placeholder: '请输入您的问题...'
})

// 定义Emits
const emit = defineEmits<{
  (e: 'send-message', message: string): void
}>()

// 响应式状态
const inputMessage = ref<string>('')
const inputRef = ref<HTMLTextAreaElement | null>(null)

// 方法
function sendMessage(): void {
  if (inputMessage.value.trim() && !props.disabled) {
    emit('send-message', inputMessage.value.trim())
    inputMessage.value = ''
  }
}

function handleKeyDown(event: KeyboardEvent): void {
  if (event.key === 'Enter' && !event.shiftKey) {
    event.preventDefault()
    sendMessage()
  }
}

function focus(): void {
  inputRef.value?.focus()
}

// 生命周期钩子
onMounted(() => {
  // 组件挂载后聚焦输入框
  nextTick(() => {
    focus()
  })
})

// 定义暴露给父组件的方法
defineExpose({
  focus
})
</script>

<style scoped>
.chat-input {
  padding: 20px;
  background-color: white;
  border-top: 1px solid #e1e5e9;
}

.input-container {
  display: flex;
  align-items: flex-end;
  gap: 12px;
  max-width: 800px;
  margin: 0 auto;
}

:deep(.el-textarea__inner) {
  border-radius: 24px;
  padding: 12px 16px;
  font-size: 14px;
  line-height: 1.4;
}

:deep(.el-button) {
  width: 44px;
  height: 44px;
  transition: background-color 0.2s;
}

@media (max-width: 768px) {
  .chat-input {
    padding: 15px;
  }
  
  .input-container {
    gap: 8px;
  }
  
  :deep(.el-textarea__inner) {
    font-size: 16px; /* 防止在移动设备上自动缩放 */
  }
}
</style>