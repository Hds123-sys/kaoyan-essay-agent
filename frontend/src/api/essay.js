import request from './request'

export const correct = (data) => {
  return request({
    url: '/correct',
    method: 'post',
    data: {
      topic: data.topic || '',
      userEssay: data.userEssay,
      essayType: data.essayType,
      imageUrl: data.imageUrl || null,
      templateId: data.templateId || null
    },
    timeout: 60000 // 批改接口需要更长时间
  })
}

export const getReference = (topic, essayType) => {
  // 后端暂未实现reference接口，返回模拟数据
  return Promise.resolve({
    reference_essay: '参考范文功能暂未实现，请直接提交作文进行批改',
    word_count: 0,
    highlights: [],
    degraded: true
  })
}

export const reCorrect = (data) => {
  // 后端暂未实现re-correct接口，使用correct接口
  return correct({
    topic: data.topic,
    userEssay: data.userEssay,
    essayType: data.essayType,
    imageUrl: data.imageUrl
  })
}