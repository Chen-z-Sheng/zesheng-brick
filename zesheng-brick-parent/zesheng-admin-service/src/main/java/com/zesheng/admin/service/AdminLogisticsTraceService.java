package com.zesheng.admin.service;

import com.zesheng.common.dto.logistics.LogisticsTraceVo;

/**
 * 管理端查看报单物流轨迹
 */
public interface AdminLogisticsTraceService {

    LogisticsTraceVo traceSellOrder(Long id);

    LogisticsTraceVo traceFormSubmission(Long id);
}
