'use strict';

const identity = require('../../utils/clientIdentityLogin');

Component({
    properties: {
        visible: { type: Boolean, value: false },
        title: { type: String, value: '登录' }
    },

    data: identity.getInitialData(),

    lifetimes: {
        attached() {
            identity.syncIdentityMode(this);
        }
    },

    observers: {
        visible(v) {
            if (!v) {
                return;
            }
            this.setData({ agreePlatform: false, agreeOperator: false });
            if (this.data.identityMode === 'donut') {
                identity.refreshPhoneMask(this);
            }
        }
    },

    methods: {
        onQuickLoginBlocked() {
            identity.ensureAgreePlatform(this);
        },

        onOneClickBlocked() {
            identity.ensureAgreePlatform(this);
        },

        handleModalClose() {
            this.triggerEvent('close');
        },

        catchTap() {},

        refreshPhoneMask() {
            identity.refreshPhoneMask(this);
        },

        onAgreePlatformChange(e) {
            identity.onAgreePlatformChange(this, e);
            // 一键登录场景下，平台协议与运营商条款合并为同一勾选
            if (this.data.identityMode === 'donut') {
                const vals = e.detail.value || [];
                const checked = vals.indexOf('agree') >= 0;
                this.setData({ agreeOperator: checked });
            }
        },

        openPlatformDoc(e) {
            identity.openPlatformDoc(e);
        },

        openCarrierDoc(e) {
            identity.openCarrierDoc(e);
        },

        onPhoneOneClick(e) {
            identity.onPhoneOneClick(this, e, (res) => {
                this.triggerEvent('success', { userInfo: res.userInfo });
                this.triggerEvent('close');
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
            identity.submitSmsLogin(this, (res) => {
                this.triggerEvent('success', { userInfo: res.userInfo });
                this.triggerEvent('close');
            });
        },

        onPhoneQuickLogin(e) {
            identity.onPhoneQuickLogin(this, e, (res) => {
                this.triggerEvent('success', { userInfo: res.userInfo });
                this.triggerEvent('close');
            });
        },

        goSmsLogin() {
            if (!identity.ensureAgreePlatform(this)) {
                return;
            }
            this.triggerEvent('close');
            wx.navigateTo({ url: '/pages/sms-login/sms-login' });
        }
    }
});
