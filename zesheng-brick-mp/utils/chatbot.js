const request = require('./request');
const logger = require('./logger');
const { getOpenId, setOpenId, getUserInfo } = require('./storage');

let initPromise = null;
const DEFAULT_USER_NAME = '微信用户';
const DEFAULT_USER_AVATAR = 'https://mmbiz.qpic.cn/mmbiz/icTdbqWNOwNRna42FI242Lcia07jQodd2FJGIYQfG0LAJGFxM4FbnQP6yfMxBgJ0F3YRqJCJ1aPAK2dQagdusBZg/0';

function toSafeString(value) {
    if (value === null || value === undefined) return '';
    if (typeof value === 'string') return value.trim();
    return String(value).trim();
}

function isValidOpenId(openId) {
    if (!openId || typeof openId !== 'string') return false;
    const trimmed = openId.trim();
    return trimmed.length > 0 && trimmed !== 'null' && trimmed !== 'undefined';
}

function deepFindOpenId(source, depth = 0) {
    if (!source || depth > 5) return '';
    if (typeof source === 'string') return '';
    if (Array.isArray(source)) {
        for (let i = 0; i < source.length; i += 1) {
            const found = deepFindOpenId(source[i], depth + 1);
            if (isValidOpenId(found)) return found;
        }
        return '';
    }
    if (typeof source !== 'object') return '';
    const keys = Object.keys(source);
    for (let i = 0; i < keys.length; i += 1) {
        const key = keys[i];
        const value = source[key];
        if (/openid/i.test(key) && isValidOpenId(value)) {
            return value.trim();
        }
    }
    for (let i = 0; i < keys.length; i += 1) {
        const found = deepFindOpenId(source[keys[i]], depth + 1);
        if (isValidOpenId(found)) return found;
    }
    return '';
}

function extractOpenId(payload) {
    if (!payload) return '';
    if (typeof payload === 'string') return isValidOpenId(payload) ? payload : '';
    if (isValidOpenId(payload.openid)) return payload.openid;
    if (isValidOpenId(payload.openId)) return payload.openId;
    if (payload.data && (isValidOpenId(payload.data.openid) || isValidOpenId(payload.data.openId))) {
        return payload.data.openid || payload.data.openId;
    }
    if (payload.userInfo && (isValidOpenId(payload.userInfo.openid) || isValidOpenId(payload.userInfo.openId))) {
        return payload.userInfo.openid || payload.userInfo.openId;
    }
    if (payload.data && payload.data.userInfo && (isValidOpenId(payload.data.userInfo.openid) || isValidOpenId(payload.data.userInfo.openId))) {
        return payload.data.userInfo.openid || payload.data.userInfo.openId;
    }
    return deepFindOpenId(payload);
}

function wxLoginCode() {
    return new Promise((resolve, reject) => {
        wx.login({
            success: (res) => resolve(res),
            fail: (err) => reject(err),
        });
    });
}

async function ensureOpenId() {
    const cached = getOpenId();
    if (isValidOpenId(cached)) return cached;

    const userInfo = getUserInfo();
    const userOpenId = extractOpenId(userInfo);
    if (isValidOpenId(userOpenId)) {
        setOpenId(userOpenId);
        return userOpenId;
    }

    const loginRes = await wxLoginCode();
    if (!loginRes || !loginRes.code) {
        throw new Error('获取微信登录 code 失败');
    }

    const openIdPayload = await request.post('/auth/wx-openid', { code: loginRes.code }, { showLoading: false });
    const openId = extractOpenId(openIdPayload);
    if (!isValidOpenId(openId)) {
        throw new Error('通过code换取openId失败，请检查后端/auth/wx-openid返回');
    }
    setOpenId(openId);
    return openId;
}

/**
 * 获取系统布局信息
 * 包括状态栏、导航栏、安全区域等高度
 */
function getSystemLayoutInfo() {
    try {
        // 使用 wx.getWindowInfo (基础库 2.23.0+ 推荐)
        const windowInfo = wx.getWindowInfo();
        const menuButton = wx.getMenuButtonBoundingClientRect();

        const statusBarHeight = windowInfo.statusBarHeight || 20;
        const screenHeight = windowInfo.screenHeight || 667;
        const screenWidth = windowInfo.screenWidth || 375;
        const safeAreaBottom = windowInfo.safeAreaInsets?.bottom || 0;

        // 导航栏高度 = 胶囊按钮上边界 - 状态栏高度 + 胶囊按钮高度
        const navBarHeight = (menuButton.top - statusBarHeight) * 2 + menuButton.height;

        return {
            statusBarHeight: statusBarHeight,
            navBarHeight: navBarHeight,
            safeAreaBottom: Math.round(safeAreaBottom),
            screenHeight: screenHeight,
            screenWidth: screenWidth,
        };
    } catch (e) {
        // 降级方案：使用胶囊按钮信息估算
        try {
            const menuButton = wx.getMenuButtonBoundingClientRect();
            return {
                statusBarHeight: 20,
                navBarHeight: 44,
                safeAreaBottom: 34, // 常见 iPhone 安全区域
                screenHeight: 844,
                screenWidth: 390,
            };
        } catch (e2) {
            return {
                statusBarHeight: 20,
                navBarHeight: 44,
                safeAreaBottom: 34,
                screenHeight: 667,
                screenWidth: 375,
            };
        }
    }
}

/**
 * 计算底部操作栏高度
 * 基础高度约 56px，加上安全区域底部高度
 */
function calcOperateCardHeight() {
    const layoutInfo = getSystemLayoutInfo();
    return Math.round(56 + layoutInfo.safeAreaBottom);
}

function resetChatbotInit() {
    initPromise = null;
}

function initChatbot(options = {}) {
    if (options.forceReinit === true) {
        initPromise = null;
    }
    if (initPromise) {
        return initPromise;
    }

    initPromise = ensureOpenId().then((openId) => {
        const plugin = requirePlugin('chatbot');
        const isAnonymous = options.anonymous === true;
        const userName = isAnonymous ? DEFAULT_USER_NAME : (toSafeString(options.userName) || DEFAULT_USER_NAME);
        const userHeader = isAnonymous ? DEFAULT_USER_AVATAR : (toSafeString(options.userHeader) || DEFAULT_USER_AVATAR);
        const layoutInfo = getSystemLayoutInfo();
        const operateCardHeight = calcOperateCardHeight();

        logger.log('[chatbot] 布局信息:', JSON.stringify(layoutInfo));
        logger.log('[chatbot] operateCardHeight:', operateCardHeight);

        return new Promise((resolve, reject) => {
            plugin.init({
                appid: options.appid || 'shVdhiau7vzPNreYOhccE63qaDhxLM',
                openid: openId,
                userHeader,
                userName,
                anonymous: isAnonymous,
                history: true,
                historySize: 200,
                operateCardHeight,
                navHeight: layoutInfo.navBarHeight,
                success: () => resolve(openId),
                fail: (err) => reject(err),
            });
        });
    });

    return initPromise.catch((error) => {
        initPromise = null;
        throw error;
    });
}

module.exports = {
    ensureOpenId,
    getSystemLayoutInfo,
    calcOperateCardHeight,
    initChatbot,
    resetChatbotInit,
};
