const {
    buildShareAppMessagePayload,
    buildShareTimelinePayload,
    showShareMenuForPage,
} = require('../../utils/page-share');

Page({
    data: {},

    onShareAppMessage() {
        return buildShareAppMessagePayload(this);
    },

    onShareTimeline() {
        return buildShareTimelinePayload(this);
    },

    navigateToForm(e) {
        const { type } = e.currentTarget.dataset;
        const url = type === 'market'
            ? '/pages/sell-order/sell-order'
            : `/pages/form-schemes/form-schemes?type=${type}`;
        wx.navigateTo({ url });
    },

    onLoad() {
        showShareMenuForPage(this);
    },

    onShow() {
        showShareMenuForPage(this);
    },
});
