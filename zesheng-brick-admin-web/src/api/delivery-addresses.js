import request from '@/utils/request'

/** 列表，params.status=1 仅启用（方案下拉用），不传则全部（管理页） */
export function getDeliveryAddressList(params = {}) {
  return request.get('/admin/delivery-addresses', { params }).then(res => res.data)
}

export function createDeliveryAddress(data) {
  return request.post('/admin/delivery-addresses', data).then(res => res.data)
}

export function updateDeliveryAddress(id, data) {
  return request.patch(`/admin/delivery-addresses/${id}`, data).then(res => res.data)
}

export function deleteDeliveryAddress(id) {
  return request.delete(`/admin/delivery-addresses/${id}`).then(res => res.data)
}
