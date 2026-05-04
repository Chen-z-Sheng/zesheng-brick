import request from '@/utils/request'

/**
 * 公告列表（全量）
 */
export function getAnnouncementList() {
    return request.get('/admin/announcements').then(res => res.data)
}

/**
 * 分页查询公告
 */
export function getAnnouncementPage(params) {
    return request.get('/admin/announcements/page', { params }).then(res => res.data)
}

/**
 * 公告详情
 */
export function getAnnouncementById(id) {
    return request.get(`/admin/announcements/${id}`).then(res => res.data)
}

/**
 * 新增公告
 */
export function createAnnouncement(data) {
    return request.post('/admin/announcements', data).then(res => res.data)
}

/**
 * 更新公告
 */
export function updateAnnouncement(id, data) {
    return request.patch(`/admin/announcements/${id}`, data).then(res => res.data)
}

/**
 * 启用公告
 */
export function enableAnnouncement(id) {
    return request.post(`/admin/announcements/${id}/enable`).then(res => res.data)
}

/**
 * 删除公告
 */
export function deleteAnnouncement(id) {
    return request.delete(`/admin/announcements/${id}`).then(res => res.data)
}

/**
 * 富文本内上传图片（公告内容用）
 */
export function uploadAnnouncementImage(file) {
    const form = new FormData()
    form.append('file', file)
    return request.post('/admin/announcements/upload-image', form, {
        headers: { 'Content-Type': false }
    }).then(res => res.data)
}
