/**
 * 页面转发：标题统一为小程序名称，路径按当前页面区分
 */

const APP_TITLE = '泽晟搬砖助手';

const SHARE_DENY_ROUTES = new Set([
    'pages/sms-login/sms-login',
    'pages/webview/webview',
]);

/** 各页面分享打开路径（标题统一用 APP_TITLE） */
const ROUTE_PATH = {
    'pages/index/index': '/pages/index/index',
    'pages/market/market': '/pages/market/market',
    'pages/form/form': '/pages/form/form',
    'pages/my/my': '/pages/my/my',
    'pages/detail/detail': '/pages/detail/detail',
    'pages/form-schemes/form-schemes': '/pages/form-schemes/form-schemes',
    'pages/sell-order/sell-order': '/pages/sell-order/sell-order',
    'pages/order-list/order-list': '/pages/order-list/order-list',
    'pages/order-detail/order-detail': '/pages/order-detail/order-detail',
    'pages/profile/profile': '/pages/profile/profile',
    'pages/payment-info/payment-info': '/pages/payment-info/payment-info',
    'pages/help-center/help-center': '/pages/help-center/help-center',
    'pages/feedback/feedback': '/pages/feedback/feedback',
    'pages/feedback-history/feedback-history': '/pages/feedback-history/feedback-history',
    'pages/about/about': '/pages/about/about',
    'pages/announcement-history/index': '/pages/announcement-history/index',
    'pages/customer-service/customer-service': '/pages/customer-service/customer-service',
    'pages/legal-doc/legal-doc': '/pages/legal-doc/legal-doc',
};

const DEFAULT_PATH = '/pages/index/index';

function buildQueryString(options = {}) {
    return Object.keys(options)
        .filter((key) => options[key] != null && String(options[key]) !== '')
        .map((key) => `${key}=${encodeURIComponent(String(options[key]))}`)
        .join('&');
}

function buildSharePath(basePath, options = {}) {
    const query = buildQueryString(options);
    if (!query) {
        return basePath;
    }
    const joiner = basePath.includes('?') ? '&' : '?';
    return `${basePath}${joiner}${query}`;
}

function buildShareAppMessagePayload(page) {
    const route = page.route || '';
    const basePath = ROUTE_PATH[route] || DEFAULT_PATH;
    return {
        title: APP_TITLE,
        path: buildSharePath(basePath, page.options || {}),
    };
}

function buildShareTimelinePayload(page) {
    return {
        title: APP_TITLE,
        query: buildQueryString(page.options || {}),
    };
}

function showShareMenuForPage(page) {
    const route = page.route || '';
    if (SHARE_DENY_ROUTES.has(route)) {
        wx.hideShareMenu({
            menus: ['shareAppMessage', 'shareTimeline'],
        });
        return;
    }
    wx.showShareMenu({
        withShareTicket: true,
        menus: ['shareAppMessage', 'shareTimeline'],
    });
}

/**
 * 为 Page({}) 注入分享能力
 * @param {WechatMiniprogram.Page.Options} pageOptions
 */
function applyPageShare(pageOptions) {
    if (!pageOptions.onShareAppMessage) {
        pageOptions.onShareAppMessage = function onShareAppMessage() {
            return buildShareAppMessagePayload(this);
        };
    }
    if (!pageOptions.onShareTimeline) {
        pageOptions.onShareTimeline = function onShareTimeline() {
            return buildShareTimelinePayload(this);
        };
    }

    const userOnLoad = pageOptions.onLoad;
    pageOptions.onLoad = function onLoad(options) {
        showShareMenuForPage(this);
        if (typeof userOnLoad === 'function') {
            userOnLoad.call(this, options);
        }
    };

    const userOnShow = pageOptions.onShow;
    pageOptions.onShow = function onShow() {
        showShareMenuForPage(this);
        if (typeof userOnShow === 'function') {
            userOnShow.call(this);
        }
    };

    return pageOptions;
}

module.exports = {
    applyPageShare,
    buildShareAppMessagePayload,
    buildShareTimelinePayload,
    showShareMenuForPage,
    APP_TITLE,
};
