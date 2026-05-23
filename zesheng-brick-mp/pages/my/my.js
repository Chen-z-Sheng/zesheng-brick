const { getUserInfo, clearCache: doClearCache, getToken } = require('../../utils/storage');
const { getCurrentUser, logout, updateUserInfo } = require('../../services/auth');
const { syncUserInfo } = require('../../services/auth-state');
const { getOrderStats } = require('../../services/order');
const { getPaymentInfoByUserId } = require('../../services/payment-info');
const config = require('../../config/index');
const configService = require('../../services/config');
const {
    buildShareAppMessagePayload,
    buildShareTimelinePayload,
    showShareMenuForPage,
} = require('../../utils/page-share');

/**
 * 解析 sys_config 中小程序跳转配置：JSON 支持 appId / AppId 与 path；或纯 appId 字符串
 */
function parseMiniProgramJump(raw) {
    if (!raw || typeof raw !== 'string') {
        return null;
    }
    const trimmed = raw.trim();
    if (!trimmed) {
        return null;
    }
    try {
        const obj = JSON.parse(trimmed);
        if (obj && typeof obj === 'object') {
            const appIdRaw = obj.appId != null ? obj.appId : obj.AppId;
            if (appIdRaw) {
                const appId = String(appIdRaw).trim();
                const pathRaw = obj.path != null ? String(obj.path).trim() : '';
                // navigateToMiniProgram 的 path 一般不带前导 /
                const path = pathRaw.replace(/^\//, '');
                return { appId, path };
            }
        }
    } catch (e) {
        if (/^wx[0-9a-fA-F]{16}$/.test(trimmed)) {
            return { appId: trimmed, path: '' };
        }
    }
    return null;
}

// 微信默认灰色头像
const DEFAULT_AVATAR = 'https://mmbiz.qpic.cn/mmbiz/icTdbqWNOwNRna42FI242Lcia07jQodd2FJGIYQfG0LAJGFxM4FbnQP6yfMxBgJ0F3YRqJCJ1aPAK2dQagdusBZg/0';

Page({
    /**
     * 页面的初始数据
     */
    data: {
        userInfo: {},
        avatarDisplayUrl: 'https://mmbiz.qpic.cn/mmbiz/icTdbqWNOwNRna42FI242Lcia07jQodd2FJGIYQfG0LAJGFxM4FbnQP6yfMxBgJ0F3YRqJCJ1aPAK2dQagdusBZg/0',
        paymentInfo: '',
        orderStats: {
            total: 0,
            transit: 0,
            storing: 0,
            completed: 0,
            exception: 0
        },
        isLoading: false,
        loginModalVisible: false,
        loginSource: '',
        expressPromoteModalVisible: false,
    },

    getRawAvatarUrl(userInfo) {
        if (!userInfo) {
            return '';
        }
        const avatarUrl = (userInfo.avatarUrl || userInfo.avatar || '').trim();
        if (!avatarUrl || avatarUrl === DEFAULT_AVATAR) {
            return '';
        }
        return avatarUrl;
    },

    buildAvatarDisplayUrl(userInfo, forceRefresh) {
        const rawUrl = this.getRawAvatarUrl(userInfo);
        if (!rawUrl) {
            return DEFAULT_AVATAR;
        }
        if (!forceRefresh) {
            return rawUrl;
        }
        const joinChar = rawUrl.includes('?') ? '&' : '?';
        return `${rawUrl}${joinChar}_t=${Date.now()}`;
    },

    applyUserInfo(userInfo, forceRefreshAvatar) {
        this.setData({
            userInfo: userInfo || {},
            avatarDisplayUrl: this.buildAvatarDisplayUrl(userInfo, !!forceRefreshAvatar),
        });
    },

    onShareAppMessage() {
        return buildShareAppMessagePayload(this);
    },

    onShareTimeline() {
        return buildShareTimelinePayload(this);
    },

    onLoad(options) {
        showShareMenuForPage(this);
        this.loadUserInfo();
    },

    onShow() {
        showShareMenuForPage(this);
        this.loadUserInfo();
    },

    /**
     * 登录成功回调
     * @param {Object} userInfo
     */
    onLoginSuccess(userInfo) {
        this.applyUserInfo(userInfo, false);
        this.loadOrderStats();
    },

    /**
     * 加载用户信息（完成后再拉订单统计，避免 userInfo 未就绪时统计为 0）
     */
    async loadUserInfo() {
        this.setData({ isLoading: true });
        try {
            const serverUserInfo = await getCurrentUser();
            if (serverUserInfo) {
                this.setData({ isLoading: false }, () => {
                    this.applyUserInfo(serverUserInfo, false);
                    this.loadPaymentInfo();
                    this.loadOrderStats();
                });
            } else {
                const localUserInfo = getUserInfo() || {};
                this.setData({ isLoading: false }, () => {
                    this.applyUserInfo(localUserInfo, false);
                    this.loadOrderStats();
                });
            }
        } catch (error) {
            const localUserInfo = getUserInfo() || {};
            this.setData({ isLoading: false }, () => {
                this.applyUserInfo(localUserInfo, false);
                this.loadOrderStats();
            });
        }
    },

    /**
     * 加载打款信息
     */
    loadPaymentInfo() {
        const { userInfo } = this.data;
        const userId = userInfo && (userInfo.userId || userInfo.id);
        if (!userId) {
            this.setData({ paymentInfo: '' });
            return;
        }
        getPaymentInfoByUserId(userId)
            .then((paymentInfo) => {
                this.setData({ paymentInfo: paymentInfo ? '已填写' : '' });
            })
            .catch(() => {
                this.setData({ paymentInfo: '' });
            });
    },

    /**
     * 仅用于上传头像鉴权，避免未登录时调用上传接口
     */
    getUploadToken() {
        const token = getToken();
        if (!token) {
            throw new Error('请先登录');
        }
        return token;
    },

    /**
     * 上传头像到服务器
     */
    uploadAvatar(tempFilePath) {
        return new Promise((resolve, reject) => {
            let token = '';
            try {
                token = this.getUploadToken();
            } catch (error) {
                reject(error);
                return;
            }

            wx.uploadFile({
                url: `${config.BASE_URL}/auth/upload-avatar`,
                filePath: tempFilePath,
                name: 'file',
                header: { 'Authorization': `Bearer ${token}` },
                success: (res) => {
                    try {
                        const data = JSON.parse(res.data);
                        if (data.code === 200 && data.data?.url) {
                            resolve(data.data.url);
                        } else {
                            reject(new Error(data.message || '上传失败'));
                        }
                    } catch (err) {
                        reject(new Error('解析响应失败'));
                    }
                },
                fail: (err) => {
                    reject(new Error(err.errMsg || '上传失败'));
                },
            });
        });
    },

    /**
     * 加载订单统计（固结+行情合并），需在 userInfo 就绪后调用
     */
    loadOrderStats() {
        const { userInfo } = this.data;
        const userId = userInfo && (userInfo.userId || userInfo.id);
        if (!userId) {
            this.setData({
                orderStats: { total: 0, transit: 0, storing: 0, completed: 0, exception: 0 }
            });
            return;
        }
        getOrderStats()
            .then((res) => {
                this.setData({
                    orderStats: {
                        total: res.total ?? 0,
                        transit: res.transit ?? 0,
                        storing: res.storing ?? 0,
                        completed: res.completed ?? 0,
                        exception: res.exception ?? 0
                    }
                });
            })
            .catch(() => {
                this.setData({
                    orderStats: { total: 0, transit: 0, storing: 0, completed: 0, exception: 0 }
                });
            });
    },

    /**
     * 用户卡片点击：未登录时打开登录弹窗
     */
    onUserCardTap() {
        const { userInfo } = this.data;
        const userId = userInfo && (userInfo.userId || userInfo.id);
        if (!userId) {
            this.handleLogin();
        }
    },

    /**
     * 选择头像（在"我的"页点击头像时）
     */
    async onChooseAvatar(e) {
        const { avatarUrl } = e.detail;
        const { userInfo } = this.data;

        const userId = userInfo && (userInfo.userId || userInfo.id);
        if (!avatarUrl || !userId) {
            return;
        }

        wx.showLoading({ title: '上传中...' });

        try {
            const uploadedUrl = await this.uploadAvatar(avatarUrl);
            if (!uploadedUrl) {
                wx.showToast({ title: '头像上传失败', icon: 'none' });
                return;
            }

            const updateData = {
                nickName: userInfo.nickName,
                avatarUrl: uploadedUrl,
                phone: userInfo.phone,
            };

            const updatedUserInfo = await updateUserInfo(updateData);
            const mergedUserInfo = {
                ...(userInfo || {}),
                ...(updatedUserInfo || {}),
                avatarUrl: uploadedUrl,
            };
            syncUserInfo(mergedUserInfo);
            this.applyUserInfo(mergedUserInfo, true);

            const serverUserInfo = await getCurrentUser();
            if (serverUserInfo) {
                this.applyUserInfo(serverUserInfo, true);
            }

            wx.showToast({ title: '头像已更新', icon: 'success' });
        } catch (error) {
            console.error('更新头像失败:', error);
            wx.showToast({
                title: error.message || '更新失败，请重试',
                icon: 'none',
            });
        } finally {
            wx.hideLoading();
        }
    },

    /**
     * 导航到个人信息页（仅编辑按钮点击时）
     */
    navigateToProfile() {
        const { userInfo } = this.data;
        const userId = userInfo && (userInfo.userId || userInfo.id);

        if (!userId) {
            this.handleLogin();
            return;
        }

        wx.navigateTo({
            url: '/pages/profile/profile'
        });
    },

    /**
     * 显示登录模态窗口
     */
    handleLogin() {
        this.setData({
            loginModalVisible: true
        });
    },

    /**
     * 关闭登录模态窗口
     */
    handleLoginModalClose() {
        this.setData({
            loginModalVisible: false
        });
    },

    /**
     * 登录成功回调
     */
    handleLoginSuccess(e) {
        const userInfo = e.detail.userInfo;
        const { loginSource } = this.data;

        try {
            this.applyUserInfo(userInfo, false);
            this.loadOrderStats();

            // 根据登录来源执行相应操作
            if (loginSource === 'paymentInfo') {
                wx.navigateTo({
                    url: '/pages/payment-info/payment-info'
                });
            }
        } finally {
            // 确保状态清理
            this.setData({
                loginModalVisible: false,
                loginSource: ''
            });
        }
    },

    /**
     * 导航到订单页（带 statusTab：all|shipped|storing|completed|exception）
     */
    navigateToOrders(e) {
        const statusTab = e.currentTarget.dataset.status || 'all';
        wx.navigateTo({
            url: `/pages/order-list/order-list?statusTab=${statusTab}`
        });
    },

    /**
     * 导航到打款信息页面
     */
    navigateToPaymentInfo() {
        const { userInfo } = this.data;
        const userId = userInfo && (userInfo.userId || userInfo.id);

        if (!userId) {
            this.setData({ loginSource: 'paymentInfo' });
            this.handleLogin();
            return;
        }

        wx.navigateTo({
            url: '/pages/payment-info/payment-info'
        });
    },



    /**
     * 跳转关于我
     */
    navigateToAbout() {
        wx.navigateTo({
            url: '/pages/about/about'
        });
    },

    /**
     * 显示帮助中心
     */
    showHelp() {
        wx.navigateTo({
            url: '/pages/help-center/help-center'
        });
    },

    /**
     * 打开寄快递说明弹窗
     */
    onExpressEntryTap() {
        this.setData({ expressPromoteModalVisible: true });
    },

    closeExpressPromoteModal() {
        this.setData({ expressPromoteModalVisible: false });
    },

    /** 阻止弹窗内容区点击冒泡关闭 */
    catchExpressModalEmpty() {},

    /**
     * 去寄快递：拉取后台配置后跳转其它小程序
     */
    async onExpressPromGo() {
        if (this._expressNavLock) {
            return;
        }
        this._expressNavLock = true;
        wx.showLoading({ title: '加载中...', mask: true });
        try {
            const raw = await configService.fetchPublicSysConfigByKey(
                configService.CHEAP_EXPRESS_MINI_PROGRAM_KEY
            );
            const jump = parseMiniProgramJump(raw);
            // 先关 loading 再 Toast/跳转：showToast 会结束 loading，若再在 finally 里 hide 会触发配对警告
            wx.hideLoading();
            if (!jump || !jump.appId) {
                wx.showToast({ title: '暂未配置跳转，请联系管理员', icon: 'none' });
                return;
            }
            wx.navigateToMiniProgram({
                appId: jump.appId,
                path: jump.path || undefined,
                success: () => {
                    this.setData({ expressPromoteModalVisible: false });
                },
                fail: (err) => {
                    const errMsg = (err && err.errMsg) || '';
                    // 用户在系统弹窗点「取消」时静默处理，不弹英文 errMsg
                    if (/fail\s+cancel/i.test(errMsg)) {
                        return;
                    }
                    wx.showToast({
                        title: errMsg || '跳转失败',
                        icon: 'none',
                    });
                },
            });
        } catch (e) {
            wx.hideLoading();
            // request 封装已对业务错误弹 Toast
        } finally {
            this._expressNavLock = false;
        }
    },

    /**
     * 清除缓存（草稿等临时数据，保留登录状态）
     */
    clearCache() {
        wx.showModal({
            title: '清除缓存',
            content: '将清除草稿等临时数据，登录状态不会受影响，确定继续？',
            success: (res) => {
                if (res.confirm) {
                    try {
                        const { cleared } = doClearCache();
                        wx.showToast({
                            title: cleared > 0 ? `已清除 ${cleared} 项缓存` : '缓存已是最新',
                            icon: 'success'
                        });
                    } catch (err) {
                        wx.showToast({
                            title: '清除失败，请重试',
                            icon: 'none'
                        });
                    }
                }
            }
        });
    },

    /**
     * 退出登录
     */
    handleLogout() {
        wx.showModal({
            title: '提示',
            content: '确定要退出登录吗？',
            success: (res) => {
                if (res.confirm) {
                    logout();
                    this.applyUserInfo({}, false);
                    wx.showToast({
                        title: '已退出登录',
                        icon: 'success'
                    });
                }
            }
        });
    },

    /**
     * 头像加载失败时使用默认头像
     */
    onAvatarError() {
        this.setData({
            avatarDisplayUrl: DEFAULT_AVATAR
        });
    },
});
