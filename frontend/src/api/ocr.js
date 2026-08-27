import request from './request'

export const ocr = (imageUrl) => {
  return request({
    url: '/ocr',
    method: 'post',
    data: { imageUrl }
  })
}