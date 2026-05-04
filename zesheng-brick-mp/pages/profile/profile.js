const { updateUserInfo } = require('../../services/auth');
const { getUserInfo } = require('../../utils/storage');

Page({
    /**
     * 页面的初始数据
     */
    data: {
        userInfo: {},
        tempNickName: '',
        tempPhone: '',
        isEditing: false,
        statusBarHeight: 20,
    },

    /**
     * 生命周期函数--监听页面加载
     */
    onLoad(options) {
        this.initStatusBarHeight();
        this.loadUserInfo();
    },

    /**
     * 初始化状态栏高度（微信官方推荐方式）
     */
    initStatusBarHeight() {
        try {
            const info = wx.getWindowInfo();
            const statusBarHeight = info.statusBarHeight || 20;
            this.setData({ statusBarHeight });
        } catch (e) {
            this.setData({ statusBarHeight: 20 });
        }
    },

    /**
     * 生命周期函数--监听页面显示
     */
    onShow() {
        this.loadUserInfo();
    },

    /**
     * 加载用户信息
     */
    loadUserInfo() {
        const userInfo = getUserInfo() || {};
        this.setData({
            userInfo: userInfo,
            tempNickName: userInfo.nickName || '',
            tempPhone: userInfo.phone || '',
        });
    },

    /**
     * 昵称输入
     * 使用 input type="nickname" 触发
     */
    onNickNameInput(e) {
        const { value } = e.detail;
        // 限制昵称长度为1-16个字符
        if (value.length > 16) {
            wx.showToast({
                title: '昵称长度不能超过16个字符',
                icon: 'none',
            });
            return;
        }
        this.setData({
            tempNickName: value,
            isEditing: true,
        });
    },

    /**
     * 昵称失焦时保存
     */
    onNickNameBlur(e) {
        const { value } = e.detail;
        // 验证昵称长度
        if (value.length > 16) {
            wx.showToast({
                title: '昵称长度不能超过16个字符',
                icon: 'none',
            });
            return;
        }
        this.setData({
            tempNickName: value,
        });
    },

    /**
     * 手机号输入
     */
    onPhoneInput(e) {
        const { value } = e.detail;
        this.setData({
            tempPhone: value,
            isEditing: true,
        });
    },

    /**
     * 保存用户信息
     */
    async saveUserInfo() {
        const { tempNickName, tempPhone, userInfo } = this.data;

        if (tempNickName && (tempNickName.length < 1 || tempNickName.length > 16)) {
            wx.showToast({
                title: '昵称长度必须在1-16个字符之间',
                icon: 'none',
            });
            return;
        }

        const hasNickNameChange = tempNickName !== undefined && tempNickName !== userInfo.nickName;
        const hasPhoneChange = tempPhone !== undefined && tempPhone !== userInfo.phone;

        if (!hasNickNameChange && !hasPhoneChange) {
            wx.showToast({
                title: '信息未修改',
                icon: 'none',
            });
            return;
        }

        wx.showLoading({ title: '保存中...' });

        try {
            const updateData = {
                nickName: tempNickName || userInfo.nickName,
                avatarUrl: userInfo.avatarUrl || userInfo.avatar,
                phone: tempPhone !== undefined ? tempPhone : userInfo.phone,
            };

            const updatedUserInfo = await updateUserInfo(updateData);

            this.setData({
                userInfo: updatedUserInfo,
                tempNickName: updatedUserInfo.nickName,
                tempPhone: updatedUserInfo.phone,
                isEditing: false,
            });

            wx.showToast({
                title: '保存成功',
                icon: 'success',
            });
        } catch (error) {
            console.error('更新用户信息失败:', error);
            wx.showToast({
                title: '保存失败，请检查网络连接',
                icon: 'none',
            });
        } finally {
            wx.hideLoading();
        }
    },





    /**
     * 返回上一页
     */
    goBack() {
        wx.navigateBack();
    },
});
