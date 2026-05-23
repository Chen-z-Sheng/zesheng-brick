'use strict';

const { sendSmsCode, smsCodeLogin } = require('../../services/auth');

const COUNTDOWN_SECONDS = 60;

Page({
    data: {
        phone: '',
        verifyCode: '',
        sending: false,
        loading: false,
        sendDisabled: true,
        sendBtnText: '发送验证码',
        countdown: 0
    },

    onUnload() {
        if (this._timer) {
            clearInterval(this._timer);
            this._timer = null;
        }
    },

    onPhoneInput(e) {
        const v = (e.detail.value || '').replace(/\D/g, '').slice(0, 11);
        const disabled = !/^1\d{10}$/.test(v) || this.data.countdown > 0;
        this.setData({ phone: v, sendDisabled: disabled });
    },

    onCodeInput(e) {
        const v = (e.detail.value || '').replace(/\D/g, '').slice(0, 6);
        this.setData({ verifyCode: v });
    },

    async onSendCode() {
        if (this.data.sending || this.data.countdown > 0) {
            return;
        }
        const phone = (this.data.phone || '').trim();
        if (!/^1\d{10}$/.test(phone)) {
            wx.showToast({ title: '请输入正确手机号', icon: 'none' });
            return;
        }
        this.setData({ sending: true });
        try {
            await sendSmsCode(phone);
            wx.showToast({ title: '验证码已发送', icon: 'success' });
            this.startCountdown();
        } catch (err) {
            console.error('发送验证码失败:', err);
        } finally {
            this.setData({ sending: false });
        }
    },

    startCountdown() {
        if (this._timer) {
            clearInterval(this._timer);
            this._timer = null;
        }
        this.setData({ countdown: COUNTDOWN_SECONDS, sendBtnText: `${COUNTDOWN_SECONDS}s后重试`, sendDisabled: true });
        this._timer = setInterval(() => {
            const next = (this.data.countdown || 0) - 1;
            if (next <= 0) {
                clearInterval(this._timer);
                this._timer = null;
                const canSend = /^1\d{10}$/.test(this.data.phone || '');
                this.setData({ countdown: 0, sendBtnText: '发送验证码', sendDisabled: !canSend });
                return;
            }
            this.setData({ countdown: next, sendBtnText: `${next}s后重试` });
        }, 1000);
    },

    async onLogin() {
        if (this.data.loading) {
            return;
        }
        const phone = (this.data.phone || '').trim();
        const verifyCode = (this.data.verifyCode || '').trim();
        if (!/^1\d{10}$/.test(phone)) {
            wx.showToast({ title: '请输入正确手机号', icon: 'none' });
            return;
        }
        if (!/^\d{6}$/.test(verifyCode)) {
            wx.showToast({ title: '请输入6位验证码', icon: 'none' });
            return;
        }
        this.setData({ loading: true });
        try {
            const res = await smsCodeLogin({ phone, verifyCode });
            if (!res || !res.userInfo) {
                wx.showToast({ title: '登录失败，请重试', icon: 'none' });
                return;
            }
            getApp().globalData.userInfo = res.userInfo;
            wx.showToast({ title: res.isNewUser ? '注册成功' : '登录成功', icon: 'success' });
            setTimeout(() => {
                wx.navigateBack({ delta: 1 });
            }, 800);
        } catch (err) {
            console.error('短信登录失败:', err);
        } finally {
            this.setData({ loading: false });
        }
    }
});
