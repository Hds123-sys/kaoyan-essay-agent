import request from './request'

export const clearContext = () => {
  return request({
    url: '/session/clear',
    method: 'post'
  })
}