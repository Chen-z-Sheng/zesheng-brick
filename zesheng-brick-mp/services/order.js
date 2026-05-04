const { get, post } = require('../utils/request');

/**
 * 若后端仍包了一层 R（code/msg/data），剥出真正的 data；兼容重复嵌套
 */
function unwrapApiData(payload) {
  if (payload == null || typeof payload !== 'object' || Array.isArray(payload)) {
    return payload;
  }
  const hasData = Object.prototype.hasOwnProperty.call(payload, 'data');
  const hasCodeOrMsg =
    Object.prototype.hasOwnProperty.call(payload, 'code') ||
    Object.prototype.hasOwnProperty.call(payload, 'msg');
  if (hasData && hasCodeOrMsg) {
    return unwrapApiData(payload.data);
  }
  return payload;
}

const submitOrder = (payload) => request.post('/orders', payload, { showLoading: true });

const saveFormSubmission = (payload) => {
  return post('/form-submissions', payload);
};

/** 提交行情报单（出售报单），返回提交记录 id */
const submitSellOrder = (payload) => request.post('/sell-order-submissions', payload);

/** 订单统计（固结+行情），返回 { total, shipped, storing, completed, exception } */
const getOrderStats = () => get('/my/order-stats');

/** 我的固结报单分页，statusTab: all|transit|storing|completed|exception */
const getFormSubmissionMyPage = (params) => get('/form-submissions/my-page', params);

/** 我的固结报单详情 */
const getFormSubmissionDetail = (id) => get(`/form-submissions/${id}`);

/** 我的行情报单分页 */
const getSellOrderMyPage = (params) => get('/sell-order-submissions/my-page', params);

/** 我的行情报单详情 */
const getSellOrderDetail = (id) => get(`/sell-order-submissions/${id}`);

/** 批量物流摘要：items 为 { type: 'form'|'sell', id } */
const postLogisticsSummaries = (body) =>
  post('/my/logistics-summaries', body, { showLoading: false })
    .then((raw) => {
      const map = unwrapApiData(raw);
      if (!map || typeof map !== 'object' || Array.isArray(map)) {
        return {};
      }
      const out = {};
      Object.keys(map).forEach((k) => {
        out[k] = unwrapApiData(map[k]);
      });
      return out;
    });

/** 固结报单物流轨迹 */
const getFormLogisticsTrace = (id) =>
  get(`/form-submissions/${id}/logistics-trace`, {}, { showLoading: false }).then(unwrapApiData);

/** 行情报单物流轨迹 */
const getSellLogisticsTrace = (id) =>
  get(`/sell-order-submissions/${id}/logistics-trace`, {}, { showLoading: false }).then(unwrapApiData);

module.exports = {
  submitOrder,
  saveFormSubmission,
  submitSellOrder,
  getOrderStats,
  getFormSubmissionMyPage,
  getFormSubmissionDetail,
  getSellOrderMyPage,
  getSellOrderDetail,
  postLogisticsSummaries,
  getFormLogisticsTrace,
  getSellLogisticsTrace
};


