const configService = require('../../services/config');
const announcementService = require('../../services/announcement');
const { getToken } = require('../../utils/storage');
const app = getApp();

Page({
  data: {
    needScroll: false,
    contact: '',
    bannerList: [],
    announcementText: '欢迎使用泽晟搬砖助手！',
    announcementScrollSpeed: 8,
    announcementScrollDuration: 10,
    /** 需弹窗的公告，有则显示弹层，无为 null */
    announcementPopup: null
  },

  onLoad() {
    this.loadBannerList();
    this.loadAnnouncementBanner();
    if (getToken()) {
      this.loadAdminWechatContact();
    }
  },

  onShow() {
    this.loadAnnouncementBanner();
    this.fetchAnnouncementPopup();
    if (getToken()) {
      this.loadAdminWechatContact();
    }
  },

  /** 首页客服微信号（sys_config：admin_wechat_account） */
  loadAdminWechatContact() {
    configService.fetchPublicSysConfigByKey(configService.ADMIN_WECHAT_ACCOUNT_KEY)
      .then((value) => {
        const text = value != null ? String(value).trim() : '';
        this.setData({ contact: text });
      })
      .catch(() => {
        this.setData({ contact: '' });
      });
  },

  /** 从 OSS index-banner 目录加载轮播图列表，目录下有哪些图片就展示哪些 */
  loadBannerList() {
    configService.getBannerList().then((list) => {
      const bannerList = (list && Array.isArray(list) && list.length > 0) ? list : [];
      this.setData({ bannerList });
    }).catch(() => {
      this.setData({ bannerList: [] });
    });
  },

  async loadAnnouncementBanner() {
    try {
      const announcementList = await announcementService.getHistoryList();
      const currentAnnouncement = this.pickCurrentAnnouncement(announcementList);
      const announcementText = this.getAnnouncementBannerText(currentAnnouncement);
      this.setData({
        announcementText,
        announcementScrollDuration: this.calculateAnnouncementScrollDuration(announcementText)
      }, () => {
        this.checkAnnouncementScroll();
      });
    } catch (err) {
      this.setData({
        announcementText: '欢迎使用泽晟搬砖助手！',
        announcementScrollDuration: this.calculateAnnouncementScrollDuration('欢迎使用泽晟搬砖助手！')
      }, () => {
        this.checkAnnouncementScroll();
      });
    }
  },

  /**
   * 检查公告是否需要滚动
   */
  checkAnnouncementScroll() {
    const query = wx.createSelectorQuery();
    query.select('.announcement-measure-text').boundingClientRect();
    query.select('.announcement-wrapper').boundingClientRect();
    query.exec((res) => {
      if (res && res[0] && res[1]) {
        this.setData({
          needScroll: res[0].width > res[1].width
        });
      }
    });
  },

  /**
   * 登录完成后若停留在首页，补拉公告（未登录时不会把「已检查」锁死）
   */
  onLoginSuccess() {
    this.fetchAnnouncementPopup();
  },

  /** 拉取需弹窗的公告（无公告或用户关闭后本轮小程序生命周期内不再打扰） */
  async fetchAnnouncementPopup() {
    if (app.globalData.announcementPopupChecked) {
      return;
    }
    // 首次进入首页时 onLaunch 可能尚未写入 userInfo，不能仅用 globalData 判断登录态
    if (!getToken()) {
      return;
    }

    const latestUserInfo = await app.refreshUserInfo();
    if (!latestUserInfo || !latestUserInfo.userId) {
      return;
    }

    announcementService.getLatestToShow().then((announcement) => {
      if (announcement && announcement.id) {
        announcement.formattedDate = this.formatDate(announcement.updatedAt);
        this.setData({ announcementPopup: announcement });
        return;
      }
      app.globalData.announcementPopupChecked = true;
      this.setData({ announcementPopup: null });
    }).catch(() => {
      app.globalData.announcementPopupChecked = true;
      this.setData({ announcementPopup: null });
    });
  },

  onPopupBoxTap() {
    // 阻止点击弹窗内容区时关闭
  },

  pickCurrentAnnouncement(list) {
    if (!Array.isArray(list) || list.length === 0) {
      return null;
    }
    const sortedList = [...list].sort((left, right) => {
      const leftStatus = Number(left?.status) === 1 ? 1 : 0;
      const rightStatus = Number(right?.status) === 1 ? 1 : 0;
      if (leftStatus !== rightStatus) {
        return rightStatus - leftStatus;
      }

      const leftUpdatedAt = new Date(left?.updatedAt || left?.createdAt || 0).getTime();
      const rightUpdatedAt = new Date(right?.updatedAt || right?.createdAt || 0).getTime();
      if (leftUpdatedAt !== rightUpdatedAt) {
        return rightUpdatedAt - leftUpdatedAt;
      }

      return Number(right?.id || 0) - Number(left?.id || 0);
    });
    return sortedList[0] || null;
  },

  calculateAnnouncementScrollDuration(text) {
    const content = String(text || '').trim();
    const speed = Number(this.data.announcementScrollSpeed) || 8;
    const charCount = Math.max(content.length, 1);
    return Math.max(8, Math.ceil(charCount / speed));
  },

  formatDate(dateStr) {
    if (!dateStr) return '';
    const date = new Date(dateStr);
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hour = String(date.getHours()).padStart(2, '0');
    const minute = String(date.getMinutes()).padStart(2, '0');
    return `${year}-${month}-${day} ${hour}:${minute}`;
  },

  getAnnouncementBannerText(announcement) {
    if (!announcement) {
      return '欢迎使用泽晟搬砖助手！';
    }

    const plainContent = String(announcement.content || '')
      .replace(/<[^>]+>/g, ' ')
      .replace(/&nbsp;/gi, ' ')
      .replace(/\s+/g, ' ')
      .trim();

    if (plainContent) {
      return plainContent;
    }

    if (announcement.title) {
      return `最新公告：${announcement.title}`;
    }

    return '欢迎使用泽晟搬砖助手！';
  },

  /** 我知道了：写入忽略表，之后不再弹本条 */
  onAnnouncementAcknowledge() {
    const popup = this.data.announcementPopup;
    if (popup && popup.id) {
      announcementService.ignore(popup.id).catch(() => {});
    }
    app.globalData.announcementPopupChecked = true;
    this.setData({ announcementPopup: null });
  },

  /** 稍后提醒 / 点遮罩或关闭：仅关闭，本轮不再反复弹出；下次冷启动仍会提示 */
  onAnnouncementRemindLater() {
    app.globalData.announcementPopupChecked = true;
    this.setData({ announcementPopup: null });
  },

  /** 公告栏点击事件（顶部滚动条） */
  onAnnouncementTap() {
    wx.navigateTo({
      url: '/pages/announcement-history/index'
    });
  },

  /**
   * 点击复制联系方式
   */
  copyContact(e) {
    const contact = e?.currentTarget?.dataset?.contact || this.data.contact || '';
    if (!contact) {
      wx.showToast({ title: '联系方式缺失', icon: 'none' });
      return;
    }
    wx.setClipboardData({
      data: contact,
      success: () => {
        wx.showToast({ title: '已复制', icon: 'success' });
      },
      fail: () => {
        wx.showToast({ title: '复制失败', icon: 'none' });
      }
    });
  }
})