import request from '@/utils/request'

export function getIndexBannerById(key) {
  return request({
    url: `/admin/config/${key}`,
    method: 'get'
  })
}

export function updateIndexBannerById(key, payload) {
  return request({
    url: `/admin/config/${key}`,
    method: 'patch',
    data: payload
  })
}
