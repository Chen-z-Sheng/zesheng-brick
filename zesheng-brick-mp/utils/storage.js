const TOKEN_KEY = 'token';
const REFRESH_TOKEN_KEY = 'refreshToken';
const USER_INFO_KEY = 'userInfo';
const OPEN_ID_KEY = 'openId';

/**
 * 获取token
 * @returns {string}
 */
const getToken = () => wx.getStorageSync(TOKEN_KEY) || '';

/**
 * 设置token
 * @param {string} token
 */
const setToken = (token = '') => {
    wx.setStorageSync(TOKEN_KEY, token);
};

/**
 * 清除token
 */
const clearToken = () => {
    wx.removeStorageSync(TOKEN_KEY);
};

/**
 * 获取刷新token
 * @returns {string}
 */
const getRefreshToken = () => wx.getStorageSync(REFRESH_TOKEN_KEY) || '';

/**
 * 设置刷新token
 * @param {string} refreshToken
 */
const setRefreshToken = (refreshToken = '') => {
    wx.setStorageSync(REFRESH_TOKEN_KEY, refreshToken);
};

/**
 * 清除刷新token
 */
const clearRefreshToken = () => {
    wx.removeStorageSync(REFRESH_TOKEN_KEY);
};

/**
 * 获取用户信息
 * @returns {Object}
 */
const getUserInfo = () => wx.getStorageSync(USER_INFO_KEY) || {};

/**
 * 设置用户信息
 * @param {Object} userInfo
 */
const setUserInfo = (userInfo = {}) => {
    wx.setStorageSync(USER_INFO_KEY, userInfo);
};

/**
 * 清除用户信息
 */
const clearUserInfo = () => {
    wx.removeStorageSync(USER_INFO_KEY);
};

/**
 * 获取 openId
 * @returns {string}
 */
const getOpenId = () => wx.getStorageSync(OPEN_ID_KEY) || '';

/**
 * 设置 openId
 * @param {string} openId
 */
const setOpenId = (openId = '') => {
    wx.setStorageSync(OPEN_ID_KEY, openId);
};

/**
 * 清除 openId
 */
const clearOpenId = () => {
    wx.removeStorageSync(OPEN_ID_KEY);
};

/**
 * 清除所有登录相关缓存
 */
const clearAllAuth = () => {
    clearToken();
    clearRefreshToken();
    clearUserInfo();
    clearOpenId();
};

/**
 * 清除缓存数据，保留登录状态
 * 清除草稿、表单临时数据等，保留 token 和 userInfo
 */
const clearCache = () => {
    try {
        const { keys } = wx.getStorageInfoSync();
        // 保留 openId，避免智能客服会话身份变化导致历史消息丢失
        const preserveKeys = [TOKEN_KEY, REFRESH_TOKEN_KEY, USER_INFO_KEY, OPEN_ID_KEY];

        keys.forEach((key) => {
            if (!preserveKeys.includes(key)) {
                wx.removeStorageSync(key);
            }
        });

        return { cleared: keys.filter((k) => !preserveKeys.includes(k)).length };
    } catch (err) {
        console.error('清除缓存失败:', err);
        throw err;
    }
};

module.exports = {
    TOKEN_KEY,
    REFRESH_TOKEN_KEY,
    USER_INFO_KEY,
    OPEN_ID_KEY,
    getToken,
    setToken,
    clearToken,
    getRefreshToken,
    setRefreshToken,
    clearRefreshToken,
    getUserInfo,
    setUserInfo,
    clearUserInfo,
    getOpenId,
    setOpenId,
    clearOpenId,
    clearAllAuth,
    clearCache,
};
