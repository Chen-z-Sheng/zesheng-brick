package com.zesheng.admin.service;

import com.zesheng.common.dto.logistics.LogisticsTraceVo;

import java.util.List;

/**
 * 管理端查看报单物流轨迹
 */
public interface AdminLogisticsTraceService {

    List<LogisticsTraceVo> traceSellOrder(Long id);

    List<LogisticsTraceVo> traceFormSubmission(Long id);
}
