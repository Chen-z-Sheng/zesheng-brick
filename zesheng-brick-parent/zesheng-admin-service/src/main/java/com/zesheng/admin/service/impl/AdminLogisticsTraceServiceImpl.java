package com.zesheng.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zesheng.admin.entity.FormSubmission;
import com.zesheng.admin.entity.LogisticsCompany;
import com.zesheng.admin.entity.SellOrderSubmission;
import com.zesheng.admin.mapper.FormSubmissionMapper;
import com.zesheng.admin.mapper.LogisticsCompanyMapper;
import com.zesheng.admin.mapper.SellOrderSubmissionMapper;
import com.zesheng.admin.service.AdminLogisticsTraceService;
import com.zesheng.common.dto.logistics.LogisticsTraceVo;
import com.zesheng.common.kuaidi100.Kuaidi100ClientUtil;
import com.zesheng.common.kuaidi100.Kuaidi100LogisticsQuerySupport;
import com.zesheng.common.kuaidi100.Kuaidi100TraceAssembler;
import com.zesheng.common.util.ExpressNoSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdminLogisticsTraceServiceImpl implements AdminLogisticsTraceService {

    private final LogisticsCompanyMapper logisticsCompanyMapper;
    private final SellOrderSubmissionMapper sellOrderSubmissionMapper;
    private final FormSubmissionMapper formSubmissionMapper;
    private final Kuaidi100ClientUtil kuaidi100ClientUtil;

    @Override
    public List<LogisticsTraceVo> traceSellOrder(Long id) {
        if (id == null) {
            return List.of(LogisticsTraceVo.fail("参数错误"));
        }
        SellOrderSubmission row = sellOrderSubmissionMapper.selectById(id);
        if (row == null) {
            return List.of(LogisticsTraceVo.fail("记录不存在"));
        }
        List<String> nos = ExpressNoSupport.splitStored(row.getLogisticsNo());
        return Kuaidi100LogisticsQuerySupport.queryTraceList(
                row.getLogisticsCompany(),
                nos,
                row.getSenderPhone(),
                this::resolveByCompanyName,
                kuaidi100ClientUtil);
    }

    @Override
    public List<LogisticsTraceVo> traceFormSubmission(Long id) {
        if (id == null) {
            return List.of(LogisticsTraceVo.fail("参数错误"));
        }
        FormSubmission row = formSubmissionMapper.selectById(id);
        if (row == null) {
            return List.of(LogisticsTraceVo.fail("记录不存在"));
        }
        List<String> expressNos = ExpressNoSupport.readExpressNosFromFormJson(row.getDataJson());
        String companyHint = extractLogisticsCompanyHint(row.getDataJson());
        return Kuaidi100LogisticsQuerySupport.queryTraceList(
                companyHint,
                expressNos,
                null,
                this::resolveByCompanyName,
                kuaidi100ClientUtil);
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
}
