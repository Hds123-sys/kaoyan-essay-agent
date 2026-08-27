import request from './request'

export const correct = (data) => {
  return request({
    url: '/correct',
    method: 'post',
    data
  })
}

export const getReference = (data) => {
  return request({
    url: '/reference',
    method: 'post',
    data
  })
}

export const reCorrect = (data) => {
  return request({
    url: '/re-correct',
    method: 'post',
    data
  })
}