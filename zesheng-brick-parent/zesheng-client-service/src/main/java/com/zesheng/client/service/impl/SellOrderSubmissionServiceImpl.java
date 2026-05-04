package com.zesheng.client.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zesheng.client.entity.SellOrderSubmission;
import com.zesheng.client.enums.SubmissionStatus;
import com.zesheng.client.mapper.LogisticsCompanyMapper;
import com.zesheng.client.mapper.SellOrderSubmissionMapper;
import com.zesheng.client.mapper.UserSenderMapper;
import com.zesheng.client.service.ISellOrderSubmissionService;
import com.zesheng.common.response.R;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 行情报单提交 Service 实现（单表，商品明细存 items_json）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SellOrderSubmissionServiceImpl implements ISellOrderSubmissionService {

    private final SellOrderSubmissionMapper sellOrderSubmissionMapper;
    private final UserSenderMapper userSenderMapper;
    private final LogisticsCompanyMapper logisticsCompanyMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<Long> submit(Long userId, Map<String, Object> payload) {
        if (userId == null) {
            return R.error("用户未登录");
        }
        if (payload == null || payload.isEmpty()) {
            return R.error("报单数据不能为空");
        }

        String senderName = extractString(payload, "sender", "name");
        String senderPhone = extractString(payload, "sender", "phone");
        String logisticsCompany = extractString(payload, "logistics", "company");
        String logisticsNo = extractString(payload, "logistics", "no");
        int storage = parseStorage(payload.get("storage"));
        String remark = payload.containsKey("remark") ? String.valueOf(payload.get("remark")).trim() : null;
        if (remark != null && remark.isEmpty()) {
            remark = null;
        }

        if (!StringUtils.hasText(senderName) || !StringUtils.hasText(senderPhone)) {
            return R.error("请填写寄件人姓名和手机号");
        }
        if (!StringUtils.hasText(logisticsCompany) || !StringUtils.hasText(logisticsNo)) {
            return R.error("请填写物流公司和寄件单号");
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> itemsList = (List<Map<String, Object>>) payload.get("items");
        if (itemsList == null || itemsList.isEmpty()) {
            return R.error("请添加出售商品");
        }

        List<Map<String, Object>> itemsJson = new ArrayList<>();
        for (Map<String, Object> row : itemsList) {
            String name = row.get("name") != null ? String.valueOf(row.get("name")).trim() : "";
            if (!StringUtils.hasText(name)) continue;
            BigDecimal price = toBigDecimal(row.get("price"));
            int quantity = toInt(row.get("quantity"), 1);
            if (quantity < 1) quantity = 1;
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("productName", name);
            item.put("price", price);
            item.put("quantity", quantity);
            itemsJson.add(item);
        }
        if (itemsJson.isEmpty()) {
            return R.error("请添加出售商品");
        }

        SellOrderSubmission main = new SellOrderSubmission();
        main.setUserId(userId);
        main.setSenderName(senderName.trim());
        main.setSenderPhone(senderPhone.trim());
        main.setLogisticsCompany(logisticsCompany.trim());
        main.setLogisticsNo(logisticsNo.trim());
        main.setStorage(storage);
        main.setStorageDate(storage == 1 ? LocalDate.now() : null);
        main.setRemark(remark);
        main.setItemsJson(itemsJson);
        main.setStatus(SubmissionStatus.SUBMITTED.getCode());

        sellOrderSubmissionMapper.insert(main);
        userSenderMapper.incrementUseCount(userId, senderName.trim(), senderPhone.trim());
        // 与字典名称完全一致时累加全局 sort，便于常用物流靠前
        logisticsCompanyMapper.incrementSortByEnabledName(logisticsCompany.trim());
        return R.success(main.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public IPage<SellOrderSubmission> pageMy(Long userId, int pageNum, int pageSize, String statusTab) {
        Page<SellOrderSubmission> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SellOrderSubmission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SellOrderSubmission::getUserId, userId);
        wrapper.isNull(SellOrderSubmission::getDeletedAt);
        applyStatusTab(wrapper, statusTab);
        wrapper.orderByDesc(SellOrderSubmission::getUpdatedAt);
        return sellOrderSubmissionMapper.selectPage(page, wrapper);
    }

    private void applyStatusTab(LambdaQueryWrapper<SellOrderSubmission> wrapper, String statusTab) {
        List<Integer> codes = statusCodesForTab(statusTab);
        if (codes.isEmpty()) {
            return;
        }
        wrapper.in(SellOrderSubmission::getStatus, codes);
    }

    private List<Integer> statusCodesForTab(String statusTab) {
        if (statusTab == null || statusTab.isEmpty() || "all".equalsIgnoreCase(statusTab)) {
            return Arrays.asList(1, 2, 7, 3, 4, 5, 6);
        }
        switch (statusTab.toLowerCase()) {
            case "shipped":
                return Arrays.asList(1, 2);
            case "signed":
                return Collections.singletonList(7);
            case "transit":
                return Arrays.asList(1, 2, 7);
            case "storing":
                return Collections.singletonList(3);
            case "completed":
                return Arrays.asList(4, 6);
            case "exception":
                return Collections.singletonList(5);
            default:
                return Arrays.asList(1, 2, 7, 3, 4, 5, 6);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public long countMy(Long userId, String statusTab) {
        LambdaQueryWrapper<SellOrderSubmission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SellOrderSubmission::getUserId, userId);
        wrapper.isNull(SellOrderSubmission::getDeletedAt);
        applyStatusTab(wrapper, statusTab);
        return sellOrderSubmissionMapper.selectCount(wrapper);
    }

    @Override
    @Transactional(readOnly = true)
    public SellOrderSubmission getMyById(Long userId, Long id) {
        if (userId == null || id == null) return null;
        return sellOrderSubmissionMapper.selectOne(new LambdaQueryWrapper<SellOrderSubmission>()
                .eq(SellOrderSubmission::getId, id)
                .eq(SellOrderSubmission::getUserId, userId)
                .isNull(SellOrderSubmission::getDeletedAt));
    }

    private static int parseStorage(Object v) {
        if (v == null) return 0;
        if (v instanceof Number) return ((Number) v).intValue() != 0 ? 1 : 0;
        if ("是".equals(String.valueOf(v).trim())) return 1;
        return 0;
    }

    private static String extractString(Map<String, Object> payload, String key, String subKey) {
        Object obj = payload.get(key);
        if (!(obj instanceof Map)) return null;
        Object v = ((Map<?, ?>) obj).get(subKey);
        return v != null ? String.valueOf(v).trim() : null;
    }

    private static BigDecimal toBigDecimal(Object o) {
        if (o instanceof Number) return BigDecimal.valueOf(((Number) o).doubleValue());
        if (o instanceof String) {
            try {
                return new BigDecimal((String) o);
            } catch (NumberFormatException e) {
                return BigDecimal.ZERO;
            }
        }
        return BigDecimal.ZERO;
    }

    private static int toInt(Object o, int defaultVal) {
        if (o instanceof Number) return ((Number) o).intValue();
        if (o instanceof String) {
            try {
                return Integer.parseInt((String) o);
            } catch (NumberFormatException e) {
                return defaultVal;
            }
        }
        return defaultVal;
    }
}
