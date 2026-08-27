import request from './request'

export const generateTopic = (essayType, difficulty, keywords) => {
  return request({
    url: '/generate-topic',
    method: 'post',
    data: {
      essayType,
      difficulty,
      keywords
    }
  })
}