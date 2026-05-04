const request = require('../utils/request');
const {
    setToken,
    setRefreshToken,
    getToken,
    getUserInfo
} = require('../utils/storage');
const { syncUserInfo, clearAuthState } = require('./auth-state');

async function persistAuthPayload(data) {
    if (data && data.token) {
        setToken(data.token);
    }
    if (data && data.refreshToken) {
        setRefreshToken(data.refreshToken);
    }
    if (data && data.userInfo) {
        syncUserInfo(data.userInfo);
    }
    return data;
}

const UNAUTHORIZED_CODES = [401, 10004, 10005];

function isUnauthorizedError(error) {
    if (!error) {
        return false;
    }
    if (typeof error.code !== 'undefined' && UNAUTHORIZED_CODES.indexOf(Number(error.code)) >= 0) {
        return true;
    }
    const message = `${error.msg || error.message || ''}`.toLowerCase();
    return message.indexOf('未登录') >= 0
        || message.indexOf('token过期') >= 0
        || message.indexOf('token已过期') >= 0
        || message.indexOf('token无效') >= 0;
}

const wxLogin = async (code) => {
    const data = await request.post('/auth/wx-login', { code }, { showLoading: true });
    return persistAuthPayload(data);
};

const wxPhoneQuickLogin = async (payload) => {
    const data = await request.post('/auth/wx-phone-login', payload, { showLoading: true });
    return persistAuthPayload(data);
};

const donutIdentityLogin = async (payload) => {
    const data = await request.post('/auth/donut-login', payload, { showLoading: true });
    return persistAuthPayload(data);
};

const sendSmsCode = async (phone) => request.post('/auth/send-sms-code', { phone }, { showLoading: true });

const smsCodeLogin = async (payload) => {
    const data = await request.post('/auth/sms-login', payload, { showLoading: true });
    return persistAuthPayload(data);
};

const updateUserInfo = async (userInfo) => {
    const data = await request.post('/auth/update-user-info', userInfo, { showLoading: true });
    if (data) {
        syncUserInfo(data);
    }
    return data;
};

const getCurrentUser = async () => {
    try {
        const data = await request.get('/auth/current-user', {}, { showLoading: false });
        if (data && data.userId) {
            syncUserInfo(data);
        }
        return data;
    } catch (error) {
        if (isUnauthorizedError(error)) {
            // 与刷新失败一致，清理 token，避免只剩缓存片段导致「看起来像掉了登录」
            clearAuthState();
            return null;
        }
        const cacheUserInfo = getUserInfo();
        syncUserInfo(cacheUserInfo || null);
        return cacheUserInfo || null;
    }
};

const logout = async () => {
    try {
        await request.post('/auth/logout', {}, { showLoading: false });
    } catch (error) {
        console.error('后端退出登录失败:', error);
    } finally {
        clearAuthState();
    }
};

const isLoggedIn = () => {
    const userInfo = getUserInfo();
    return !!(userInfo && userInfo.userId && getToken());
};

module.exports = {
    wxLogin,
    wxPhoneQuickLogin,
    donutIdentityLogin,
    sendSmsCode,
    smsCodeLogin,
    updateUserInfo,
    getCurrentUser,
    logout,
    isLoggedIn
};
