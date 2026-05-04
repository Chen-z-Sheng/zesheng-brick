// pages/form/form.js
Page({
  data: {},

    navigateToForm(e) {
        const { type } = e.currentTarget.dataset;
        const url = type === 'market'
            ? '/pages/sell-order/sell-order'
            : `/pages/form-schemes/form-schemes?type=${type}`;
        wx.navigateTo({ url });
    },

    onLoad() {}
});