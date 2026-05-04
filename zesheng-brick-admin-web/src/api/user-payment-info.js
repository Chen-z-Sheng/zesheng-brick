import request from '@/utils/request'

/**
 * 按小程序用户 ID 查询收款信息（打款用）
 * @param {number|string} userId
 */
export function getUserPaymentInfoByUserId(userId) {
  return request.get(`/admin/user-payment-info/by-user/${userId}`).then((res) => res.data)
}
