const app = getApp();

Component({
  properties: {
    msg: Object,
    recording: Boolean // 语音输入时是否显示动图
  },

  data: {
    userAvatar: ''
  },

  lifetimes: {
    ready: function () {
      // 获取用户头像，避免使用 open-data（兼容性更好）
      try {
        const userInfo = app.globalData.userInfo || {};
        const avatar = userInfo.avatarUrl || userInfo.avatar || userInfo.userHeader || '';
        this.setData({ userAvatar: avatar });
      } catch (e) {
        // 使用默认头像
        this.setData({
          userAvatar: 'https://mmbiz.qpic.cn/mmbiz/icTdbqWNOwNRna42FI242Lcia07jQodd2FJGIYQfG0LAJGFxM4FbnQP6yfMxBgJ0F3YRqJCJ1aPAK2dQagdusBZg/0'
        });
      }
    }
  },

  methods: {
    onUserImgLoad: function(e) {
      // 图片加载成功
    },

    onUserImgError: function(e) {
      console.error('[customQueryMessage] 用户图片加载失败:', this.data.msg);
    },

    onPreviewUserImage: function() {
      const msg = this.data.msg || {};
      const url = (msg.data && msg.data.url) || msg.url || (msg.data && msg.data.imageUrl) || '';
      if (!url) return;
      wx.previewImage({
        current: url,
        urls: [url]
      });
    }
  }
});
