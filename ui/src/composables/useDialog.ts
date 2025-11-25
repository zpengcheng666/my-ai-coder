import { ref, Ref } from 'vue'

// 定义返回类型
export interface UseDialogReturn {
  showDocumentManager: Ref<boolean>
  showSettings: Ref<boolean>
  openDocumentManager: () => void
  closeDocumentManager: () => void
  openSettings: () => void
  closeSettings: () => void
}

/**
 * 对话框状态管理Composable
 * @returns 对话框相关的状态和方法
 */
export function useDialog(): UseDialogReturn {
  // 响应式状态
  const showDocumentManager = ref<boolean>(false)
  const showSettings = ref<boolean>(false)
  
  // 方法实现
  function openDocumentManager(): void {
    showDocumentManager.value = true
  }
  
  function closeDocumentManager(): void {
    showDocumentManager.value = false
  }
  
  function openSettings(): void {
    showSettings.value = true
  }
  
  function closeSettings(): void {
    showSettings.value = false
  }
  
  return {
    showDocumentManager,
    showSettings,
    openDocumentManager,
    closeDocumentManager,
    openSettings,
    closeSettings
  }
}