import request from './request'

export const getHistoryList = (page = 1, size = 20) => {
  return request({
    url: '/history',
    method: 'get',
    params: { page, size }
  })
}

export const getHistoryDetail = (id) => {
  return request({
    url: `/history/${id}`,
    method: 'get'
  })
}