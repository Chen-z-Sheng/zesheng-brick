package com.zesheng.client.service;

import com.zesheng.common.dto.logistics.LogisticsSummaryVo;
import com.zesheng.common.dto.logistics.LogisticsTraceVo;

import java.util.List;
import java.util.Map;

/**
 * C 端物流轨迹（快递100）
 */
public interface ILogisticsTraceService {

    LogisticsTraceVo traceSellOrderForUser(Long userId, Long submissionId);

    LogisticsTraceVo traceFormSubmissionForUser(Long userId, Long submissionId);

    Map<String, LogisticsSummaryVo> batchSummariesForUser(Long userId, List<LogisticsBatchItem> items);

    /**
     * 批量查询入参项
     */
    record LogisticsBatchItem(String type, Long id) {
    }
}
