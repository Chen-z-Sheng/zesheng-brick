package com.zesheng.client.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zesheng.client.entity.FormSubmission;
import com.zesheng.client.enums.SubmissionStatus;
import com.zesheng.client.mapper.FormSchemeClientMapper;
import com.zesheng.client.mapper.FormSubmissionMapper;
import com.zesheng.client.mapper.LogisticsCompanyMapper;
import com.zesheng.client.service.IFormSubmissionService;
import com.zesheng.common.response.R;
import com.zesheng.common.util.ExpressNoSupport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FormSubmissionServiceImpl implements IFormSubmissionService {

    private final FormSubmissionMapper formSubmissionMapper;
    private final FormSchemeClientMapper formSchemeClientMapper;
    private final LogisticsCompanyMapper logisticsCompanyMapper;

    @Override
    @Transactional(readOnly = true)
    public IPage<FormSubmission> pageMy(Long userId, int pageNum, int pageSize, String statusTab) {
        Page<FormSubmission> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<FormSubmission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FormSubmission::getUserId, userId);
        wrapper.isNull(FormSubmission::getDeletedAt);
        applyStatusTab(wrapper, statusTab);
        wrapper.orderByDesc(FormSubmission::getUpdatedAt);
        return formSubmissionMapper.selectPage(page, wrapper);
    }

    private void applyStatusTab(LambdaQueryWrapper<FormSubmission> wrapper, String statusTab) {
        List<SubmissionStatus> statusList = statusListForTab(statusTab);
        if (statusList.isEmpty()) {
            return;
        }
        wrapper.in(FormSubmission::getStatus, statusList);
    }

    private List<SubmissionStatus> statusListForTab(String statusTab) {
        if (statusTab == null || statusTab.isEmpty() || "all".equalsIgnoreCase(statusTab)) {
            return Arrays.asList(SubmissionStatus.SUBMITTED, SubmissionStatus.TRANSIT, SubmissionStatus.RECEIVED,
                    SubmissionStatus.STORING, SubmissionStatus.PAID, SubmissionStatus.EXCEPTION, SubmissionStatus.RETURNED);
        }
        switch (statusTab.toLowerCase()) {
            case "shipped":
                return Arrays.asList(SubmissionStatus.SUBMITTED, SubmissionStatus.TRANSIT);
            case "signed":
                return Collections.singletonList(SubmissionStatus.RECEIVED);
            case "transit":
                return Arrays.asList(SubmissionStatus.SUBMITTED, SubmissionStatus.TRANSIT, SubmissionStatus.RECEIVED);
            case "storing":
                return Collections.singletonList(SubmissionStatus.STORING);
            case "completed":
                return Arrays.asList(SubmissionStatus.PAID, SubmissionStatus.RETURNED);
            case "exception":
                return Collections.singletonList(SubmissionStatus.EXCEPTION);
            default:
                return Arrays.asList(SubmissionStatus.SUBMITTED, SubmissionStatus.TRANSIT, SubmissionStatus.RECEIVED,
                        SubmissionStatus.STORING, SubmissionStatus.PAID, SubmissionStatus.EXCEPTION, SubmissionStatus.RETURNED);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public long countMy(Long userId, String statusTab) {
        LambdaQueryWrapper<FormSubmission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FormSubmission::getUserId, userId);
        wrapper.isNull(FormSubmission::getDeletedAt);
        applyStatusTab(wrapper, statusTab);
        return formSubmissionMapper.selectCount(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<Long> save(Long userId, Long schemeId, Integer quantity, Integer status, java.util.Map<String, Object> dataJson) {
        Assert.notNull(userId, "用户未登录");
        Assert.notNull(schemeId, "方案不能为空");
        if (quantity == null || quantity < 1) {
            quantity = 1;
        }
        // 兼容旧客户端不传 status：默认按已提交
        if (status == null) {
            status = SubmissionStatus.SUBMITTED.getCode();
        }

        BigDecimal unitPrice = formSchemeClientMapper.getUnitPriceBySchemeId(schemeId);
        if (unitPrice == null) {
            return R.error("方案不存在或已停用");
        }

        SubmissionStatus submissionStatus = SubmissionStatus.getByCode(status);
        if (dataJson != null) {
            List<String> expressNos = ExpressNoSupport.readExpressNosFromFormJson(dataJson);
            ExpressNoSupport.writeExpressNosToFormJson(dataJson, expressNos);
        }
        FormSubmission entity = new FormSubmission();
        entity.setUserId(userId);
        entity.setSchemeId(schemeId);
        entity.setQuantity(quantity);
        entity.setStatus(submissionStatus);
        entity.setDataJson(dataJson);

        formSubmissionMapper.insert(entity);
        // 仅正式提交时统计物流公司热度；草稿不统计
        if (submissionStatus == SubmissionStatus.SUBMITTED && dataJson != null) {
            Object raw = dataJson.get("logisticsCompany");
            if (raw != null && StringUtils.hasText(String.valueOf(raw).trim())) {
                logisticsCompanyMapper.incrementSortByEnabledName(String.valueOf(raw).trim());
            }
        }
        return R.success(entity.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public FormSubmission getMyById(Long userId, Long id) {
        if (userId == null || id == null) return null;
        FormSubmission entity = formSubmissionMapper.selectOne(new LambdaQueryWrapper<FormSubmission>()
                .eq(FormSubmission::getId, id)
                .eq(FormSubmission::getUserId, userId)
                .isNull(FormSubmission::getDeletedAt));
        if (entity != null && entity.getSchemeId() != null) {
            String name = formSchemeClientMapper.getSchemeNameBySchemeId(entity.getSchemeId());
            if (name != null) entity.setSchemeName(name);
            BigDecimal unitPrice = formSchemeClientMapper.getUnitPriceBySchemeId(entity.getSchemeId());
            if (unitPrice != null) entity.setUnitPrice(unitPrice);
        }
        return entity;
    }
}
