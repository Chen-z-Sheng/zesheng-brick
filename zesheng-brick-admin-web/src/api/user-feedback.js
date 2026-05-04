import request from '@/utils/request'

export function getUserFeedbackPage(params = {}) {
    return request.get('/admin/user-feedback', { params }).then(res => res.data)
}

export function getUserFeedbackDetail(id) {
    return request.get(`/admin/user-feedback/${id}`).then(res => res.data)
}

export function replyUserFeedback(id, data) {
    return request.patch(`/admin/user-feedback/${id}/reply`, data).then(res => res.data)
}
