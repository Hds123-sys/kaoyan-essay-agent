import { defineStore } from 'pinia'
import { ref } from 'vue'

// 生成UUID v4
function generateUUID() {
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
    const r = Math.random() * 16 | 0
    const v = c === 'x' ? r : (r & 0x3 | 0x8)
    return v.toString(16)
  })
}

export const useSessionStore = defineStore('session', () => {
  const sessionId = ref('')

  // 初始化时从localStorage读取
  const init = () => {
    const storedId = localStorage.getItem('sessionId')
    if (storedId) {
      sessionId.value = storedId
    } else {
      sessionId.value = generateUUID()
      localStorage.setItem('sessionId', sessionId.value)
    }
  }

  const setSessionId = (id) => {
    sessionId.value = id
    localStorage.setItem('sessionId', id)
  }

  const clearSession = () => {
    sessionId.value = ''
    localStorage.removeItem('sessionId')
  }

  return {
    sessionId,
    init,
    setSessionId,
    clearSession
  }
})