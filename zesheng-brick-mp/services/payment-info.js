const { getToken, getUserInfo } = require('../utils/storage');
const { getCurrentUser } = require('./auth');
const request = require('../utils/request');

/**
 * 解析当前用户 ID（缓存优先，缺失时再拉取 current-user）
 */
async function resolveUserId() {
    const cached = getUserInfo() || {};
    let userId = cached.userId || cached.id;
    if (userId) return userId;
    if (!getToken()) return null;
    try {
        const fresh = await getCurrentUser();
        return fresh && (fresh.userId || fresh.id) ? (fresh.userId || fresh.id) : null;
    } catch (e) {
        return null;
    }
}

/**
 * 是否已存在打款信息记录（不发业务失败 Toast，避免与提交前提示重复）
 */
function fetchHasPaymentInfo(userId) {
    const token = getToken();
    if (!userId || !token) {
        return Promise.resolve(false);
    }
    return request
        .get(`/payment-info/by-user/${userId}`, {}, { showLoading: false })
        .then((data) => !!data);
}

function getPaymentInfoByUserId(userId) {
    const token = getToken();
    if (!userId || !token) {
        return Promise.resolve(null);
    }
    return request.get(`/payment-info/by-user/${userId}`, {}, { showLoading: false });
}

/**
 * 提交类操作前校验：未填写打款信息则提示并跳转打款信息页
 * @returns {Promise<boolean>} true 表示可继续提交
 */
async function ensurePaymentInfoBeforeSubmit() {
    const userId = await resolveUserId();
    if (!userId) {
        wx.showToast({ title: '请先登录', icon: 'none' });
        return false;
    }
    try {
        const has = await fetchHasPaymentInfo(userId);
        if (has) {
            return true;
        }
    } catch (error) {
        wx.showToast({ title: '网络异常，请稍后重试', icon: 'none' });
        return false;
    }
    wx.showToast({ title: '请先填写打款信息', icon: 'none' });
    setTimeout(() => {
        wx.navigateTo({ url: '/pages/payment-info/payment-info' });
    }, 500);
    return false;
}

module.exports = {
    resolveUserId,
    fetchHasPaymentInfo,
    getPaymentInfoByUserId,
    ensurePaymentInfoBeforeSubmit,
};
