const { getFormSubmissionMyPage, getSellOrderMyPage, postLogisticsSummaries } = require('../../services/order');

const PAGE_SIZE = 20;
const TABS = [
    { key: 'all', label: '全部' },
    { key: 'transit', label: '运输中' },
    { key: 'storing', label: '入库中' },
    { key: 'completed', label: '已完成' },
    { key: 'exception', label: '异常' }
];

const STATUS_TEXT = {
    0: '草稿',
    1: '已提交',
    2: '运输中',
    3: '入库中',
    4: '已打款',
    5: '异常',
    6: '已退货',
    7: '已签收'
};

/** 全部 tab 仅显示四类状态：运输中、入库中、已完成、异常 */
const STATUS_CATEGORY = {
    0: '草稿',
    1: '运输中',
    2: '运输中',
    7: '运输中',
    3: '入库中',
    4: '已完成',
    6: '已完成',
    5: '异常'
};

function pickTrim(obj, ...keys) {
    if (!obj || typeof obj !== 'object') return '';
    for (let i = 0; i < keys.length; i++) {
        const k = keys[i];
        const v = obj[k];
        if (v != null && String(v).trim() !== '') return String(v).trim();
    }
    return '';
}

function normalizeForm(r) {
    const code = r.status != null ? (typeof r.status === 'object' ? r.status.code : r.status) : null;
    const dataJson = r.dataJson || r.data_json || {};
    const rawExpress = dataJson.expressNo ?? dataJson.express_no ?? r.expressNo ?? r.logisticsNo ?? '';
    const expressNo = rawExpress != null && rawExpress !== '' ? String(rawExpress).trim() : '';
    const logisticsCompany = pickTrim(dataJson, 'logisticsCompany', 'expressCompany');
    const senderName = pickTrim(dataJson, 'senderName');
    const senderPhone = pickTrim(dataJson, 'senderPhone');
    const adminNote = (r.adminInternalNote != null && r.adminInternalNote !== '') ? String(r.adminInternalNote).trim() : '';
    return {
        type: 'form',
        id: r.id,
        uniqueKey: `form-${r.id}`,
        createdAt: r.createdAt,
        updatedAt: r.updatedAt,
        status: code,
        statusText: STATUS_TEXT[code] || '—',
        statusCategoryText: STATUS_CATEGORY[code] || '—',
        schemeName: r.schemeName || '固结报单',
        quantity: r.quantity,
        expressNo,
        logisticsCompany,
        senderName,
        senderPhone,
        summary: `数量 ${r.quantity || 1}`,
        adminInternalNote: adminNote
    };
}

function normalizeSell(r) {
    const items = r.itemsJson || r.items || r.productList || [];
    const productDetail = items.length > 0
        ? items.map((i) => `${i.productName || i.name || '商品'} x${i.quantity || 1}`).join('；')
        : (r.productDetail || '—');
    const storage = r.storage != null ? Number(r.storage) : NaN;
    const depositText = storage === 1 ? '寄存' : (storage === 0 ? '不寄存' : '—');
    const adminNote = (r.adminInternalNote != null && r.adminInternalNote !== '') ? String(r.adminInternalNote).trim() : '';
    return {
        type: 'sell',
        id: r.id,
        uniqueKey: `sell-${r.id}`,
        createdAt: r.createdAt,
        updatedAt: r.updatedAt,
        status: r.status,
        statusText: STATUS_TEXT[r.status] || '—',
        statusCategoryText: STATUS_CATEGORY[r.status] || '—',
        logisticsCompany: (r.logisticsCompany || '').trim(),
        senderName: (r.senderName || '').trim(),
        senderPhone: (r.senderPhone || '').trim(),
        logisticsNo: r.logisticsNo || '',
        productDetail,
        depositText,
        summary: ((r.logisticsCompany || '') + ' ' + (r.logisticsNo || '')).trim() || '行情报单',
        adminInternalNote: adminNote
    };
}

/** 仅当同时填写寄件人姓名、手机号与物流公司时才拉取物流摘要（否则只展示单号） */
function shouldFetchLogisticsSummary(row) {
    const company = (row.logisticsCompany || '').trim();
    const name = (row.senderName || '').trim();
    const phone = (row.senderPhone || '').trim();
    if (!company || !name || !phone) return false;
    if (row.type === 'sell') return !!(row.logisticsNo || '').trim();
    if (row.type === 'form') return !!(row.expressNo || '').trim();
    return false;
}

function enrichWithLogistics(list, done) {
    const items = list
        .filter((x) => shouldFetchLogisticsSummary(x))
        .map((x) => ({ type: x.type, id: x.id }));
    if (items.length === 0) {
        done(list);
        return;
    }
    postLogisticsSummaries({ items })
        .then((map) => {
            if (!map || typeof map !== 'object') {
                done(list);
                return;
            }
            const merged = list.map((row) => {
                const key = `${row.type}-${row.id}`;
                const s = map[key];
                if (!s) return row;
                return { ...row, logisticsSummary: s };
            });
            done(merged);
        })
        .catch(() => done(list));
}

function mergeByUpdatedAt(formList, sellList) {
    const combined = [
        ...(formList || []).map((r) => ({ ...r, _ts: r.updatedAt || r.createdAt || '' })),
        ...(sellList || []).map((r) => ({ ...r, _ts: r.updatedAt || r.createdAt || '' }))
    ];
    combined.sort((a, b) => (b._ts || '').localeCompare(a._ts || ''));
    return combined.map(({ _ts, ...r }) => r);
}

function getTabKey(index) {
    return TABS[index] ? TABS[index].key : 'all';
}

Page({
    data: {
        tabs: TABS,
        currentIndex: 0,
        listAll: [],
        listTransit: [],
        listStoring: [],
        listCompleted: [],
        listException: [],
        formPageNum_all: 1,
        sellPageNum_all: 1,
        formDone_all: false,
        sellDone_all: false,
        formPageNum_transit: 1,
        sellPageNum_transit: 1,
        formDone_transit: false,
        sellDone_transit: false,
        formPageNum_storing: 1,
        sellPageNum_storing: 1,
        formDone_storing: false,
        sellDone_storing: false,
        formPageNum_completed: 1,
        sellPageNum_completed: 1,
        formDone_completed: false,
        sellDone_completed: false,
        formPageNum_exception: 1,
        sellPageNum_exception: 1,
        formDone_exception: false,
        sellDone_exception: false,
        hasMore_all: true,
        hasMore_transit: true,
        hasMore_storing: true,
        hasMore_completed: true,
        hasMore_exception: true,
        loading: false
    },

    onLoad(options) {
        const statusTab = (options && options.statusTab) || 'all';
        const idx = TABS.findIndex((t) => t.key === statusTab);
        const currentIndex = idx >= 0 ? idx : 0;
        this.setData({ currentIndex });
        if (typeof wx.setNavigationBarTitle === 'function') {
            wx.setNavigationBarTitle({ title: '我的订单' });
        }
        this.loadListForTab(TABS[currentIndex].key, true);
    },

    onPullDownRefresh() {
        const key = getTabKey(this.data.currentIndex);
        this.resetTabPagination(key);
        this.loadListForTab(key, true).finally(() => {
            if (wx.stopPullDownRefresh) wx.stopPullDownRefresh();
        });
    },

    onReachBottom() {
        const key = getTabKey(this.data.currentIndex);
        const hasMore = this.data['hasMore_' + key];
        if (this.data.loading || !hasMore) return;
        this.loadListForTab(key, false);
    },

    onTabTap(e) {
        const index = parseInt(e.currentTarget.dataset.index, 10);
        if (index === this.data.currentIndex) return;
        this.setData({ currentIndex: index });
        const key = TABS[index].key;
        const cap = key === 'all' ? 'All' : (key.charAt(0).toUpperCase() + key.slice(1));
        const list = this.data['list' + cap];
        if (!list || list.length === 0) {
            this.loadListForTab(key, true);
        }
    },

    onSwiperChange(e) {
        const index = e.detail.current;
        if (index === this.data.currentIndex) return;
        this.setData({ currentIndex: index });
        const key = TABS[index].key;
        const cap = key === 'all' ? 'All' : (key.charAt(0).toUpperCase() + key.slice(1));
        const list = this.data['list' + cap];
        if (!list || list.length === 0) {
            this.loadListForTab(key, true);
        }
    },

    onCardTap(e) {
        const { type, id } = e.currentTarget.dataset;
        if (!type || !id) return;
        wx.navigateTo({
            url: `/pages/order-detail/order-detail?type=${type}&id=${id}`
        });
    },

    resetTabPagination(key) {
        const cap = key === 'all' ? 'All' : (key.charAt(0).toUpperCase() + key.slice(1));
        this.setData({
            ['list' + cap]: [],
            ['formPageNum_' + key]: 1,
            ['sellPageNum_' + key]: 1,
            ['formDone_' + key]: false,
            ['sellDone_' + key]: false,
            ['hasMore_' + key]: true
        });
    },

    loadListForTab(tabKey, refresh) {
        if (this.data.loading) return Promise.resolve();
        const cap = tabKey === 'all' ? 'All' : (tabKey.charAt(0).toUpperCase() + tabKey.slice(1));
        const listKey = 'list' + cap;
        const list = refresh ? [] : (this.data[listKey] || []);
        const formPage = refresh ? 1 : this.data['formPageNum_' + tabKey];
        const sellPage = refresh ? 1 : this.data['sellPageNum_' + tabKey];

        this.setData({ loading: true });

        const formPromise = getFormSubmissionMyPage({
            pageNum: formPage,
            pageSize: PAGE_SIZE,
            statusTab: tabKey
        }).then((res) => {
            const records = (res && res.records) || [];
            const pageMeta = (res && res.pageMeta) || {};
            const total = pageMeta.total || 0;
            const size = pageMeta.pageSize || PAGE_SIZE;
            const formDone = records.length < size || formPage * size >= total;
            return { list: records.map(normalizeForm), formDone };
        }).catch(() => ({ list: [], formDone: true }));

        const sellPromise = getSellOrderMyPage({
            pageNum: sellPage,
            pageSize: PAGE_SIZE,
            statusTab: tabKey
        }).then((res) => {
            const records = (res && res.records) || [];
            const pageMeta = (res && res.pageMeta) || {};
            const total = pageMeta.total || 0;
            const size = pageMeta.pageSize || PAGE_SIZE;
            const sellDone = records.length < size || sellPage * size >= total;
            return { list: records.map(normalizeSell), sellDone };
        }).catch(() => ({ list: [], sellDone: true }));

        return Promise.all([formPromise, sellPromise]).then(([formRes, sellRes]) => {
            const newPart = mergeByUpdatedAt(formRes.list, sellRes.list);
            const merged = refresh ? newPart : list.concat(newPart);
            const seen = new Set();
            const deduped = merged.filter((x) => {
                const k = `${x.type}-${x.id}`;
                if (seen.has(k)) return false;
                seen.add(k);
                return true;
            });
            deduped.sort((a, b) => (b.updatedAt || b.createdAt || '').localeCompare(a.updatedAt || a.createdAt || ''));

            const nextFormPage = formRes.formDone ? formPage : formPage + 1;
            const nextSellPage = sellRes.sellDone ? sellPage : sellPage + 1;
            const hasMore = !formRes.formDone || !sellRes.sellDone;

            enrichWithLogistics(deduped, (finalList) => {
                this.setData({
                    [listKey]: finalList,
                    ['formPageNum_' + tabKey]: nextFormPage,
                    ['sellPageNum_' + tabKey]: nextSellPage,
                    ['formDone_' + tabKey]: formRes.formDone,
                    ['sellDone_' + tabKey]: sellRes.sellDone,
                    ['hasMore_' + tabKey]: hasMore,
                    loading: false
                });
            });
        }).catch(() => {
            this.setData({ loading: false });
        });
    }
});
