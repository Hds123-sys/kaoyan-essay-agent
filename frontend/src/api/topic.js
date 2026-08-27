import request from './request'

export const generateTopic = (essayType) => {
  return request({
    url: '/topic/generate',
    method: 'post',
    data: {
      essay_type: essayType
    }
  })
}