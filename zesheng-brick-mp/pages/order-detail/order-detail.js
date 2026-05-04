const { getFormSubmissionDetail, getSellOrderDetail, getFormLogisticsTrace, getSellLogisticsTrace } = require('../../services/order');

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

function formatDateTime(str) {
    if (!str) return '—';
    return String(str).replace('T', ' ').substring(0, 19);
}

Page({
    data: {
        type: 'form',
        id: null,
        loading: true,
        error: '',
        form: null,
        sell: null,
        statusText: '',
        createdAt: '',
        updatedAt: '',
        proofUrls: [],
        dataJsonLabels: {},
        logisticsTrace: null,
        logisticsLoading: false,
        logisticsExpanded: false,
        enableLogisticsTrace: false
    },

    onLoad(options) {
        const type = (options && options.type) || 'form';
        const id = options && options.id ? Number(options.id) : null;
        if (!id) {
            this.setData({ loading: false, error: '参数错误' });
            return;
        }
        wx.setNavigationBarTitle({ title: type === 'form' ? '固结报单详情' : '行情报单详情' });
        this.setData({ type, id });
        this.loadDetail(type, id);
    },

    loadDetail(type, id) {
        this.setData({ loading: true, error: '' });
        const api = type === 'form' ? getFormSubmissionDetail(id) : getSellOrderDetail(id);
        api.then((data) => {
            if (!data) {
                this.setData({ loading: false, error: '记录不存在' });
                return;
            }
            if (type === 'form') {
                this.setFormData(data);
            } else {
                this.setSellData(data);
            }
            this.loadLogistics(type, id, data);
        }).catch((err) => {
            const msg = (err && err.message) || (err && err.errMsg) || '加载失败';
            this.setData({ loading: false, error: msg });
        });
    },

    loadLogistics(type, id, detail) {
        const enable = this.computeEnableLogisticsTrace(type, detail);
        if (!enable) {
            this.setData({ logisticsLoading: false, logisticsTrace: null, logisticsExpanded: false, enableLogisticsTrace: false });
            return;
        }
        this.setData({ enableLogisticsTrace: true, logisticsLoading: true, logisticsTrace: null, logisticsExpanded: false });
        const api = type === 'form' ? getFormLogisticsTrace(id) : getSellLogisticsTrace(id);
        api
            .then((trace) => {
                this.setData({ logisticsTrace: trace || null, logisticsLoading: false });
            })
            .catch(() => {
                this.setData({ logisticsTrace: null, logisticsLoading: false });
            });
    },

    toggleLogisticsExpand() {
        this.setData({ logisticsExpanded: !this.data.logisticsExpanded });
    },

    computeEnableLogisticsTrace(type, detail) {
        if (!detail) return false;
        if (type === 'form') {
            const dj = detail.dataJson || {};
            const companyHint = (dj.logisticsCompany || dj.expressCompany || '').trim();
            const senderOk = (dj.senderName || '').trim() && (dj.senderPhone || '').trim();
            return !!(companyHint && senderOk);
        }
        if (type === 'sell') {
            const companyHint = (detail.logisticsCompany || '').trim();
            const senderOk = (detail.senderName || '').trim() && (detail.senderPhone || '').trim();
            return !!(companyHint && senderOk);
        }
        return false;
    },

    setFormData(r) {
        const status = r.status != null ? (typeof r.status === 'object' ? r.status.code : r.status) : null;
        const dataJson = r.dataJson || {};
        const proofUrls = [];
        if (r.settledProofUrl) proofUrls.push(r.settledProofUrl);
        if (r.settledProofUrls && r.settledProofUrls.length) proofUrls.push(...r.settledProofUrls);
        const dataJsonLabels = [];
        const keys = ['expressNo', 'senderName', 'senderPhone', 'logisticsCompany', 'giftDesc', 'orderNoMain', 'orderNoGift', 'signDate', 'remark'];
        const keyNames = {
            expressNo: '快递单号',
            senderName: '收件人姓名',
            senderPhone: '收件人手机',
            logisticsCompany: '物流公司',
            giftDesc: '加赠说明',
            orderNoMain: '主订单号',
            orderNoGift: '赠品订单号',
            signDate: '签收日期',
            remark: '备注'
        };
        keys.forEach((k) => {
            const v = dataJson[k];
            if (v != null && v !== '') dataJsonLabels.push({ label: keyNames[k], value: String(v) });
        });
        const settledAtText = r.settledAt ? formatDateTime(r.settledAt) : '';
        const unitPrice = r.unitPrice != null ? Number(r.unitPrice) : null;
        const qty = r.quantity != null ? Number(r.quantity) : 1;
        const expectedAmountStr = (unitPrice != null && !Number.isNaN(unitPrice))
            ? (unitPrice * qty).toFixed(2)
            : null;
        this.setData({
            loading: false,
            form: { ...r, settledAtText, expectedAmountStr },
            statusText: STATUS_TEXT[status] || '—',
            createdAt: formatDateTime(r.createdAt),
            updatedAt: formatDateTime(r.updatedAt),
            proofUrls,
            dataJsonLabels
        });
    },

    setSellData(r) {
        const proofUrls = r.settledProofUrls && r.settledProofUrls.length ? [...r.settledProofUrls] : [];
        const items = (r.itemsJson || []).map((i) => ({
            name: i.productName || i.name || '—',
            quantity: i.quantity != null ? i.quantity : 1
        }));
        this.setData({
            loading: false,
            sell: r,
            statusText: STATUS_TEXT[r.status] || '—',
            createdAt: formatDateTime(r.createdAt),
            updatedAt: formatDateTime(r.updatedAt),
            proofUrls,
            depositText: r.storage === 1 ? '寄存' : (r.storage === 0 ? '不寄存' : '—'),
            items
        });
    }
});
