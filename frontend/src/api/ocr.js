import request from './request'

export const upload = (file) => {
  const formData = new FormData()
  formData.append('file', file)

  return request({
    url: '/ocr/upload',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}