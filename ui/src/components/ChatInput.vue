<template>
  <div class="chat-input">
    <div class="input-container">
      <textarea
        ref="inputRef"
        v-model="inputMessage"
        :placeholder="placeholder"
        :disabled="disabled"
        class="input-textarea"
        rows="1"
        @keydown="handleKeyDown"
        @input="adjustHeight"
      />
      <button
        :disabled="disabled || !inputMessage.trim()"
        @click="sendMessage"
        class="send-button"
      >
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path d="M2 21l21-9L2 3v7l15 2-15 2v7z" fill="currentColor"/>
        </svg>
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick, onMounted } from 'vue'

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
    adjustHeight()
  }
}

function handleKeyDown(event: KeyboardEvent): void {
  if (event.key === 'Enter' && !event.shiftKey) {
    event.preventDefault()
    sendMessage()
  }
}

function adjustHeight(): void {
  nextTick(() => {
    const textarea = inputRef.value
    if (textarea) {
      textarea.style.height = 'auto'
      textarea.style.height = Math.min(textarea.scrollHeight, 120) + 'px'
    }
  })
}

function focus(): void {
  inputRef.value?.focus()
}

// 生命周期钩子
onMounted(() => {
  adjustHeight()
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

.input-textarea {
  flex: 1;
  padding: 12px 16px;
  border: 1px solid #ddd;
  border-radius: 24px;
  font-size: 14px;
  line-height: 1.4;
  resize: none;
  outline: none;
  transition: border-color 0.2s;
  min-height: 44px;
  max-height: 120px;
  overflow-y: auto;
}

.input-textarea:focus {
  border-color: #007bff;
}

.input-textarea:disabled {
  background-color: #f5f5f5;
  color: #999;
  cursor: not-allowed;
}

.send-button {
  width: 44px;
  height: 44px;
  background-color: #007bff;
  border: none;
  border-radius: 50%;
  color: white;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background-color 0.2s;
  flex-shrink: 0;
}

.send-button:hover:not(:disabled) {
  background-color: #0056b3;
}

.send-button:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

@media (max-width: 768px) {
  .chat-input {
    padding: 15px;
  }
  
  .input-container {
    gap: 8px;
  }
  
  .input-textarea {
    font-size: 16px; /* 防止在移动设备上自动缩放 */
  }
}
</style>