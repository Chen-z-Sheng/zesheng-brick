const { initChatbot, resetChatbotInit } = require('../../utils/chatbot');
const { getUserInfo, getToken } = require('../../utils/storage');

function normalizePluginString(value) {
    if (value === null || value === undefined) {
        return '';
    }
    const text = String(value).trim();
    if (!text || text === 'null' || text === 'undefined') {
        return '';
    }
    return text;
}

Page({
    data: {
        loading: true,
        initError: '',
        flag: false,
    },

    onLoad() {
        this.initService();
    },

    onUnload() {
        resetChatbotInit();
    },

    async initService() {
        this.setData({
            loading: true,
            initError: '',
            flag: false,
        });
        const token = getToken();
        if (!token) {
            this.setData({
                loading: false,
                initError: '请先登录后再使用智能客服',
            });
            return;
        }

        try {
            const userInfo = getUserInfo() || {};
            const userName = userInfo.nickName || userInfo.nickname || userInfo.userName || userInfo.name || '';
            const userHeader = userInfo.avatarUrl || userInfo.avatar || userInfo.userHeader || '';

            await initChatbot({
                forceReinit: true,
                userName,
                userHeader,
                anonymous: false,
            });

            this.setData({
                loading: false,
                initError: '',
                flag: true,
            });
        } catch (error) {
            this.setData({
                loading: false,
                flag: false,
                initError: normalizePluginString(error && (error.msg || error.message || error.errMsg)) || '客服初始化失败，请稍后重试',
            });
        }
    },

    getQueryCallback() {},

    goBackHome() {
        wx.navigateBack({ delta: 1 });
    },
});
