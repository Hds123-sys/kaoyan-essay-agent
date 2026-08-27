import request from './request'

export const generateTopic = (essayType) => {
  return request({
    url: '/generate-topic',
    method: 'post',
    data: {
      essay_type: essayType,
      difficulty: '中等',
      keywords: []
    }
  })
}