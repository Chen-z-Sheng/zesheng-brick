'use strict';

const { getLegalDoc } = require('../../utils/legalDocs');

Page({
    data: {
        title: '',
        updateTime: '',
        sections: []
    },

    onLoad(options) {
        const type = options && options.type === 'privacy' ? 'privacy' : 'user';
        const doc = getLegalDoc(type);
        wx.setNavigationBarTitle({ title: doc.title });
        this.setData({
            title: doc.title,
            updateTime: doc.updateTime,
            sections: doc.sections
        });
    }
});
