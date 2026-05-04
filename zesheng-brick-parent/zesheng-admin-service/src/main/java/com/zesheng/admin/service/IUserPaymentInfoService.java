package com.zesheng.admin.service;

import com.zesheng.admin.entity.UserPaymentInfo;

/**
 * 用户收款信息查询（client_user_payment_info）
 */
public interface IUserPaymentInfoService {

    /**
     * 按小程序用户 ID 查询一条收款信息，无记录返回 null
     */
    UserPaymentInfo getByUserId(Long userId);
}
