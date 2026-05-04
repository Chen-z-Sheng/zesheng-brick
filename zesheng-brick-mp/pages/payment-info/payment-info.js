// pages/payment-info/payment-info.js
const { getUserInfo, getToken } = require('../../utils/storage');
const config = require('../../config/index');
const { get, post, patch } = require('../../utils/request');

Page({
  data: {
    formData: {
      realName: '',
      alipayAccount: '',
      wechatQRCode: '',
      alipayQRCode: '',
      bankCardNumber: '',
      bankName: '',
      bankBranch: ''
    },
    userId: '',
    paymentInfoId: '',
    wechatUploading: false,
    alipayUploading: false
  },

  onLoad() {
    const userInfo = getUserInfo() || {};
    const userId = userInfo.userId || userInfo.id;
    this.setData({ userId });
    this.loadPaymentInfo();
  },

  loadPaymentInfo() {
    const { userId } = this.data;
    if (!userId) return;

    const token = getToken();
    if (!token) return;

    get(`/payment-info/by-user/${userId}`, {}, { showLoading: false })
      .then((paymentInfo) => {
        if (paymentInfo) {
          const cur = this.data.formData;
          this.setData({
            formData: {
              realName: paymentInfo.realName || '',
              alipayAccount: paymentInfo.alipayAccount || '',
              wechatQRCode: paymentInfo.wechatQrcode || paymentInfo.wechatQRCode || cur.wechatQRCode || '',
              alipayQRCode: paymentInfo.alipayQrcode || paymentInfo.alipayQRCode || cur.alipayQRCode || '',
              bankCardNumber: paymentInfo.bankCardNo || paymentInfo.bankCardNumber || '',
              bankName: paymentInfo.bankName || '',
              bankBranch: paymentInfo.bankBranch || ''
            },
            paymentInfoId: paymentInfo.id
          });
        }
      })
      .catch((err) => {
        console.error('[payment-info] 加载打款信息失败:', err);
      });
  },

  onInputChange(e) {
    const field = e.currentTarget.dataset.field;
    let value = e.detail.value;
    if (field === 'bankName') {
      const filtered = value.replace(/[^\u4e00-\u9fa5]/g, '');
      if (value !== filtered) {
        wx.showToast({ title: '只能输入简体中文', icon: 'none' });
      }
      value = filtered;
    } else if (field === 'realName') {
      const filtered = value.replace(/[^\u4e00-\u9fa5]/g, '');
      if (value !== filtered) {
        wx.showToast({ title: '真实姓名只能输入简体中文', icon: 'none' });
      }
      value = filtered;
    } else if (field === 'bankCardNumber') {
      const filtered = value.replace(/[^0-9]/g, '');
      if (value !== filtered) {
        wx.showToast({ title: '银行卡卡号只能输入数字', icon: 'none' });
      }
      value = filtered;
    }
    this.setData({ [`formData.${field}`]: value });
  },

  chooseWechatQRCode() {
    this.chooseImage('wechatQRCode');
  },

  chooseAlipayQRCode() {
    this.chooseImage('alipayQRCode');
  },

  chooseImage(field) {
    wx.chooseMedia({
      count: 1,
      mediaType: ['image'],
      sizeType: ['compressed'],
      sourceType: ['album', 'camera'],
      success: (res) => {
        const tempFile = res.tempFiles[0];
        const tempPath = tempFile.tempFilePath;
        if (!tempPath) return;
        // 先用本地临时路径即时显示预览，用户立即看到所选图片
        const update = { [`formData.${field}`]: tempPath };
        if (field === 'wechatQRCode') update.wechatUploading = true;
        else update.alipayUploading = true;
        this.setData(update);
        this.doUpload(tempPath, field);
      }
    });
  },

  doUpload(tempFilePath, field) {
    const userId = this.data.userId;
    if (!userId) {
      wx.showToast({ title: '用户ID不存在', icon: 'none' });
      this.clearUploadState(field);
      return;
    }

    const directory = field === 'wechatQRCode' ? 'wechat-qrcode' : 'alipay-qrcodes';
    const token = getToken();
    if (!token) {
      wx.showToast({ title: '请先登录', icon: 'none' });
      this.clearUploadState(field);
      return;
    }

    wx.showLoading({ title: '上传中...' });

    const uploadUrl = `${config.BASE_URL}/api/oss/upload?directory=${encodeURIComponent(directory)}&userId=${encodeURIComponent(userId)}`;
    wx.uploadFile({
      url: uploadUrl,
      filePath: tempFilePath,
      name: 'file',
      header: { 'Authorization': `Bearer ${token}` },
      success: (res) => {
        try {
          const body = typeof res.data === 'string' ? JSON.parse(res.data) : res.data;
          const fileUrl = body?.data?.fileUrl || body?.data?.url;
          if (res.statusCode === 200 && fileUrl) {
            this.setData({
              [`formData.${field}`]: fileUrl,
              wechatUploading: field === 'wechatQRCode' ? false : this.data.wechatUploading,
              alipayUploading: field === 'alipayQRCode' ? false : this.data.alipayUploading
            });
            wx.showToast({ title: '上传成功', icon: 'success' });
          } else {
            wx.showToast({ title: body?.msg || '上传失败', icon: 'none' });
            this.setData({ [`formData.${field}`]: '' });
          }
        } catch (e) {
          wx.showToast({ title: '解析响应失败', icon: 'none' });
          this.setData({ [`formData.${field}`]: '' });
        }
        this.clearUploadState(field);
      },
      fail: () => {
        wx.showToast({ title: '上传失败', icon: 'none' });
        this.setData({ [`formData.${field}`]: '' });
        this.clearUploadState(field);
      },
      complete: () => {
        wx.hideLoading();
      }
    });
  },

  clearUploadState(field) {
    if (field === 'wechatQRCode') {
      this.setData({ wechatUploading: false });
    } else {
      this.setData({ alipayUploading: false });
    }
  },

  removeWechatQRCode() {
    this.setData({ 'formData.wechatQRCode': '', wechatUploading: false });
  },

  removeAlpayQRCode() {
    this.setData({ 'formData.alipayQRCode': '', alipayUploading: false });
  },

  isRemoteUrl(url) {
    return url && (url.startsWith('http://') || url.startsWith('https://'));
  },

  // 提交表单
  submitForm(e) {
    const { formData, userId, paymentInfoId } = this.data;

    // 表单验证
    if (!formData.realName) {
      wx.showToast({ title: '请输入真实姓名', icon: 'none' });
      return;
    }
    if (!formData.alipayAccount) {
      wx.showToast({ title: '请输入支付宝账号', icon: 'none' });
      return;
    }
    if (!formData.wechatQRCode) {
      wx.showToast({ title: '请上传微信收款码', icon: 'none' });
      return;
    }
    if (!this.isRemoteUrl(formData.wechatQRCode)) {
      wx.showToast({ title: '微信收款码上传中，请稍候', icon: 'none' });
      return;
    }
    if (!formData.alipayQRCode) {
      wx.showToast({ title: '请上传支付宝收款码', icon: 'none' });
      return;
    }
    if (!this.isRemoteUrl(formData.alipayQRCode)) {
      wx.showToast({ title: '支付宝收款码上传中，请稍候', icon: 'none' });
      return;
    }
    if (!formData.bankCardNumber) {
      wx.showToast({ title: '请输入银行卡号', icon: 'none' });
      return;
    }
    if (!formData.bankName) {
      wx.showToast({ title: '请输入开户银行', icon: 'none' });
      return;
    }
    if (!formData.bankBranch) {
      wx.showToast({ title: '请输入开户支行', icon: 'none' });
      return;
    }

    const token = getToken();
    if (!token) {
      wx.showToast({ title: '请先登录', icon: 'none' });
      return;
    }

    const requestData = {
      realName: formData.realName,
      alipayAccount: formData.alipayAccount,
      wechatQrcode: formData.wechatQRCode,
      alipayQrcode: formData.alipayQRCode,
      bankCardNo: formData.bankCardNumber,
      bankName: formData.bankName,
      bankBranch: formData.bankBranch,
      userId: userId
    };

    wx.showLoading({ title: '保存中...' });

    if (paymentInfoId) {
      // 更新打款信息
      patch(`/payment-info/${paymentInfoId}`, requestData)
        .then(() => {
          wx.showToast({ title: '保存成功', icon: 'success' });
          setTimeout(() => { wx.navigateBack(); }, 1200);
        })
        .catch(() => {
          wx.showToast({ title: '保存失败', icon: 'none' });
        })
        .finally(() => { wx.hideLoading(); });
    } else {
      // 新增打款信息
      post('/payment-info', requestData)
        .then(() => {
          wx.showToast({ title: '保存成功', icon: 'success' });
          setTimeout(() => { wx.navigateBack(); }, 1200);
        })
        .catch(() => {
          wx.showToast({ title: '保存失败', icon: 'none' });
        })
        .finally(() => { wx.hideLoading(); });
    }
  },

  resetForm() {
    this.setData({
      formData: {
        realName: '',
        alipayAccount: '',
        wechatQRCode: '',
        alipayQRCode: '',
        bankCardNumber: '',
        bankName: '',
        bankBranch: ''
      },
      paymentInfoId: '',
      wechatUploading: false,
      alipayUploading: false
    });
  }
});
