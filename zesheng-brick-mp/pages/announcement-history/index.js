const announcementService = require('../../services/announcement');

Page({
  data: {
    loading: true,
    announcementList: [],
    detailVisible: false,
    activeAnnouncement: null
  },

  onLoad() {
    this.loadAnnouncementList();
  },

  async onPullDownRefresh() {
    await this.loadAnnouncementList();
    wx.stopPullDownRefresh();
  },

  async loadAnnouncementList() {
    this.setData({ loading: true });
    try {
      const announcementList = this.sortAnnouncements(await announcementService.getHistoryList());
      this.setData({
        announcementList: Array.isArray(announcementList)
          ? announcementList.map((item) => ({
              ...item,
              createdAtText: this.formatDateTime(item.createdAt),
              previewText: this.createPreviewText(item.content)
            }))
          : []
      });
    } catch (err) {
      this.setData({ announcementList: [] });
      wx.showToast({
        title: '加载公告失败',
        icon: 'none'
      });
    } finally {
      this.setData({ loading: false });
    }
  },

  openDetail(e) {
    const index = e.currentTarget.dataset.index;
    const activeAnnouncement = this.data.announcementList[index];
    if (!activeAnnouncement) {
      return;
    }
    this.setData({
      detailVisible: true,
      activeAnnouncement
    });
  },

  closeDetail() {
    this.setData({
      detailVisible: false,
      activeAnnouncement: null
    });
  },

  noop() {},

  sortAnnouncements(list) {
    if (!Array.isArray(list)) {
      return [];
    }
    return [...list].sort((left, right) => {
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
  },

  createPreviewText(content) {
    if (!content) {
      return '';
    }
    return content
      .replace(/<[^>]+>/g, ' ')
      .replace(/&nbsp;/gi, ' ')
      .replace(/\s+/g, ' ')
      .trim()
      .slice(0, 72);
  },

  formatDateTime(value) {
    if (!value) {
      return '-';
    }
    return String(value).replace('T', ' ').slice(0, 19);
  }
});
