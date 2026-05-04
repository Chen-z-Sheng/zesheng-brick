import request from '@/utils/request'

/**
 * 获取所有表单提交数据（分页+排序）
 * @param params 分页排序查询参数（PageAndSortQueryDto），默认空对象
 * @returns 表单提交数据列表（后端返回的data部分）
 */
export function getFormSubmissions(params = {}) {
  return request.get('/admin/form-submissions', { params }).then(res => res.data)
}

/**
 * 获取单个表单提交数据详情
 * @param id 表单提交数据ID
 * @returns 单个表单提交数据
 */
export function getFormSubmission(id) {
  return request.get(`/admin/form-submissions/${id}`).then(res => res.data)
}

/** 固结报单物流轨迹（快递100） */
export function getFormSubmissionLogisticsTrace(id) {
  return request.get(`/admin/form-submissions/${id}/logistics-trace`).then(res => res.data)
}

/**
 * 创建表单提交数据（如果有新增场景）
 * @param payload 表单提交数据体
 * @returns 创建后的表单提交数据s
 */
export function createFormSubmission(payload) {
  return request.post('/admin/form-submissions', payload).then(res => res.data)
}

/**
 * 更新表单提交数据（部分更新，如需要）
 * @param id 表单提交数据ID
 * @param payload 要更新的字段数据
 * @returns 更新后的表单提交数据
 */
export function updateFormSubmission(id, payload) {
  return request.patch(`/admin/form-submissions/${id}`, payload).then(res => res.data)
}

/**
 * 删除表单提交数据（如需要）
 * @param id 表单提交数据ID
 * @returns 布尔值（表示删除成功）
 */
export function deleteFormSubmission(id) {
  return request.delete(`/admin/form-submissions/${id}`).then(() => true)
}

/**
 * 上传回款凭证（可多张）
 */
export function uploadSettledProof(id, file) {
  const form = new FormData()
  form.append('file', file)
  return request.post(`/admin/form-submissions/${id}/settled-proof`, form, {
    headers: { 'Content-Type': false }
  }).then(res => res.data)
}

/**
 * 删除一条回款凭证（传 index 更可靠，也可传 url）
 * @param id 记录 ID
 * @param opts { index?: number, url?: string } 传 index 按索引删，或传 url 按地址删
 */
export function removeSettledProof(id, opts = {}) {
  const params = {}
  if (opts.index !== undefined && opts.index !== null) params.index = opts.index
  if (opts.url !== undefined && opts.url !== null && opts.url !== '') params.url = opts.url
  return request.delete(`/admin/form-submissions/${id}/settled-proof`, { params }).then(res => res.data)
}