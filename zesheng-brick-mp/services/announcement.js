const request = require('../utils/request');
const { getToken, getUserInfo } = require('../utils/storage');

function hasLoggedInUser() {
    const token = getToken();
    const userInfo = getUserInfo();
    return !!(token && userInfo && userInfo.userId);
}

/**
 * 获取当前用户需要弹窗展示的最新公告（未忽略且在生效期内）
 * 需登录后调用
 * @returns {Promise<Object|null>} 公告对象 { id, title, content, ... } 或 null
 */
function getLatestToShow() {
    if (!hasLoggedInUser()) {
        return Promise.resolve(null);
    }
    return request.get('/announcements/latest-to-show', {}, { showLoading: false }).then((data) => {
        if (data && data.id) return data;
        return null;
    }).catch(() => null);
}

/**
 * 获取公告历史列表（供首页滚动条与历史页；未登录不请求，避免无效 401）
 * @returns {Promise<Array>}
 */
function getHistoryList() {
    if (!getToken()) {
        return Promise.resolve([]);
    }
    return request.get('/announcements/history', {}, { showLoading: false }).then((data) => {
        return Array.isArray(data) ? data : [];
    }).catch(() => []);
}

/**
 * 用户选择“不再提示本条公告”，提交忽略记录
 * @param {number} announcementId 公告ID
 */
function ignore(announcementId) {
    if (!hasLoggedInUser()) {
        return Promise.resolve(false);
    }
    return request
        .post('/announcements/ignore', { announcementId }, { showLoading: false })
        .then(() => true)
        .catch(() => false);
}

module.exports = {
    getLatestToShow,
    getHistoryList,
    ignore
};
