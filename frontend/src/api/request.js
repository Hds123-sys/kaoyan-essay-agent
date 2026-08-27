import axios from 'axios'
import { useSessionStore } from '@/stores/session'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: '/api',
  timeout: 30000
})

// 生成UUID v4
function generateUUID() {
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
    const r = Math.random() * 16 | 0
    const v = c === 'x' ? r : (r & 0x3 | 0x8)
    return v.toString(16)
  })
}

// 请求拦截器
request.interceptors.request.use(
  config => {
    const sessionStore = useSessionStore()
    let sessionId = sessionStore.sessionId

    if (!sessionId) {
      sessionId = generateUUID()
      sessionStore.setSessionId(sessionId)
    }

    config.headers['X-Session-Id'] = sessionId
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    const sessionStatus = response.headers['x-session-status']

    if (sessionStatus === 'expired') {
      const sessionStore = useSessionStore()
      sessionStore.clearSession()
      ElMessage.error('会话已过期，请重新开始')
      setTimeout(() => {
        window.location.reload()
      }, 1500)
      return Promise.reject(new Error('Session expired'))
    }

    const res = response.data
    if (res.code !== 0) {
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || '请求失败'))
    }

    return res.data
  },
  error => {
    const message = error.response?.data?.message || error.message || '请求失败'
    ElMessage.error(message)
    return Promise.reject(error)
  }
)

export default request