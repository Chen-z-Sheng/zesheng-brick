import request from '@/utils/request'

/**
 * FAQ列表（全量）
 */
export function getHelpFaqList() {
    return request.get('/admin/help-faq').then(res => res.data)
}

/**
 * 分页查询FAQ
 */
export function getHelpFaqPage(params) {
    return request.get('/admin/help-faq/page', { params }).then(res => res.data)
}

/**
 * FAQ详情
 */
export function getHelpFaqById(id) {
    return request.get(`/admin/help-faq/${id}`).then(res => res.data)
}

/**
 * 新增FAQ
 */
export function createHelpFaq(data) {
    return request.post('/admin/help-faq', data).then(res => res.data)
}

/**
 * 更新FAQ
 */
export function updateHelpFaq(id, data) {
    return request.patch(`/admin/help-faq/${id}`, data).then(res => res.data)
}

/**
 * 删除FAQ
 */
export function deleteHelpFaq(id) {
    return request.delete(`/admin/help-faq/${id}`).then(res => res.data)
}
