const logger = require('../../utils/logger');
const { getProductDetail, getPriceHistory } = require('../../services/recycle-market');

Page({
    data: {
        level3Id: null,
        productName: '',
        price: 0,
        updateDate: '',
        priceHistory: [],
        trendDirection: 'up',
        vendors: [],
        specs: [],
    },

    onLoad(options) {
        const { level3Id, name, price } = options;
        this.setData({
            level3Id: level3Id || null,
            productName: decodeURIComponent(name || ''),
            price: parseFloat(price) || 0,
        });
        this.loadProductDetail();
        this.loadPriceHistory();
    },

    async loadProductDetail() {
        const level3Id = this.data.level3Id;
        if (!level3Id) return;
        try {
            const today = new Date().toISOString().slice(0, 10);
            const data = await getProductDetail(level3Id, today);
            if (data) {
                const priceNum = data.price != null ? Number(data.price) : 0;
                this.setData({
                    price: priceNum,
                    updateDate: data.updateDate || '',
                });
            }
        } catch (err) {
            logger.error('加载商品详情失败', err);
        }
    },

    async loadPriceHistory() {
        const level3Id = this.data.level3Id;
        if (!level3Id) return;
        try {
            const list = await getPriceHistory(level3Id, 30);
            const arr = Array.isArray(list) ? list : [];
            const priceHistory = arr.map((item) => ({
                date: this.formatChartDate(item.priceDate),
                price: Number(item.price) || 0,
            }));
            const trendDirection =
                priceHistory.length >= 2 && priceHistory[priceHistory.length - 1].price >= priceHistory[0].price
                    ? 'up'
                    : 'down';
            this.setData({ priceHistory, trendDirection });
            setTimeout(() => this.drawPriceChart(), 100);
        } catch (err) {
            logger.error('加载历史行情失败', err);
            this.setData({ priceHistory: [] });
        }
    },

    formatChartDate(dateStr) {
        if (!dateStr) return '';
        const d = new Date(dateStr);
        return `${d.getMonth() + 1}/${d.getDate()}`;
    },

    onReady() {
        setTimeout(() => this.drawPriceChart(), 300);
    },

    drawPriceChart() {
        const query = wx.createSelectorQuery();
        query.select('#priceChart')
            .fields({ node: true, size: true })
            .exec((res) => {
                if (!res[0] || !res[0].node) return;
                const canvas = res[0].node;
                const ctx = canvas.getContext('2d');
                const dpr = wx.getWindowInfo().pixelRatio;
                canvas.width = res[0].width * dpr;
                canvas.height = res[0].height * dpr;
                ctx.scale(dpr, dpr);
                const width = res[0].width;
                const height = res[0].height;
                ctx.clearRect(0, 0, width, height);

                const priceHistory = this.data.priceHistory;
                if (!priceHistory || priceHistory.length === 0) return;

                const prices = priceHistory.map((item) => item.price);
                const maxPrice = Math.max(...prices);
                const minPrice = Math.min(...prices);
                const range = maxPrice - minPrice || 1;

                const paddingLeft = 40;
                const paddingRight = 20;
                const paddingTop = 20;
                const paddingBottom = 40;
                const chartWidth = width - paddingLeft - paddingRight;
                const chartHeight = height - paddingTop - paddingBottom;

                ctx.beginPath();
                ctx.lineWidth = 1;
                ctx.strokeStyle = '#cccccc';
                ctx.moveTo(paddingLeft, paddingTop);
                ctx.lineTo(paddingLeft, height - paddingBottom);
                ctx.lineTo(width - paddingRight, height - paddingBottom);
                ctx.stroke();

                if (priceHistory.length > 1) {
                    ctx.beginPath();
                    ctx.lineWidth = 2;
                    ctx.strokeStyle = this.data.trendDirection === 'up' ? '#fa5151' : '#07c160';
                    for (let i = 0; i < priceHistory.length; i++) {
                        const x = paddingLeft + (i / (priceHistory.length - 1)) * chartWidth;
                        const normalizedPrice = (priceHistory[i].price - minPrice) / range;
                        const y = height - paddingBottom - normalizedPrice * chartHeight;
                        if (i === 0) ctx.moveTo(x, y);
                        else ctx.lineTo(x, y);
                    }
                    ctx.stroke();
                }

                ctx.font = '10px sans-serif';
                ctx.fillStyle = '#999999';
                ctx.textAlign = 'center';
                const step = Math.max(1, Math.floor(priceHistory.length / 7));
                for (let i = 0; i < priceHistory.length; i += step) {
                    const x = paddingLeft + (i / Math.max(1, priceHistory.length - 1)) * chartWidth;
                    ctx.fillText(priceHistory[i].date, x, height - paddingBottom + 15);
                }

                ctx.textAlign = 'right';
                for (let i = 0; i <= 4; i++) {
                    const price = minPrice + (i / 4) * range;
                    const y = height - paddingBottom - (i / 4) * chartHeight;
                    ctx.fillText('¥' + Math.round(price), paddingLeft - 5, y + 3);
                }
            });
    },
});
