import { ref, Ref } from 'vue'

// 定义返回类型
export interface UseSidebarReturn {
  sidebarCollapsed: Ref<boolean>
  toggleSidebar: () => void
}

/**
 * 侧边栏状态管理Composable
 * @returns 侧边栏相关的状态和方法
 */
export function useSidebar(): UseSidebarReturn {
  // 响应式状态
  const sidebarCollapsed = ref<boolean>(false)
  
  // 方法实现
  function toggleSidebar(): void {
    sidebarCollapsed.value = !sidebarCollapsed.value
  }
  
  return {
    sidebarCollapsed,
    toggleSidebar
  }
}