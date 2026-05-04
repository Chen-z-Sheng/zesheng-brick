import request from '@/utils/request'

// 分页查询方案
export function getSchemes(params = {}) {
  return request.get('/admin/form-schemes/page', { params }).then(res => res.data)
}

// 查询全部方案列表
export function getSchemeList() {
  return request.get('/admin/form-schemes').then(res => res.data)
}

export function getScheme(id) {
  return request.get(`/admin/form-schemes/${id}`).then(res => res.data)
}

export function createScheme(payload) {
  return request.post('/admin/form-schemes', payload).then(res => res.data)
}

export function updateScheme(id, payload) {
  return request.patch(`/admin/form-schemes/${id}`, payload).then(res => res.data)
}

// 单个删除方案
export function deleteSingleScheme(id) {
  return request.delete(`/admin/form-schemes/${id}`).then(res => res.data)
}

export function batchDeleteScheme(ids) {
  const realIds = Array.isArray(ids) ? ids : Array.from(ids);
  return request.post('/admin/form-schemes/delete', {
    ids: realIds
  }).then(res => res.data)
}
