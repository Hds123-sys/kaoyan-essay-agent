import request from './request'

export const correct = (data) => {
  return request({
    url: '/essay/correct',
    method: 'post',
    data: {
      user_essay: data.userEssay,
      essay_type: data.essayType,
      topic: data.topic || null,
      image_url: data.imageUrl || null,
      is_heavily_edited: data.isHeavilyEdited || false
    }
  })
}

export const getReference = (topic, essayType) => {
  return request({
    url: '/essay/reference',
    method: 'post',
    data: {
      topic,
      essay_type: essayType
    }
  })
}

export const reCorrect = (data) => {
  return request({
    url: '/essay/re-correct',
    method: 'post',
    data: {
      record_id: data.recordId,
      topic: data.topic || null,
      user_essay: data.userEssay || null,
      essay_type: data.essayType || null
    }
  })
}