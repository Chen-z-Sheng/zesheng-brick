'use strict';

const identity = require('../../utils/clientIdentityLogin');

Component({
    properties: {
        visible: { type: Boolean, value: false },
        title: { type: String, value: '登录' },
        desc: { type: String, value: '登录后享受更多服务' }
    },

    data: identity.getInitialData(),

    lifetimes: {
        attached() {
            identity.syncIdentityMode(this);
        }
    },

    observers: {
        visible(v) {
            if (v && this.data.identityMode === 'donut') {
                identity.refreshPhoneMask(this);
            }
        }
    },

    methods: {
        handleModalClose() {
            this.triggerEvent('close');
        },

        catchTap() {},

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
            this.triggerEvent('close');
            wx.navigateTo({ url: '/pages/sms-login/sms-login' });
        }
    }
});
