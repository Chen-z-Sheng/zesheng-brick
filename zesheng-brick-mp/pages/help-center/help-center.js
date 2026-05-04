// pages/help-center/help-center.js
const config = require('../../config/index');
const { get } = require('../../utils/request');
const { getToken } = require('../../utils/storage');

Page({
    data: {
        searchKeyword: '',
        faqList: [],
        filteredFaqList: [],
        expandedIds: [],
        faqLoading: true
    },

    onLoad() {
        this.loadFaq();
    },

    loadFaq() {
        this.setData({ faqLoading: true });
        get('/help-faq/list', {}, { showLoading: false })
            .then((list) => {
                if (Array.isArray(list)) {
                    const faqList = list.map(item => ({ ...item, expanded: false }));
                    this.setData({ faqList, filteredFaqList: faqList });
                } else {
                    this.setData({ faqList: [], filteredFaqList: [] });
                }
            })
            .catch((err) => {
                console.error('[help-center] 加载FAQ失败:', err);
                this.setData({ faqList: [], filteredFaqList: [] });
            })
            .finally(() => {
                this.setData({ faqLoading: false });
            });
    },

    onSearchInput(e) {
        const keyword = (e.detail.value || '').trim().toLowerCase();
        const faqList = this.data.faqList;
        const filtered = keyword
            ? faqList.filter(item =>
                (item.question || '').toLowerCase().includes(keyword) ||
                (item.answer || '').toLowerCase().includes(keyword)
            )
            : faqList;
        const expandedIds = this.data.expandedIds;
        const filteredFaqList = filtered.map(item => ({
            ...item,
            expanded: expandedIds.includes(item.id)
        }));
        this.setData({
            searchKeyword: e.detail.value,
            filteredFaqList
        });
    },

    toggleFaq(e) {
        const id = Number(e.currentTarget.dataset.id) || e.currentTarget.dataset.id;
        let expandedIds = [...this.data.expandedIds];
        const idx = expandedIds.indexOf(id);
        if (idx >= 0) {
            expandedIds.splice(idx, 1);
        } else {
            expandedIds.push(id);
        }
        const filteredFaqList = this.data.filteredFaqList.map(item => ({
            ...item,
            expanded: expandedIds.includes(item.id)
        }));
        this.setData({ expandedIds, filteredFaqList });
    },

    // 进入微信对话开放平台插件客服页（与 WXML 中「人工客服」按钮绑定）
    onContactHuman() {
        wx.navigateTo({
            url: '/pages/customer-service/customer-service'
        });
    },

    onGoFeedback() {
        wx.navigateTo({
            url: '/pages/feedback/feedback'
        });
    }
});
