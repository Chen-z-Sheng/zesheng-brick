'use strict';

const identity = require('../../utils/clientIdentityLogin');
const app = getApp();

Page({
    data: identity.getInitialData(),

    onLoad() {
        if (app.globalData.userInfo && app.globalData.userInfo.userId) {
            wx.switchTab({ url: '/pages/index/index' });
            return;
        }
        identity.syncIdentityMode(this);
    },

    onShow() {
        if (this.data.identityMode === 'donut') {
            identity.refreshPhoneMask(this);
        }
    },

    refreshPhoneMask() {
        identity.refreshPhoneMask(this);
    },

    onAgreeOperatorChange(e) {
        identity.onAgreeOperatorChange(this, e);
    },

    openCarrierDoc(e) {
        identity.openCarrierDoc(e);
    },

    onPhoneOneClick(e) {
        identity.onPhoneOneClick(this, e, () => {
            wx.switchTab({ url: '/pages/index/index' });
        });
    },

    onSendSmsDone(e) {
        identity.onSendSmsDone(e);
    },

    onSmsPhoneInput(e) {
        identity.onSmsPhoneInput(this, e);
    },

    onSmsCodeInput(e) {
        identity.onSmsCodeInput(this, e);
    },

    submitSmsLogin() {
        identity.submitSmsLogin(this, () => {
            wx.switchTab({ url: '/pages/index/index' });
        });
    },

    onPhoneQuickLogin(e) {
        identity.onPhoneQuickLogin(this, e, () => {
            wx.switchTab({ url: '/pages/index/index' });
        });
    }
});
