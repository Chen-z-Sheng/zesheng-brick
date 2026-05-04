/**
 * 用户寄件人地址簿（行情报单 / 固结报单共用，对应服务端 user_senders 表）
 */
const { request, get, post } = require('../utils/request');

/** 获取当前用户寄件人列表 */
function getSenderList() {
  return get('/user-senders');
}

/** 新增寄件人 */
function addSender(payload) {
  return post('/user-senders', payload);
}

/** 修改寄件人 */
function updateSender(id, payload) {
  return request({ url: `/user-senders/${id}`, method: 'PATCH', data: payload });
}

/** 删除寄件人 */
function deleteSender(id) {
  return request({ url: `/user-senders/${id}`, method: 'DELETE' });
}

module.exports = {
  getSenderList,
  addSender,
  updateSender,
  deleteSender
};
