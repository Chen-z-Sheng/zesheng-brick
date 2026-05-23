'use strict';

const { wxPhoneQuickLogin, donutIdentityLogin, sendSmsCode, smsCodeLogin } = require('../services/auth');

const CARRIER_LINKS = {
    cm: 'https://wap.cmpassport.com/resources/html/contract.html',
    cu: 'https://opencloud.wostore.cn/authz/resource/html/disclaimer.html?fromsdk=true',
    ct: 'https://e.189.cn/sdk/agreement/show.do?order=2&type=main&appKey=&hidetop=true&returnUrl='
};

function getInitialData() {
    return {
        identityMode: 'legacy',
        agreeOperator: false,
        agreePlatform: false,
        phoneMask: '获取中…',
        nativeOneClickReady: false,
        smsPhone: '',
        smsCode: '',
        sendSmsDisabled: true,
        loading: false
    };
}

function isDonutIdentityAvailable() {
    return typeof wx.getPhoneMask === 'function' && typeof wx.phoneSmsLogin === 'function';
}

function syncIdentityMode(ctx) {
    ctx.setData({ identityMode: isDonutIdentityAvailable() ? 'donut' : 'legacy' });
}

function refreshPhoneMask(ctx) {
    if (typeof wx.getPhoneMask !== 'function') {
        return;
    }
    wx.getPhoneMask({
        success(res) {
            if (res.phoneMask) {
                ctx.setData({ phoneMask: res.phoneMask, nativeOneClickReady: true });
            } else {
                ctx.setData({ phoneMask: '未获取到掩码', nativeOneClickReady: false });
            }
        },
        fail() {
            ctx.setData({
                phoneMask: '请开启蜂窝网络并在真机使用',
                nativeOneClickReady: false
            });
        }
    });
}

function onAgreeOperatorChange(ctx, e) {
    const vals = e.detail.value || [];
    ctx.setData({ agreeOperator: vals.indexOf('agree') >= 0 });
}

function onAgreePlatformChange(ctx, e) {
    const vals = e.detail.value || [];
    ctx.setData({ agreePlatform: vals.indexOf('agree') >= 0 });
}

function ensureAgreePlatform(ctx) {
    if (!ctx.data.agreePlatform) {
        wx.showToast({ title: '请先阅读并勾选同意相关协议', icon: 'none' });
        return false;
    }
    return true;
}

function openPlatformDoc(e) {
    const which = (e.currentTarget.dataset || {}).which || 'user';
    const type = which === 'privacy' ? 'privacy' : 'user';
    wx.navigateTo({
        url: `/pages/legal-doc/legal-doc?type=${type}`
    });
}

function openCarrierDoc(e) {
    const which = (e.currentTarget.dataset || {}).which || 'cm';
    const url = CARRIER_LINKS[which] || CARRIER_LINKS.cm;
    wx.setClipboardData({
        data: url,
        success() {
            wx.showToast({ title: '条款链接已复制', icon: 'none' });
        }
    });
}

function wxLoginPromise() {
    return new Promise((resolve) => {
        wx.login({ success: resolve, fail: () => resolve({}) });
    });
}

async function onPhoneOneClick(ctx, e, onSuccess) {
    if (ctx.data.loading) {
        return;
    }
    if (!ensureAgreePlatform(ctx)) {
        return;
    }
    const d = e.detail || {};
    if (d.errCode && d.errCode !== 0) {
        wx.showToast({ title: d.errMsg || '一键登录失败', icon: 'none' });
        return;
    }
    if (!d.code) {
        wx.showToast({ title: '未获取登录凭证', icon: 'none' });
        return;
    }
    await submitDonutCode(ctx, d.code, { manageLoading: true }, onSuccess);
}

function onSendSmsDone(e) {
    return e;
}

function onSmsPhoneInput(ctx, e) {
    const v = (e.detail.value || '').replace(/\D/g, '');
    ctx.setData({ smsPhone: v, sendSmsDisabled: !/^1\d{10}$/.test(v) });
}

function onSmsCodeInput(ctx, e) {
    const v = (e.detail.value || '').replace(/\D/g, '').slice(0, 6);
    ctx.setData({ smsCode: v });
}

async function submitSmsLogin(ctx, onSuccess) {
    if (ctx.data.loading) {
        return;
    }
    if (!ensureAgreePlatform(ctx)) {
        return;
    }
    const phone = (ctx.data.smsPhone || '').trim();
    const code = (ctx.data.smsCode || '').trim();
    if (!/^1\d{10}$/.test(phone)) {
        wx.showToast({ title: '请输入正确手机号', icon: 'none' });
        return;
    }
    if (!/^\d{6}$/.test(code)) {
        wx.showToast({ title: '请输入6位验证码', icon: 'none' });
        return;
    }
    ctx.setData({ loading: true });
    try {
        const res = await smsCodeLogin({
            phone,
            verifyCode: code
        });
        if (!res || !res.userInfo) {
            wx.showToast({ title: '登录失败，请重试', icon: 'none' });
            return;
        }
        getApp().globalData.userInfo = res.userInfo;
        wx.showToast({ title: res.isNewUser ? '注册成功' : '登录成功', icon: 'success' });
        setTimeout(() => {
            if (typeof onSuccess === 'function') {
                onSuccess(res);
            }
        }, 800);
    } catch (err) {
        console.error('短信验证码登录失败:', err);
    } finally {
        ctx.setData({ loading: false });
    }
}

async function requestSmsCode(ctx) {
    if (ctx.data.loading) {
        return;
    }
    const phone = (ctx.data.smsPhone || '').trim();
    if (!/^1\d{10}$/.test(phone)) {
        wx.showToast({ title: '请输入正确手机号', icon: 'none' });
        return;
    }
    ctx.setData({ loading: true });
    try {
        await sendSmsCode(phone);
        wx.showToast({ title: '验证码已发送', icon: 'success' });
    } catch (err) {
        console.error('发送短信验证码失败:', err);
    } finally {
        ctx.setData({ loading: false });
    }
}

async function submitDonutCode(ctx, identityCode, options, onSuccess) {
    const manageLoading = !options || options.manageLoading !== false;
    if (manageLoading) {
        ctx.setData({ loading: true });
    }
    try {
        const loginRes = await donutIdentityLogin({ code: identityCode });
        if (!loginRes || !loginRes.userInfo) {
            wx.showToast({ title: '登录失败，请重试', icon: 'none' });
            return;
        }
        getApp().globalData.userInfo = loginRes.userInfo;
        wx.showToast({ title: loginRes.isNewUser ? '注册成功' : '登录成功', icon: 'success' });
        setTimeout(() => {
            if (typeof onSuccess === 'function') {
                onSuccess(loginRes);
            }
        }, 800);
    } catch (err) {
        console.error('登录失败:', err);
    } finally {
        if (manageLoading) {
            ctx.setData({ loading: false });
        }
    }
}

async function onPhoneQuickLogin(ctx, e, onSuccess) {
    if (ctx.data.loading) {
        return;
    }
    if (!ensureAgreePlatform(ctx)) {
        return;
    }
    const detail = e.detail || {};
    if (detail.errMsg && detail.errMsg.indexOf('getPhoneNumber:ok') === -1) {
        if (detail.errMsg.indexOf('deny') !== -1 || detail.errMsg.indexOf('cancel') !== -1) {
            wx.showToast({ title: '需要授权手机号才能登录', icon: 'none' });
        }
        return;
    }
    const phoneCode = detail.code;
    if (!phoneCode) {
        wx.showToast({ title: '请升级客户端版本后重试', icon: 'none' });
        return;
    }
    ctx.setData({ loading: true });
    try {
        const loginRes = await wxLoginPromise();
        if (!loginRes.code) {
            wx.showToast({ title: '获取登录凭证失败', icon: 'none' });
            return;
        }
        const res = await wxPhoneQuickLogin({
            loginCode: loginRes.code,
            phoneCode
        });
        if (!res || !res.userInfo) {
            wx.showToast({ title: '登录失败，请重试', icon: 'none' });
            return;
        }
        getApp().globalData.userInfo = res.userInfo;
        wx.showToast({ title: res.isNewUser ? '注册成功' : '登录成功', icon: 'success' });
        setTimeout(() => {
            if (typeof onSuccess === 'function') {
                onSuccess(res);
            }
        }, 800);
    } catch (err) {
        console.error('登录失败:', err);
    } finally {
        ctx.setData({ loading: false });
    }
}

module.exports = {
    getInitialData,
    isDonutIdentityAvailable,
    syncIdentityMode,
    refreshPhoneMask,
    onAgreeOperatorChange,
    onAgreePlatformChange,
    openPlatformDoc,
    ensureAgreePlatform,
    openCarrierDoc,
    onPhoneOneClick,
    onSendSmsDone,
    onSmsPhoneInput,
    onSmsCodeInput,
    requestSmsCode,
    submitSmsLogin,
    submitDonutCode,
    onPhoneQuickLogin
};
