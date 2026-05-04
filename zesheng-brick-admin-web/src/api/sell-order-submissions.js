import request from '@/utils/request'

/**
 * 分页查询行情报单记录
 */
export function getSellOrderSubmissions(params = {}) {
  return request.get('/admin/sell-order-submissions', { params }).then(res => res.data)
}

/**
 * 获取单条行情报单详情
 */
export function getSellOrderSubmission(id) {
  return request.get(`/admin/sell-order-submissions/${id}`).then(res => res.data)
}

/** 行情报单物流轨迹（快递100） */
export function getSellOrderSubmissionLogisticsTrace(id) {
  return request.get(`/admin/sell-order-submissions/${id}/logistics-trace`).then(res => res.data)
}

/**
 * 更新行情报单（状态、备注、商品明细）
 */
export function updateSellOrderSubmission(id, payload) {
  return request.patch(`/admin/sell-order-submissions/${id}`, payload).then(res => res.data)
}

/**
 * 上传回款凭证（可多张）
 */
export function uploadSettledProof(id, file) {
  const form = new FormData()
  form.append('file', file)
  return request.post(`/admin/sell-order-submissions/${id}/settled-proof`, form, {
    headers: { 'Content-Type': false }
  }).then(res => res.data)
}

/**
 * 删除一条回款凭证（传 index 更可靠，避免长 URL 被截断）
 * @param id 记录 ID
 * @param opts { index?: number, url?: string }
 */
export function removeSettledProof(id, opts = {}) {
  const params = {}
  if (opts.index !== undefined && opts.index !== null) params.index = opts.index
  if (opts.url !== undefined && opts.url !== null && opts.url !== '') params.url = opts.url
  return request.delete(`/admin/sell-order-submissions/${id}/settled-proof`, { params }).then(res => res.data)
}
