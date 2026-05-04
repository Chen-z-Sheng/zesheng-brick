import request from '@/utils/request'

/**
 * 系统配置列表（全量，不分页）
 */
export function getConfigList() {
    return request.get('/admin/config').then(res => res.data)
}

/**
 * 按 key 查询单条配置
 */
export function getConfigByKey(key) {
    return request.get(`/admin/config/by-key/${encodeURIComponent(key)}`).then(res => res.data)
}

/**
 * 新增配置
 * @param {Object} payload - { configKey, value, valueType?, remark? }，value 可为对象或 JSON 字符串
 */
export function createConfig(payload) {
    const body = { ...payload }
    if (typeof body.value !== 'string') {
        body.value = JSON.stringify(body.value)
    }
    return request.post('/admin/config', body).then(res => res.data)
}

/**
 * 更新配置
 * @param {string|number} id - 配置 ID
 * @param {Object} payload - { configKey?, value?, valueType?, remark? }
 */
export function updateConfig(id, payload) {
    const body = { ...payload }
    if (body.value !== undefined && typeof body.value !== 'string') {
        body.value = JSON.stringify(body.value)
    }
    return request.patch(`/admin/config/${id}`, body).then(res => res.data)
}

/**
 * 删除配置
 */
export function deleteConfig(id) {
    return request.delete(`/admin/config/${id}`).then(res => res.data)
}
