// 固结报单：固定字段表单
const { fetchPlans } = require('../../services/form-schemes');
const { saveFormSubmission } = require('../../services/order');
const { ensurePaymentInfoBeforeSubmit } = require('../../services/payment-info');
const { getLogisticsCompanies } = require('../../services/logistics');
const { getSenderList } = require('../../services/sender');
const {
  resolveSenderFallbackByError,
  addSenderWithFallback,
  deleteSenderWithFallback,
} = require('../../services/sender-book');
const { getToken } = require('../../utils/storage');
const config = require('../../config/index');
const {
  DEFAULT_MOVE_THRESHOLD,
  initTouchGuard,
  onTouchStart,
  onTouchMove,
  shouldBlockSelect,
  resetMoveState,
} = require('../../utils/dropdown-touch-guard');
const {
  isUnauthorizedError,
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
  validateLogisticsSelected,
} = require('../../utils/order-form-shared');

/** 与行情报单共用本地兜底缓存键 */
const SENDER_STORAGE_KEY = 'sell_order_sender_list';

const MAX_IMAGE_SIZE = 2 * 1024 * 1024; // 2MB 超过则压缩
const COMPRESS_QUALITY = 80;

/** 方案下拉：最大高度、单行估算、占位（与 suggest-item 大致一致） */
const PLAN_DROPDOWN_MAX_RPX = 400;
const PLAN_DROPDOWN_ROW_RPX = 88;
const PLAN_DROPDOWN_PLACEHOLDER_RPX = 100;

function isEmptyValue(v) {
  if (v === null || v === undefined) return true;
  if (Array.isArray(v)) return v.length === 0;
  if (typeof v === 'string') return v.trim() === '';
  return false;
}

Page({
  data: {
    planList: [],
    filteredPlanList: [],
    planSearchText: '',
    showPlanDropdown: false,
    isLoadingPlans: false,
    currentPlan: null,
    planIndex: -1,
    searchTimer: null,
    needLogin: false,

    schemeName: '',
    giftDesc: '',
    quantity: 1,
    expressNo: '',
    orderNoMain: '',
    orderNoGift: '',
    imageUrls: [],
    deliveryAddressText: '',
    signDate: '',
    signDateStr: '',
    remark: '',
    senderList: [],
    senderOptions: ['新增'],
    selectedSenderIndex: 0,
    selectedSenderDisplay: '选填，可不选',
    showSenderModal: false,
    modalSenderName: '',
    modalSenderPhone: '',
    showSenderListPopup: false,
    logisticsFilter: '',
    logisticsSelected: '',
    showLogisticsSuggest: false,
    logisticsSuggestList: [],
    planDropdownHeightRpx: PLAN_DROPDOWN_PLACEHOLDER_RPX,
    planDropdownScrollable: false,
  },

  onLoad() {
    this._logisticsSuggestBlurTimer = null;
    this._planDropdownBlurTimer = null;
    initTouchGuard(this);
    if (!getToken()) {
      this.setData({
        needLogin: true,
        planList: [],
        filteredPlanList: [],
        isLoadingPlans: false,
      });
      return;
    }
    this.getPlanList();
    this.loadSenderList();
  },

  onHide() {
    this.clearLogisticsSuggestBlurTimer();
    this.clearPlanDropdownBlurTimer();
  },

  onUnload() {
    this.clearLogisticsSuggestBlurTimer();
    this.clearPlanDropdownBlurTimer();
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

  scheduleLogisticsSuggestHide() {
    this.clearLogisticsSuggestBlurTimer();
    // 延迟须大于「失焦 → 再点选项」的间隔；开发者工具鼠标仅触发 tap 不触发 touchstart，过短会先关掉列表
    this._logisticsSuggestBlurTimer = setTimeout(() => {
      this._logisticsSuggestBlurTimer = null;
      this.setData({ showLogisticsSuggest: false });
    }, 800);
  },

  onLogisticsSuggestTouch(e) {
    this.clearLogisticsSuggestBlurTimer();
  },

  // ==================== 页面基础交互 ====================
  onShow() {
    if (getToken() && this.data.needLogin) {
      this.setData({ needLogin: false });
      this.getPlanList();
      this.loadSenderList();
    }
  },

  onPageTapDismissDropdowns(e) {
    if (e && e.mark && e.mark.dropdownArea) {
      return;
    }
    this.clearPlanDropdownBlurTimer();
    this.clearLogisticsSuggestBlurTimer();
    if (this.data.showPlanDropdown || this.data.showLogisticsSuggest) {
      this.setData({
        showPlanDropdown: false,
        showLogisticsSuggest: false,
      });
    }
  },

  clearPlanDropdownBlurTimer() {
    if (this._planDropdownBlurTimer != null) {
      clearTimeout(this._planDropdownBlurTimer);
      this._planDropdownBlurTimer = null;
    }
  },

  schedulePlanDropdownHide() {
    this.clearPlanDropdownBlurTimer();
    this._planDropdownBlurTimer = setTimeout(() => {
      this._planDropdownBlurTimer = null;
      if (!this.data.schemeName) {
        this.setData({ showPlanDropdown: false });
      }
    }, 800);
  },

  onPlanDropdownTouch(e) {
    this.clearPlanDropdownBlurTimer();
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

  onPlanOptionTouchEnd(e) {
    if (this.shouldBlockDropdownSelect()) {
      resetMoveState(this);
      return;
    }
    this.selectPlan(e);
  },

  onLogisticsOptionTouchEnd(e) {
    if (this.shouldBlockDropdownSelect()) {
      resetMoveState(this);
      return;
    }
    this.selectLogisticsCompany(e);
  },

  // ==================== 方案选择 ====================
  /** 条数少则容器随内容增高，超过上限再固定高度并允许内部滚动；勿用 onPageScroll 关列表（易与滚轮穿透误触） */
  refreshPlanDropdownMetrics() {
    const { showPlanDropdown, isLoadingPlans, filteredPlanList } = this.data;
    if (!showPlanDropdown) {
      return;
    }
    let planDropdownHeightRpx = PLAN_DROPDOWN_PLACEHOLDER_RPX;
    let planDropdownScrollable = false;
    if (isLoadingPlans || filteredPlanList.length === 0) {
      planDropdownHeightRpx = PLAN_DROPDOWN_PLACEHOLDER_RPX;
    } else {
      const contentRpx = filteredPlanList.length * PLAN_DROPDOWN_ROW_RPX;
      if (contentRpx > PLAN_DROPDOWN_MAX_RPX) {
        planDropdownHeightRpx = PLAN_DROPDOWN_MAX_RPX;
        planDropdownScrollable = true;
      } else {
        planDropdownHeightRpx = Math.max(contentRpx, PLAN_DROPDOWN_ROW_RPX);
      }
    }
    this.setData({ planDropdownHeightRpx, planDropdownScrollable });
  },

  dismissLogisticsSuggestNow() {
    this.clearLogisticsSuggestBlurTimer();
    if (this.data.showLogisticsSuggest) {
      this.setData({ showLogisticsSuggest: false });
    }
  },

  getPlanList(kw = '') {
    this.setData({ isLoadingPlans: true });
    fetchPlans(kw)
      .then((list = []) => {
        this.setData({
          planList: list,
          filteredPlanList: list,
          planIndex: -1,
          currentPlan: null,
          planSearchText: '',
          showPlanDropdown: false,
          isLoadingPlans: false,
          needLogin: false,
          schemeName: '',
          deliveryAddressText: '',
          giftDesc: '',
          quantity: 1,
          expressNo: '',
          orderNoMain: '',
          orderNoGift: '',
          imageUrls: [],
          signDate: '',
          signDateStr: '',
          remark: '',
          logisticsFilter: '',
          logisticsSelected: '',
          showLogisticsSuggest: false,
          logisticsSuggestList: [],
        });
      })
      .catch((err) => {
        if (isUnauthorizedError(err)) {
          this.setData({ isLoadingPlans: false, needLogin: true, planList: [], filteredPlanList: [] });
        } else {
          wx.showToast({ title: '获取方案失败', icon: 'none' });
          this.setData({ isLoadingPlans: false });
        }
      });
  },

  focusPlanInput() {
    this.clearPlanDropdownBlurTimer();
    resetMoveState(this);
    this.setData({ showPlanDropdown: true }, () => this.refreshPlanDropdownMetrics());
  },

  blurPlanInput() {
    this.schedulePlanDropdownHide();
  },

  onPlanSearchInput(e) {
    this.clearPlanDropdownBlurTimer();
    resetMoveState(this);
    const value = e.detail.value || '';
    this.setData({ planSearchText: value });
    if (this.data.searchTimer) clearTimeout(this.data.searchTimer);
    const kw = value.trim();
    if (!kw) {
      this.setData({ filteredPlanList: this.data.planList, showPlanDropdown: true }, () => {
        this.refreshPlanDropdownMetrics();
      });
      return;
    }
    const timer = setTimeout(() => {
      this.setData({ isLoadingPlans: true, showPlanDropdown: true }, () => {
        this.refreshPlanDropdownMetrics();
      });
      fetchPlans(kw)
        .then((list = []) => {
          this.setData({ filteredPlanList: list, isLoadingPlans: false }, () => {
            this.refreshPlanDropdownMetrics();
          });
        })
        .catch((err) => {
          if (isUnauthorizedError(err)) {
            this.setData({ needLogin: true, planList: [], filteredPlanList: [] });
          } else {
            wx.showToast({ title: '搜索失败', icon: 'none' });
          }
          this.setData({ isLoadingPlans: false }, () => {
            this.refreshPlanDropdownMetrics();
          });
        });
    }, 300);
    this.setData({ searchTimer: timer });
  },

  selectPlan(e) {
    if (this.shouldBlockDropdownSelect()) {
      resetMoveState(this);
      return;
    }
    resetMoveState(this);
    this.clearPlanDropdownBlurTimer();
    const index = Number(e.currentTarget.dataset.index);
    const planId = e.currentTarget.dataset.planId;
    const plan = this.data.filteredPlanList.find((item) => String(item.id) === String(planId))
      || this.data.filteredPlanList[index];
    if (!plan) {
      return;
    }
    this.setData({
      planIndex: index,
      currentPlan: plan,
      showPlanDropdown: false,
      planSearchText: plan.name,
      schemeName: plan.name,
      deliveryAddressText: plan.deliveryAddressText || '',
    });
  },

  clearPlanSelection() {
    this.clearPlanDropdownBlurTimer();
    this.setData({
      schemeName: '',
      currentPlan: null,
      planIndex: -1,
      planSearchText: '',
      deliveryAddressText: '',
      showPlanDropdown: true,
      filteredPlanList: this.data.planList,
    }, () => this.refreshPlanDropdownMetrics());
  },

  onGiftDescInput(e) {
    this.setData({ giftDesc: e.detail.value || '' });
  },

  onQuantityInput(e) {
    let v = parseInt(e.detail.value, 10);
    if (!Number.isFinite(v) || v < 1) v = 1;
    v = Math.floor(v);
    this.setData({ quantity: v });
  },

  incQuantity() {
    this.setData({ quantity: this.data.quantity + 1 });
  },

  decQuantity() {
    const next = Math.max(1, this.data.quantity - 1);
    this.setData({ quantity: next });
  },

  onExpressNoInput(e) {
    this.setData({ expressNo: e.detail.value || '' });
  },

  onLogisticsHelpTap() {
    wx.showModal({
      title: '说明',
      content: '收件人与「行情报单」中的寄件人共用同一信息。物流公司须从列表点选（可输入关键词筛选）；选填，填写完整后可在「我的订单」查看物流，否则仅显示快递单号。',
      showCancel: false,
      confirmText: '知道了',
    });
  },

  onSenderCellTap() {
    if (this.data.senderList.length === 0) {
      this.openSenderModal();
      return;
    }
    this.openSenderListPopup();
  },

  // ==================== 收件人地址簿 ====================
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
    this.setData(buildSenderViewState(list, selectedIndex, '选填'));
  },

  openSenderListPopup() {
    this.setData(buildOpenSenderListPopupState());
  },

  closeSenderListPopup() {
    this.setData(buildCloseSenderListPopupState());
  },

  selectSenderByIndex(e) {
    const index = parseInt(e.currentTarget.dataset.index, 10);
    this.setData(buildSenderSelectState(this.data.senderList, index, '选填'));
  },

  onDeleteSenderTap(e) {
    const index = parseInt(e.currentTarget.dataset.index, 10);
    const list = this.data.senderList || [];
    const sender = list[index];
    if (!sender) return;
    const name = (sender.name || '该').trim() || '该';
    wx.showModal({
      title: '确认删除',
      content: `是否删除${name}的收件人信息？（与行情报单地址簿同步删除）`,
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
              defaultText: '选填',
            });
            this.setData({
              ...senderState,
              showSenderListPopup: newList.length > 0,
            });
            wx.showToast({ title: '已删除', icon: 'success' });
            return false;
          })
          .then((isRemoteDeleted) => {
            if (!isRemoteDeleted) {
              return;
            }
            const sl = this.data.senderList;
            const senderState = buildSenderStateAfterDelete({
              selectedSenderIndex: this.data.selectedSenderIndex,
              deleteIndex: index,
              newList: sl,
              defaultText: '选填',
            });
            this.setData({
              selectedSenderIndex: senderState.selectedSenderIndex,
              selectedSenderDisplay: senderState.selectedSenderDisplay,
              showSenderListPopup: sl.length > 0,
            });
          })
          .catch(() => wx.showToast({ title: '删除失败', icon: 'none' }));
      },
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
    const senderInputError = validateSenderInput(name, phone, '姓名');
    if (senderInputError) {
      wx.showToast({ title: senderInputError, icon: 'none' });
      return;
    }
    const list = (this.data.senderList || []).slice();
    const exist = list.find((s) => (s.phone || '') === phone && (s.name || '') === name);
    if (exist) {
      this.setData({
        selectedSenderIndex: list.indexOf(exist) + 1,
        selectedSenderDisplay: formatSenderLabel({ name, phone }),
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
          ...buildSenderViewState(fallbackList, fallbackList.length, '选填'),
          selectedSenderDisplay: formatSenderLabel({ name, phone }),
        });
        return fallbackList;
      })
      .then((loaded) => {
        if (loaded && loaded.length) {
          const last = loaded[loaded.length - 1];
          this.setData({
            selectedSenderIndex: loaded.length,
            selectedSenderDisplay: formatSenderLabel(last),
          });
        }
      })
      .catch(() => wx.showToast({ title: '添加失败', icon: 'none' }));
  },

  // ==================== 物流信息 ====================
  onLogisticsCompanyInput(e) {
    this.clearLogisticsSuggestBlurTimer();
    resetMoveState(this);
    const raw = e.detail.value || '';
    const prevSel = (this.data.logisticsSelected || '').trim();
    if (prevSel && raw.trim() !== prevSel) {
      this.setData({ logisticsSelected: '' });
    }
    this.setData({ logisticsFilter: raw });
    this.fetchLogisticsSuggestList(raw.trim());
  },

  onLogisticsCompanyFocus() {
    this.clearLogisticsSuggestBlurTimer();
    resetMoveState(this);
    const v = (this.data.logisticsFilter || '').trim();
    this.fetchLogisticsSuggestList(v);
  },

  fetchLogisticsSuggestList(name) {
    getLogisticsCompanies(name)
      .then((res) => {
        this.setData({
          logisticsSuggestList: normalizeLogisticsSuggestList(res),
          showLogisticsSuggest: true,
        });
      })
      .catch(() => {
        this.setData({ logisticsSuggestList: [], showLogisticsSuggest: true });
      });
  },

  onLogisticsCompanyBlur() {
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

  selectLogisticsCompany(e) {
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

  onOrderNoMainInput(e) {
    this.setData({ orderNoMain: e.detail.value || '' });
  },

  onOrderNoGiftInput(e) {
    this.setData({ orderNoGift: e.detail.value || '' });
  },

  onSignDateChange(e) {
    const val = e.detail.value;
    this.setData({ signDate: val, signDateStr: val });
  },

  onRemarkInput(e) {
    this.setData({ remark: e.detail.value || '' });
  },

  // ==================== 图片上传 ====================
  chooseImage() {
    const that = this;
    wx.chooseMedia({
      count: 9 - this.data.imageUrls.length,
      mediaType: ['image'],
      sourceType: ['album', 'camera'],
      success(res) {
        const tempFiles = res.tempFiles || [];
        const urls = that.data.imageUrls.slice();
        let processed = 0;
        tempFiles.forEach((file) => {
          const size = file.size || 0;
          const path = file.tempFilePath;
          if (size > MAX_IMAGE_SIZE && path) {
            wx.compressImage({
              src: path,
              quality: COMPRESS_QUALITY,
              success(compressRes) {
                that.uploadOneImage(compressRes.tempFilePath, urls, tempFiles.length, processed);
              },
              fail() {
                that.uploadOneImage(path, urls, tempFiles.length, processed);
              },
            });
          } else {
            that.uploadOneImage(path, urls, tempFiles.length, processed);
          }
          processed++;
        });
      },
    });
  },

  uploadOneImage(filePath, urlList, total, index) {
    const plan = this.data.currentPlan;
    if (!plan || !plan.id) {
      wx.showToast({ title: '请先选择下单方案', icon: 'none' });
      return;
    }
    const token = getToken();
    const url = `${config.BASE_URL}/form-submissions/upload-image`;
    wx.uploadFile({
      url,
      filePath,
      name: 'file',
      formData: { schemeId: plan.id },
      header: token ? { Authorization: `Bearer ${token}` } : {},
      success: (res) => {
        try {
          const data = JSON.parse(res.data);
          if (data && data.code === 200 && data.data) {
            urlList.push(data.data);
            this.setData({ imageUrls: urlList });
          } else {
            wx.showToast({ title: data?.msg || '上传失败', icon: 'none' });
          }
        } catch (err) {
          wx.showToast({ title: '上传失败', icon: 'none' });
        }
      },
      fail: () => {
        wx.showToast({ title: '上传失败', icon: 'none' });
      },
    });
  },

  removeImage(e) {
    const index = e.currentTarget.dataset.index;
    const urls = this.data.imageUrls.slice();
    urls.splice(index, 1);
    this.setData({ imageUrls: urls });
  },

  // ==================== 提交/草稿 ====================
  buildSubmitData(status) {
    const {
      currentPlan,
      quantity,
      giftDesc,
      expressNo,
      orderNoMain,
      orderNoGift,
      imageUrls,
      signDate,
      remark,
      senderList,
      selectedSenderIndex,
      logisticsSelected,
    } = this.data;
    let senderName = '';
    let senderPhone = '';
    if (selectedSenderIndex > 0 && senderList && senderList[selectedSenderIndex - 1]) {
      const s = senderList[selectedSenderIndex - 1];
      senderName = (s.name || '').trim();
      senderPhone = (s.phone || '').trim();
    }
    return {
      schemeId: currentPlan ? currentPlan.id : null,
      quantity: quantity || 1,
      status,
      dataJson: {
        giftDesc: giftDesc || '',
        expressNo: expressNo || '',
        senderName,
        senderPhone,
        logisticsCompany: (logisticsSelected || '').trim(),
        orderNoMain: orderNoMain || '',
        orderNoGift: orderNoGift || '',
        imageUrls: imageUrls || [],
        signDate: signDate || '',
        remark: remark || '',
      },
    };
  },

  validateForSubmit() {
    const { currentPlan, expressNo, quantity } = this.data;
    if (!currentPlan) return '请选择下单方案';
    if (isEmptyValue(expressNo)) return '请填写快递单号';
    if (!Number.isFinite(quantity) || quantity < 1) return '下单数量至少为1';
    const pickErr = this.validateLogisticsPick();
    if (pickErr) return pickErr;
    return '';
  },

  /** 填写了筛选文字则必须已从列表点选；未填物流公司则视为不填（选填） */
  validateLogisticsPick() {
    const errorMessage = validateLogisticsSelected(this.data.logisticsFilter, this.data.logisticsSelected, false);
    if (!errorMessage) {
      return '';
    }
    return '请从列表中选择物流公司，或清空搜索文字';
  },

  async saveDraft() {
    const { currentPlan } = this.data;
    if (!currentPlan) {
      wx.showToast({ title: '请选择方案', icon: 'none' });
      return;
    }
    const pickErr = this.validateLogisticsPick();
    if (pickErr) {
      wx.showToast({ title: pickErr, icon: 'none' });
      return;
    }
    try {
      wx.showLoading({ title: '保存草稿中...' });
      const payload = this.buildSubmitData(0);
      const res = await saveFormSubmission(payload);
      wx.showToast({ title: '草稿保存成功', icon: 'success' });
      const id = res && (res.id !== undefined ? res.id : res);
      if (id != null) wx.setStorageSync('draftId', id);
    } catch (err) {
      wx.showToast({ title: err?.message || '草稿保存失败', icon: 'none' });
    } finally {
      wx.hideLoading();
    }
  },

  async submitForm() {
    const errMsg = this.validateForSubmit();
    if (errMsg) {
      wx.showToast({ title: errMsg, icon: 'none' });
      return;
    }
    const canSubmit = await ensurePaymentInfoBeforeSubmit();
    if (!canSubmit) {
      return;
    }
    try {
      wx.showLoading({ title: '提交中...' });
      const payload = this.buildSubmitData(1);
      const res = await saveFormSubmission(payload);
      wx.showToast({ title: '提交成功', icon: 'success' });
      const id = res && (res.id !== undefined ? res.id : res);
      if (id != null) wx.setStorageSync('submissionId', id);
      setTimeout(() => wx.navigateBack(), 1200);
    } catch (err) {
      wx.showToast({ title: err?.message || '提交失败', icon: 'none' });
    } finally {
      wx.hideLoading();
    }
  },
});
