Component({
  properties: {
    msg: Object
  },

  data: {
    imgError: false
  },

  lifetimes: {
    ready: function () {
      // 重置图片错误状态（组件复用时需要）
      this.setData({ imgError: false });
    }
  },

  methods: {
    onImgLoad: function(e) {
      // 图片加载成功，确保错误状态被清除
      if (this.data.imgError) {
        this.setData({ imgError: false });
      }
    },

    onImgError: function(e) {
      console.error('[customImageMessage] 图片加载失败:', this.data.msg && this.data.msg.data ? this.data.msg.data.url : 'unknown');
      this.setData({ imgError: true });
    },

    onPreviewImage: function() {
      const url = this.data.msg && this.data.msg.data ? this.data.msg.data.url : '';
      if (!url) return;
      wx.previewImage({
        current: url,
        urls: [url]
      });
    }
  }
});
