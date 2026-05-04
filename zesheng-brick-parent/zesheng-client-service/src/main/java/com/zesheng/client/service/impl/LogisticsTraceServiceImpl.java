package com.zesheng.client.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zesheng.client.entity.FormSubmission;
import com.zesheng.client.entity.LogisticsCompany;
import com.zesheng.client.entity.SellOrderSubmission;
import com.zesheng.client.mapper.FormSubmissionMapper;
import com.zesheng.client.mapper.LogisticsCompanyMapper;
import com.zesheng.client.mapper.SellOrderSubmissionMapper;
import com.zesheng.client.service.ILogisticsTraceService;
import com.zesheng.common.dto.logistics.LogisticsSummaryVo;
import com.zesheng.common.dto.logistics.LogisticsTraceVo;
import com.zesheng.common.kuaidi100.Kuaidi100ClientUtil;
import com.zesheng.common.kuaidi100.Kuaidi100LogisticsQuerySupport;
import com.zesheng.common.kuaidi100.Kuaidi100TraceAssembler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 根据字典表匹配物流公司编码，失败则走快递100智能识别
 */
@Service
@RequiredArgsConstructor
public class LogisticsTraceServiceImpl implements ILogisticsTraceService {

    private static final int BATCH_MAX = 30;

    private final LogisticsCompanyMapper logisticsCompanyMapper;
    private final SellOrderSubmissionMapper sellOrderSubmissionMapper;
    private final FormSubmissionMapper formSubmissionMapper;
    private final Kuaidi100ClientUtil kuaidi100ClientUtil;

    @Override
    public LogisticsTraceVo traceSellOrderForUser(Long userId, Long submissionId) {
        if (userId == null || submissionId == null) {
            return LogisticsTraceVo.fail("参数错误");
        }
        SellOrderSubmission row = sellOrderSubmissionMapper.selectById(submissionId);
        if (row == null || row.getDeletedAt() != null || !userId.equals(row.getUserId())) {
            return LogisticsTraceVo.fail("记录不存在或无权查看");
        }
        return Kuaidi100LogisticsQuerySupport.queryTrace(
                row.getLogisticsCompany(),
                row.getLogisticsNo(),
                row.getSenderPhone(),
                this::resolveByCompanyName,
                kuaidi100ClientUtil);
    }

    @Override
    public LogisticsTraceVo traceFormSubmissionForUser(Long userId, Long submissionId) {
        if (userId == null || submissionId == null) {
            return LogisticsTraceVo.fail("参数错误");
        }
        FormSubmission row = formSubmissionMapper.selectById(submissionId);
        if (row == null || row.getDeletedAt() != null || !userId.equals(row.getUserId())) {
            return LogisticsTraceVo.fail("记录不存在或无权查看");
        }
        String expressNo = extractExpressNo(row.getDataJson());
        String companyHint = extractLogisticsCompanyHint(row.getDataJson());
        String senderPhone = extractSenderPhone(row.getDataJson());
        return Kuaidi100LogisticsQuerySupport.queryTrace(
                companyHint,
                expressNo,
                senderPhone,
                this::resolveByCompanyName,
                kuaidi100ClientUtil);
    }

    @Override
    public Map<String, LogisticsSummaryVo> batchSummariesForUser(Long userId, List<LogisticsBatchItem> items) {
        Map<String, LogisticsSummaryVo> out = new LinkedHashMap<>();
        if (userId == null || items == null || items.isEmpty()) {
            return out;
        }
        int n = Math.min(items.size(), BATCH_MAX);
        for (int i = 0; i < n; i++) {
            LogisticsBatchItem it = items.get(i);
            if (it == null || it.id() == null || !StringUtils.hasText(it.type())) {
                continue;
            }
            String key = it.type().trim().toLowerCase() + "-" + it.id();
            LogisticsTraceVo full;
            if ("sell".equalsIgnoreCase(it.type())) {
                full = traceSellOrderForUser(userId, it.id());
            } else if ("form".equalsIgnoreCase(it.type())) {
                full = traceFormSubmissionForUser(userId, it.id());
            } else {
                LogisticsSummaryVo s = new LogisticsSummaryVo();
                s.setSuccess(false);
                s.setErrorMessage("不支持的类型");
                out.put(key, s);
                continue;
            }
            out.put(key, full.toSummary());
        }
        return out;
    }

    private Optional<String> resolveByCompanyName(String logisticsCompanyName) {
        if (!StringUtils.hasText(logisticsCompanyName)) {
            return Optional.empty();
        }
        String trimmed = logisticsCompanyName.trim();
        LogisticsCompany exact = logisticsCompanyMapper.selectOne(new LambdaQueryWrapper<LogisticsCompany>()
                .eq(LogisticsCompany::getStatus, true)
                .eq(LogisticsCompany::getName, trimmed)
                .last("LIMIT 1"));
        if (exact != null && StringUtils.hasText(exact.getCode())) {
            return Optional.of(Kuaidi100TraceAssembler.normalizeComCode(exact.getCode()));
        }
        List<LogisticsCompany> candidates = logisticsCompanyMapper.selectList(new LambdaQueryWrapper<LogisticsCompany>()
                .eq(LogisticsCompany::getStatus, true)
                .like(LogisticsCompany::getName, trimmed)
                .last("LIMIT 5"));
        if (candidates != null && candidates.size() == 1 && StringUtils.hasText(candidates.get(0).getCode())) {
            return Optional.of(Kuaidi100TraceAssembler.normalizeComCode(candidates.get(0).getCode()));
        }
        return Optional.empty();
    }

    private static String extractExpressNo(Map<String, Object> dataJson) {
        if (dataJson == null) {
            return "";
        }
        Object v = dataJson.get("expressNo");
        if (v == null) {
            v = dataJson.get("express_no");
        }
        if (v == null) {
            return "";
        }
        String s = String.valueOf(v).trim();
        return s;
    }

    private static String extractLogisticsCompanyHint(Map<String, Object> dataJson) {
        if (dataJson == null) {
            return null;
        }
        Object v = dataJson.get("expressCompany");
        if (v == null) {
            v = dataJson.get("logisticsCompany");
        }
        if (v == null) {
            return null;
        }
        String s = String.valueOf(v).trim();
        return s.isEmpty() ? null : s;
    }

    private static String extractSenderPhone(Map<String, Object> dataJson) {
        if (dataJson == null) {
            return null;
        }
        Object v = dataJson.get("senderPhone");
        if (v == null) {
            return null;
        }
        String s = String.valueOf(v).trim();
        return s.isEmpty() ? null : s;
    }
}
