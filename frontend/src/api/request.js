import axios from 'axios'
import { useSessionStore } from '@/stores/session'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
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
      return ElMessageBox.confirm(
        '会话已过期，是否重新开始？',
        '会话过期',
        {
          confirmButtonText: '重新开始',
          cancelButtonText: '取消',
          type: 'warning'
        }
      ).then(() => {
        const sessionStore = useSessionStore()
        sessionStore.clearSession()
        localStorage.clear()
        window.location.reload()
        return Promise.reject(new Error('Session expired'))
      }).catch(() => {
        return Promise.reject(new Error('Session expired'))
      })
    }

    const res = response.data
    if (res.code !== 0) {
      return Promise.reject(new Error(res.message || '请求失败'))
    }

    return res.data
  },
  error => {
    if (error.response) {
      const status = error.response.status
      const message = error.response.data?.message

      switch (status) {
        case 429:
          if (message?.includes('42902')) {
            ElMessage.warning('当前有批改任务正在处理，请稍候')
          } else {
            ElMessage.warning('请求过于频繁，请稍后重试')
          }
          break
        case 400:
          ElMessage.error(message || '参数校验失败')
          break
        case 403:
          ElMessage.error('无权访问该记录')
          break
        case 500:
          ElMessage.error('服务异常，请稍后重试')
          break
        default:
          ElMessage.error(message || '请求失败')
      }
    } else if (error.code === 'ECONNABORTED') {
      ElMessage.error('网络超时，请检查连接')
    } else {
      ElMessage.error('网络错误，请检查连接')
    }

    return Promise.reject(error)
  }
)

export default request