Page({
    data: {
        url: '',
    },

    onLoad(options) {
        const rawUrl = options && options.url ? options.url : '';
        let url = '';
        try {
            url = rawUrl ? decodeURIComponent(rawUrl) : '';
        } catch (e) {
            url = '';
        }
        const t = (url || '').trim();
        if (!t || t === 'null' || t === 'undefined') {
            wx.showToast({ title: '链接无效', icon: 'none' });
            setTimeout(() => wx.navigateBack(), 1500);
            return;
        }
        this.setData({ url: t });
    },
});

