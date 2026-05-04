const {
    getToken,
    getUserInfo,
    setUserInfo,
    clearUserInfo,
    clearAllAuth,
} = require('../utils/storage');

function setGlobalUserInfo(userInfo) {
    try {
        const app = getApp && getApp();
        if (app && app.globalData) {
            app.globalData.userInfo = userInfo || null;
        }
    } catch (error) {
        // ignore
    }
}

function syncUserInfo(userInfo) {
    const normalizedUserInfo = userInfo && typeof userInfo === 'object' ? userInfo : null;
    if (normalizedUserInfo && (normalizedUserInfo.userId || normalizedUserInfo.id)) {
        const outputUserInfo = normalizedUserInfo.userId
            ? normalizedUserInfo
            : { ...normalizedUserInfo, userId: normalizedUserInfo.id };
        setUserInfo(outputUserInfo);
        setGlobalUserInfo(outputUserInfo);
        return outputUserInfo;
    }
    clearUserInfo();
    setGlobalUserInfo(null);
    return null;
}

function clearAuthState() {
    clearAllAuth();
    setGlobalUserInfo(null);
}

function getCachedLoginState() {
    const token = getToken();
    const userInfo = getUserInfo() || {};
    if (!token || !userInfo.userId) {
        return { token: '', userInfo: null, loggedIn: false };
    }
    return { token, userInfo, loggedIn: true };
}

module.exports = {
    setGlobalUserInfo,
    syncUserInfo,
    clearAuthState,
    getCachedLoginState,
};
