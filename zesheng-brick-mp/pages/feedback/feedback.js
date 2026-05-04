const config = require('../../config/index');
const { getToken } = require('../../utils/storage');
const { submitFeedback } = require('../../services/feedback');

Page({
    data: {
        feedbackTypeOptions: ['请选择反馈类型', '功能异常', '产品建议', '体验问题', '其他'],
        feedbackTypeIndex: 0,
        content: '',
        imageUrls: [],
        submitting: false
    },

    onFeedbackTypeChange(e) {
        this.setData({
            feedbackTypeIndex: Number(e.detail.value) || 0
        });
    },

    onContentInput(e) {
        this.setData({
            content: e.detail.value || ''
        });
    },

    onChooseImage() {
        const remainCount = 3 - this.data.imageUrls.length;
        if (remainCount <= 0) {
            return;
        }
        wx.chooseMedia({
            count: remainCount,
            mediaType: ['image'],
            sourceType: ['album', 'camera'],
            success: (res) => {
                const files = res.tempFiles || [];
                files.forEach(file => {
                    if (file && file.tempFilePath) {
                        this.uploadImage(file.tempFilePath);
                    }
                });
            }
        });
    },

    uploadImage(filePath) {
        const token = getToken();
        wx.uploadFile({
            url: `${config.BASE_URL}/feedback/upload-image`,
            filePath,
            name: 'file',
            header: token ? { Authorization: `Bearer ${token}` } : {},
            success: (res) => {
                try {
                    const parsed = JSON.parse(res.data);
                    if (parsed && parsed.code === 200 && parsed.data) {
                        const nextUrls = this.data.imageUrls.concat(parsed.data).slice(0, 3);
                        this.setData({ imageUrls: nextUrls });
                    } else {
                        wx.showToast({ title: parsed?.msg || '图片上传失败', icon: 'none' });
                    }
                } catch (error) {
                    wx.showToast({ title: '图片上传失败', icon: 'none' });
                }
            },
            fail: () => {
                wx.showToast({ title: '图片上传失败', icon: 'none' });
            }
        });
    },

    onRemoveImage(e) {
        const index = Number(e.currentTarget.dataset.index);
        if (!Number.isInteger(index) || index < 0) {
            return;
        }
        const nextUrls = this.data.imageUrls.slice();
        nextUrls.splice(index, 1);
        this.setData({ imageUrls: nextUrls });
    },

    onPreviewImage(e) {
        const current = e.currentTarget.dataset.url || '';
        wx.previewImage({
            current,
            urls: this.data.imageUrls
        });
    },

    async onSubmit() {
        if (this.data.feedbackTypeIndex <= 0) {
            wx.showToast({ title: '请选择反馈类型', icon: 'none' });
            return;
        }
        const content = (this.data.content || '').trim();
        if (content.length < 5) {
            wx.showToast({ title: '问题描述至少 5 个字', icon: 'none' });
            return;
        }
        if (this.data.submitting) {
            return;
        }
        const feedbackType = this.data.feedbackTypeOptions[this.data.feedbackTypeIndex];
        this.setData({ submitting: true });
        try {
            await submitFeedback({
                feedbackType,
                content,
                imageUrls: this.data.imageUrls
            });
            wx.showToast({ title: '反馈提交成功', icon: 'success' });
            setTimeout(() => {
                wx.navigateBack();
            }, 1000);
        } catch (error) {
            wx.showToast({ title: error?.msg || error?.message || '提交失败', icon: 'none' });
        } finally {
            this.setData({ submitting: false });
        }
    },

    onGoHistory() {
        wx.navigateTo({
            url: '/pages/feedback-history/feedback-history'
        });
    }
});
