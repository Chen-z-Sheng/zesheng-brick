import request from '@/utils/request'

/** 分页列表：pageNum、pageSize、name（可选，模糊匹配公司名称） */
export function getLogisticsCompanyPage(params = {}) {
  return request.get('/admin/logistics-companies/page', { params }).then((res) => res.data)
}

export function getLogisticsCompanyDetail(id) {
  return request.get(`/admin/logistics-companies/${id}`).then((res) => res.data)
}

export function createLogisticsCompany(data) {
  return request.post('/admin/logistics-companies', data).then((res) => res.data)
}

export function updateLogisticsCompany(id, data) {
  return request.patch(`/admin/logistics-companies/${id}`, data).then((res) => res.data)
}

export function deleteLogisticsCompany(id) {
  return request.delete(`/admin/logistics-companies/${id}`).then((res) => res.data)
}
