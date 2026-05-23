const logger = require('../../utils/logger');
const { getCategories, getProducts, getPriceHistory } = require('../../services/recycle-market');
const {
    buildShareAppMessagePayload,
    buildShareTimelinePayload,
    showShareMenuForPage,
} = require('../../utils/page-share');

function normalizeCategoryTree(list) {
    if (!Array.isArray(list)) return [];
    return list.map((level1) => ({
        id: level1.id ?? level1.level1Id ?? null,
        name: (level1.name || '').trim(),
        children: (Array.isArray(level1.children) ? level1.children : []).map((level2) => ({
            id: level2.id ?? level2.level2Id ?? null,
            name: (level2.name || '').trim(),
            children: (Array.isArray(level2.children) ? level2.children : []).map((level3) => ({
                id: level3.id ?? level3.level3Id ?? null,
                level2Id: level3.level2Id ?? level2.id ?? null,
                name: (level3.name || '').trim(),
            })).filter((item) => item.id != null && item.name),
        })).filter((item) => item.id != null && item.name),
    })).filter((item) => item.id != null && item.name);
}

function findCategoryPathByKeyword(categories, keyword) {
    const normalizedKeyword = (keyword || '').trim().toLowerCase();
    if (!normalizedKeyword) {
        return null;
    }
    for (let i = 0; i < categories.length; i += 1) {
        const level1 = categories[i];
        const level2List = Array.isArray(level1.children) ? level1.children : [];
        for (let j = 0; j < level2List.length; j += 1) {
            const level2 = level2List[j];
            const level3List = Array.isArray(level2.children) ? level2.children : [];
            for (let k = 0; k < level3List.length; k += 1) {
                const level3 = level3List[k];
                const level3Name = (level3.name || '').toLowerCase();
                if (level3Name.includes(normalizedKeyword)) {
                    return {
                        level1Id: level1.id,
                        level2Id: level2.id,
                    };
                }
            }
        }
    }
    return null;
}

function toNumberOrNull(value) {
    if (value === null || value === undefined || value === '') {
        return null;
    }
    const num = Number(value);
    return Number.isFinite(num) ? num : null;
}

function normalizeSearchText(value) {
    if (value === null || value === undefined) {
        return '';
    }
    const text = String(value);
    const lower = text.trim().toLowerCase();
    if (lower === 'undefined' || lower === 'null') {
        return '';
    }
    return text;
}

function sanitizeKeyword(value) {
    const normalized = normalizeSearchText(value);
    const trimmed = normalized.trim();
    if (!trimmed) {
        return '';
    }
    if (trimmed.toLowerCase() === 'undefined' || trimmed.toLowerCase() === 'null') {
        return '';
    }
    return trimmed;
}

function toDateText(value) {
    if (!value) {
        return '';
    }
    const text = String(value).trim();
    if (!text) {
        return '';
    }
    if (text.length >= 10 && text.includes('-')) {
        return text.slice(0, 10);
    }
    const parsedDate = new Date(text);
    if (Number.isNaN(parsedDate.getTime())) {
        return '';
    }
    const year = parsedDate.getFullYear();
    const month = String(parsedDate.getMonth() + 1).padStart(2, '0');
    const day = String(parsedDate.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
}

function resolveLatestMarket(product) {
    const priceCandidates = [
        product.recyclePrice,
        product.latestRecyclePrice,
        product.price,
        product.latestPrice,
        product.todayPrice,
    ];
    const dateCandidates = [
        product.priceDate,
        product.latestPriceDate,
        product.recyclePriceDate,
        product.updateDate,
        product.updateTime,
    ];

    let recyclePrice = null;
    for (let i = 0; i < priceCandidates.length; i += 1) {
        const parsed = toNumberOrNull(priceCandidates[i]);
        if (parsed !== null) {
            recyclePrice = parsed;
            break;
        }
    }

    let latestDate = '';
    for (let i = 0; i < dateCandidates.length; i += 1) {
        const parsed = toDateText(dateCandidates[i]);
        if (parsed) {
            latestDate = parsed;
            break;
        }
    }

    return {
        recyclePrice,
        latestDate,
    };
}

function resolveTrendDirection(product, recyclePrice) {
    const yesterdayCandidates = [
        product.yesterdayPrice,
        product.prevPrice,
        product.previousPrice,
        product.lastPrice,
        product.beforePrice,
    ];

    let yesterdayPrice = null;
    for (let i = 0; i < yesterdayCandidates.length; i += 1) {
        const parsed = toNumberOrNull(yesterdayCandidates[i]);
        if (parsed !== null) {
            yesterdayPrice = parsed;
            break;
        }
    }

    const explicitDirection = (product.trendDirection || product.direction || product.priceTrend || '').toString().toLowerCase();
    if (explicitDirection === 'up' || explicitDirection === 'rise') {
        return 'up';
    }
    if (explicitDirection === 'down' || explicitDirection === 'fall') {
        return 'down';
    }

    if (recyclePrice === null || yesterdayPrice === null) {
        return 'flat';
    }
    if (recyclePrice > yesterdayPrice) {
        return 'up';
    }
    if (recyclePrice < yesterdayPrice) {
        return 'down';
    }
    return 'flat';
}

function normalizeProductList(list) {
    if (!Array.isArray(list)) {
        return [];
    }
    return list
        .map((item) => {
            const id = item.id ?? item.level2Id ?? item.productId ?? '';
            const productName = (item.name ?? item.productName ?? '').toString().trim();
            const { recyclePrice, latestDate } = resolveLatestMarket(item);
            const trendDirection = resolveTrendDirection(item, recyclePrice);
            if (!productName) {
                return null;
            }
            return {
                id: String(id || productName),
                level1Id: item.level1Id ?? null,
                level2Id: item.level2Id ?? item.id ?? null,
                level3Id: item.level3Id ?? null,
                productName,
                recyclePrice,
                displayPrice: recyclePrice === null ? '--' : recyclePrice.toFixed(2),
                latestDate,
                trendDirection,
            };
        })
        .filter(Boolean);
}

function convertHistoryList(historyList) {
    const list = Array.isArray(historyList) ? historyList : [];
    return list
        .map((item) => {
            const price = toNumberOrNull(item.price);
            const dateText = toDateText(item.priceDate || item.date || item.updateDate);
            if (price === null || !dateText) {
                return null;
            }
            return {
                price,
                date: dateText,
                label: dateText.slice(5),
            };
        })
        .filter(Boolean)
        .sort((a, b) => new Date(a.date).getTime() - new Date(b.date).getTime());
}

Page({
    data: {
        pageLoading: false,
        productsLoading: false,
        errorMessage: '',
        level1Categories: [],
        activeLevel1Id: null,
        level2Categories: [],
        activeLevel2Id: null,
        keyword: '',
        displayKeyword: '',
        thirdCategoryProducts: [],
        filteredThirdProducts: [],
        historyVisible: false,
        selectedProductName: '',
        selectedProductPrice: '--',
        selectedProductDate: '',
        historyTrendDirection: 'flat',
        historyList: [],
        level3CanScroll: false,
    },

    onShareAppMessage() {
        return buildShareAppMessagePayload(this);
    },

    onShareTimeline() {
        return buildShareTimelinePayload(this);
    },

    onLoad() {
        showShareMenuForPage(this);
        this._level2ProductMap = {};
        this._globalIndexLoadingPromise = null;
        this._globalSearchReady = false;
        this._level2ToLevel1Map = {};
        this._globalSearchMode = false;
        this._searchDebounceTimer = null;
        this._level2ScrollTop = 0;
        this.syncKeyword('');
        this.initializePage();
    },

    onShow() {
        showShareMenuForPage(this);
        this.syncKeyword(this.data.keyword);
    },

    onPullDownRefresh() {
        this.reloadCurrentCategory().finally(() => {
            wx.stopPullDownRefresh();
        });
    },

    async initializePage() {
        this.setData({
            pageLoading: true,
            errorMessage: '',
        });
        try {
            const categories = await this.loadCategories();
            if (!categories.length) {
                this.setData({
                    pageLoading: false,
                    errorMessage: '暂无可用分类',
                    thirdCategoryProducts: [],
                    filteredThirdProducts: [],
                });
                return;
            }
            const firstLevel1 = categories[0];
            const level2Categories = firstLevel1.children || [];
            const firstLevel2 = level2Categories[0] || null;
            this.setData({
                level1Categories: categories,
                activeLevel1Id: firstLevel1.id,
                level2Categories,
                activeLevel2Id: firstLevel2 ? firstLevel2.id : null,
            });
            this.buildCategoryRelationMap(categories);
            if (firstLevel2 && firstLevel2.id != null) {
                await this.loadProductsByLevel2(firstLevel2.id);
            } else {
                this.setData({
                    pageLoading: false,
                    thirdCategoryProducts: [],
                    filteredThirdProducts: [],
                });
            }
            this.preloadGlobalProductIndex();
        } catch (error) {
            logger.error('初始化行情页失败', error);
            this.setData({
                pageLoading: false,
                errorMessage: '行情加载失败，请稍后重试',
            });
        }
    },

    async loadCategories() {
        const list = await getCategories();
        return normalizeCategoryTree(list);
    },

    buildCategoryRelationMap(categories) {
        const map = {};
        (categories || []).forEach((level1) => {
            (level1.children || []).forEach((level2) => {
                map[String(level2.id)] = String(level1.id);
            });
        });
        this._level2ToLevel1Map = map;
    },

    getLevel2ProductsFromCache(level2Id) {
        if (level2Id == null || !this._level2ProductMap) {
            return null;
        }
        return this._level2ProductMap[String(level2Id)] || null;
    },

    setLevel2ProductsToCache(level2Id, list) {
        if (level2Id == null) {
            return;
        }
        this._level2ProductMap[String(level2Id)] = Array.isArray(list) ? list : [];
    },

    async loadProductsByLevel2(level2Id, options = {}) {
        const { useCache = true } = options;
        if (useCache) {
            const cached = this.getLevel2ProductsFromCache(level2Id);
            if (cached) {
                this.setData({
                    thirdCategoryProducts: cached,
                    pageLoading: false,
                    productsLoading: false,
                    errorMessage: '',
                });
                this.applyKeywordFilter(this.data.keyword);
                this.scheduleMeasureLevel3Scrollbar();
                return;
            }
        }
        this.setData({
            pageLoading: false,
            productsLoading: true,
            errorMessage: '',
        });
        try {
            const list = await getProducts(null, level2Id, null);
            const products = normalizeProductList(list);
            this.setLevel2ProductsToCache(level2Id, products);
            this.setData({
                thirdCategoryProducts: products,
                productsLoading: false,
            });
            this.applyKeywordFilter(this.data.keyword);
            this.scheduleMeasureLevel3Scrollbar();
        } catch (error) {
            logger.error('加载分类行情失败', error);
            this.setData({
                thirdCategoryProducts: [],
                filteredThirdProducts: [],
                productsLoading: false,
                errorMessage: '查询行情失败，请稍后重试',
                level3CanScroll: false,
            });
        }
    },

    /**
     * 分批预加载商品数据，避免一次性发起过多请求
     * @param {Array} tasks - 预加载任务数组
     * @param {number} batchSize - 每批数量，默认3
     */
    async preloadWithBatching(tasks, batchSize = 3) {
        const results = [];
        for (let i = 0; i < tasks.length; i += batchSize) {
            const batch = tasks.slice(i, i + batchSize);
            // eslint-disable-next-line no-await-in-loop
            const batchResults = await Promise.all(batch);
            results.push(...batchResults);
        }
        return results;
    },

    async preloadGlobalProductIndex() {
        if (this._globalSearchReady) {
            return;
        }
        if (this._globalIndexLoadingPromise) {
            return this._globalIndexLoadingPromise;
        }
        const categories = this.data.level1Categories || [];
        // 预加载所有二级分类商品，支持全局搜索自动定位
        const tasks = [];
        // 只预加载前10个分类，避免过多请求
        const limitedCategories = categories.slice(0, 10);
        limitedCategories.forEach((level1) => {
            (level1.children || []).slice(0, 5).forEach((level2) => {
                const level2Id = level2.id;
                if (level2Id == null || this.getLevel2ProductsFromCache(level2Id)) {
                    return;
                }
                tasks.push(
                    getProducts(level1.id, level2Id, null)
                        .then((list) => {
                            const products = normalizeProductList(list).map((item) => ({
                                ...item,
                                level1Id: item.level1Id || level1.id,
                                level2Id: item.level2Id || level2Id,
                            }));
                            this.setLevel2ProductsToCache(level2Id, products);
                        })
                        .catch((error) => {
                            logger.warn('预加载分类商品失败', error);
                            this.setLevel2ProductsToCache(level2Id, []);
                        })
                );
            });
        });

        this._globalIndexLoadingPromise = (async () => {
            // 分批执行，每批3个请求
            await this.preloadWithBatching(tasks, 3);
            this._globalSearchReady = true;
        })().finally(() => {
            this._globalIndexLoadingPromise = null;
        });
        return this._globalIndexLoadingPromise;
    },

    findGlobalMatchedProduct(keyword) {
        const lowerKeyword = (keyword || '').toLowerCase();
        if (!lowerKeyword) {
            return null;
        }
        const level1List = this.data.level1Categories || [];
        for (let i = 0; i < level1List.length; i += 1) {
            const level1 = level1List[i];
            const level2List = level1.children || [];
            for (let j = 0; j < level2List.length; j += 1) {
                const level2 = level2List[j];
                const products = this.getLevel2ProductsFromCache(level2.id) || [];
                const matched = products.find((item) => (item.productName || '').toLowerCase().includes(lowerKeyword));
                if (matched) {
                    return {
                        level1Id: level1.id,
                        level2Id: level2.id,
                        product: matched,
                    };
                }
            }
        }
        return null;
    },

    reloadCurrentCategory() {
        const currentId = this.data.activeLevel2Id;
        if (currentId == null) {
            return Promise.resolve();
        }
        return this.loadProductsByLevel2(currentId);
    },

    onLevel1Tap(event) {
        const level1Id = event.currentTarget.dataset.id;
        if (level1Id == null || String(level1Id) === String(this.data.activeLevel1Id)) {
            return;
        }
        const level1 = (this.data.level1Categories || []).find((item) => String(item.id) === String(level1Id));
        const level2Categories = level1 ? (level1.children || []) : [];
        const firstLevel2 = level2Categories[0] || null;
        this.setData({
            activeLevel1Id: level1Id,
            level2Categories,
            activeLevel2Id: firstLevel2 ? firstLevel2.id : null,
        });
        this._globalSearchMode = false;
        this.resetLevel2Scroll();
        if (firstLevel2 && firstLevel2.id != null) {
            this.loadProductsByLevel2(firstLevel2.id);
            return;
        }
        this.setData({
            pageLoading: false,
            productsLoading: false,
            thirdCategoryProducts: [],
            filteredThirdProducts: [],
        });
    },

    getLevel2ScrollNode(callback) {
        wx.createSelectorQuery()
            .in(this)
            .select('.level2-column')
            .node()
            .exec((res) => {
                const node = res && res[0] ? res[0].node : null;
                callback(node);
            });
    },

    scrollLevel2To(top, animated) {
        const targetTop = Math.max(0, Number(top) || 0);
        this._level2ScrollTop = targetTop;
        this.getLevel2ScrollNode((node) => {
            if (!node || typeof node.scrollTo !== 'function') {
                return;
            }
            node.scrollTo({
                top: targetTop,
                animated: animated !== false,
            });
        });
    },

    resetLevel2Scroll() {
        this.scrollLevel2To(0, false);
    },

    onLevel2Scroll(event) {
        const scrollTop = event && event.detail ? event.detail.scrollTop : 0;
        this._level2ScrollTop = Number(scrollTop) || 0;
    },

    /**
     * 仅当选中项被遮挡时做最小位移，上下留空隙；已在可视区内则不滚动
     */
    ensureLevel2Visible(level2Id) {
        if (level2Id == null) {
            return;
        }
        const query = wx.createSelectorQuery().in(this);
        query.select('.level2-column').fields({ size: true, scrollOffset: true });
        query.select('.level2-inner').boundingClientRect();
        query.select('#level2-' + level2Id).boundingClientRect();
        query.exec((res) => {
            const column = res && res[0];
            const inner = res && res[1];
            const item = res && res[2];
            if (!column || !inner || !item || !column.height) {
                return;
            }

            const containerHeight = column.height;
            const currentScrollTop = Number(column.scrollTop);
            const scrollTop = Number.isFinite(currentScrollTop)
                ? currentScrollTop
                : (this._level2ScrollTop || 0);
            const itemTop = item.top - inner.top;
            const itemHeight = item.height;
            const itemBottom = itemTop + itemHeight;
            const innerHeight = inner.height || 0;
            const maxScrollTop = Math.max(0, innerHeight - containerHeight);

            const windowInfo = wx.getWindowInfo();
            const edgeGap = Math.round((windowInfo.windowWidth / 750) * 48);
            const viewTop = scrollTop + edgeGap;
            const viewBottom = scrollTop + containerHeight - edgeGap;

            let targetScrollTop = scrollTop;
            if (itemTop < viewTop) {
                targetScrollTop = Math.max(0, itemTop - edgeGap);
            } else if (itemBottom > viewBottom) {
                targetScrollTop = itemBottom - containerHeight + edgeGap;
            } else {
                return;
            }

            targetScrollTop = Math.min(Math.max(0, targetScrollTop), maxScrollTop);
            if (Math.abs(targetScrollTop - scrollTop) < 1) {
                return;
            }

            this.scrollLevel2To(targetScrollTop, true);
        });
    },

    onLevel2Tap(event) {
        const level2Id = event.currentTarget.dataset.id;
        if (level2Id == null || String(level2Id) === String(this.data.activeLevel2Id)) {
            return;
        }
        this.setData({ activeLevel2Id: level2Id });
        this._globalSearchMode = false;
        this.loadProductsByLevel2(level2Id);
    },

    syncKeyword(value) {
        const safeKeyword = sanitizeKeyword(value);
        this.setData({
            keyword: safeKeyword,
            displayKeyword: safeKeyword,
        });
        return safeKeyword;
    },

    onKeywordInput(event) {
        const rawKeyword = event && event.detail ? event.detail.value : '';
        this.syncKeyword(rawKeyword);

        // 防抖：300ms 后再执行搜索
        if (this._searchDebounceTimer) {
            clearTimeout(this._searchDebounceTimer);
        }
        this._searchDebounceTimer = setTimeout(() => {
            const keyword = this.data.keyword;
            this.handleKeywordInputChange(keyword);
        }, 300);
    },

    async handleKeywordInputChange(keyword) {
        if (!keyword) {
            this._globalSearchMode = false;
            this.applyKeywordFilter('');
            return;
        }
        await this.preloadGlobalProductIndex();
        const matched = this.findGlobalMatchedProduct(keyword);
        if (!matched) {
            this.setData({ filteredThirdProducts: [] });
            return;
        }
        const level1 = (this.data.level1Categories || []).find((item) => String(item.id) === String(matched.level1Id));
        const level2Categories = level1 ? (level1.children || []) : [];
        const needSwitchLevel2 = String(this.data.activeLevel2Id) !== String(matched.level2Id);

        this.setData({
            activeLevel1Id: matched.level1Id,
            level2Categories,
            activeLevel2Id: matched.level2Id,
        }, () => {
            this.ensureLevel2Visible(matched.level2Id);
        });
        this._globalSearchMode = true;

        if (needSwitchLevel2) {
            await this.loadProductsByLevel2(matched.level2Id);
        }
        this.applyKeywordFilter(keyword);
    },

    onClearKeyword() {
        this.syncKeyword('');
        this.applyKeywordFilter('');
    },

    applyKeywordFilter(keyword) {
        const lowerKeyword = (keyword || '').toLowerCase();
        const filteredProducts = !lowerKeyword
            ? this.data.thirdCategoryProducts
            : this.data.thirdCategoryProducts.filter((item) => item.productName.toLowerCase().includes(lowerKeyword));
        this.setData({ filteredThirdProducts: filteredProducts });
        this.scheduleMeasureLevel3Scrollbar();
    },

    scheduleMeasureLevel3Scrollbar() {
        if (this._measureScrollbarTimer) {
            clearTimeout(this._measureScrollbarTimer);
        }
        this._measureScrollbarTimer = setTimeout(() => {
            this._measureScrollbarTimer = null;
            this.measureLevel3Scrollbar();
        }, 100);
    },

    measureLevel3Scrollbar() {
        const query = wx.createSelectorQuery();
        query.select('.level3-column').boundingClientRect();
        query.select('.level3-content').boundingClientRect();
        query.exec((res) => {
            const columnRect = res && res[0];
            const contentRect = res && res[1];
            const containerHeight = Number(columnRect && columnRect.height) || 0;
            const contentHeight = Number(contentRect && contentRect.height) || 0;
            const canScroll = !!containerHeight && !!contentHeight && contentHeight > containerHeight + 1;
            this.setData({
                level3CanScroll: canScroll,
            });
        });
    },

    async onProductTap(event) {
        const item = event.currentTarget.dataset.item;
        if (!item || !item.level3Id) {
            wx.showToast({
                title: '当前商品暂无历史行情',
                icon: 'none',
            });
            return;
        }
        wx.showLoading({ title: '加载中...' });
        try {
            const history = await getPriceHistory(item.level3Id, 30);
            const historyList = convertHistoryList(history);
            const historyTrendDirection = historyList.length >= 2
                ? (historyList[historyList.length - 1].price >= historyList[0].price ? 'up' : 'down')
                : 'flat';

            this.setData({
                historyVisible: true,
                selectedProductName: item.productName,
                selectedProductPrice: item.displayPrice,
                selectedProductDate: item.latestDate || '',
                historyList,
                historyTrendDirection,
            });
            setTimeout(() => this.drawHistoryChart(), 80);
        } catch (error) {
            logger.error('加载历史行情失败', error);
            wx.showToast({
                title: '历史行情加载失败',
                icon: 'none',
            });
        } finally {
            wx.hideLoading();
        }
    },

    onCloseHistory() {
        this.setData({
            historyVisible: false,
            historyList: [],
        });
    },

    drawHistoryChart() {
        const historyList = this.data.historyList || [];
        if (!historyList.length) {
            return;
        }

        const query = wx.createSelectorQuery();
        query.select('#historyChart')
            .fields({ node: true, size: true })
            .exec((res) => {
                if (!res[0] || !res[0].node) {
                    return;
                }
                const canvas = res[0].node;
                const ctx = canvas.getContext('2d');
                const dpr = wx.getWindowInfo().pixelRatio || 1;
                const width = res[0].width;
                const height = res[0].height;
                canvas.width = width * dpr;
                canvas.height = height * dpr;
                ctx.scale(dpr, dpr);
                ctx.clearRect(0, 0, width, height);

                const prices = historyList.map((item) => item.price).filter((p) => p !== null && p !== undefined && Number.isFinite(p));
                if (prices.length === 0) {
                    return;
                }
                const maxPrice = Math.max(...prices);
                const minPrice = Math.min(...prices);
                const range = maxPrice - minPrice || 1;

                const paddingLeft = 56;
                const paddingRight = 16;
                const paddingTop = 16;
                const paddingBottom = 42;
                const chartWidth = width - paddingLeft - paddingRight;
                const chartHeight = height - paddingTop - paddingBottom;

                ctx.beginPath();
                ctx.strokeStyle = '#dbe2ea';
                ctx.lineWidth = 1;
                ctx.moveTo(paddingLeft, paddingTop);
                ctx.lineTo(paddingLeft, height - paddingBottom);
                ctx.lineTo(width - paddingRight, height - paddingBottom);
                ctx.stroke();

                if (historyList.length >= 2) {
                    ctx.beginPath();
                    ctx.lineWidth = 2;
                    // 红涨绿跌（中国市场习惯）
                    ctx.strokeStyle = this.data.historyTrendDirection === 'down' ? '#22c55e' : '#dc2626';
                    historyList.forEach((item, index) => {
                        const x = paddingLeft + (index / (historyList.length - 1)) * chartWidth;
                        const y = height - paddingBottom - ((item.price - minPrice) / range) * chartHeight;
                        if (index === 0) {
                            ctx.moveTo(x, y);
                        } else {
                            ctx.lineTo(x, y);
                        }
                    });
                    ctx.stroke();
                }

                ctx.font = '10px sans-serif';
                ctx.fillStyle = '#64748b';
                ctx.textAlign = 'center';
                const xStep = Math.max(1, Math.floor(historyList.length / 6));
                for (let i = 0; i < historyList.length; i += xStep) {
                    const x = paddingLeft + (i / Math.max(1, historyList.length - 1)) * chartWidth;
                    ctx.fillText(historyList[i].label, x, height - 20);
                }

                ctx.textAlign = 'right';
                for (let i = 0; i <= 4; i += 1) {
                    const price = minPrice + (i / 4) * range;
                    const y = height - paddingBottom - (i / 4) * chartHeight;
                    ctx.fillText(`¥${Math.round(price)}`, paddingLeft - 6, y + 3);
                }
            });
    },
});
