import request from './request'

export const sanitizeMarkdown = (markdown) => {
  return request({
    url: '/report/sanitize',
    method: 'post',
    data: { markdown }
  })
}