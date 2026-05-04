const request = require('../utils/request');

// 获取方案列表
const fetchPlans = (keyword = '') => {
  const params = {};

  const kw = (keyword || '').trim();
  if (kw) {
    params.keyword = kw;
  }

  return request
    .get('/form-schemes', params)
    .then((res) => {
      const data = res?.data || res;
      return data || [];
    });
};

module.exports = {
  fetchPlans
};