// 从后端API获取物流公司列表
const { submitSellOrder } = require('../../services/order');
const { ensurePaymentInfoBeforeSubmit } = require('../../services/payment-info');
const { getSenderList } = require('../../services/sender');
const {
    resolveSenderFallbackByError,
    addSenderWithFallback,
    deleteSenderWithFallback
} = require('../../services/sender-book');
const { getCategories } = require('../../services/recycle-market');
const { getLogisticsCompanies } = require('../../services/logistics');
const { getToken } = require('../../utils/storage');
const {
    DEFAULT_MOVE_THRESHOLD,
    initTouchGuard,
    onTouchStart,
    onTouchMove,
    shouldBlockSelect,
    resetMoveState
} = require('../../utils/dropdown-touch-guard');
const {
    formatSenderLabel,
    buildSenderViewState,
    buildSenderStateAfterDelete,
    buildOpenSenderListPopupState,
    buildCloseSenderListPopupState,
    buildOpenSenderModalState,
    buildCloseSenderModalState,
    buildSenderSelectState,
    validateSenderInput,
    normalizeLogisticsSuggestList,
    validateLogisticsSelected
} = require('../../utils/order-form-shared');

const SENDER_STORAGE_KEY = 'sell_order_sender_list';

function createEmptyProduct() {
    return {
        name: '',
        quantity: 1
    };
}

Page({
    data: {
        productList: [createEmptyProduct()],
        productNameOptions: [],
        productSuggestList: [],
        showProductSuggest: false,
        activeProductIndex: -1,
        needLogin: false,
        senderList: [],
        senderOptions: ['新增'],
        selectedSenderIndex: 0,
        selectedSenderDisplay: '请选择寄件人',
        logisticsFilter: '',
        logisticsSelected: '',
        logisticsNo: '',
        storageOptions: ['否', '是'],
        storageIndex: 0,
        remark: '',
        showLogisticsSuggest: false,
        logisticsSuggestList: [],
        showSenderModal: false,
        modalSenderName: '',
        modalSenderPhone: '',
        showSenderListPopup: false,
    },

    onLoad() {
        this._logisticsSuggestBlurTimer = null;
        this._productSuggestBlurTimer = null;
        initTouchGuard(this);
        if (!getToken()) {
            this.setData({ needLogin: true });
            return;
        }
        this.setData({ needLogin: false });
        this.loadProductNameOptions();
        this.loadSenderList();
    },

    onShow() {
        if (getToken() && this.data.needLogin) {
            this.setData({ needLogin: false });
            this.loadProductNameOptions();
            this.loadSenderList();
        }
    },

    // ==================== 下拉通用交互 ====================
    onPageTapDismissDropdowns(e) {
        if (e && e.mark && e.mark.dropdownArea) {
            return;
        }
        this.dismissProductSuggestNow();
        this.dismissLogisticsSuggestNow();
    },

    dismissLogisticsSuggestNow() {
        this.clearLogisticsSuggestBlurTimer();
        if (this.data.showLogisticsSuggest) {
            this.setData({ showLogisticsSuggest: false });
        }
    },

    dismissProductSuggestNow() {
        this.clearProductSuggestBlurTimer();
        if (this.data.showProductSuggest) {
            this.setData({ showProductSuggest: false, activeProductIndex: -1 });
        }
    },

    onHide() {
        this.clearLogisticsSuggestBlurTimer();
        this.clearProductSuggestBlurTimer();
    },

    onUnload() {
        this.clearLogisticsSuggestBlurTimer();
        this.clearProductSuggestBlurTimer();
        if (this._logisticsBlurSyncTimer != null) {
            clearTimeout(this._logisticsBlurSyncTimer);
            this._logisticsBlurSyncTimer = null;
        }
    },

    clearLogisticsSuggestBlurTimer() {
        if (this._logisticsSuggestBlurTimer != null) {
            clearTimeout(this._logisticsSuggestBlurTimer);
            this._logisticsSuggestBlurTimer = null;
        }
    },

    clearProductSuggestBlurTimer() {
        if (this._productSuggestBlurTimer != null) {
            clearTimeout(this._productSuggestBlurTimer);
            this._productSuggestBlurTimer = null;
        }
    },

    scheduleLogisticsSuggestHide() {
        this.clearLogisticsSuggestBlurTimer();
        this._logisticsSuggestBlurTimer = setTimeout(() => {
            this._logisticsSuggestBlurTimer = null;
            this.setData({ showLogisticsSuggest: false });
        }, 800);
    },

    scheduleProductSuggestHide() {
        this.clearProductSuggestBlurTimer();
        this._productSuggestBlurTimer = setTimeout(() => {
            this._productSuggestBlurTimer = null;
            this.setData({ showProductSuggest: false, activeProductIndex: -1 });
        }, 800);
    },

    onLogisticsSuggestTouch(e) {
        this.clearLogisticsSuggestBlurTimer();
    },

    onProductSuggestTouch(e) {
        this.clearProductSuggestBlurTimer();
    },

    shouldBlockDropdownSelect() {
        return shouldBlockSelect(this);
    },

    onDropdownOptionTouchStart(e) {
        onTouchStart(this, e);
    },

    onDropdownOptionTouchMove(e) {
        onTouchMove(this, e, DEFAULT_MOVE_THRESHOLD);
    },

    onProductOptionTouchEnd(e) {
        if (this.shouldBlockDropdownSelect()) {
            resetMoveState(this);
            return;
        }
        this.selectProductName(e);
    },

    onLogisticsOptionTouchEnd(e) {
        if (this.shouldBlockDropdownSelect()) {
            resetMoveState(this);
            return;
        }
        this.selectLogistics(e);
    },

    loadProductNameOptions() {
        getCategories()
            .then((list = []) => {
                // 分类树：一级 → 二级 → 三级（商品名在三级节点，与行情页一致）
                const nameMap = {};
                (list || []).forEach((level1) => {
                    (level1.children || []).forEach((level2) => {
                        (level2.children || []).forEach((level3) => {
                            const name = (level3.name || level3.productName || '').trim();
                            if (name) {
                                nameMap[name] = true;
                            }
                        });
                    });
                });
                const names = Object.keys(nameMap).sort((a, b) => a.localeCompare(b, 'zh-CN'));
                this.setData({
                    productNameOptions: names
                });
            })
            .catch(() => {
                this.setData({ productNameOptions: [] });
            });
    },

    filterProductSuggestList(keyword) {
        const value = (keyword || '').trim();
        if (!value) {
            return this.data.productNameOptions.slice(0, 20);
        }
        const lower = value.toLowerCase();
        return this.data.productNameOptions
            .filter((name) => (name || '').toLowerCase().includes(lower))
            .slice(0, 20);
    },

    addProductItem() {
        this.setData({
            productList: [...this.data.productList, createEmptyProduct()]
        });
    },

    changeQuantity(e) {
        const index = Number(e.currentTarget.dataset.index);
        const delta = Number(e.currentTarget.dataset.delta);
        const list = this.data.productList.slice();
        const item = list[index];
        if (!item) return;
        item.quantity = Math.max(1, (item.quantity || 1) + delta);
        list[index] = item;
        this.setData({ productList: list });
    },

    onQuantityInput(e) {
        const index = parseInt(e.currentTarget.dataset.index, 10);
        const val = e.detail.value.replace(/\D/g, '') || '0';
        const q = Math.max(1, parseInt(val, 10) || 1);
        const list = this.data.productList.slice();
        if (list[index]) {
            list[index].quantity = q;
            this.setData({ productList: list });
        }
    },

    onProductNameInput(e) {
        this.clearProductSuggestBlurTimer();
        resetMoveState(this);
        const index = Number(e.currentTarget.dataset.index);
        const list = this.data.productList.slice();
        if (!list[index]) return;
        const value = e.detail.value || '';
        list[index].name = value;
        this.setData({
            productList: list,
            activeProductIndex: index,
            productSuggestList: this.filterProductSuggestList(value),
            showProductSuggest: true
        });
    },

    onProductNameFocus(e) {
        this.clearProductSuggestBlurTimer();
        resetMoveState(this);
        const index = Number(e.currentTarget.dataset.index);
        const currentItem = this.data.productList[index];
        this.setData({
            activeProductIndex: index,
            productSuggestList: this.filterProductSuggestList(currentItem ? currentItem.name : ''),
            showProductSuggest: true
        });
    },

    onProductNameBlur() {
        this.scheduleProductSuggestHide();
    },

    selectProductName(e) {
        if (this.shouldBlockDropdownSelect()) {
            resetMoveState(this);
            return;
        }
        resetMoveState(this);
        this.clearProductSuggestBlurTimer();
        const value = e.currentTarget.dataset.value || '';
        const index = Number(e.currentTarget.dataset.index);
        const list = this.data.productList.slice();
        if (!list[index]) return;
        list[index].name = value;
        this.setData({
            productList: list,
            showProductSuggest: false,
            activeProductIndex: -1
        });
    },

    removeProductItem(e) {
        this.dismissProductSuggestNow();
        const index = Number(e.currentTarget.dataset.index);
        const list = this.data.productList.filter((_, i) => i !== index);
        this.setData({ productList: list.length ? list : [createEmptyProduct()] });
        wx.showToast({ title: '已移除', icon: 'none' });
    },

    // ==================== 寄件人地址簿 ====================
    loadSenderList() {
        return getSenderList()
            .then((res) => {
                const list = Array.isArray(res) ? res : [];
                this.setData({ needLogin: false });
                this.applySenderList(list);
                return list;
            })
            .catch((err) => {
                const result = resolveSenderFallbackByError(err, SENDER_STORAGE_KEY);
                if (result.unauthorized) {
                    this.setData({ needLogin: true });
                    return [];
                }
                this.setData({ needLogin: false });
                this.applySenderList(result.senderList);
                return result.senderList;
            });
    },

    applySenderList(list) {
        const selectedIndex = this.data.selectedSenderIndex;
        this.setData(buildSenderViewState(list, selectedIndex, '请选择寄件人'));
    },

    openSenderListPopup() {
        this.setData(buildOpenSenderListPopupState());
    },

    closeSenderListPopup() {
        this.setData(buildCloseSenderListPopupState());
    },

    selectSenderByIndex(e) {
        const index = parseInt(e.currentTarget.dataset.index, 10);
        this.setData(buildSenderSelectState(this.data.senderList, index, '请选择寄件人'));
    },

    onDeleteSenderTap(e) {
        const index = parseInt(e.currentTarget.dataset.index, 10);
        const list = this.data.senderList || [];
        const sender = list[index];
        if (!sender) return;
        const name = (sender.name || '该').trim() || '该';
        wx.showModal({
            title: '确认删除',
            content: '是否删除' + name + '寄件人信息？',
            success: (res) => {
                if (!res.confirm) return;
                deleteSenderWithFallback(SENDER_STORAGE_KEY, sender, list, index)
                    .then((result) => {
                        if (result.remoteDeleted) {
                            wx.showToast({ title: '已删除', icon: 'success' });
                            return this.loadSenderList().then(() => true);
                        }
                        const newList = result.senderList || [];
                        const senderState = buildSenderStateAfterDelete({
                            selectedSenderIndex: this.data.selectedSenderIndex,
                            deleteIndex: index,
                            newList,
                            defaultText: '请选择寄件人'
                        });
                        this.setData({ ...senderState, showSenderListPopup: newList.length > 0 });
                        wx.showToast({ title: '已删除', icon: 'success' });
                        return false;
                    })
                    .then((isRemoteDeleted) => {
                        if (!isRemoteDeleted) {
                            return;
                        }
                        const senderState = buildSenderStateAfterDelete({
                            selectedSenderIndex: this.data.selectedSenderIndex,
                            deleteIndex: index,
                            newList: this.data.senderList,
                            defaultText: '请选择寄件人'
                        });
                        this.setData({
                            selectedSenderIndex: senderState.selectedSenderIndex,
                            selectedSenderDisplay: senderState.selectedSenderDisplay,
                            showSenderListPopup: this.data.senderList.length > 0
                        });
                    })
                    .catch(() => wx.showToast({ title: '删除失败', icon: 'none' }));
            }
        });
    },

    openSenderModal() {
        this.setData(buildOpenSenderModalState());
    },

    closeSenderModal() {
        this.setData(buildCloseSenderModalState());
    },

    onModalSenderNameInput(e) {
        this.setData({ modalSenderName: e.detail.value });
    },
    onModalSenderPhoneInput(e) {
        this.setData({ modalSenderPhone: e.detail.value });
    },

    confirmAddSender() {
        const name = (this.data.modalSenderName || '').trim();
        const phone = (this.data.modalSenderPhone || '').trim();
        const senderInputError = validateSenderInput(name, phone, '寄件人姓名');
        if (senderInputError) {
            wx.showToast({ title: senderInputError, icon: 'none' });
            return;
        }
        const list = (this.data.senderList || []).slice();
        const exist = list.find(s => (s.phone || '') === phone && (s.name || '') === name);
        if (exist) {
            this.setData({
                selectedSenderIndex: list.indexOf(exist) + 1,
                selectedSenderDisplay: formatSenderLabel(exist)
            });
            this.closeSenderModal();
            wx.showToast({ title: '已添加', icon: 'success' });
            return;
        }
        addSenderWithFallback(SENDER_STORAGE_KEY, list, { name, phone })
            .then((result) => {
                this.closeSenderModal();
                wx.showToast({ title: '已添加', icon: 'success' });
                if (result.success) {
                    return this.loadSenderList();
                }
                const fallbackList = result.senderList || [];
                this.setData({
                    ...buildSenderViewState(fallbackList, fallbackList.length, '请选择寄件人'),
                    selectedSenderDisplay: formatSenderLabel({ name, phone })
                });
                return fallbackList;
            })
            .then((list) => {
                if (list && list.length) {
                    const last = list[list.length - 1];
                    this.setData({
                        selectedSenderIndex: list.length,
                        selectedSenderDisplay: formatSenderLabel(last)
                    });
                }
            })
            .catch(() => wx.showToast({ title: '添加失败', icon: 'none' }));
    },

    // ==================== 物流信息 ====================
    onLogisticsInput(e) {
        this.clearLogisticsSuggestBlurTimer();
        resetMoveState(this);
        const raw = e.detail.value || '';
        const prevSel = (this.data.logisticsSelected || '').trim();
        if (prevSel && raw.trim() !== prevSel) {
            this.setData({ logisticsSelected: '' });
        }
        this.setData({ logisticsFilter: raw });
        this.getLogisticsSuggestList(raw.trim());
    },

    onLogisticsFocus() {
        this.clearLogisticsSuggestBlurTimer();
        resetMoveState(this);
        const v = (this.data.logisticsFilter || '').trim();
        this.getLogisticsSuggestList(v);
    },

    getLogisticsSuggestList(name) {
        getLogisticsCompanies(name)
            .then((res) => {
                this.setData({
                    logisticsSuggestList: normalizeLogisticsSuggestList(res),
                    showLogisticsSuggest: true
                });
            })
            .catch(() => {
                this.setData({ logisticsSuggestList: [], showLogisticsSuggest: true });
            });
    },

    onLogisticsBlur() {
        this.scheduleLogisticsSuggestHide();
        if (this._logisticsBlurSyncTimer != null) {
            clearTimeout(this._logisticsBlurSyncTimer);
        }
        this._logisticsBlurSyncTimer = setTimeout(() => {
            this._logisticsBlurSyncTimer = null;
            const errorMessage = validateLogisticsSelected(this.data.logisticsFilter, this.data.logisticsSelected, false);
            if (errorMessage) {
                wx.showToast({ title: errorMessage, icon: 'none' });
            }
        }, 520);
    },

    selectLogistics(e) {
        if (this.shouldBlockDropdownSelect()) {
            resetMoveState(this);
            return;
        }
        resetMoveState(this);
        this.clearLogisticsSuggestBlurTimer();
        const value = e.currentTarget.dataset.value || '';
        this.setData({
            logisticsSelected: value,
            logisticsFilter: value,
            showLogisticsSuggest: false,
        });
    },

    onLogisticsNoInput(e) {
        this.setData({ logisticsNo: e.detail.value });
    },

    onStorageChange(e) {
        this.setData({ storageIndex: parseInt(e.detail.value, 10) });
    },

    onRemarkInput(e) {
        this.setData({ remark: e.detail.value });
    },

    // ==================== 提交报单 ====================
    submit() {
        const { productList, senderList, selectedSenderIndex, logisticsSelected, logisticsNo, storageIndex, storageOptions } = this.data;
        const validProducts = (productList || [])
            .map(item => ({
                name: (item.name || '').trim(),
                quantity: Math.max(1, Number(item.quantity) || 1)
            }))
            .filter(item => item.name);
        if (!validProducts.length) {
            wx.showToast({ title: '请填写商品名称', icon: 'none' });
            return;
        }
        if (selectedSenderIndex === 0 || !senderList || !senderList.length) {
            wx.showToast({ title: '请选择或新增寄件人', icon: 'none' });
            return;
        }
        const sender = senderList[selectedSenderIndex - 1];
        if (!sender || !(sender.name || sender.phone)) {
            wx.showToast({ title: '请选择或新增寄件人', icon: 'none' });
            return;
        }
        const logisticsErrorMessage = validateLogisticsSelected(this.data.logisticsFilter, logisticsSelected, true);
        if (logisticsErrorMessage) {
            wx.showToast({ title: logisticsErrorMessage, icon: 'none' });
            return;
        }
        if (!(logisticsNo && logisticsNo.trim())) {
            wx.showToast({ title: '请填写物流单号', icon: 'none' });
            return;
        }
        const storage = storageIndex !== null && storageIndex >= 0 ? storageOptions[storageIndex] : '否';
        const payload = {
            items: validProducts,
            sender: { name: (sender.name || '').trim(), phone: (sender.phone || '').trim(), address: (sender.address || '').trim() },
            logistics: { company: logisticsSelected.trim(), no: logisticsNo.trim() },
            storage,
            remark: this.data.remark ? this.data.remark.trim() : ''
        };
        wx.showLoading({ title: '提交中...' });
        ensurePaymentInfoBeforeSubmit()
            .then((canSubmit) => {
                if (!canSubmit) {
                    wx.hideLoading();
                    return;
                }
                return submitSellOrder(payload)
                    .then((res) => {
                        wx.hideLoading();
                        const id = res && (res.id !== undefined ? res.id : res);
                        if (id != null) wx.setStorageSync('sellOrderSubmissionId', id);
                        wx.showToast({ title: '提交成功', icon: 'success' });
                        setTimeout(() => wx.navigateBack(), 1500);
                    })
                    .catch((err) => {
                        wx.hideLoading();
                        wx.showToast({ title: err?.message || '提交失败', icon: 'none' });
                    });
            });
    }
});
