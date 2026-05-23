const { applyPageShare } = require('./utils/page-share');

const originalPage = Page;
Page = function pageWithShare(options) {
    applyPageShare(options);
    return originalPage(options);
};

const { getCurrentUser } = require('./services/auth');
const { syncUserInfo, getCachedLoginState, setGlobalUserInfo } = require('./services/auth-state');
const { getToken } = require('./utils/storage');
const { ensureValidToken } = require('./utils/request');

App({
    globalData: {
        userInfo: null,
        isLoginChecked: false,
        announcementPopupChecked: false
    },

    onLaunch() {
        this.setStatusBarHeight();
        this.checkLoginStatus();
    },

    /**
     * 设置状态栏高度 CSS 变量
     * 微信官方推荐方式：使用 --status-bar-height 变量
     */
    setStatusBarHeight() {
        try {
            const info = wx.getWindowInfo();
            const statusBarHeight = info.statusBarHeight || 20;
            wx.setStorageSync('statusBarHeight', statusBarHeight);
            // 动态设置 CSS 变量
            const pages = getCurrentPages();
            const currentPage = pages[pages.length - 1];
            if (currentPage) {
                currentPage.setData({ statusBarHeight });
            }
        } catch (e) {
            console.warn('获取状态栏高度失败:', e);
        }
    },

    /**
     * 检查登录状态：先用 refresh 换新 access（JWT 默认约 2 小时过期），再拉用户信息
     */
    checkLoginStatus() {
        const app = this;
        (async () => {
            try {
                await ensureValidToken();
                const token = getToken();
                if (!token) {
                    syncUserInfo(null);
                    return;
                }
                const loginState = getCachedLoginState();
                if (loginState.loggedIn && loginState.userInfo) {
                    setGlobalUserInfo(loginState.userInfo);
                }
                await app.refreshUserInfo().catch(() => {});
            } catch (error) {
                console.error('登录检查失败:', error);
            } finally {
                app.globalData.isLoginChecked = true;
            }
        })();
    },

    /**
     * 刷新用户信息
     */
    async refreshUserInfo() {
        try {
            const userInfo = await getCurrentUser();
            if (userInfo) {
                setGlobalUserInfo(userInfo);
                return userInfo;
            }
            setGlobalUserInfo(null);
            return null;
        } catch (error) {
            setGlobalUserInfo(null);
            console.error('刷新用户信息失败:', error);
            return null;
        }
    },

    /**
     * 更新全局用户信息
     * @param {Object} userInfo
     */
    updateGlobalUserInfo(userInfo) {
        setGlobalUserInfo(userInfo);
    },

    /**
     * 触发登录成功事件
     * @param {Object} userInfo
     */
    emitLoginSuccess(userInfo) {
        // 使用微信事件总线通知各页面
        const pages = getCurrentPages();
        const currentPage = pages[pages.length - 1];
        if (currentPage && currentPage.onLoginSuccess) {
            currentPage.onLoginSuccess(userInfo);
        }
    },
});
