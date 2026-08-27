import request from './request'

export const clearContext = () => {
  // 从session store获取sessionId
  const sessionId = localStorage.getItem('essay_session_id')
  if (!sessionId) {
    return Promise.reject(new Error('会话ID不存在'))
  }
  return request({
    url: `/session/${sessionId}`,
    method: 'delete'
  })
}