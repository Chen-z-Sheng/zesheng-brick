const { get } = require('../utils/request');

// 获取物流公司列表
const getLogisticsCompanies = (name) => get('/logistics-company/list', { name });

module.exports = {
  getLogisticsCompanies
};
