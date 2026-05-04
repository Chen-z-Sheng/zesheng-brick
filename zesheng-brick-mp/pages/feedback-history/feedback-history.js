const { getMyFeedbackPage } = require('../../services/feedback');

function formatTime(value) {
    if (!value) {
        return '';
    }
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) {
        return value;
    }
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hour = String(date.getHours()).padStart(2, '0');
    const minute = String(date.getMinutes()).padStart(2, '0');
    return `${year}-${month}-${day} ${hour}:${minute}`;
}

Page({
    data: {
        loading: false,
        list: []
    },

    onShow() {
        this.loadList();
    },

    async loadList() {
        this.setData({ loading: true });
        try {
            const pageData = await getMyFeedbackPage({
                pageNum: 1,
                pageSize: 50
            });
            const records = Array.isArray(pageData?.records) ? pageData.records : [];
            const list = records.map(item => ({
                ...item,
                createdAtText: formatTime(item.createdAt)
            }));
            this.setData({ list });
        } catch (error) {
            this.setData({ list: [] });
            wx.showToast({ title: error?.msg || error?.message || '加载失败', icon: 'none' });
        } finally {
            this.setData({ loading: false });
        }
    }
});
